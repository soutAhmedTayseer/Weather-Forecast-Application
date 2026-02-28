package com.example.weatherforecastapplication.repository

import com.example.weatherforecastapplication.data.local.CityLocationLocalDataSource
import com.example.weatherforecastapplication.data.models.CityLocation
import com.example.weatherforecastapplication.data.models.ForecastResponseApi
import com.example.weatherforecastapplication.data.models.ResponseState
import com.example.weatherforecastapplication.data.remote.WeatherRemoteDataSource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class WeatherRepositoryImpl(
    private val remoteDataSource: WeatherRemoteDataSource,
    private val localDataSource: CityLocationLocalDataSource
) : WeatherRepository {

    private val API_KEY = "ede7c5fa0ceb3830331bf1b977204c42"

    override suspend fun getFiveDayForecast(
        lat: Double, lon: Double, units: String, lang: String
    ): Flow<ResponseState<ForecastResponseApi>> = flow {

        emit(ResponseState.Loading)

        try {
            val response = remoteDataSource.getFiveDayForecast(lat, lon, API_KEY, units, lang)
            if (response.isSuccessful && response.body() != null) {
                emit(ResponseState.Success(response.body()!!))
            } else {
                emit(ResponseState.Error(response.message() ?: "An unknown network error occurred"))
            }
        } catch (e: Exception) {
            emit(ResponseState.Error(e.localizedMessage ?: "Failed to connect to the server"))
        }
    }

    override fun getFavoriteLocations(): Flow<List<CityLocation>> {
        return localDataSource.getAllFavoriteLocations()
    }

    override suspend fun insertFavoriteLocation(location: CityLocation) {
        localDataSource.insertLocation(location)
    }

    override suspend fun deleteFavoriteLocation(location: CityLocation) {
        localDataSource.deleteLocation(location)
    }
}