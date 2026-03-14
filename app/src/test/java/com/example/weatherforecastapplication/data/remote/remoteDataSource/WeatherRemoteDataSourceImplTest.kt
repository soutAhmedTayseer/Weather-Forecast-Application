package com.example.weatherforecastapplication.data.remote.remoteDataSource

import com.example.weatherforecastapplication.data.models.ForecastResponseApi
import com.example.weatherforecastapplication.data.models.LocationData
import com.example.weatherforecastapplication.data.remote.network.WeatherApiService
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Before
import org.junit.Test
import retrofit2.Response

class WeatherRemoteDataSourceImplTest {

    private lateinit var apiService: WeatherApiService
    private lateinit var remoteDataSource: WeatherRemoteDataSourceImpl

    @Before
    fun setup() {
        apiService = mockk()
        remoteDataSource = WeatherRemoteDataSourceImpl(apiService)
    }

    @Test
    fun getFiveDayForecast_validCoordinates_returnsSuccessfulResponse() = runTest {
        val mockResponse = mockk<ForecastResponseApi>()
        coEvery { apiService.getFiveDayForecast(30.0, 31.0, "metric", "en", any()) } returns Response.success(mockResponse)

        val result = remoteDataSource.getFiveDayForecast(30.0, 31.0, "metric", "en", "dummy_key")

        assertThat(result.isSuccessful, `is`(true))
        assertThat(result.body(), `is`(mockResponse))
    }

    @Test
    fun searchLocations_validCityName_returnsSuccessfulResponse() = runTest {
        val mockList = listOf(mockk<LocationData>())

        coEvery {
            apiService.searchLocations(eq("Cairo"), any(), any())
        } returns Response.success(mockList)

        val result = remoteDataSource.searchLocations("Cairo", "dummy_key")

        assertThat(result.isSuccessful, `is`(true))
        assertThat(result.body()?.size, `is`(1))
    }

    @Test
    fun getFiveDayForecast_serverError_returnsErrorResponse() = runTest {
        val errorResponse = Response.error<ForecastResponseApi>(404, mockk(relaxed = true))
        coEvery { apiService.getFiveDayForecast(any(), any(), any(), any(), any()) } returns errorResponse

        val result = remoteDataSource.getFiveDayForecast(0.0, 0.0, "metric", "en", "key")

        assertThat(result.isSuccessful, `is`(false))
        assertThat(result.code(), `is`(404))
    }
}