package com.example.weatherforecastapplication.presentation.settingsScreen.view

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.weatherforecastapplication.R
import com.example.weatherforecastapplication.core.navigation.ScreenRoute
import com.example.weatherforecastapplication.core.theme.component.RetroBorderWidth
import com.example.weatherforecastapplication.core.theme.component.RetroCard
import com.example.weatherforecastapplication.core.theme.component.RetroCornerShape
import com.example.weatherforecastapplication.core.theme.component.RetroSnackbarHost
import com.example.weatherforecastapplication.core.theme.component.RetroTopAppBar
import com.example.weatherforecastapplication.core.theme.component.SplashAnimation
import com.example.weatherforecastapplication.presentation.settingsScreen.viewmodel.SettingsViewModel
import kotlinx.coroutines.flow.MutableStateFlow
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
            val message = if (event.arg.isNotEmpty()) context.getString(event.stringResId, event.arg) else context.getString(event.stringResId)
            snackbarHostState.showSnackbar(message)
        }
    }

    val savedStateHandle = navController.currentBackStackEntry?.savedStateHandle
    val mapMessage by (savedStateHandle?.getStateFlow<String?>("snackbar_message", null) ?: MutableStateFlow(null)).collectAsState()

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
        snackbarHost = { RetroSnackbarHost(snackbarHostState) },
        topBar = { RetroTopAppBar(title = stringResource(id = R.string.settings_title)) }
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 16.dp)
                    .verticalScroll(scrollState),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                SettingsSection(title = stringResource(id = R.string.location_method)) {
                    RetroToggleGroup(
                        options = listOf(gpsStr, mapStr),
                        selectedOption = if (locationMethod == "gps") gpsStr else mapStr,
                        onOptionSelected = {
                            if (it == gpsStr) viewModel.setLocationMethod("gps")
                            else navController.navigate(ScreenRoute.MapSelection.createRoute(isForHome = true))
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
                            val apiUnit = when (it) {
                                fahrenheitStr -> "imperial"
                                kelvinStr -> "standard"
                                else -> "metric"
                            }
                            viewModel.setTempUnit(apiUnit, it)
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
                Spacer(modifier = Modifier.height(100.dp))
            }

            AnimatedVisibility(
                visible = isTranslating,
                enter = fadeIn(animationSpec = tween(400)) + scaleIn(initialScale = 0.95f),
                exit = fadeOut(animationSpec = tween(400)) + scaleOut(targetScale = 0.95f)
            ) {
                Box(
                    modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background.copy(alpha = 0.8f)).clickable(enabled = false) {},
                    contentAlignment = Alignment.Center
                ) {
                    SplashAnimation(modifier = Modifier.size(250.dp))
                }
            }
        }
    }
}

// DRY: Reusing RetroCard for uniform blocky corners
@Composable
fun SettingsSection(title: String, content: @Composable () -> Unit) {
    RetroCard(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(bottom = 16.dp)
            )
            content()
        }
    }
}

// UI UPGRADE: Uses RetroCornerShape, RetroBorderWidth, and custom pixel fonts
@Composable
fun RetroToggleGroup(options: List<String>, selectedOption: String, onOptionSelected: (String) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .border(RetroBorderWidth, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f), RetroCornerShape)
            .clip(RetroCornerShape)
    ) {
        options.forEachIndexed { index, option ->
            val isSelected = option == selectedOption
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clickable { onOptionSelected(option) }
                    .background(if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent)
                    .padding(vertical = 12.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = option,
                    color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
                    style = MaterialTheme.typography.labelMedium, // Forces Pixel Font
                    textAlign = TextAlign.Center
                )
            }
            // Divider between buttons
            if (index < options.size - 1) {
                Box(Modifier.width(RetroBorderWidth).fillMaxHeight().background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f)))
            }
        }
    }
}