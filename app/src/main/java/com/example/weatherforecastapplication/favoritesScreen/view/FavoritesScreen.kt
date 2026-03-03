package com.example.weatherforecastapplication.favoritesscreen.view

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.weatherforecastapplication.data.models.CityLocation
import com.example.weatherforecastapplication.favoritesscreen.viewmodel.FavoritesViewModel
import com.example.weatherforecastapplication.navigation.ScreenRoute
import com.example.weatherforecastapplication.ui.theme.component.RetroAlertDialog

@Composable
fun FavoritesScreen(viewModel: FavoritesViewModel, navController: NavController) {
    val favorites by viewModel.favoritesList.collectAsState()

    // State to hold the location currently being deleted to trigger the dialog
    var locationToDelete by remember { mutableStateOf<CityLocation?>(null) }

    // The Dialog Trigger
    if (locationToDelete != null) {
        RetroAlertDialog(
            title = "Delete Location",
            message = "Are you sure you want to remove ${locationToDelete?.cityName} from your favorites?",
            onConfirm = {
                viewModel.deleteLocation(locationToDelete!!)
                locationToDelete = null
            },
            onDismiss = { locationToDelete = null }
        )
    }

    Scaffold(
        containerColor = Color.Transparent,
        floatingActionButton = {
            FloatingActionButton(
                onClick = { navController.navigate(ScreenRoute.MapSelection.route) },
                containerColor = Color(0xFF74B9FF), // Matches your retro blue
                contentColor = Color.White
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Location")
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
        ) {
            Text(
                text = "Favorite Locations",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.padding(vertical = 16.dp)
            )

            if (favorites.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No favorites yet. Click '+' to add one!", color = MaterialTheme.colorScheme.onBackground)
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(bottom = 100.dp)
                ) {
                    items(favorites, key = { it.id }) { location ->
                        FavoriteItemCard(
                            location = location,
                            // Intercept the delete request to show the dialog
                            onDeleteRequest = { locationToDelete = location },
                            onClick = {
                                // Navigate to details, passing the data!
                                navController.navigate(ScreenRoute.FavoriteDetails.createRoute(location.lat, location.lon, location.cityName))
                            }
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FavoriteItemCard(location: CityLocation, onDeleteRequest: () -> Unit, onClick: () -> Unit) {
    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = { dismissValue ->
            if (dismissValue == SwipeToDismissBoxValue.EndToStart) {
                onDeleteRequest()
                false // Return false so the card snaps back until confirmed
            } else false
        }
    )

    SwipeToDismissBox(
        state = dismissState,
        enableDismissFromStartToEnd = false,
        backgroundContent = {
            Box(
                Modifier
                    .fillMaxSize()
                    .background(Color(0xFFFF7675), RoundedCornerShape(16.dp)) // Soft retro red
                    .padding(horizontal = 20.dp),
                contentAlignment = Alignment.CenterEnd
            ) {
                Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color.White)
            }
        }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.95f), RoundedCornerShape(16.dp))
                .border(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f), RoundedCornerShape(16.dp))
                .clickable { onClick() }
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = location.cityName,
                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.weight(1f)
            )
            // Optional: You can also add a clickable delete icon here for users who don't know they can swipe!
            Icon(
                imageVector = Icons.Default.Delete,
                contentDescription = "Delete",
                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                modifier = Modifier.clickable { onDeleteRequest() }
            )
        }
    }
}