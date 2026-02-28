package com.example.weatherforecastapplication.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.weatherforecastapplication.data.models.CityLocation
import kotlinx.coroutines.flow.Flow

@Dao
interface CityLocationDao {
    @Query("SELECT * FROM favorite_locations")
    fun getAllFavoriteLocations(): Flow<List<CityLocation>> // Using Flow for reactive UI updates

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLocation(location: CityLocation)

    @Delete
    suspend fun deleteLocation(location: CityLocation)
}