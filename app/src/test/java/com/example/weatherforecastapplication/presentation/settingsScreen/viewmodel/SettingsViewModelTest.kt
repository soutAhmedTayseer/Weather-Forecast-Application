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
        // Set main dispatcher for testing ViewModel coroutines
        Dispatchers.setMain(testDispatcher)

        settingsRepository = mockk(relaxed = true)

        // Arrange default settings flows to prevent crashes on init
        every { settingsRepository.locationMethodFlow } returns flowOf("gps")
        every { settingsRepository.languageFlow } returns flowOf("en")

        viewModel = SettingsViewModel(settingsRepository)
    }

    @After
    fun teardown() {
        Dispatchers.resetMain()
    }

    // Test 1
    @Test
    fun setLocationMethod_callsRepositorySaveLocationMethod() = runTest {
        // 1. Arrange
        val method = "map"
        coEvery { settingsRepository.saveLocationMethod(method) } returns Unit

        // 2. Act
        viewModel.setLocationMethod(method)
        testDispatcher.scheduler.advanceUntilIdle()

        // 3. Assert
        coVerify(exactly = 1) { settingsRepository.saveLocationMethod(method) }
    }

    // Test 2
    @Test
    fun setTempUnit_callsRepositorySaveTempUnit() = runTest {
        // 1. Arrange
        val unit = "imperial"
        val displayName = "Fahrenheit"
        coEvery { settingsRepository.saveTempUnit(unit) } returns Unit

        // 2. Act
        viewModel.setTempUnit(unit, displayName)
        testDispatcher.scheduler.advanceUntilIdle()

        // 3. Assert
        coVerify(exactly = 1) { settingsRepository.saveTempUnit(unit) }
    }

    // Test 3
    @Test
    fun saveHomeLocationFromMap_callsRepositorySaveHomeLocation() = runTest {
        // 1. Arrange
        val lat = 30.0444
        val lon = 31.2357
        val cityName = "Cairo"
        coEvery { settingsRepository.saveHomeLocation(lat, lon) } returns Unit

        // 2. Act
        viewModel.saveHomeLocationFromMap(lat, lon, cityName)
        testDispatcher.scheduler.advanceUntilIdle()

        // 3. Assert
        coVerify(exactly = 1) { settingsRepository.saveHomeLocation(lat, lon) }
    }
}