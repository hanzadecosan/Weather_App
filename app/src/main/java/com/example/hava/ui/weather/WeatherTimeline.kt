package com.example.hava.ui.weather

import com.example.hava.model.WeatherResponse
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.Duration
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

enum class SolarPhase { DAWN, DAY, SUNSET, NIGHT }
data class SolarMoment(val time: LocalDateTime, val sunrise: Boolean)
data class WeatherTimeline(
    val now: LocalDateTime,
    val phase: SolarPhase,
    val nextEvent: SolarMoment?,
    val sunrise: LocalDateTime?,
    val sunset: LocalDateTime?,
    val hourlyIndices: List<Int>,
    val dailyIndex: Int
)

internal fun parseWeatherTime(value: String?): LocalDateTime? =
    value?.let { runCatching { LocalDateTime.parse(it) }.getOrNull() }

fun weatherTimeline(weather: WeatherResponse, instant: Instant): WeatherTimeline {
    val zone = runCatching { ZoneId.of(weather.timezone) }.getOrDefault(ZoneId.of("Europe/Istanbul"))
    val now = LocalDateTime.ofInstant(instant, zone)
    val rises = weather.daily.sunrise.mapNotNull(::parseWeatherTime)
    val sets = weather.daily.sunset.mapNotNull(::parseWeatherTime)
    val rise = rises.firstOrNull { it.toLocalDate() == now.toLocalDate() }
    val set = sets.firstOrNull { it.toLocalDate() == now.toLocalDate() }
    // A two-hour invitation window, followed by twenty minutes of twilight.
    fun near(time: LocalDateTime?) = time != null &&
        !now.isBefore(time.minusHours(2)) && now.isBefore(time.plusMinutes(20))
    val phase = when {
        near(rise) -> SolarPhase.DAWN
        near(set) -> SolarPhase.SUNSET
        rise != null && set != null -> if (now >= rise && now < set) SolarPhase.DAY else SolarPhase.NIGHT
        else -> if (weather.current.isDay == 1) SolarPhase.DAY else SolarPhase.NIGHT
    }
    val next = (rises.map { SolarMoment(it, true) } + sets.map { SolarMoment(it, false) })
        .filter { it.time >= now }.minByOrNull { it.time }
    val hourly = weather.hourly
    val count = minOf(hourly.time.size, hourly.temperatures.size, hourly.weatherCodes.size)
    val indices = (0 until count).filter { index ->
        parseWeatherTime(hourly.time[index])?.let { it >= now.truncatedTo(ChronoUnit.HOURS) } == true
    }.take(12)
    return WeatherTimeline(now, phase, next, rise, set, indices,
        weather.daily.time.indexOf(now.toLocalDate().toString()))
}

fun solarMessage(timeline: WeatherTimeline): String {
    val active = when (timeline.phase) {
        SolarPhase.DAWN -> timeline.sunrise?.let { SolarMoment(it, true) }
        SolarPhase.SUNSET -> timeline.sunset?.let { SolarMoment(it, false) }
        else -> null
    }
    val event = active ?: timeline.nextEvent ?: return "Gökyüzünün ritmini keşfet."
    val name = if (event.sunrise) "gün doğumu" else "gün batımı"
    val at = event.time.format(DateTimeFormatter.ofPattern("HH.mm"))
    val minutes = Duration.between(timeline.now, event.time).toMinutes()
    return when {
        event.time <= timeline.now -> if (event.sunrise) "Güneş doğuyor. Yeni güne merhaba." else "Güneş ufukta kayboluyor. Gökyüzünün renklerini izle."
        minutes < 1 -> "$name başlamak üzere. Gökyüzüne bak."
        event.time.toLocalDate() == timeline.now.toLocalDate() -> "Bugün saat $at: ${if (event.sunrise) "gün doğumunu " else "gün batımını"} kaçırmayın!"
        else -> "Yarın saat $at: ${if (event.sunrise) "gün doğumunu" else "gün batımını"} izle."
    }
}
