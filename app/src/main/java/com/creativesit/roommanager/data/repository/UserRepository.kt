package com.creativesit.roommanager.data.repository

import com.creativesit.roommanager.data.local.SessionManager
import com.creativesit.roommanager.data.model.CreateUserRequest
import com.creativesit.roommanager.data.model.GenericIdData
import com.creativesit.roommanager.data.model.User
import com.creativesit.roommanager.data.remote.ApiResult
import com.creativesit.roommanager.data.remote.RetrofitClient
import com.creativesit.roommanager.data.remote.safeApiCall

class UserRepository(private val sessionManager: SessionManager) {
    private val api get() = RetrofitClient.getApiService(sessionManager)

    suspend fun getUsers(): ApiResult<List<User>> = safeApiCall { api.getUsers() }

    suspend fun createUser(req: CreateUserRequest): ApiResult<GenericIdData> =
        safeApiCall { api.createUser(req) }

    suspend fun updateUser(id: Int, fields: Map<String, Any?>): ApiResult<Unit> =
        safeApiCall { api.updateUser(id, fields) }
}
