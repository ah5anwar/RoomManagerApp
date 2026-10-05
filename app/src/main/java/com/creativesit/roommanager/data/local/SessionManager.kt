package com.creativesit.roommanager.data.local

import android.content.Context
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import com.creativesit.roommanager.data.model.User
import com.google.gson.Gson
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking

private val Context.dataStore by preferencesDataStore(name = "room_manager_session")

/**
 * লগইন টোকেন ও বর্তমান ইউজার তথ্য স্থায়ীভাবে সংরক্ষণ করে।
 * OkHttp Interceptor থেকে সিঙ্ক্রোনাসভাবে টোকেন পড়ার দরকার হয় বলে [getTokenBlocking] রাখা হয়েছে।
 */
class SessionManager(private val context: Context) {

    private val gson = Gson()

    companion object {
        private val KEY_TOKEN = stringPreferencesKey("auth_token")
        private val KEY_USER_JSON = stringPreferencesKey("user_json")
    }

    val tokenFlow: Flow<String?> = context.dataStore.data.map { it[KEY_TOKEN] }

    val userFlow: Flow<User?> = context.dataStore.data.map { prefs ->
        prefs[KEY_USER_JSON]?.let { runCatching { gson.fromJson(it, User::class.java) }.getOrNull() }
    }

    /** টোকেনের মেয়াদ শেষ হয়ে গেলে (সার্ভার 401 দিলে) এটা true হয়ে যায়, UI তখন লগইন স্ক্রিনে ফিরিয়ে দেয় */
    val sessionExpired = MutableStateFlow(false)
    fun notifySessionExpired() { sessionExpired.value = true }
    fun consumeSessionExpiredFlag() { sessionExpired.value = false }

    suspend fun saveSession(token: String, user: User) {
        context.dataStore.edit { prefs ->
            prefs[KEY_TOKEN] = token
            prefs[KEY_USER_JSON] = gson.toJson(user)
        }
    }

    suspend fun updateUser(user: User) {
        context.dataStore.edit { prefs -> prefs[KEY_USER_JSON] = gson.toJson(user) }
    }

    suspend fun clearSession() {
        context.dataStore.edit { prefs ->
            prefs.remove(KEY_TOKEN)
            prefs.remove(KEY_USER_JSON)
        }
    }

    suspend fun getUserOnce(): User? = userFlow.first()
    suspend fun getTokenOnce(): String? = tokenFlow.first()

    /** Interceptor-এর জন্য — OkHttp এর intercept() ব্যাকগ্রাউন্ড থ্রেডে চলে বলে এখানে ব্লকিং রিড নিরাপদ */
    fun getTokenBlocking(): String? = runBlocking { tokenFlow.first() }
}
