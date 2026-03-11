package com.example.weatherforecastapplication.data.models.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.weatherforecastapplication.data.models.ForecastResponseApi

@Entity(tableName = "cached_weather")
data class CachedWeather(
    // We will use the latitude and longitude combined as a unique ID (e.g., "31.2001_29.9187")
    // This allows you to cache weather for multiple favorite cities!
    @PrimaryKey val id: String,
    val weatherData: ForecastResponseApi,
    val timestamp: Long // Helps us know how old the data is
)