package com.example.weatherforecastapplication.data.local

import androidx.room.TypeConverter
import com.example.weatherforecastapplication.data.models.ForecastResponseApi
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class WeatherTypeConverters {
    private val gson = Gson()

    // Converts your massive API Response into a single String to save in the Database
    @TypeConverter
    fun fromForecastResponse(response: ForecastResponseApi?): String? {
        return gson.toJson(response)
    }

    // Converts the String from the Database back into your Kotlin Object for the UI
    @TypeConverter
    fun toForecastResponse(jsonString: String?): ForecastResponseApi? {
        val type = object : TypeToken<ForecastResponseApi>() {}.type
        return gson.fromJson(jsonString, type)
    }
}