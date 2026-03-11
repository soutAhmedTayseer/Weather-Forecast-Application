package com.example.weatherforecastapplication.di

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.weatherforecastapplication.presentation.alertsScreen.viewmodel.AlertsViewModel
import com.example.weatherforecastapplication.domain.repository.WeatherRepository
import com.example.weatherforecastapplication.presentation.favoritesScreen.viewmodel.FavoritesViewModel
import com.example.weatherforecastapplication.presentation.homeScreen.viewmodel.HomeViewModel
import com.example.weatherforecastapplication.presentation.mapScreen.viewmodel.MapViewModel
import com.example.weatherforecastapplication.data.repository.SettingsRepository
import com.example.weatherforecastapplication.presentation.settingsScreen.viewmodel.SettingsViewModel

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