package com.example.weatherforecastapplication.data.repository

import com.example.weatherforecastapplication.data.local.dao.AlertDao
import com.example.weatherforecastapplication.data.local.dao.WeatherDao
import com.example.weatherforecastapplication.data.local.localDataSource.CityLocationLocalDataSource
import com.example.weatherforecastapplication.data.models.entities.WeatherAlert
import com.example.weatherforecastapplication.data.remote.remoteDataSource.WeatherRemoteDataSource
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class WeatherRepositoryImplTest {

    private lateinit var remoteDataSource: WeatherRemoteDataSource
    private lateinit var localDataSource: CityLocationLocalDataSource
    private lateinit var weatherDao: WeatherDao
    private lateinit var alertDao: AlertDao
    private lateinit var repository: WeatherRepositoryImpl

    @Before
    fun setup() {
        remoteDataSource = mockk()
        localDataSource = mockk()
        weatherDao = mockk()
        alertDao = mockk()

        repository = WeatherRepositoryImpl(remoteDataSource, localDataSource, weatherDao, alertDao)
    }

    // Test 1
    @Test
    fun getAlerts_returnsFlowFromAlertDao() = runTest {
        val mockAlerts = listOf(mockk<WeatherAlert>())
        coEvery { alertDao.getAllAlerts() } returns flowOf(mockAlerts)

        val result = repository.getAlerts().first()

        assertEquals(1, result.size)
        coVerify(exactly = 1) { alertDao.getAllAlerts() }
    }

    // Test 2
    @Test
    fun insertAlert_delegatesToAlertDao() = runTest {
        val mockAlert = mockk<WeatherAlert>()
        coEvery { alertDao.insertAlert(mockAlert) } returns Unit

        repository.insertAlert(mockAlert)

        coVerify(exactly = 1) { alertDao.insertAlert(mockAlert) }
    }

    // Test 3
    @Test
    fun deleteAlert_delegatesToAlertDao() = runTest {
        val mockAlert = mockk<WeatherAlert>()
        coEvery { alertDao.deleteAlert(mockAlert) } returns Unit

        repository.deleteAlert(mockAlert)

        coVerify(exactly = 1) { alertDao.deleteAlert(mockAlert) }
    }
}