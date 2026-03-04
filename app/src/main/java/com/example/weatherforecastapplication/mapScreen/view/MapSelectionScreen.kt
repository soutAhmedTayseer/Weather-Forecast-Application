package com.example.weatherforecastapplication.mapscreen.view

import android.location.Address
import android.location.Geocoder
import android.os.Build
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.navigation.NavController
import com.example.weatherforecastapplication.mapscreen.viewmodel.MapViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.osmdroid.config.Configuration
import org.osmdroid.events.MapEventsReceiver
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.MapEventsOverlay
import org.osmdroid.views.overlay.Marker
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapSelectionScreen(
    viewModel: MapViewModel,
    navController: NavController,
    onLocationSaved: (Double, Double, String) -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    val searchQuery by viewModel.searchQuery.collectAsState()
    val searchResults by viewModel.searchResults.collectAsState()

    var selectedGeoPoint by remember { mutableStateOf<GeoPoint?>(null) }
    var selectedCityName by remember { mutableStateOf("") }
    var isGeocoding by remember { mutableStateOf(false) }

    // Initialize OSM configuration
    LaunchedEffect(Unit) {
        Configuration.getInstance().userAgentValue = context.packageName
    }

    Box(modifier = Modifier.fillMaxSize()) {

        // 1. FREE OPEN STREET MAP
        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = { ctx ->
                MapView(ctx).apply {
                    setTileSource(TileSourceFactory.MAPNIK)
                    setMultiTouchControls(true)
                    controller.setZoom(6.0)
                    controller.setCenter(GeoPoint(26.8206, 30.8025)) // Center on Egypt

                    // Setup Tap Listener
                    val mapEventsReceiver = object : MapEventsReceiver {
                        override fun singleTapConfirmedHelper(p: GeoPoint?): Boolean {
                            if (p != null) {
                                selectedGeoPoint = p
                                isGeocoding = true
                                selectedCityName = "Locating..."

                                // NATIVE REVERSE GEOCODING
                                coroutineScope.launch(Dispatchers.IO) {
                                    try {
                                        val geocoder = Geocoder(context, Locale.getDefault())

                                        // NEW API 33+ WAY (Asynchronous)
                                        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                                            geocoder.getFromLocation(p.latitude, p.longitude, 1, object : Geocoder.GeocodeListener {
                                                override fun onGeocode(addresses: MutableList<android.location.Address>) {
                                                    val name = if (addresses.isNotEmpty()) {
                                                        addresses[0].locality ?: addresses[0].subAdminArea ?: addresses[0].adminArea ?: "Unknown Location"
                                                    } else "Unknown Location"

                                                    // Compose State is thread-safe, update it directly!
                                                    selectedCityName = name
                                                    isGeocoding = false
                                                }
                                                override fun onError(errorMessage: String?) {
                                                    selectedCityName = "Unknown Location"
                                                    isGeocoding = false
                                                }
                                            })
                                        } else {
                                            // OLD API < 33 WAY (Synchronous)
                                            @Suppress("DEPRECATION")
                                            val addresses = geocoder.getFromLocation(p.latitude, p.longitude, 1)
                                            val name = if (!addresses.isNullOrEmpty()) {
                                                addresses[0].locality ?: addresses[0].subAdminArea ?: addresses[0].adminArea ?: "Unknown Location"
                                            } else {
                                                "Unknown Location"
                                            }

                                            // Compose State is thread-safe, update it directly!
                                            selectedCityName = name
                                            isGeocoding = false
                                        }
                                    } catch (e: Exception) {
                                        selectedCityName = "Unknown Location"
                                        isGeocoding = false
                                    }
                                }
                            }
                            return true
                        }
                        override fun longPressHelper(p: GeoPoint?): Boolean = false
                    }
                    overlays.add(MapEventsOverlay(mapEventsReceiver))
                }
            },
            update = { mapView ->
                // Clear old markers and draw the new one when user taps or searches
                mapView.overlays.removeAll { it is Marker }
                selectedGeoPoint?.let { point ->
                    val marker = Marker(mapView)
                    marker.position = point
                    marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                    marker.title = selectedCityName
                    mapView.overlays.add(marker)
                    mapView.controller.animateTo(point)
                    mapView.invalidate()
                }
            }
        )

        // 2. SEARCH BAR (Uses the free OpenWeatherMap Autocomplete we built!)
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .statusBarsPadding()
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { viewModel.onSearchQueryChanged(it) },
                placeholder = { Text("Search city (e.g., Cairo)") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search") },
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(4.dp, RoundedCornerShape(12.dp)),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                    unfocusedBorderColor = Color.Transparent,
                    focusedBorderColor = MaterialTheme.colorScheme.primary
                ),
                shape = RoundedCornerShape(12.dp)
            )

            if (searchResults.isNotEmpty()) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp)
                        .shadow(8.dp, RoundedCornerShape(12.dp)),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    LazyColumn(modifier = Modifier.heightIn(max = 250.dp)) {
                        items(searchResults) { location ->
                            Text(
                                text = location.displayName,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        // When clicking a search result, drop the pin there!
                                        selectedGeoPoint = GeoPoint(location.lat, location.lon)
                                        selectedCityName = location.name
                                        viewModel.clearSearch()
                                    }
                                    .padding(16.dp),
                                style = MaterialTheme.typography.bodyLarge
                            )
                            HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f))
                        }
                    }
                }
            }
        }

        // Back Button
        FilledIconButton(
            onClick = { navController.popBackStack() },
            modifier = Modifier
                .padding(top = 90.dp, start = 16.dp)
                .align(Alignment.TopStart),
            colors = IconButtonDefaults.filledIconButtonColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f))
        ) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = MaterialTheme.colorScheme.onSurface)
        }

        // 3. RETRO SAVE BUTTON
        if (selectedGeoPoint != null) {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f)),
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 48.dp, start = 24.dp, end = 24.dp)
                    .fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = if (isGeocoding) "Finding location..." else selectedCityName,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = {
                            onLocationSaved(
                                selectedGeoPoint!!.latitude,
                                selectedGeoPoint!!.longitude,
                                selectedCityName
                            )
                            navController.previousBackStackEntry?.savedStateHandle?.set("snackbar_message", "Location set to $selectedCityName!")
                            navController.popBackStack()
                        },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !isGeocoding,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF74B9FF)),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Save to Favorites", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}