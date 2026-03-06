package com.example.weatherforecastapplication.repository

import com.example.weatherforecastapplication.data.models.CityLocation
import com.example.weatherforecastapplication.data.models.ForecastResponseApi
import com.example.weatherforecastapplication.data.models.LocationData
import com.example.weatherforecastapplication.data.models.ResponseState
import com.example.weatherforecastapplication.data.models.WeatherAlert
import kotlinx.coroutines.flow.Flow

interface WeatherRepository {
    // Remote
    suspend fun getFiveDayForecast(
        lat: Double,
        lon: Double,
        units: String,
        lang: String
    ): Flow<ResponseState<ForecastResponseApi>>

    // Local
    fun getFavoriteLocations(): Flow<List<CityLocation>>
    suspend fun insertFavoriteLocation(location: CityLocation)
    suspend fun deleteFavoriteLocation(location: CityLocation)

    suspend fun searchLocations(query: String): Flow<List<LocationData>>
    // Alerts
    fun getAlerts(): Flow<List<WeatherAlert>>
    suspend fun insertAlert(alert: WeatherAlert)
    suspend fun deleteAlert(alert: WeatherAlert)
    suspend fun getActiveAlerts(currentTime: Long): List<WeatherAlert>
}