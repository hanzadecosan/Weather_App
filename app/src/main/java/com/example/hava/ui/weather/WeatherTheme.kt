
package com.example.hava.ui.weather

import androidx.compose.ui.graphics.Color

enum class WeatherKind {
    SUNNY, CLOUDY, FOGGY, RAINY, SNOWY, STORMY
}

fun weatherKind(code: Int): WeatherKind {
    return when (code) {
        0, 1 -> WeatherKind.SUNNY
        2, 3 -> WeatherKind.CLOUDY
        45, 48 -> WeatherKind.FOGGY
        51, 53, 55, 56, 57,
        61, 63, 65, 66, 67,
        80, 81, 82 -> WeatherKind.RAINY
        71, 73, 75, 77, 85, 86 -> WeatherKind.SNOWY
        95, 96, 99 -> WeatherKind.STORMY
        else -> WeatherKind.CLOUDY
    }
}

fun weatherDescription(kind: WeatherKind): String =
    when (kind) {
        WeatherKind.SUNNY -> "Güneşli"
        WeatherKind.CLOUDY -> "Bulutlu"
        WeatherKind.FOGGY -> "Sisli"
        WeatherKind.RAINY -> "Yağmurlu"
        WeatherKind.SNOWY -> "Karlı"
        WeatherKind.STORMY -> "Fırtınalı"
    }

fun weatherSymbol(kind: WeatherKind): String =
    when (kind) {
        WeatherKind.SUNNY -> "☀"
        WeatherKind.CLOUDY -> "☁"
        WeatherKind.FOGGY -> "≋"
        WeatherKind.RAINY -> "☂"
        WeatherKind.SNOWY -> "❄"
        WeatherKind.STORMY -> "ϟ"
    }

fun weatherColors(kind: WeatherKind): List<Color> =
    when (kind) {
        WeatherKind.SUNNY -> listOf(
            Color(0xFFFFF6E6),
            Color(0xFFFFD992),
            Color(0xFFFFF8EC)
        )
        WeatherKind.CLOUDY, WeatherKind.FOGGY -> listOf(
            Color(0xFFE5EFF7),
            Color(0xFFB9D0E4),
            Color(0xFFF7FAFC)
        )
        WeatherKind.RAINY -> listOf(
            Color(0xFF6289B2),
            Color(0xFF91B3D0),
            Color(0xFFD9E8F3)
        )
        WeatherKind.SNOWY -> listOf(
            Color(0xFFE4E9FF),
            Color(0xFFC8D9F5),
            Color(0xFFF9FBFF)
        )
        WeatherKind.STORMY -> listOf(
            Color(0xFF536D94),
            Color(0xFF819BC0),
            Color(0xFFD8E2F1)
        )
    }

fun weatherAdvice(kind: WeatherKind): String =
    when (kind) {
        WeatherKind.SUNNY ->
            "Açık bir gökyüzü seni bekliyor. Favori tişörtünü ve güneş gözlüğünü unutma! Sahilde bir yürüyüş de enerjini yükseltecektir!"
        WeatherKind.CLOUDY ->
            "Bulutlu bir gün. Yanına hafif bir ceket almak iyi olabilir, veya evde kalıp güzel bir kitap okumak da..."
        WeatherKind.FOGGY ->
            "Sis görüş mesafesini azaltabilir. Yolculukta dikkatli ol."
        WeatherKind.RAINY ->
            "Yağmur bekleniyor. Şemsiyeni yanına almayı unutma."
        WeatherKind.SNOWY ->
            "Karlı bir gün. Kalın giyin ve kaygan zeminlere dikkat et."
        WeatherKind.STORMY ->
            "Fırtınalı hava bekleniyor. Dışarıda dikkatli ol."
    }
