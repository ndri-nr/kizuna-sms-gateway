package com.kizunagateway.core.ui.theme

import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.material3.MaterialTheme

object KizunaColors {
    val Primary = Color(0xFF00A884)
    val Background = Color(0xFF111B21)
    val Surface = Color(0xFF202C33)
    val OnSurface = Color(0xFFE9EDEF)
    val Muted = Color(0xFF8696A0)
    val Success = Color(0xFF00A884)
    val Error = Color(0xFFF15C5C)
}

private val KizunaColorScheme = darkColorScheme(
    primary = KizunaColors.Primary,
    onPrimary = Color.White,
    secondary = KizunaColors.Primary,
    background = KizunaColors.Background,
    surface = KizunaColors.Surface,
    onBackground = KizunaColors.OnSurface,
    onSurface = KizunaColors.OnSurface,
    surfaceVariant = KizunaColors.Surface,
    onSurfaceVariant = KizunaColors.Muted
)

@Composable
fun KizunaTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = KizunaColorScheme,
        content = content
    )
}
