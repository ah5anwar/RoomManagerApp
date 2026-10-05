package com.creativesit.roommanager.data.model

data class Duty(
    val id: Int,
    val duty_type: String, // market | cooking | trash | cleaning
    val assigned_user_id: Int,
    val duty_date: String,
    val status: String = "pending", // pending | done | missed
    val amount: Double? = null, // শুধু market ডিউটির খরচ (টাকা)
    val name: String? = null,
    val user_code: String? = null,
    val room_no: String? = null
)

/** ডিউটি টাইপের নাম, আইকন — অ্যাপজুড়ে ব্যবহারের জন্য কেন্দ্রীয় সংজ্ঞা */
enum class DutyType(val apiValue: String, val label: String, val icon: String) {
    MARKET("market", "বাজার করা", "🛒"),
    COOKING("cooking", "রান্না করা", "🍳"),
    TRASH("trash", "ময়লা ফেলা", "🗑️"),
    CLEANING("cleaning", "রুম ক্লিন করা", "🧹");

    companion object {
        fun fromApi(value: String) = entries.find { it.apiValue == value } ?: MARKET
        val ALL = entries.toList()
    }
}

data class EligibleUser(
    val id: Int,
    val user_code: String,
    val name: String,
    val room_no: String? = null
)

/** duties.php?action=by-type-month এর data অংশ */
data class DutyMonthData(
    val eligible_users: List<EligibleUser> = emptyList(),
    val assignments: List<Duty> = emptyList()
)

data class AssignMonthRequest(
    val duty_type: String,
    val month: Int,
    val year: Int,
    val assignments: Map<String, Int?> // দিন (স্ট্রিং) -> ইউজার আইডি (null মানে খালি)
)

data class DutyStatusRequest(val status: String, val amount: Double? = null)
data class DutyStatusResult(val amount: Double? = null)

/** duties.php?action=market-summary এর data অংশ */
data class MarketSummaryUser(val user_id: Int, val name: String, val user_code: String, val total: Double)
data class MarketSummaryEntry(val id: Int, val duty_date: String, val assigned_user_id: Int, val amount: Double?, val status: String, val name: String, val user_code: String)
data class MarketSummaryData(
    val entries: List<MarketSummaryEntry> = emptyList(),
    val total: Double = 0.0,
    val by_user: List<MarketSummaryUser> = emptyList()
)
