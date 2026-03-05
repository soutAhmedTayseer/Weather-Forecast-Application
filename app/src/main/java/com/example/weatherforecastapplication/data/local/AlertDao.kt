package com.example.weatherforecastapplication.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.weatherforecastapplication.data.models.WeatherAlert
import kotlinx.coroutines.flow.Flow

@Dao
interface AlertDao {

    @Query("SELECT * FROM weather_alerts WHERE id = :id LIMIT 1")
    suspend fun getAlertById(id: Int): WeatherAlert?

    // For the UI to display the list of alerts
    @Query("SELECT * FROM weather_alerts ORDER BY startTime ASC")
    fun getAllAlerts(): Flow<List<WeatherAlert>>

    @Query("DELETE FROM weather_alerts WHERE id = :id")
    suspend fun deleteAlertById(id: Int)

    // For the WorkManager to fetch alerts that are currently active
    @Query("SELECT * FROM weather_alerts WHERE :currentTime BETWEEN startTime AND endTime")
    suspend fun getActiveAlerts(currentTime: Long): List<WeatherAlert>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAlert(alert: WeatherAlert)

    @Delete
    suspend fun deleteAlert(alert: WeatherAlert)
}