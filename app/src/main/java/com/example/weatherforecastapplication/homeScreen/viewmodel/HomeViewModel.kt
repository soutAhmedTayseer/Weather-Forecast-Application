package com.example.weatherforecastapplication.homescreen.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.weatherforecastapplication.data.models.ForecastResponseApi
import com.example.weatherforecastapplication.data.models.ResponseState
import com.example.weatherforecastapplication.repository.WeatherRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class HomeViewModel(
    private val repository: WeatherRepository
) : ViewModel() {

    private val _weatherState = MutableStateFlow<ResponseState<ForecastResponseApi>>(ResponseState.Loading)
    val weatherState: StateFlow<ResponseState<ForecastResponseApi>> = _weatherState.asStateFlow()

    init {
        // Fetch default location (Alexandria) on startup
        getWeatherData(31.2001, 29.9187, "metric", "en")
    }

    fun getWeatherData(lat: Double, lon: Double, units: String, lang: String) {
        viewModelScope.launch {
            repository.getFiveDayForecast(lat, lon, units, lang).collect { state ->
                _weatherState.value = state
            }
        }
    }
}