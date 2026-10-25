package com.example.hava.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import androidx.compose.ui.graphics.Color

private val LightColorScheme = lightColorScheme(
    primary = Color(0xFF97512F), onPrimary = Color.White,
    background = Color(0xFFF8F6F2), onBackground = Color(0xFF302C2A),
    surface = Color(0xFFF8F6F2), onSurface = Color(0xFF302C2A),
    surfaceContainer = Color(0xFFEEEAE5), onSurfaceVariant = Color(0xFF706760),
    outline = Color(0xFF9B8F84)
)
private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFFFFCB97), onPrimary = Color(0xFF49301E),
    background = Color(0xFF131B2C), onBackground = Color(0xFFF4F1F6),
    surface = Color(0xFF131B2C), onSurface = Color(0xFFF4F1F6),
    surfaceContainer = Color(0xFF202C43), onSurfaceVariant = Color(0xFFB6BCCC),
    outline = Color(0xFF8490A7)
)

@Composable
fun HavaTheme(darkTheme: Boolean = isSystemInDarkTheme(), content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme,
        typography = Typography, content = content)
}

@Composable
fun SystemBarAppearance(darkIcons: Boolean) {
    val view = LocalView.current
    val context = LocalContext.current
    SideEffect {
        var owner: Context = context
        while (owner is ContextWrapper && owner !is Activity) owner = owner.baseContext
        (owner as? Activity)?.let { activity ->
            WindowCompat.getInsetsController(activity.window, view).apply {
                isAppearanceLightStatusBars = darkIcons
                isAppearanceLightNavigationBars = darkIcons
            }
        }
    }
}
