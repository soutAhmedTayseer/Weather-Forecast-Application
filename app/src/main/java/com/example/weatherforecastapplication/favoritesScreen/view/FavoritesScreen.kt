package com.example.weatherforecastapplication.favoritesscreen.view

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.weatherforecastapplication.R
import com.example.weatherforecastapplication.data.models.CityLocation
import com.example.weatherforecastapplication.favoritesscreen.viewmodel.FavoritesViewModel
import com.example.weatherforecastapplication.favoritesscreen.viewmodel.FavoriteWeatherUiState
import com.example.weatherforecastapplication.navigation.ScreenRoute
import com.example.weatherforecastapplication.ui.theme.component.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FavoritesScreen(viewModel: FavoritesViewModel, navController: NavController) {
    val favoritesWeather by viewModel.favoritesWeather.collectAsState()
    val tempUnit by viewModel.tempUnitFlow.collectAsState()
    val isRefreshing by viewModel.isRefreshing.collectAsState()

    var locationToDelete by remember { mutableStateOf<CityLocation?>(null) }

    val tempSymbol = when (tempUnit) {
        "imperial" -> "°F"
        "standard" -> "K"
        else -> "°C"
    }

    if (locationToDelete != null) {
        RetroAlertDialog(
            title = stringResource(id = R.string.delete_favorite_title),
            message = stringResource(id = R.string.delete_favorite_message),
            onConfirm = {
                viewModel.deleteLocation(locationToDelete!!)
                locationToDelete = null
            },
            onDismiss = { locationToDelete = null }
        )
    }

    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            RetroTopAppBar(title = stringResource(id = R.string.nav_favorites))
        },
        floatingActionButton = {
            RetroFAB(
                contentDescription = stringResource(id = R.string.add_favorite),
                onClick = { navController.navigate(ScreenRoute.MapSelection.createRoute(isForHome = false)) }
            )
        }
    ) { paddingValues ->
        PullToRefreshBox(
            isRefreshing = isRefreshing,
            onRefresh = { viewModel.refreshFavorites() },
            modifier = Modifier.fillMaxSize().padding(paddingValues)
        ) {
            if (favoritesWeather.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        text = stringResource(id = R.string.no_favorites_yet),
                        color = MaterialTheme.colorScheme.onBackground,
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 100.dp)
                ) {
                    items(favoritesWeather, key = { it.location.lat.toString() + it.location.lon.toString() }) { state ->
                        FavoriteItemCard(
                            state = state,
                            tempSymbol = tempSymbol,
                            onClick = { navController.navigate(ScreenRoute.FavoriteDetails.createRoute(state.location.lat, state.location.lon, state.location.cityName)) },
                            onDeleteRequest = { locationToDelete = state.location }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun FavoriteItemCard(
    state: FavoriteWeatherUiState,
    tempSymbol: String,
    onClick: () -> Unit,
    onDeleteRequest: () -> Unit
) {
    RetroSwipeToDeleteContainer(onDelete = onDeleteRequest) {
        RetroCard(onClick = onClick) {
            Row(
                modifier = Modifier.padding(16.dp).fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (state.isLoading) {
                    CircularProgressIndicator(modifier = Modifier.size(32.dp), strokeWidth = 3.dp, color = MaterialTheme.colorScheme.primary)
                } else {
                    AnimatedWeatherIcon(
                        iconRes = getWeatherIcon(state.icon),
                        modifier = Modifier.size(50.dp)
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = state.location.cityName,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (!state.isLoading && state.temp != "--") {
                            Text(
                                text = "${state.temp}$tempSymbol",
                                // FIX: Changed to labelMedium so it uses your Type.kt Minecraft font!
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.width(16.dp))
                        }

                        if (state.description.isNotEmpty()) {
                            Text(
                                text = state.description.replaceFirstChar { it.uppercase() },
                                // FIX: Changed to labelMedium so it uses your Type.kt Minecraft font!
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.width(16.dp))

                Icon(
                    painter = painterResource(id = R.drawable.ic_delete),
                    contentDescription = stringResource(id = R.string.delete),
                    tint = Color.Unspecified,
                    modifier = Modifier
                        .size(38.dp)
                        .clickable { onDeleteRequest() }
                )
            }
        }
    }
}