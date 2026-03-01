package com.example.weatherforecastapplication.homescreen.view

import androidx.annotation.DrawableRes
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.rememberLottieComposition
import com.example.weatherforecastapplication.R
import com.example.weatherforecastapplication.data.models.ForecastResponseApi
import com.example.weatherforecastapplication.data.models.ResponseState
import com.example.weatherforecastapplication.homescreen.viewmodel.HomeViewModel
import com.example.weatherforecastapplication.ui.theme.component.SplashAnimation
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(viewModel: HomeViewModel) {
    val weatherState by viewModel.weatherState.collectAsState()
    var isRefreshing by remember { mutableStateOf(false) }
    var showRefreshAnimation by remember { mutableStateOf(false) }

    // Handle refresh logic and force the animation to show for at least 2 seconds
    LaunchedEffect(isRefreshing) {
        if (isRefreshing) {
            showRefreshAnimation = true
            viewModel.getWeatherData(31.2001, 29.9187, "metric", "en")
            delay(2000) // Force animation display for 2 seconds
            isRefreshing = false
            showRefreshAnimation = false
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {

        // --- 1. DYNAMIC BACKGROUND ANIMATION PLACEHOLDER ---
        if (weatherState is ResponseState.Success) {
            val weatherData = (weatherState as ResponseState.Success).data

            // Calculate accurate local time for the background toggle
            val currentUtcTime = System.currentTimeMillis() / 1000L
            val localTimeInSeconds = currentUtcTime + weatherData.city.timezone

            // Sunrise/Sunset from API adjusted to local time
            val localSunrise = weatherData.city.sunrise + weatherData.city.timezone
            val localSunset = weatherData.city.sunset + weatherData.city.timezone

            val isDay = localTimeInSeconds in localSunrise..localSunset

            BackgroundAnimationPlaceholder(isDayTime = isDay)
        } else {
            // Fallback background while loading or error
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background)
            )
        }

        // --- 2. SWIPE TO REFRESH WRAPPER ---
        PullToRefreshBox(
            isRefreshing = isRefreshing,
            onRefresh = { isRefreshing = true },
            modifier = Modifier
                .fillMaxSize()
                .padding(WindowInsets.statusBars.asPaddingValues())
        ) {
            // Show centered loading animation during refresh
            if (showRefreshAnimation) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    SplashAnimation()
                }
            } else {
                when (weatherState) {
                    is ResponseState.Loading -> {
                        SplashAnimation(modifier = Modifier.align(Alignment.Center))
                    }

                    is ResponseState.Error -> {
                        val errorMessage = (weatherState as ResponseState.Error).message
                        Text(
                            text = "${stringResource(id = R.string.error_prefix)} $errorMessage",
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
    }
}

@Composable
fun BackgroundAnimationPlaceholder(isDayTime: Boolean) {
    val animationResId = if (isDayTime) {
        R.raw.day_background
    } else {
        R.raw.night_background
    }

    val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(animationResId))

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(if (isDayTime) Color(0xFF87CEEB) else Color(0xFF192A56)),
        contentAlignment = Alignment.Center
    ) {
        LottieAnimation(
            composition = composition,
            iterations = LottieConstants.IterateForever,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.FillBounds
        )

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.1f))
        )
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

        Spacer(modifier = Modifier.height(24.dp))

        HourlyForecastList(weatherData = weatherData)

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = stringResource(id = R.string.weather_details),
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier
                .align(Alignment.Start)
                .padding(start = 8.dp, bottom = 8.dp)
        )
        InfoGrid(weatherData = weatherData)

        Spacer(modifier = Modifier.height(24.dp))

        ForecastList(weatherData = weatherData)

        Spacer(modifier = Modifier.height(100.dp))
    }
}

