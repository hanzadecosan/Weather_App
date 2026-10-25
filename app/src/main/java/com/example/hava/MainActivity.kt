
package com.example.hava

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.enableEdgeToEdge
import androidx.activity.compose.setContent
import androidx.activity.compose.BackHandler
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.hava.ui.city.CityScreen
import com.example.hava.ui.city.CityViewModel
import com.example.hava.ui.weather.WeatherDetailScreen
import com.example.hava.ui.weather.WeatherViewModel
import com.example.hava.ui.theme.HavaTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            HavaTheme {
                val cityViewModel: CityViewModel = viewModel()
                val weatherViewModel: WeatherViewModel = viewModel()

                val cities by cityViewModel.filteredCities
                    .collectAsStateWithLifecycle(
                        initialValue = emptyList()
                    )

                val searchQuery by cityViewModel.searchQuery
                    .collectAsStateWithLifecycle()

                val weatherState by weatherViewModel.uiState
                    .collectAsStateWithLifecycle()

                var selectedCity by rememberSaveable {
                    mutableStateOf<String?>(null)
                }

                BackHandler(enabled = selectedCity != null) {
                    selectedCity = null
                }

                LaunchedEffect(selectedCity) {
                    selectedCity?.let { city ->
                        weatherViewModel.loadWeather(city, force = false)
                    }
                }

                val city = selectedCity
                if (city == null) {
                    CityScreen(
                        cities = cities,
                        searchQuery = searchQuery,
                        onSearchChange = cityViewModel::updateSearchQuery,
                        onCityClick = { city ->
                            selectedCity = city
                        }
                    )
                } else {
                    WeatherDetailScreen(
                        cityName = city,
                        state = weatherState,
                        onBack = {
                            selectedCity = null
                        },
                        onRetry = {
                            weatherViewModel.loadWeather(
                                city
                            )
                        }
                    )
                }
            }
        }
    }
}
