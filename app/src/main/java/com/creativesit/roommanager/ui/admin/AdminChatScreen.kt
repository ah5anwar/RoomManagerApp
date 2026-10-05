package com.creativesit.roommanager.ui.admin

import androidx.compose.runtime.*
import com.creativesit.roommanager.AppContainer
import com.creativesit.roommanager.data.model.User
import com.creativesit.roommanager.data.remote.ApiResult
import com.creativesit.roommanager.ui.common.ChatContent

@Composable
fun AdminChatScreen(currentUser: User) {
    var customers by remember { mutableStateOf<List<User>>(emptyList()) }

    LaunchedEffect(Unit) {
        val res = AppContainer.userRepository.getUsers()
        if (res is ApiResult.Success) customers = res.data.filter { it.role == "customer" }
    }

    ChatContent(currentUser = currentUser, allowPrivateTab = true, otherUsers = customers)
}
