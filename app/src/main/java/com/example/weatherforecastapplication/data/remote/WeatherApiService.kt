package com.example.weatherforecastapplication.data.remote

import com.example.weatherforecastapplication.data.models.ForecastResponseApi
import com.example.weatherforecastapplication.data.models.LocationData
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface WeatherApiService {
    // Uses the required endpoint for the weather forecast
    @GET("data/2.5/forecast")
    suspend fun getFiveDayForecast(
        @Query("lat") lat: Double,
        @Query("lon") lon: Double,
        @Query("appid") apiKey: String,
        @Query("units") units: String = "metric", // Defaulting to Celsius
        @Query("lang") lang: String = "en"
    ): Response<ForecastResponseApi>

    // Free Geo API for autocomplete search
    @GET("geo/1.0/direct")
    suspend fun searchLocations(
        @Query("q") query: String,
        @Query("limit") limit: Int = 5, // Return top 5 results
        @Query("appid") apiKey: String
    ): Response<List<LocationData>>
}