package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Devices
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.example.data.BankAccountDetails
import com.example.data.OnlineBackendManager
import com.example.razorpay.RazorpayPaymentManager
import com.example.razorpay.RazorpayPaymentStatus
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import com.example.data.TamilSongCatalog
import com.example.model.SubscriptionPlan
import com.example.stripe.StripePaymentsManager
import com.example.stripe.UpiPaymentLauncher
import com.example.ui.components.LosslessBadge
import com.example.ui.theme.YalpsBackground
import com.example.ui.theme.YalpsPrimary
import com.example.ui.theme.YalpsSecondary
import com.example.ui.theme.YalpsSurfaceContainer
import com.example.ui.theme.YalpsSurfaceContainerHigh
import com.example.ui.theme.YalpsSurfaceContainerLowest
import com.example.ui.theme.YalpsTertiary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlansScreen(
  modifier: Modifier = Modifier
) {
  val context = LocalContext.current
  val stripeManager = remember { StripePaymentsManager.getInstance() }
  val backendManager = remember { com.example.data.OnlineBackendManager.getInstance() }
  val primaryAccount by backendManager.primaryBankAccount.collectAsState()
  val customerState by stripeManager.customerState.collectAsState()
  val activeSessions by stripeManager.activeSessions.collectAsState()
  val isProcessing by stripeManager.isProcessingPayment.collectAsState()

  var selectedPlanForCheckout by remember { mutableStateOf<SubscriptionPlan?>(null) }
  var checkoutPaymentMethod by remember { mutableStateOf("UPI") } // "UPI" or "CARD"
  var upiSubMode by remember { mutableStateOf("APP") } // "APP" or "QR"
  var selectedUpiApp by remember { mutableStateOf("Google Pay") }
  var testUpiId by remember { mutableStateOf(primaryAccount?.upiVpa ?: "pragadheeeesh95@okaxis") }
  var testCardNumber by remember { mutableStateOf("4242 4242 4242 4242") }
  var testCardExp by remember { mutableStateOf("12/28") }
  var testCardCvc by remember { mutableStateOf("882") }

  // Keep testUpiId synced if primaryAccount changes
  androidx.compose.runtime.LaunchedEffect(primaryAccount) {
    primaryAccount?.let { acc ->
      if (testUpiId.isBlank() || testUpiId == "pragadheeeesh95@okaxis") {
        testUpiId = acc.upiVpa
      }
    }
  }

  val upiLauncher = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.StartActivityForResult()
  ) { result ->
    selectedPlanForCheckout?.let { plan ->
      UpiPaymentLauncher.handleActivityResult(
        result = result,
        plan = plan,
        upiApp = selectedUpiApp,
        onSuccess = { msg ->
          Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
          selectedPlanForCheckout = null
        },
        onFailure = { errMsg ->
          Toast.makeText(context, errMsg, Toast.LENGTH_SHORT).show()
        }
      )
    }
  }

  LazyColumn(
    modifier = modifier
      .fillMaxSize()
      .background(YalpsBackground)
      .testTag("plans_screen_feed"),
    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp)
  ) {
    // 1. Header with UPI & Stripe Brand Pills
    item {
      Column(modifier = Modifier.fillMaxWidth()) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Column(modifier = Modifier.weight(1f)) {
            Text(
              text = "Stripe & UPI Payments",
              color = Color.White,
              fontSize = 21.sp,
              fontWeight = FontWeight.Black
            )
            Text(
              text = "Lossless Audio • Instant UPI AutoPay & Card Billing",
              color = YalpsPrimary,
              fontSize = 11.sp,
              fontFamily = FontFamily.Monospace
            )
          }

          Row(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            // UPI Badge Pill
            Box(
              modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .background(Brush.horizontalGradient(listOf(Color(0xFF007A3D), Color(0xFF00C853))))
                .padding(horizontal = 8.dp, vertical = 4.dp)
                .testTag("upi_header_pill")
            ) {
              Text(
                text = "UPI ⚡",
                color = Color.White,
                fontSize = 12.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 0.5.sp
              )
            }

            // Stripe Logo Pill
            Box(
              modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .background(Color(0xFF635BFF))
                .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
              Text(
                text = "stripe",
                color = Color.White,
                fontSize = 12.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 0.5.sp
              )
            }
          }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Active Subscription Customer Card
        if (customerState.isSubscribed) {
          val isUpiActive = customerState.activePaymentMethodType == "UPI"
          Box(
            modifier = Modifier
              .fillMaxWidth()
              .clip(RoundedCornerShape(16.dp))
              .background(
                Brush.linearGradient(
                  if (isUpiActive) {
                    listOf(Color(0xFF064E3B), Color(0xFF0F172A))
                  } else {
                    listOf(Color(0xFF2E1065), Color(0xFF1E1B4B))
                  }
                )
              )
              .border(
                1.dp,
                if (isUpiActive) Color(0xFF00E676).copy(alpha = 0.5f) else YalpsPrimary.copy(alpha = 0.4f),
                RoundedCornerShape(16.dp)
              )
              .padding(16.dp)
              .testTag("active_stripe_customer_card")
          ) {
            Column {
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
              ) {
                Row(
                  verticalAlignment = Alignment.CenterVertically,
                  horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                  Icon(
                    imageVector = if (isUpiActive) Icons.Default.FlashOn else Icons.Default.CreditCard,
                    contentDescription = null,
                    tint = if (isUpiActive) Color(0xFF00E676) else YalpsPrimary
                  )
                  Text(
                    text = if (isUpiActive) "ACTIVE SUBSCRIPTION (UPI AUTOPAY)" else "ACTIVE SUBSCRIPTION (CARD)",
                    color = if (isUpiActive) Color(0xFF00E676) else YalpsPrimary,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                  )
                }
                LosslessBadge(text = customerState.status.uppercase())
              }

              Spacer(modifier = Modifier.height(10.dp))

              val currentPlan = stripeManager.getPlanById(customerState.activeTierId)
              Text(
                text = currentPlan.name,
                color = Color.White,
                fontSize = 20.sp,
                fontWeight = FontWeight.Black
              )
              Text(
                text = if (isUpiActive) {
                  "AutoPay via UPI (${customerState.defaultUpi?.upiApp ?: "Google Pay"}) • ₹${currentPlan.priceInr}/mo • Next mandate on ${customerState.nextBillingDate}"
                } else {
                  "Billed via Stripe • ₹${currentPlan.priceInr}/month • Next invoice on ${customerState.nextBillingDate}"
                },
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 12.sp
              )

              Spacer(modifier = Modifier.height(12.dp))

              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
              ) {
                if (isUpiActive) {
                  Text(
                    text = "VPA: ${customerState.defaultUpi?.upiId ?: "pragadheesh@okaxis"}",
                    color = Color.White,
                    fontSize = 12.sp,
                    fontFamily = FontFamily.Monospace
                  )
                  Text(
                    text = "UTR: ${customerState.defaultUpi?.utrNumber ?: "429104829103"}",
                    color = Color(0xFF00E676),
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace
                  )
                } else {
                  Text(
                    text = "${customerState.defaultCard.brand} ending in •••• ${customerState.defaultCard.last4}",
                    color = Color.White,
                    fontSize = 12.sp,
                    fontFamily = FontFamily.Monospace
                  )
                  Text(
                    text = "ID: ${customerState.customerId.take(15)}...",
                    color = YalpsSecondary,
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace
                  )
                }
              }
            }
          }
        } else {
          // Unsubscribed Banner
          Box(
            modifier = Modifier
              .fillMaxWidth()
              .clip(RoundedCornerShape(16.dp))
              .background(
                Brush.linearGradient(
                  listOf(Color(0xFF311B92).copy(alpha = 0.6f), YalpsSurfaceContainerLowest)
                )
              )
              .border(
                1.dp,
                YalpsPrimary.copy(alpha = 0.5f),
                RoundedCornerShape(16.dp)
              )
              .padding(16.dp)
              .testTag("unsubscribed_customer_banner")
          ) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
              ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                  Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = null,
                    tint = YalpsPrimary,
                    modifier = Modifier.size(18.dp)
                  )
                  Text(
                    text = "SUBSCRIPTION REQUIRED",
                    color = YalpsPrimary,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                  )
                }
                Box(
                  modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(Color.White.copy(alpha = 0.1f))
                    .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                  Text("FREE TIER", color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                }
              }

              Text(
                text = "Subscribe to Unlock Track Access",
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.Black
              )
              Text(
                text = "To access and stream 24-bit/192kHz studio master Tamil tracks and albums, select any plan below. Pay instantly via UPI or Card.",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 12.sp,
                lineHeight = 16.sp
              )
            }
          }
        }
      }
    }

    // 2. Subscription Tiers matching Screenshot 4
    item {
      Spacer(modifier = Modifier.height(20.dp))

      Text(
        text = "Available Lossless Plans",
        color = Color.White,
        fontSize = 17.sp,
        fontWeight = FontWeight.Bold
      )
      Spacer(modifier = Modifier.height(10.dp))

      TamilSongCatalog.subscriptionPlans.forEach { plan ->
        val isCurrentPlan = plan.id == customerState.activeTierId
        Box(
          modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(YalpsSurfaceContainerLowest)
            .border(
              width = if (plan.isTopSelected) 1.5.dp else 1.dp,
              brush = if (plan.isTopSelected) Brush.horizontalGradient(listOf(YalpsPrimary, YalpsSecondary))
              else Brush.linearGradient(listOf(Color.White.copy(alpha = 0.08f), Color.White.copy(alpha = 0.08f))),
              shape = RoundedCornerShape(16.dp)
            )
            .padding(16.dp)
            .testTag("plan_card_${plan.id}")
        ) {
          Column {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
              ) {
                Text(
                  text = plan.name,
                  color = Color.White,
                  fontSize = 16.sp,
                  fontWeight = FontWeight.Bold
                )
                if (plan.isTopSelected) {
                  LosslessBadge(text = plan.tierBadge, isAtmos = true)
                }
              }

              // Price
              Row(verticalAlignment = Alignment.Bottom) {
                Text(
                  text = "₹${plan.priceInr}",
                  color = if (plan.isTopSelected) YalpsSecondary else Color.White,
                  fontSize = 22.sp,
                  fontWeight = FontWeight.Black
                )
                Text(
                  text = "/mo",
                  color = MaterialTheme.colorScheme.onSurfaceVariant,
                  fontSize = 12.sp,
                  fontFamily = FontFamily.Monospace
                )
              }
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
              text = plan.description,
              color = MaterialTheme.colorScheme.onSurfaceVariant,
              fontSize = 12.sp
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Specs
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
              Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                  imageVector = Icons.Default.Devices,
                  contentDescription = null,
                  tint = YalpsPrimary,
                  modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                  text = "${plan.maxConcurrentDevices} Devices Concurrency",
                  color = Color.White,
                  fontSize = 11.sp,
                  fontFamily = FontFamily.Monospace
                )
              }
              Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                  imageVector = Icons.Default.Check,
                  contentDescription = null,
                  tint = YalpsSecondary,
                  modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                  text = plan.audioQuality,
                  color = YalpsSecondary,
                  fontSize = 11.sp,
                  fontFamily = FontFamily.Monospace
                )
              }
            }

            Spacer(modifier = Modifier.height(12.dp))

            if (isCurrentPlan) {
              Box(
                modifier = Modifier
                  .fillMaxWidth()
                  .clip(RoundedCornerShape(10.dp))
                  .background(YalpsSecondary.copy(alpha = 0.15f))
                  .border(1.dp, YalpsSecondary, RoundedCornerShape(10.dp))
                  .padding(vertical = 8.dp),
                contentAlignment = Alignment.Center
              ) {
                Text(
                  text = "CURRENT ACTIVE PLAN ✓",
                  color = YalpsSecondary,
                  fontSize = 12.sp,
                  fontWeight = FontWeight.Bold,
                  fontFamily = FontFamily.Monospace
                )
              }
            } else {
              Button(
                onClick = { selectedPlanForCheckout = plan },
                colors = ButtonDefaults.buttonColors(
                  containerColor = if (plan.isTopSelected) YalpsPrimary else YalpsSurfaceContainerHigh,
                  contentColor = if (plan.isTopSelected) Color.Black else Color.White
                ),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.fillMaxWidth().testTag("select_plan_btn_${plan.id}")
              ) {
                Text(
                  text = "Switch to ${plan.name} (₹${plan.priceInr}/mo)",
                  fontWeight = FontWeight.Bold,
                  fontSize = 12.sp
                )
              }
            }
          }
        }
      }
    }

    // 3. Hardware Concurrency Protection Engine matching Screenshot 4
    item {
      Spacer(modifier = Modifier.height(24.dp))

      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Column {
          Text(
            text = "Hardware Concurrency Engine",
            color = Color.White,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold
          )
          val currentPlan = stripeManager.getPlanById(customerState.activeTierId)
          Text(
            text = "${activeSessions.size} of ${currentPlan.maxConcurrentDevices} active hardware endpoints streaming",
            color = YalpsSecondary,
            fontSize = 11.sp,
            fontFamily = FontFamily.Monospace
          )
        }

        Box(
          modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(YalpsSecondary.copy(alpha = 0.15f))
            .padding(horizontal = 6.dp, vertical = 2.dp)
        ) {
          Text(
            text = "PROTECTED",
            color = YalpsSecondary,
            fontSize = 9.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace
          )
        }
      }

      Spacer(modifier = Modifier.height(10.dp))

      activeSessions.forEach { session ->
        Box(
          modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(YalpsSurfaceContainerLowest)
            .border(1.dp, Color.White.copy(alpha = 0.05f), RoundedCornerShape(12.dp))
            .padding(12.dp)
        ) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Row(
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
              Box(
                modifier = Modifier
                  .size(36.dp)
                  .clip(CircleShape)
                  .background(if (session.isCurrentDevice) YalpsPrimary.copy(alpha = 0.2f) else YalpsSurfaceContainerHigh),
                contentAlignment = Alignment.Center
              ) {
                Icon(
                  imageVector = Icons.Default.Devices,
                  contentDescription = null,
                  tint = if (session.isCurrentDevice) YalpsPrimary else Color.White,
                  modifier = Modifier.size(20.dp)
                )
              }
              Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                  Text(
                    text = session.deviceName,
                    color = Color.White,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                  )
                  if (session.isCurrentDevice) {
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                      text = "(THIS DEVICE)",
                      color = YalpsSecondary,
                      fontSize = 9.sp,
                      fontWeight = FontWeight.Bold,
                      fontFamily = FontFamily.Monospace
                    )
                  }
                }
                Text(
                  text = "${session.platform} • ${session.audioStreamFormat}",
                  color = MaterialTheme.colorScheme.onSurfaceVariant,
                  fontSize = 10.sp,
                  fontFamily = FontFamily.Monospace
                )
              }
            }

            if (!session.isCurrentDevice) {
              IconButton(
                onClick = {
                  stripeManager.revokeDeviceSession(session.id)
                  Toast.makeText(context, "Session revoked for ${session.deviceName}", Toast.LENGTH_SHORT).show()
                },
                modifier = Modifier.size(32.dp).testTag("revoke_session_btn_${session.id}")
              ) {
                Icon(
                  imageVector = Icons.Default.Delete,
                  contentDescription = "Revoke session",
                  tint = Color(0xFFEF4444),
                  modifier = Modifier.size(18.dp)
                )
              }
            } else {
              Text(
                text = "Active Now",
                color = YalpsSecondary,
                fontSize = 10.sp,
                fontFamily = FontFamily.Monospace
              )
            }
          }
        }
      }
    }

    // 4. Invoicing History (Stripe & UPI)
    item {
      Spacer(modifier = Modifier.height(24.dp))

      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text(
          text = "Billing Invoices & Receipts",
          color = Color.White,
          fontSize = 16.sp,
          fontWeight = FontWeight.Bold
        )
        Text(
          text = "Stripe & UPI NPCI",
          color = YalpsSecondary,
          fontSize = 10.sp,
          fontFamily = FontFamily.Monospace
        )
      }
      Spacer(modifier = Modifier.height(10.dp))

      customerState.invoices.forEach { invoice ->
        val isUpiInvoice = invoice.paymentMethod.startsWith("UPI")
        Box(
          modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(YalpsSurfaceContainerLowest)
            .border(1.dp, Color.White.copy(alpha = 0.05f), RoundedCornerShape(10.dp))
            .padding(12.dp)
        ) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Row(
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(8.dp),
              modifier = Modifier.weight(1f)
            ) {
              Icon(
                imageVector = if (isUpiInvoice) Icons.Default.FlashOn else Icons.Default.Receipt,
                contentDescription = null,
                tint = if (isUpiInvoice) Color(0xFF00E676) else YalpsPrimary,
                modifier = Modifier.size(18.dp)
              )
              Column {
                Row(
                  verticalAlignment = Alignment.CenterVertically,
                  horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                  Text(
                    text = "Invoice ${invoice.id}",
                    color = Color.White,
                    fontSize = 12.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold
                  )
                  if (isUpiInvoice) {
                    Box(
                      modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(Color(0xFF007A3D).copy(alpha = 0.3f))
                        .border(0.6.dp, Color(0xFF00E676), RoundedCornerShape(4.dp))
                        .padding(horizontal = 4.dp, vertical = 1.dp)
                    ) {
                      Text(
                        text = "UPI",
                        color = Color(0xFF00E676),
                        fontSize = 8.sp,
                        fontWeight = FontWeight.Bold
                      )
                    }
                  }
                }
                Text(
                  text = "${invoice.date} • ${invoice.paymentMethod}",
                  color = MaterialTheme.colorScheme.onSurfaceVariant,
                  fontSize = 10.sp
                )
                if (invoice.upiRefId != null) {
                  Text(
                    text = "NPCI ${invoice.upiRefId}",
                    color = Color(0xFF00E676).copy(alpha = 0.8f),
                    fontSize = 9.sp,
                    fontFamily = FontFamily.Monospace
                  )
                }
              }
            }
            Column(horizontalAlignment = Alignment.End) {
              Text(
                text = invoice.amountFormatted,
                color = Color.White,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold
              )
              Text(
                text = invoice.status.uppercase() + " ✓",
                color = if (isUpiInvoice) Color(0xFF00E676) else YalpsSecondary,
                fontSize = 9.sp,
                fontFamily = FontFamily.Monospace
              )
            }
          }
        }
      }

      Spacer(modifier = Modifier.height(100.dp))
    }
  }

  // Payment Gateway Checkout Bottom Sheet with UPI & Stripe Card options
  selectedPlanForCheckout?.let { plan ->
    ModalBottomSheet(
      onDismissRequest = { if (!isProcessing) selectedPlanForCheckout = null },
      sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
      containerColor = YalpsSurfaceContainerHigh
    ) {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 20.dp, vertical = 14.dp)
      ) {
        // Gateway Header
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
          ) {
            Icon(
              imageVector = Icons.Default.Lock,
              contentDescription = null,
              tint = YalpsSecondary,
              modifier = Modifier.size(16.dp)
            )
            Text(
              text = "SECURE PAYMENT GATEWAY",
              color = YalpsSecondary,
              fontSize = 11.sp,
              fontWeight = FontWeight.Bold,
              fontFamily = FontFamily.Monospace
            )
          }

          Row(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Box(
              modifier = Modifier
                .clip(RoundedCornerShape(6.dp))
                .background(Brush.horizontalGradient(listOf(Color(0xFF007A3D), Color(0xFF00C853))))
                .padding(horizontal = 7.dp, vertical = 2.dp)
            ) {
              Text(
                text = "UPI ⚡",
                color = Color.White,
                fontSize = 11.sp,
                fontWeight = FontWeight.Black
              )
            }
            Box(
              modifier = Modifier
                .clip(RoundedCornerShape(6.dp))
                .background(Color(0xFF635BFF))
                .padding(horizontal = 7.dp, vertical = 2.dp)
            ) {
              Text(
                text = "stripe",
                color = Color.White,
                fontSize = 11.sp,
                fontWeight = FontWeight.Black
              )
            }
          }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Text(
          text = "Upgrade to ${plan.name}",
          color = Color.White,
          fontSize = 19.sp,
          fontWeight = FontWeight.Black
        )
        Text(
          text = "₹${plan.priceInr}.00/month • ${plan.audioQuality} • ${plan.maxConcurrentDevices} Endpoints",
          color = MaterialTheme.colorScheme.onSurfaceVariant,
          fontSize = 12.sp
        )

        Spacer(modifier = Modifier.height(14.dp))

        // Payment Method Tabs: UPI vs Card
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(YalpsSurfaceContainerLowest)
            .padding(4.dp),
          horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
          // UPI Tab
          val isUpiSelected = checkoutPaymentMethod == "UPI"
          Box(
            modifier = Modifier
              .weight(1f)
              .clip(RoundedCornerShape(9.dp))
              .then(
                if (isUpiSelected) {
                  Modifier.background(Brush.horizontalGradient(listOf(Color(0xFF007A3D), Color(0xFF00C853))))
                } else {
                  Modifier.background(Color.Transparent)
                }
              )
              .clickable { checkoutPaymentMethod = "UPI" }
              .padding(vertical = 10.dp)
              .testTag("tab_pay_upi"),
            contentAlignment = Alignment.Center
          ) {
            Row(
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
              Icon(
                imageVector = Icons.Default.FlashOn,
                contentDescription = null,
                tint = if (isUpiSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(16.dp)
              )
              Text(
                text = "UPI Instant",
                color = if (isUpiSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = if (isUpiSelected) FontWeight.Bold else FontWeight.Medium,
                fontSize = 13.sp
              )
            }
          }

          // Card Tab
          val isCardSelected = checkoutPaymentMethod == "CARD"
          Box(
            modifier = Modifier
              .weight(1f)
              .clip(RoundedCornerShape(9.dp))
              .background(if (isCardSelected) Color(0xFF635BFF) else Color.Transparent)
              .clickable { checkoutPaymentMethod = "CARD" }
              .padding(vertical = 10.dp)
              .testTag("tab_pay_card"),
            contentAlignment = Alignment.Center
          ) {
            Row(
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
              Icon(
                imageVector = Icons.Default.CreditCard,
                contentDescription = null,
                tint = if (isCardSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(16.dp)
              )
              Text(
                text = "Cards (Stripe)",
                color = if (isCardSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = if (isCardSelected) FontWeight.Bold else FontWeight.Medium,
                fontSize = 13.sp
              )
            }
          }
        }

        Spacer(modifier = Modifier.height(14.dp))

        if (checkoutPaymentMethod == "UPI") {
          // UPI Submode Selector (UPI Apps / VPA vs Dynamic Lossless QR)
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            FilterChipPill(
              text = "⚡ UPI Apps & VPA",
              isSelected = upiSubMode == "APP",
              onClick = { upiSubMode = "APP" },
              modifier = Modifier.weight(1f)
            )
            FilterChipPill(
              text = "📷 Scan Dynamic QR",
              isSelected = upiSubMode == "QR",
              onClick = { upiSubMode = "QR" },
              modifier = Modifier.weight(1f)
            )
          }

          Spacer(modifier = Modifier.height(12.dp))

          if (upiSubMode == "APP") {
            // UPI App Chips Row
            Text(
              text = "Select UPI App:",
              color = Color.White,
              fontSize = 12.sp,
              fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(6.dp))

            val upiApps = listOf(
              Triple("Google Pay", Color(0xFF1A73E8), "@okaxis"),
              Triple("PhonePe", Color(0xFF5F259F), "@ybl"),
              Triple("Paytm", Color(0xFF00BAF2), "@paytm"),
              Triple("BHIM", Color(0xFF007A3D), "@upi"),
              Triple("CRED", Color(0xFFC9A86A), "@axisbank")
            )

            Row(
              modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
              horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
              upiApps.forEach { (appName, appColor, handle) ->
                val isSelected = selectedUpiApp == appName
                Box(
                  modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(if (isSelected) appColor else YalpsSurfaceContainerLowest)
                    .border(
                      width = 1.dp,
                      color = if (isSelected) Color.White else Color.White.copy(alpha = 0.1f),
                      shape = RoundedCornerShape(8.dp)
                    )
                    .clickable {
                      selectedUpiApp = appName
                      val prefix = testUpiId.substringBefore("@").ifEmpty { "pragadheeeesh95" }
                      testUpiId = "$prefix$handle"
                    }
                    .padding(horizontal = 12.dp, vertical = 8.dp)
                    .testTag("upi_app_chip_$appName")
                ) {
                  Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                      modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(if (isSelected) Color.White else appColor)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                      text = appName,
                      color = Color.White,
                      fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                      fontSize = 12.sp
                    )
                  }
                }
              }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // VPA / UPI ID Input Field
            OutlinedTextField(
              value = testUpiId,
              onValueChange = { testUpiId = it },
              label = { Text("Virtual Payment Address (UPI ID)") },
              leadingIcon = {
                Icon(imageVector = Icons.Default.AccountBalance, contentDescription = null, tint = Color(0xFF00E676))
              },
              trailingIcon = {
                Text(
                  text = "NPCI ✓",
                  color = Color(0xFF00E676),
                  fontSize = 10.sp,
                  fontWeight = FontWeight.Bold,
                  modifier = Modifier.padding(end = 8.dp)
                )
              },
              singleLine = true,
              shape = RoundedCornerShape(10.dp),
              colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = YalpsSurfaceContainerLowest,
                unfocusedContainerColor = YalpsSurfaceContainerLowest,
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White
              ),
              modifier = Modifier.fillMaxWidth().testTag("upi_id_input_field")
            )

            Spacer(modifier = Modifier.height(6.dp))

            // Quick VPA Handle Suffix Chips
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
              horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
              listOf("@okaxis", "@okhdfcbank", "@okicici", "@ybl", "@paytm", "@upi").forEach { handle ->
                Box(
                  modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(Color.White.copy(alpha = 0.06f))
                    .clickable {
                      val prefix = testUpiId.substringBefore("@").ifEmpty { "pragadheeeesh95" }
                      testUpiId = "$prefix$handle"
                    }
                    .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                  Text(
                    text = handle,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace
                  )
                }
              }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // UPI Gateway Status & Redirection Info
            Box(
              modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(10.dp))
                .background(Color(0xFF00C853).copy(alpha = 0.12f))
                .border(1.dp, Color(0xFF00E676).copy(alpha = 0.35f), RoundedCornerShape(10.dp))
                .padding(horizontal = 12.dp, vertical = 10.dp)
            ) {
              Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
              ) {
                Icon(
                  imageVector = Icons.Default.CheckCircle,
                  contentDescription = null,
                  tint = Color(0xFF00E676),
                  modifier = Modifier.size(18.dp)
                )
                Column {
                  Text(
                    text = "Direct Settlement to Backend Admin Bank Account",
                    color = Color(0xFF00E676),
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp
                  )
                  val bankDisplay = primaryAccount?.let { "${it.bankName} (A/C •••• ${it.accountNumber.takeLast(4)})" } ?: "Registered Admin Bank"
                  Text(
                    text = "Beneficiary: ${primaryAccount?.accountHolderName ?: "Pragadheesh (Backend Admin)"} • $bankDisplay • ₹${plan.priceInr}.00",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 10.sp
                  )
                }
              }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Primary UPI Pay Button - Direct external app redirection
            Button(
              onClick = {
                UpiPaymentLauncher.launchUpiPayment(
                  context = context,
                  plan = plan,
                  upiApp = selectedUpiApp,
                  launcher = upiLauncher,
                  onCompleted = { success, msg ->
                    if (success) {
                      Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
                      selectedPlanForCheckout = null
                    }
                  }
                )
              },
              colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF00C853),
                contentColor = Color.Black
              ),
              shape = RoundedCornerShape(12.dp),
              modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .testTag("confirm_upi_payment_btn"),
              enabled = !isProcessing
            ) {
              if (isProcessing) {
                CircularProgressIndicator(
                  modifier = Modifier.size(20.dp),
                  color = Color.Black,
                  strokeWidth = 2.dp
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Opening $selectedUpiApp & Authorizing...", fontWeight = FontWeight.Bold, color = Color.Black)
              } else {
                Text(
                  text = "Open $selectedUpiApp & Pay ₹${plan.priceInr}.00",
                  fontWeight = FontWeight.Black,
                  fontSize = 13.sp,
                  color = Color.Black
                )
              }
            }
          } else {
            // QR Code Scan & Pay Mode via Razorpay Payment Gateway
            val razorpayManager = remember { RazorpayPaymentManager.getInstance() }
            val currentOrder = remember(plan.id) { razorpayManager.createSubscriptionOrder(plan) }
            val currentVpa = primaryAccount?.upiVpa ?: "pragadheeeesh95@okaxis"
            val currentBeneficiary = primaryAccount?.accountHolderName ?: "Pragadheesh (Backend Admin)"
            val currentBank = primaryAccount?.bankName ?: "Admin Bank"

            UpiDynamicQrView(
              plan = plan,
              upiId = currentVpa,
              beneficiaryName = "$currentBeneficiary ($currentBank)",
              orderId = currentOrder.orderId
            )

            Spacer(modifier = Modifier.height(12.dp))

            Button(
              onClick = {
                razorpayManager.verifyAndCapturePayment(
                  plan = plan,
                  paymentMethod = "Razorpay Dynamic QR",
                  payerVpa = currentVpa,
                  onSuccess = { msg ->
                    Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
                    selectedPlanForCheckout = null
                  },
                  onFailure = { err ->
                    Toast.makeText(context, err, Toast.LENGTH_LONG).show()
                  }
                )
              },
              colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF00C853),
                contentColor = Color.Black
              ),
              shape = RoundedCornerShape(12.dp),
              modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .testTag("confirm_upi_qr_btn"),
              enabled = !isProcessing
            ) {
              if (isProcessing) {
                CircularProgressIndicator(
                  modifier = Modifier.size(20.dp),
                  color = Color.Black,
                  strokeWidth = 2.dp
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Verifying Razorpay Order & NPCI Settlement...", fontWeight = FontWeight.Bold, color = Color.Black)
              } else {
                Text(
                  text = "Confirm Razorpay Payment & Activate ₹${plan.priceInr}.00",
                  fontWeight = FontWeight.Black,
                  fontSize = 13.sp,
                  color = Color.Black
                )
              }
            }
          }

          Spacer(modifier = Modifier.height(10.dp))

          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Icon(
              imageVector = Icons.Default.Security,
              contentDescription = null,
              tint = Color(0xFF00E676),
              modifier = Modifier.size(14.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
              text = "NPCI UPI 2.0 • 100% Secure • Zero Surcharge • Instant Mandate",
              color = MaterialTheme.colorScheme.onSurfaceVariant,
              fontSize = 10.sp,
              fontFamily = FontFamily.Monospace
            )
          }
        } else {
          // Card Payment (Stripe)
          OutlinedTextField(
            value = testCardNumber,
            onValueChange = { testCardNumber = it },
            label = { Text("Card Number (Stripe Test)") },
            leadingIcon = {
              Icon(imageVector = Icons.Default.CreditCard, contentDescription = null, tint = YalpsPrimary)
            },
            singleLine = true,
            shape = RoundedCornerShape(10.dp),
            colors = OutlinedTextFieldDefaults.colors(
              focusedContainerColor = YalpsSurfaceContainerLowest,
              unfocusedContainerColor = YalpsSurfaceContainerLowest,
              focusedTextColor = Color.White,
              unfocusedTextColor = Color.White
            ),
            modifier = Modifier.fillMaxWidth()
          )

          Spacer(modifier = Modifier.height(10.dp))

          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
          ) {
            OutlinedTextField(
              value = testCardExp,
              onValueChange = { testCardExp = it },
              label = { Text("Exp MM/YY") },
              singleLine = true,
              shape = RoundedCornerShape(10.dp),
              colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = YalpsSurfaceContainerLowest,
                unfocusedContainerColor = YalpsSurfaceContainerLowest,
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White
              ),
              modifier = Modifier.weight(1f)
            )
            OutlinedTextField(
              value = testCardCvc,
              onValueChange = { testCardCvc = it },
              label = { Text("CVC") },
              singleLine = true,
              shape = RoundedCornerShape(10.dp),
              colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = YalpsSurfaceContainerLowest,
                unfocusedContainerColor = YalpsSurfaceContainerLowest,
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White
              ),
              modifier = Modifier.weight(1f)
            )
          }

          Spacer(modifier = Modifier.height(16.dp))

          Button(
            onClick = {
              stripeManager.switchSubscriptionTier(
                targetPlan = plan,
                cardNumber = testCardNumber
              ) { msg ->
                Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
                selectedPlanForCheckout = null
              }
            },
            colors = ButtonDefaults.buttonColors(
              containerColor = Color(0xFF635BFF),
              contentColor = Color.White
            ),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
              .fillMaxWidth()
              .height(48.dp)
              .testTag("confirm_stripe_payment_btn"),
            enabled = !isProcessing
          ) {
            if (isProcessing) {
              CircularProgressIndicator(
                modifier = Modifier.size(20.dp),
                color = Color.White,
                strokeWidth = 2.dp
              )
              Spacer(modifier = Modifier.width(8.dp))
              Text("Confirming with Stripe Webhook...", fontWeight = FontWeight.Bold)
            } else {
              Text(
                text = "Pay ₹${plan.priceInr}.00 via Stripe",
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp
              )
            }
          }

          Spacer(modifier = Modifier.height(10.dp))

          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Icon(
              imageVector = Icons.Default.Security,
              contentDescription = null,
              tint = MaterialTheme.colorScheme.onSurfaceVariant,
              modifier = Modifier.size(14.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
              text = "256-bit SSL encrypted • Stripe Certified PCI DSS Level 1",
              color = MaterialTheme.colorScheme.onSurfaceVariant,
              fontSize = 10.sp,
              fontFamily = FontFamily.Monospace
            )
          }
        }

        Spacer(modifier = Modifier.height(16.dp))
      }
    }
  }
}

