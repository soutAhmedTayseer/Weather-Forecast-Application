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
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
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
    }.stateIn(viewModelScope, SharingStarted.Eagerly, Pair(31.2001, 29.9187))

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing = _isRefreshing.asStateFlow()

    fun saveAlert(context: Context, alert: WeatherAlert) {
        viewModelScope.launch {
            val name = try {
                val geocoder = Geocoder(context, Locale.getDefault())
                @Suppress("DEPRECATION")
                val addresses = geocoder.getFromLocation(alert.lat, alert.lon, 1)
                if (!addresses.isNullOrEmpty()) addresses[0].locality ?: addresses[0].subAdminArea ?: "Selected Location" else "Selected Location"
            } catch (e: Exception) { "Selected Location" }

            // Keep the existing ID if it's an edit, otherwise generate a new one
            val newId = if (alert.id == 0) System.currentTimeMillis().toInt() else alert.id
            val finalAlert = alert.copy(id = newId, cityName = name)

            repository.insertAlert(finalAlert)

            val triggerTime = if (finalAlert.startTime <= System.currentTimeMillis()) System.currentTimeMillis() + 2000 else finalAlert.startTime
            AlarmScheduler.scheduleAlarm(context, finalAlert.copy(startTime = triggerTime))
        }
    }

    // NEW: Manual Pull-to-Refresh logic
    fun refreshAlerts() {
        viewModelScope.launch {
            _isRefreshing.value = true
            val currentAlerts = alertsList.value
            for (alert in currentAlerts) {
                repository.getFiveDayForecast(alert.lat, alert.lon, "metric", "en").collect { state ->
                    if (state is ResponseState.Success) {
                        val firstForecast = state.data.list.firstOrNull()
                        val icon = firstForecast?.weather?.firstOrNull()?.icon ?: "01d"
                        val desc = firstForecast?.weather?.firstOrNull()?.description ?: "Clear"
                        val temp = firstForecast?.main?.temp ?: 0.0

                        val updatedAlert = alert.copy(
                            currentIcon = icon,
                            currentDescription = "$desc, ${temp.toInt()}°C"
                        )
                        repository.insertAlert(updatedAlert) // Updates the DB instantly
                    }
                }
            }
            _isRefreshing.value = false
        }
    }

    fun deleteAlert(alert: WeatherAlert) {
        viewModelScope.launch { repository.deleteAlert(alert) }
    }
}