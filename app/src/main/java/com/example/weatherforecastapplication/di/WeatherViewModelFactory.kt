package com.example.weatherforecastapplication.di

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.weatherforecastapplication.alertsScreen.viewmodel.AlertsViewModel
import com.example.weatherforecastapplication.homescreen.viewmodel.HomeViewModel
import com.example.weatherforecastapplication.repository.WeatherRepository
import com.example.weatherforecastapplication.favoritesscreen.viewmodel.FavoritesViewModel
import com.example.weatherforecastapplication.mapscreen.viewmodel.MapViewModel
import com.example.weatherforecastapplication.repository.SettingsRepository
import com.example.weatherforecastapplication.settingsScreen.SettingsViewModel

class WeatherViewModelFactory(
    private val repository: WeatherRepository,
    private val settingsRepository: SettingsRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(HomeViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return HomeViewModel(repository, settingsRepository) as T
        }
        if (modelClass.isAssignableFrom(FavoritesViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            // FIX: We now pass the settingsRepository so Favorites can fetch real-time weather!
            return FavoritesViewModel(repository, settingsRepository) as T
        }
        if (modelClass.isAssignableFrom(MapViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return MapViewModel(repository) as T
        }
        if (modelClass.isAssignableFrom(SettingsViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return SettingsViewModel(settingsRepository) as T
        }
        if (modelClass.isAssignableFrom(AlertsViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return AlertsViewModel(repository, settingsRepository) as T
        }

        throw IllegalArgumentException("Unknown ViewModel class")
    }
}