package com.creativesit.roommanager.data.model

/**
 * সব API এন্ডপয়েন্ট একই ফরম্যাটে রেসপন্স দেয়: { success, message, data }
 * তাই একটা জেনেরিক র‍্যাপার ব্যবহার করা হচ্ছে।
 */
data class ApiResponse<T>(
    val success: Boolean,
    val message: String? = null,
    val data: T? = null
)

/** ভাড়া/ডিউটি অ্যাসাইন-মান্থ এর মতো এন্ডপয়েন্ট যেখানে data একটা অবজেক্ট (নাম্বার/আইডি সহ) */
data class GenericIdData(
    val id: Any? = null,
    val user_code: String? = null,
    val role: String? = null,
    val file_path: String? = null
)
