package com.example.model

data class SubscriptionPlan(
  val id: String,
  val name: String,
  val tierBadge: String,
  val priceInr: Int,
  val priceUsd: Double,
  val billingPeriod: String = "month",
  val maxConcurrentDevices: Int,
  val audioQuality: String,
  val description: String,
  val activeUsers: String,
  val mrrContribution: String,
  val isTopSelected: Boolean = false,
  val stripePriceId: String
)

data class StripeCardInfo(
  val brand: String = "Visa",
  val last4: String = "4242",
  val expMonth: Int = 12,
  val expYear: Int = 2028,
  val isDefault: Boolean = true
)

data class UpiPaymentInfo(
  val upiId: String = "yalps.listener@okaxis",
  val upiApp: String = "Google Pay",
  val utrNumber: String = "429104829103",
  val vpaHandle: String = "@okaxis",
  val isVerified: Boolean = true
)

data class StripeInvoice(
  val id: String,
  val date: String,
  val amountFormatted: String,
  val status: String = "Paid",
  val paymentMethod: String = "Card •••• 4242",
  val upiRefId: String? = null,
  val pdfUrl: String = "#"
)

data class StripeCustomerState(
  val customerId: String = "cus_YalpsAudiophile_9281",
  val activeTierId: String = "",
  val status: String = "Inactive",
  val nextBillingDate: String = "",
  val defaultCard: StripeCardInfo = StripeCardInfo(),
  val defaultUpi: UpiPaymentInfo? = null,
  val activePaymentMethodType: String = "NONE",
  val activeDevicesCount: Int = 0,
  val invoices: List<StripeInvoice> = emptyList()
) {
  val isSubscribed: Boolean get() = activeTierId.isNotBlank() && activeTierId != "tier_free" && status == "Active"
}
