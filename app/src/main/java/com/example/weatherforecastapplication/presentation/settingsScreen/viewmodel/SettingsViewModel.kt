package com.example.weatherforecastapplication.settingsScreen

import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.weatherforecastapplication.repository.SettingsRepository
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SettingsViewModel(
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    // --- SNACKBAR EVENT FLOW ---
    private val _snackbarEvent = MutableSharedFlow<String>()
    val snackbarEvent = _snackbarEvent.asSharedFlow()

    // ... your existing StateFlows ...
    val locationMethod: StateFlow<String> = settingsRepository.locationMethodFlow.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "gps")
    val tempUnit: StateFlow<String> = settingsRepository.tempUnitFlow.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "metric")
    val windUnit: StateFlow<String> = settingsRepository.windUnitFlow.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "m/s")
    val language: StateFlow<String> = settingsRepository.languageFlow.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "en")

    fun setLocationMethod(method: String) = viewModelScope.launch {
        settingsRepository.saveLocationMethod(method)
        if (method == "gps") {
            _snackbarEvent.emit("GPS Selected! Using real-time location.")
        }
        // If map, we will emit the snackbar AFTER they pick the location from the map screen
    }

    // Save custom Map location
    fun saveHomeLocationFromMap(lat: Double, lon: Double, cityName: String) = viewModelScope.launch {
        settingsRepository.saveHomeLocation(lat, lon)
        _snackbarEvent.emit("Home location set to $cityName!")
    }

    fun setTempUnit(unit: String, displayName: String) = viewModelScope.launch {
        settingsRepository.saveTempUnit(unit)
        _snackbarEvent.emit("Temperature unit changed to $displayName")
    }

    fun setWindUnit(unit: String) = viewModelScope.launch {
        settingsRepository.saveWindUnit(unit)
        _snackbarEvent.emit("Wind speed unit changed to $unit")
    }

    fun setLanguage(lang: String) = viewModelScope.launch {
        settingsRepository.saveLanguage(lang)
        // THIS CHANGES THE APP LANGUAGE DYNAMICALLY WITHOUT RESTARTING!
        AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags(lang))
        val langName = if (lang == "en") "English" else "Arabic"
        _snackbarEvent.emit("Language changed to $langName")
    }
}