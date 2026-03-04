package com.example.weatherforecastapplication.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.weatherforecastapplication.data.local.CityDatabase
import com.example.weatherforecastapplication.data.local.CityLocationLocalDataSourceImpl
import com.example.weatherforecastapplication.data.remote.RetrofitHelper
import com.example.weatherforecastapplication.data.remote.WeatherRemoteDataSourceImpl
import com.example.weatherforecastapplication.di.WeatherViewModelFactory
import com.example.weatherforecastapplication.favoritesscreen.view.FavoriteDetailsScreen
import com.example.weatherforecastapplication.favoritesscreen.view.FavoritesScreen
import com.example.weatherforecastapplication.favoritesscreen.viewmodel.FavoritesViewModel
import com.example.weatherforecastapplication.homescreen.view.HomeScreen
import com.example.weatherforecastapplication.homescreen.viewmodel.HomeViewModel
import com.example.weatherforecastapplication.mapscreen.view.MapSelectionScreen
import com.example.weatherforecastapplication.mapscreen.viewmodel.MapViewModel
import com.example.weatherforecastapplication.repository.SettingsRepository
import com.example.weatherforecastapplication.repository.WeatherRepositoryImpl
import com.example.weatherforecastapplication.settingsScreen.SettingsScreen
import com.example.weatherforecastapplication.settingsScreen.SettingsViewModel
import com.example.weatherforecastapplication.splashScreen.SplashScreen

@Composable
fun SetupNavHost(navController: NavHostController, modifier: Modifier = Modifier) {

    // --- MANUAL DEPENDENCY INJECTION SETUP ---
    val context = LocalContext.current
    val database = CityDatabase.getDatabase(context)
    val localDataSource = CityLocationLocalDataSourceImpl(database.cityLocationDao())
    val remoteDataSource = WeatherRemoteDataSourceImpl(RetrofitHelper.weatherApiService)

    val repository = WeatherRepositoryImpl(remoteDataSource, localDataSource, database.weatherDao())

    // 1. INITIALIZE SETTINGS REPOSITORY
    val settingsRepository = SettingsRepository(context)

    // 2. PASS BOTH REPOSITORIES TO FACTORY (Fixes the compile crash!)
    val factory = WeatherViewModelFactory(repository, settingsRepository)
    // -----------------------------------------

    NavHost(
        navController = navController,
        startDestination = ScreenRoute.Splash.route,
        modifier = modifier
    ) {
        composable(ScreenRoute.Splash.route) { SplashScreen(navController) }

        composable(ScreenRoute.Home.route) {
            val homeViewModel: HomeViewModel = viewModel(factory = factory)
            HomeScreen(viewModel = homeViewModel)
        }

        composable(ScreenRoute.Favorites.route) {
            val favViewModel: FavoritesViewModel = viewModel(factory = factory)
            FavoritesScreen(viewModel = favViewModel, navController = navController)
        }

        composable(
            route = ScreenRoute.FavoriteDetails.route,
            arguments = listOf(
                navArgument("lat") { type = NavType.FloatType },
                navArgument("lon") { type = NavType.FloatType },
                navArgument("cityName") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val lat = backStackEntry.arguments?.getFloat("lat")?.toDouble() ?: 0.0
            val lon = backStackEntry.arguments?.getFloat("lon")?.toDouble() ?: 0.0
            val cityName = backStackEntry.arguments?.getString("cityName") ?: ""

            val detailsViewModel: HomeViewModel = viewModel(factory = factory)
            FavoriteDetailsScreen(
                viewModel = detailsViewModel,
                navController = navController,
                lat = lat,
                lon = lon,
                cityName = cityName
            )
        }

        // 3. SETTINGS ROUTE RESTORED (Passes navController properly)
        composable(ScreenRoute.Settings.route) {
            val settingsViewModel: SettingsViewModel = viewModel(factory = factory)
            SettingsScreen(viewModel = settingsViewModel, navController = navController)
        }

        composable(ScreenRoute.Alerts.route) { PlaceholderScreen("Alerts Screen") }

        // MAP ROUTE (Handles both Favorites picking and Settings picking)
        composable(
            route = ScreenRoute.MapSelection.route,
            arguments = listOf(navArgument("isForHome") { type = NavType.BoolType })
        ) { backStackEntry ->
            val isForHome = backStackEntry.arguments?.getBoolean("isForHome") ?: false

            val mapViewModel: MapViewModel = viewModel(factory = factory)
            val favViewModel: FavoritesViewModel = viewModel(factory = factory)
            val settingsViewModel: SettingsViewModel = viewModel(factory = factory)

            MapSelectionScreen(
                viewModel = mapViewModel,
                navController = navController,
                onLocationSaved = { lat, lon, name ->
                    if (isForHome) {
                        settingsViewModel.saveHomeLocationFromMap(lat, lon, name)
                    } else {
                        favViewModel.saveLocation(lat, lon, name)
                    }
                }
            )
        }
    }
}

@Composable
fun PlaceholderScreen(title: String) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(text = title)
    }
}