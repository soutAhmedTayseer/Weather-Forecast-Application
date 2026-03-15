package com.example.weatherforecastapplication.presentation.settingsScreen.viewmodel

import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.weatherforecastapplication.R
import com.example.weatherforecastapplication.data.repository.SettingsRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class SnackbarEvent(val stringResId: Int, val arg: String = "")

class SettingsViewModel(
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    private val _snackbarEvent = MutableSharedFlow<SnackbarEvent>()
    val snackbarEvent = _snackbarEvent.asSharedFlow()

    private val _isTranslating = MutableStateFlow(false)
    val isTranslating = _isTranslating.asStateFlow()

    // Expose DataStore values directly as StateFlows
    val locationMethod: StateFlow<String> = settingsRepository.locationMethodFlow.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "gps")
    val tempUnit: StateFlow<String> = settingsRepository.tempUnitFlow.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "metric")
    val windUnit: StateFlow<String> = settingsRepository.windUnitFlow.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "m/s")
    val language: StateFlow<String> = settingsRepository.languageFlow.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "en")

    fun setLocationMethod(method: String) = viewModelScope.launch {
        settingsRepository.saveLocationMethod(method)
        if (method == "gps") {
            _snackbarEvent.emit(SnackbarEvent(R.string.gps_selected))
        }
    }

    fun saveHomeLocationFromMap(lat: Double, lon: Double, cityName: String) = viewModelScope.launch {
        settingsRepository.saveHomeLocation(lat, lon)
        _snackbarEvent.emit(SnackbarEvent(R.string.home_location_set, cityName))
    }

    fun setTempUnit(unit: String, displayName: String) = viewModelScope.launch {
        settingsRepository.saveTempUnit(unit)
        _snackbarEvent.emit(SnackbarEvent(R.string.temp_unit_changed, displayName))
    }

    fun setWindUnit(unit: String, displayName: String) = viewModelScope.launch {
        settingsRepository.saveWindUnit(unit)
        _snackbarEvent.emit(SnackbarEvent(R.string.wind_unit_changed, displayName))
    }

    fun setLanguage(lang: String) = viewModelScope.launch {
        if (language.value != lang) {
            _isTranslating.value = true
            settingsRepository.saveLanguage(lang)
            delay(1000)
            AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags(lang))
            _isTranslating.value = false
        }
    }
}