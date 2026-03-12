package com.example.weatherforecastapplication.core.theme.component

import android.os.Build
import androidx.annotation.DrawableRes
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.PullToRefreshDefaults
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.airbnb.lottie.compose.*
import com.example.weatherforecastapplication.R
import kotlinx.coroutines.delay
import coil.ImageLoader
import coil.compose.AsyncImage
import coil.decode.GifDecoder
import coil.decode.ImageDecoderDecoder
import coil.request.ImageRequest

// --- 1. MAIN SPLASH ANIMATION (Lottie for Startup) ---
@Composable
fun SplashAnimation(modifier: Modifier = Modifier) {
    val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.weather_loading))
    LottieAnimation(
        composition = composition,
        iterations = LottieConstants.IterateForever,
        modifier = modifier.size(250.dp)
    )
}

// --- 2. NEW: CUSTOM GIF ANIMATION WITH DYNAMIC TEXT & GIF ---
@Composable
fun ScreenLoadingAnimation(
    message: String,
    @DrawableRes gifRes: Int, // Dynamic GIF
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val imageLoader = remember {
        ImageLoader.Builder(context)
            .components {
                if (Build.VERSION.SDK_INT >= 28) add(ImageDecoderDecoder.Factory()) else add(GifDecoder.Factory())
            }
            .build()
    }

    Column(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        AsyncImage(
            model = ImageRequest.Builder(context).data(gifRes).build(),
            contentDescription = "Loading...",
            imageLoader = imageLoader,
            modifier = Modifier.size(150.dp)
        )
        Spacer(modifier = Modifier.height(24.dp))
        Text(text = message, style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.primary)
    }
}

// --- 3. DRY PULL TO REFRESH WRAPPER ---
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SolidSwipeRefreshLayout(
    onRefresh: () -> Unit,
    loadingMessage: String,
    @DrawableRes gifRes: Int = R.drawable.finnloading, // Default to Finn so other screens don't break
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    var isRefreshing by remember { mutableStateOf(false) }
    var showRefreshAnimation by remember { mutableStateOf(false) }
    val pullRefreshState = rememberPullToRefreshState()

    LaunchedEffect(isRefreshing) {
        if (isRefreshing) {
            showRefreshAnimation = true
            onRefresh()
            delay(3000L)
            isRefreshing = false
            showRefreshAnimation = false
        }
    }

    PullToRefreshBox(
        state = pullRefreshState,
        isRefreshing = isRefreshing,
        onRefresh = { isRefreshing = true },
        modifier = modifier,
        indicator = {
            if (!showRefreshAnimation) {
                PullToRefreshDefaults.Indicator(
                    state = pullRefreshState,
                    isRefreshing = isRefreshing,
                    modifier = Modifier.align(Alignment.TopCenter),
                    color = MaterialTheme.colorScheme.primary,
                    containerColor = MaterialTheme.colorScheme.surface
                )
            }
        }
    ) {
        if (showRefreshAnimation) {
            ScreenLoadingAnimation(message = loadingMessage, gifRes = gifRes)
        } else {
            content()
        }
    }
}

// --- 4. NEW: EMPTY STATE PLACEHOLDER ---
@Composable
fun EmptyStateComponent(
    message: String,
    @DrawableRes gifRes: Int,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val imageLoader = remember {
        ImageLoader.Builder(context)
            .components {
                if (Build.VERSION.SDK_INT >= 28) add(ImageDecoderDecoder.Factory()) else add(GifDecoder.Factory())
            }
            .build()
    }

    Column(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        AsyncImage(
            model = ImageRequest.Builder(context).data(gifRes).build(),
            contentDescription = "Empty State",
            imageLoader = imageLoader,
            modifier = Modifier.size(150.dp)
        )
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = message,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onBackground
        )
    }
}

// --- 5. CARTOON ALERT DIALOG ---
@Composable
fun CartoonAlertDialog(title: String, message: String, onConfirm: () -> Unit, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(24.dp),
        containerColor = MaterialTheme.colorScheme.surface,
        title = { Text(text = title, style = MaterialTheme.typography.titleLarge) },
        text = { Text(text = message, style = MaterialTheme.typography.bodyLarge) },
        confirmButton = { Button(onClick = onConfirm, colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)) { Text("OK", color = MaterialTheme.colorScheme.onPrimary) } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel", color = MaterialTheme.colorScheme.onSurface) } }
    )
}