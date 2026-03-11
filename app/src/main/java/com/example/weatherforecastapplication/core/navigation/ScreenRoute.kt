package com.example.weatherforecastapplication.core.navigation

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import com.example.weatherforecastapplication.R

sealed class ScreenRoute(val route: String, @StringRes val titleResId: Int, @DrawableRes val icon: Int?) {
    object Splash : ScreenRoute("splash_screen", R.string.nav_splash, null)

    object Home : ScreenRoute("home_screen", R.string.nav_home, R.drawable.ic_home)
    object Favorites : ScreenRoute("favorites_screen", R.string.nav_favorites, R.drawable.ic_favorite)
    object Alerts : ScreenRoute("alerts_screen", R.string.nav_alerts, R.drawable.ic_alert)
    object Settings : ScreenRoute("settings_screen", R.string.nav_settings, R.drawable.ic_settings)

    object MapSelection : ScreenRoute("map_selection_screen/{isForHome}", R.string.nav_map, null) {
        fun createRoute(isForHome: Boolean) = "map_selection_screen/$isForHome"
    }
    object FavoriteDetails : ScreenRoute("favorite_details/{lat}/{lon}/{cityName}", R.string.nav_details, null) {
        fun createRoute(lat: Double, lon: Double, cityName: String) = "favorite_details/$lat/$lon/$cityName"
    }
}