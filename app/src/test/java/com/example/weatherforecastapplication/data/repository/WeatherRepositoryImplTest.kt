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
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.MatcherAssert.assertThat
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

    @Test
    fun getAlerts_requestsAlerts_returnsFlowFromAlertDao() = runTest {
        val mockAlerts = listOf(mockk<WeatherAlert>())
        coEvery { alertDao.getAllAlerts() } returns flowOf(mockAlerts)

        val result = repository.getAlerts().first()

        assertThat(result.size, `is`(1))
        coVerify(exactly = 1) { alertDao.getAllAlerts() }
    }

    @Test
    fun insertAlert_validAlert_delegatesToAlertDao() = runTest {
        val mockAlert = mockk<WeatherAlert>()
        coEvery { alertDao.insertAlert(mockAlert) } returns Unit

        repository.insertAlert(mockAlert)

        coVerify(exactly = 1) { alertDao.insertAlert(mockAlert) }
    }

    @Test
    fun deleteAlert_validAlert_delegatesToAlertDao() = runTest {
        val mockAlert = mockk<WeatherAlert>()
        coEvery { alertDao.deleteAlert(mockAlert) } returns Unit

        repository.deleteAlert(mockAlert)

        coVerify(exactly = 1) { alertDao.deleteAlert(mockAlert) }
    }
}