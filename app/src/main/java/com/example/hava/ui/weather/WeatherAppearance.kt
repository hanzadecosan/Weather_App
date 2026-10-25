package com.example.hava.ui.weather

import androidx.compose.ui.graphics.Color

/** The solar widget has its own event palette, independent of the screen background. */
internal data class SolarCardPalette(
    val colors: List<Color>,
    val ink: Color,
    val border: Color
) {
    val muted: Color get() = ink.copy(alpha = 0.90f)
}

internal fun solarCardPalette(sunrise: Boolean, dark: Boolean): SolarCardPalette {
    val colors = when {
        sunrise && dark -> listOf(Color(0xFF40374F), Color(0xFF714451), Color(0xFF835846))
        sunrise -> listOf(Color(0xFFEAC8EF), Color(0xFFF6C2C2), Color(0xFFFFE3B4))
        dark -> listOf(Color(0xFF7A4537), Color(0xFF6A3449), Color(0xFF40324F))
        else -> listOf(Color(0xFFFFCE88), Color(0xFFF8A683), Color(0xFFED9199))
    }
    return SolarCardPalette(
        colors = colors,
        ink = if (dark) Color(0xFFFFF4E7) else Color(0xFF402A35),
        border = Color.White.copy(alpha = if (dark) 0.24f else 0.65f)
    )
}

/** Visual tokens only. Forecast and solar-time decisions remain outside this file. */
data class WeatherAppearance(
    val paper: Color,
    val glow: Color,
    val secondaryGlow: Color,
    val ink: Color,
    val isDark: Boolean = false
) {
    val muted: Color get() = ink.copy(alpha = 0.66f)
    val glass: Color get() = if (isDark) Color.White.copy(alpha = 0.10f) else Color.White.copy(alpha = 0.62f)
    val border: Color get() = if (isDark) Color.White.copy(alpha = 0.18f) else Color.White.copy(alpha = 0.85f)
}

fun weatherAppearance(kind: WeatherKind, phase: SolarPhase): WeatherAppearance = when (phase) {
    SolarPhase.DAWN -> WeatherAppearance(Color(0xFFFFF6EE), Color(0xFFF5A6AF), Color(0xFFFFD57B), Color(0xFF35242F))
    SolarPhase.SUNSET -> WeatherAppearance(Color(0xFFFFF5EC), Color(0xFFFFA33E), Color(0xFFF07862), Color(0xFF35251F))
    SolarPhase.NIGHT -> WeatherAppearance(Color(0xFF131B32), Color(0xFF384D86), Color(0xFF594E83), Color(0xFFF3F1F8), true)
    SolarPhase.DAY -> when (kind) {
        WeatherKind.SUNNY -> WeatherAppearance(Color(0xFFF8F6F1), Color(0xFFFFCC5C), Color(0xFFFFA455), Color(0xFF2C2925))
        WeatherKind.CLOUDY, WeatherKind.FOGGY -> WeatherAppearance(Color(0xFFF2F5F7), Color(0xFFA6C6DF), Color(0xFF6A91BF), Color(0xFF263445))
        WeatherKind.RAINY, WeatherKind.STORMY -> WeatherAppearance(Color(0xFFF0F3F6), Color(0xFF91B5D1), Color(0xFF7483BA), Color(0xFF263346))
        WeatherKind.SNOWY -> WeatherAppearance(Color(0xFFF5F6FA), Color(0xFFC7D5F2), Color(0xFFAFAEDC), Color(0xFF303449))
    }
}
