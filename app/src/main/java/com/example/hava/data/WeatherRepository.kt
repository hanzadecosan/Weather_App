
package com.example.hava.data

import com.example.hava.model.WeatherResponse

open class WeatherRepository(
    private val api: WeatherApi = ApiClient.weatherApi
) {
    open suspend fun getWeather(
        latitude: Double,
        longitude: Double
    ): WeatherResponse {
        return api.getWeather(latitude, longitude).also { response ->
            // Gson may otherwise put null into Kotlin non-null properties on malformed responses.
            checkNotNull(response.current)
            checkNotNull(response.hourly).let {
                checkNotNull(it.time)
                checkNotNull(it.temperatures)
                checkNotNull(it.weatherCodes)
                checkNotNull(it.isDay)
            }
            checkNotNull(response.daily).let {
                checkNotNull(it.time)
                checkNotNull(it.sunrise)
                checkNotNull(it.sunset)
                checkNotNull(it.maxTemperatures)
            }
        }
    }
}
