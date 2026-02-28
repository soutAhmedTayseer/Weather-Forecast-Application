package com.example.weatherforecastapplication.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Settings
import androidx.compose.ui.graphics.vector.ImageVector

sealed class ScreenRoute(val route: String, val title: String, val icon: ImageVector) {
    object Home : ScreenRoute("home_screen", "Home", Icons.Default.Home)
    object Favorites : ScreenRoute("favorites_screen", "Favorites", Icons.Default.Favorite)
    object Alerts : ScreenRoute("alerts_screen", "Alerts", Icons.Default.Notifications)
    object Settings : ScreenRoute("settings_screen", "Settings", Icons.Default.Settings)
}