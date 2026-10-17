package com.qasioon.expensetracker.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val Green = Color(0xFF0B6E4F)
val GreenLight = Color(0xFFB2F0DF)
val Amber = Color(0xFFFFB300)
val ExpenseRed = Color(0xFFD32F2F)
val IncomeGreen = Color(0xFF2E7D32)

private val LightColors = lightColorScheme(
    primary = Green,
    onPrimary = Color.White,
    primaryContainer = GreenLight,
    onPrimaryContainer = Color(0xFF00201A),
    secondary = Amber,
    onSecondary = Color.Black,
    background = Color(0xFFF7F8FA),
    onBackground = Color(0xFF191C1B),
    surface = Color.White,
    onSurface = Color(0xFF191C1B),
    surfaceVariant = Color(0xFFE7ECE9),
    onSurfaceVariant = Color(0xFF404944),
    outline = Color(0xFF707975),
    error = ExpenseRed
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFF63DBB8),
    onPrimary = Color(0xFF00382B),
    primaryContainer = Color(0xFF00513E),
    onPrimaryContainer = GreenLight,
    secondary = Amber,
    onSecondary = Color.Black,
    background = Color(0xFF101413),
    onBackground = Color(0xFFE0E3E1),
    surface = Color(0xFF101413),
    onSurface = Color(0xFFE0E3E1),
    surfaceVariant = Color(0xFF404944),
    onSurfaceVariant = Color(0xFFC0C9C4),
    outline = Color(0xFF8A938E),
    error = Color(0xFFFFB4AB)
)

@Composable
fun ExpenseTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        typography = Typography(),
        content = content
    )
}
