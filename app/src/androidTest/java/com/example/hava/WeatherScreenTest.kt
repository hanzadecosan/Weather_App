package com.example.hava

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.platform.app.InstrumentationRegistry
import com.example.hava.model.*
import com.example.hava.ui.city.CityScreen
import com.example.hava.ui.theme.HavaTheme
import com.example.hava.ui.weather.*
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import java.time.Clock
import java.time.Instant
import java.time.ZoneOffset
import java.io.File
import android.graphics.Bitmap
import androidx.compose.ui.graphics.asAndroidBitmap

class WeatherScreenTest {
    @get:Rule val compose = createComposeRule()
    private val instant = Instant.parse("2026-10-25T14:00:00Z")
    private fun weather() = WeatherResponse(CurrentWeather(22.0, 21.0, 60, 0, 8.0, 1),
        HourlyWeather(listOf("2026-10-25T17:00", "2026-10-25T18:00", "2026-10-25T19:00"),
            listOf(22.0, 21.0, 20.0), listOf(0, 0, 0), listOf(1, 1, 0)),
        DailyWeather(listOf(25.0), listOf("2026-10-25T07:20"), listOf("2026-10-25T18:12"), listOf("2026-10-25")),
        "Europe/Istanbul")

    @Test fun sunsetInvitationAndActionsAreVisible() {
        var back = false
        var retry = false
        compose.setContent {
            HavaTheme {
                WeatherDetailScreen("İstanbul", WeatherUiState(weather = weather(), cityName = "İstanbul",
                    updatedAt = instant.toEpochMilli()), onBack = { back = true }, onRetry = { retry = true },
                    clock = Clock.fixed(instant, ZoneOffset.UTC))
            }
        }
        compose.onNodeWithText("ALTIN SAAT").assertIsDisplayed()
        compose.onNodeWithText("Bugün saat 18.12: gün batımını izle.").assertIsDisplayed()
        capture("sunset")
        compose.onNodeWithText("Yenile ↻").performClick()
        compose.runOnIdle { assertTrue(retry) }
        compose.onNodeWithText("← Şehirler").performClick()
        compose.runOnIdle { assertTrue(back) }
    }

    @Test fun darkCityScreenHasExplicitEmptyState() {
        compose.setContent {
            HavaTheme(darkTheme = true) {
                CityScreen(emptyList(), "olmayan", {}, {})
            }
        }
        compose.onNodeWithText("Şehir bulunamadı").assertIsDisplayed()
        compose.onNodeWithText("Şehir ara").assertExists()
        capture("city-dark")
    }

    @Test fun oldCityWeatherIsNeverRenderedUnderNewName() {
        compose.setContent {
            HavaTheme {
                WeatherDetailScreen("İzmir", WeatherUiState(weather = weather(), cityName = "Ankara"), {}, {},
                    clock = Clock.fixed(instant, ZoneOffset.UTC))
            }
        }
        compose.onNodeWithText("İzmir için hava hazırlanıyor…").assertIsDisplayed()
        compose.onNodeWithText("22°").assertDoesNotExist()
    }

    @Test fun sunriseHasItsOwnInvitation() {
        showScene(Instant.parse("2026-10-25T03:30:00Z"))
        compose.onNodeWithText("GÜNÜN İLK IŞIĞI").assertIsDisplayed()
        compose.onNodeWithText("Bugün saat 07.20: gün doğumunu izle.").assertIsDisplayed()
        capture("sunrise")
    }

    @Test fun nightUsesClearSkyDescription() {
        showScene(Instant.parse("2026-10-25T19:00:00Z"))
        compose.onNodeWithText("Açık bir gökyüzü").assertIsDisplayed()
        capture("night")
    }

    @Test fun cloudyDayRetainsConditionAndMetrics() {
        showScene(Instant.parse("2026-10-25T10:00:00Z"), code = 3)
        compose.onNodeWithText("Bulutlu").assertIsDisplayed()
        compose.onNodeWithText("%60").assertIsDisplayed()
        capture("cloudy")
    }

    @Test fun cityShortcutsSelectRealCityNames() {
        var selected: String? = null
        compose.setContent {
            HavaTheme(darkTheme = false) {
                CityScreen(com.example.hava.data.CityCatalog.cities, "", {}, { selected = it })
            }
        }
        compose.onNodeWithText("Adana").assertIsDisplayed()
        capture("city-light")
        compose.onNodeWithText("İstanbul ↗").performClick()
        compose.runOnIdle { assertEquals("İstanbul", selected) }
    }

    private fun showScene(time: Instant, code: Int = 0) {
        compose.setContent {
            HavaTheme {
                WeatherDetailScreen("İstanbul", WeatherUiState(weather = weather().let {
                    it.copy(current = it.current.copy(weatherCode = code))
                }, cityName = "İstanbul", updatedAt = time.toEpochMilli()), {}, {},
                    clock = Clock.fixed(time, ZoneOffset.UTC))
            }
        }
    }

    private fun capture(name: String) {
        compose.waitForIdle()
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        if (android.os.Build.VERSION.SDK_INT < 26) return
        val bitmap = compose.onRoot().captureToImage().asAndroidBitmap()
        val directory = File(instrumentation.targetContext.getExternalFilesDir(null), "screenshots")
        directory.mkdirs()
        File(directory, "$name.png").outputStream().use { bitmap.compress(Bitmap.CompressFormat.PNG, 100, it) }
    }
}
