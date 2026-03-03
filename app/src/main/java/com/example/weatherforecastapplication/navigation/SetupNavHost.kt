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
import com.example.weatherforecastapplication.homescreen.view.HomeScreen
import com.example.weatherforecastapplication.homescreen.viewmodel.HomeViewModel
import com.example.weatherforecastapplication.repository.WeatherRepositoryImpl
import com.example.weatherforecastapplication.splashScreen.SplashScreen
import com.example.weatherforecastapplication.favoritesscreen.view.FavoritesScreen
import com.example.weatherforecastapplication.favoritesscreen.viewmodel.FavoritesViewModel
import com.example.weatherforecastapplication.mapscreen.view.MapSelectionScreen
import com.example.weatherforecastapplication.mapscreen.viewmodel.MapViewModel

@Composable
fun SetupNavHost(navController: NavHostController, modifier: Modifier = Modifier) {

    // --- MANUAL DEPENDENCY INJECTION SETUP ---
    val context = LocalContext.current
    val database = CityDatabase.getDatabase(context)
    val localDataSource = CityLocationLocalDataSourceImpl(database.cityLocationDao())
    val remoteDataSource = WeatherRemoteDataSourceImpl(RetrofitHelper.weatherApiService)

    // 1. Pass the database.weatherDao() into the repository here!
    val repository = WeatherRepositoryImpl(remoteDataSource, localDataSource, database.weatherDao())

    val factory = WeatherViewModelFactory(repository)
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

        composable(ScreenRoute.MapSelection.route) {
            val mapViewModel: MapViewModel = viewModel(factory = factory)
            // We also need the FavoritesViewModel here to save the data!
            val favViewModel: FavoritesViewModel = viewModel(factory = factory)

            MapSelectionScreen(
                viewModel = mapViewModel,
                navController = navController,
                onLocationSaved = { lat, lon, name ->
                    favViewModel.saveLocation(lat, lon, name)
                })
        }

        composable(
            route = ScreenRoute.FavoriteDetails.route,
            arguments = listOf(
                navArgument("lat") { type = NavType.FloatType },
                navArgument("lon") { type = NavType.FloatType },
                navArgument("cityName") { type = NavType.StringType })) { backStackEntry ->
            // Extract the arguments
            val lat = backStackEntry.arguments?.getFloat("lat")?.toDouble() ?: 0.0
            val lon = backStackEntry.arguments?.getFloat("lon")?.toDouble() ?: 0.0
            val cityName = backStackEntry.arguments?.getString("cityName") ?: ""

            // Reuse the HomeViewModel for fetching data!
            val detailsViewModel: HomeViewModel = viewModel(factory = factory)

            FavoriteDetailsScreen(
                viewModel = detailsViewModel,
                navController = navController,
                lat = lat,
                lon = lon,
                cityName = cityName
            )
        }

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