package com.example.weatherforecastapplication.homescreen.view

import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.weatherforecastapplication.data.models.ForecastResponseApi
import com.example.weatherforecastapplication.data.models.ResponseState
import com.example.weatherforecastapplication.homescreen.viewmodel.HomeViewModel
import com.example.weatherforecastapplication.ui.theme.component.SplashAnimation

@Composable
fun HomeScreen(viewModel: HomeViewModel) {
    val weatherState by viewModel.weatherState.collectAsState()

    // Adding top padding so it doesn't overlap with the system clock/battery
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(WindowInsets.statusBars.asPaddingValues())
    ) {
        when (weatherState) {
            is ResponseState.Loading -> {
                // Reusing your awesome Lottie animation for loading states!
                SplashAnimation(modifier = Modifier.align(Alignment.Center))
            }
            is ResponseState.Error -> {
                val errorMessage = (weatherState as ResponseState.Error).message
                Text(
                    text = "Oops! $errorMessage",
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.align(Alignment.Center)
                )
            }
            is ResponseState.Success -> {
                val weatherData = (weatherState as ResponseState.Success).data
                WeatherContent(weatherData = weatherData)
            }
        }
    }
}

@Composable
fun WeatherContent(weatherData: ForecastResponseApi) {
    // We will build this out fully next, but here is a quick test UI!
    val currentWeather = weatherData.list.firstOrNull()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = weatherData.city.name, // Displays city
            style = MaterialTheme.typography.displayLarge,
            color = MaterialTheme.colorScheme.primary
        )

        Text(
            text = "${currentWeather?.main?.temp}°", // Displays the current temperature
            style = MaterialTheme.typography.displayLarge,
            color = MaterialTheme.colorScheme.onBackground
        )

        Text(
            text = currentWeather?.weather?.firstOrNull()?.description ?: "Unknown", // Displays weather description
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.secondary
        )
    }
}