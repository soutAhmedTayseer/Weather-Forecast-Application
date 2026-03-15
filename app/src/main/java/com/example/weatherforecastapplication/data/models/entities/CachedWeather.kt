package com.example.weatherforecastapplication.data.models.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.weatherforecastapplication.data.models.dataClasses.ForecastResponseApi

@Entity(tableName = "cached_weather")
data class CachedWeather(
    // We will use the latitude and longitude combined as a unique ID
    // This allows you to cache weather for multiple favorite cities
    @PrimaryKey val id: String,
    val weatherData: ForecastResponseApi,
    val timestamp: Long
)