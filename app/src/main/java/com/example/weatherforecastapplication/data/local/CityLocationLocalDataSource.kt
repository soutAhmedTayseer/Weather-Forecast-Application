package com.example.weatherforecastapplication.data.local

import com.example.weatherforecastapplication.data.models.CityLocation
import kotlinx.coroutines.flow.Flow

interface CityLocationLocalDataSource {
    fun getAllFavoriteLocations(): Flow<List<CityLocation>>
    suspend fun insertLocation(location: CityLocation)
    suspend fun deleteLocation(location: CityLocation)
}