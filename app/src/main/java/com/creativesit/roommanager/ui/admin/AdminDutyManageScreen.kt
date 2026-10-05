package com.creativesit.roommanager.ui.admin

import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
import com.creativesit.roommanager.data.model.*
import com.creativesit.roommanager.data.remote.ApiResult
import com.creativesit.roommanager.ui.common.*
import com.creativesit.roommanager.ui.theme.*
import com.creativesit.roommanager.util.*
import kotlinx.coroutines.launch

@Composable
fun AdminDutyManageScreen(dutyTypeId: String) {
    val type = DutyType.fromApi(dutyTypeId)
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val isMarket = dutyTypeId == "market"
    var systemStartYear by remember { mutableStateOf<Int?>(null) }
    var systemStartMonth by remember { mutableStateOf<Int?>(null) }
    // ডিউটি সর্বোচ্চ ১ মাস আগামি পর্যন্ত সেট করা যাবে (৭নং শর্ত)
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
    var eligibleUsers by remember { mutableStateOf<List<EligibleUser>>(emptyList()) }
    var assignments by remember { mutableStateOf<Map<Int, Int?>>(emptyMap()) }
    // দিন -> বিদ্যমান Duty (আইডি+স্ট্যাটাসসহ), না থাকলে null
    var existingDuties by remember { mutableStateOf<Map<Int, Duty>>(emptyMap()) }
    var marketSummary by remember { mutableStateOf<MarketSummaryData?>(null) }
    var pickerForDay by remember { mutableStateOf<Int?>(null) }
    var statusPickerForDay by remember { mutableStateOf<Int?>(null) }
    var amountPickerForDay by remember { mutableStateOf<Int?>(null) }
    var amountInput by remember { mutableStateOf("") }
    var saving by remember { mutableStateOf(false) }
    var sendingSummary by remember { mutableStateOf(false) }
    var refreshTrigger by remember { mutableStateOf(0) }

    LaunchedEffect(dutyTypeId, month, year, refreshTrigger) {
        loading = true
        val res = AppContainer.dutyRepository.byTypeMonth(dutyTypeId, month, year)
        if (res is ApiResult.Success) {
            eligibleUsers = res.data.eligible_users
            val days = daysInMonth(year, month)
            val map = mutableMapOf<Int, Int?>()
            val dutyMap = mutableMapOf<Int, Duty>()
            for (d in 1..days) {
                val dateStr = isoDate(year, month, d)
                val existing = res.data.assignments.find { it.duty_date == dateStr }
                map[d] = existing?.assigned_user_id
                if (existing != null) dutyMap[d] = existing
            }
            assignments = map
            existingDuties = dutyMap
        } else {
            Toast.makeText(context, (res as ApiResult.Error).message, Toast.LENGTH_SHORT).show()
        }
        if (isMarket) {
            val sres = AppContainer.dutyRepository.marketSummary(month, year)
            if (sres is ApiResult.Success) marketSummary = sres.data
        }
        loading = false
    }

    ScrollScreen {
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
                Spacer(Modifier.height(10.dp))
                SecondaryButton(
                    text = if (sendingSummary) "পাঠানো হচ্ছে..." else "📲 WhatsApp এ পাঠান",
                    enabled = !sendingSummary,
                    onClick = {
                        sendingSummary = true
                        scope.launch {
                            val res = AppContainer.notificationRepository.sendMarketSummary(month, year)
                            sendingSummary = false
                            val msg = if (res is ApiResult.Success) (res.message ?: "পাঠানো হয়েছে") else (res as ApiResult.Error).message
                            Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
                        }
                    }
                )
            }
            Spacer(Modifier.height(14.dp))
        }

        SectionCard("${type.icon} ${type.label} — মাসিক শিডিউল") {
            Row(Modifier.fillMaxWidth().padding(bottom = 6.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
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
            Text(
                "কাকে অ্যাসাইন করা হবে তা বদলাতে দিনের উপর ট্যাপ করুন। স্ট্যাটাস (সম্পন্ন/মিস) বদলাতে ব্যাজের উপর ট্যাপ করুন।",
                fontSize = 11.sp, color = InkSoft, modifier = Modifier.padding(bottom = 10.dp)
            )

            if (loading) {
                LoadingBox()
            } else if (eligibleUsers.isEmpty()) {
                EmptyState("এই দায়িত্বের অনুমতি পাওয়া কোনো গ্রাহক নেই। \"ইউজার\" ট্যাব থেকে পারমিশন দিন।")
            } else {
                Column {
                    for (day in 1..daysInMonth(year, month)) {
                        val userId = assignments[day]
                        val user = eligibleUsers.find { it.id == userId }
                        val duty = existingDuties[day]
                        Row(
                            Modifier.fillMaxWidth().padding(vertical = 10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(
                                Modifier.weight(1f).clickable { pickerForDay = day }
                            ) {
                                Text("$day তারিখ, ${weekdayOf(year, month, day)}", fontSize = 12.sp, fontWeight = FontWeight.Medium)
                                Text(
                                    user?.let { "${it.name} (${it.user_code})" } ?: "-- খালি --",
                                    fontSize = 12.sp, color = if (user != null) Ink else InkSoft
                                )
                            }
                            if (duty != null) {
                                Box(Modifier.clickable { statusPickerForDay = day }) {
                                    StatusBadge(duty.status)
                                }
                                if (isMarket) {
                                    Spacer(Modifier.width(8.dp))
                                    Text(
                                        duty.amount?.let { "৳${formatMoney(it)}" } ?: "টাকা যোগ করুন",
                                        fontSize = 12.sp,
                                        color = if (duty.amount != null) Sage else Rule,
                                        modifier = Modifier.clickable {
                                            amountInput = duty.amount?.let {
                                                if (it == it.toLong().toDouble()) it.toLong().toString() else it.toString()
                                            } ?: ""
                                            amountPickerForDay = day
                                        }
                                    )
                                }
                            }
                        }
                        Divider()
                    }
                }

                Spacer(Modifier.height(14.dp))
                PrimaryButton(
                    text = if (saving) "সংরক্ষণ হচ্ছে..." else "মাসিক শিডিউল সংরক্ষণ করুন",
                    enabled = !saving,
                    modifier = Modifier.fillMaxWidth(),
                    onClick = {
                        saving = true
                        scope.launch {
                            val body = assignments.mapKeys { it.key.toString() }
                            val res = AppContainer.dutyRepository.assignMonth(
                                AssignMonthRequest(dutyTypeId, month, year, body)
                            )
                            saving = false
                            when (res) {
                                is ApiResult.Success -> { Toast.makeText(context, "শিডিউল সংরক্ষণ হয়েছে", Toast.LENGTH_SHORT).show(); refreshTrigger++ }
                                is ApiResult.Error -> Toast.makeText(context, res.message, Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                )
            }
        }
    }

    // ---- কে দায়িত্বে থাকবে বাছাই করার ডায়ালগ ----
    val dayForPicker = pickerForDay
    if (dayForPicker != null) {
        Dialog(onDismissRequest = { pickerForDay = null }) {
            Card(colors = CardDefaults.cardColors(containerColor = CardBg)) {
                Column(
                    Modifier.padding(16.dp).fillMaxWidth().heightIn(max = 420.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    Text("$dayForPicker তারিখে কে দায়িত্ব পালন করবে?", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    Spacer(Modifier.height(10.dp))
                    Row(
                        Modifier.fillMaxWidth()
                            .clickable {
                                assignments = assignments.toMutableMap().apply { put(dayForPicker, null) }
                                pickerForDay = null
                            }
                            .padding(vertical = 10.dp)
                    ) { Text("-- খালি --", color = InkSoft) }
                    Divider()
                    eligibleUsers.forEach { u ->
                        Row(
                            Modifier.fillMaxWidth()
                                .clickable {
                                    assignments = assignments.toMutableMap().apply { put(dayForPicker, u.id) }
                                    pickerForDay = null
                                }
                                .padding(vertical = 10.dp)
                        ) { Text("${u.name} (${u.user_code})") }
                        Divider()
                    }
                }
            }
        }
    }

    // ---- ডিউটির স্ট্যাটাস (পেন্ডিং/সম্পন্ন/মিস) সরাসরি বদলানোর ডায়ালগ (এডমিন/ম্যানেজার) ----
    val dayForStatus = statusPickerForDay
    val dutyForStatus = dayForStatus?.let { existingDuties[it] }
    if (dayForStatus != null && dutyForStatus != null) {
        Dialog(onDismissRequest = { statusPickerForDay = null }) {
            Card(colors = CardDefaults.cardColors(containerColor = CardBg)) {
                Column(Modifier.padding(16.dp).fillMaxWidth()) {
                    Text("$dayForStatus তারিখের স্ট্যাটাস", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    Spacer(Modifier.height(10.dp))
                    listOf("pending" to "বাকি আছে", "done" to "সম্পন্ন", "missed" to "মিস হয়েছে").forEach { (value, label) ->
                        Row(
                            Modifier.fillMaxWidth()
                                .clickable {
                                    val dutyId = dutyForStatus.id
                                    statusPickerForDay = null
                                    scope.launch {
                                        val res = AppContainer.dutyRepository.updateStatus(dutyId, value)
                                        val msg = if (res is ApiResult.Success) "স্ট্যাটাস আপডেট হয়েছে" else (res as ApiResult.Error).message
                                        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                                        refreshTrigger++
                                    }
                                }
                                .padding(vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            StatusBadge(value, customLabel = label)
                        }
                        Divider()
                    }
                }
            }
        }
    }

    // ---- বাজারের টাকার পরিমাণ এডিট করার ডায়ালগ (এডমিন/ম্যানেজার সবসময় বদলাতে পারবেন) ----
    val dayForAmount = amountPickerForDay
    val dutyForAmount = dayForAmount?.let { existingDuties[it] }
    if (dayForAmount != null && dutyForAmount != null) {
        Dialog(onDismissRequest = { amountPickerForDay = null }) {
            Card(colors = CardDefaults.cardColors(containerColor = CardBg)) {
                Column(Modifier.padding(16.dp).fillMaxWidth()) {
                    Text("$dayForAmount তারিখের বাজার খরচ", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    Spacer(Modifier.height(10.dp))
                    OutlinedTextField(
                        value = amountInput, onValueChange = { amountInput = it },
                        placeholder = { Text("টাকার পরিমাণ") }, modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(Modifier.height(12.dp))
                    PrimaryButton("সংরক্ষণ করুন", modifier = Modifier.fillMaxWidth(), onClick = {
                        val amt = amountInput.toDoubleOrNull()
                        if (amt == null || amt < 0) {
                            Toast.makeText(context, "সঠিক টাকার পরিমাণ লিখুন", Toast.LENGTH_SHORT).show()
                            return@PrimaryButton
                        }
                        val dutyId = dutyForAmount.id
                        amountPickerForDay = null
                        scope.launch {
                            val res = AppContainer.dutyRepository.updateStatus(dutyId, dutyForAmount.status, amt)
                            val msg = if (res is ApiResult.Success) "সংরক্ষণ হয়েছে" else (res as ApiResult.Error).message
                            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                            refreshTrigger++
                        }
                    })
                }
            }
        }
    }
}
