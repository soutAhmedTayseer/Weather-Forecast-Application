package com.example.weatherforecastapplication.homescreen.view

import androidx.annotation.DrawableRes
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.weatherforecastapplication.data.models.ForecastResponseApi
import com.example.weatherforecastapplication.data.models.ResponseState
import com.example.weatherforecastapplication.homescreen.viewmodel.HomeViewModel
import com.example.weatherforecastapplication.ui.theme.component.SplashAnimation
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import com.example.weatherforecastapplication.R

@Composable
fun HomeScreen(viewModel: HomeViewModel) {
    val weatherState by viewModel.weatherState.collectAsState()

    // Adding top padding so it doesn't overlap with the system clock/battery
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(WindowInsets.statusBars.asPaddingValues())
    ) {
        when (weatherState) {
            is ResponseState.Loading -> {
                // Reusing your awesome Lottie animation for loading states!
                SplashAnimation(modifier = Modifier.align(Alignment.Center))
            }
            is ResponseState.Error -> {
                val errorMessage = (weatherState as ResponseState.Error).message
                Text(
                    text = "Oops! $errorMessage",
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.align(Alignment.Center)
                )
            }
            is ResponseState.Success -> {
                val weatherData = (weatherState as ResponseState.Success).data
                WeatherContent(weatherData = weatherData)
            }
        }
    }
}

@Composable
fun HeroSection(weatherData: ForecastResponseApi) {
    val currentWeather = weatherData.list.firstOrNull() ?: return
    val weatherDetail = currentWeather.weather.firstOrNull()

    // 1. Get the dynamic icon based on the API code
    val iconRes = getWeatherIcon(weatherDetail?.icon ?: "")

    val dateFormat = SimpleDateFormat("EEE, MMM d", Locale.getDefault())
    val timeFormat = SimpleDateFormat("h:mm a", Locale.getDefault())
    val dateString = dateFormat.format(Date(currentWeather.dt * 1000L))
    val timeString = timeFormat.format(Date(currentWeather.dt * 1000L))

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 24.dp, bottom = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = weatherData.city.name,
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onBackground
        )

        Text(
            text = "$dateString • $timeString",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
        )

        Spacer(modifier = Modifier.height(24.dp))

        // 2. The Dynamic Icon!
        // This will now change automatically based on the weather
        Icon(
            painter = painterResource(id = iconRes),
            contentDescription = weatherDetail?.description,
            modifier = Modifier.size(180.dp),
            tint = Color.Unspecified
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "${currentWeather.main.temp.toInt()}°",
            style = MaterialTheme.typography.displayLarge,
            color = MaterialTheme.colorScheme.primary
        )

        Text(
            text = weatherDetail?.description?.replaceFirstChar { it.uppercase() } ?: "Unknown",
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.secondary
        )
    }
}

@Composable
fun InfoCard(
    label: String,
    value: String,
    icon: Int,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .padding(8.dp)
            .shadow(elevation = 4.dp, shape = RoundedCornerShape(24.dp))
            .background(MaterialTheme.colorScheme.surface, shape = RoundedCornerShape(24.dp))
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            painter = painterResource(id = icon),
            contentDescription = label,
            modifier = Modifier.size(32.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
        )
    }
}
@Composable
fun InfoGrid(weatherData: ForecastResponseApi) {
    val current = weatherData.list.firstOrNull() ?: return

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(modifier = Modifier.fillMaxWidth()) {
            InfoCard(
                label = "Humidity",
                value = "${current.main.humidity}%",
                icon = R.drawable.ic_humidity, // Make sure to add these SVGs!
                modifier = Modifier.weight(1f)
            )
            InfoCard(
                label = "Wind",
                value = "${current.wind.speed} m/s",
                icon = R.drawable.ic_wind,
                modifier = Modifier.weight(1f)
            )
        }
        Row(modifier = Modifier.fillMaxWidth()) {
            InfoCard(
                label = "Pressure",
                value = "${current.main.pressure} hPa",
                icon = R.drawable.ic_pressure,
                modifier = Modifier.weight(1f)
            )
            InfoCard(
                label = "Clouds",
                value = "${current.clouds.all}%",
                icon = R.drawable.ic_clouds_info,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@DrawableRes
fun getWeatherIcon(iconCode: String): Int {
    return when (iconCode) {
        "01d" -> R.drawable.ic_sunny
        "01n" -> R.drawable.ic_clear_night
        "02d", "02n" -> R.drawable.ic_few_clouds
        "03d", "03n", "04d", "04n" -> R.drawable.ic_cloudy
        "09d", "09n" -> R.drawable.ic_rain_showers
        "10d", "10n" -> R.drawable.ic_rain
        "11d", "11n" -> R.drawable.ic_thunderstorm
        "13d", "13n" -> R.drawable.ic_snow
        "50d", "50n" -> R.drawable.ic_mist
        else -> R.drawable.ic_rainbow
    }
}

@Composable
fun ForecastItem(item: com.example.weatherforecastapplication.data.models.ForecastItem) {
    val dayFormat = SimpleDateFormat("EEEE", Locale.getDefault())
    val dayName = dayFormat.format(Date(item.dt * 1000L))
    val iconRes = getWeatherIcon(item.weather.firstOrNull()?.icon ?: "")

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp, horizontal = 8.dp)
            .shadow(elevation = 2.dp, shape = RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surface, shape = RoundedCornerShape(16.dp))
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = dayName,
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.weight(1f)
        )

        Icon(
            painter = painterResource(id = iconRes),
            contentDescription = null,
            modifier = Modifier.size(40.dp),
            tint = Color.Unspecified
        )

        Text(
            text = "${item.main.temp.toInt()}°",
            style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
            modifier = Modifier.weight(1f),
            textAlign = androidx.compose.ui.text.style.TextAlign.End
        )
    }
}

@Composable
fun ForecastList(weatherData: ForecastResponseApi) {
    val dailyForecast = weatherData.list.filter { it.dtTxt.contains("12:00:00") }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 32.dp)
    ) {
        Text(
            text = "Next 5 Days",
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.padding(start = 8.dp, bottom = 12.dp)
        )
        dailyForecast.forEach { item ->
            ForecastItem(item = item)
        }
    }
}
@Composable
fun WeatherContent(weatherData: ForecastResponseApi) {
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .verticalScroll(scrollState),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        HeroSection(weatherData = weatherData)
        Spacer(modifier = Modifier.height(16.dp))
        InfoGrid(weatherData = weatherData)
        Spacer(modifier = Modifier.height(24.dp))

        // ADD THIS: The 5-day forecast
        ForecastList(weatherData = weatherData)

        // Extra spacer to make sure the bottom bar doesn't hide the last item
        Spacer(modifier = Modifier.height(100.dp))
    }
}
