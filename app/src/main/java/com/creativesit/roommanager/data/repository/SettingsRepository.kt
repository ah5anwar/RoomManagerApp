package com.creativesit.roommanager.data.repository

import com.creativesit.roommanager.data.local.SessionManager
import com.creativesit.roommanager.data.model.SettingsMap
import com.creativesit.roommanager.data.remote.ApiResult
import com.creativesit.roommanager.data.remote.RetrofitClient
import com.creativesit.roommanager.data.remote.safeApiCall

class SettingsRepository(private val sessionManager: SessionManager) {
    private val api get() = RetrofitClient.getApiService(sessionManager)

    suspend fun getSettings(): ApiResult<SettingsMap> = safeApiCall { api.getSettings() }

    suspend fun getPublicSettings(): ApiResult<SettingsMap> = safeApiCall { api.getPublicSettings() }

    suspend fun saveSettings(fields: Map<String, Any?>): ApiResult<Unit> =
        safeApiCall { api.saveSettings(fields) }

    suspend fun clearCache(): ApiResult<Unit> = safeApiCall { api.clearServerCache() }

    suspend fun testWhatsApp(phone: String): ApiResult<Map<String, Any?>> =
        safeApiCall { api.testWhatsApp(body = mapOf("phone" to phone)) }
}
