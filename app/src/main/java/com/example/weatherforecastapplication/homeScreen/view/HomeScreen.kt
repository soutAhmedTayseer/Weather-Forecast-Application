package com.example.weatherforecastapplication.homescreen.view

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.core.content.ContextCompat
import com.example.weatherforecastapplication.R
import com.example.weatherforecastapplication.data.models.ResponseState
import com.example.weatherforecastapplication.homescreen.viewmodel.HomeViewModel
import com.example.weatherforecastapplication.ui.theme.component.SplashAnimation
import com.example.weatherforecastapplication.ui.theme.component.WeatherDetailsLayout
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

    var isRefreshing by remember { mutableStateOf(false) }
    var showRefreshAnimation by remember { mutableStateOf(false) }

    // --- REAL GPS FETCHING LOGIC ---
    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }

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

    // Whenever "locationMethod" changes to "gps", grab the real location!
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
    // -------------------------------

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

    LaunchedEffect(isRefreshing) {
        if (isRefreshing) {
            showRefreshAnimation = true
            viewModel.fetchHomeWeatherAutomatically()
            delay(2000)
            isRefreshing = false
            showRefreshAnimation = false
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        PullToRefreshBox(
            isRefreshing = isRefreshing,
            onRefresh = { isRefreshing = true },
            modifier = Modifier.fillMaxSize().padding(WindowInsets.statusBars.asPaddingValues())
        ) {
            if (showRefreshAnimation) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { SplashAnimation() }
            } else {
                when (weatherState) {
                    is ResponseState.Loading -> SplashAnimation(modifier = Modifier.align(Alignment.Center))
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
    }
}