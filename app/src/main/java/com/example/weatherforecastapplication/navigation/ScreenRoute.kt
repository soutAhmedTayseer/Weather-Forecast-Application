package com.example.weatherforecastapplication.navigation

import androidx.annotation.DrawableRes
import com.example.weatherforecastapplication.R // Make sure to import your R class!

sealed class ScreenRoute(val route: String, val title: String, @DrawableRes val icon: Int?) {
    object Splash : ScreenRoute("splash_screen", "Splash", null)

    object Home : ScreenRoute("home_screen", "Home", R.drawable.ic_home)
    object Favorites : ScreenRoute("favorites_screen", "Favorites", R.drawable.ic_favorite)
    object Alerts : ScreenRoute("alerts_screen", "Alerts", R.drawable.ic_alert)
    object Settings : ScreenRoute("settings_screen", "Settings", R.drawable.ic_settings)
    object MapSelection : ScreenRoute("map_selection_screen", "Map", null)
    object FavoriteDetails : ScreenRoute("favorite_details/{lat}/{lon}/{cityName}", "Details", null) {
        fun createRoute(lat: Double, lon: Double, cityName: String) = "favorite_details/$lat/$lon/$cityName"
    }}