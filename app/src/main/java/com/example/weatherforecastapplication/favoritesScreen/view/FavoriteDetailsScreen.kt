package com.example.weatherforecastapplication.favoritesscreen.view

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.weatherforecastapplication.data.models.ResponseState
import com.example.weatherforecastapplication.homescreen.viewmodel.HomeViewModel
import com.example.weatherforecastapplication.ui.theme.component.SplashAnimation
import com.example.weatherforecastapplication.ui.theme.component.WeatherDetailsLayout
import kotlinx.coroutines.delay

@Composable
fun FavoriteDetailsScreen(
    viewModel: HomeViewModel,
    navController: NavController,
    lat: Double,
    lon: Double,
    cityName: String
) {
    val weatherState by viewModel.weatherState.collectAsState()

    // Collect the dynamic settings!
    val tempUnit by viewModel.tempUnitFlow.collectAsState()
    val windUnit by viewModel.windUnitFlow.collectAsState()

    var liveCurrentTimeMillis by remember { mutableLongStateOf(System.currentTimeMillis()) }
    LaunchedEffect(Unit) {
        while (true) {
            delay(1000L)
            liveCurrentTimeMillis = System.currentTimeMillis()
        }
    }

    // Fetch data for the specific favorite location when screen opens
    LaunchedEffect(Unit) {
        viewModel.getWeatherData(lat, lon)
    }

    Box(modifier = Modifier.fillMaxSize()) {
        when (weatherState) {
            is ResponseState.Loading -> {
                SplashAnimation(modifier = Modifier.align(Alignment.Center))
            }
            is ResponseState.Error -> {
                Text("Error Loading Data", modifier = Modifier.align(Alignment.Center))
            }
            is ResponseState.Success -> {
                val weatherData = (weatherState as ResponseState.Success).data
                val localTimeInSeconds = (liveCurrentTimeMillis / 1000L) + weatherData.city.timezone
                val localSunrise = weatherData.city.sunrise + weatherData.city.timezone
                val localSunset = weatherData.city.sunset + weatherData.city.timezone
                val isDay = localTimeInSeconds in localSunrise..localSunset

                // Pass the dynamic units to the UI
                WeatherDetailsLayout(
                    weatherData = weatherData,
                    liveTime = liveCurrentTimeMillis,
                    isDay = isDay,
                    tempUnit = tempUnit,
                    windUnit = windUnit
                )
            }
        }

        // Custom Retro Back Button Overlay
        FilledIconButton(
            onClick = { navController.popBackStack() },
            modifier = Modifier
                .padding(top = 48.dp, start = 16.dp)
                .align(Alignment.TopStart),
            colors = IconButtonDefaults.filledIconButtonColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.8f))
        ) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = MaterialTheme.colorScheme.onSurface)
        }
    }
}