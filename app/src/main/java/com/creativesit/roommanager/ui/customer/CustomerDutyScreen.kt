package com.creativesit.roommanager.ui.customer

import android.widget.Toast
import androidx.compose.foundation.layout.*
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
import com.creativesit.roommanager.data.model.MarketSummaryData
import com.creativesit.roommanager.data.model.User
import com.creativesit.roommanager.data.remote.ApiResult
import com.creativesit.roommanager.ui.admin.MonthYearDropdown
import com.creativesit.roommanager.ui.common.*
import com.creativesit.roommanager.ui.theme.*
import com.creativesit.roommanager.util.*
import kotlinx.coroutines.launch

/**
 * গ্রাহক এখন এই ডিউটি টাইপের পুরো মাসের রোস্টার দেখতে পারবে (কার নাম কোন দিনে) —
 * শুধু নিজের অ্যাসাইনমেন্ট না। নিজের নাম এলে সেটা বড়/হাইলাইট করে দেখানো হয়।
 * বাজার (market) এর ক্ষেত্রে অতিরিক্ত: টাকার পরিমাণ (একবারই বসানো যায়, তারপর লক) ও মাসিক সারাংশ।
 */
@Composable
fun CustomerDutyScreen(user: User, dutyTypeId: String) {
    val type = DutyType.fromApi(dutyTypeId)
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val isMarket = dutyTypeId == "market"
    var systemStartYear by remember { mutableStateOf<Int?>(null) }
    var systemStartMonth by remember { mutableStateOf<Int?>(null) }
    val bounds by remember(systemStartYear, systemStartMonth) {
        mutableStateOf(monthYearBounds(maxAheadMonths = 1, systemStartYear = systemStartYear, systemStartMonth = systemStartMonth))
    }

    LaunchedEffect(Unit) {
        val res = AppContainer.settingsRepository.getPublicSettings()
        if (res is ApiResult.Success) {
            systemStartYear = res.data["system_start_year"]?.toIntOrNull()
            systemStartMonth = res.data["system_start_month"]?.toIntOrNull()
        }
    }

    var month by remember(dutyTypeId) { mutableStateOf(currentMonth()) }
    var year by remember(dutyTypeId) { mutableStateOf(currentYear()) }
    var loading by remember { mutableStateOf(true) }
    var assignments by remember { mutableStateOf<List<Duty>>(emptyList()) }
    var marketSummary by remember { mutableStateOf<MarketSummaryData?>(null) }
    var refreshTrigger by remember { mutableStateOf(0) }
    val today = remember { todayIso() }
    val amountInputs = remember { mutableStateMapOf<Int, String>() } // duty.id -> টাইপ করা টাকা

    LaunchedEffect(dutyTypeId, month, year, refreshTrigger) {
        loading = true
        val res = AppContainer.dutyRepository.byTypeMonth(dutyTypeId, month, year)
        if (res is ApiResult.Success) assignments = res.data.assignments
        else Toast.makeText(context, (res as ApiResult.Error).message, Toast.LENGTH_SHORT).show()

        if (isMarket) {
            val sres = AppContainer.dutyRepository.marketSummary(month, year)
            if (sres is ApiResult.Success) marketSummary = sres.data
        }
        loading = false
    }

    fun submitDone(dutyId: Int, amount: Double? = null) {
        scope.launch {
            val res = AppContainer.dutyRepository.updateStatus(dutyId, "done", amount)
            val msg = if (res is ApiResult.Success) "সম্পন্ন হিসেবে চিহ্নিত হলো" else (res as ApiResult.Error).message
            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
            refreshTrigger++
        }
    }

    ScrollScreen {
        // ---- বাজারের মাসিক খরচের সারাংশ (সবাই মিলে কত, কে কত) ----
        if (isMarket && marketSummary != null) {
            SectionCard("💰 ${BN_MONTHS[month]} $year — বাজার খরচের সারাংশ") {
                StatCard("৳${formatMoney(marketSummary!!.total)}", "সবাই মিলে মোট খরচ")
                Spacer(Modifier.height(10.dp))
                if (marketSummary!!.by_user.isEmpty()) {
                    EmptyState("এই মাসে কোনো খরচ যোগ হয়নি")
                } else {
                    marketSummary!!.by_user.forEach { u ->
                        Row(Modifier.fillMaxWidth().padding(vertical = 6.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(u.name, fontSize = 13.sp)
                            Text("৳${formatMoney(u.total)}", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }
            Spacer(Modifier.height(14.dp))
        }

        SectionCard("${type.icon} ${type.label} — মাসিক তালিকা") {
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
                        val (cm, cy) = bounds.clamp(month, newYear)
                        month = cm; year = cy
                    },
                    modifier = Modifier.weight(1f)
                )
            }

            if (loading) LoadingBox()
            else if (assignments.isEmpty()) EmptyState("এই মাসে এই দায়িত্বে কোনো শিডিউল সেট করা হয়নি")
            else Column {
                val days = daysInMonth(year, month)
                for (day in 1..days) {
                    val dateStr = isoDate(year, month, day)
                    val duty = assignments.find { it.duty_date == dateStr }
                    val isMe = duty?.assigned_user_id == user.id
                    Column(Modifier.fillMaxWidth().padding(vertical = if (isMe) 10.dp else 7.dp)) {
                        Row(
                            Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                "$day তারিখ${if (dateStr == today) " (আজ)" else ""}, ${weekdayOf(year, month, day)}",
                                fontSize = if (isMe) 14.sp else 12.sp,
                                fontWeight = if (isMe) FontWeight.Bold else FontWeight.Normal,
                                color = if (isMe) Rule else InkSoft
                            )
                            if (duty != null) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        duty.name ?: "-",
                                        fontSize = if (isMe) 16.sp else 12.sp,
                                        fontWeight = if (isMe) FontWeight.Bold else FontWeight.Normal,
                                        color = if (isMe) Rule else Ink
                                    )
                                    Spacer(Modifier.width(8.dp))
                                    StatusBadge(duty.status)
                                    if (isMarket && duty.amount != null) {
                                        Spacer(Modifier.width(8.dp))
                                        Text("৳${formatMoney(duty.amount)}", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = Sage)
                                    }
                                    if (isMe && duty.status == "pending" && !isMarket) {
                                        Spacer(Modifier.width(8.dp))
                                        SecondaryButton("✅ সম্পন্ন", onClick = { submitDone(duty.id) })
                                    }
                                }
                            } else {
                                Text("-- খালি --", fontSize = 12.sp, color = InkSoft)
                            }
                        }
                        // ---- বাজারের ক্ষেত্রে: নিজের এখনো-সম্পন্ন-না-করা দায়িত্বে টাকা লেখার ইনপুট ----
                        if (isMarket && isMe && duty != null && duty.status == "pending") {
                            Row(Modifier.fillMaxWidth().padding(top = 6.dp), verticalAlignment = Alignment.CenterVertically) {
                                OutlinedTextField(
                                    value = amountInputs[duty.id] ?: "",
                                    onValueChange = { amountInputs[duty.id] = it },
                                    placeholder = { Text("টাকা") },
                                    modifier = Modifier.weight(1f)
                                )
                                Spacer(Modifier.width(8.dp))
                                SecondaryButton("✅ সম্পন্ন", onClick = {
                                    val amt = amountInputs[duty.id]?.toDoubleOrNull()
                                    if (amt == null || amt <= 0) {
                                        Toast.makeText(context, "সঠিক টাকার পরিমাণ লিখুন", Toast.LENGTH_SHORT).show()
                                    } else {
                                        submitDone(duty.id, amt)
                                    }
                                })
                            }
                        } else if (isMarket && isMe && duty != null && duty.status == "done" && duty.amount == null) {
                            // এডমিন/ম্যানেজার আগেই সম্পন্ন মার্ক করেছে কিন্তু টাকা লেখা হয়নি — একবার বসানোর সুযোগ
                            Row(Modifier.fillMaxWidth().padding(top = 6.dp), verticalAlignment = Alignment.CenterVertically) {
                                OutlinedTextField(
                                    value = amountInputs[duty.id] ?: "",
                                    onValueChange = { amountInputs[duty.id] = it },
                                    placeholder = { Text("টাকা লিখুন") },
                                    modifier = Modifier.weight(1f)
                                )
                                Spacer(Modifier.width(8.dp))
                                SecondaryButton("টাকা যোগ করুন", onClick = {
                                    val amt = amountInputs[duty.id]?.toDoubleOrNull()
                                    if (amt == null || amt <= 0) {
                                        Toast.makeText(context, "সঠিক টাকার পরিমাণ লিখুন", Toast.LENGTH_SHORT).show()
                                    } else {
                                        submitDone(duty.id, amt)
                                    }
                                })
                            }
                        }
                    }
                    Divider()
                }
            }
        }
    }
}
