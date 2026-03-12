package com.example.weatherforecastapplication.presentation.homeScreen.viewmodel

import com.example.weatherforecastapplication.data.models.ForecastResponseApi
import com.example.weatherforecastapplication.data.models.stateManagement.ResponseState
import com.example.weatherforecastapplication.data.repository.SettingsRepository
import com.example.weatherforecastapplication.domain.repository.WeatherRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModelTest {

    private lateinit var repository: WeatherRepository
    private lateinit var settingsRepository: SettingsRepository
    private lateinit var viewModel: HomeViewModel

    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)

        repository = mockk()
        settingsRepository = mockk(relaxed = true)

        every { settingsRepository.locationMethodFlow } returns flowOf("gps")
        every { settingsRepository.tempUnitFlow } returns flowOf("metric")
        every { settingsRepository.windUnitFlow } returns flowOf("m/s")

        viewModel = HomeViewModel(repository, settingsRepository)
    }

    @After
    fun teardown() {
        Dispatchers.resetMain()
    }

    // Test 1
    @Test
    fun initialState_isResponseStateLoading() {
        val currentState = viewModel.weatherState.value
        assertTrue(currentState is ResponseState.Loading)
    }

    // Test 2
    @Test
    fun updateGpsLocation_callsSettingsRepositorySaveGps() = runTest {
        val lat = 30.0
        val lon = 31.0
        coEvery { settingsRepository.saveGpsLocation(lat, lon) } returns Unit

        viewModel.updateGpsLocation(lat, lon)
        testDispatcher.scheduler.advanceUntilIdle()

        coVerify(exactly = 1) { settingsRepository.saveGpsLocation(lat, lon) }
    }

    // Test 3
    @Test
    fun getWeatherData_updatesWeatherStateToSuccess() = runTest {
        // Arrange
        val mockResponse = mockk<ForecastResponseApi>()
        coEvery { repository.getFiveDayForecast(any(), any(), any(), any()) } returns flowOf(ResponseState.Success(mockResponse))
        every { settingsRepository.languageFlow } returns flowOf("en")

        // Act
        viewModel.getWeatherData(30.0, 31.0)
        testDispatcher.scheduler.advanceUntilIdle()

        // Assert
        val currentState = viewModel.weatherState.value
        assertTrue(currentState is ResponseState.Success)
        assertEquals(mockResponse, (currentState as ResponseState.Success).data)
    }
}