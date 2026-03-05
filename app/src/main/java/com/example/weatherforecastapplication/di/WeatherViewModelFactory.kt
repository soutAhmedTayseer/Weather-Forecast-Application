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
            // 1. Pass the settingsRepository to the HomeViewModel too!
            return HomeViewModel(repository, settingsRepository) as T
        }
        if (modelClass.isAssignableFrom(FavoritesViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return FavoritesViewModel(repository) as T
        }
        if (modelClass.isAssignableFrom(MapViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return MapViewModel(repository) as T
        }
        // 2. ADD THIS MISSING BLOCK TO FIX THE CRASH!
        if (modelClass.isAssignableFrom(SettingsViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return SettingsViewModel(settingsRepository) as T
        }
        if (modelClass.isAssignableFrom(AlertsViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return AlertsViewModel(repository, settingsRepository) as T // Updated!
        }

        throw IllegalArgumentException("Unknown ViewModel class")
    }
}