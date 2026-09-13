package com.example.stripe

import android.content.Context
import android.content.SharedPreferences
import com.example.auth.UserSessionManager
import com.example.data.OnlineBackendManager
import com.example.data.TamilSongCatalog
import com.example.model.StripeCardInfo
import com.example.model.StripeCustomerState
import com.example.model.StripeInvoice
import com.example.model.SubscriptionPlan
import com.example.model.UpiPaymentInfo
import com.example.notion.NotionAnalyticsManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

data class ActiveDeviceSession(
  val id: String,
  val deviceName: String,
  val platform: String,
  val audioStreamFormat: String,
  val lastActive: String,
  val isCurrentDevice: Boolean = false
)

class StripePaymentsManager private constructor() {

  companion object {
    @Volatile
    private var instance: StripePaymentsManager? = null

    fun getInstance(): StripePaymentsManager {
      return instance ?: synchronized(this) {
        instance ?: StripePaymentsManager().also { instance = it }
      }
    }
  }

  private val scope = CoroutineScope(Dispatchers.Main)
  private val dateFormat = SimpleDateFormat("MMM d, yyyy", Locale.getDefault())
  private var sharedPrefs: SharedPreferences? = null

  private val _customerState = MutableStateFlow(StripeCustomerState())
  val customerState: StateFlow<StripeCustomerState> = _customerState.asStateFlow()

  private val _activeSessions = MutableStateFlow<List<ActiveDeviceSession>>(
    listOf(
      ActiveDeviceSession(
        id = "dev_01",
        deviceName = "Pixel 9 Pro Studio",
        platform = "Android 15 (Direct Bit-Perfect)",
        audioStreamFormat = "FLAC 96kHz / 24-bit",
        lastActive = "Active Now",
        isCurrentDevice = true
      ),
      ActiveDeviceSession(
        id = "dev_02",
        deviceName = "Living Room Topping E50 DAC",
        platform = "Audiophile Endpoint / USB DAC",
        audioStreamFormat = "Dolby Atmos 7.1.4",
        lastActive = "5 mins ago",
        isCurrentDevice = false
      )
    )
  )
  val activeSessions: StateFlow<List<ActiveDeviceSession>> = _activeSessions.asStateFlow()

  private val _isProcessingPayment = MutableStateFlow(false)
  val isProcessingPayment: StateFlow<Boolean> = _isProcessingPayment.asStateFlow()

  fun initialize(context: Context) {
    if (sharedPrefs == null) {
      sharedPrefs = context.applicationContext.getSharedPreferences("yalps_stripe_prefs", Context.MODE_PRIVATE)
      loadState()
    }
  }

  private fun loadState() {
    val prefs = sharedPrefs ?: return
    val activeTier = prefs.getString("active_tier_id", "") ?: ""
    val status = prefs.getString("status", "Inactive") ?: "Inactive"
    val paymentMethodType = prefs.getString("payment_method_type", "NONE") ?: "NONE"
    val cardLast4 = prefs.getString("card_last4", "4242") ?: "4242"
    val upiId = prefs.getString("upi_id", "")
    val upiApp = prefs.getString("upi_app", "Google Pay") ?: "Google Pay"

    val upiInfo = if (!upiId.isNullOrEmpty()) {
      UpiPaymentInfo(upiId = upiId, upiApp = upiApp)
    } else null

    _customerState.update { current ->
      current.copy(
        activeTierId = activeTier,
        status = status,
        activePaymentMethodType = paymentMethodType,
        defaultCard = current.defaultCard.copy(last4 = cardLast4),
        defaultUpi = upiInfo ?: current.defaultUpi
      )
    }
  }

  private fun saveState() {
    val prefs = sharedPrefs ?: return
    val state = _customerState.value
    prefs.edit()
      .putString("active_tier_id", state.activeTierId)
      .putString("status", state.status)
      .putString("payment_method_type", state.activePaymentMethodType)
      .putString("card_last4", state.defaultCard.last4)
      .putString("upi_id", state.defaultUpi?.upiId ?: "")
      .putString("upi_app", state.defaultUpi?.upiApp ?: "Google Pay")
      .apply()
  }

  fun getPlanById(planId: String): SubscriptionPlan {
    return TamilSongCatalog.subscriptionPlans.find { it.id == planId }
      ?: TamilSongCatalog.subscriptionPlans[1]
  }

