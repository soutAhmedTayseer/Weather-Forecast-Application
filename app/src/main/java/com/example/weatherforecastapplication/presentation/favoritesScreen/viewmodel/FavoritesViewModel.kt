package com.example.weatherforecastapplication.presentation.favoritesScreen.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.weatherforecastapplication.data.models.entities.CityLocation
import com.example.weatherforecastapplication.data.models.stateManagement.ResponseState
import com.example.weatherforecastapplication.data.repository.SettingsRepository
import com.example.weatherforecastapplication.domain.repository.WeatherRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.supervisorScope

data class FavoriteWeatherUiState(
    val location: CityLocation,
    val isLoading: Boolean = true,
    val temp: String = "--",
    val icon: String = "01d",
    val description: String = "",
    val translatedCityName: String = location.cityName
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

    val tempUnitFlow = settingsRepository.tempUnitFlow.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        "metric"
    )

    init {
        // SOLID: Master pipeline dynamically reacts to Database, Units, and Language changes
        viewModelScope.launch {
            combine(
                repository.getFavoriteLocations(),
                settingsRepository.tempUnitFlow,
                settingsRepository.languageFlow,
                refreshTrigger
            ) { locations, unit, lang, _ ->
                Triple(locations, unit, lang)
            }.collectLatest { (locations, unit, lang) ->

                // Set loading state safely
                _favoritesWeather.update { currentList ->
                    locations.map { loc ->
                        val existing =
                            currentList.find { it.location.lat == loc.lat && it.location.lon == loc.lon }
                        existing?.copy(isLoading = true) ?: FavoriteWeatherUiState(location = loc)
                    }
                }

                // Run concurrent streams for each location
                supervisorScope {
                    locations.forEach { loc ->
                        launch {
                            // FIX: Using collect instead of .first() allows us to bypass the cache and get the fresh remote data!
                            repository.getFiveDayForecast(loc.lat, loc.lon, unit, lang)
                                .collect { state ->
                                    if (state is ResponseState.Success) {
                                        val first = state.data.list.firstOrNull()
                                        val temp = first?.main?.temp?.toInt()?.toString() ?: "--"
                                        val icon = first?.weather?.firstOrNull()?.icon ?: "01d"
                                        val desc = first?.weather?.firstOrNull()?.description ?: "Unknown"
                                        val translatedName = state.data.city.name

                                        _favoritesWeather.update { currentList ->
                                            currentList.map { item ->
                                                if (item.location.lat == loc.lat && item.location.lon == loc.lon) {
                                                    item.copy(
                                                        isLoading = false,
                                                        temp = temp,
                                                        icon = icon,
                                                        description = desc,
                                                        translatedCityName = translatedName
                                                    )
                                                } else item
                                            }
                                        }
                                    } else if (state is ResponseState.Error) {
                                        _favoritesWeather.update { currentList ->
                                            currentList.map { item ->
                                                if (item.location.lat == loc.lat && item.location.lon == loc.lon && item.temp == "--") {
                                                    item.copy(
                                                        isLoading = false,
                                                        description = "Error! Swipe to retry."
                                                    )
                                                } else item.copy(isLoading = false)
                                            }
                                        }
                                    }
                                }
                        }
                    }
                }
            }
        }

        // Safely turn off the refresh spinner when everything finishes loading
        viewModelScope.launch {
            _favoritesWeather.collect { list ->
                if (list.isNotEmpty() && list.all { !it.isLoading }) {
                    _isRefreshing.value = false
                }
            }
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
            repository.insertFavoriteLocation(
                CityLocation(
                    lat = lat,
                    lon = lon,
                    cityName = cityName
                )
            )
        }
    }
}