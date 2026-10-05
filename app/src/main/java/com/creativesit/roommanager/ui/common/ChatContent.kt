package com.creativesit.roommanager.ui.common

import android.Manifest
import android.content.pm.PackageManager
import android.media.MediaPlayer
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import coil.compose.AsyncImage
import com.creativesit.roommanager.AppContainer
import com.creativesit.roommanager.data.model.ChatMessage
import com.creativesit.roommanager.data.model.User
import com.creativesit.roommanager.data.remote.ApiResult
import com.creativesit.roommanager.ui.theme.*
import com.creativesit.roommanager.util.VoiceRecorder
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/** PHP থেকে আসা রিলেটিভ file_path (যেমন uploads/chat_images/xyz.jpg) কে সম্পূর্ণ URL এ রূপান্তর করে */
fun mediaFullUrl(relativePath: String?): String? {
    if (relativePath.isNullOrBlank()) return null
    val rootUrl = com.creativesit.roommanager.BuildConfig.BASE_URL.removeSuffix("/").removeSuffix("/api")
    return "$rootUrl/${relativePath.removePrefix("/")}"
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatContent(
    currentUser: User,
    allowPrivateTab: Boolean,
    otherUsers: List<User> = emptyList()
) {
    var mode by remember { mutableStateOf("group") } // group | private
    var privateTarget by remember { mutableStateOf<User?>(null) }
    var messages by remember { mutableStateOf<List<ChatMessage>>(emptyList()) }
    var input by remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val listState = rememberLazyListState()

    // otherUsers পরে (অ্যাসিঙ্ক্রোনাসভাবে) লোড হতে পারে, তাই লোড হওয়ার পর এখনো কেউ সিলেক্ট না থাকলে প্রথমজনকে ডিফল্ট ধরা হয়
    LaunchedEffect(otherUsers) {
        if (privateTarget == null) privateTarget = otherUsers.firstOrNull()
    }

    suspend fun reload() {
        val res = if (mode == "group") AppContainer.chatRepository.group()
        else privateTarget?.let { AppContainer.chatRepository.private(it.id) } ?: ApiResult.Success(emptyList())
        if (res is ApiResult.Success) messages = res.data
    }

    // ট্যাব/প্রাইভেট টার্গেট বদলালে তাৎক্ষণিক রিলোড, তারপর প্রতি ৪ সেকেন্ডে পোলিং
    LaunchedEffect(mode, privateTarget) {
        while (true) {
            reload()
            delay(4000)
        }
    }
    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) listState.animateScrollToItem(messages.size - 1)
    }

    val imagePicker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        if (uri != null) scope.launch {
            val recv = if (mode == "private") privateTarget?.id else null
            val res = AppContainer.chatRepository.sendMedia(uri, "image", recv)
            if (res is ApiResult.Error) Toast.makeText(context, res.message, Toast.LENGTH_SHORT).show()
            reload()
        }
    }

    // ---- ভয়েস রেকর্ডিং ----
    val voiceRecorder = remember { VoiceRecorder(context) }
    var isRecording by remember { mutableStateOf(false) }
    var recordSeconds by remember { mutableStateOf(0) }

    fun uploadVoice() {
        val file = voiceRecorder.stopRecording()
        isRecording = false
        if (file == null) {
            Toast.makeText(context, "রেকর্ডিং ব্যর্থ হয়েছে", Toast.LENGTH_SHORT).show()
            return
        }
        if (recordSeconds < 1) {
            file.delete()
            Toast.makeText(context, "রেকর্ডিং খুব ছোট, আবার চেষ্টা করুন", Toast.LENGTH_SHORT).show()
            return
        }
        scope.launch {
            val recv = if (mode == "private") privateTarget?.id else null
            val res = AppContainer.chatRepository.sendMediaFile(file, "voice", recv)
            if (res is ApiResult.Error) Toast.makeText(context, res.message, Toast.LENGTH_SHORT).show()
            reload()
        }
    }

    val micPermissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        if (granted) {
            if (voiceRecorder.startRecording()) { isRecording = true; recordSeconds = 0 }
            else Toast.makeText(context, "রেকর্ডিং শুরু করা যায়নি", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(context, "ভয়েস মেসেজ পাঠাতে মাইক্রোফোনের অনুমতি দরকার", Toast.LENGTH_SHORT).show()
        }
    }

    LaunchedEffect(isRecording) {
        while (isRecording) {
            delay(1000)
            recordSeconds++
        }
    }

    fun onMicClick() {
        if (isRecording) {
            uploadVoice()
        } else {
            val hasPermission = ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED
            if (hasPermission) {
                if (voiceRecorder.startRecording()) { isRecording = true; recordSeconds = 0 }
                else Toast.makeText(context, "রেকর্ডিং শুরু করা যায়নি", Toast.LENGTH_SHORT).show()
            } else {
                micPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
            }
        }
    }

    DisposableEffect(Unit) {
        onDispose { if (voiceRecorder.isRecording()) voiceRecorder.cancelRecording() }
    }

    Column(Modifier.fillMaxSize().padding(16.dp)) {
        if (allowPrivateTab) {
            Row(Modifier.padding(bottom = 10.dp)) {
                ChatTab("গ্রুপ চ্যাট", mode == "group") { mode = "group" }
                Spacer(Modifier.width(8.dp))
                ChatTab("প্রাইভেট মেসেজ", mode == "private") { mode = "private" }
            }
            if (mode == "private" && otherUsers.isNotEmpty()) {
                var expanded by remember { mutableStateOf(false) }
                ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }) {
                    OutlinedTextField(
                        value = privateTarget?.let { "${it.name} (${it.user_code})" } ?: "",
                        onValueChange = {}, readOnly = true,
                        modifier = Modifier.fillMaxWidth().menuAnchor(),
                        label = { Text("গ্রাহক বেছে নিন") }
                    )
                    ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                        otherUsers.forEach { u ->
                            DropdownMenuItem(text = { Text("${u.name} (${u.user_code})") }, onClick = {
                                privateTarget = u; expanded = false
                            })
                        }
                    }
                }
                Spacer(Modifier.height(10.dp))
            }
        } else {
            Text(
                "সবাই এখানে বার্তা দেখতে ও পাঠাতে পারবেন। প্রাইভেট মেসেজ শুধু এডমিন পাঠাতে পারেন।",
                fontSize = 11.sp, color = InkSoft, modifier = Modifier.padding(bottom = 8.dp)
            )
        }

        LazyColumn(
            state = listState,
            modifier = Modifier.weight(1f).fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(messages, key = { it.id }) { m ->
                val mine = m.sender_id == currentUser.id
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = if (mine) Arrangement.Start else Arrangement.End
                ) {
                    Column(
                        Modifier
                            .background(if (mine) ClayBg else SageBg, RoundedCornerShape(12.dp))
                            .padding(horizontal = 12.dp, vertical = 8.dp)
                            .widthIn(max = 260.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(m.name ?: "", fontSize = 10.sp, color = InkSoft, modifier = Modifier.weight(1f))
                            // শুধু এডমিন যেকোনো মেসেজ ডিলিট করতে পারবেন
                            if (currentUser.isAdmin) {
                                Text(
                                    "🗑️", fontSize = 11.sp,
                                    modifier = Modifier.clickable {
                                        scope.launch {
                                            val res = AppContainer.chatRepository.deleteMessage(m.id)
                                            if (res is ApiResult.Error) Toast.makeText(context, res.message, Toast.LENGTH_SHORT).show()
                                            reload()
                                        }
                                    }
                                )
                            }
                        }
                        when (m.message_type) {
                            "text" -> Text(m.content ?: "", fontSize = 13.sp, color = Ink)
                            "image" -> AsyncImage(
                                model = mediaFullUrl(m.file_path), contentDescription = null,
                                modifier = Modifier.size(180.dp).padding(top = 4.dp)
                            )
                            "voice" -> VoiceMessageBubble(filePath = m.file_path)
                        }
                    }
                }
            }
        }

        if (isRecording) {
            Row(
                Modifier.fillMaxWidth().padding(bottom = 6.dp).background(ClayBg, RoundedCornerShape(8.dp)).padding(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("🔴 রেকর্ড হচ্ছে... ${recordSeconds}s", color = Rule, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
            }
        }
        Row(Modifier.padding(top = 10.dp), verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = { imagePicker.launch("image/*") }, enabled = !isRecording) {
                Icon(Icons.Default.Image, contentDescription = "ছবি পাঠান", tint = if (isRecording) InkSoft else Rule)
            }
            IconButton(onClick = { onMicClick() }) {
                Icon(
                    if (isRecording) Icons.Default.Stop else Icons.Default.Mic,
                    contentDescription = if (isRecording) "রেকর্ড বন্ধ করে পাঠান" else "ভয়েস রেকর্ড করুন",
                    tint = if (isRecording) Color(0xFFD32F2F) else Rule
                )
            }
            OutlinedTextField(
                value = input, onValueChange = { input = it },
                placeholder = { Text("মেসেজ লিখুন...") },
                modifier = Modifier.weight(1f),
                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Rule)
            )
            Spacer(Modifier.width(8.dp))
            IconButton(onClick = {
                if (input.isNotBlank()) {
                    val text = input; input = ""
                    scope.launch {
                        val recv = if (mode == "private") privateTarget?.id else null
                        val res = AppContainer.chatRepository.sendText(text, recv)
                        if (res is ApiResult.Error) Toast.makeText(context, res.message, Toast.LENGTH_SHORT).show()
                        reload()
                    }
                }
            }) {
                Icon(Icons.Default.Send, contentDescription = "পাঠান", tint = Rule)
            }
        }
    }
}

