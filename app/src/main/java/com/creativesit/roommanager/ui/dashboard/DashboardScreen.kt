package com.creativesit.roommanager.ui.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.creativesit.roommanager.AppContainer
import com.creativesit.roommanager.data.model.User
import com.creativesit.roommanager.ui.admin.*
import com.creativesit.roommanager.ui.common.LoadingBox
import com.creativesit.roommanager.ui.common.WebPageScreen
import com.creativesit.roommanager.ui.customer.*
import com.creativesit.roommanager.ui.theme.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(onLogout: () -> Unit) {
    val user by AppContainer.sessionManager.userFlow.collectAsState(initial = null)

    if (user == null) {
        LoadingBox()
        return
    }
    val currentUser = user!!
    val sections = remember(currentUser) { Sections.forRole(currentUser.role, currentUser.permissions ?: emptyList()) }
    var selected by remember { mutableStateOf(sections.first()) }
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    // ড্যাশবোর্ড খোলার সাথে সাথেই সার্ভার থেকে সর্বশেষ পারমিশন/তথ্য রিফ্রেশ করা হয় —
    // এডমিন লগইনের পরে পারমিশন বদলালেও যেন সাথে সাথে প্রতিফলিত হয়
    LaunchedEffect(Unit) {
        AppContainer.authRepository.refreshMe()
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(drawerContainerColor = Ink) {
                Column(Modifier.fillMaxHeight().padding(vertical = 20.dp)) {
                    Text(
                        "RM", color = Gold, fontWeight = FontWeight.Bold, fontSize = 20.sp,
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 10.dp)
                    )
                    LazyColumn(Modifier.weight(1f)) {
                        items(sections) { sec ->
                            val isSelected = sec.id == selected.id
                            Row(
                                Modifier.fillMaxWidth()
                                    .background(if (isSelected) Rule else Color.Transparent, RoundedCornerShape(8.dp))
                                    .clickable { selected = sec; scope.launch { drawerState.close() } }
                                    .padding(horizontal = 20.dp, vertical = 14.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(sec.icon, fontSize = 16.sp)
                                Spacer(Modifier.width(14.dp))
                                Text(sec.label, color = if (isSelected) Color.White else Color(0xFFCFC9BA), fontSize = 14.sp)
                            }
                        }
                    }
                    Row(
                        Modifier.fillMaxWidth()
                            .clickable {
                                scope.launch { AppContainer.authRepository.logout(); onLogout() }
                            }
                            .padding(horizontal = 20.dp, vertical = 14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.ExitToApp, contentDescription = null, tint = Color(0xFFCFC9BA))
                        Spacer(Modifier.width(14.dp))
                        Text("লগআউট", color = Color(0xFFCFC9BA), fontSize = 14.sp)
                    }
                }
            }
        }
    ) {
        Scaffold(
            containerColor = Paper,
            topBar = {
                TopAppBar(
                    title = { Text(selected.label, fontWeight = FontWeight.Bold, color = Ink) },
                    navigationIcon = {
                        IconButton(onClick = { scope.launch { drawerState.open() } }) {
                            Icon(Icons.Default.Menu, contentDescription = "মেনু", tint = Ink)
                        }
                    },
                    actions = {
                        Column(horizontalAlignment = Alignment.End, modifier = Modifier.padding(end = 12.dp)) {
                            Text(
                                when (currentUser.role) { "admin" -> "এডমিন"; "manager" -> "ম্যানেজার"; else -> "গ্রাহক" },
                                fontSize = 10.sp, color = Gold, fontWeight = FontWeight.Bold
                            )
                            Text(currentUser.name, fontSize = 11.sp, color = InkSoft)
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Paper)
                )
            }
        ) { padding ->
            Box(Modifier.padding(padding).fillMaxSize()) {
                DashboardContent(section = selected.id, currentUser = currentUser)
            }
        }
    }
}

@Composable
private fun DashboardContent(section: String, currentUser: User) {
    when (section) {
        "overview" -> if (currentUser.isCustomer) CustomerHomeScreen(currentUser) else AdminHomeScreen(currentUser)
        "users" -> AdminUsersScreen(currentUser)
        "rent" -> if (currentUser.isCustomer) CustomerRentScreen(currentUser) else AdminRentScreen()
        "market", "cooking", "trash", "cleaning" ->
            if (currentUser.isCustomer) CustomerDutyScreen(currentUser, section) else AdminDutyManageScreen(section)
        "notify" -> AdminNotifyScreen()
        "notifications" -> CustomerNotificationsScreen()
        "chat" -> if (currentUser.isCustomer) CustomerChatScreen(currentUser) else AdminChatScreen(currentUser)
        "settings" -> AdminSettingsScreen()
        "about" -> WebPageScreen(url = webPageUrl("about.html"))
        "developer" -> WebPageScreen(url = webPageUrl("developer.html"))
    }
}

/** BuildConfig.BASE_URL (…/RoomManager/api/) থেকে সাইটের রুট বের করে public/ ফোল্ডারের পেজের পূর্ণ URL বানায় */
private fun webPageUrl(page: String): String {
    val root = com.creativesit.roommanager.BuildConfig.BASE_URL.trimEnd('/').removeSuffix("/api")
    return "$root/public/$page"
}
