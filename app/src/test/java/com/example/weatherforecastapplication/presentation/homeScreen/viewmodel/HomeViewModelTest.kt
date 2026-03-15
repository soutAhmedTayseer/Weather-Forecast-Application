package com.example.weatherforecastapplication.presentation.homeScreen.viewmodel

import com.example.weatherforecastapplication.data.models.dataClasses.ForecastResponseApi
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
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.MatcherAssert.assertThat
import org.junit.After
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
        // We replace Android's real Main Thread with our controllable Test Thread.
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

    @Test
    fun init_viewModelCreated_initialStateIsResponseStateLoading() {
        val currentState = viewModel.weatherState.value

        assertThat(currentState is ResponseState.Loading, `is`(true))
    }

    @Test
    fun updateGpsLocation_newCoordinates_callsSettingsRepositorySaveGps() = runTest {
        val lat = 30.0
        val lon = 31.0
        coEvery { settingsRepository.saveGpsLocation(lat, lon) } returns Unit

        viewModel.updateGpsLocation(lat, lon)
        // FAST-FORWARD TIME: We tell the test to instantly skip any 'delays' or background loading so we can check the result immediately.
        testDispatcher.scheduler.advanceUntilIdle()

        // Verify that the ViewModel actually told the Repository to save the exact coordinates we gave it.
        coVerify(exactly = 1) { settingsRepository.saveGpsLocation(lat, lon) }
    }

    @Test
    fun getWeatherData_successfulFetch_updatesWeatherStateToSuccess() = runTest {
        val mockResponse = mockk<ForecastResponseApi>()
        coEvery { repository.getFiveDayForecast(any(), any(), any(), any()) } returns flowOf(
            ResponseState.Success(mockResponse)
        )
        every { settingsRepository.languageFlow } returns flowOf("en")

        viewModel.getWeatherData(30.0, 31.0)
        testDispatcher.scheduler.advanceUntilIdle()

        val currentState = viewModel.weatherState.value

        assertThat(currentState is ResponseState.Success, `is`(true))
        assertThat((currentState as ResponseState.Success).data, `is`(mockResponse))
    }
}