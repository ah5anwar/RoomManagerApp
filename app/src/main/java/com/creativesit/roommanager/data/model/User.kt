package com.creativesit.roommanager.data.model

data class User(
    val id: Int,
    val user_code: String,
    val name: String,
    val phone: String? = null,
    val whatsapp_number: String? = null,
    val email: String? = null,
    val room_no: String? = null,
    val role: String, // admin | manager | customer
    val permissions: List<String>? = emptyList(),
    val monthly_rent: Double? = 0.0,
    val join_date: String? = null,
    val status: String? = "active"
) {
    val isAdmin get() = role == "admin"
    val isManager get() = role == "manager"
    val isCustomer get() = role == "customer"
    val isStaff get() = isAdmin || isManager

    fun hasPermission(perm: String): Boolean = permissions?.contains(perm) == true
}

data class LoginData(
    val user: User,
    val token: String,
    val expires_at: String? = null
)

data class LoginRequest(val user_code: String, val password: String)

data class CreateUserRequest(
    val role: String,
    val name: String,
    val phone: String,
    val whatsapp_number: String,
    val room_no: String? = null,
    val monthly_rent: Double? = null,
    val status: String = "active",
    val permissions: List<String> = emptyList(),
    val password: String? = null
)
