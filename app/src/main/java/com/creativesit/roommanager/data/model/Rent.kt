package com.creativesit.roommanager.data.model

data class RentPayment(
    val id: Int? = null,
    val user_id: Int,
    val month: Int,
    val year: Int,
    val amount_due: Double,
    val amount_paid: Double = 0.0,
    val status: String = "due", // paid | due | partial
    val payment_date: String? = null,
    val note: String? = null
)

/** rent.php?action=monthly-summary এর প্রতিটা রো */
data class RentSummaryRow(
    val user_id: Int,
    val user_code: String,
    val name: String,
    val room_no: String? = null,
    val monthly_rent: Double? = null,
    val payment_id: Int? = null,
    val amount_due: Double? = null,
    val amount_paid: Double? = null,
    val status: String? = "due",
    val payment_date: String? = null
)

/** rent.php?action=due-list এর প্রতিটা রো */
data class RentDueRow(
    val user_id: Int,
    val user_code: String,
    val name: String,
    val room_no: String? = null,
    val whatsapp_number: String? = null,
    val month: Int,
    val year: Int,
    val due_amount: Double
)

data class UpdatePaymentRequest(
    val user_id: Int,
    val month: Int,
    val year: Int,
    val amount_paid: Double,
    val amount_due: Double? = null,
    val note: String? = null
)

/** update-payment এর রেসপন্সে WhatsApp আসলেই পাঠানো গেছে কিনা তা থাকে */
data class UpdatePaymentResult(val whatsapp_sent: Boolean = false)
