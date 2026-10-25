package com.example.hava.ui.weather

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import kotlin.math.PI
import kotlin.math.sin

/** Soft mesh-like light fields and seamless diagonal light/rain trails. No layout work per frame. */
@Composable
fun AnimatedWeatherBackground(
    kind: WeatherKind,
    isDay: Boolean,
    modifier: Modifier = Modifier,
    phase: SolarPhase = if (isDay) SolarPhase.DAY else SolarPhase.NIGHT
) {
    val appearance = weatherAppearance(kind, phase)
    val paper by animateColorAsState(appearance.paper, tween(1600), label = "paper")
    val glow by animateColorAsState(appearance.glow, tween(1600), label = "glow")
    val secondary by animateColorAsState(appearance.secondaryGlow, tween(1600), label = "secondaryGlow")
    val enabled = weatherMotionEnabled()
    val progress = ambientProgress(enabled)
    Canvas(modifier.fillMaxSize().background(paper)) {
        val time = progress.value
        val drift = sin(time * 2 * PI).toFloat()
        val w = size.width
        val h = size.height
        fun light(center: Offset, radius: Float, color: Color, alpha: Float) {
            drawCircle(Brush.radialGradient(listOf(color.copy(alpha = alpha), color.copy(alpha = 0f)),
                center = center, radius = radius), radius, center)
        }
        light(Offset(w * (0.82f + drift * 0.08f), h * 0.08f), w * 1.10f, glow, if (appearance.isDark) 0.58f else 0.95f)
        light(Offset(w * (0.06f - drift * 0.07f), h * 0.33f), w * 0.82f, secondary, if (appearance.isDark) 0.24f else 0.35f)
        val wet = kind == WeatherKind.RAINY || kind == WeatherKind.STORMY
        repeat(if (wet) 28 else 18) { index ->
            val x = w * (index / 15f - 0.4f) + drift * w * 0.05f
            val y = if (wet) ((index * 0.137f + time * 4) % 1f) * h else h * 0.02f + index % 4 * h * 0.035f
            drawLine(color = (if (appearance.isDark) Color(0xFFACBBDD) else Color.White).copy(alpha = if (wet) 0.20f else 0.27f),
                start = Offset(x, y), end = Offset(x + w * 0.52f, y - h * 0.24f),
                strokeWidth = if (index % 5 == 0) 3.5f else 1.2f)
        }
        if (phase == SolarPhase.NIGHT || kind == WeatherKind.SNOWY) {
            repeat(30) { index ->
                val x = ((index * 0.618f) % 1f) * w
                val y = ((index * 0.137f + if (kind == WeatherKind.SNOWY) time else 0f) % 1f) * h
                drawCircle(Color.White.copy(alpha = 0.18f + 0.15f * sin(time * 2 * PI + index).toFloat()),
                    radius = 1.5f + index % 3, center = Offset(x, y))
            }
        }
    }
}

@Composable
private fun ambientProgress(enabled: Boolean): State<Float> {
    if (!enabled) return remember { mutableFloatStateOf(0f) }
    val transition = rememberInfiniteTransition(label = "ambientLight")
    return transition.animateFloat(0f, 1f,
        infiniteRepeatable(tween(24_000, easing = LinearEasing)), label = "ambientProgress")
}
