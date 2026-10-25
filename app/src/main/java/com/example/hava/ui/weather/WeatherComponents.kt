package com.example.hava.ui.weather

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.painterResource
import androidx.compose.foundation.Image
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.hava.R
import com.example.hava.model.WeatherResponse
import java.time.Duration
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.math.roundToInt

internal val WeatherHourFormat: DateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm")
private enum class MetricSymbol { HIGH, HUMIDITY, WIND }
internal fun Double?.degrees(): String = this?.takeIf(Double::isFinite)?.let { "${it.roundToInt()}°" } ?: "—"

@Composable
internal fun WeatherHero(cityName: String, weather: WeatherResponse, timeline: WeatherTimeline,
    appearance: WeatherAppearance, kind: WeatherKind, isDay: Boolean) {
    Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
        Text(cityName, color = appearance.ink, fontSize = 28.sp, fontWeight = FontWeight.Medium,
            textAlign = TextAlign.Center)
        Text(timeline.now.format(DateTimeFormatter.ofPattern("d MMMM EEEE · HH:mm", Locale.forLanguageTag("tr-TR"))),
            color = appearance.muted, style = MaterialTheme.typography.bodySmall)
        Box(Modifier.fillMaxWidth().height(154.dp), contentAlignment = Alignment.Center) {
            Text(weather.current.temperature.degrees(), color = appearance.ink,
                fontSize = 124.sp, lineHeight = 142.sp, fontWeight = FontWeight.ExtraLight, letterSpacing = (-7).sp)
            WeatherMotion(kind, isDay = isDay,
                phase = timeline.phase.takeIf { kind == WeatherKind.SUNNY && (it == SolarPhase.DAWN || it == SolarPhase.SUNSET) },
                modifier = Modifier.align(Alignment.CenterEnd).size(64.dp))
        }
        Text(if (kind == WeatherKind.SUNNY && !isDay) "Açık bir gökyüzü" else weatherDescription(kind),
            color = appearance.ink, style = MaterialTheme.typography.titleMedium)
        Spacer(Modifier.height(5.dp))
        Text("Hissedilen ${weather.current.feelsLike.degrees()}", color = appearance.muted,
            style = MaterialTheme.typography.bodySmall)
    }
}

@Composable
internal fun GlassPanel(appearance: WeatherAppearance, modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit) {
    val shape = RoundedCornerShape(28.dp)
    Column(modifier.fillMaxWidth().background(appearance.glass, shape)
        .border(1.dp, appearance.border, shape).padding(20.dp), content = content)
}

@Composable
internal fun WeatherMetrics(weather: WeatherResponse, dailyIndex: Int, appearance: WeatherAppearance) {
    GlassPanel(appearance) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
            Metric("En yüksek", weather.daily.maxTemperatures.getOrNull(dailyIndex).degrees(), MetricSymbol.HIGH, appearance)
            Metric("Nem", weather.current.humidity?.let { "%$it" } ?: "—", MetricSymbol.HUMIDITY, appearance)
            Metric("Rüzgâr", weather.current.windSpeed?.takeIf(Double::isFinite)?.let { "${it.roundToInt()} km/sa" } ?: "—", MetricSymbol.WIND, appearance)
        }
    }
}

@Composable
private fun RowScope.Metric(label: String, value: String, symbol: MetricSymbol, appearance: WeatherAppearance) {
    Column(Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
        MetricGlyph(symbol, appearance.ink)
        Spacer(Modifier.height(8.dp))
        Text(value, color = appearance.ink, fontSize = 18.sp, fontWeight = FontWeight.Medium, textAlign = TextAlign.Center)
        Text(label, color = appearance.muted, fontSize = 11.sp)
    }
}

/** Small line icons keep metrics legible without mixing emoji font styles. */
@Composable
private fun MetricGlyph(symbol: MetricSymbol, color: Color) {
    Canvas(Modifier.size(23.dp)) {
        val w = size.width
        val h = size.height
        val stroke = Stroke(width = 1.5.dp.toPx(), cap = StrokeCap.Round)
        when (symbol) {
            MetricSymbol.HIGH -> {
                drawLine(color, Offset(w*.5f,h*.85f), Offset(w*.5f,h*.15f), stroke.width, StrokeCap.Round)
                drawLine(color, Offset(w*.2f,h*.45f), Offset(w*.5f,h*.15f), stroke.width, StrokeCap.Round)
                drawLine(color, Offset(w*.8f,h*.45f), Offset(w*.5f,h*.15f), stroke.width, StrokeCap.Round)
            }
            MetricSymbol.HUMIDITY -> {
                val drop = Path().apply {
                    moveTo(w*.5f,h*.08f)
                    cubicTo(w*.2f,h*.43f,w*.08f,h*.58f,w*.23f,h*.79f)
                    cubicTo(w*.4f,h*1.02f,w*.83f,h*.94f,w*.83f,h*.64f)
                    cubicTo(w*.83f,h*.47f,w*.66f,h*.28f,w*.5f,h*.08f)
                    close()
                }
                drawPath(drop,color,style=stroke)
                drawLine(color,Offset(w*.32f,h*.66f),Offset(w*.65f,h*.66f),stroke.width,StrokeCap.Round)
            }
            MetricSymbol.WIND -> repeat(3) { line ->
                val y = h * (.24f + line*.25f)
                drawLine(color,Offset(w*.08f,y),Offset(w*(.76f-line*.08f),y),stroke.width,StrokeCap.Round)
                drawArc(color,270f,180f,false,Offset(w*.58f,y-h*.18f),androidx.compose.ui.geometry.Size(w*.3f,h*.22f),style=stroke)
            }
        }
    }
}

