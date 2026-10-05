package com.creativesit.roommanager.ui.admin

import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
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
import com.creativesit.roommanager.data.model.CreateUserRequest
import com.creativesit.roommanager.data.model.User
import com.creativesit.roommanager.data.remote.ApiResult
import com.creativesit.roommanager.ui.common.*
import com.creativesit.roommanager.ui.theme.*
import com.creativesit.roommanager.util.formatMoney
import kotlinx.coroutines.launch

private val ALL_PERMS = listOf("market" to "🛒 বাজার", "cooking" to "🍳 রান্না", "trash" to "🗑️ ময়লা ফেলা", "cleaning" to "🧹 রুম ক্লিন", "chat" to "💬 চ্যাট")

@Composable
fun AdminUsersScreen(currentUser: User) {
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    var loading by remember { mutableStateOf(true) }
    var users by remember { mutableStateOf<List<User>>(emptyList()) }
    var showDialog by remember { mutableStateOf(false) }
    var editingUser by remember { mutableStateOf<User?>(null) }
    var refreshTrigger by remember { mutableStateOf(0) }
    val isManager = currentUser.role == "manager"

    LaunchedEffect(refreshTrigger) {
        loading = true
        val res = AppContainer.userRepository.getUsers()
        if (res is ApiResult.Success) users = res.data.filter { it.role != "admin" }
        else Toast.makeText(context, (res as ApiResult.Error).message, Toast.LENGTH_SHORT).show()
        loading = false
    }

    ScrollScreen {
        SectionCard("ইউজার তালিকা (গ্রাহক ও ম্যানেজার)") {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                PrimaryButton("+ নতুন ইউজার", onClick = { editingUser = null; showDialog = true })
            }
            Spacer(Modifier.height(10.dp))
            if (loading) LoadingBox()
            else if (users.isEmpty()) EmptyState("কোনো ইউজার নেই")
            else Column {
                users.forEach { u ->
                    // ম্যানেজার শুধু গ্রাহকদের ব্যবস্থাপনা করতে পারবে — অন্য ম্যানেজারের গায়ে হাত দিতে পারবে না
                    val canManage = !isManager || u.role == "customer"
                    Row(
                        Modifier.fillMaxWidth().padding(vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(u.name, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                                Spacer(Modifier.width(6.dp))
                                RoleBadge(u.role)
                            }
                            Text(
                                "${u.user_code} • ${u.room_no ?: "-"} • ${u.phone ?: ""}" +
                                    if (u.role == "customer") " • ৳${formatMoney(u.monthly_rent)}" else "",
                                fontSize = 11.sp, color = InkSoft
                            )
                        }
                        if (canManage) {
                            SecondaryButton("সম্পাদনা", onClick = { editingUser = u; showDialog = true })
                        } else {
                            Text("অনুমতি নেই", fontSize = 11.sp, color = InkSoft)
                        }
                    }
                    Divider()
                }
            }
        }
    }

    if (showDialog) {
        UserFormDialog(
            existing = editingUser,
            hideManagerOption = isManager,
            onDismiss = { showDialog = false },
            onSaved = { showDialog = false; refreshTrigger++ }
        )
    }
}

