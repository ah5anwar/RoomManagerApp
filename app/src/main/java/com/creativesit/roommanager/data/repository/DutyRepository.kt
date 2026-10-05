package com.creativesit.roommanager.data.repository

import com.creativesit.roommanager.data.local.SessionManager
import com.creativesit.roommanager.data.model.*
import com.creativesit.roommanager.data.remote.ApiResult
import com.creativesit.roommanager.data.remote.RetrofitClient
import com.creativesit.roommanager.data.remote.safeApiCall

class DutyRepository(private val sessionManager: SessionManager) {
    private val api get() = RetrofitClient.getApiService(sessionManager)

    suspend fun byDate(date: String): ApiResult<List<Duty>> = safeApiCall { api.dutiesByDate(date = date) }

    suspend fun byUser(userId: Int? = null, dutyType: String? = null): ApiResult<List<Duty>> =
        safeApiCall { api.dutiesByUser(userId = userId, dutyType = dutyType) }

    suspend fun byTypeMonth(dutyType: String, month: Int, year: Int): ApiResult<DutyMonthData> =
        safeApiCall { api.dutiesByTypeMonth(dutyType = dutyType, month = month, year = year) }

    suspend fun assignMonth(req: AssignMonthRequest): ApiResult<Unit> =
        safeApiCall { api.assignMonthlySchedule(body = req) }

    suspend fun updateStatus(id: Int, status: String, amount: Double? = null): ApiResult<DutyStatusResult> =
        safeApiCall { api.updateDutyStatus(id = id, body = DutyStatusRequest(status, amount)) }

    suspend fun marketSummary(month: Int, year: Int): ApiResult<MarketSummaryData> =
        safeApiCall { api.marketSummary(month = month, year = year) }

    suspend fun delete(id: Int): ApiResult<Unit> = safeApiCall { api.deleteDuty(id) }
}
