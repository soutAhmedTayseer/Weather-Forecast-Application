package com.example.weatherforecastapplication.favoritesscreen.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.weatherforecastapplication.data.models.CityLocation
import com.example.weatherforecastapplication.data.models.ResponseState
import com.example.weatherforecastapplication.repository.SettingsRepository
import com.example.weatherforecastapplication.repository.WeatherRepository
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.supervisorScope

data class FavoriteWeatherUiState(
    val location: CityLocation,
    val isLoading: Boolean = true,
    val temp: String = "--",
    val icon: String = "01d",
    val description: String = ""
)

class FavoritesViewModel(
    private val repository: WeatherRepository,
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    private val _favoritesWeather = MutableStateFlow<List<FavoriteWeatherUiState>>(emptyList())
    val favoritesWeather: StateFlow<List<FavoriteWeatherUiState>> = _favoritesWeather.asStateFlow()

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()
    private val refreshTrigger = MutableStateFlow(0)

    val tempUnitFlow = settingsRepository.tempUnitFlow.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "metric")

    init {
        viewModelScope.launch {
            combine(
                repository.getFavoriteLocations(),
                settingsRepository.tempUnitFlow,
                settingsRepository.languageFlow,
                refreshTrigger
            ) { locations, unit, lang, _ ->

                _favoritesWeather.value = locations.map { loc ->
                    val existing = _favoritesWeather.value.find { it.location.lat == loc.lat && it.location.lon == loc.lon }
                    existing?.copy(isLoading = true) ?: FavoriteWeatherUiState(location = loc)
                }

                supervisorScope {
                    val jobs = locations.map { loc ->
                        async {
                            try {
                                val state = repository.getFiveDayForecast(loc.lat, loc.lon, unit, lang)
                                    .first { it !is ResponseState.Loading }

                                if (state is ResponseState.Success) {
                                    val first = state.data.list.firstOrNull()
                                    val temp = first?.main?.temp?.toInt()?.toString() ?: "--"
                                    val icon = first?.weather?.firstOrNull()?.icon ?: "01d"
                                    val desc = first?.weather?.firstOrNull()?.description ?: "Unknown"

                                    _favoritesWeather.update { currentList ->
                                        currentList.map { item ->
                                            if (item.location.lat == loc.lat && item.location.lon == loc.lon) {
                                                item.copy(isLoading = false, temp = temp, icon = icon, description = desc)
                                            } else item
                                        }
                                    }
                                } else {
                                    throw Exception("API Error")
                                }
                            } catch (e: Exception) {
                                _favoritesWeather.update { currentList ->
                                    currentList.map { item ->
                                        if (item.location.lat == loc.lat && item.location.lon == loc.lon) {
                                            item.copy(isLoading = false, description = "Error! Swipe to retry.")
                                        } else item
                                    }
                                }
                            }
                        }
                    }
                    jobs.awaitAll()
                }
                _isRefreshing.value = false
            }.collect {}
        }
    }

    fun refreshFavorites() {
        _isRefreshing.value = true
        refreshTrigger.value += 1
    }

    fun deleteLocation(location: CityLocation) {
        viewModelScope.launch { repository.deleteFavoriteLocation(location) }
    }

    fun saveLocation(lat: Double, lon: Double, cityName: String) {
        viewModelScope.launch {
            repository.insertFavoriteLocation(CityLocation(lat = lat, lon = lon, cityName = cityName))
        }
    }
}