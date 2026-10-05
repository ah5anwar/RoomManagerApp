package com.creativesit.roommanager.data.model

data class ChatMessage(
    val id: Int,
    val sender_id: Int,
    val receiver_id: Int? = null,
    val is_group: Int = 1,
    val message_type: String = "text", // text | image | voice
    val content: String? = null,
    val file_path: String? = null,
    val name: String? = null,
    val user_code: String? = null,
    val created_at: String? = null
)

data class SendChatRequest(val content: String, val receiver_id: Int? = null)

data class NotificationLog(
    val id: Int,
    val user_id: Int,
    val name: String? = null,
    val category: String,
    val message: String,
    val status: String, // queued | sent | delivered | read | failed
    val error_message: String? = null,
    val created_at: String
)

data class SendCustomNotifyRequest(val message: String, val user_ids: List<Int> = emptyList())
data class SendDueListRequest(val user_id: Int)

/** settings.php GET রেসপন্স ডাইনামিক key-value, তাই Map হিসেবে পার্স করা হয় */
typealias SettingsMap = Map<String, String>

data class GenerateMonthlyRequest(val month: Int, val year: Int)
data class GenerateMonthlyResult(val created: Int = 0, val skipped: Int = 0)
