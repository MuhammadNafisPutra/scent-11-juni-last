package com.contoh.scentapp.ui.theme

import android.app.Activity
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val DarkColorScheme = darkColorScheme(
    primary       = ScentGold,
    background    = ScentBlack,
    surface       = Color(0xFF1A1A1A),
    surfaceVariant= Color(0xFF1E1E1E),
    onPrimary     = ScentBlack,
    onBackground  = ScentWhite,
    onSurface     = ScentWhite,
    outline       = Color(0xFF333333),
    outlineVariant= Color(0xFF2A2A2A),
    secondaryContainer = Color(0xFF1E1E1E)
)

private val LightColorScheme = lightColorScheme(
    primary       = ScentGold,
    background    = Color(0xFFF8F6F2),
    surface       = Color(0xFFFFFFFF),
    surfaceVariant= Color(0xFFF0EDE8),
    onPrimary     = ScentWhite,
    onBackground  = Color(0xFF1A1A1A),
    onSurface     = Color(0xFF1A1A1A),
    outline       = Color(0xFFD8D0C8),
    outlineVariant= Color(0xFFEAE5DF),
    secondaryContainer = Color(0xFFF0EDE8)
)

@Composable
fun ScentAppTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content  : @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as AppCompatActivity).window // ✅ Ganti ke AppCompatActivity
            @Suppress("DEPRECATION")
            if (Build.VERSION.SDK_INT < 35) {
                window.statusBarColor = colorScheme.background.toArgb()
            }
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography  = Typography,
        content     = content
    )
}