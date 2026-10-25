
package com.example.hava.data

import com.example.hava.model.WeatherResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface WeatherApi {

    @GET("v1/forecast")
    suspend fun getWeather(
        @Query("latitude") latitude: Double,
        @Query("longitude") longitude: Double,

        @Query("current")
        current: String =
            "temperature_2m,apparent_temperature," +
                    "relative_humidity_2m,weather_code," +
                    "wind_speed_10m,is_day",

        @Query("hourly")
        hourly: String =
            "temperature_2m,weather_code,is_day",

        @Query("daily")
        daily: String =
            "temperature_2m_max,sunrise,sunset",

        @Query("forecast_days")
        forecastDays: Int = 2,

        @Query("timezone")
        timezone: String = "auto"
    ): WeatherResponse
}
