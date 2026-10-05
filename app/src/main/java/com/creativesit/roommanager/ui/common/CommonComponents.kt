package com.creativesit.roommanager.ui.common

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.creativesit.roommanager.ui.theme.*

/** স্ক্রলযোগ্য, প্যাডেড কলাম — এডমিন/গ্রাহকের প্রতিটা স্ক্রিনে কমন লেআউট */
@Composable
fun ScrollScreen(modifier: Modifier = Modifier, content: @Composable ColumnScope.() -> Unit) {
    Column(
        modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        content()
    }
}

@Composable
fun SectionCard(title: String, modifier: Modifier = Modifier, content: @Composable ColumnScope.() -> Unit) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = CardBg),
        border = BorderStroke(1.dp, PaperLine),
        shape = RoundedCornerShape(10.dp)
    ) {
        Column(Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.size(width = 4.dp, height = 16.dp).background(Rule, RoundedCornerShape(2.dp)))
                Spacer(Modifier.width(8.dp))
                Text(title, style = MaterialTheme.typography.titleMedium)
            }
            Spacer(Modifier.height(10.dp))
            content()
        }
    }
}

@Composable
fun StatCard(number: String, label: String, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = CardBg),
        border = BorderStroke(1.dp, PaperLine),
        shape = RoundedCornerShape(10.dp)
    ) {
        Column(Modifier.padding(14.dp)) {
            Text(number, fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Rule)
            Spacer(Modifier.height(2.dp))
            Text(label, fontSize = 12.sp, color = InkSoft)
        }
    }
}

data class BadgeStyle(val bg: Color, val fg: Color)

fun statusBadgeStyle(status: String): BadgeStyle = when (status) {
    "paid", "done", "delivered" -> BadgeStyle(SageBg, Sage)
    "due", "missed", "failed" -> BadgeStyle(ClayBg, Clay)
    "partial", "sent" -> BadgeStyle(GoldBg, Gold)
    "pending", "queued" -> BadgeStyle(PurpleBg, Purple)
    "read" -> BadgeStyle(SageBg, Sage)
    "manager" -> BadgeStyle(ManagerBg, ManagerFg)
    else -> BadgeStyle(PaperLine, InkSoft)
}

fun statusLabel(status: String): String = when (status) {
    "paid" -> "পরিশোধিত"
    "due" -> "বকেয়া"
    "partial" -> "আংশিক"
    "pending" -> "বাকি আছে"
    "done" -> "সম্পন্ন"
    "missed" -> "মিস হয়েছে"
    "queued" -> "সারিতে আছে"
    "sent" -> "পাঠানো হয়েছে"
    "delivered" -> "পৌঁছেছে"
    "read" -> "পড়েছে"
    "failed" -> "ব্যর্থ"
    "active" -> "সক্রিয়"
    "inactive" -> "নিষ্ক্রিয়"
    else -> status
}

@Composable
fun StatusBadge(status: String, modifier: Modifier = Modifier, customLabel: String? = null) {
    val style = statusBadgeStyle(status)
    Box(
        modifier = modifier
            .background(style.bg, RoundedCornerShape(999.dp))
            .padding(horizontal = 10.dp, vertical = 3.dp)
    ) {
        Text(customLabel ?: statusLabel(status), color = style.fg, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
fun RoleBadge(role: String, modifier: Modifier = Modifier) {
    val label = when (role) { "admin" -> "এডমিন"; "manager" -> "ম্যানেজার"; else -> "গ্রাহক" }
    val style = if (role == "manager") BadgeStyle(ManagerBg, ManagerFg) else BadgeStyle(PaperLine, InkSoft)
    Box(modifier.background(style.bg, RoundedCornerShape(999.dp)).padding(horizontal = 10.dp, vertical = 3.dp)) {
        Text(label, color = style.fg, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
fun LoadingBox(modifier: Modifier = Modifier) {
    Box(modifier.fillMaxWidth().padding(30.dp), contentAlignment = Alignment.Center) {
        CircularProgressIndicator(color = Rule)
    }
}

@Composable
fun EmptyState(text: String, modifier: Modifier = Modifier) {
    Box(modifier.fillMaxWidth().padding(30.dp), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(Icons.Default.Info, contentDescription = null, tint = InkSoft)
            Spacer(Modifier.height(6.dp))
            Text(text, color = InkSoft, fontSize = 13.sp)
        }
    }
}

@Composable
fun ErrorBanner(message: String, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.fillMaxWidth()
            .background(ClayBg, RoundedCornerShape(8.dp))
            .padding(12.dp)
    ) {
        Text(message, color = Clay, fontSize = 13.sp)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SimpleDropdown(
    label: String,
    selectedLabel: String,
    options: List<String>,
    onSelect: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }, modifier = modifier) {
        OutlinedTextField(
            value = selectedLabel,
            onValueChange = {},
            readOnly = true,
            label = { Text(label) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier.fillMaxWidth().menuAnchor()
        )
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            options.forEachIndexed { idx, opt ->
                DropdownMenuItem(text = { Text(opt) }, onClick = { onSelect(idx); expanded = false })
            }
        }
    }
}

@Composable
fun PrimaryButton(text: String, onClick: () -> Unit, modifier: Modifier = Modifier, enabled: Boolean = true) {
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier,
        colors = ButtonDefaults.buttonColors(containerColor = Rule, contentColor = Color.White),
        shape = RoundedCornerShape(8.dp)
    ) { Text(text, fontWeight = FontWeight.SemiBold) }
}

@Composable
fun SecondaryButton(text: String, onClick: () -> Unit, modifier: Modifier = Modifier, enabled: Boolean = true) {
    OutlinedButton(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier,
        colors = ButtonDefaults.outlinedButtonColors(contentColor = Rule),
        border = BorderStroke(1.5.dp, Rule),
        shape = RoundedCornerShape(8.dp)
    ) { Text(text, fontSize = 13.sp) }
}
