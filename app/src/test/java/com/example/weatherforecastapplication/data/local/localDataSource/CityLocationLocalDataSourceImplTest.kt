package com.example.weatherforecastapplication.data.local.localDataSource

import com.example.weatherforecastapplication.data.local.dao.CityLocationDao
import com.example.weatherforecastapplication.data.models.entities.CityLocation
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Before
import org.junit.Test

class CityLocationLocalDataSourceImplTest {

    private lateinit var dao: CityLocationDao
    private lateinit var localDataSource: CityLocationLocalDataSourceImpl

    @Before
    fun setup() {
        dao = mockk()
        localDataSource = CityLocationLocalDataSourceImpl(dao)
    }

    @Test
    fun insertLocation_validLocation_delegatesToDao() = runTest {
        val location = CityLocation(lat = 30.0, lon = 31.0, cityName = "Cairo")
        coEvery { dao.insertLocation(location) } returns Unit

        localDataSource.insertLocation(location)

        coVerify(exactly = 1) { dao.insertLocation(location) }
    }

    @Test
    fun deleteLocation_validLocation_delegatesToDao() = runTest {
        val location = CityLocation(lat = 30.0, lon = 31.0, cityName = "Cairo")
        coEvery { dao.deleteLocation(location) } returns Unit

        localDataSource.deleteLocation(location)

        coVerify(exactly = 1) { dao.deleteLocation(location) }
    }

    @Test
    fun getAllFavoriteLocations_requestsData_returnsFlowFromDao() = runTest {
        val mockList = listOf(CityLocation(lat = 30.0, lon = 31.0, cityName = "Cairo"))
        coEvery { dao.getAllFavoriteLocations() } returns flowOf(mockList)

        val result = localDataSource.getAllFavoriteLocations().first()

        assertThat(result.size, `is`(1))
        assertThat(result[0].cityName, `is`("Cairo"))
    }
}