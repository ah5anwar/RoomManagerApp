package com.creativesit.roommanager.ui.admin

import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.creativesit.roommanager.AppContainer
import com.creativesit.roommanager.data.model.NotificationLog
import com.creativesit.roommanager.data.model.User
import com.creativesit.roommanager.data.remote.ApiResult
import com.creativesit.roommanager.ui.common.*
import com.creativesit.roommanager.ui.theme.InkSoft
import com.creativesit.roommanager.ui.theme.Rule
import com.creativesit.roommanager.util.formatDateTime
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminNotifyScreen() {
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    var customers by remember { mutableStateOf<List<User>>(emptyList()) }
    var selectedTargetId by remember { mutableStateOf<Int?>(null) } // null মানে "সবাইকে"
    var message by remember { mutableStateOf("") }
    var sending by remember { mutableStateOf(false) }
    var logs by remember { mutableStateOf<List<NotificationLog>>(emptyList()) }
    var refreshTrigger by remember { mutableStateOf(0) }

    LaunchedEffect(refreshTrigger) {
        val usersRes = AppContainer.userRepository.getUsers()
        if (usersRes is ApiResult.Success) customers = usersRes.data.filter { it.role == "customer" }
        val logsRes = AppContainer.notificationRepository.logs()
        if (logsRes is ApiResult.Success) logs = logsRes.data
    }

    ScrollScreen {
        SectionCard("কাস্টম মেসেজ পাঠান") {
            val targetOptions = listOf("সবাইকে") + customers.map { "${it.name} (${it.user_code})" }
            val selectedIdx = customers.indexOfFirst { it.id == selectedTargetId }.let { if (it == -1) 0 else it + 1 }
            SimpleDropdown(
                label = "প্রাপক",
                selectedLabel = targetOptions.getOrElse(selectedIdx) { "সবাইকে" },
                options = targetOptions,
                onSelect = { idx -> selectedTargetId = if (idx == 0) null else customers.getOrNull(idx - 1)?.id },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(10.dp))
            OutlinedTextField(
                value = message, onValueChange = { message = it },
                label = { Text("মেসেজ") }, minLines = 3,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(12.dp))
            PrimaryButton(
                text = if (sending) "পাঠানো হচ্ছে..." else "WhatsApp এ পাঠান",
                enabled = !sending,
                onClick = {
                    if (message.isBlank()) { Toast.makeText(context, "মেসেজ লিখুন", Toast.LENGTH_SHORT).show(); return@PrimaryButton }
                    sending = true
                    scope.launch {
                        val ids = selectedTargetId?.let { listOf(it) } ?: emptyList()
                        val res = AppContainer.notificationRepository.sendCustom(message, ids)
                        sending = false
                        when (res) {
                            is ApiResult.Success -> { message = ""; Toast.makeText(context, res.message ?: "পাঠানো হয়েছে", Toast.LENGTH_SHORT).show(); refreshTrigger++ }
                            is ApiResult.Error -> Toast.makeText(context, res.message, Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            )
        }

        Spacer(Modifier.height(14.dp))

        SectionCard("নোটিফিকেশন হিস্ট্রি") {
            if (logs.isEmpty()) EmptyState("কোনো ইতিহাস নেই")
            else Column {
                logs.forEach { l ->
                    Row(Modifier.fillMaxWidth().padding(vertical = 8.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                        Column(Modifier.weight(1f).padding(end = 8.dp)) {
                            Text("${l.name ?: ""} — ${l.category}", fontWeight = FontWeight.Medium, fontSize = 12.sp)
                            Text(l.message.take(70), fontSize = 11.sp, color = InkSoft, maxLines = 2)
                            if (l.status == "failed" && !l.error_message.isNullOrBlank()) {
                                Text("⚠️ ${l.error_message}", fontSize = 10.sp, color = Rule, maxLines = 2)
                            }
                            Text(formatDateTime(l.created_at), fontSize = 10.sp, color = InkSoft)
                        }
                        StatusBadge(l.status)
                    }
                    Divider()
                }
            }
        }
    }
}
