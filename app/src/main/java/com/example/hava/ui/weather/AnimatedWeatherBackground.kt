
package com.example.hava.ui.weather

import androidx.compose.animation.core.*
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.unit.dp
import kotlin.math.PI
import kotlin.math.sin
import kotlin.random.Random

private data class Particle(
    val x: Float,
    val y: Float,
    val speed: Float,
    val size: Float
)

@Composable
fun AnimatedWeatherBackground(
    kind: WeatherKind,
    isDay: Boolean,
    modifier: Modifier = Modifier,
    phase: SolarPhase = if (isDay) SolarPhase.DAY else SolarPhase.NIGHT
) {
    val night = phase == SolarPhase.NIGHT
    val protectLightText = night || phase == SolarPhase.DAWN || phase == SolarPhase.SUNSET ||
        kind == WeatherKind.RAINY || kind == WeatherKind.STORMY

    val colors = when {
        phase == SolarPhase.DAWN -> listOf(Color(0xFF242F55), Color(0xFFB26072), Color(0xFFF1A778))
        phase == SolarPhase.SUNSET -> listOf(Color(0xFF34314D), Color(0xFFB66357), Color(0xFFDB985E))
        night -> listOf(
            Color(0xFF1D2535),
            Color(0xFF343E55),
            Color(0xFF202A3B)
        )
        kind == WeatherKind.SUNNY -> listOf(
            Color(0xFFFFF8ED),
            Color(0xFFFFDFB9),
            Color(0xFFFFF6E8)
        )
        kind == WeatherKind.CLOUDY || kind == WeatherKind.FOGGY -> listOf(
            Color(0xFFE7EEF4),
            Color(0xFFB5C9D9),
            Color(0xFFF3F6F9)
        )
        kind == WeatherKind.RAINY ||
                kind == WeatherKind.STORMY -> listOf(
            Color(0xFF435C7B),
            Color(0xFF738DA9),
            Color(0xFF364B66)
        )
        else -> listOf(
            Color(0xFFF0F4FF),
            Color(0xFFCBDCF2),
            Color(0xFFF7FAFF)
        )
    }

    val top by animateColorAsState(colors[0], tween(1800), label = "skyTop")
    val middle by animateColorAsState(colors[1], tween(1800), label = "skyMiddle")
    val bottom by animateColorAsState(colors[2], tween(1800), label = "skyBottom")
    val skyColors = listOf(top, middle, bottom)

    val transition = rememberInfiniteTransition(
        label = "animatedWeather"
    )

    val time by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = 12000,
                easing = LinearEasing
            )
        ),
        label = "weatherTime"
    )

    val glow by transition.animateFloat(
        initialValue = 0.75f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = 3800,
                easing = FastOutSlowInEasing
            ),
            repeatMode = RepeatMode.Reverse
        ),
        label = "weatherGlow"
    )

    val particles = remember {
        val random = Random(42)
        List(130) {
            Particle(
                x = random.nextFloat(),
                y = random.nextFloat(),
                speed = (1 + random.nextInt(3)).toFloat(),
                size = 0.6f + random.nextFloat() * 2f
            )
        }
    }

    Canvas(
        modifier = modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(skyColors))
    ) {
        val w = size.width
        val h = size.height

        val cx = w * 0.84f
        val cy = h * 0.28f
        val radius = w * 0.21f
        val horizon = cy + radius * 0.25f

        val diskColor = when {
            night -> Color(0xFFE8ECF5)
            kind == WeatherKind.SUNNY -> Color(0xFFFFAD70)
            else -> Color(0xFFDCE8F2)
        }

        val floatY = sin(time * 2f * PI).toFloat() * 8.dp.toPx()

        // IŞIK HALOSU
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    diskColor.copy(alpha = 0.30f * glow),
                    diskColor.copy(alpha = 0.12f * glow),
                    Color.Transparent
                ),
                center = Offset(cx, cy),
                radius = radius * 2.3f
            ),
            radius = radius * 2.3f,
            center = Offset(cx, cy)
        )

        // GÜNEŞ / AY
        drawCircle(
            brush = Brush.verticalGradient(
                listOf(
                    diskColor,
                    diskColor.copy(alpha = 0.75f)
                ),
                startY = cy - radius,
                endY = cy + radius
            ),
            radius = radius,
            center = Offset(cx, cy + floatY)
        )

        // UFUK ÇİZGİSİ VE YANSIMA
        clipRect(top = horizon, bottom = h) {
            drawRect(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        skyColors[1].copy(alpha = 0.82f),
                        skyColors[2].copy(alpha = 0.92f)
                    ),
                    startY = horizon,
                    endY = h
                ),
                topLeft = Offset(0f, horizon),
                size = Size(w, h - horizon)
            )

            drawOval(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        diskColor.copy(alpha = 0.42f * glow),
                        diskColor.copy(alpha = 0.12f),
                        Color.Transparent
                    ),
                    startY = horizon,
                    endY = horizon + radius * 1.5f
                ),
                topLeft = Offset(
                    cx - radius * 1.1f,
                    horizon
                ),
                size = Size(
                    radius * 2.2f,
                    radius * 1.5f
                )
            )
        }

        // HAREKET EDEN BULUTLAR
        if (kind == WeatherKind.CLOUDY || kind == WeatherKind.FOGGY ||
            kind == WeatherKind.RAINY ||
            kind == WeatherKind.STORMY
        ) {
            val cloudColor = if (night) {
                Color(0xFF8191AA)
            } else {
                Color(0xFFE5EDF5)
            }

            drawCloud(
                x = w * 0.1f + sin(time * 2f * PI).toFloat() * w * 0.25f,
                y = h * 0.22f,
                width = w * 0.65f,
                color = cloudColor.copy(alpha = 0.65f)
            )

            drawCloud(
                x = w * 0.6f - sin(time * 2f * PI).toFloat() * w * 0.25f,
                y = h * 0.34f,
                width = w * 0.8f,
                color = cloudColor.copy(alpha = 0.50f)
            )
        }

        // YAĞMUR
        if (kind == WeatherKind.RAINY ||
            kind == WeatherKind.STORMY
        ) {
            particles.take(
                if (kind == WeatherKind.STORMY) 130 else 90
            ).forEach { particle ->

                val progress = (
                        particle.y +
                                time * particle.speed * 3f
                        ) % 1f

                val x = particle.x * w
                val y = progress * h

                drawLine(
                    color = Color.White.copy(alpha = 0.38f),
                    start = Offset(x, y),
                    end = Offset(
                        x - 8.dp.toPx(),
                        y + 22.dp.toPx()
                    ),
                    strokeWidth = particle.size.dp.toPx()
                )
            }
        }

        // KAR
        if (kind == WeatherKind.SNOWY) {
            particles.take(85).forEach { particle ->

                val progress = (
                        particle.y +
                                time * particle.speed
                        ) % 1f

                val drift = sin(
                    (time * 2f * PI + particle.y * 8f).toDouble()
                ).toFloat() * 20.dp.toPx()

                drawCircle(
                    color = Color.White.copy(alpha = 0.85f),
                    radius = particle.size.dp.toPx(),
                    center = Offset(
                        particle.x * w + drift,
                        progress * h
                    )
                )
            }
        }

        // YILDIZLAR
        if (night) {
            particles.take(35).forEach { particle ->
                val twinkle = (
                        0.35f +
                                0.35f * sin(
                            (time * 2f * PI +
                                    particle.x * 12f).toDouble()
                        ).toFloat()
                        ).coerceIn(0.1f, 0.8f)

                drawCircle(
                    color = Color.White.copy(alpha = twinkle),
                    radius = particle.size.dp.toPx() * 0.6f,
                    center = Offset(
                        particle.x * w,
                        particle.y * h * 0.65f
                    )
                )
            }
        }

        // ŞİMŞEK
        if (kind == WeatherKind.STORMY) {
            val flash = time in 0.16f..0.18f ||
                    time in 0.55f..0.565f

            if (flash) {
                drawRect(
                    Color.White.copy(alpha = 0.32f)
                )

                val path = Path().apply {
                    moveTo(w * 0.65f, h * 0.15f)
                    lineTo(w * 0.54f, h * 0.29f)
                    lineTo(w * 0.63f, h * 0.29f)
                    lineTo(w * 0.49f, h * 0.47f)
                }

                drawPath(
                    path = path,
                    color = Color.White,
                    style = androidx.compose.ui.graphics.drawscope.Stroke(
                        width = 3.dp.toPx()
                    )
                )
            }
        }
        if (protectLightText) {
            drawRect(Brush.horizontalGradient(listOf(
                Color.Black.copy(alpha = 0.45f), Color.Black.copy(alpha = 0.12f)
            )))
        }
    }
}

private fun DrawScope.drawCloud(
    x: Float,
    y: Float,
    width: Float,
    color: Color
) {
    val height = width * 0.34f

    drawOval(
        color = color,
        topLeft = Offset(x, y + height * 0.35f),
        size = Size(width, height * 0.65f)
    )

    drawCircle(
        color = color,
        radius = width * 0.19f,
        center = Offset(
            x + width * 0.35f,
            y + height * 0.45f
        )
    )

    drawCircle(
        color = color,
        radius = width * 0.24f,
        center = Offset(
            x + width * 0.62f,
            y + height * 0.35f
        )
    )
}