@Composable
fun HeroSection(weatherData: ForecastResponseApi) {
    val currentWeather = weatherData.list.firstOrNull() ?: return
    val weatherDetail = currentWeather.weather.firstOrNull()

    val iconRes = getWeatherIcon(weatherDetail?.icon ?: "")

    // --- ACCURATE REAL-TIME CALCULATION ---
    // Get actual current time in UTC epoch, add the city's offset
    val absoluteCityTimeInMillis = System.currentTimeMillis() + (weatherData.city.timezone * 1000L)
    val date = Date(absoluteCityTimeInMillis)

    // Force formatter to UTC so it prints exactly the offset time we calculated
    val dateFormat = SimpleDateFormat("EEE, MMM d", Locale.getDefault()).apply {
        timeZone = TimeZone.getTimeZone("UTC")
    }
    val timeFormat = SimpleDateFormat("h:mm a", Locale.getDefault()).apply {
        timeZone = TimeZone.getTimeZone("UTC")
    }

    val dateString = dateFormat.format(date)
    val timeString = timeFormat.format(date)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 24.dp),
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
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.8f)
        )

        Spacer(modifier = Modifier.height(16.dp))

        Icon(
            painter = painterResource(id = iconRes),
            contentDescription = weatherDetail?.description,
            modifier = Modifier.size(160.dp),
            tint = Color.Unspecified
        )

        Text(
            text = "${currentWeather.main.temp.toInt()}°",
            style = MaterialTheme.typography.displayLarge,
            color = MaterialTheme.colorScheme.primary
        )

        val unknownText = stringResource(id = R.string.unknown)
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.onSurface
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            modifier = Modifier.padding(top = 8.dp)
        ) {
            Text(
                text = weatherDetail?.description?.replaceFirstChar { it.uppercase() } ?: unknownText,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 12.dp)
            )
        }
    }
}

@Composable
fun HourlyForecastList(weatherData: ForecastResponseApi) {
    val hourlyData = weatherData.list.take(8)

    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = stringResource(id = R.string.today),
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.padding(start = 8.dp, bottom = 8.dp)
        )
        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(horizontal = 8.dp)
        ) {
            items(hourlyData) { item ->
                HourlyItem(item, weatherData.city.timezone)
            }
        }
    }
}

@Composable
fun HourlyItem(
    item: com.example.weatherforecastapplication.data.models.ForecastItem, timezoneOffset: Int
) {
    val localTimeInMillis = (item.dt + timezoneOffset) * 1000L
    val timeFormat = SimpleDateFormat("h a", Locale.getDefault()).apply {
        timeZone = TimeZone.getTimeZone("UTC")
    }
    val timeString = timeFormat.format(Date(localTimeInMillis))
    val iconRes = getWeatherIcon(item.weather.firstOrNull()?.icon ?: "")

    Column(
        modifier = Modifier
            .shadow(elevation = 2.dp, shape = RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surface, shape = RoundedCornerShape(16.dp))
            .padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = timeString, style = MaterialTheme.typography.labelMedium)
        Spacer(modifier = Modifier.height(8.dp))
        Icon(
            painter = painterResource(id = iconRes),
            contentDescription = null,
            modifier = Modifier.size(36.dp),
            tint = Color.Unspecified
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "${item.main.temp.toInt()}°",
            style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold)
        )
    }
}

@Composable
fun InfoCard(label: String, value: String, icon: Int, modifier: Modifier = Modifier) {
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
            modifier = Modifier.size(36.dp),
            tint = Color.Unspecified
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
                label = stringResource(id = R.string.humidity),
                value = "${current.main.humidity}%",
                icon = R.drawable.ic_humidity,
                modifier = Modifier.weight(1f)
            )
            InfoCard(
                label = stringResource(id = R.string.wind),
                value = "${current.wind.speed} m/s",
                icon = R.drawable.ic_wind,
                modifier = Modifier.weight(1f)
            )
        }
        Row(modifier = Modifier.fillMaxWidth()) {
            InfoCard(
                label = stringResource(id = R.string.pressure),
                value = "${current.main.pressure} hPa",
                icon = R.drawable.ic_pressure,
                modifier = Modifier.weight(1f)
            )
            InfoCard(
                label = stringResource(id = R.string.clouds),
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
fun ForecastItem(
    item: com.example.weatherforecastapplication.data.models.ForecastItem, timezoneOffset: Int
) {
    val localTimeInMillis = (item.dt + timezoneOffset) * 1000L
    val dayFormat = SimpleDateFormat("EEEE", Locale.getDefault()).apply {
        timeZone = TimeZone.getTimeZone("UTC")
    }
    val dayName = dayFormat.format(Date(localTimeInMillis))
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
            textAlign = TextAlign.End
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
            text = stringResource(id = R.string.next_5_days),
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.padding(start = 8.dp, bottom = 12.dp)
        )
        dailyForecast.forEach { item ->
            ForecastItem(
                item = item, timezoneOffset = weatherData.city.timezone
            )
        }
    }
}