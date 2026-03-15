package com.example.weatherforecastapplication.presentation.homeScreen.view

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.example.weatherforecastapplication.R
import com.example.weatherforecastapplication.data.models.stateManagement.ResponseState
import com.example.weatherforecastapplication.presentation.homeScreen.viewmodel.HomeViewModel
import com.example.weatherforecastapplication.core.theme.component.ScreenLoadingAnimation
import com.example.weatherforecastapplication.core.theme.component.SolidSwipeRefreshLayout
import com.example.weatherforecastapplication.core.theme.component.WeatherDetailsLayout
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(viewModel: HomeViewModel) {
    val context = LocalContext.current
    val weatherState by viewModel.weatherState.collectAsState()

    val tempUnit by viewModel.tempUnitFlow.collectAsState()
    val windUnit by viewModel.windUnitFlow.collectAsState()
    val locationMethod by viewModel.locationMethodFlow.collectAsState()

    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }

    val homeLoadingMessage = stringResource(id = R.string.fetching_local_weather)
    var isInitialLoading by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        delay(3000L)
        isInitialLoading = false
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        if (permissions.getOrDefault(Manifest.permission.ACCESS_FINE_LOCATION, false) ||
            permissions.getOrDefault(Manifest.permission.ACCESS_COARSE_LOCATION, false)) {
            try {
                fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                    location?.let {
                        viewModel.updateGpsLocation(it.latitude, it.longitude)
                    }
                }
            } catch (e: SecurityException) { e.printStackTrace() }
        }
    }

    LaunchedEffect(locationMethod) {
        if (locationMethod == "gps") {
            val hasFine = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
            val hasCoarse = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED

            if (hasFine || hasCoarse) {
                try {
                    fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                        location?.let {
                            viewModel.updateGpsLocation(it.latitude, it.longitude)
                        }
                    }
                } catch (e: SecurityException) { e.printStackTrace() }
            } else {
                permissionLauncher.launch(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION))
            }
        }
    }

    var liveCurrentTimeMillis by remember { mutableLongStateOf(System.currentTimeMillis()) }

    LaunchedEffect(Unit) {
        while (true) {
            delay(1000L)
            liveCurrentTimeMillis = System.currentTimeMillis()
        }
    }

    LaunchedEffect(Unit) {
        viewModel.fetchHomeWeatherAutomatically()
    }

    SolidSwipeRefreshLayout(
        onRefresh = { viewModel.fetchHomeWeatherAutomatically() },
        loadingMessage = homeLoadingMessage,
        gifRes = R.drawable.finnloading,
        modifier = Modifier.fillMaxSize().padding(WindowInsets.statusBars.asPaddingValues())
    ) {
        Box(modifier = Modifier.fillMaxSize().animateContentSize()) {

            if (isInitialLoading || weatherState is ResponseState.Loading) {
                ScreenLoadingAnimation(message = homeLoadingMessage, gifRes = R.drawable.finnloading)
            }
            else if (weatherState is ResponseState.Error) {
                val errorMessage = (weatherState as ResponseState.Error).message
                Text(
                    text = "${stringResource(id = R.string.error_prefix)} $errorMessage",
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.align(Alignment.Center).padding(16.dp)
                )
            }
            else if (weatherState is ResponseState.Success) {
                val weatherData = (weatherState as ResponseState.Success).data
                val currentUtcTime = liveCurrentTimeMillis / 1000L
                val localTimeInSeconds = currentUtcTime + weatherData.city.timezone
                val localSunrise = weatherData.city.sunrise + weatherData.city.timezone
                val localSunset = weatherData.city.sunset + weatherData.city.timezone
                val isDay = localTimeInSeconds in localSunrise..localSunset

                WeatherDetailsLayout(
                    weatherData = weatherData,
                    liveTime = liveCurrentTimeMillis,
                    isDay = isDay,
                    tempUnit = tempUnit,
                    windUnit = windUnit
                )
            }
        }
    }
}