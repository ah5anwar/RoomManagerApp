package com.creativesit.roommanager.ui.customer

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.creativesit.roommanager.AppContainer
import com.creativesit.roommanager.data.model.RentPayment
import com.creativesit.roommanager.data.model.User
import com.creativesit.roommanager.data.remote.ApiResult
import com.creativesit.roommanager.ui.common.*
import com.creativesit.roommanager.util.BN_MONTHS
import com.creativesit.roommanager.util.formatMoney
import kotlinx.coroutines.launch

@Composable
fun CustomerRentScreen(user: User) {
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    var loading by remember { mutableStateOf(true) }
    var rows by remember { mutableStateOf<List<RentPayment>>(emptyList()) }
    var sendingWa by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        val res = AppContainer.rentRepository.history()
        if (res is ApiResult.Success) rows = res.data.sortedWith(compareByDescending<RentPayment> { it.year }.thenByDescending { it.month })
        loading = false
    }

    val totalDue = rows.filter { it.status != "paid" }.sumOf { it.amount_due - it.amount_paid }
    val dueMonthsCount = rows.count { it.status != "paid" }

    ScrollScreen {
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            StatCard("৳${formatMoney(totalDue)}", "সর্বমোট বকেয়া", Modifier.weight(1f))
            StatCard("$dueMonthsCount", "কয় মাস বাকি", Modifier.weight(1f))
        }
        Spacer(Modifier.height(14.dp))
        if (totalDue > 0) {
            PrimaryButton(
                text = if (sendingWa) "পাঠানো হচ্ছে..." else "📲 WhatsApp এ বকেয়ার তালিকা পাঠান",
                enabled = !sendingWa,
                modifier = Modifier.fillMaxWidth(),
                onClick = {
                    sendingWa = true
                    scope.launch {
                        val res = AppContainer.notificationRepository.sendDueList(user.id)
                        sendingWa = false
                        val msg = if (res is ApiResult.Success) (res.message ?: "পাঠানো হয়েছে") else (res as ApiResult.Error).message
                        Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
                    }
                }
            )
            Spacer(Modifier.height(14.dp))
        }
        SectionCard("আমার ভাড়ার হিসাব (মাস অনুযায়ী)") {
            if (loading) LoadingBox()
            else if (rows.isEmpty()) EmptyState("এখনো কোনো ভাড়ার হিসাব যোগ হয়নি")
            else {
                Column {
                    rows.forEach { r ->
                        Column(Modifier.fillMaxWidth().padding(vertical = 10.dp)) {
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("${BN_MONTHS[r.month]} ${r.year}", fontWeight = FontWeight.SemiBold)
                                StatusBadge(r.status)
                            }
                            Spacer(Modifier.height(4.dp))
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("নির্ধারিত: ৳${formatMoney(r.amount_due)}", fontSize = 12.sp)
                                Text("জমা: ৳${formatMoney(r.amount_paid)}", fontSize = 12.sp)
                                Text("বাকি: ৳${formatMoney(r.amount_due - r.amount_paid)}", fontSize = 12.sp)
                            }
                            Divider(Modifier.padding(top = 10.dp))
                        }
                    }
                }
            }
        }
    }
}
