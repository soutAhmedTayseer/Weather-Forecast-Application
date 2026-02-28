package com.example.weatherforecastapplication.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val NightColorScheme = darkColorScheme(
    primary = NightStarYellow,
    secondary = NightPurple,
    background = NightDeepNavy,
    surface = NightCardBackground,
    onPrimary = NightDeepNavy,
    onBackground = NightTextLight,
    onSurface = NightTextLight,
    error = CartoonRedError
)

private val DayColorScheme = lightColorScheme(
    primary = DaySkyBlue,
    secondary = DaySunYellow,
    background = DayCloudWhite,
    surface = DayCardBackground,
    onPrimary = DayTextDark,
    onBackground = DayTextDark,
    onSurface = DayTextDark,
    error = CartoonRedError
)

@Composable
fun WeatherForecastApplicationTheme(
    // We will hook this up to OpenWeatherMap sunset/sunrise data later!
    isDayTime: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = if (isDayTime) DayColorScheme else NightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}