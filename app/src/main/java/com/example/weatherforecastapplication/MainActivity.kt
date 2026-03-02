package com.example.weatherforecastapplication

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.weatherforecastapplication.navigation.BottomNavigationBar
import com.example.weatherforecastapplication.navigation.ScreenRoute
import com.example.weatherforecastapplication.navigation.SetupNavHost
import com.example.weatherforecastapplication.ui.theme.WeatherForecastApplicationTheme
import com.example.weatherforecastapplication.ui.theme.component.GlobalWeatherBackground
import java.util.Calendar

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            // Calculate a default global day/night based on the user's actual clock
            val currentHour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
            val isGlobalDayTime = currentHour in 6..18 // 6 AM to 6 PM

            WeatherForecastApplicationTheme(isDayTime = isGlobalDayTime) {
                val navController = rememberNavController()
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentRoute = navBackStackEntry?.destination?.route
                val showBottomBar = currentRoute != ScreenRoute.Splash.route

                // WRAP EVERYTHING IN YOUR NEW GLOBAL BACKGROUND
                GlobalWeatherBackground(isDayTime = isGlobalDayTime) {
                    Scaffold(
                        // Make Scaffold background transparent so the Lottie animation shows through!
                        containerColor = Color.Transparent,
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
                            color = Color.Transparent // Surface must also be transparent
                        ) {
                            SetupNavHost(navController = navController)
                        }
                    }
                }
            }
        }
    }
}