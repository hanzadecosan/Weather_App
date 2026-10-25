package com.example.hava.ui.weather

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.repeatOnLifecycle
import com.example.hava.ui.theme.SystemBarAppearance
import kotlinx.coroutines.delay
import java.time.Clock
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId

/** Coordinates lifecycle/time and state; visual sections live in WeatherComponents.kt. */
@Composable
fun WeatherDetailScreen(
    cityName: String,
    state: WeatherUiState,
    onBack: () -> Unit,
    onRetry: () -> Unit,
    clock: Clock = Clock.systemUTC()
) {
    val now = rememberWeatherTime(cityName, clock)
    LaunchedEffect(now, state.updatedAt, state.cityName) {
        if (state.cityName == cityName && state.weather != null && !state.isLoading && state.error == null &&
            now.toEpochMilli() - state.updatedAt >= 600_000) onRetry()
    }
    val weather = state.weather.takeIf { state.cityName == cityName }
    if (weather == null) {
        WeatherStatus(cityName, state.error.takeIf { state.cityName == cityName }, onBack, onRetry)
        return
    }
    val timeline = weatherTimeline(weather, now)
    val kind = weatherKind(weather.current.weatherCode)
    val appearance = weatherAppearance(kind, timeline.phase)
    val isDay = if (timeline.sunrise != null && timeline.sunset != null)
        timeline.now >= timeline.sunrise && timeline.now < timeline.sunset else weather.current.isDay == 1
    SystemBarAppearance(darkIcons = !appearance.isDark)
    Box(Modifier.fillMaxSize()) {
        AnimatedWeatherBackground(kind, isDay, Modifier.matchParentSize(), timeline.phase)
        Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).safeDrawingPadding()
            .padding(horizontal = 24.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                TextButton(onClick = onBack) { Text("← Şehirler", color = appearance.ink) }
                TextButton(onClick = onRetry, enabled = !state.isLoading) {
                    Text(if (state.isLoading) "Yenileniyor…" else "Yenile ↻", color = appearance.ink)
                }
            }
            Spacer(Modifier.height(12.dp))
            if (state.error != null) {
                GlassPanel(appearance) { Text("${state.error} Son alınan veriler gösteriliyor.", color = appearance.ink) }
                Spacer(Modifier.height(16.dp))
            }
            WeatherHero(cityName, weather, timeline, appearance, kind, isDay)
            Spacer(Modifier.height(24.dp))
            WeatherMetrics(weather, timeline.dailyIndex, appearance)
            Spacer(Modifier.height(16.dp))
            SolarLightCard(timeline, kind, appearance)
            Spacer(Modifier.height(28.dp))
            HourlyForecast(weather, timeline, appearance)
            Spacer(Modifier.height(22.dp))
            GlassPanel(appearance) {
                Text("Küçük bir not", color = appearance.ink, style = MaterialTheme.typography.titleSmall)
                Spacer(Modifier.height(8.dp))
                Text(if (!isDay && kind == WeatherKind.SUNNY)
                    "Açık bir gece. Şehrin ışıklarından uzaklaşıp gökyüzüne bak." else weatherAdvice(kind),
                    color = appearance.muted, style = MaterialTheme.typography.bodyMedium)
            }
            Spacer(Modifier.height(22.dp))
            val zone = runCatching { ZoneId.of(weather.timezone) }.getOrDefault(ZoneId.of("Europe/Istanbul"))
            val updated = LocalDateTime.ofInstant(Instant.ofEpochMilli(state.updatedAt), zone).format(WeatherHourFormat)
            Text("Open-Meteo · Güncelleme $updated", color = appearance.muted,
                style = MaterialTheme.typography.labelSmall, modifier = Modifier.align(Alignment.CenterHorizontally))
            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
private fun rememberWeatherTime(cityName: String, clock: Clock): Instant {
    val lifecycle = LocalLifecycleOwner.current.lifecycle
    var now by remember(cityName, clock) { mutableStateOf(clock.instant()) }
    LaunchedEffect(lifecycle, cityName, clock) {
        lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
            while (true) {
                now = clock.instant()
                delay(15_000)
            }
        }
    }
    return now
}

@Composable
private fun WeatherStatus(cityName: String, error: String?, onBack: () -> Unit, onRetry: () -> Unit) {
    SystemBarAppearance(darkIcons = true)
    Surface(Modifier.fillMaxSize()) {
        Box(Modifier.fillMaxSize()) {
            AnimatedWeatherBackground(WeatherKind.CLOUDY, true, Modifier.matchParentSize())
            Column(Modifier.fillMaxSize().safeDrawingPadding().padding(24.dp)) {
                TextButton(onClick = onBack) { Text("← Şehirler", color = weatherAppearance(WeatherKind.CLOUDY, SolarPhase.DAY).ink) }
                Column(Modifier.weight(1f).fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center) {
                    WeatherMotion(WeatherKind.CLOUDY, modifier = Modifier.size(120.dp))
                    Text(if (error == null) "$cityName için hava hazırlanıyor…" else "Gökyüzüne ulaşamadık",
                        color = weatherAppearance(WeatherKind.CLOUDY, SolarPhase.DAY).ink,
                        style = MaterialTheme.typography.titleMedium)
                    Spacer(Modifier.height(16.dp))
                    if (error == null) LinearProgressIndicator(Modifier.width(100.dp))
                    else {
                        Text(error, color = weatherAppearance(WeatherKind.CLOUDY, SolarPhase.DAY).ink)
                        Spacer(Modifier.height(16.dp))
                        Button(onClick = onRetry) { Text("Tekrar dene") }
                    }
                }
            }
        }
    }
}
