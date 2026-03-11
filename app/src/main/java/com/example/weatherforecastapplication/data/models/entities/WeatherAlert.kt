package com.example.weatherforecastapplication.data.models.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "weather_alerts")
data class WeatherAlert(
    @PrimaryKey
    val id: Int,
    val startTime: Long,
    val endTime: Long,
    val isAlarm: Boolean,
    val isNotification: Boolean,
    val lat: Double,
    val lon: Double,
    val cityName: String,
    val notificationToneUri: String,
    val alarmToneUri: String,
    val currentIcon: String = "01d",
    val currentDescription: String = "Monitoring..."
)