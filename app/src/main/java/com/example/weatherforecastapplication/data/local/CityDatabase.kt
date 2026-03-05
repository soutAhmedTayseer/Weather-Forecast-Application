package com.example.weatherforecastapplication.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.weatherforecastapplication.data.models.CityLocation
import com.example.weatherforecastapplication.data.models.WeatherAlert

// Ensure you update your entities list and version number if necessary!
@Database(entities = [CityLocation::class, CachedWeather::class, WeatherAlert::class], version = 7, exportSchema = false)
@TypeConverters(WeatherTypeConverters::class)abstract class CityDatabase : RoomDatabase() {

    abstract fun cityLocationDao(): CityLocationDao
    abstract fun weatherDao(): WeatherDao
    abstract fun alertDao(): AlertDao

    companion object {
        @Volatile
        private var INSTANCE: CityDatabase? = null

        fun getDatabase(context: Context): CityDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    CityDatabase::class.java,
                    "weather_database"
                )
                    .fallbackToDestructiveMigration() // Helps clear old DB versions during testing
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}