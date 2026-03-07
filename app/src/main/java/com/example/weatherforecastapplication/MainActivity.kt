package com.example.weatherforecastapplication

import android.Manifest
import android.app.AlarmManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.PowerManager
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.weatherforecastapplication.navigation.BottomNavigationBar
import com.example.weatherforecastapplication.navigation.ScreenRoute
import com.example.weatherforecastapplication.navigation.SetupNavHost
import com.example.weatherforecastapplication.ui.theme.WeatherForecastApplicationTheme
import com.example.weatherforecastapplication.ui.theme.component.GlobalWeatherBackground
import com.example.weatherforecastapplication.utils.NotificationUtils
import java.util.Calendar

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Initialize Notification Channel
        NotificationUtils.createNotificationChannel(this)

        setContent {
            // Keep permissions cleanly extracted
            RequestEssentialPermissions()

            // Reverted back to your simple, time-based Day/Night logic
            val currentHour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
            val isGlobalDayTime = currentHour in 6..18

            WeatherForecastApplicationTheme(isDayTime = isGlobalDayTime) {
                val navController = rememberNavController()
                val navBackStackEntry = navController.currentBackStackEntryAsState().value
                val currentRoute = navBackStackEntry?.destination?.route

                val showBottomBar = currentRoute !in listOf(
                    ScreenRoute.Splash.route,
                    ScreenRoute.MapSelection.route,
                    ScreenRoute.FavoriteDetails.route
                )

                GlobalWeatherBackground(isDayTime = isGlobalDayTime) {
                    Scaffold(
                        bottomBar = {
                            if (showBottomBar) {
                                BottomNavigationBar(navController = navController)
                            }
                        }, containerColor = Color.Transparent
                    ) { innerPadding ->
                        Surface(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(innerPadding),
                            color = Color.Transparent
                        ) {
                            SetupNavHost(navController = navController)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun RequestEssentialPermissions() {
    val context = LocalContext.current
    val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager
    val powerManager = context.getSystemService(Context.POWER_SERVICE) as? PowerManager

    val batteryLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) {}

    val overlayLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && powerManager?.isIgnoringBatteryOptimizations(
                    context.packageName
                ) == false
            ) {
                val intent = Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS).apply {
                    data = Uri.parse("package:${context.packageName}")
                }
                batteryLauncher.launch(intent)
            }
        }

    val alarmLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(context)) {
                val intent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION).apply {
                    data = Uri.parse("package:${context.packageName}")
                }
                overlayLauncher.launch(intent)
            }
        }

    val multiPermissionLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && alarmManager?.canScheduleExactAlarms() == false) {
                val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM).apply {
                    data = Uri.parse("package:${context.packageName}")
                }
                alarmLauncher.launch(intent)
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(
                    context
                )
            ) {
                val intent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION).apply {
                    data = Uri.parse("package:${context.packageName}")
                }
                overlayLauncher.launch(intent)
            }
        }

    LaunchedEffect(Unit) {
        val permissionsToRequest = mutableListOf(
            Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION
        )
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) permissionsToRequest.add(Manifest.permission.POST_NOTIFICATIONS)

        val ungranted = permissionsToRequest.filter {
            ContextCompat.checkSelfPermission(
                context, it
            ) != PackageManager.PERMISSION_GRANTED
        }

        if (ungranted.isNotEmpty()) {
            multiPermissionLauncher.launch(ungranted.toTypedArray())
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && alarmManager?.canScheduleExactAlarms() == false) {
                alarmLauncher.launch(Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM).apply {
                    data = Uri.parse("package:${context.packageName}")
                })
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(
                    context
                )
            ) {
                overlayLauncher.launch(Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION).apply {
                    data = Uri.parse("package:${context.packageName}")
                })
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && powerManager?.isIgnoringBatteryOptimizations(
                    context.packageName
                ) == false
            ) {
                batteryLauncher.launch(Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS).apply {
                    data = Uri.parse("package:${context.packageName}")
                })
            }
        }
    }
}