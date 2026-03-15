package com.example.weatherforecastapplication.core.theme.component

import androidx.annotation.DrawableRes
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.weatherforecastapplication.R
import com.example.weatherforecastapplication.data.models.dataClasses.ForecastResponseApi
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import android.os.Build
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import coil.ImageLoader
import coil.compose.AsyncImage
import coil.decode.GifDecoder
import coil.decode.ImageDecoderDecoder
import com.example.weatherforecastapplication.data.models.dataClasses.ForecastItem
import kotlinx.coroutines.delay

@Composable
fun GlobalWeatherBackground(isDayTime: Boolean, content: @Composable () -> Unit) {
    val context = LocalContext.current

    val imageLoader = remember {
        ImageLoader.Builder(context)
            .components {
                if (Build.VERSION.SDK_INT >= 28) {
                    add(ImageDecoderDecoder.Factory())
                } else {
                    add(GifDecoder.Factory())
                }
            }
            .build()
    }

    val gifResId = if (isDayTime) R.drawable.day_background else R.drawable.night_background

    Box(modifier = Modifier.fillMaxSize()) {
        AsyncImage(
            model = gifResId,
            imageLoader = imageLoader,
            contentDescription = "Background GIF",
            contentScale = ContentScale.FillBounds,
            modifier = Modifier.fillMaxSize()
        )

        val overlayAlpha = if (isDayTime) 0.4f else 0.2f
        Box(modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = overlayAlpha)))
        content()
    }
}

@Composable
fun WeatherDetailsLayout(
    weatherData: ForecastResponseApi,
    liveTime: Long,
    isDay: Boolean,
    tempUnit: String,
    windUnit: String
) {
    val scrollState = rememberScrollState()
    val firstForecast = weatherData.list.firstOrNull() ?: return

    val tempSymbol = when (tempUnit) {
        "imperial" -> "°F"
        "standard" -> "K"
        else -> "°C"
    }
    val windSymbol = if (windUnit == "mph") "mph" else "m/s"

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(horizontal = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        CurrentWeatherHeader(weatherData, firstForecast, liveTime, tempSymbol, isDay)
        Spacer(modifier = Modifier.height(32.dp))

        WeatherDetailsGrid(firstForecast, windSymbol)
        Spacer(modifier = Modifier.height(32.dp))

        HourlyForecastRow(weatherData, tempSymbol, isDay)
        Spacer(modifier = Modifier.height(32.dp))

        DailyForecastColumn(weatherData, tempSymbol, isDay)
        Spacer(modifier = Modifier.height(100.dp))
    }
}

@Composable
fun CurrentWeatherHeader(
    weatherData: ForecastResponseApi,
    firstForecast: ForecastItem,
    liveTime: Long,
    tempSymbol: String,
    isDay: Boolean
) {
    val absoluteCityTimeInMillis = liveTime + (weatherData.city.timezone * 1000L)
    val date = Date(absoluteCityTimeInMillis)
    val dateFormat = SimpleDateFormat("EEE, MMM d", Locale.getDefault()).apply { timeZone = TimeZone.getTimeZone("UTC") }
    val timeFormat = SimpleDateFormat("h:mm a", Locale.getDefault()).apply { timeZone = TimeZone.getTimeZone("UTC") }

    val headerTextColor = Color(0xFF74B9FF)
    val softBlueTempColor = Color(0xFF74B9FF)

    val weatherDetail = firstForecast.weather.firstOrNull()
    val iconCode = weatherDetail?.icon ?: "01d"

    Column(
        modifier = Modifier.fillMaxWidth().padding(top = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.Start) {
            Text(text = weatherData.city.name, style = MaterialTheme.typography.displaySmall, color = headerTextColor)
            Text(text = "${dateFormat.format(date)} • ${timeFormat.format(date)}", style = MaterialTheme.typography.titleLarge, color = headerTextColor.copy(alpha = 0.8f))
        }

        Spacer(modifier = Modifier.height(24.dp))

        AnimatedWeatherIcon(iconRes = getWeatherIcon(iconCode), modifier = Modifier.size(160.dp))

        Spacer(modifier = Modifier.height(24.dp))

        Text(text = "${firstForecast.main.temp.toInt()}$tempSymbol", style = MaterialTheme.typography.displayLarge, color = softBlueTempColor)

        RetroCard(modifier = Modifier.padding(top = 16.dp)) {
            Text(
                text = weatherDetail?.description?.replaceFirstChar { it.uppercase() } ?: stringResource(id = R.string.unknown),
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 12.dp)
            )
        }
    }
}

