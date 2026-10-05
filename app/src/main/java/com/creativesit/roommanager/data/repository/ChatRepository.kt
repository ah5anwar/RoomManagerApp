package com.creativesit.roommanager.data.repository

import android.content.Context
import android.net.Uri
import com.creativesit.roommanager.data.local.SessionManager
import com.creativesit.roommanager.data.model.*
import com.creativesit.roommanager.data.remote.ApiResult
import com.creativesit.roommanager.data.remote.RetrofitClient
import com.creativesit.roommanager.data.remote.safeApiCall
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File

class ChatRepository(private val context: Context, private val sessionManager: SessionManager) {
    private val api get() = RetrofitClient.getApiService(sessionManager)

    suspend fun group(afterId: Int = 0): ApiResult<List<ChatMessage>> =
        safeApiCall { api.chatGroup(afterId = afterId) }

    suspend fun private(withUserId: Int): ApiResult<List<ChatMessage>> =
        safeApiCall { api.chatPrivate(withUserId = withUserId) }

    suspend fun adminInfo(): ApiResult<User> = safeApiCall { api.chatAdminInfo() }

    suspend fun deleteMessage(id: Int): ApiResult<Unit> = safeApiCall { api.deleteChatMessage(id = id) }

    suspend fun sendText(content: String, receiverId: Int? = null): ApiResult<GenericIdData> =
        safeApiCall { api.sendChatText(body = SendChatRequest(content, receiverId)) }

    /** [uri] থেকে সাময়িক ফাইল বানিয়ে আপলোড করে (ছবি/গ্যালারি থেকে বেছে নেওয়া ভয়েস) */
    suspend fun sendMedia(uri: Uri, type: String, receiverId: Int? = null): ApiResult<GenericIdData> {
        val tempFile = uriToTempFile(uri, type)
        return sendMediaFile(tempFile, type, receiverId)
    }

    /** সরাসরি রেকর্ড করা ফাইল (যেমন in-app ভয়েস রেকর্ডিং) আপলোড করে */
    suspend fun sendMediaFile(file: File, type: String, receiverId: Int? = null): ApiResult<GenericIdData> {
        val mediaType = if (type == "voice") "audio/*" else "image/*"
        val filePart = MultipartBody.Part.createFormData(
            "file", file.name, file.asRequestBody(mediaType.toMediaTypeOrNull())
        )
        val typePart = type.toRequestBody("text/plain".toMediaTypeOrNull())
        val receiverPart = receiverId?.toString()?.toRequestBody("text/plain".toMediaTypeOrNull())
        return safeApiCall { api.sendChatMedia(file = filePart, type = typePart, receiverId = receiverPart) }
    }

    private fun uriToTempFile(uri: Uri, type: String): File {
        val ext = if (type == "voice") "m4a" else "jpg"
        val tempFile = File.createTempFile("upload_", ".$ext", context.cacheDir)
        context.contentResolver.openInputStream(uri)?.use { input ->
            tempFile.outputStream().use { output -> input.copyTo(output) }
        }
        return tempFile
    }
}
