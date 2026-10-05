package com.creativesit.roommanager

import android.app.Application
import com.creativesit.roommanager.data.local.SessionManager
import com.creativesit.roommanager.data.repository.*

/**
 * ছোট, ম্যানুয়াল DI কনটেইনার — Hilt/Dagger ছাড়াই সব রিপোজিটরি এক জায়গা থেকে অ্যাক্সেস করার জন্য।
 * ViewModel গুলো AppContainer.xxxRepository ব্যবহার করবে।
 */
object AppContainer {
    lateinit var sessionManager: SessionManager
        private set

    lateinit var authRepository: AuthRepository
        private set
    lateinit var userRepository: UserRepository
        private set
    lateinit var rentRepository: RentRepository
        private set
    lateinit var dutyRepository: DutyRepository
        private set
    lateinit var notificationRepository: NotificationRepository
        private set
    lateinit var chatRepository: ChatRepository
        private set
    lateinit var settingsRepository: SettingsRepository
        private set

    fun init(app: Application) {
        sessionManager = SessionManager(app.applicationContext)
        authRepository = AuthRepository(sessionManager)
        userRepository = UserRepository(sessionManager)
        rentRepository = RentRepository(sessionManager)
        dutyRepository = DutyRepository(sessionManager)
        notificationRepository = NotificationRepository(sessionManager)
        chatRepository = ChatRepository(app.applicationContext, sessionManager)
        settingsRepository = SettingsRepository(sessionManager)
    }
}

class RoomManagerApp : Application() {
    override fun onCreate() {
        super.onCreate()
        AppContainer.init(this)
    }
}