@Composable
fun WeatherDetailsGrid(firstForecast: ForecastItem, windSymbol: String) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(modifier = Modifier.fillMaxWidth().height(110.dp), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            InfoTile(label = stringResource(id = R.string.humidity), value = "${firstForecast.main.humidity}%", icon = R.drawable.ic_humidity, modifier = Modifier.weight(1f).fillMaxHeight())
            InfoTile(label = stringResource(id = R.string.wind), value = "${firstForecast.wind.speed} $windSymbol", icon = R.drawable.ic_wind, modifier = Modifier.weight(1f).fillMaxHeight())
        }
        Row(modifier = Modifier.fillMaxWidth().height(110.dp), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            InfoTile(label = stringResource(id = R.string.pressure), value = "${firstForecast.main.pressure} hPa", icon = R.drawable.ic_pressure, modifier = Modifier.weight(1f).fillMaxHeight())
            InfoTile(label = stringResource(id = R.string.clouds), value = "${firstForecast.clouds.all}%", icon = R.drawable.ic_cloudy, modifier = Modifier.weight(1f).fillMaxHeight())
        }
    }
}

@Composable
fun InfoTile(label: String, value: String, @DrawableRes icon: Int, modifier: Modifier = Modifier) {
    RetroCard(modifier = modifier) {
        Column(
            modifier = Modifier.fillMaxSize().padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(painter = painterResource(id = icon), contentDescription = label, modifier = Modifier.size(32.dp), tint = Color.Unspecified)
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = value, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurface)
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = label, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
        }
    }
}

@Composable
fun HourlyForecastRow(weatherData: ForecastResponseApi, tempSymbol: String, isDay: Boolean) {
    val hourlyData = weatherData.list.take(8)
    val timezoneOffset = weatherData.city.timezone

    val listState = rememberLazyListState()

    LaunchedEffect(hourlyData) {
        if (hourlyData.isNotEmpty()) {
            while (true) {
                delay(3000)

                if (!listState.isScrollInProgress) {
                    val currentItem = listState.firstVisibleItemIndex
                    val nextItem = if (currentItem < hourlyData.size - 1) currentItem + 1 else 0

                    listState.animateScrollToItem(nextItem)
                }
            }
        }
    }

    // 4. The UI
    LazyRow(
        state = listState,
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(horizontal = 16.dp)
    ) {
        items(hourlyData) { item ->
            HourlyForecastCard(item, tempSymbol, timezoneOffset)
        }
    }
}

@Composable
fun HourlyForecastCard(item: ForecastItem, tempSymbol: String, timezoneOffset: Int) {
    val timeFormat = java.text.SimpleDateFormat("h a", java.util.Locale.getDefault())
    timeFormat.timeZone = java.util.TimeZone.getTimeZone("UTC")
    val timeString = timeFormat.format(java.util.Date((item.dt + timezoneOffset) * 1000L))

    RetroCard(
        modifier = Modifier
            .width(100.dp)
            .padding(vertical = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp).fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = timeString,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(12.dp))
            AnimatedWeatherIcon(
                iconRes = getWeatherIcon(item.weather.firstOrNull()?.icon ?: ""),
                modifier = Modifier.size(36.dp)
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "${item.main.temp.toInt()}$tempSymbol",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}
@Composable
fun DailyForecastColumn(weatherData: ForecastResponseApi, tempSymbol: String, isDay: Boolean) {
    val headerTextColor = if (isDay) Color(0xFF2D3436) else Color.White
    Column(modifier = Modifier.fillMaxWidth().padding(bottom = 32.dp)) {
        Text(text = stringResource(id = R.string.next_5_days), style = MaterialTheme.typography.titleLarge, color = headerTextColor, modifier = Modifier.padding(start = 8.dp, bottom = 12.dp))
        weatherData.list.filter { it.dtTxt.contains("12:00:00") }.take(5).forEach { item ->
            ForecastItem(item = item, timezoneOffset = weatherData.city.timezone, tempSymbol = tempSymbol)
        }
    }
}

@Composable
fun ForecastItem(item: ForecastItem, timezoneOffset: Int, tempSymbol: String) {
    val dayFormat = SimpleDateFormat("EEEE", Locale.getDefault()).apply { timeZone = TimeZone.getTimeZone("UTC") }
    val dayName = dayFormat.format(Date((item.dt + timezoneOffset) * 1000L))

    RetroCard(modifier = Modifier.padding(vertical = 6.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(text = dayName, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurface, modifier = Modifier.weight(1f))
            AnimatedWeatherIcon(iconRes = getWeatherIcon(item.weather.firstOrNull()?.icon ?: ""), modifier = Modifier.size(40.dp))
            Text(
                text = "${item.main.temp.toInt()}$tempSymbol",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.End
            )
        }
    }
}

@DrawableRes
fun getWeatherIcon(iconCode: String): Int {
    return when (iconCode) {
        "01d" -> R.drawable.ic_sunny
        "01n" -> R.drawable.ic_clear_night
        "02d", "03d", "04d" -> R.drawable.ic_cloudy
        "02n", "03n", "04n" -> R.drawable.ic_few_clouds
        "09d", "10d" -> R.drawable.ic_rain
        "09n", "10n" -> R.drawable.ic_rain_showers
        "11d", "11n" -> R.drawable.ic_thunderstorm
        "13d", "13n" -> R.drawable.ic_snow
        "50d", "50n" -> R.drawable.ic_mist
        else -> R.drawable.ic_rainbow
    }
}