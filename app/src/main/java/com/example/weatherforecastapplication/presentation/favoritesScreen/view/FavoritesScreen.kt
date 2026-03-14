package com.example.weatherforecastapplication.presentation.favoritesScreen.view

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.weatherforecastapplication.R
import com.example.weatherforecastapplication.core.navigation.ScreenRoute
import com.example.weatherforecastapplication.core.theme.component.AnimatedWeatherIcon
import com.example.weatherforecastapplication.core.theme.component.EmptyStateComponent
import com.example.weatherforecastapplication.core.theme.component.RetroAlertDialog
import com.example.weatherforecastapplication.core.theme.component.RetroCard
import com.example.weatherforecastapplication.core.theme.component.RetroFAB
import com.example.weatherforecastapplication.core.theme.component.RetroSwipeToDeleteContainer
import com.example.weatherforecastapplication.core.theme.component.RetroTopAppBar
import com.example.weatherforecastapplication.core.theme.component.SolidSwipeRefreshLayout
import com.example.weatherforecastapplication.core.theme.component.SplashAnimation
import com.example.weatherforecastapplication.core.theme.component.getWeatherIcon
import com.example.weatherforecastapplication.data.models.entities.CityLocation
import com.example.weatherforecastapplication.presentation.favoritesScreen.viewmodel.FavoritesViewModel
import com.example.weatherforecastapplication.presentation.favoritesScreen.viewmodel.FavoriteWeatherUiState

@Composable
fun FavoritesScreen(viewModel: FavoritesViewModel, navController: NavController) {
    val favoritesWeather by viewModel.favoritesWeather.collectAsState()
    val tempUnit by viewModel.tempUnitFlow.collectAsState()

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
        SolidSwipeRefreshLayout(
            onRefresh = { viewModel.refreshFavorites() },
            loadingMessage = stringResource(id = R.string.updating_favorites),
            gifRes = R.drawable.jakeloading, // JAKE ASSIGNED HERE
            modifier = Modifier.fillMaxSize().padding(paddingValues)
        ) {
            if (favoritesWeather.isEmpty()) {
                // NEW EMPTY STATE WITH JAKE GIF
                EmptyStateComponent(
                    message = stringResource(id = R.string.no_favorites_yet),
                    gifRes = R.drawable.jakeloading
                )
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
fun FavoriteItemCard(state: FavoriteWeatherUiState, tempSymbol: String, onClick: () -> Unit, onDeleteRequest: () -> Unit) {
    RetroSwipeToDeleteContainer(onDelete = onDeleteRequest) {
        RetroCard(onClick = onClick) {
            Row(modifier = Modifier.padding(16.dp).fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                if (state.isLoading) {
                    SplashAnimation(modifier = Modifier.size(50.dp))
                } else {
                    AnimatedWeatherIcon(iconRes = getWeatherIcon(state.icon), modifier = Modifier.size(50.dp))
                }

                Spacer(modifier = Modifier.width(16.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(text = state.translatedCityName, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurface)
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (!state.isLoading && state.temp != "--") {
                            Text(text = "${state.temp}$tempSymbol", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurface)
                            Spacer(modifier = Modifier.width(16.dp))
                        }
                        if (state.description.isNotEmpty()) {
                            Text(text = state.description.replaceFirstChar { it.uppercase() }, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
                        }
                    }
                }

                Spacer(modifier = Modifier.width(16.dp))

                Icon(painter = painterResource(id = R.drawable.ic_delete), contentDescription = stringResource(id = R.string.delete), tint = Color.Unspecified, modifier = Modifier.size(38.dp).clickable { onDeleteRequest() })
            }
        }
    }
}