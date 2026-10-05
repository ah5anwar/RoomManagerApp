package com.creativesit.roommanager.ui.customer

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.creativesit.roommanager.AppContainer
import com.creativesit.roommanager.data.model.Duty
import com.creativesit.roommanager.data.model.DutyType
import com.creativesit.roommanager.data.model.User
import com.creativesit.roommanager.data.remote.ApiResult
import com.creativesit.roommanager.ui.common.*
import com.creativesit.roommanager.ui.theme.*
import com.creativesit.roommanager.util.formatMoney
import com.creativesit.roommanager.util.todayIso
import kotlinx.coroutines.launch

@Composable
fun CustomerHomeScreen(user: User) {
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    var loading by remember { mutableStateOf(true) }
    var totalDue by remember { mutableStateOf(0.0) }
    var todayDuties by remember { mutableStateOf<List<Duty>>(emptyList()) }
    var refreshTrigger by remember { mutableStateOf(0) }

    LaunchedEffect(refreshTrigger) {
        loading = true
        val rentRes = AppContainer.rentRepository.history()
        if (rentRes is ApiResult.Success) {
            totalDue = rentRes.data.filter { it.status != "paid" }.sumOf { it.amount_due - it.amount_paid }
        }
        val dutyRes = AppContainer.dutyRepository.byDate(todayIso())
        if (dutyRes is ApiResult.Success) {
            // এডমিন যেসব দায়িত্বে পারমিশন দিয়েছে সেগুলোর আজকের পুরো তালিকা (শুধু নিজেরটা না, সবার নাম)
            val perms = user.permissions ?: emptyList()
            todayDuties = dutyRes.data.filter { perms.contains(it.duty_type) }
        }
        loading = false
    }

    ScrollScreen {
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            StatCard("৳${formatMoney(totalDue)}", "মোট বকেয়া", Modifier.weight(1f))
            StatCard("${todayDuties.count { it.assigned_user_id == user.id }}", "আমার আজকের দায়িত্ব", Modifier.weight(1f))
            StatCard(user.room_no ?: "-", "রুম নম্বর", Modifier.weight(1f))
        }
        Spacer(Modifier.height(14.dp))

        // ---- ইন-অ্যাপ নোটিফিকেশন ব্যানার: বকেয়া ভাড়া ও আজকের বাকি দায়িত্ব ----
        if (!loading) {
            val pendingMine = todayDuties.filter { it.assigned_user_id == user.id && it.status == "pending" }
            if (totalDue > 0) {
                NotifyBanner("💰 আপনার মোট ৳${formatMoney(totalDue)} টাকা ভাড়া বকেয়া আছে — \"ভাড়া\" ট্যাব থেকে বিস্তারিত দেখুন।")
                Spacer(Modifier.height(8.dp))
            }
            if (pendingMine.isNotEmpty()) {
                val names = pendingMine.joinToString("। ") { d -> "আজ ${DutyType.fromApi(d.duty_type).label} আপনার দায়িত্বে" }
                NotifyBanner("📋 $names — সম্পন্ন হলে \"সম্পন্ন করলাম\" চাপুন।")
                Spacer(Modifier.height(8.dp))
            }
            Spacer(Modifier.height(6.dp))
        }

        SectionCard("আজকের সব দায়িত্ব") {
            if (loading) {
                LoadingBox()
            } else if (todayDuties.isEmpty()) {
                EmptyState("আজ কোনো দায়িত্ব সেট করা নেই")
            } else {
                todayDuties.forEach { duty ->
                    val isMe = duty.assigned_user_id == user.id
                    val type = DutyType.fromApi(duty.duty_type)
                    Row(
                        Modifier.fillMaxWidth().padding(vertical = if (isMe) 10.dp else 7.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(Modifier.weight(1f)) {
                            Text(
                                "${type.icon} ${type.label}",
                                fontSize = if (isMe) 15.sp else 12.sp,
                                fontWeight = if (isMe) FontWeight.Bold else FontWeight.Normal,
                                color = if (isMe) Rule else Ink
                            )
                            Text(
                                duty.name ?: "-",
                                fontSize = if (isMe) 16.sp else 12.sp,
                                fontWeight = if (isMe) FontWeight.Bold else FontWeight.Normal,
                                color = if (isMe) Rule else InkSoft
                            )
                        }
                        StatusBadge(duty.status, Modifier.padding(end = 8.dp))
                        if (isMe && duty.status == "pending") {
                            SecondaryButton("✅ সম্পন্ন", onClick = {
                                scope.launch {
                                    val res = AppContainer.dutyRepository.updateStatus(duty.id, "done")
                                    val msg = if (res is ApiResult.Success) "দায়িত্ব সম্পন্ন হিসেবে চিহ্নিত হলো" else (res as ApiResult.Error).message
                                    Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                                    refreshTrigger++
                                }
                            })
                        }
                    }
                    Divider()
                }
            }
        }
    }
}

@Composable
private fun NotifyBanner(text: String) {
    Row(
        Modifier.fillMaxWidth().background(GoldBg, RoundedCornerShape(8.dp)).padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text, fontSize = 12.sp, color = Ink)
    }
}
