package com.example.weatherforecastapplication.data.repository

import com.example.weatherforecastapplication.data.local.dao.AlertDao
import com.example.weatherforecastapplication.data.models.entities.CachedWeather
import com.example.weatherforecastapplication.data.local.localDataSource.CityLocationLocalDataSource
import com.example.weatherforecastapplication.data.local.dao.WeatherDao
import com.example.weatherforecastapplication.data.models.entities.CityLocation
import com.example.weatherforecastapplication.data.models.ForecastResponseApi
import com.example.weatherforecastapplication.data.models.LocationData
import com.example.weatherforecastapplication.data.models.stateManagement.ResponseState
import com.example.weatherforecastapplication.data.models.entities.WeatherAlert
import com.example.weatherforecastapplication.data.remote.remoteDataSource.WeatherRemoteDataSource
import com.example.weatherforecastapplication.domain.repository.WeatherRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class WeatherRepositoryImpl(
    private val remoteDataSource: WeatherRemoteDataSource,
    private val localDataSource: CityLocationLocalDataSource,
    private val weatherDao: WeatherDao,
    private val alertDao: AlertDao
) : WeatherRepository {

    private val API_KEY = "ede7c5fa0ceb3830331bf1b977204c42"

    override suspend fun getFiveDayForecast(
        lat: Double, lon: Double, units: String, lang: String
    ): Flow<ResponseState<ForecastResponseApi>> = flow {

        emit(ResponseState.Loading)

        val cacheId = "${lat}_${lon}" // Create a unique ID for this specific location

        // 1. OFFLINE FIRST: Check the local database immediately
        val cachedData = weatherDao.getCachedWeather(cacheId)
        if (cachedData != null) {
            // Instantly show the user the old data so they aren't staring at a loading screen
            emit(ResponseState.Success(cachedData.weatherData))
        }

        // 2. NETWORK FETCH: Try to get fresh data from the API
        try {
            // Note: Passed your API_KEY here to match your remote data source setup!
            val response = remoteDataSource.getFiveDayForecast(lat, lon, API_KEY, units, lang)

            if (response.isSuccessful && response.body() != null) {
                val freshResponse = response.body()!!

                // 3. SAVE TO DB: Update the local database with the fresh data
                val newCacheEntity = CachedWeather(
                    id = cacheId,
                    weatherData = freshResponse,
                    timestamp = System.currentTimeMillis()
                )
                weatherDao.insertWeather(newCacheEntity)

                // 4. UPDATE UI: Emit the fresh data to the screen
                emit(ResponseState.Success(freshResponse))
            } else {
                // If network fails and we have no offline data, emit the error
                if (cachedData == null) {
                    emit(
                        ResponseState.Error(
                            response.message() ?: "An unknown network error occurred"
                        )
                    )
                }
            }
        } catch (e: Exception) {
            // If there's no internet connection AND we had no offline data, show an error.
            // If we already showed offline data, we just silently fail the network call so the user is uninterrupted.
            if (cachedData == null) {
                emit(ResponseState.Error(e.localizedMessage ?: "Failed to connect to the server"))
            }
        }
    }

    // --- Favorite Locations Logic ---

    override fun getFavoriteLocations(): Flow<List<CityLocation>> {
        return localDataSource.getAllFavoriteLocations()
    }

    override suspend fun insertFavoriteLocation(location: CityLocation) {
        localDataSource.insertLocation(location)
    }

    override suspend fun deleteFavoriteLocation(location: CityLocation) {
        localDataSource.deleteLocation(location)
    }

    override suspend fun searchLocations(query: String): Flow<List<LocationData>> = flow {
        try {
            val response = remoteDataSource.searchLocations(query, API_KEY)
            if (response.isSuccessful && response.body() != null) {
                emit(response.body()!!)
            } else {
                emit(emptyList()) // Return empty if failed so the dropdown just disappears
            }
        } catch (e: Exception) {
            emit(emptyList())
        }
    }

    // --- Alerts Logic ---
    override fun getAlerts(): Flow<List<WeatherAlert>> = alertDao.getAllAlerts()

    override suspend fun insertAlert(alert: WeatherAlert) {
        alertDao.insertAlert(alert)
    }

    override suspend fun deleteAlert(alert: WeatherAlert) {
        alertDao.deleteAlert(alert)
    }

    override suspend fun getActiveAlerts(currentTime: Long): List<WeatherAlert> {
        return alertDao.getActiveAlerts(currentTime)
    }

}