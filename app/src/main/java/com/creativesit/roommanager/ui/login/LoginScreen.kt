package com.creativesit.roommanager.ui.login

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.creativesit.roommanager.AppContainer
import com.creativesit.roommanager.data.model.User
import com.creativesit.roommanager.data.remote.ApiResult
import com.creativesit.roommanager.ui.common.ErrorBanner
import com.creativesit.roommanager.ui.common.PrimaryButton
import com.creativesit.roommanager.ui.theme.*
import kotlinx.coroutines.launch

@Composable
fun LoginScreen(onLoginSuccess: (User) -> Unit) {
    var userCode by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var errorMsg by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    fun doLogin() {
        if (userCode.isBlank() || password.isBlank()) {
            errorMsg = "ইউজার আইডি ও পাসওয়ার্ড দিন"
            return
        }
        errorMsg = null
        isLoading = true
        scope.launch {
            val result = AppContainer.authRepository.login(userCode.trim(), password)
            isLoading = false
            when (result) {
                is ApiResult.Success -> onLoginSuccess(result.data.user)
                is ApiResult.Error -> errorMsg = result.message
            }
        }
    }

    Box(
        Modifier.fillMaxSize().background(Paper),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier.padding(24.dp).fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = CardBg),
            border = androidx.compose.foundation.BorderStroke(1.dp, PaperLine),
            shape = RoundedCornerShape(14.dp)
        ) {
            Column(
                Modifier.padding(28.dp).fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    Modifier.size(60.dp)
                        .border(3.dp, Rule, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text("RM", color = Rule, fontSize = 22.sp, fontWeight = FontWeight.Bold)
                }
                Spacer(Modifier.height(14.dp))
                Text("রুম ম্যানেজার", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Ink)
                Text("আপনার ইউজার আইডি ও পাসওয়ার্ড দিয়ে প্রবেশ করুন", fontSize = 12.sp, color = InkSoft)
                Spacer(Modifier.height(20.dp))

                OutlinedTextField(
                    value = userCode,
                    onValueChange = { userCode = it },
                    label = { Text("ইউজার আইডি") },
                    placeholder = { Text("AH-001") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Rule)
                )
                Spacer(Modifier.height(12.dp))
                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("পাসওয়ার্ড") },
                    singleLine = true,
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Rule)
                )
                Spacer(Modifier.height(18.dp))

                if (errorMsg != null) {
                    ErrorBanner(errorMsg!!, Modifier.padding(bottom = 12.dp))
                }

                PrimaryButton(
                    text = if (isLoading) "..." else "প্রবেশ করুন",
                    onClick = { doLogin() },
                    enabled = !isLoading,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}
