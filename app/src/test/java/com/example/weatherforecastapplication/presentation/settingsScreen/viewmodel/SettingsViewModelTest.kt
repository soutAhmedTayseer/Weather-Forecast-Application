package com.example.weatherforecastapplication.presentation.settingsScreen.viewmodel

import com.example.weatherforecastapplication.data.repository.SettingsRepository
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
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SettingsViewModelTest {

    private lateinit var settingsRepository: SettingsRepository
    private lateinit var viewModel: SettingsViewModel

    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)

        settingsRepository = mockk(relaxed = true)

        every { settingsRepository.locationMethodFlow } returns flowOf("gps")
        every { settingsRepository.languageFlow } returns flowOf("en")

        viewModel = SettingsViewModel(settingsRepository)
    }

    @After
    fun teardown() {
        Dispatchers.resetMain()
    }

    @Test
    fun setLocationMethod_validMethod_delegatesToRepository() = runTest {
        val method = "map"
        coEvery { settingsRepository.saveLocationMethod(method) } returns Unit

        viewModel.setLocationMethod(method)
        testDispatcher.scheduler.advanceUntilIdle()

        coVerify(exactly = 1) { settingsRepository.saveLocationMethod(method) }
    }

    @Test
    fun setTempUnit_validUnit_delegatesToRepository() = runTest {
        val unit = "imperial"
        val displayName = "Fahrenheit"
        coEvery { settingsRepository.saveTempUnit(unit) } returns Unit

        viewModel.setTempUnit(unit, displayName)
        testDispatcher.scheduler.advanceUntilIdle()

        coVerify(exactly = 1) { settingsRepository.saveTempUnit(unit) }
    }

    @Test
    fun saveHomeLocationFromMap_validCoordinates_delegatesToRepository() = runTest {
        val lat = 30.0444
        val lon = 31.2357
        val cityName = "Cairo"
        coEvery { settingsRepository.saveHomeLocation(lat, lon) } returns Unit

        viewModel.saveHomeLocationFromMap(lat, lon, cityName)
        testDispatcher.scheduler.advanceUntilIdle()

        coVerify(exactly = 1) { settingsRepository.saveHomeLocation(lat, lon) }
    }
}