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
    primary = Color(0xFF275D59), onPrimary = Color.White,
    background = Color(0xFFF5F4EE), onBackground = Color(0xFF172D32),
    surface = Color(0xFFF5F4EE), onSurface = Color(0xFF172D32),
    surfaceContainer = Color(0xFFE9EDE6), onSurfaceVariant = Color(0xFF526568),
    outline = Color(0xFF748481)
)
private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFFA5D4C5), onPrimary = Color(0xFF123A35),
    background = Color(0xFF101E24), onBackground = Color(0xFFE4EDE9),
    surface = Color(0xFF101E24), onSurface = Color(0xFFE4EDE9),
    surfaceContainer = Color(0xFF1C3037), onSurfaceVariant = Color(0xFFB3C7C6),
    outline = Color(0xFF7E9695)
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