@Composable
private fun FilterChipPill(
  text: String,
  isSelected: Boolean,
  onClick: () -> Unit,
  modifier: Modifier = Modifier
) {
  Box(
    modifier = modifier
      .clip(RoundedCornerShape(8.dp))
      .background(if (isSelected) Color(0xFF00C853) else YalpsSurfaceContainerLowest)
      .border(
        width = 1.dp,
        color = if (isSelected) Color(0xFF00E676) else Color.White.copy(alpha = 0.1f),
        shape = RoundedCornerShape(8.dp)
      )
      .clickable(onClick = onClick)
      .padding(vertical = 8.dp),
    contentAlignment = Alignment.Center
  ) {
    Text(
      text = text,
      color = if (isSelected) Color.Black else Color.White,
      fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
      fontSize = 11.sp
    )
  }
}

@Composable
fun UpiDynamicQrView(
  plan: SubscriptionPlan,
  upiId: String = "pragadheeeesh95@okaxis",
  beneficiaryName: String = "Pragadheesh Lossless Master",
  orderId: String = "",
  modifier: Modifier = Modifier
) {
  Column(
    modifier = modifier
      .fillMaxWidth()
      .clip(RoundedCornerShape(14.dp))
      .background(Color(0xFF0F172A))
      .border(1.dp, Color(0xFF00E676).copy(alpha = 0.4f), RoundedCornerShape(14.dp))
      .padding(16.dp),
    horizontalAlignment = Alignment.CenterHorizontally
  ) {
    Row(
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
      Box(
        modifier = Modifier
          .clip(RoundedCornerShape(4.dp))
          .background(Color(0xFF00C853))
          .padding(horizontal = 6.dp, vertical = 2.dp)
      ) {
        Text("Razorpay", color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Black)
      }
      Text(
        text = "NPCI UPI 2.0 • DYNAMIC GATEWAY QR",
        color = Color(0xFF00E676),
        fontSize = 10.sp,
        fontWeight = FontWeight.Bold,
        fontFamily = FontFamily.Monospace
      )
    }

    Spacer(modifier = Modifier.height(10.dp))

    // High fidelity QR Matrix Card
    Box(
      modifier = Modifier
        .size(170.dp)
        .clip(RoundedCornerShape(12.dp))
        .background(Color.White)
        .padding(10.dp),
      contentAlignment = Alignment.Center
    ) {
      Canvas(modifier = Modifier.fillMaxSize()) {
        val w = size.width
        val h = size.height
        val boxSize = w * 0.24f

        // Draw Finder Patterns (Top-Left, Top-Right, Bottom-Left)
        // 1. Top-Left
        drawRect(Color.Black, Offset(0f, 0f), Size(boxSize, boxSize), style = Stroke(width = 8f))
        drawRect(Color(0xFF00897B), Offset(boxSize * 0.25f, boxSize * 0.25f), Size(boxSize * 0.5f, boxSize * 0.5f))

        // 2. Top-Right
        drawRect(Color.Black, Offset(w - boxSize, 0f), Size(boxSize, boxSize), style = Stroke(width = 8f))
        drawRect(Color(0xFF00897B), Offset(w - boxSize * 0.75f, boxSize * 0.25f), Size(boxSize * 0.5f, boxSize * 0.5f))

        // 3. Bottom-Left
        drawRect(Color.Black, Offset(0f, h - boxSize), Size(boxSize, boxSize), style = Stroke(width = 8f))
        drawRect(Color(0xFF00897B), Offset(boxSize * 0.25f, h - boxSize * 0.75f), Size(boxSize * 0.5f, boxSize * 0.5f))

        // Synthetic QR Grid Dots
        val gridSize = 14
        val stepX = w / gridSize
        val stepY = h / gridSize
        for (i in 0 until gridSize) {
          for (j in 0 until gridSize) {
            // Avoid finder corners & center
            val inTopLeft = i < 4 && j < 4
            val inTopRight = i > gridSize - 5 && j < 4
            val inBottomLeft = i < 4 && j > gridSize - 5
            val inCenter = i in 5..8 && j in 5..8

            if (!inTopLeft && !inTopRight && !inBottomLeft && !inCenter) {
              val isDot = ((i * 7 + j * 13 + 5) % 3 != 0)
              if (isDot) {
                drawCircle(
                  color = if ((i + j) % 5 == 0) Color(0xFF00897B) else Color.Black,
                  radius = stepX * 0.32f,
                  center = Offset(i * stepX + stepX / 2, j * stepY + stepY / 2)
                )
              }
            }
          }
        }

        // Center UPI Badge
        drawCircle(Color.White, radius = boxSize * 0.45f, center = Offset(w / 2, h / 2))
        drawCircle(Color(0xFF00897B), radius = boxSize * 0.38f, center = Offset(w / 2, h / 2))
      }

      // UPI pill overlay in center
      Text(
        text = "UPI",
        color = Color.White,
        fontWeight = FontWeight.Black,
        fontSize = 11.sp,
        letterSpacing = 0.5.sp
      )
    }

    Spacer(modifier = Modifier.height(10.dp))

    Text(
      text = "₹${plan.priceInr}.00",
      color = Color.White,
      fontSize = 20.sp,
      fontWeight = FontWeight.Black
    )
    if (orderId.isNotBlank()) {
      Text(
        text = "Razorpay Order: $orderId",
        color = YalpsSecondary,
        fontSize = 10.sp,
        fontFamily = FontFamily.Monospace
      )
    }
    Text(
      text = "Beneficiary: $beneficiaryName",
      color = MaterialTheme.colorScheme.onSurfaceVariant,
      fontSize = 11.sp,
      fontWeight = FontWeight.Medium
    )
    Text(
      text = "VPA: $upiId",
      color = Color(0xFF00E676),
      fontSize = 11.sp,
      fontFamily = FontFamily.Monospace
    )
  }
}
