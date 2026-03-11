package com.example.weatherforecastapplication.data.local.localDataSource

import com.example.weatherforecastapplication.data.models.entities.CityLocation
import kotlinx.coroutines.flow.Flow

interface CityLocationLocalDataSource {
    fun getAllFavoriteLocations(): Flow<List<CityLocation>>
    suspend fun insertLocation(location: CityLocation)
    suspend fun deleteLocation(location: CityLocation)
}