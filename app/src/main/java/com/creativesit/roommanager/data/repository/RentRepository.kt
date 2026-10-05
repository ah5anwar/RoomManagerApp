package com.creativesit.roommanager.data.repository

import com.creativesit.roommanager.data.local.SessionManager
import com.creativesit.roommanager.data.model.*
import com.creativesit.roommanager.data.remote.ApiResult
import com.creativesit.roommanager.data.remote.RetrofitClient
import com.creativesit.roommanager.data.remote.safeApiCall

class RentRepository(private val sessionManager: SessionManager) {
    private val api get() = RetrofitClient.getApiService(sessionManager)

    suspend fun history(userId: Int? = null): ApiResult<List<RentPayment>> =
        safeApiCall { api.rentHistory(userId = userId) }

    suspend fun monthlySummary(month: Int, year: Int): ApiResult<List<RentSummaryRow>> =
        safeApiCall { api.rentMonthlySummary(month = month, year = year) }

    suspend fun dueList(): ApiResult<List<RentDueRow>> = safeApiCall { api.rentDueList() }

    suspend fun updatePayment(req: UpdatePaymentRequest): ApiResult<UpdatePaymentResult> =
        safeApiCall { api.updatePayment(body = req) }

    suspend fun generateMonthly(month: Int, year: Int): ApiResult<GenerateMonthlyResult> =
        safeApiCall { api.generateMonthlyRent(body = GenerateMonthlyRequest(month, year)) }
}
