package com.example.taskplanner.ui.theme

import android.os.Build
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = Blue80,
    secondary = Green80,
    tertiary = Coral80,
    background = Color(0xFF101418),
    surface = Color(0xFF101418),
    surfaceContainer = Color(0xFF1B2025),
    onBackground = Color(0xFFE1E6EC),
    onSurface = Color(0xFFE1E6EC),
    onSurfaceVariant = Color(0xFFC0C7CF)
)

private val LightColorScheme = lightColorScheme(
    primary = Blue40,
    secondary = Green40,
    tertiary = Coral40,
    background = Color(0xFFFAFCFF),
    surface = Color(0xFFFAFCFF),
    surfaceContainer = Color(0xFFFFFFFF),
    onBackground = Color(0xFF191C20),
    onSurface = Color(0xFF191C20),
    onSurfaceVariant = Color(0xFF535F6B)
)

@Composable
fun TaskPlannerTheme(
    darkTheme: Boolean,
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
