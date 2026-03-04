package com.example.weatherforecastapplication.settingsScreen

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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.weatherforecastapplication.navigation.ScreenRoute
import com.example.weatherforecastapplication.ui.theme.component.RetroSnackbarHost
import kotlinx.coroutines.flow.collectLatest

@Composable
fun SettingsScreen(viewModel: SettingsViewModel, navController: NavController) {
    val locationMethod by viewModel.locationMethod.collectAsState()
    val tempUnit by viewModel.tempUnit.collectAsState()
    val windUnit by viewModel.windUnit.collectAsState()
    val language by viewModel.language.collectAsState()

    val scrollState = rememberScrollState()
    val snackbarHostState = remember { SnackbarHostState() }

    // Listen for Snackbar events from the ViewModel (for Unit/Language changes)
    LaunchedEffect(Unit) {
        viewModel.snackbarEvent.collectLatest { message ->
            snackbarHostState.showSnackbar(message)
        }
    }

    // NEW: Listen for messages passed back from the Map Screen securely!
    val savedStateHandle = navController.currentBackStackEntry?.savedStateHandle

    // Use MutableStateFlow(null) as the fallback so the types match perfectly
    val mapMessage by (savedStateHandle?.getStateFlow<String?>("snackbar_message", null)
        ?: kotlinx.coroutines.flow.MutableStateFlow(null)).collectAsState()

    LaunchedEffect(mapMessage) {
        mapMessage?.let {
            snackbarHostState.showSnackbar(it)
            savedStateHandle?.remove<String>("snackbar_message") // Clear it
        }
    }

    Scaffold(
        containerColor = Color.Transparent,
        snackbarHost = { RetroSnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize() // SCALABILITY: Fills the entire screen
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
                .verticalScroll(scrollState),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Settings",
                style = MaterialTheme.typography.displaySmall,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.padding(vertical = 32.dp)
            )

            // 1. Location Method
            SettingsSection(title = "Location Method") {
                RetroToggleGroup(
                    options = listOf("GPS", "Map"),
                    selectedOption = if (locationMethod == "gps") "GPS" else "Map",
                    onOptionSelected = {
                        if (it == "GPS") {
                            viewModel.setLocationMethod("gps")
                        } else {
                            // Navigate to Map Selection with flag indicating it's for Home settings
                            navController.navigate(ScreenRoute.MapSelection.createRoute(isForHome = true))
                        }
                    }
                )
            }

            // 2. Temperature Unit
            SettingsSection(title = "Temperature Unit") {
                RetroToggleGroup(
                    options = listOf("Celsius", "Fahrenheit", "Kelvin"),
                    selectedOption = when (tempUnit) {
                        "imperial" -> "Fahrenheit"
                        "standard" -> "Kelvin"
                        else -> "Celsius"
                    },
                    onOptionSelected = {
                        val apiValue = when (it) {
                            "Fahrenheit" -> "imperial"
                            "Kelvin" -> "standard"
                            else -> "metric"
                        }
                        viewModel.setTempUnit(apiValue, it)
                    }
                )
            }

            // 3. Wind Speed Unit
            SettingsSection(title = "Wind Speed Unit") {
                RetroToggleGroup(
                    options = listOf("m/s", "mph"),
                    selectedOption = windUnit,
                    onOptionSelected = { viewModel.setWindUnit(it) }
                )
            }

            // 4. Language
            SettingsSection(title = "Language") {
                RetroToggleGroup(
                    options = listOf("English", "Arabic"),
                    selectedOption = if (language == "en") "English" else "Arabic",
                    onOptionSelected = { viewModel.setLanguage(if (it == "English") "en" else "ar") }
                )
            }
        }
    }
}

// --- REUSABLE RETRO UI COMPONENTS ---

@Composable
fun SettingsSection(title: String, content: @Composable () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth() // SCALABILITY
            .padding(bottom = 24.dp)
            .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.95f), RoundedCornerShape(16.dp))
            .border(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f), RoundedCornerShape(16.dp))
            .padding(16.dp)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(bottom = 12.dp)
        )
        content()
    }
}

@Composable
fun RetroToggleGroup(
    options: List<String>,
    selectedOption: String,
    onOptionSelected: (String) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth() // SCALABILITY
            .border(2.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f), RoundedCornerShape(8.dp))
            .clip(RoundedCornerShape(8.dp))
    ) {
        options.forEachIndexed { index, option ->
            val isSelected = option == selectedOption
            Box(
                modifier = Modifier
                    .weight(1f) // SCALABILITY: Ensures buttons split width evenly
                    .clickable { onOptionSelected(option) }
                    .background(if (isSelected) Color(0xFF74B9FF) else Color.Transparent)
                    .padding(vertical = 12.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = option,
                    color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                    textAlign = TextAlign.Center
                )
            }

            if (index < options.size - 1) {
                Box(
                    modifier = Modifier
                        .width(2.dp)
                        .fillMaxHeight()
                        .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f))
                )
            }
        }
    }
}