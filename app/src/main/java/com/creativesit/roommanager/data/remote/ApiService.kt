package com.creativesit.roommanager.data.remote

import com.creativesit.roommanager.data.model.*
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.*

interface ApiService {

    // ==================== AUTH ====================
    @POST("auth.php")
    suspend fun login(
        @Query("action") action: String = "login",
        @Body body: LoginRequest
    ): Response<ApiResponse<LoginData>>

    @GET("auth.php")
    suspend fun me(@Query("action") action: String = "me"): Response<ApiResponse<User>>

    // ==================== USERS ====================
    @GET("users.php")
    suspend fun getUsers(): Response<ApiResponse<List<User>>>

    @GET("users.php")
    suspend fun getUser(@Query("id") id: Int): Response<ApiResponse<User>>

    @POST("users.php")
    suspend fun createUser(@Body body: CreateUserRequest): Response<ApiResponse<GenericIdData>>

    @PUT("users.php")
    suspend fun updateUser(
        @Query("id") id: Int,
        @Body body: Map<String, @JvmSuppressWildcards Any?>
    ): Response<ApiResponse<Unit>>

    // ==================== RENT ====================
    @GET("rent.php")
    suspend fun rentHistory(
        @Query("action") action: String = "history",
        @Query("user_id") userId: Int? = null
    ): Response<ApiResponse<List<RentPayment>>>

    @GET("rent.php")
    suspend fun rentMonthlySummary(
        @Query("action") action: String = "monthly-summary",
        @Query("month") month: Int,
        @Query("year") year: Int
    ): Response<ApiResponse<List<RentSummaryRow>>>

    @GET("rent.php")
    suspend fun rentDueList(
        @Query("action") action: String = "due-list"
    ): Response<ApiResponse<List<RentDueRow>>>

    @POST("rent.php")
    suspend fun updatePayment(
        @Query("action") action: String = "update-payment",
        @Body body: UpdatePaymentRequest
    ): Response<ApiResponse<UpdatePaymentResult>>

    // ==================== DUTIES ====================
    @GET("duties.php")
    suspend fun dutiesByDate(
        @Query("action") action: String = "by-date",
        @Query("date") date: String
    ): Response<ApiResponse<List<Duty>>>

    @GET("duties.php")
    suspend fun dutiesByUser(
        @Query("action") action: String = "by-user",
        @Query("user_id") userId: Int? = null,
        @Query("duty_type") dutyType: String? = null
    ): Response<ApiResponse<List<Duty>>>

    @GET("duties.php")
    suspend fun dutiesByTypeMonth(
        @Query("action") action: String = "by-type-month",
        @Query("duty_type") dutyType: String,
        @Query("month") month: Int,
        @Query("year") year: Int
    ): Response<ApiResponse<DutyMonthData>>

    @POST("duties.php")
    suspend fun assignMonthlySchedule(
        @Query("action") action: String = "assign-month",
        @Body body: AssignMonthRequest
    ): Response<ApiResponse<Unit>>

    @PUT("duties.php")
    suspend fun updateDutyStatus(
        @Query("action") action: String = "status",
        @Query("id") id: Int,
        @Body body: DutyStatusRequest
    ): Response<ApiResponse<DutyStatusResult>>

    @GET("duties.php")
    suspend fun marketSummary(
        @Query("action") action: String = "market-summary",
        @Query("month") month: Int,
        @Query("year") year: Int
    ): Response<ApiResponse<MarketSummaryData>>

    @DELETE("duties.php")
    suspend fun deleteDuty(@Query("id") id: Int): Response<ApiResponse<Unit>>

    // ==================== NOTIFICATIONS ====================
    @POST("notifications.php")
    suspend fun sendCustomNotify(
        @Query("action") action: String = "send-custom",
        @Body body: SendCustomNotifyRequest
    ): Response<ApiResponse<Unit>>

    @POST("notifications.php")
    suspend fun sendDueList(
        @Query("action") action: String = "send-due-list",
        @Body body: SendDueListRequest
    ): Response<ApiResponse<Unit>>

    @POST("notifications.php")
    suspend fun sendMarketSummary(
        @Query("action") action: String = "send-market-summary",
        @Body body: Map<String, @JvmSuppressWildcards Any?>
    ): Response<ApiResponse<Unit>>

    @GET("notifications.php")
    suspend fun notificationLogs(
        @Query("action") action: String = "logs",
        @Query("limit") limit: Int = 40
    ): Response<ApiResponse<List<NotificationLog>>>

    @DELETE("notifications.php")
    suspend fun clearNotificationHistory(
        @Query("action") action: String = "clear-history"
    ): Response<ApiResponse<Unit>>

    // ==================== CHAT ====================
    @GET("chat.php")
    suspend fun chatGroup(
        @Query("action") action: String = "group",
        @Query("after_id") afterId: Int = 0
    ): Response<ApiResponse<List<ChatMessage>>>

    @GET("chat.php")
    suspend fun chatPrivate(
        @Query("action") action: String = "private",
        @Query("with") withUserId: Int
    ): Response<ApiResponse<List<ChatMessage>>>

    @GET("chat.php")
    suspend fun chatAdminInfo(
        @Query("action") action: String = "admin-info"
    ): Response<ApiResponse<User>>

    @POST("chat.php")
    suspend fun sendChatText(
        @Query("action") action: String = "send",
        @Body body: SendChatRequest
    ): Response<ApiResponse<GenericIdData>>

    @Multipart
    @POST("chat.php")
    suspend fun sendChatMedia(
        @Query("action") action: String = "send-media",
        @Part file: MultipartBody.Part,
        @Part("type") type: RequestBody,
        @Part("receiver_id") receiverId: RequestBody? = null
    ): Response<ApiResponse<GenericIdData>>

    @DELETE("chat.php")
    suspend fun deleteChatMessage(
        @Query("action") action: String = "delete",
        @Query("id") id: Int
    ): Response<ApiResponse<Unit>>

    // ==================== SETTINGS ====================
    @GET("settings.php")
    suspend fun getSettings(): Response<ApiResponse<SettingsMap>>

    @GET("settings.php")
    suspend fun getPublicSettings(
        @Query("action") action: String = "public"
    ): Response<ApiResponse<SettingsMap>>

    @POST("settings.php")
    suspend fun saveSettings(@Body body: Map<String, @JvmSuppressWildcards Any?>): Response<ApiResponse<Unit>>

    @POST("settings.php")
    suspend fun clearServerCache(
        @Query("action") action: String = "clear-cache"
    ): Response<ApiResponse<Unit>>

    @POST("settings.php")
    suspend fun testWhatsApp(
        @Query("action") action: String = "test-whatsapp",
        @Body body: Map<String, @JvmSuppressWildcards Any?>
    ): Response<ApiResponse<Map<String, @JvmSuppressWildcards Any?>>>

    @POST("rent.php")
    suspend fun generateMonthlyRent(
        @Query("action") action: String = "generate-monthly",
        @Body body: GenerateMonthlyRequest
    ): Response<ApiResponse<GenerateMonthlyResult>>
}
