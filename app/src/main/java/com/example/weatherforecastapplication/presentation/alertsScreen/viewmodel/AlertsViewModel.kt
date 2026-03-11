package com.example.weatherforecastapplication.presentation.alertsScreen.viewmodel

import android.content.Context
import android.location.Geocoder
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.weatherforecastapplication.core.worker.AlarmScheduler
import com.example.weatherforecastapplication.data.models.stateManagement.ResponseState
import com.example.weatherforecastapplication.data.models.entities.WeatherAlert
import com.example.weatherforecastapplication.data.repository.SettingsRepository
import com.example.weatherforecastapplication.domain.repository.WeatherRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.supervisorScope
import java.util.Locale

class AlertsViewModel(
    private val repository: WeatherRepository,
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    val alertsList: StateFlow<List<WeatherAlert>> = repository.getAlerts()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val locationFlow = combine(
        settingsRepository.locationMethodFlow,
        settingsRepository.homeLatFlow,
        settingsRepository.homeLonFlow,
        settingsRepository.gpsLatFlow,
        settingsRepository.gpsLonFlow
    ) { method, homeLat, homeLon, gpsLat, gpsLon ->
        if (method == "map") Pair(homeLat, homeLon) else Pair(gpsLat, gpsLon)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), Pair(0.0, 0.0))

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    private val refreshTrigger = MutableStateFlow(0)

    init {
        // Automatically fetch and update DB the second a unit or language setting changes!
        viewModelScope.launch {
            combine(
                repository.getAlerts(),
                settingsRepository.tempUnitFlow,
                settingsRepository.languageFlow,
                refreshTrigger
            ) { alerts, unit, lang, _ ->
                Triple(alerts, unit, lang)
            }.collectLatest { (currentAlerts, currentUnit, currentLang) ->

                if (currentAlerts.isEmpty()) {
                    _isRefreshing.value = false
                    return@collectLatest
                }

                // Dynamically resolve the correct symbol based on the live DataStore unit
                val unitSymbol = when (currentUnit) {
                    "imperial" -> "°F"
                    "standard" -> "K"
                    else -> "°C"
                }

                supervisorScope {
                    currentAlerts.forEach { alert ->
                        launch {
                            // Collect continuous stream to bypass cache limit
                            repository.getFiveDayForecast(alert.lat, alert.lon, currentUnit, currentLang).collect { state ->
                                if (state is ResponseState.Success) {
                                    val firstForecast = state.data.list.firstOrNull()
                                    if (firstForecast != null) {
                                        val temp = firstForecast.main.temp
                                        val desc = firstForecast.weather.firstOrNull()?.description?.replaceFirstChar { it.uppercase() } ?: "Unknown"
                                        val icon = firstForecast.weather.firstOrNull()?.icon ?: "01d"
                                        val translatedCityName = state.data.city.name

                                        // Appends the accurate dynamic unit symbol
                                        val newDesc = "$desc, ${temp.toInt()}$unitSymbol"

                                        // Prevents infinite loop: Only update DB if the data actually changed
                                        if (alert.cityName != translatedCityName || alert.currentDescription != newDesc || alert.currentIcon != icon) {
                                            repository.insertAlert(alert.copy(
                                                cityName = translatedCityName,
                                                currentIcon = icon,
                                                currentDescription = newDesc
                                            ))
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Safely end the loading spinner
        viewModelScope.launch {
            refreshTrigger.collect {
                delay(1500)
                _isRefreshing.value = false
            }
        }
    }

    fun refreshAlerts(isManualRefresh: Boolean = true) {
        if (isManualRefresh) {
            _isRefreshing.value = true
            refreshTrigger.value += 1
        }
    }

    fun saveAlert(context: Context, alert: WeatherAlert) {
        viewModelScope.launch {
            val currentLang = settingsRepository.languageFlow.first()
            val locale = Locale(currentLang)

            val name = try {
                val geocoder = Geocoder(context, locale)
                @Suppress("DEPRECATION")
                val addresses = geocoder.getFromLocation(alert.lat, alert.lon, 1)
                if (!addresses.isNullOrEmpty()) addresses[0].locality ?: addresses[0].subAdminArea ?: "Selected Location" else "Selected Location"
            } catch (e: Exception) { "Selected Location" }

            val newId = if (alert.id == 0) System.currentTimeMillis().toInt() else alert.id
            val finalAlert = alert.copy(id = newId, cityName = name)

            repository.insertAlert(finalAlert)
            refreshAlerts(isManualRefresh = false)

            val triggerTime = if (finalAlert.startTime <= System.currentTimeMillis()) System.currentTimeMillis() + 2000 else finalAlert.startTime
            AlarmScheduler.scheduleAlarm(context, finalAlert.copy(startTime = triggerTime))
        }
    }

    fun deleteAlert(alert: WeatherAlert) {
        viewModelScope.launch { repository.deleteAlert(alert) }
    }
}