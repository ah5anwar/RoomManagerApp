package com.creativesit.roommanager.ui.admin

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Divider
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.creativesit.roommanager.AppContainer
import com.creativesit.roommanager.data.model.Duty
import com.creativesit.roommanager.data.model.DutyType
import com.creativesit.roommanager.data.model.User
import com.creativesit.roommanager.data.remote.ApiResult
import com.creativesit.roommanager.ui.common.*
import com.creativesit.roommanager.ui.theme.InkSoft
import com.creativesit.roommanager.util.formatMoney
import com.creativesit.roommanager.util.todayIso

@Composable
fun AdminHomeScreen(user: User) {
    var loading by remember { mutableStateOf(true) }
    var totalCustomers by remember { mutableStateOf(0) }
    var totalDue by remember { mutableStateOf(0.0) }
    var todayDuties by remember { mutableStateOf<List<Duty>>(emptyList()) }

    LaunchedEffect(Unit) {
        loading = true
        val usersRes = AppContainer.userRepository.getUsers()
        if (usersRes is ApiResult.Success) totalCustomers = usersRes.data.count { it.role == "customer" }

        val dueRes = AppContainer.rentRepository.dueList()
        if (dueRes is ApiResult.Success) totalDue = dueRes.data.sumOf { it.due_amount }

        val dutyRes = AppContainer.dutyRepository.byDate(todayIso())
        if (dutyRes is ApiResult.Success) todayDuties = dutyRes.data
        loading = false
    }

    ScrollScreen {
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            StatCard("$totalCustomers", "মোট গ্রাহক", Modifier.weight(1f))
            StatCard("${todayDuties.size}", "আজকের ডিউটি", Modifier.weight(1f))
            StatCard("৳${formatMoney(totalDue)}", "সর্বমোট বকেয়া", Modifier.weight(1f))
        }
        Spacer(Modifier.height(14.dp))
        SectionCard("আজকের সব ডিউটি") {
            if (loading) LoadingBox()
            else if (todayDuties.isEmpty()) EmptyState("আজ কোনো ডিউটি সেট করা নেই")
            else Column {
                todayDuties.forEach { d ->
                    val type = DutyType.fromApi(d.duty_type)
                    Row(
                        Modifier.fillMaxWidth().padding(vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("${type.icon} ${type.label}", fontSize = 13.sp)
                            Text(
                                "${d.name ?: ""} (${d.user_code ?: ""}) — রুম ${d.room_no ?: "-"}",
                                fontSize = 11.sp, color = InkSoft
                            )
                        }
                        StatusBadge(d.status)
                    }
                    Divider()
                }
            }
        }
    }
}
