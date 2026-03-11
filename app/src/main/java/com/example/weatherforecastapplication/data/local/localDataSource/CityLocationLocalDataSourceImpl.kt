package com.example.weatherforecastapplication.data.local.localDataSource

import com.example.weatherforecastapplication.data.local.dao.CityLocationDao
import com.example.weatherforecastapplication.data.models.entities.CityLocation
import kotlinx.coroutines.flow.Flow

class CityLocationLocalDataSourceImpl(
    private val dao: CityLocationDao
) : CityLocationLocalDataSource {

    override fun getAllFavoriteLocations(): Flow<List<CityLocation>> {
        return dao.getAllFavoriteLocations()
    }

    override suspend fun insertLocation(location: CityLocation) {
        dao.insertLocation(location)
    }

    override suspend fun deleteLocation(location: CityLocation) {
        dao.deleteLocation(location)
    }
}