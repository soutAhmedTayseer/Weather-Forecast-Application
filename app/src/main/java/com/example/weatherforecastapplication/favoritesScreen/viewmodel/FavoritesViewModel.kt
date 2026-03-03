package com.example.weatherforecastapplication.favoritesscreen.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.weatherforecastapplication.data.models.CityLocation
import com.example.weatherforecastapplication.repository.WeatherRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class FavoritesViewModel(
    private val repository: WeatherRepository
) : ViewModel() {

    // Automatically listens to Room DB changes
    val favoritesList: StateFlow<List<CityLocation>> = repository.getFavoriteLocations()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun deleteLocation(location: CityLocation) {
        viewModelScope.launch {
            repository.deleteFavoriteLocation(location)
        }
    }

    fun saveLocation(lat: Double, lon: Double, cityName: String) {
        viewModelScope.launch {
            val newLocation = CityLocation(lat = lat, lon = lon, cityName = cityName)
            repository.insertFavoriteLocation(newLocation)
        }
    }
}