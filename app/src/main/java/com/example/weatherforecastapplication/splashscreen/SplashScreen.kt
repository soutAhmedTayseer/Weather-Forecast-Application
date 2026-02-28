package com.example.weatherforecastapplication.ui.splash

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.weatherforecastapplication.navigation.ScreenRoute
import com.example.weatherforecastapplication.ui.theme.component.SplashAnimation
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(navController: NavController) {
    LaunchedEffect(key1 = true) {
        delay(3000)
        navController.navigate(ScreenRoute.Home.route) {
            popUpTo(ScreenRoute.Splash.route) { inclusive = true }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        SplashAnimation()

        Spacer(modifier = Modifier.height(24.dp))

        // --- THE NEW SLOGAN ---
        // "Catching Clouds & Sunshine!"
        // "Your Pocket Sky Buddy!"
        Text(
            text = "Catching Clouds & Sunshine!",
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.primary
        )
    }
}