@Composable
internal fun SolarLightCard(timeline: WeatherTimeline, kind: WeatherKind, appearance: WeatherAppearance) {
    GlassPanel(appearance) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(when (timeline.phase) {
                    SolarPhase.DAWN -> "GÜNÜN İLK IŞIĞI"
                    SolarPhase.SUNSET -> "ALTIN SAAT"
                    else -> "GÜN IŞIĞININ RİTMİ"
                }, color = appearance.muted, fontSize = 10.sp, letterSpacing = 1.5.sp)
                Spacer(Modifier.height(6.dp))
                Text(solarMessage(timeline), color = appearance.ink, fontSize = 17.sp,
                    lineHeight = 23.sp, fontWeight = FontWeight.Medium)
            }
            WeatherMotion(WeatherKind.SUNNY, phase = if (timeline.phase == SolarPhase.DAWN || timeline.nextEvent?.sunrise == true) SolarPhase.DAWN else SolarPhase.SUNSET,
                modifier = Modifier.padding(start = 12.dp).size(58.dp))
        }
        Spacer(Modifier.height(16.dp))
        DaylightTrack(timeline, appearance)
        Row(Modifier.fillMaxWidth().padding(top = 8.dp), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("Doğuş ${timeline.sunrise?.format(WeatherHourFormat) ?: "—"}", color = appearance.muted, fontSize = 11.sp)
            Text("Batış ${timeline.sunset?.format(WeatherHourFormat) ?: "—"}", color = appearance.muted, fontSize = 11.sp)
        }
        if (kind != WeatherKind.SUNNY) {
            Text("Görünürlük hava koşullarına bağlıdır.", modifier = Modifier.padding(top = 8.dp),
                color = appearance.muted, fontSize = 10.sp)
        }
    }
}

@Composable
private fun DaylightTrack(timeline: WeatherTimeline, appearance: WeatherAppearance) {
    val progress = if (timeline.sunrise != null && timeline.sunset != null) {
        val length = Duration.between(timeline.sunrise, timeline.sunset).seconds
        if (length > 0) (Duration.between(timeline.sunrise, timeline.now).seconds.toFloat() / length).coerceIn(0f,1f) else null
    } else null
    Canvas(Modifier.fillMaxWidth().height(10.dp)) {
        val y = size.height / 2
        drawLine(appearance.ink.copy(alpha = .12f), Offset(0f,y), Offset(size.width,y), 2.dp.toPx(), StrokeCap.Round)
        if (progress != null) {
            drawLine(appearance.ink.copy(alpha = .5f), Offset(0f,y), Offset(size.width*progress,y),2.dp.toPx(),StrokeCap.Round)
            drawCircle(appearance.ink, radius = 4.dp.toPx(),center=Offset(size.width*progress,y))
        }
    }
}

@Composable
internal fun HourlyForecast(weather: WeatherResponse, timeline: WeatherTimeline, appearance: WeatherAppearance) {
    Text("Saat saat", color = appearance.ink, style = MaterialTheme.typography.titleMedium)
    Text("Önümüzdeki 12 saat", color = appearance.muted, style = MaterialTheme.typography.bodySmall)
    Spacer(Modifier.height(14.dp))
    if (timeline.hourlyIndices.isEmpty()) Text("Saatlik tahmin şu an mevcut değil.", color = appearance.muted)
    Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        timeline.hourlyIndices.forEach { index ->
            val time = parseWeatherTime(weather.hourly.time[index]) ?: return@forEach
            Column(Modifier.width(67.dp).padding(vertical = 8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Text(weather.hourly.temperatures[index].degrees(), color = appearance.ink,
                    fontSize = 19.sp, fontWeight = FontWeight.Medium)
                WeatherStill(weather.hourly.weatherCodes[index]?.let(::weatherKind),
                    weather.hourly.isDay.getOrNull(index) == 1, Modifier.size(48.dp))
                Text(time.format(WeatherHourFormat), color = appearance.muted, fontSize = 12.sp)
                if (time.toLocalDate() != timeline.now.toLocalDate()) Text("Yarın", color = appearance.muted, fontSize = 9.sp)
            }
        }
    }
}

@Composable
private fun WeatherStill(kind: WeatherKind?, isDay: Boolean, modifier: Modifier) {
    val resource = when (kind) {
        WeatherKind.SUNNY -> if (isDay) R.drawable.motion_sun_still else R.drawable.motion_night_still
        WeatherKind.CLOUDY -> R.drawable.motion_cloud_still
        WeatherKind.RAINY -> R.drawable.motion_rain_still
        WeatherKind.SNOWY -> R.drawable.motion_snow_still
        WeatherKind.STORMY -> R.drawable.motion_storm_still
        WeatherKind.FOGGY -> R.drawable.motion_fog_still
        null -> null
    }
    if (resource != null) Image(painterResource(resource), null, modifier)
    else Box(modifier, contentAlignment = Alignment.Center) { Text("—") }
}
