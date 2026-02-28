package com.example.weatherforecastapplication.data.remote

import com.example.weatherforecastapplication.data.models.ForecastResponseApi
import retrofit2.Response

interface WeatherRemoteDataSource {
    suspend fun getFiveDayForecast(
        lat: Double,
        lon: Double,
        apiKey: String,
        units: String,
        lang: String
    ): Response<ForecastResponseApi>
}