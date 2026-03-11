package com.example.weatherforecastapplication.data.remote.remoteDataSource

import com.example.weatherforecastapplication.data.models.ForecastResponseApi
import com.example.weatherforecastapplication.data.models.LocationData
import retrofit2.Response

interface WeatherRemoteDataSource {
    suspend fun getFiveDayForecast(
        lat: Double,
        lon: Double,
        apiKey: String,
        units: String,
        lang: String
    ): Response<ForecastResponseApi>

    suspend fun searchLocations(query: String, apiKey: String): Response<List<LocationData>>
}