package com.creativesit.roommanager.ui.admin

import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.creativesit.roommanager.AppContainer
import com.creativesit.roommanager.data.model.RentDueRow
import com.creativesit.roommanager.data.model.RentSummaryRow
import com.creativesit.roommanager.data.model.UpdatePaymentRequest
import com.creativesit.roommanager.data.remote.ApiResult
import com.creativesit.roommanager.ui.common.*
import com.creativesit.roommanager.ui.theme.*
import com.creativesit.roommanager.util.BN_MONTHS
import com.creativesit.roommanager.util.currentMonth
import com.creativesit.roommanager.util.currentYear
import com.creativesit.roommanager.util.formatMoney
import com.creativesit.roommanager.util.monthYearBounds
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminRentScreen() {
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    var systemStartYear by remember { mutableStateOf<Int?>(null) }
    var systemStartMonth by remember { mutableStateOf<Int?>(null) }
    val bounds by remember(systemStartYear, systemStartMonth) {
        mutableStateOf(monthYearBounds(maxAheadMonths = 0, systemStartYear = systemStartYear, systemStartMonth = systemStartMonth))
    } // ভাড়া শুধু বর্তমান মাস পর্যন্ত, ভবিষ্যতে না
    var month by remember { mutableStateOf(bounds.maxMonth) }
    var year by remember { mutableStateOf(bounds.maxYear) }
    var loading by remember { mutableStateOf(true) }
    var summary by remember { mutableStateOf<List<RentSummaryRow>>(emptyList()) }
    var dueList by remember { mutableStateOf<List<RentDueRow>>(emptyList()) }
    var refreshTrigger by remember { mutableStateOf(0) }
    var paymentDialogFor by remember { mutableStateOf<RentSummaryRow?>(null) }

    LaunchedEffect(Unit) {
        val res = AppContainer.settingsRepository.getPublicSettings()
        if (res is ApiResult.Success) {
            systemStartYear = res.data["system_start_year"]?.toIntOrNull()
            systemStartMonth = res.data["system_start_month"]?.toIntOrNull()
        }
    }

    LaunchedEffect(month, year, refreshTrigger) {
        loading = true
        val res = AppContainer.rentRepository.monthlySummary(month, year)
        if (res is ApiResult.Success) summary = res.data
        val dueRes = AppContainer.rentRepository.dueList()
        if (dueRes is ApiResult.Success) dueList = dueRes.data
        loading = false
    }

    ScrollScreen {
        SectionCard("মাসিক ভাড়ার হিসাব") {
            Row(Modifier.fillMaxWidth().padding(bottom = 10.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                MonthYearDropdown(
                    label = BN_MONTHS[month],
                    options = bounds.monthOptionsFor(year).map { BN_MONTHS[it] },
                    onSelect = { idx -> month = bounds.monthOptionsFor(year)[idx] },
                    modifier = Modifier.weight(1f)
                )
                MonthYearDropdown(
                    label = year.toString(),
                    options = bounds.yearOptions().map { it.toString() },
                    onSelect = { idx ->
                        val newYear = bounds.yearOptions()[idx]
                        val (clampedMonth, clampedYear) = bounds.clamp(month, newYear)
                        month = clampedMonth; year = clampedYear
                    },
                    modifier = Modifier.weight(1f)
                )
            }
            if (loading) LoadingBox()
            else if (summary.isEmpty()) EmptyState("কোনো গ্রাহক নেই")
            else Column {
                summary.forEach { r ->
                    val due = r.amount_due ?: r.monthly_rent ?: 0.0
                    val paid = r.amount_paid ?: 0.0
                    Row(
                        Modifier.fillMaxWidth().padding(vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(Modifier.weight(1f)) {
                            Text("${r.name} (${r.user_code})", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                            Text("নির্ধারিত ৳${formatMoney(due)} • জমা ৳${formatMoney(paid)} • বাকি ৳${formatMoney(due - paid)}", fontSize = 11.sp, color = InkSoft)
                        }
                        StatusBadge(r.status ?: "due", Modifier.padding(end = 8.dp))
                        SecondaryButton("আপডেট", onClick = { paymentDialogFor = r })
                    }
                    Divider()
                }
            }
        }

        Spacer(Modifier.height(14.dp))

        SectionCard("সর্বমোট বকেয়ার তালিকা") {
            if (dueList.isEmpty()) EmptyState("কোনো বকেয়া নেই 🎉")
            else Column {
                dueList.forEach { d ->
                    Row(
                        Modifier.fillMaxWidth().padding(vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(Modifier.weight(1f)) {
                            Text("${d.name} (${d.user_code})", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                            Text("${BN_MONTHS[d.month]} ${d.year} — ৳${formatMoney(d.due_amount)}", fontSize = 11.sp, color = InkSoft)
                        }
                        SecondaryButton("তালিকা পাঠান", onClick = {
                            scope.launch {
                                val res = AppContainer.notificationRepository.sendDueList(d.user_id)
                                val msg = if (res is ApiResult.Success) "বকেয়ার তালিকা পাঠানো হয়েছে" else (res as ApiResult.Error).message
                                Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                            }
                        })
                    }
                    Divider()
                }
            }
        }
    }

    val target = paymentDialogFor
    if (target != null) {
        val defaultDue = target.amount_due ?: target.monthly_rent ?: 0.0
        var amountPaidText by remember(target) { mutableStateOf(defaultDue.let { if (it == it.toLong().toDouble()) it.toLong().toString() else it.toString() }) }
        var saving by remember(target) { mutableStateOf(false) }

        Dialog(onDismissRequest = { if (!saving) paymentDialogFor = null }) {
            Card(colors = CardDefaults.cardColors(containerColor = CardBg)) {
                Column(Modifier.padding(20.dp).fillMaxWidth()) {
                    Text("${target.name} — ভাড়া আপডেট", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Spacer(Modifier.height(4.dp))
                    Text("${BN_MONTHS[month]} $year মাসের জন্য কত টাকা জমা হয়েছে?", fontSize = 12.sp, color = InkSoft)
                    Spacer(Modifier.height(12.dp))
                    OutlinedTextField(
                        value = amountPaidText, onValueChange = { amountPaidText = it },
                        label = { Text("জমাকৃত টাকা") }, modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(Modifier.height(16.dp))
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                        SecondaryButton("বাতিল", onClick = { paymentDialogFor = null }, enabled = !saving)
                        Spacer(Modifier.width(8.dp))
                        PrimaryButton(if (saving) "..." else "সংরক্ষণ করুন", enabled = !saving, onClick = {
                            val amountPaid = amountPaidText.toDoubleOrNull()
                            if (amountPaid == null) {
                                Toast.makeText(context, "সঠিক সংখ্যা দিন", Toast.LENGTH_SHORT).show()
                                return@PrimaryButton
                            }
                            saving = true
                            scope.launch {
                                val res = AppContainer.rentRepository.updatePayment(
                                    UpdatePaymentRequest(target.user_id, month, year, amountPaid, defaultDue)
                                )
                                saving = false
                                when (res) {
                                    is ApiResult.Success -> {
                                        val waSent = res.data.whatsapp_sent
                                        val msg = res.message ?: if (waSent) "ভাড়ার হিসাব আপডেট হয়েছে ও গ্রাহককে WhatsApp এ জানানো হয়েছে"
                                                                  else "ভাড়ার হিসাব আপডেট হয়েছে, কিন্তু WhatsApp মেসেজ পাঠানো যায়নি"
                                        Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
                                        paymentDialogFor = null
                                        refreshTrigger++
                                    }
                                    is ApiResult.Error -> Toast.makeText(context, res.message, Toast.LENGTH_SHORT).show()
                                }
                            }
                        })
                    }
                }
            }
        }
    }
}

@Composable
fun MonthYearDropdown(label: String, options: List<String>, onSelect: (Int) -> Unit, modifier: Modifier = Modifier) {
    SimpleDropdown(label = "", selectedLabel = label, options = options, onSelect = onSelect, modifier = modifier)
}
