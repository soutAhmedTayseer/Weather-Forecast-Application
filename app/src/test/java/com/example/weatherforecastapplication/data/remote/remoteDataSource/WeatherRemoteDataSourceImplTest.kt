package com.example.weatherforecastapplication.data.remote.remoteDataSource

import com.example.weatherforecastapplication.data.models.ForecastResponseApi
import com.example.weatherforecastapplication.data.models.LocationData
import com.example.weatherforecastapplication.data.remote.network.WeatherApiService
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import retrofit2.Response

class WeatherRemoteDataSourceImplTest {

    private lateinit var apiService: WeatherApiService
    private lateinit var remoteDataSource: WeatherRemoteDataSourceImpl

    @Before
    fun setup() {
        apiService = mockk() // Fake Retrofit API
        remoteDataSource = WeatherRemoteDataSourceImpl(apiService)
    }

    // Test 1
    @Test
    fun getFiveDayForecast_returnsSuccessfulResponse() = runTest {
        val mockResponse = mockk<ForecastResponseApi>()
        coEvery { apiService.getFiveDayForecast(30.0, 31.0, "metric", "en", any()) } returns Response.success(mockResponse)

        val result = remoteDataSource.getFiveDayForecast(30.0, 31.0, "metric", "en", "dummy_key")

        assertTrue(result.isSuccessful)
        assertEquals(mockResponse, result.body())
    }

    // Test 2
    @Test
    fun searchLocations_returnsSuccessfulResponse() = runBlocking {
        val mockList = listOf(mockk<LocationData>())

        coEvery {
            apiService.searchLocations(eq("Cairo"), any(), any())
        } returns Response.success(mockList)

        val result = remoteDataSource.searchLocations("Cairo", "dummy_key")

        assertTrue(result.isSuccessful)
        assertEquals(1, result.body()?.size)
    }

    // Test 3
    @Test
    fun getFiveDayForecast_handlesErrorResponse() = runBlocking {
        // Simulate a 404 Not Found error from the server
        val errorResponse = Response.error<ForecastResponseApi>(404, mockk(relaxed = true))
        coEvery { apiService.getFiveDayForecast(any(), any(), any(), any(), any()) } returns errorResponse

        val result = remoteDataSource.getFiveDayForecast(0.0, 0.0, "metric", "en", "key")

        assertTrue(!result.isSuccessful)
        assertEquals(404, result.code())
    }
}