package com.creativesit.roommanager.ui.customer

import androidx.compose.runtime.*
import com.creativesit.roommanager.AppContainer
import com.creativesit.roommanager.data.model.User
import com.creativesit.roommanager.data.remote.ApiResult
import com.creativesit.roommanager.ui.common.ChatContent

@Composable
fun CustomerChatScreen(user: User) {
    var adminUser by remember { mutableStateOf<User?>(null) }

    LaunchedEffect(Unit) {
        val res = AppContainer.chatRepository.adminInfo()
        if (res is ApiResult.Success) adminUser = res.data
    }

    // গ্রাহক গ্রুপ চ্যাট করতে পারে, আর এডমিনের সাথে নিজের প্রাইভেট থ্রেড দেখতে পারে
    // (শুধু এডমিন প্রাইভেট মেসেজ শুরু করতে পারেন, কিন্তু গ্রাহক সেই মেসেজ দেখতে ও রিপ্লাই দিতে পারবে)
    ChatContent(
        currentUser = user,
        allowPrivateTab = adminUser != null,
        otherUsers = adminUser?.let { listOf(it) } ?: emptyList()
    )
}