@Composable
private fun ChatTab(label: String, active: Boolean, onClick: () -> Unit) {
    Box(
        Modifier
            .background(if (active) ClayBg else CardBg, RoundedCornerShape(999.dp))
            .clickableChip(onClick)
            .padding(horizontal = 14.dp, vertical = 7.dp)
    ) {
        Text(label, fontSize = 12.sp, color = if (active) Rule else InkSoft, fontWeight = if (active) FontWeight.SemiBold else FontWeight.Normal)
    }
}

private fun Modifier.clickableChip(onClick: () -> Unit): Modifier =
    this.clickable(onClick = onClick)

/** ভয়েস মেসেজ বাবল - ট্যাপ করলে প্লে/পজ হয় */
@Composable
private fun VoiceMessageBubble(filePath: String?) {
    val context = LocalContext.current
    val fullUrl = remember(filePath) { mediaFullUrl(filePath) }
    var isPlaying by remember(filePath) { mutableStateOf(false) }
    var player by remember(filePath) { mutableStateOf<MediaPlayer?>(null) }

    DisposableEffect(filePath) {
        onDispose { player?.release(); player = null }
    }

    Row(
        Modifier
            .clickable {
                if (fullUrl.isNullOrBlank()) return@clickable
                if (isPlaying) {
                    player?.pause()
                    isPlaying = false
                } else {
                    try {
                        val existing = player
                        if (existing != null) {
                            existing.start()
                            isPlaying = true
                        } else {
                            val mp = MediaPlayer()
                            mp.setDataSource(fullUrl)
                            mp.setOnPreparedListener {
                                it.start()
                                isPlaying = true
                            }
                            mp.setOnCompletionListener { isPlaying = false }
                            mp.setOnErrorListener { _, _, _ ->
                                Toast.makeText(context, "ভয়েস মেসেজ চালানো যায়নি", Toast.LENGTH_SHORT).show()
                                isPlaying = false
                                true
                            }
                            mp.prepareAsync()
                            player = mp
                        }
                    } catch (e: Exception) {
                        Toast.makeText(context, "ভয়েস মেসেজ চালানো যায়নি", Toast.LENGTH_SHORT).show()
                    }
                }
            }
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            if (isPlaying) Icons.Default.Stop else Icons.Default.PlayArrow,
            contentDescription = if (isPlaying) "থামান" else "চালান",
            tint = Rule, modifier = Modifier.size(20.dp)
        )
        Spacer(Modifier.width(6.dp))
        Text("ভয়েস মেসেজ", fontSize = 12.sp, color = Ink)
    }
}
