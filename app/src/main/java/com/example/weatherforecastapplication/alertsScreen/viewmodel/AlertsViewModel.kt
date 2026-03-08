package com.example.weatherforecastapplication.alertsScreen.viewmodel

import android.content.Context
import android.location.Geocoder
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.weatherforecastapplication.data.models.ResponseState
import com.example.weatherforecastapplication.data.models.WeatherAlert
import com.example.weatherforecastapplication.repository.SettingsRepository
import com.example.weatherforecastapplication.repository.WeatherRepository
import com.example.weatherforecastapplication.worker.AlarmScheduler
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.supervisorScope
import kotlinx.coroutines.withTimeoutOrNull
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

    init {
        // Auto-refresh when ViewModel loads
        refreshAlerts(isManualRefresh = false)
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

            // FIX: Safely handle ID generation without crashing
            val newId = if (alert.id == 0) System.currentTimeMillis().toInt() else alert.id
            val finalAlert = alert.copy(id = newId, cityName = name)

            repository.insertAlert(finalAlert)
            refreshAlerts(isManualRefresh = false)

            val triggerTime = if (finalAlert.startTime <= System.currentTimeMillis()) System.currentTimeMillis() + 2000 else finalAlert.startTime
            AlarmScheduler.scheduleAlarm(context, finalAlert.copy(startTime = triggerTime))
        }
    }

    fun refreshAlerts(isManualRefresh: Boolean = true) {
        if (_isRefreshing.value) return

        viewModelScope.launch {
            if (isManualRefresh) _isRefreshing.value = true
            val startTime = System.currentTimeMillis()

            try {
                val currentAlerts = repository.getAlerts().first()
                if (currentAlerts.isEmpty()) return@launch

                val currentLang = settingsRepository.languageFlow.first()
                val currentUnit = settingsRepository.tempUnitFlow.first()

                // SOLID: Process all alerts in parallel safely
                supervisorScope {
                    val jobs = currentAlerts.map { alert ->
                        async {
                            try {
                                val state = repository.getFiveDayForecast(alert.lat, alert.lon, currentUnit, currentLang)
                                    .first { it !is ResponseState.Loading }

                                if (state is ResponseState.Success) {
                                    val firstForecast = state.data.list.firstOrNull()
                                    val translatedCityName = state.data.city.name
                                    if (firstForecast != null) {
                                        val temp = firstForecast.main.temp
                                        val desc = firstForecast.weather.firstOrNull()?.description?.replaceFirstChar { it.uppercase() } ?: "Unknown"
                                        val icon = firstForecast.weather.firstOrNull()?.icon ?: "01d"

                                        val updatedAlert = alert.copy(
                                            cityName = translatedCityName,
                                            currentIcon = icon,
                                            currentDescription = "$desc, ${temp.toInt()}°"
                                        )

                                        val freshDbCheck = repository.getAlerts().first()
                                        if (freshDbCheck.any { it.id == alert.id }) {
                                            repository.insertAlert(updatedAlert)
                                        }
                                    }
                                }
                            } catch (e: Exception) { e.printStackTrace() }
                        }
                    }
                    jobs.awaitAll()
                }

            } finally {
                if (isManualRefresh) {
                    val elapsedTime = System.currentTimeMillis() - startTime
                    if (elapsedTime < 1500) delay(1500 - elapsedTime)
                    _isRefreshing.value = false
                }
            }
        }
    }

    fun deleteAlert(alert: WeatherAlert) {
        viewModelScope.launch { repository.deleteAlert(alert) }
    }
}