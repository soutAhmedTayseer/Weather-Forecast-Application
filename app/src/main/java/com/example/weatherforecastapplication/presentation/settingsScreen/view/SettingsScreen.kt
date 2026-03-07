package com.example.weatherforecastapplication.settingsScreen

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.weatherforecastapplication.R
import com.example.weatherforecastapplication.navigation.ScreenRoute
import com.example.weatherforecastapplication.ui.theme.component.RetroSnackbarHost
import com.example.weatherforecastapplication.ui.theme.component.SplashAnimation
import kotlinx.coroutines.flow.collectLatest

@Composable
fun SettingsScreen(viewModel: SettingsViewModel, navController: NavController) {
    val context = LocalContext.current
    val locationMethod by viewModel.locationMethod.collectAsState()
    val tempUnit by viewModel.tempUnit.collectAsState()
    val windUnit by viewModel.windUnit.collectAsState()
    val language by viewModel.language.collectAsState()

    val isTranslating by viewModel.isTranslating.collectAsState()

    val scrollState = rememberScrollState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.snackbarEvent.collectLatest { event ->
            val message = if (event.arg.isNotEmpty()) {
                context.getString(event.stringResId, event.arg)
            } else {
                context.getString(event.stringResId)
            }
            snackbarHostState.showSnackbar(message)
        }
    }

    val savedStateHandle = navController.currentBackStackEntry?.savedStateHandle
    val mapMessage by (savedStateHandle?.getStateFlow<String?>("snackbar_message", null)
        ?: kotlinx.coroutines.flow.MutableStateFlow(null)).collectAsState()

    LaunchedEffect(mapMessage) {
        mapMessage?.let {
            snackbarHostState.showSnackbar(it)
            savedStateHandle?.remove<String>("snackbar_message")
        }
    }

    val gpsStr = stringResource(id = R.string.gps)
    val mapStr = stringResource(id = R.string.map)
    val celsiusStr = stringResource(id = R.string.celsius)
    val fahrenheitStr = stringResource(id = R.string.fahrenheit)
    val kelvinStr = stringResource(id = R.string.kelvin)
    val msStr = stringResource(id = R.string.m_s)
    val mphStr = stringResource(id = R.string.mph)
    val engStr = stringResource(id = R.string.english)
    val arStr = stringResource(id = R.string.arabic)

    Scaffold(
        containerColor = Color.Transparent,
        snackbarHost = { RetroSnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 16.dp)
                    .verticalScroll(scrollState),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = stringResource(id = R.string.settings_title),
                    style = MaterialTheme.typography.displaySmall,
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.padding(vertical = 32.dp)
                )

                SettingsSection(title = stringResource(id = R.string.location_method)) {
                    RetroToggleGroup(
                        options = listOf(gpsStr, mapStr),
                        selectedOption = if (locationMethod == "gps") gpsStr else mapStr,
                        onOptionSelected = {
                            if (it == gpsStr) {
                                viewModel.setLocationMethod("gps")
                            } else {
                                navController.navigate(ScreenRoute.MapSelection.createRoute(isForHome = true))
                            }
                        }
                    )
                }

                SettingsSection(title = stringResource(id = R.string.temperature_unit)) {
                    RetroToggleGroup(
                        options = listOf(celsiusStr, fahrenheitStr, kelvinStr),
                        selectedOption = when (tempUnit) {
                            "imperial" -> fahrenheitStr
                            "standard" -> kelvinStr
                            else -> celsiusStr
                        },
                        onOptionSelected = {
                            val apiValue = when (it) {
                                fahrenheitStr -> "imperial"
                                kelvinStr -> "standard"
                                else -> "metric"
                            }
                            viewModel.setTempUnit(apiValue, it)
                        }
                    )
                }

                SettingsSection(title = stringResource(id = R.string.wind_speed_unit)) {
                    RetroToggleGroup(
                        options = listOf(msStr, mphStr),
                        selectedOption = if (windUnit == "mph") mphStr else msStr,
                        onOptionSelected = { viewModel.setWindUnit(if (it == mphStr) "mph" else "m/s", it) }
                    )
                }

                SettingsSection(title = stringResource(id = R.string.language)) {
                    RetroToggleGroup(
                        options = listOf(engStr, arStr),
                        selectedOption = if (language == "en") engStr else arStr,
                        onOptionSelected = { viewModel.setLanguage(if (it == engStr) "en" else "ar") }
                    )
                }
            }

            // Smoother Animation with Fades and Scaling
            AnimatedVisibility(
                visible = isTranslating,
                enter = fadeIn(animationSpec = tween(400)) + scaleIn(initialScale = 0.95f),
                exit = fadeOut(animationSpec = tween(400)) + scaleOut(targetScale = 0.95f)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.background)
                        .clickable(enabled = false) {}, // Intercepts touches while loading
                    contentAlignment = Alignment.Center
                ) {
                    SplashAnimation(modifier = Modifier.size(250.dp))
                }
            }
        }
    }
}

@Composable
fun SettingsSection(title: String, content: @Composable () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 24.dp)
            .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.95f), RoundedCornerShape(16.dp))
            .border(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f), RoundedCornerShape(16.dp))
            .padding(16.dp)
    ) {
        Text(text = title, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), color = MaterialTheme.colorScheme.onSurface, modifier = Modifier.padding(bottom = 12.dp))
        content()
    }
}

@Composable
fun RetroToggleGroup(options: List<String>, selectedOption: String, onOptionSelected: (String) -> Unit) {
    Row(modifier = Modifier.fillMaxWidth().border(2.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f), RoundedCornerShape(8.dp)).clip(RoundedCornerShape(8.dp))) {
        options.forEachIndexed { index, option ->
            val isSelected = option == selectedOption
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clickable { onOptionSelected(option) }
                    .background(if (isSelected) Color(0xFF74B9FF) else Color.Transparent)
                    .padding(vertical = 12.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(text = option, color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal, textAlign = TextAlign.Center)
            }
            if (index < options.size - 1) {
                Box(Modifier.width(2.dp).fillMaxHeight().background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f)))
            }
        }
    }
}