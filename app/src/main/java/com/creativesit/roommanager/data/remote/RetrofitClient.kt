package com.creativesit.roommanager.data.remote

import com.creativesit.roommanager.BuildConfig
import com.creativesit.roommanager.data.local.SessionManager
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

/**
 * Retrofit ইনস্টান্স একবারই তৈরি হয় (singleton) — সার্ভার URL কম্পাইল-টাইমে
 * BuildConfig.BASE_URL থেকে আসে, তাই প্রতিটা কলে DataStore থেকে ব্লকিং রিড করার
 * দরকার নেই (যেটা UI থ্রেডে জ্যাঙ্ক তৈরি করতে পারত)।
 */
object RetrofitClient {

    @Volatile private var apiService: ApiService? = null

    fun getApiService(sessionManager: SessionManager): ApiService {
        apiService?.let { return it }

        synchronized(this) {
            apiService?.let { return it }

            val logging = HttpLoggingInterceptor().apply {
                level = if (BuildConfig.DEBUG) HttpLoggingInterceptor.Level.BODY else HttpLoggingInterceptor.Level.NONE
            }

            val client = OkHttpClient.Builder()
                .addInterceptor(AuthInterceptor(sessionManager))
                .addInterceptor(logging)
                .connectTimeout(20, TimeUnit.SECONDS)
                .readTimeout(20, TimeUnit.SECONDS)
                .writeTimeout(20, TimeUnit.SECONDS)
                .build()

            val retrofit = Retrofit.Builder()
                .baseUrl(BuildConfig.BASE_URL)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build()

            val service = retrofit.create(ApiService::class.java)
            apiService = service
            return service
        }
    }

    fun reset() {
        apiService = null
    }
}
