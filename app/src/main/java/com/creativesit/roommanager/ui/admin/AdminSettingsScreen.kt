package com.creativesit.roommanager.ui.admin

import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.creativesit.roommanager.BuildConfig
import com.creativesit.roommanager.AppContainer
import com.creativesit.roommanager.data.remote.ApiResult
import com.creativesit.roommanager.ui.common.*
import com.creativesit.roommanager.ui.theme.Clay
import com.creativesit.roommanager.ui.theme.InkSoft
import com.creativesit.roommanager.ui.theme.Sage
import kotlinx.coroutines.launch

private val TIMEZONES = listOf(
    "Asia/Dhaka" to "Asia/Dhaka (বাংলাদেশ, GMT+6)",
    "Asia/Kolkata" to "Asia/Kolkata (ভারত, GMT+5:30)",
    "Asia/Karachi" to "Asia/Karachi (পাকিস্তান, GMT+5)",
    "Asia/Kathmandu" to "Asia/Kathmandu (নেপাল, GMT+5:45)",
    "Asia/Dubai" to "Asia/Dubai (UAE, GMT+4)",
    "Asia/Riyadh" to "Asia/Riyadh (সৌদি আরব, GMT+3)",
    "Asia/Kuala_Lumpur" to "Asia/Kuala_Lumpur (মালয়েশিয়া, GMT+8)",
    "Asia/Singapore" to "Asia/Singapore (সিঙ্গাপুর, GMT+8)",
    "UTC" to "UTC"
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminSettingsScreen() {
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val clipboard = LocalClipboardManager.current

    var phoneId by remember { mutableStateOf("") }
    var bizId by remember { mutableStateOf("") }
    var accessToken by remember { mutableStateOf("") }
    var time1 by remember { mutableStateOf("08:00") }
    var time2 by remember { mutableStateOf("14:00") }
    var time3 by remember { mutableStateOf("20:00") }
    var timezone by remember { mutableStateOf("Asia/Dhaka") }
    var webhookToken by remember { mutableStateOf("room_manager_verify_2026") }
    var startMonth by remember { mutableStateOf(com.creativesit.roommanager.util.currentMonth()) }
    var startYear by remember { mutableStateOf(com.creativesit.roommanager.util.currentYear().toString()) }
    var loading by remember { mutableStateOf(true) }
    var saving by remember { mutableStateOf(false) }
    var generating by remember { mutableStateOf(false) }
    var clearingNotify by remember { mutableStateOf(false) }
    var clearingCache by remember { mutableStateOf(false) }
    var testPhone by remember { mutableStateOf("") }
    var testingWa by remember { mutableStateOf(false) }
    var testResult by remember { mutableStateOf<String?>(null) }

    val webhookUrl = remember { BuildConfig.BASE_URL.trimEnd('/').removeSuffix("/api") + "/api/webhook.php" }

    LaunchedEffect(Unit) {
        val res = AppContainer.settingsRepository.getSettings()
        if (res is ApiResult.Success) {
            val s = res.data
            phoneId = s["whatsapp_phone_number_id"] ?: ""
            bizId = s["whatsapp_business_account_id"] ?: ""
            time1 = s["notification_time_1"] ?: "08:00"
            time2 = s["notification_time_2"] ?: "14:00"
            time3 = s["notification_time_3"] ?: "20:00"
            timezone = s["timezone"] ?: "Asia/Dhaka"
            webhookToken = s["whatsapp_webhook_verify_token"] ?: "room_manager_verify_2026"
            startMonth = s["system_start_month"]?.toIntOrNull() ?: com.creativesit.roommanager.util.currentMonth()
            startYear = s["system_start_year"] ?: com.creativesit.roommanager.util.currentYear().toString()
        }
        loading = false
    }

    if (loading) { LoadingBox(); return }

    ScrollScreen {
        SectionCard("WhatsApp API সেটিংস (Meta Cloud API)") {
            OutlinedTextField(value = phoneId, onValueChange = { phoneId = it }, label = { Text("Phone Number ID") }, modifier = Modifier.fillMaxWidth())
            Spacer(Modifier.height(10.dp))
            OutlinedTextField(value = bizId, onValueChange = { bizId = it }, label = { Text("Business Account ID") }, modifier = Modifier.fillMaxWidth())
            Spacer(Modifier.height(10.dp))
            OutlinedTextField(
                value = accessToken, onValueChange = { accessToken = it },
                label = { Text("Access Token") },
                placeholder = { Text("নতুন টোকেন বসাতে চাইলে লিখুন") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(12.dp))
            PrimaryButton("সংরক্ষণ করুন", onClick = {
                scope.launch {
                    val body = mutableMapOf<String, Any?>(
                        "whatsapp_phone_number_id" to phoneId,
                        "whatsapp_business_account_id" to bizId
                    )
                    if (accessToken.isNotBlank()) body["whatsapp_access_token"] = accessToken
                    val res = AppContainer.settingsRepository.saveSettings(body)
                    val msg = if (res is ApiResult.Success) "সংরক্ষণ করা হয়েছে" else (res as ApiResult.Error).message
                    Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                }
            })

            Spacer(Modifier.height(18.dp))
            Text("🧪 টেস্ট মেসেজ পাঠান", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
            Text(
                "উপরের সেটিংস সংরক্ষণের পর একটা নম্বর দিয়ে টেস্ট করুন — Meta থেকে আসা আসল রেসপন্স/এরর দেখাবে।",
                fontSize = 11.sp, color = InkSoft, modifier = Modifier.padding(top = 2.dp, bottom = 8.dp)
            )
            Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                OutlinedTextField(
                    value = testPhone, onValueChange = { testPhone = it },
                    placeholder = { Text("01712345678") }, modifier = Modifier.weight(1f)
                )
                Spacer(Modifier.width(8.dp))
                SecondaryButton(if (testingWa) "..." else "টেস্ট পাঠান", enabled = !testingWa, onClick = {
                    if (testPhone.isBlank()) { Toast.makeText(context, "ফোন নম্বর দিন", Toast.LENGTH_SHORT).show(); return@SecondaryButton }
                    testingWa = true
                    scope.launch {
                        val res = AppContainer.settingsRepository.testWhatsApp(testPhone)
                        testingWa = false
                        testResult = when (res) {
                            is ApiResult.Success -> res.message ?: "সম্পন্ন হয়েছে"
                            is ApiResult.Error -> res.message
                        }
                    }
                })
            }
            testResult?.let {
                Spacer(Modifier.height(8.dp))
                Text(it, fontSize = 11.sp, color = if (it.contains("ব্যর্থ")) Clay else Sage)
            }
        }

        Spacer(Modifier.height(14.dp))

        SectionCard("🔗 Webhook Callback URL") {
            Text(
                "Meta App Dashboard → WhatsApp → Configuration → Webhook এ গিয়ে নিচের মান দুটো বসান।",
                fontSize = 11.sp, color = InkSoft
            )
            Spacer(Modifier.height(8.dp))
            CopyableField("Callback URL", webhookUrl, clipboard, context)
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(
                value = webhookToken, onValueChange = { webhookToken = it },
                label = { Text("Verify Token") }, modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(10.dp))
            PrimaryButton("Verify Token সংরক্ষণ করুন", onClick = {
                scope.launch {
                    val res = AppContainer.settingsRepository.saveSettings(mapOf("whatsapp_webhook_verify_token" to webhookToken))
                    val msg = if (res is ApiResult.Success) "সংরক্ষণ করা হয়েছে" else (res as ApiResult.Error).message
                    Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                }
            })
        }

        Spacer(Modifier.height(14.dp))

        SectionCard("নোটিফিকেশনের সময় (দৈনিক ৩ বার)") {
            OutlinedTextField(value = time1, onValueChange = { time1 = it }, label = { Text("১ম বার (HH:mm)") }, modifier = Modifier.fillMaxWidth())
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(value = time2, onValueChange = { time2 = it }, label = { Text("২য় বার (HH:mm)") }, modifier = Modifier.fillMaxWidth())
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(value = time3, onValueChange = { time3 = it }, label = { Text("৩য় বার (HH:mm)") }, modifier = Modifier.fillMaxWidth())
            Spacer(Modifier.height(12.dp))
            PrimaryButton("সময় সংরক্ষণ করুন", onClick = {
                scope.launch {
                    val res = AppContainer.settingsRepository.saveSettings(
                        mapOf("notification_time_1" to time1, "notification_time_2" to time2, "notification_time_3" to time3)
                    )
                    val msg = if (res is ApiResult.Success) "সংরক্ষণ করা হয়েছে" else (res as ApiResult.Error).message
                    Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                }
            })
        }

        Spacer(Modifier.height(14.dp))

        SectionCard("🕒 সময় ও টাইমজোন") {
            Text(
                "এখানে যে টাইমজোন সেট করবেন, পুরো সিস্টেম সেই টাইমজোন অনুযায়ী চলবে।",
                fontSize = 11.sp, color = InkSoft, modifier = Modifier.padding(bottom = 8.dp)
            )
            val tzLabel = TIMEZONES.find { it.first == timezone }?.second ?: timezone
            SimpleDropdown(
                label = "টাইমজোন",
                selectedLabel = tzLabel,
                options = TIMEZONES.map { it.second },
                onSelect = { idx -> timezone = TIMEZONES[idx].first },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(10.dp))
            PrimaryButton("টাইমজোন সংরক্ষণ করুন", onClick = {
                scope.launch {
                    saving = true
                    val res = AppContainer.settingsRepository.saveSettings(mapOf("timezone" to timezone))
                    saving = false
                    val msg = if (res is ApiResult.Success) "সংরক্ষণ করা হয়েছে" else (res as ApiResult.Error).message
                    Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                }
            }, enabled = !saving)
        }

        Spacer(Modifier.height(14.dp))

        SectionCard("📅 সিস্টেম শুরুর তারিখ") {
            Text(
                "এখানে যে মাস/বছর সেট করবেন, সেটাই হবে সবচেয়ে পুরনো মাস — ভাড়া ও ডিউটির মাস/সাল তালিকায় এর আগের কোনো মাস দেখানো হবে না।",
                fontSize = 11.sp, color = InkSoft, modifier = Modifier.padding(bottom = 8.dp)
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                SimpleDropdown(
                    label = "শুরুর মাস",
                    selectedLabel = com.creativesit.roommanager.util.BN_MONTHS[startMonth],
                    options = (1..12).map { com.creativesit.roommanager.util.BN_MONTHS[it] },
                    onSelect = { idx -> startMonth = idx + 1 },
                    modifier = Modifier.weight(1f)
                )
                OutlinedTextField(
                    value = startYear, onValueChange = { startYear = it },
                    label = { Text("শুরুর বছর") }, modifier = Modifier.weight(1f)
                )
            }
            Spacer(Modifier.height(10.dp))
            PrimaryButton("সংরক্ষণ করুন", onClick = {
                val yearNum = startYear.toIntOrNull()
                if (yearNum == null || yearNum < 2000) {
                    Toast.makeText(context, "সঠিক বছর দিন", Toast.LENGTH_SHORT).show()
                    return@PrimaryButton
                }
                scope.launch {
                    val res = AppContainer.settingsRepository.saveSettings(
                        mapOf("system_start_month" to startMonth, "system_start_year" to yearNum)
                    )
                    val msg = if (res is ApiResult.Success) "সংরক্ষণ করা হয়েছে" else (res as ApiResult.Error).message
                    Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                }
            })
        }

        Spacer(Modifier.height(14.dp))

        SectionCard("🛠️ মেইনটেন্যান্স (শুধু এডমিন)") {
            Text("এই মাসের ভাড়ার বকেয়া এখনই সবার জন্য তৈরি করুন", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
            Text(
                "এটা প্রতি মাসের ১ তারিখে স্বয়ংক্রিয়ভাবে হয় (cron সেটআপ থাকলে), তবে এখনই ম্যানুয়ালি চালাতে চাইলে চাপুন। আগে থেকে থাকা এন্ট্রি নষ্ট হবে না।",
                fontSize = 11.sp, color = InkSoft, modifier = Modifier.padding(top = 2.dp, bottom = 8.dp)
            )
            SecondaryButton(
                text = if (generating) "তৈরি হচ্ছে..." else "এই মাসের বকেয়া তৈরি করুন",
                onClick = {
                    generating = true
                    scope.launch {
                        val res = AppContainer.rentRepository.generateMonthly(
                            com.creativesit.roommanager.util.currentMonth(),
                            com.creativesit.roommanager.util.currentYear()
                        )
                        generating = false
                        val msg = if (res is ApiResult.Success) (res.message ?: "সম্পন্ন হয়েছে") else (res as ApiResult.Error).message
                        Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
                    }
                },
                enabled = !generating
            )

            Spacer(Modifier.height(16.dp))
            Text("নোটিফিকেশন হিস্ট্রি", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
            Text(
                "সব নোটিফিকেশন লগ স্থায়ীভাবে মুছে যাবে।",
                fontSize = 11.sp, color = InkSoft, modifier = Modifier.padding(top = 2.dp, bottom = 8.dp)
            )
            SecondaryButton(
                text = if (clearingNotify) "মুছে ফেলা হচ্ছে..." else "🗑️ নোটিফিকেশন হিস্ট্রি ক্লিয়ার করুন",
                onClick = {
                    clearingNotify = true
                    scope.launch {
                        val res = AppContainer.notificationRepository.clearHistory()
                        clearingNotify = false
                        val msg = if (res is ApiResult.Success) (res.message ?: "মুছে ফেলা হয়েছে") else (res as ApiResult.Error).message
                        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                    }
                },
                enabled = !clearingNotify
            )

            Spacer(Modifier.height(16.dp))
            Text("সার্ভার ক্যাশ", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
            Text(
                "কোড আপডেট করার পরও পুরনো আচরণ দেখালে (PHP OPcache এর কারণে হতে পারে) এটা চাপুন।",
                fontSize = 11.sp, color = InkSoft, modifier = Modifier.padding(top = 2.dp, bottom = 8.dp)
            )
            SecondaryButton(
                text = if (clearingCache) "ক্লিয়ার হচ্ছে..." else "🧹 সার্ভার ক্যাশ ক্লিয়ার করুন",
                onClick = {
                    clearingCache = true
                    scope.launch {
                        val res = AppContainer.settingsRepository.clearCache()
                        clearingCache = false
                        val msg = if (res is ApiResult.Success) (res.message ?: "ক্লিয়ার হয়েছে") else (res as ApiResult.Error).message
                        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                    }
                },
                enabled = !clearingCache
            )
        }
    }
}

@Composable
private fun CopyableField(
    label: String, value: String,
    clipboard: androidx.compose.ui.platform.ClipboardManager,
    context: android.content.Context
) {
    Column {
        Text(label, fontSize = 11.sp, color = InkSoft)
        Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
            OutlinedTextField(
                value = value, onValueChange = {}, readOnly = true,
                modifier = Modifier.weight(1f)
            )
            Spacer(Modifier.width(6.dp))
            SecondaryButton("কপি", onClick = {
                clipboard.setText(AnnotatedString(value))
                Toast.makeText(context, "কপি হয়েছে", Toast.LENGTH_SHORT).show()
            })
        }
    }
}
