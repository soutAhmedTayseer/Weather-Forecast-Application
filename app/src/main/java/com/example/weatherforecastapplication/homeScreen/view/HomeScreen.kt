package com.example.weatherforecastapplication.homescreen.view

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.example.weatherforecastapplication.R
import com.example.weatherforecastapplication.data.models.ResponseState
import com.example.weatherforecastapplication.homescreen.viewmodel.HomeViewModel
import com.example.weatherforecastapplication.ui.theme.component.SplashAnimation
import com.example.weatherforecastapplication.ui.theme.component.WeatherDetailsLayout
import kotlinx.coroutines.delay
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(viewModel: HomeViewModel) {
    val weatherState by viewModel.weatherState.collectAsState()
    var isRefreshing by remember { mutableStateOf(false) }
    var showRefreshAnimation by remember { mutableStateOf(false) }

    val currentLanguage = Locale.getDefault().language

    // LIVE TICKING CLOCK STATE
    var liveCurrentTimeMillis by remember { mutableLongStateOf(System.currentTimeMillis()) }
    LaunchedEffect(Unit) {
        while (true) {
            delay(1000L)
            liveCurrentTimeMillis = System.currentTimeMillis()
        }
    }

    LaunchedEffect(isRefreshing) {
        if (isRefreshing) {
            showRefreshAnimation = true
            viewModel.getWeatherData(31.2001, 29.9187, "metric", currentLanguage)
            delay(2000)
            isRefreshing = false
            showRefreshAnimation = false
        }
    }

    LaunchedEffect(Unit) {
        if (weatherState is ResponseState.Loading) {
            viewModel.getWeatherData(31.2001, 29.9187, "metric", currentLanguage)
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        PullToRefreshBox(
            isRefreshing = isRefreshing,
            onRefresh = { isRefreshing = true },
            modifier = Modifier
                .fillMaxSize()
                .padding(WindowInsets.statusBars.asPaddingValues())
        ) {
            if (showRefreshAnimation) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    SplashAnimation()
                }
            } else {
                when (weatherState) {
                    is ResponseState.Loading -> {
                        SplashAnimation(modifier = Modifier.align(Alignment.Center))
                    }
                    is ResponseState.Error -> {
                        val errorMessage = (weatherState as ResponseState.Error).message
                        Text(
                            text = "${stringResource(id = R.string.error_prefix)} $errorMessage",
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.titleLarge,
                            modifier = Modifier.align(Alignment.Center)
                        )
                    }
                    is ResponseState.Success -> {
                        val weatherData = (weatherState as ResponseState.Success).data

                        val currentUtcTime = liveCurrentTimeMillis / 1000L
                        val localTimeInSeconds = currentUtcTime + weatherData.city.timezone
                        val localSunrise = weatherData.city.sunrise + weatherData.city.timezone
                        val localSunset = weatherData.city.sunset + weatherData.city.timezone

                        // Calculate Day/Night Status
                        val isDay = localTimeInSeconds in localSunrise..localSunset

                        // Simply call the highly-reusable UI component!
                        WeatherDetailsLayout(
                            weatherData = weatherData,
                            liveTime = liveCurrentTimeMillis,
                            isDay = isDay
                        )
                    }
                }
            }
        }
    }
}