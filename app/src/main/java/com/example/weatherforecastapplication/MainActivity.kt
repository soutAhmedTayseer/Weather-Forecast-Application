package com.example.weatherforecastapplication

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.weatherforecastapplication.navigation.BottomNavigationBar
import com.example.weatherforecastapplication.navigation.ScreenRoute
import com.example.weatherforecastapplication.navigation.SetupNavHost
import com.example.weatherforecastapplication.ui.theme.WeatherForecastApplicationTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            // Applies your Day/Night palette and Baloo font globally
            WeatherForecastApplicationTheme(isDayTime = true) { // We'll make this dynamic later!
                val navController = rememberNavController()

                // Track current screen to hide bottom bar on Splash
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentRoute = navBackStackEntry?.destination?.route
                val showBottomBar = currentRoute != ScreenRoute.Splash.route

                Scaffold(
                    bottomBar = {
                        if (showBottomBar) {
                            BottomNavigationBar(navController = navController)
                        }
                    }
                ) { innerPadding ->
                    Surface(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding),
                        color = MaterialTheme.colorScheme.background
                    ) {
                        SetupNavHost(navController = navController)
                    }
                }
            }
        }
    }
}