@Composable
private fun UserFormDialog(existing: User?, hideManagerOption: Boolean = false, onDismiss: () -> Unit, onSaved: () -> Unit) {
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    var role by remember { mutableStateOf(existing?.role ?: "customer") }
    var name by remember { mutableStateOf(existing?.name ?: "") }
    var phone by remember { mutableStateOf(existing?.phone ?: "") }
    var whatsapp by remember { mutableStateOf(existing?.whatsapp_number ?: "") }
    var room by remember { mutableStateOf(existing?.room_no ?: "") }
    var rent by remember { mutableStateOf((existing?.monthly_rent ?: 6000.0).let { if (it == it.toLong().toDouble()) it.toLong().toString() else it.toString() }) }
    var status by remember { mutableStateOf(existing?.status ?: "active") }
    var password by remember { mutableStateOf("") }
    var selectedPerms by remember {
        mutableStateOf(existing?.permissions?.toMutableSet() ?: ALL_PERMS.map { it.first }.toMutableSet())
    }
    var saving by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = { if (!saving) onDismiss() }) {
        Card(colors = CardDefaults.cardColors(containerColor = CardBg)) {
            Column(
                Modifier.padding(20.dp).fillMaxWidth().heightIn(max = 560.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Text(if (existing == null) "নতুন ইউজার" else "ইউজার সম্পাদনা", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(14.dp))

                val roleOptions = if (hideManagerOption) listOf("গ্রাহক (Customer)") else listOf("গ্রাহক (Customer)", "ম্যানেজার (Manager)")
                SimpleDropdown(
                    label = "ভূমিকা",
                    selectedLabel = if (role == "manager") "ম্যানেজার (Manager)" else "গ্রাহক (Customer)",
                    options = roleOptions,
                    onSelect = { idx -> role = if (roleOptions[idx].startsWith("ম্যানেজার")) "manager" else "customer" },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(10.dp))
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("নাম") }, modifier = Modifier.fillMaxWidth())
                Spacer(Modifier.height(10.dp))
                OutlinedTextField(value = phone, onValueChange = { phone = it }, label = { Text("ফোন") }, modifier = Modifier.fillMaxWidth())
                Spacer(Modifier.height(10.dp))
                OutlinedTextField(value = whatsapp, onValueChange = { whatsapp = it }, label = { Text("WhatsApp নম্বর") }, modifier = Modifier.fillMaxWidth())

                if (role == "customer") {
                    Spacer(Modifier.height(10.dp))
                    OutlinedTextField(value = room, onValueChange = { room = it }, label = { Text("রুম নম্বর") }, modifier = Modifier.fillMaxWidth())
                    Spacer(Modifier.height(10.dp))
                    OutlinedTextField(
                        value = rent, onValueChange = { rent = it }, label = { Text("মাসিক ভাড়া") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                Spacer(Modifier.height(10.dp))
                OutlinedTextField(
                    value = password, onValueChange = { password = it },
                    label = { Text(if (existing == null) "পাসওয়ার্ড" else "নতুন পাসওয়ার্ড (খালি রাখলে অপরিবর্তিত)") },
                    modifier = Modifier.fillMaxWidth()
                )

                if (role == "customer") {
                    Spacer(Modifier.height(14.dp))
                    Text("কোন অপশনগুলো এই গ্রাহক দেখতে পারবে", fontSize = 12.sp, color = InkSoft)
                    Spacer(Modifier.height(6.dp))
                    ALL_PERMS.forEach { (key, label) ->
                        Row(
                            Modifier.fillMaxWidth().clickable {
                                selectedPerms = selectedPerms.toMutableSet().apply {
                                    if (contains(key)) remove(key) else add(key)
                                }
                            },
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(
                                checked = selectedPerms.contains(key),
                                onCheckedChange = {
                                    selectedPerms = selectedPerms.toMutableSet().apply {
                                        if (it) add(key) else remove(key)
                                    }
                                }
                            )
                            Text(label, fontSize = 13.sp)
                        }
                    }
                }

                Spacer(Modifier.height(10.dp))
                SimpleDropdown(
                    label = "স্ট্যাটাস",
                    selectedLabel = if (status == "active") "সক্রিয়" else "নিষ্ক্রিয়",
                    options = listOf("সক্রিয়", "নিষ্ক্রিয়"),
                    onSelect = { idx -> status = if (idx == 1) "inactive" else "active" },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(Modifier.height(18.dp))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    SecondaryButton("বাতিল", onClick = onDismiss, enabled = !saving)
                    Spacer(Modifier.width(8.dp))
                    PrimaryButton(if (saving) "..." else "সংরক্ষণ করুন", enabled = !saving, onClick = {
                        if (name.isBlank()) {
                            Toast.makeText(context, "নাম দিন", Toast.LENGTH_SHORT).show()
                            return@PrimaryButton
                        }
                        if (existing == null && password.isBlank()) {
                            Toast.makeText(context, "নতুন ইউজারের জন্য পাসওয়ার্ড দিন", Toast.LENGTH_SHORT).show()
                            return@PrimaryButton
                        }
                        saving = true
                        scope.launch {
                            val rentValue = rent.toDoubleOrNull() ?: 0.0
                            val result = if (existing == null) {
                                AppContainer.userRepository.createUser(
                                    CreateUserRequest(
                                        role = role, name = name, phone = phone, whatsapp_number = whatsapp,
                                        room_no = if (role == "manager") null else room,
                                        monthly_rent = if (role == "manager") null else rentValue,
                                        status = status,
                                        permissions = if (role == "manager") emptyList() else selectedPerms.toList(),
                                        password = password
                                    )
                                )
                            } else {
                                val fields = mutableMapOf<String, Any?>(
                                    "role" to role, "name" to name, "phone" to phone,
                                    "whatsapp_number" to whatsapp, "status" to status,
                                    "room_no" to if (role == "manager") null else room,
                                    "monthly_rent" to if (role == "manager") 0.0 else rentValue,
                                    "permissions" to if (role == "manager") emptyList<String>() else selectedPerms.toList()
                                )
                                if (password.isNotBlank()) fields["password"] = password
                                AppContainer.userRepository.updateUser(existing.id, fields)
                            }
                            saving = false
                            when (result) {
                                is ApiResult.Success -> { Toast.makeText(context, "সংরক্ষিত হয়েছে", Toast.LENGTH_SHORT).show(); onSaved() }
                                is ApiResult.Error -> Toast.makeText(context, result.message, Toast.LENGTH_SHORT).show()
                            }
                        }
                    })
                }
            }
        }
    }
}

// (নিচের কাস্টম wrapper ফাংশনগুলো বাদ দেওয়া হয়েছে — উপরে সরাসরি androidx.compose.foundation এর
// verticalScroll ও clickable এক্সটেনশন ফাংশন ইম্পোর্ট করে ব্যবহার করা হয়েছে)
