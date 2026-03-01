package com.example.weatherforecastapplication.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.weatherforecastapplication.data.models.CityLocation

// Ensure you update your entities list and version number if necessary!
@Database(entities = [CityLocation::class, CachedWeather::class], version = 2, exportSchema = false)
@TypeConverters(WeatherTypeConverters::class) // Tell Room to use your new converters
abstract class CityDatabase : RoomDatabase() {

    abstract fun cityLocationDao(): CityLocationDao
    abstract fun weatherDao(): WeatherDao

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