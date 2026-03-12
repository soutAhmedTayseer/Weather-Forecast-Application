package com.example.weatherforecastapplication.data.local.localDataSource

import com.example.weatherforecastapplication.data.local.dao.CityLocationDao
import com.example.weatherforecastapplication.data.models.entities.CityLocation
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class CityLocationLocalDataSourceImplTest {

    private lateinit var dao: CityLocationDao
    private lateinit var localDataSource: CityLocationLocalDataSourceImpl

    @Before
    fun setup() {
        dao = mockk() // Fake DAO
        localDataSource = CityLocationLocalDataSourceImpl(dao)
    }

    // Test 1
    @Test
    fun insertLocation_delegatesToDao() = runBlocking {
        val location = CityLocation(lat = 30.0, lon = 31.0, cityName = "Cairo")
        coEvery { dao.insertLocation(location) } returns Unit

        localDataSource.insertLocation(location)

        coVerify(exactly = 1) { dao.insertLocation(location) }
    }

    // Test 2
    @Test
    fun deleteLocation_delegatesToDao() = runBlocking {
        val location = CityLocation(lat = 30.0, lon = 31.0, cityName = "Cairo")
        coEvery { dao.deleteLocation(location) } returns Unit

        localDataSource.deleteLocation(location)

        coVerify(exactly = 1) { dao.deleteLocation(location) }
    }

    // Test 3
    @Test
    fun getAllFavoriteLocations_returnsFlowFromDao() = runBlocking {
        val mockList = listOf(CityLocation(lat = 30.0, lon = 31.0, cityName = "Cairo"))
        coEvery { dao.getAllFavoriteLocations() } returns flowOf(mockList)

        val result = localDataSource.getAllFavoriteLocations().first()

        assertEquals(1, result.size)
        assertEquals("Cairo", result[0].cityName)
    }
}