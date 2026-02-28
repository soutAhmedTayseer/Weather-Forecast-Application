package com.example.weatherforecastapplication.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.weatherforecastapplication.ui.splash.SplashScreen

@Composable
fun SetupNavHost(navController: NavHostController, modifier: Modifier = Modifier) {
    NavHost(
        navController = navController,
        startDestination = ScreenRoute.Splash.route, // Start at Splash!
        modifier = modifier
    ) {
        composable(ScreenRoute.Splash.route) { SplashScreen(navController) }
        composable(ScreenRoute.Home.route) { PlaceholderScreen("Home Screen") }
        composable(ScreenRoute.Favorites.route) { PlaceholderScreen("Favorites Screen") }
        composable(ScreenRoute.Alerts.route) { PlaceholderScreen("Alerts Screen") }
        composable(ScreenRoute.Settings.route) { PlaceholderScreen("Settings Screen") }
    }
}

@Composable
fun PlaceholderScreen(title: String) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(text = title)
    }
}