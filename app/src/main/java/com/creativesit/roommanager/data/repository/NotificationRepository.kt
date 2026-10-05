package com.creativesit.roommanager.data.repository

import com.creativesit.roommanager.data.local.SessionManager
import com.creativesit.roommanager.data.model.*
import com.creativesit.roommanager.data.remote.ApiResult
import com.creativesit.roommanager.data.remote.RetrofitClient
import com.creativesit.roommanager.data.remote.safeApiCall

class NotificationRepository(private val sessionManager: SessionManager) {
    private val api get() = RetrofitClient.getApiService(sessionManager)

    suspend fun sendCustom(message: String, userIds: List<Int>): ApiResult<Unit> =
        safeApiCall { api.sendCustomNotify(body = SendCustomNotifyRequest(message, userIds)) }

    suspend fun sendDueList(userId: Int): ApiResult<Unit> =
        safeApiCall { api.sendDueList(body = SendDueListRequest(userId)) }

    suspend fun sendMarketSummary(month: Int, year: Int): ApiResult<Unit> =
        safeApiCall { api.sendMarketSummary(body = mapOf("month" to month, "year" to year)) }

    suspend fun logs(limit: Int = 40): ApiResult<List<NotificationLog>> =
        safeApiCall { api.notificationLogs(limit = limit) }

    suspend fun clearHistory(): ApiResult<Unit> = safeApiCall { api.clearNotificationHistory() }
}
