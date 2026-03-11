package com.example.weatherforecastapplication.data.models.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "favorite_locations")
data class CityLocation(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val lat: Double,
    val lon: Double,
    val cityName: String
)