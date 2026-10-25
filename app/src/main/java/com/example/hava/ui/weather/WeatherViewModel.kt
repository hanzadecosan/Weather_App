package com.example.hava.ui.weather

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.hava.data.CityRepository
import com.example.hava.data.WeatherRepository
import com.example.hava.model.City
import com.example.hava.model.WeatherResponse
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.io.IOException
import java.text.Normalizer
import java.util.Locale

data class WeatherUiState(
    val isLoading: Boolean = false,
    val weather: WeatherResponse? = null,
    val error: String? = null,
    val cityName: String? = null,
    val updatedAt: Long = 0
)

internal fun normalizedCity(value: String): String = Normalizer.normalize(
    value.trim().lowercase(Locale.forLanguageTag("tr-TR")).replace('ı', 'i'),
    Normalizer.Form.NFD
).replace("\\p{M}+".toRegex(), "")

internal fun selectCity(cities: List<City>, name: String): City? {
    val matches = cities.filter {
        it.countryCode.equals("TR", true) && normalizedCity(it.name) == normalizedCity(name)
    }
    // Never silently substitute a different city or choose between ambiguous matches.
    return matches.singleOrNull { it.featureCode == "PPLA" || it.featureCode == "PPLC" }
        ?: matches.singleOrNull() ?: matches.singleOrNull {
        it.region?.let(::normalizedCity) == normalizedCity(name)
    }
}

class WeatherViewModel(
    private val cityRepository: CityRepository = CityRepository(),
    private val weatherRepository: WeatherRepository = WeatherRepository()
) : ViewModel() {
    private val _uiState = MutableStateFlow(WeatherUiState())
    val uiState = _uiState.asStateFlow()
    private var request: Job? = null
    private var generation = 0L
    private val coordinates = mutableMapOf<String, City>()

    fun loadWeather(cityName: String, force: Boolean = true) {
        val previous = _uiState.value
        if (!force && previous.cityName == cityName &&
            (previous.isLoading || (previous.weather != null && System.currentTimeMillis() - previous.updatedAt < 600_000))) return
        request?.cancel()
        val token = ++generation
        _uiState.value = if (previous.cityName == cityName && previous.weather != null)
            previous.copy(isLoading = true, error = null)
        else WeatherUiState(isLoading = true, cityName = cityName)
        request = viewModelScope.launch {
            try {
                // These administrative province names use different city-center names.
                val lookup = when (cityName) { "Hatay" -> "Antakya"; "Kocaeli" -> "İzmit"; "Sakarya" -> "Adapazarı"; else -> cityName }
                val city = coordinates[cityName] ?: selectCity(cityRepository.searchCity(lookup), lookup)
                if (city == null) {
                    if (token == generation) _uiState.value = WeatherUiState(cityName = cityName,
                        error = "Bu il için kesin bir konum bulunamadı. Lütfen tekrar dene.")
                    return@launch
                }
                coordinates[cityName] = city
                val weather = weatherRepository.getWeather(city.latitude, city.longitude)
                ensureActive()
                checkNotNull(weather.current)
                checkNotNull(weather.hourly)
                checkNotNull(weather.daily)
                require(weather.current.temperature?.isFinite() != false)
                if (token == generation) _uiState.value = WeatherUiState(weather = weather,
                    cityName = cityName, updatedAt = System.currentTimeMillis())
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                val message = when (e) {
                    is HttpException -> if (e.code() == 429) "Çok fazla istek gönderildi. Biraz sonra tekrar dene." else "Hava durumu servisine şu an ulaşılamıyor."
                    is IOException -> "Bağlantı kurulamadı. İnternetini kontrol edip tekrar dene."
                    else -> "Hava durumu verisi okunamadı. Lütfen tekrar dene."
                }
                if (token == generation) _uiState.value = _uiState.value.copy(isLoading = false, error = message)
            }
        }
    }
}
