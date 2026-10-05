package com.creativesit.roommanager.data.repository

import com.creativesit.roommanager.data.local.SessionManager
import com.creativesit.roommanager.data.model.LoginData
import com.creativesit.roommanager.data.model.LoginRequest
import com.creativesit.roommanager.data.model.User
import com.creativesit.roommanager.data.remote.ApiResult
import com.creativesit.roommanager.data.remote.RetrofitClient
import com.creativesit.roommanager.data.remote.safeApiCall

class AuthRepository(private val sessionManager: SessionManager) {
    private val api get() = RetrofitClient.getApiService(sessionManager)

    suspend fun login(userCode: String, password: String): ApiResult<LoginData> {
        val result = safeApiCall { api.login(body = LoginRequest(userCode, password)) }
        if (result is ApiResult.Success) {
            sessionManager.saveSession(result.data.token, result.data.user)
        }
        return result
    }

    /**
     * সার্ভার থেকে সর্বশেষ ইউজার তথ্য (বিশেষত পারমিশন) এনে লোকাল সেশন আপডেট করে —
     * এডমিন যদি লগইনের পরে পারমিশন বদলে দেন, এটা কল না করলে পুরনো তথ্যই দেখাতে থাকবে।
     */
    suspend fun refreshMe(): ApiResult<User> {
        val result = safeApiCall { api.me() }
        if (result is ApiResult.Success) {
            sessionManager.updateUser(result.data)
        }
        return result
    }

    suspend fun logout() {
        sessionManager.clearSession()
        RetrofitClient.reset()
    }
}
