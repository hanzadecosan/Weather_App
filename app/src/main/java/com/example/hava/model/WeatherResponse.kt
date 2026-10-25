
package com.example.hava.model

import com.google.gson.annotations.SerializedName

data class WeatherResponse(
    val current: CurrentWeather,
    val hourly: HourlyWeather,
    val daily: DailyWeather,
    val timezone: String = "Europe/Istanbul"
)

data class CurrentWeather(
    @SerializedName("temperature_2m")
    val temperature: Double?,

    @SerializedName("apparent_temperature")
    val feelsLike: Double?,

    @SerializedName("relative_humidity_2m")
    val humidity: Int?,

    @SerializedName("weather_code")
    val weatherCode: Int,

    @SerializedName("wind_speed_10m")
    val windSpeed: Double?,

    @SerializedName("is_day")
    val isDay: Int
)

data class HourlyWeather(
    val time: List<String>,

    @SerializedName("temperature_2m")
    val temperatures: List<Double?>,

    @SerializedName("weather_code")
    val weatherCodes: List<Int?>,
    @SerializedName("is_day")
    val isDay: List<Int?> = emptyList()
)

data class DailyWeather(
    @SerializedName("temperature_2m_max")
    val maxTemperatures: List<Double?>,

    val sunrise: List<String?>,
    val sunset: List<String?>,
    val time: List<String> = emptyList()
)
