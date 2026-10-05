package com.creativesit.roommanager.ui.customer

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.creativesit.roommanager.AppContainer
import com.creativesit.roommanager.data.model.NotificationLog
import com.creativesit.roommanager.data.remote.ApiResult
import com.creativesit.roommanager.ui.common.*
import com.creativesit.roommanager.ui.theme.InkSoft
import com.creativesit.roommanager.util.formatDateTime

/** গ্রাহকের নিজের জন্য পাঠানো সব নোটিফিকেশন (ভাড়া, ডিউটি রিমাইন্ডার, কাস্টম মেসেজ) - অ্যাপ-ভিতরের নোটিফিকেশন সেন্টার */
@Composable
fun CustomerNotificationsScreen() {
    var loading by remember { mutableStateOf(true) }
    var logs by remember { mutableStateOf<List<NotificationLog>>(emptyList()) }

    LaunchedEffect(Unit) {
        val res = AppContainer.notificationRepository.logs(limit = 50)
        if (res is ApiResult.Success) logs = res.data
        loading = false
    }

    ScrollScreen {
        SectionCard("🔔 আমার নোটিফিকেশন") {
            if (loading) LoadingBox()
            else if (logs.isEmpty()) EmptyState("এখনো কোনো নোটিফিকেশন আসেনি")
            else Column {
                logs.forEach { l ->
                    Row(Modifier.fillMaxWidth().padding(vertical = 10.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                        Column(Modifier.weight(1f).padding(end = 8.dp)) {
                            Text(categoryLabel(l.category), fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                            Text(l.message, fontSize = 12.sp, color = InkSoft)
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

private fun categoryLabel(category: String): String = when (category) {
    "duty_reminder" -> "🧹 দায়িত্ব রিমাইন্ডার"
    "rent_due" -> "৳ ভাড়া বকেয়া"
    "rent_paid" -> "৳ ভাড়া পরিশোধ"
    "rent_overdue_list" -> "৳ বকেয়ার তালিকা"
    "custom" -> "📢 এডমিনের মেসেজ"
    else -> category
}
