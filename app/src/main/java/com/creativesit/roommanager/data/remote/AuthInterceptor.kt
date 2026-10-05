package com.creativesit.roommanager.data.remote

import com.creativesit.roommanager.data.local.SessionManager
import okhttp3.Interceptor
import okhttp3.Response

/** প্রতিটা রিকোয়েস্টে সেভ করা টোকেন থাকলে Authorization: Bearer <token> হেডার যোগ করে */
class AuthInterceptor(private val sessionManager: SessionManager) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val token = sessionManager.getTokenBlocking()
        val request = chain.request().newBuilder().apply {
            if (!token.isNullOrBlank()) {
                addHeader("Authorization", "Bearer $token")
            }
        }.build()
        return chain.proceed(request)
    }
}
