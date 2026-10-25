package com.example.hava

import com.example.hava.model.*
import com.example.hava.data.*
import com.example.hava.ui.weather.*
import kotlinx.coroutines.*
import kotlinx.coroutines.test.*
import kotlinx.coroutines.flow.first
import org.junit.Assert.*
import org.junit.Test
import java.time.Instant
import java.time.LocalDateTime

@OptIn(ExperimentalCoroutinesApi::class)
class WeatherBehaviorTest {
    private fun sample(temperature: Double = 20.0) = WeatherResponse(
        CurrentWeather(temperature, 19.0, 60, 0, 10.0, 1),
        HourlyWeather((0..47).map { LocalDateTime.parse("2026-10-25T00:00").plusHours(it.toLong()).toString() },
            List(48) { 20.0 }, List(48) { 0 }),
        DailyWeather(listOf(24.0, 25.0), listOf("2026-10-25T07:20", "2026-10-26T07:21"),
            listOf("2026-10-25T18:12", "2026-10-26T18:11"), listOf("2026-10-25", "2026-10-26")),
        "Europe/Istanbul"
    )
    @Test fun sunsetUsesCityTimeAndActualEvent() {
        val timeline = weatherTimeline(sample(), Instant.parse("2026-10-25T14:00:00Z"))
        assertEquals(SolarPhase.SUNSET, timeline.phase)
        assertEquals(17, timeline.now.hour)
        assertTrue(solarMessage(timeline).contains("18.12"))
        assertEquals((17..28).toList(), timeline.hourlyIndices)
    }
    @Test fun sunriseAndTwilightBoundaries() {
        assertEquals(SolarPhase.NIGHT, weatherTimeline(sample(), Instant.parse("2026-10-25T02:19:00Z")).phase)
        assertEquals(SolarPhase.DAWN, weatherTimeline(sample(), Instant.parse("2026-10-25T02:20:00Z")).phase)
        assertEquals(SolarPhase.DAY, weatherTimeline(sample(), Instant.parse("2026-10-25T04:40:00Z")).phase)
        assertEquals(SolarPhase.NIGHT, weatherTimeline(sample(), Instant.parse("2026-10-25T15:32:00Z")).phase)
    }
    @Test fun midnightSelectsNewDayAndRemainingHours() {
        val timeline = weatherTimeline(sample(), Instant.parse("2026-10-25T21:05:00Z"))
        assertEquals(1, timeline.dailyIndex)
        assertEquals(24, timeline.hourlyIndices.first())
        assertTrue(solarMessage(timeline).contains("07.21"))
    }
    @Test fun unequalArraysAndMissingSolarDataAreSafe() {
        val weather = sample().copy(hourly = HourlyWeather(listOf("invalid", "2026-10-25T17:00"),
            listOf(null), emptyList()), daily = DailyWeather(emptyList(), listOf(null), emptyList()))
        val timeline = weatherTimeline(weather, Instant.parse("2026-10-25T14:00:00Z"))
        assertTrue(timeline.hourlyIndices.isEmpty())
        assertNull(timeline.nextEvent)
        assertEquals(-1, timeline.dailyIndex)
    }
    @Test fun matchingNeverFallsBackToUnrelatedOrAmbiguousCity() {
        val ankara = City(1, "Ankara", 39.9, 32.8, countryCode = "TR")
        assertNull(selectCity(listOf(ankara), "İzmir"))
        assertNull(selectCity(listOf(ankara, ankara.copy(id = 2)), "Ankara"))
        assertEquals("izmir", normalizedCity(" İZMİR "))
        assertEquals("sanliurfa", normalizedCity("Şanlıurfa"))
        assertEquals(ankara, selectCity(listOf(ankara), "ankara"))
        val capital = ankara.copy(featureCode = "PPLC")
        assertEquals(capital, selectCity(listOf(ankara.copy(id = 2, region = "Başka il"), capital), "Ankara"))
    }
    @Test fun obsoleteRequestCannotOverwriteNewCity() = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        try {
            val cities = object : CityRepository() {
                override suspend fun searchCity(name: String) = listOf(City(if (name == "Ankara") 1 else 2,
                    name, if (name == "Ankara") 1.0 else 2.0, 30.0, countryCode = "TR"))
            }
            val weather = object : WeatherRepository() {
                override suspend fun getWeather(latitude: Double, longitude: Double): WeatherResponse {
                    // Simulate an upstream operation that does not cooperate with cancellation.
                    withContext(NonCancellable) { delay(if (latitude == 1.0) 1000 else 100) }
                    return sample(latitude)
                }
            }
            val model = WeatherViewModel(cities, weather)
            model.loadWeather("Ankara")
            runCurrent()
            model.loadWeather("İzmir")
            advanceUntilIdle()
            assertEquals("İzmir", model.uiState.value.cityName)
            assertEquals(2.0, model.uiState.value.weather!!.current.temperature!!, 0.0)
            assertNull(model.uiState.value.error)
        } finally { Dispatchers.resetMain() }
    }
    @Test fun serverFailureLeavesRetryableError() = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        try {
            val cities = object : CityRepository() {
                override suspend fun searchCity(name: String): List<City> = throw java.io.IOException("offline")
            }
            val model = WeatherViewModel(cities)
            model.loadWeather("Ankara")
            advanceUntilIdle()
            assertFalse(model.uiState.value.isLoading)
            assertNotNull(model.uiState.value.error)
            assertNull(model.uiState.value.weather)
        } finally { Dispatchers.resetMain() }
    }

    @Test fun failedRefreshKeepsLastGoodMeasurements() = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        try {
            val cities = object : CityRepository() {
                override suspend fun searchCity(name: String) = listOf(City(1, name, 39.0, 32.0, countryCode = "TR"))
            }
            var calls = 0
            val weather = object : WeatherRepository() {
                override suspend fun getWeather(latitude: Double, longitude: Double): WeatherResponse {
                    if (++calls > 1) throw java.io.IOException("offline")
                    return sample()
                }
            }
            val model = WeatherViewModel(cities, weather)
            model.loadWeather("Ankara")
            advanceUntilIdle()
            model.loadWeather("Ankara")
            assertNotNull(model.uiState.value.weather)
            assertTrue(model.uiState.value.isLoading)
            advanceUntilIdle()
            assertFalse(model.uiState.value.isLoading)
            assertNotNull(model.uiState.value.error)
            assertEquals(20.0, model.uiState.value.weather!!.current.temperature!!, 0.0)
        } finally { Dispatchers.resetMain() }
    }

    @Test fun searchRestoresAndAcceptsUnaccentedTurkishInput() = runTest {
        val saved = androidx.lifecycle.SavedStateHandle(mapOf("search" to "sanliurfa"))
        val model = com.example.hava.ui.city.CityViewModel(saved)
        assertEquals("sanliurfa", model.searchQuery.value)
        assertEquals(listOf("Şanlıurfa"), model.filteredCities.first())
    }
}
