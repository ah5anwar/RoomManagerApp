package com.creativesit.roommanager.data.remote

/** UI স্তরে সহজে হ্যান্ডল করার জন্য একটা সিল্‌ড রেজাল্ট টাইপ */
sealed class ApiResult<out T> {
    data class Success<T>(val data: T, val message: String? = null) : ApiResult<T>()
    data class Error(val message: String) : ApiResult<Nothing>()
}

/**
 * retrofit2.Response<ApiResponse<T>> কে নিরাপদে ApiResult এ রূপান্তর করে —
 * নেটওয়ার্ক এরর, HTTP এরর, ও success:false — সবকিছু একইভাবে হ্যান্ডল হয়।
 */
suspend fun <T> safeApiCall(block: suspend () -> retrofit2.Response<com.creativesit.roommanager.data.model.ApiResponse<T>>): ApiResult<T> {
    return try {
        val response = block()
        val body = response.body()
        if (response.code() == 401) {
            com.creativesit.roommanager.AppContainer.sessionManager.notifySessionExpired()
            return ApiResult.Error("সেশনের মেয়াদ শেষ হয়ে গেছে, আবার লগইন করুন")
        }
        if (response.isSuccessful && body != null && body.success) {
            @Suppress("UNCHECKED_CAST")
            ApiResult.Success(body.data as T, body.message)
        } else {
            val errMsg = body?.message ?: parseErrorBody(response.errorBody()?.string()) ?: "সার্ভার এরর (${response.code()})"
            ApiResult.Error(errMsg)
        }
    } catch (e: java.net.UnknownHostException) {
        ApiResult.Error("ইন্টারনেট সংযোগ নেই বা সার্ভার URL ভুল")
    } catch (e: java.net.SocketTimeoutException) {
        ApiResult.Error("সার্ভার সাড়া দিতে দেরি করছে, আবার চেষ্টা করুন")
    } catch (e: Exception) {
        ApiResult.Error(e.message ?: "অজানা এরর হয়েছে")
    }
}

private fun parseErrorBody(raw: String?): String? {
    if (raw.isNullOrBlank()) return null
    return try {
        val obj = com.google.gson.JsonParser.parseString(raw).asJsonObject
        obj.get("message")?.asString
    } catch (e: Exception) {
        null
    }
}
