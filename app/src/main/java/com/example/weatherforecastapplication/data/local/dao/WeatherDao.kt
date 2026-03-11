package com.example.weatherforecastapplication.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.weatherforecastapplication.data.models.entities.CachedWeather

@Dao
interface WeatherDao {
    // Fetches the cached weather for a specific location
    @Query("SELECT * FROM cached_weather WHERE id = :id")
    suspend fun getCachedWeather(id: String): CachedWeather?

    // Saves the fresh API data, overwriting the old data if it exists
    @Insert(onConflict = OnConflictStrategy.Companion.REPLACE)
    suspend fun insertWeather(weather: CachedWeather)

    // Deletes weather for a location (useful if they remove a Favorite)
    @Query("DELETE FROM cached_weather WHERE id = :id")
    suspend fun deleteWeather(id: String)
}