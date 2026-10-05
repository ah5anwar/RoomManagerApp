package com.creativesit.roommanager.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

private val LightColors = lightColorScheme(
    primary = Rule,
    onPrimary = Color.White,
    secondary = Gold,
    background = Paper,
    surface = CardBg,
    onBackground = Ink,
    onSurface = Ink,
    error = Clay,
)

private val AppTypography = Typography(
    titleLarge = TextStyle(fontWeight = FontWeight.Bold, fontSize = 22.sp, color = Ink),
    titleMedium = TextStyle(fontWeight = FontWeight.SemiBold, fontSize = 17.sp, color = Ink),
    bodyLarge = TextStyle(fontSize = 15.sp, color = Ink),
    bodyMedium = TextStyle(fontSize = 13.sp, color = Ink),
    labelSmall = TextStyle(fontSize = 11.sp, color = InkSoft),
)

@Composable
fun RoomManagerTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = LightColors,
        typography = AppTypography,
        content = content
    )
}