  fun switchSubscriptionTier(
    targetPlan: SubscriptionPlan,
    cardNumber: String = "4242",
    onSuccess: (String) -> Unit
  ) {
    scope.launch {
      _isProcessingPayment.value = true
      delay(1200) // Simulate Stripe PaymentIntent confirmation & Webhook response

      val newInvoice = StripeInvoice(
        id = "in_" + UUID.randomUUID().toString().take(8),
        date = dateFormat.format(Date()),
        amountFormatted = "₹${targetPlan.priceInr}.00",
        status = "Paid",
        paymentMethod = "Card •••• ${cardNumber.filter { it.isDigit() }.takeLast(4).ifEmpty { "4242" }}",
        upiRefId = null
      )

      _customerState.update { current ->
        current.copy(
          activeTierId = targetPlan.id,
          status = "Active",
          defaultCard = current.defaultCard.copy(last4 = cardNumber.filter { it.isDigit() }.takeLast(4).ifEmpty { "4242" }),
          activePaymentMethodType = "CARD",
          nextBillingDate = "October 9, 2026",
          invoices = listOf(newInvoice) + current.invoices
        )
      }
      saveState()
      UserSessionManager.getInstance().setProMembership(true)

      // Record real-time Backend Admin payment settlement
      val activeBank = OnlineBackendManager.getInstance().primaryBankAccount.value
      val activeBankName = activeBank?.bankName ?: "Backend Admin Bank"

      OnlineBackendManager.getInstance().recordRealtimePayment(
        plan = targetPlan,
        paymentMethod = "STRIPE_CARD",
        amountInr = targetPlan.priceInr,
        payerReference = "CARD-••••${cardNumber.filter { it.isDigit() }.takeLast(4).ifEmpty { "4242" }}",
        payerEmail = "listener@yalps-audio.io"
      )

      // Log event to Notion
      NotionAnalyticsManager.getInstance().logPlanUpgraded(targetPlan.name, targetPlan.priceInr)

      _isProcessingPayment.value = false
      onSuccess("Stripe Card payment of ₹${targetPlan.priceInr}.00 successful! Credited directly to Backend Admin's registered bank account ($activeBankName) & subscription updated to ${targetPlan.name}.")
    }
  }

  fun switchSubscriptionTierWithUpi(
    targetPlan: SubscriptionPlan,
    upiId: String,
    upiApp: String,
    onSuccess: (String) -> Unit
  ) {
    scope.launch {
      _isProcessingPayment.value = true
      delay(1400) // Simulate UPI Intent / NPCI Settlement & Autopay Mandate

      val cleanUpi = upiId.trim().ifEmpty { "yalps.listener@okaxis" }
      val randomUtr = "4" + (10000000000L..99999999999L).random().toString()
      val newInvoice = StripeInvoice(
        id = "in_upi_" + UUID.randomUUID().toString().take(8),
        date = dateFormat.format(Date()),
        amountFormatted = "₹${targetPlan.priceInr}.00",
        status = "Paid",
        paymentMethod = "UPI • $upiApp",
        upiRefId = "UTR-$randomUtr"
      )

      val upiInfo = UpiPaymentInfo(
        upiId = cleanUpi,
        upiApp = upiApp,
        utrNumber = randomUtr,
        vpaHandle = if (cleanUpi.contains("@")) "@" + cleanUpi.substringAfter("@") else "@okaxis",
        isVerified = true
      )

      _customerState.update { current ->
        current.copy(
          activeTierId = targetPlan.id,
          status = "Active",
          defaultUpi = upiInfo,
          activePaymentMethodType = "UPI",
          nextBillingDate = "October 9, 2026",
          invoices = listOf(newInvoice) + current.invoices
        )
      }
      saveState()
      UserSessionManager.getInstance().setProMembership(true)

      // Record real-time Backend Admin payment settlement
      val activeBank = OnlineBackendManager.getInstance().primaryBankAccount.value
      val activeBankName = activeBank?.bankName ?: "Backend Admin Bank"

      OnlineBackendManager.getInstance().recordRealtimePayment(
        plan = targetPlan,
        paymentMethod = "UPI_$upiApp",
        amountInr = targetPlan.priceInr,
        payerReference = "UTR-$randomUtr",
        payerEmail = cleanUpi
      )

      // Log event to Notion
      NotionAnalyticsManager.getInstance().logPlanUpgraded("${targetPlan.name} (UPI: $upiApp)", targetPlan.priceInr)

      _isProcessingPayment.value = false
      onSuccess("UPI Payment of ₹${targetPlan.priceInr}.00 authorized via $upiApp!\nNPCI UTR Ref: $randomUtr\nDirectly credited to Backend Admin's registered bank account ($activeBankName) & Lossless plan ${targetPlan.name} is now active.")
    }
  }

  fun revokeDeviceSession(sessionId: String) {
    _activeSessions.update { list ->
      list.filterNot { it.id == sessionId }
    }
    _customerState.update { it.copy(activeDevicesCount = _activeSessions.value.size) }
  }

  fun updatePaymentCard(brand: String, last4: String) {
    _customerState.update { current ->
      current.copy(
        defaultCard = StripeCardInfo(
          brand = brand,
          last4 = last4,
          expMonth = 10,
          expYear = 2029,
          isDefault = true
        )
      )
    }
    saveState()
  }
}
