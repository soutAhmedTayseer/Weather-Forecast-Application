package com.example.weatherforecastapplication.data.remote

import com.example.weatherforecastapplication.data.models.ForecastResponseApi
import retrofit2.Response

class WeatherRemoteDataSourceImpl(
    private val apiService: WeatherApiService
) : WeatherRemoteDataSource {

    override suspend fun getFiveDayForecast(
        lat: Double, lon: Double, apiKey: String, units: String, lang: String
    ): Response<ForecastResponseApi> {
        return apiService.getFiveDayForecast(lat, lon, apiKey, units, lang)
    }
}