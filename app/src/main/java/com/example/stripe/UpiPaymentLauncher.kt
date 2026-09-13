package com.example.stripe

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.result.ActivityResult
import com.example.data.OnlineBackendManager
import com.example.model.SubscriptionPlan

object UpiPaymentLauncher {

  const val PACKAGE_GPAY = "com.google.android.apps.nbu.paisa.user"
  const val PACKAGE_PHONEPE = "com.phonepe.app"
  const val PACKAGE_PAYTM = "net.one97.paytm"
  const val PACKAGE_BHIM = "in.org.npci.upiapp"
  const val PACKAGE_CRED = "com.dreamplug.androidapp"
  const val PACKAGE_AMAZON = "in.amazon.mShop.android.shopping"

  fun getPackageName(appName: String): String? {
    val clean = appName.trim().lowercase()
    return when {
      clean.contains("gpay") || clean.contains("google pay") -> PACKAGE_GPAY
      clean.contains("phonepe") -> PACKAGE_PHONEPE
      clean.contains("paytm") -> PACKAGE_PAYTM
      clean.contains("bhim") -> PACKAGE_BHIM
      clean.contains("cred") -> PACKAGE_CRED
      clean.contains("amazon") -> PACKAGE_AMAZON
      else -> null
    }
  }

  fun buildUpiUri(
    plan: SubscriptionPlan,
    payeeVpa: String = "pragadheeeesh95@okaxis",
    payeeName: String = "Pragadheesh YALPS Lossless",
    transactionRef: String = "YALPS" + (100000..999999).random()
  ): Uri {
    val amountFormatted = String.format(java.util.Locale.US, "%.2f", plan.priceInr.toDouble())
    return Uri.Builder()
      .scheme("upi")
      .authority("pay")
      .appendQueryParameter("pa", payeeVpa)
      .appendQueryParameter("pn", payeeName)
      .appendQueryParameter("tid", "TXN" + System.currentTimeMillis())
      .appendQueryParameter("tr", transactionRef)
      .appendQueryParameter("tn", "YALPS ${plan.name} Lossless Audio Subscription")
      .appendQueryParameter("am", amountFormatted)
      .appendQueryParameter("cu", "INR")
      .build()
  }

  /**
   * Dispatches the payment intent directly to the selected UPI app (GPay, PhonePe, Paytm, BHIM, etc.)
   * Pre-filling the exact plan amount (₹30, ₹50, ₹100), payee VPA, and reference note.
   */
  fun launchUpiPayment(
    context: Context,
    plan: SubscriptionPlan,
    upiApp: String,
    launcher: ManagedActivityResultLauncher<Intent, ActivityResult>?,
    onCompleted: (Boolean, String) -> Unit
  ) {
    val backendManager = OnlineBackendManager.getInstance()
    val activeBank = backendManager.primaryBankAccount.value
      ?: backendManager.bankAccounts.value.firstOrNull { it.isPrimary }
      ?: backendManager.bankAccounts.value.firstOrNull()
    val backendConfig = backendManager.backendConfig.value

    val payeeVpa = activeBank?.upiVpa?.ifBlank { null }
      ?: backendConfig.creatorUpiVpa.ifEmpty { "pragadheeeesh95@okaxis" }
    val payeeName = activeBank?.accountHolderName?.ifBlank { null }
      ?: backendConfig.creatorBeneficiaryName.ifEmpty { "Pragadheesh YALPS" }
    val transactionRef = "YALPSADM" + (100000..999999).random()

    val upiUri = buildUpiUri(
      plan = plan,
      payeeVpa = payeeVpa,
      payeeName = payeeName,
      transactionRef = transactionRef
    )

    val targetPackage = getPackageName(upiApp)
    val intent = Intent(Intent.ACTION_VIEW, upiUri).apply {
      if (targetPackage != null) {
        setPackage(targetPackage)
      }
    }

    try {
      if (launcher != null) {
        launcher.launch(intent)
      } else {
        context.startActivity(intent)
      }
    } catch (e: ActivityNotFoundException) {
      // If targeted app is not installed on this device, launch general UPI app chooser
      try {
        val chooser = Intent.createChooser(Intent(Intent.ACTION_VIEW, upiUri), "Complete ₹${plan.priceInr} Payment via UPI")
        if (launcher != null) {
          launcher.launch(chooser)
        } else {
          context.startActivity(chooser)
        }
      } catch (e2: Exception) {
        Toast.makeText(
          context,
          "No external UPI app found on device. Please use Razorpay Dynamic QR or enter UPI ID.",
          Toast.LENGTH_LONG
        ).show()
        onCompleted(false, "No UPI application found. Please complete transaction via Razorpay Dynamic QR to activate subscription.")
      }
    } catch (e: Exception) {
      onCompleted(false, "Unable to launch $upiApp: ${e.localizedMessage ?: "Unknown error"}. Transaction not completed.")
    }
  }

  /**
   * Processes the intent callback result from the external UPI application.
   * STRICT: Only approves access if the response explicitly contains SUCCESS or approval.
   */
  fun handleActivityResult(
    result: ActivityResult,
    plan: SubscriptionPlan,
    upiApp: String,
    onSuccess: (String) -> Unit,
    onFailure: (String) -> Unit
  ) {
    val data = result.data
    val response = data?.getStringExtra("response") ?: data?.dataString ?: ""

    // UPI responses return "Status=SUCCESS&txnId=...&responseCode=00&ApprovalRefNo=..."
    if (response.contains("Status=SUCCESS", ignoreCase = true) ||
        (result.resultCode == Activity.RESULT_OK && response.contains("SUCCESS", ignoreCase = true))
    ) {
      StripePaymentsManager.getInstance().switchSubscriptionTierWithUpi(
        targetPlan = plan,
        upiId = "listener@okaxis",
        upiApp = upiApp,
        onSuccess = onSuccess
      )
    } else if (response.contains("FAIL", ignoreCase = true) || result.resultCode == Activity.RESULT_CANCELED) {
      onFailure("Payment was cancelled or failed in $upiApp. Music access remains locked until transaction is completed.")
    } else {
      // Unconfirmed or user pressed back without completing
      onFailure("Transaction not confirmed by bank. Music access remains locked.")
    }
  }
}
