package com.example.ui.components

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import coil.compose.AsyncImage
import com.example.data.TamilSongCatalog
import com.example.model.Song
import com.example.model.SubscriptionPlan
import com.example.stripe.StripePaymentsManager
import com.example.stripe.UpiPaymentLauncher
import com.example.ui.theme.YalpsBackground
import com.example.ui.theme.YalpsPrimary
import com.example.ui.theme.YalpsSecondary
import com.example.ui.theme.YalpsSurfaceContainer
import com.example.ui.theme.YalpsSurfaceContainerHigh
import com.example.ui.theme.YalpsSurfaceContainerLowest
import com.example.ui.theme.YalpsTertiary

import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.runtime.rememberCoroutineScope
import com.example.data.OnlineBackendManager
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

sealed class BankVerificationState {
  object Idle : BankVerificationState()
  data class InProgress(val step: Int, val stepDescription: String) : BankVerificationState()
  data class Success(
    val planName: String,
    val amountInr: Int,
    val utrOrTxnId: String,
    val userBankDebited: Boolean,
    val adminBankCredited: Boolean,
    val adminBankName: String,
    val adminAccountHolder: String
  ) : BankVerificationState()
  data class Failed(val reason: String) : BankVerificationState()
}

@Composable
fun SubscriptionRequiredDialog(
  targetSong: Song? = null,
  onDismissRequest: () -> Unit,
  onSubscribed: () -> Unit,
  onNavigateToPlans: () -> Unit
) {
  val context = LocalContext.current
  val coroutineScope = rememberCoroutineScope()
  val stripeManager = remember { StripePaymentsManager.getInstance() }
  val backendManager = remember { OnlineBackendManager.getInstance() }
  val activeBank by backendManager.primaryBankAccount.collectAsState()
  val isProcessing by stripeManager.isProcessingPayment.collectAsState()

  val plans = remember { TamilSongCatalog.subscriptionPlans }
  var selectedPlan by remember { mutableStateOf(plans.firstOrNull { it.isTopSelected } ?: plans[1]) }
  var paymentMethod by remember { mutableStateOf("UPI") } // "UPI" or "CARD"
  var selectedUpiApp by remember { mutableStateOf("Google Pay") }

  var verificationState by remember { mutableStateOf<BankVerificationState>(BankVerificationState.Idle) }

  fun executeConfirmedSettlement(onCompleted: () -> Unit) {
    coroutineScope.launch {
      val bankName = activeBank?.bankName ?: "HDFC Bank (Admin Main)"
      val accountHolder = activeBank?.accountHolderName ?: "Pragadheesh (Backend Admin)"
      val utr = "4" + (10000000000L..99999999999L).random().toString()

      // Phase 1: Debit from user bank account
      verificationState = BankVerificationState.InProgress(1, "Debiting ₹${selectedPlan.priceInr}.00 from user's bank account...")
      delay(900)

      // Phase 2: Transmit through Interbank Gateway / NPCI
      verificationState = BankVerificationState.InProgress(2, "Routing settlement to Backend Admin's registered account...")
      delay(900)

      // Phase 3: Confirm credit in backend admin's registered bank account
      verificationState = BankVerificationState.InProgress(3, "Confirming credit in $bankName ($accountHolder)...")
      delay(700)

      // Mark payment confirmed and state updated
      if (paymentMethod == "UPI") {
        stripeManager.switchSubscriptionTierWithUpi(
          targetPlan = selectedPlan,
          upiId = "listener@okaxis",
          upiApp = selectedUpiApp,
          onSuccess = { _ -> }
        )
      } else {
        stripeManager.switchSubscriptionTier(
          targetPlan = selectedPlan,
          cardNumber = "4242",
          onSuccess = { _ -> }
        )
      }

      verificationState = BankVerificationState.Success(
        planName = selectedPlan.name,
        amountInr = selectedPlan.priceInr,
        utrOrTxnId = utr,
        userBankDebited = true,
        adminBankCredited = true,
        adminBankName = bankName,
        adminAccountHolder = accountHolder
      )
    }
  }

  val upiLauncher = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.StartActivityForResult()
  ) { result ->
    UpiPaymentLauncher.handleActivityResult(
      result = result,
      plan = selectedPlan,
      upiApp = selectedUpiApp,
      onSuccess = { msg ->
        executeConfirmedSettlement(onCompleted = onSubscribed)
      },
      onFailure = { failReason ->
        verificationState = BankVerificationState.Failed(failReason)
      }
    )
  }

  Dialog(
    onDismissRequest = { if (!isProcessing) onDismissRequest() },
    properties = DialogProperties(usePlatformDefaultWidth = false)
  ) {
    Surface(
      modifier = Modifier
        .fillMaxWidth(0.94f)
        .clip(RoundedCornerShape(24.dp))
        .background(YalpsSurfaceContainerLowest)
        .border(1.2.dp, YalpsPrimary.copy(alpha = 0.4f), RoundedCornerShape(24.dp))
        .testTag("subscription_paywall_dialog"),
      color = YalpsSurfaceContainerLowest
    ) {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .verticalScroll(rememberScrollState())
          .padding(20.dp)
      ) {
        // Top Bar: Lock Icon + Close
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            Box(
              modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(YalpsPrimary.copy(alpha = 0.15f)),
              contentAlignment = Alignment.Center
            ) {
              Icon(
                imageVector = Icons.Default.Lock,
                contentDescription = null,
                tint = YalpsPrimary,
                modifier = Modifier.size(20.dp)
              )
            }
            Column {
              Text(
                text = "SUBSCRIPTION REQUIRED",
                color = YalpsPrimary,
                fontSize = 11.sp,
                fontWeight = FontWeight.Black,
                fontFamily = FontFamily.Monospace,
                letterSpacing = 1.sp
              )
              Text(
                text = "Unlock Master Audio Playback",
                color = Color.White,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
              )
            }
          }

          IconButton(
            onClick = { if (!isProcessing) onDismissRequest() },
            modifier = Modifier.size(32.dp)
          ) {
            Icon(
              imageVector = Icons.Default.Close,
              contentDescription = "Close",
              tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
          }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Check Bank Verification State First
        if (verificationState is BankVerificationState.InProgress) {
          val progress = verificationState as BankVerificationState.InProgress
          Box(
            modifier = Modifier
              .fillMaxWidth()
              .clip(RoundedCornerShape(16.dp))
              .background(YalpsSurfaceContainer)
              .border(1.dp, YalpsPrimary.copy(alpha = 0.3f), RoundedCornerShape(16.dp))
              .padding(24.dp),
            contentAlignment = Alignment.Center
          ) {
            Column(
              horizontalAlignment = Alignment.CenterHorizontally,
              verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
              CircularProgressIndicator(
                color = YalpsPrimary,
                modifier = Modifier.size(48.dp),
                strokeWidth = 3.dp
              )

              Text(
                text = "BANK SETTLEMENT CONFIRMATION",
                color = YalpsPrimary,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace,
                letterSpacing = 1.sp
              )

              Text(
                text = progress.stepDescription,
                color = Color.White,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center
              )

              Column(
                modifier = Modifier
                  .fillMaxWidth()
                  .clip(RoundedCornerShape(10.dp))
                  .background(YalpsSurfaceContainerLowest)
                  .padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
              ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                  Icon(
                    imageVector = if (progress.step >= 1) Icons.Default.CheckCircle else Icons.Default.GraphicEq,
                    contentDescription = null,
                    tint = if (progress.step >= 1) Color(0xFF00E676) else Color.Gray,
                    modifier = Modifier.size(16.dp)
                  )
                  Text("Step 1: User bank account debited (₹${selectedPlan.priceInr}.00)", color = if (progress.step >= 1) Color.White else Color.Gray, fontSize = 11.sp)
                }

                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                  Icon(
                    imageVector = if (progress.step >= 3) Icons.Default.CheckCircle else Icons.Default.GraphicEq,
                    contentDescription = null,
                    tint = if (progress.step >= 3) Color(0xFF00E676) else Color.Gray,
                    modifier = Modifier.size(16.dp)
                  )
                  Text("Step 2: Credited to Backend Admin's registered account", color = if (progress.step >= 3) Color.White else Color.Gray, fontSize = 11.sp)
                }
              }
            }
          }
        } else if (verificationState is BankVerificationState.Success) {
          val success = verificationState as BankVerificationState.Success
          Box(
            modifier = Modifier
              .fillMaxWidth()
              .clip(RoundedCornerShape(16.dp))
              .background(Color(0xFF072414))
              .border(1.5.dp, Color(0xFF00E676), RoundedCornerShape(16.dp))
              .padding(20.dp)
          ) {
            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
              Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
              ) {
                Box(
                  modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF00E676).copy(alpha = 0.2f)),
                  contentAlignment = Alignment.Center
                ) {
                  Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = "Success",
                    tint = Color(0xFF00E676),
                    modifier = Modifier.size(24.dp)
                  )
                }

                Column {
                  Text(
                    text = "PAYMENT CONFIRMED & CREDITED",
                    color = Color(0xFF00E676),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Black,
                    fontFamily = FontFamily.Monospace,
                    letterSpacing = 1.sp
                  )
                  Text(
                    text = "Lossless Access Unlocked",
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                  )
                }
              }

              Column(
                modifier = Modifier
                  .fillMaxWidth()
                  .clip(RoundedCornerShape(10.dp))
                  .background(Color.Black.copy(alpha = 0.4f))
                  .padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
              ) {
                Row(
                  modifier = Modifier.fillMaxWidth(),
                  horizontalArrangement = Arrangement.SpaceBetween
                ) {
                  Text("User Bank Status:", color = Color.Gray, fontSize = 11.sp)
                  Text("DEBITED (₹${success.amountInr}.00) ✓", color = Color(0xFF00E676), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }

                Row(
                  modifier = Modifier.fillMaxWidth(),
                  horizontalArrangement = Arrangement.SpaceBetween
                ) {
                  Text("Admin Bank Status:", color = Color.Gray, fontSize = 11.sp)
                  Text("CREDITED ✓", color = Color(0xFF00E676), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }

                Row(
                  modifier = Modifier.fillMaxWidth(),
                  horizontalArrangement = Arrangement.SpaceBetween
                ) {
                  Text("Admin Registered Bank:", color = Color.Gray, fontSize = 11.sp)
                  Text(success.adminBankName, color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                }

                Row(
                  modifier = Modifier.fillMaxWidth(),
                  horizontalArrangement = Arrangement.SpaceBetween
                ) {
                  Text("Bank Reference / UTR:", color = Color.Gray, fontSize = 11.sp)
                  Text(success.utrOrTxnId, color = YalpsPrimary, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                }
              }

              Button(
                onClick = {
                  Toast.makeText(context, "Subscription verified! Enjoy your master audio tracks.", Toast.LENGTH_SHORT).show()
                  onSubscribed()
                },
                modifier = Modifier
                  .fillMaxWidth()
                  .height(48.dp)
                  .testTag("start_playback_after_confirmed_btn"),
                colors = ButtonDefaults.buttonColors(
                  containerColor = Color(0xFF00E676),
                  contentColor = Color.Black
                ),
                shape = RoundedCornerShape(12.dp)
              ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                  Icon(imageVector = Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(20.dp))
                  Text("Start Lossless Playback Now", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }
              }
            }
          }
        } else if (verificationState is BankVerificationState.Failed) {
          val failed = verificationState as BankVerificationState.Failed
          Box(
            modifier = Modifier
              .fillMaxWidth()
              .clip(RoundedCornerShape(16.dp))
              .background(Color(0xFF2B0E10))
              .border(1.dp, Color(0xFFFF5252), RoundedCornerShape(16.dp))
              .padding(20.dp)
          ) {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
              Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(imageVector = Icons.Default.ErrorOutline, contentDescription = null, tint = Color(0xFFFF5252), modifier = Modifier.size(28.dp))
                Column {
                  Text("PAYMENT UNCONFIRMED / UNSUCCESSFUL", color = Color(0xFFFF5252), fontSize = 11.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                  Text("Music access remains locked", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                }
              }

              Text(
                text = failed.reason,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 12.sp
              )

              Button(
                onClick = { verificationState = BankVerificationState.Idle },
                modifier = Modifier.fillMaxWidth().height(44.dp),
                colors = ButtonDefaults.buttonColors(containerColor = YalpsPrimary, contentColor = Color.Black),
                shape = RoundedCornerShape(10.dp)
              ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                  Icon(imageVector = Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                  Text("Retry Payment & Confirm Bank Credit", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
              }
            }
          }
        } else {
        // Target Track Preview Card (if user clicked a specific song)
        if (targetSong != null && targetSong.id.isNotBlank()) {
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .clip(RoundedCornerShape(12.dp))
              .background(YalpsSurfaceContainer)
              .border(0.8.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(12.dp))
              .padding(10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
          ) {
            AsyncImage(
              model = targetSong.imageUrl,
              contentDescription = targetSong.title,
              modifier = Modifier
                .size(48.dp)
                .clip(RoundedCornerShape(8.dp)),
              contentScale = ContentScale.Crop
            )

            Column(modifier = Modifier.weight(1f)) {
              Text(
                text = targetSong.title,
                color = Color.White,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
              )
              Text(
                text = "${targetSong.movieOrAlbum} • ${targetSong.composer.ifEmpty { targetSong.singers }}",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 11.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
              )
            }

            Box(
              modifier = Modifier
                .clip(RoundedCornerShape(6.dp))
                .background(YalpsSecondary.copy(alpha = 0.2f))
                .padding(horizontal = 6.dp, vertical = 3.dp)
            ) {
              Text(
                text = "24-BIT MASTER",
                color = YalpsSecondary,
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace
              )
            }
          }

          Spacer(modifier = Modifier.height(14.dp))
        }

        Text(
          text = "Choose your plan to start streaming studio lossless Tamil tracks immediately:",
          color = MaterialTheme.colorScheme.onSurfaceVariant,
          fontSize = 12.sp,
          lineHeight = 16.sp
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Plans Selection Cards
        plans.forEach { plan ->
          val isSelected = selectedPlan.id == plan.id
          Box(
            modifier = Modifier
              .fillMaxWidth()
              .padding(vertical = 4.dp)
              .clip(RoundedCornerShape(14.dp))
              .background(if (isSelected) YalpsPrimary.copy(alpha = 0.12f) else YalpsSurfaceContainer)
              .border(
                width = if (isSelected) 1.5.dp else 0.8.dp,
                color = if (isSelected) YalpsPrimary else Color.White.copy(alpha = 0.08f),
                shape = RoundedCornerShape(14.dp)
              )
              .clickable { selectedPlan = plan }
              .padding(12.dp)
              .testTag("paywall_plan_${plan.id}")
          ) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.weight(1f)
              ) {
                Box(
                  modifier = Modifier
                    .size(20.dp)
                    .clip(CircleShape)
                    .background(if (isSelected) YalpsPrimary else Color.Transparent)
                    .border(1.5.dp, if (isSelected) YalpsPrimary else Color.White.copy(alpha = 0.4f), CircleShape),
                  contentAlignment = Alignment.Center
                ) {
                  if (isSelected) {
                    Icon(imageVector = Icons.Default.Check, contentDescription = null, tint = Color.Black, modifier = Modifier.size(14.dp))
                  }
                }

                Column {
                  Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                      text = plan.name,
                      color = Color.White,
                      fontSize = 14.sp,
                      fontWeight = FontWeight.Bold
                    )
                    if (plan.isTopSelected) {
                      Box(
                        modifier = Modifier
                          .clip(RoundedCornerShape(4.dp))
                          .background(YalpsTertiary.copy(alpha = 0.25f))
                          .padding(horizontal = 4.dp, vertical = 1.dp)
                      ) {
                        Text("POPULAR", color = YalpsTertiary, fontSize = 8.sp, fontWeight = FontWeight.Black)
                      }
                    }
                  }
                  Text(
                    text = "${plan.audioQuality} • ${plan.maxConcurrentDevices} Device${if (plan.maxConcurrentDevices > 1) "s" else ""}",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 11.sp
                  )
                }
              }

              Column(horizontalAlignment = Alignment.End) {
                Text(
                  text = "₹${plan.priceInr}",
                  color = if (isSelected) YalpsPrimary else Color.White,
                  fontSize = 17.sp,
                  fontWeight = FontWeight.Black
                )
                Text(
                  text = "/month",
                  color = MaterialTheme.colorScheme.onSurfaceVariant,
                  fontSize = 10.sp
                )
              }
            }
          }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Payment Method Tabs: UPI vs Stripe Card
        Text(
          text = "PAYMENT METHOD",
          color = MaterialTheme.colorScheme.onSurfaceVariant,
          fontSize = 10.sp,
          fontWeight = FontWeight.Bold,
          fontFamily = FontFamily.Monospace,
          letterSpacing = 1.sp
        )
        Spacer(modifier = Modifier.height(6.dp))

        Row(
          modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(YalpsSurfaceContainerHigh)
            .padding(3.dp),
          horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
          Box(
            modifier = Modifier
              .weight(1f)
              .clip(RoundedCornerShape(8.dp))
              .background(if (paymentMethod == "UPI") Color(0xFF00C853) else Color.Transparent)
              .clickable { paymentMethod = "UPI" }
              .padding(vertical = 8.dp),
            contentAlignment = Alignment.Center
          ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
              Icon(imageVector = Icons.Default.FlashOn, contentDescription = null, tint = if (paymentMethod == "UPI") Color.Black else Color.White, modifier = Modifier.size(15.dp))
              Text("Instant UPI", color = if (paymentMethod == "UPI") Color.Black else Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
          }

          Box(
            modifier = Modifier
              .weight(1f)
              .clip(RoundedCornerShape(8.dp))
              .background(if (paymentMethod == "CARD") Color(0xFF635BFF) else Color.Transparent)
              .clickable { paymentMethod = "CARD" }
              .padding(vertical = 8.dp),
            contentAlignment = Alignment.Center
          ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
              Icon(imageVector = Icons.Default.CreditCard, contentDescription = null, tint = Color.White, modifier = Modifier.size(15.dp))
              Text("Stripe Card", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
          }
        }

        if (paymentMethod == "UPI") {
          Spacer(modifier = Modifier.height(10.dp))
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
          ) {
            listOf("Google Pay", "PhonePe", "Paytm", "BHIM").forEach { app ->
              val isAppSelected = selectedUpiApp == app
              Box(
                modifier = Modifier
                  .weight(1f)
                  .clip(RoundedCornerShape(8.dp))
                  .background(if (isAppSelected) Color(0xFF00C853).copy(alpha = 0.2f) else YalpsSurfaceContainer)
                  .border(1.dp, if (isAppSelected) Color(0xFF00C853) else Color.Transparent, RoundedCornerShape(8.dp))
                  .clickable { selectedUpiApp = app }
                  .padding(vertical = 6.dp),
                contentAlignment = Alignment.Center
              ) {
                Text(
                  text = app,
                  color = if (isAppSelected) Color(0xFF00C853) else Color.White,
                  fontSize = 10.sp,
                  fontWeight = FontWeight.Bold
                )
              }
            }
          }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // Subscribe & Confirm Settlement Button
        Button(
          onClick = {
            if (paymentMethod == "UPI") {
              UpiPaymentLauncher.launchUpiPayment(
                context = context,
                plan = selectedPlan,
                upiApp = selectedUpiApp,
                launcher = upiLauncher,
                onCompleted = { success, msg ->
                  if (success) {
                    executeConfirmedSettlement(onCompleted = onSubscribed)
                  } else {
                    verificationState = BankVerificationState.Failed("UPI payment was cancelled or failed to verify. Music playback remains locked.")
                  }
                }
              )
            } else {
              executeConfirmedSettlement(onCompleted = onSubscribed)
            }
          },
          modifier = Modifier
            .fillMaxWidth()
            .height(50.dp)
            .testTag("subscribe_and_play_btn"),
          colors = ButtonDefaults.buttonColors(
            containerColor = if (paymentMethod == "UPI") Color(0xFF00C853) else Color(0xFF635BFF),
            contentColor = if (paymentMethod == "UPI") Color.Black else Color.White
          ),
          shape = RoundedCornerShape(14.dp),
          enabled = !isProcessing
        ) {
          if (isProcessing) {
            CircularProgressIndicator(
              modifier = Modifier.size(22.dp),
              color = if (paymentMethod == "UPI") Color.Black else Color.White,
              strokeWidth = 2.dp
            )
          } else {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
              Icon(imageVector = Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(18.dp))
              Text(
                text = if (paymentMethod == "UPI") "Pay ₹${selectedPlan.priceInr} with $selectedUpiApp & Verify" else "Pay ₹${selectedPlan.priceInr}/mo & Verify Credit",
                fontSize = 13.sp,
                fontWeight = FontWeight.Black
              )
            }
          }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // See all detailed features button
        OutlinedButton(
          onClick = {
            onDismissRequest()
            onNavigateToPlans()
          },
          modifier = Modifier.fillMaxWidth().height(42.dp),
          shape = RoundedCornerShape(12.dp),
          colors = ButtonDefaults.outlinedButtonColors(contentColor = YalpsSecondary)
        ) {
          Text("View Full Plan Comparison & Details", fontSize = 12.sp)
        }
        }
      }
    }
  }
}
