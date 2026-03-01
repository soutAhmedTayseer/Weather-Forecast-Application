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
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.weatherforecastapplication.data.local.CityDatabase
import com.example.weatherforecastapplication.data.local.CityLocationLocalDataSourceImpl
import com.example.weatherforecastapplication.data.remote.RetrofitHelper
import com.example.weatherforecastapplication.data.remote.WeatherRemoteDataSourceImpl
import com.example.weatherforecastapplication.di.WeatherViewModelFactory
import com.example.weatherforecastapplication.homescreen.view.HomeScreen
import com.example.weatherforecastapplication.homescreen.viewmodel.HomeViewModel
import com.example.weatherforecastapplication.repository.WeatherRepositoryImpl
import com.example.weatherforecastapplication.ui.splash.SplashScreen

@Composable
fun SetupNavHost(navController: NavHostController, modifier: Modifier = Modifier) {

    // --- MANUAL DEPENDENCY INJECTION SETUP ---
    val context = LocalContext.current
    val database = CityDatabase.getDatabase(context)
    val localDataSource = CityLocationLocalDataSourceImpl(database.cityLocationDao())
    val remoteDataSource = WeatherRemoteDataSourceImpl(RetrofitHelper.weatherApiService)
    val repository = WeatherRepositoryImpl(remoteDataSource, localDataSource)
    val factory = WeatherViewModelFactory(repository)
    // -----------------------------------------

    NavHost(
        navController = navController,
        startDestination = ScreenRoute.Splash.route,
        modifier = modifier
    ) {
        composable(ScreenRoute.Splash.route) { SplashScreen(navController) }

        composable(ScreenRoute.Home.route) {
            // Get the ViewModel using our factory
            val homeViewModel: HomeViewModel = viewModel(factory = factory)
            HomeScreen(viewModel = homeViewModel)
        }

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