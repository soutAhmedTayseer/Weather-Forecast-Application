package com.example.weatherforecastapplication.homescreen.view

import androidx.annotation.DrawableRes
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
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

    val currentLanguage = Locale.getDefault().language

    // LIVE TICKING CLOCK STATE
    var liveCurrentTimeMillis by remember { mutableLongStateOf(System.currentTimeMillis()) }
    LaunchedEffect(Unit) {
        while (true) {
            delay(1000L)
            liveCurrentTimeMillis = System.currentTimeMillis()
        }
    }

    LaunchedEffect(isRefreshing) {
        if (isRefreshing) {
            showRefreshAnimation = true
            viewModel.getWeatherData(31.2001, 29.9187, "metric", currentLanguage)
            delay(2000)
            isRefreshing = false
            showRefreshAnimation = false
        }
    }

    LaunchedEffect(Unit) {
        if (weatherState is ResponseState.Loading) {
            viewModel.getWeatherData(31.2001, 29.9187, "metric", currentLanguage)
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        if (weatherState is ResponseState.Success) {
            val weatherData = (weatherState as ResponseState.Success).data
            val currentUtcTime = liveCurrentTimeMillis / 1000L
            val localTimeInSeconds = currentUtcTime + weatherData.city.timezone
            val localSunrise = weatherData.city.sunrise + weatherData.city.timezone
            val localSunset = weatherData.city.sunset + weatherData.city.timezone

            // Calculate Day/Night Status
            val isDay = localTimeInSeconds in localSunrise..localSunset

            BackgroundAnimationPlaceholder(isDayTime = isDay)

            PullToRefreshBox(
                isRefreshing = isRefreshing,
                onRefresh = { isRefreshing = true },
                modifier = Modifier
                    .fillMaxSize()
                    .padding(WindowInsets.statusBars.asPaddingValues())
            ) {
                if (showRefreshAnimation) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        SplashAnimation()
                    }
                } else {
                    // Pass the ticking clock AND the isDay flag down to the content
                    WeatherContent(weatherData = weatherData, liveTime = liveCurrentTimeMillis, isDay = isDay)
                }
            }

        } else {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background)
            )
            // Error & Loading States for initial load
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
                else -> {}
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
    }
}

@Composable
fun WeatherContent(weatherData: ForecastResponseApi, liveTime: Long, isDay: Boolean) {
    val scrollState = rememberScrollState()

    // Dynamic text color based on the Lottie Background
    val headerTextColor = if (isDay) Color(0xFF2D3436) else Color.White

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .verticalScroll(scrollState),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        HeroSection(weatherData = weatherData, liveTime = liveTime, isDay = isDay)

        Spacer(modifier = Modifier.height(24.dp))

        HourlyForecastList(weatherData = weatherData, isDay = isDay)

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = stringResource(id = R.string.weather_details),
            style = MaterialTheme.typography.titleLarge,
            color = headerTextColor,
            modifier = Modifier
                .align(Alignment.Start)
                .padding(start = 8.dp, bottom = 8.dp)
        )
        InfoGrid(weatherData = weatherData)

        Spacer(modifier = Modifier.height(24.dp))

        ForecastList(weatherData = weatherData, isDay = isDay)

        Spacer(modifier = Modifier.height(100.dp))
    }
}

@Composable
fun HeroSection(weatherData: ForecastResponseApi, liveTime: Long, isDay: Boolean) {
    val currentWeather = weatherData.list.firstOrNull() ?: return
    val weatherDetail = currentWeather.weather.firstOrNull()

    val iconRes = getWeatherIcon(weatherDetail?.icon ?: "")

    val absoluteCityTimeInMillis = liveTime + (weatherData.city.timezone * 1000L)
    val date = Date(absoluteCityTimeInMillis)

    val dateFormat = SimpleDateFormat("EEE, MMM d", Locale.getDefault()).apply {
        timeZone = TimeZone.getTimeZone("UTC")
    }
    val timeFormat = SimpleDateFormat("h:mm a", Locale.getDefault()).apply {
        timeZone = TimeZone.getTimeZone("UTC")
    }

    val dateString = dateFormat.format(date)
    val timeString = timeFormat.format(date)

    // DYNAMIC & NEUTRAL COLORS
    val headerTextColor = if (isDay) Color(0xFF2D3436) else Color.White
    val softBlueTempColor = Color(0xFF74B9FF) // The specific blue from your screenshot

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = weatherData.city.name,
            style = MaterialTheme.typography.titleLarge,
            color = headerTextColor
        )

        Text(
            text = "$dateString • $timeString",
            style = MaterialTheme.typography.bodyLarge,
            color = headerTextColor.copy(alpha = 0.8f)
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
            color = softBlueTempColor // Fits beautifully on both light and dark
        )

        val unknownText = stringResource(id = R.string.unknown)

        // RETRO FLAT STYLE CARD
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f),
                contentColor = MaterialTheme.colorScheme.onSurface
            ),
            modifier = Modifier
                .padding(top = 8.dp)
                .border(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f), RoundedCornerShape(16.dp))
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
fun HourlyForecastList(weatherData: ForecastResponseApi, isDay: Boolean) {
    val hourlyData = weatherData.list.take(8)
    val headerTextColor = if (isDay) Color(0xFF2D3436) else Color.White

    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = stringResource(id = R.string.today),
            style = MaterialTheme.typography.titleLarge,
            color = headerTextColor,
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
            .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.95f), shape = RoundedCornerShape(16.dp))
            .border(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f), RoundedCornerShape(16.dp))
            .padding(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = timeString, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurface)
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
            style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
fun InfoCard(label: String, value: String, icon: Int, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .padding(8.dp)
            .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.95f), shape = RoundedCornerShape(24.dp))
            .border(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f), RoundedCornerShape(24.dp))
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
            .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.95f), shape = RoundedCornerShape(16.dp))
            .border(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f), RoundedCornerShape(16.dp))
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = dayName,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface,
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
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1f),
            textAlign = TextAlign.End
        )
    }
}

@Composable
fun ForecastList(weatherData: ForecastResponseApi, isDay: Boolean) {
    val dailyForecast = weatherData.list.filter { it.dtTxt.contains("12:00:00") }
    val headerTextColor = if (isDay) Color(0xFF2D3436) else Color.White

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 32.dp)
    ) {
        Text(
            text = stringResource(id = R.string.next_5_days),
            style = MaterialTheme.typography.titleLarge,
            color = headerTextColor,
            modifier = Modifier.padding(start = 8.dp, bottom = 12.dp)
        )
        dailyForecast.forEach { item ->
            ForecastItem(
                item = item, timezoneOffset = weatherData.city.timezone
            )
        }
    }
}