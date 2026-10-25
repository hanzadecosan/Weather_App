package com.example.hava.ui.weather

import androidx.compose.foundation.background
import androidx.compose.ui.graphics.luminance
import com.example.hava.ui.theme.SystemBarAppearance
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.repeatOnLifecycle
import kotlinx.coroutines.delay
import java.time.Instant
import java.time.Clock
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.math.roundToInt

private val Ink = Color(0xFF142E36)
private fun Double?.degrees(): String = this?.takeIf { it.isFinite() }?.let { "${it.roundToInt()}°" } ?: "—"
private val hourFormat = DateTimeFormatter.ofPattern("HH:mm")

@Composable
fun WeatherDetailScreen(cityName: String, state: WeatherUiState, onBack: () -> Unit, onRetry: () -> Unit,
    clock: Clock = Clock.systemUTC()) {
    val lifecycle = LocalLifecycleOwner.current
    var now by remember(cityName, clock) { mutableStateOf(clock.instant()) }
    LaunchedEffect(lifecycle, cityName, clock) {
        lifecycle.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
            while (true) { now = clock.instant(); delay(15_000) }
        }
    }
    // Refresh stale measurements on resume and while the screen stays open.
    LaunchedEffect(now, state.updatedAt, state.cityName) {
        if (state.cityName == cityName && state.weather != null && !state.isLoading && state.error == null &&
            now.toEpochMilli() - state.updatedAt >= 600_000) onRetry()
    }
    val weather = state.weather.takeIf { state.cityName == cityName }
    if (weather == null) {
        SystemBarAppearance(darkIcons = MaterialTheme.colorScheme.background.luminance() > 0.5f)
        Surface(Modifier.fillMaxSize()) {
            Column(Modifier.safeDrawingPadding().padding(24.dp)) {
                TextButton(onClick = onBack) { Text("← Şehirler") }
                Column(Modifier.weight(1f).fillMaxWidth(), verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally) {
                    if (state.cityName == cityName && state.error != null) {
                        Text("Gökyüzüne ulaşamadık", style = MaterialTheme.typography.headlineSmall)
                        Spacer(Modifier.height(12.dp))
                        Text(state.error)
                        Spacer(Modifier.height(20.dp))
                        Button(onClick = onRetry) { Text("Tekrar dene") }
                    } else {
                        CircularProgressIndicator()
                        Spacer(Modifier.height(20.dp))
                        Text("$cityName için hava hazırlanıyor…")
                    }
                }
            }
        }
        return
    }
    val timeline = weatherTimeline(weather, now)
    val current = weather.current
    val kind = weatherKind(current.weatherCode)
    val isDay = if (timeline.sunrise != null && timeline.sunset != null)
        timeline.now >= timeline.sunrise && timeline.now < timeline.sunset
    else current.isDay == 1
    val specialLight = timeline.phase == SolarPhase.DAWN || timeline.phase == SolarPhase.SUNSET
    val foreground = if (specialLight || !isDay || kind == WeatherKind.RAINY || kind == WeatherKind.STORMY) Color.White else Ink
    val card = if (foreground == Color.White) Color(0xFF132737).copy(alpha = 0.84f) else Color(0xFFFFFDF7).copy(alpha = 0.92f)
    SystemBarAppearance(darkIcons = foreground != Color.White)
    val description = if (kind == WeatherKind.SUNNY && !isDay) "Açık" else weatherDescription(kind)
    val format = DateTimeFormatter.ofPattern("d MMMM EEEE · HH:mm", Locale.forLanguageTag("tr-TR"))
    Box(Modifier.fillMaxSize()) {
        AnimatedWeatherBackground(kind, isDay, Modifier.matchParentSize(), phase = timeline.phase)
        Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).safeDrawingPadding().padding(horizontal = 24.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically) {
                TextButton(onClick = onBack) { Text("← Şehirler", color = foreground) }
                TextButton(onClick = onRetry, enabled = !state.isLoading) { Text(if (state.isLoading) "Yenileniyor…" else "Yenile ↻", color = foreground) }
            }
            Spacer(Modifier.height(22.dp))
            if (state.error != null) {
                Panel(card) { Text("${state.error} Son alınan veriler gösteriliyor.", color = foreground) }
                Spacer(Modifier.height(16.dp))
            }
            Text("TÜRKİYE / ANLIK HAVA", color = foreground.copy(alpha = 0.8f),
                style = MaterialTheme.typography.labelLarge)
            Text(cityName, color = foreground, style = MaterialTheme.typography.displaySmall, fontWeight = FontWeight.Medium)
            Text(timeline.now.format(format), color = foreground.copy(alpha = 0.8f))
            Spacer(Modifier.height(32.dp))
            Text(current.temperature.degrees(), color = foreground, fontSize = 100.sp,
                fontWeight = FontWeight.Light, lineHeight = 108.sp)
            Text(description, color = foreground, style = MaterialTheme.typography.headlineSmall)
            Text("Hissedilen ${current.feelsLike.degrees()}", color = foreground.copy(alpha = 0.85f))
            Spacer(Modifier.height(28.dp))
            Panel(card) {
                Text(when (timeline.phase) {
                    SolarPhase.DAWN -> "GÜNÜN İLK IŞIĞI"
                    SolarPhase.SUNSET -> "ALTIN SAAT"
                    else -> "GÜNÜN IŞIĞINI YAKALA"
                }, color = foreground.copy(alpha = 0.75f), style = MaterialTheme.typography.labelMedium)
                Spacer(Modifier.height(8.dp))
                Text(solarMessage(timeline), color = foreground, style = MaterialTheme.typography.titleLarge)
                Spacer(Modifier.height(16.dp))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Metric("Gün doğumu", timeline.sunrise?.format(hourFormat) ?: "—", foreground)
                    Metric("Gün batımı", timeline.sunset?.format(hourFormat) ?: "—", foreground)
                }
                if (kind != WeatherKind.SUNNY) {
                    Spacer(Modifier.height(12.dp))
                    Text("Görünürlük hava koşullarına bağlıdır.", color = foreground.copy(alpha = 0.75f), style = MaterialTheme.typography.bodySmall)
                }
            }
            Spacer(Modifier.height(14.dp))
            Panel(card) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Metric("En yüksek", weather.daily.maxTemperatures.getOrNull(timeline.dailyIndex).degrees(), foreground)
                    Metric("Nem", current.humidity?.let { "%$it" } ?: "—", foreground)
                    Metric("Rüzgâr", current.windSpeed?.takeIf { it.isFinite() }?.let { "${it.roundToInt()} km/sa" } ?: "—", foreground)
                }
            }
            Spacer(Modifier.height(28.dp))
            Text("Önümüzdeki saatler", color = foreground, style = MaterialTheme.typography.titleLarge)
            Spacer(Modifier.height(12.dp))
            if (timeline.hourlyIndices.isEmpty()) Text("Saatlik tahmin şu an mevcut değil.", color = foreground)
            Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                timeline.hourlyIndices.forEach { index ->
                    val time = parseWeatherTime(weather.hourly.time[index])!!
                    val code = weather.hourly.weatherCodes[index]
                    val hourKind = code?.let(::weatherKind)
                    val hourDay = weather.hourly.isDay.getOrNull(index) == 1
                    Column(Modifier.width(88.dp).background(card, RoundedCornerShape(22.dp)).padding(vertical = 18.dp),
                        horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(time.format(hourFormat), color = foreground, style = MaterialTheme.typography.labelLarge)
                        if (time.toLocalDate() != timeline.now.toLocalDate()) Text("Yarın", color = foreground, style = MaterialTheme.typography.labelSmall)
                        Spacer(Modifier.height(12.dp))
                        Text(if (hourKind == null) "—" else if (hourKind == WeatherKind.SUNNY && !hourDay) "☾" else weatherSymbol(hourKind),
                            color = foreground, fontSize = 26.sp)
                        Spacer(Modifier.height(12.dp))
                        Text(weather.hourly.temperatures[index].degrees(), color = foreground, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
            Spacer(Modifier.height(18.dp))
            Panel(card) {
                Text("Bugün aklında olsun", color = foreground, style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(8.dp))
                Text(if (!isDay && kind == WeatherKind.SUNNY) "Açık bir gece. Şehrin ışıklarından uzaklaşıp gökyüzüne bak." else weatherAdvice(kind), color = foreground)
            }
            Spacer(Modifier.height(18.dp))
            Text("Veri: Open-Meteo · Son güncelleme ${java.time.LocalDateTime.ofInstant(Instant.ofEpochMilli(state.updatedAt), runCatching { java.time.ZoneId.of(weather.timezone) }.getOrDefault(java.time.ZoneId.of("Europe/Istanbul"))).format(hourFormat)}",
                color = foreground.copy(alpha = 0.8f), style = MaterialTheme.typography.bodySmall)
            Spacer(Modifier.height(28.dp))
        }
    }
}

@Composable
private fun Panel(color: Color, content: @Composable ColumnScope.() -> Unit) {
    Column(Modifier.fillMaxWidth().background(color, RoundedCornerShape(24.dp)).padding(20.dp), content = content)
}

@Composable
private fun Metric(label: String, value: String, color: Color) {
    Column {
        Text(label, color = color.copy(alpha = 0.75f), style = MaterialTheme.typography.labelMedium)
        Spacer(Modifier.height(6.dp))
        Text(value, color = color, style = MaterialTheme.typography.titleMedium)
    }
}
