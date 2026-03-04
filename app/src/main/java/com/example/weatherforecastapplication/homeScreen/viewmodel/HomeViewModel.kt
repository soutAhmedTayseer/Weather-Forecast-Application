package com.example.weatherforecastapplication.homescreen.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.weatherforecastapplication.data.models.ForecastResponseApi
import com.example.weatherforecastapplication.data.models.ResponseState
import com.example.weatherforecastapplication.repository.SettingsRepository
import com.example.weatherforecastapplication.repository.WeatherRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class HomeViewModel(
    private val repository: WeatherRepository,
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    private val _weatherState = MutableStateFlow<ResponseState<ForecastResponseApi>>(ResponseState.Loading)
    val weatherState: StateFlow<ResponseState<ForecastResponseApi>> = _weatherState.asStateFlow()

    // Expose flows to the UI
    val locationMethodFlow = settingsRepository.locationMethodFlow.stateIn(viewModelScope, SharingStarted.Eagerly, "gps")
    val tempUnitFlow = settingsRepository.tempUnitFlow.stateIn(viewModelScope, SharingStarted.Eagerly, "metric")
    val windUnitFlow = settingsRepository.windUnitFlow.stateIn(viewModelScope, SharingStarted.Eagerly, "m/s")

    // Allows the HomeScreen to push the device's real GPS into the DataStore
    fun updateGpsLocation(lat: Double, lon: Double) {
        viewModelScope.launch {
            settingsRepository.saveGpsLocation(lat, lon)
        }
    }

    fun fetchHomeWeatherAutomatically() {
        viewModelScope.launch {
            // 1. Figure out WHICH coordinates to use based on the user's setting
            val locationFlow = combine(
                settingsRepository.locationMethodFlow,
                settingsRepository.homeLatFlow,
                settingsRepository.homeLonFlow,
                settingsRepository.gpsLatFlow,
                settingsRepository.gpsLonFlow
            ) { method, homeLat, homeLon, gpsLat, gpsLon ->
                if (method == "map") Pair(homeLat, homeLon) else Pair(gpsLat, gpsLon)
            }

            // 2. Figure out WHICH units to use
            val configFlow = combine(
                settingsRepository.tempUnitFlow,
                settingsRepository.languageFlow
            ) { unit, lang -> Pair(unit, lang) }

            // 3. Combine them together and fetch!
            combine(locationFlow, configFlow) { loc, conf ->
                executeFetch(loc.first, loc.second, conf.first, conf.second)
            }.collect { state ->
                _weatherState.value = state
            }
        }
    }

    fun getWeatherData(lat: Double, lon: Double) {
        viewModelScope.launch {
            combine(
                settingsRepository.tempUnitFlow,
                settingsRepository.languageFlow
            ) { unit, lang ->
                executeFetch(lat, lon, unit, lang)
            }.collect { state ->
                _weatherState.value = state
            }
        }
    }

    private suspend fun executeFetch(lat: Double, lon: Double, unit: String, lang: String): ResponseState<ForecastResponseApi> {
        if (_weatherState.value !is ResponseState.Success) {
            _weatherState.value = ResponseState.Loading
        }
        var finalState: ResponseState<ForecastResponseApi> = ResponseState.Loading
        repository.getFiveDayForecast(lat, lon, unit, lang).collect { state ->
            finalState = state
        }
        return finalState
    }
}