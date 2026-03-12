package com.example.weatherforecastapplication.core.theme.component

import androidx.annotation.DrawableRes
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.weatherforecastapplication.R

// --- THE REFINED DESIGN SYSTEM ---
// 12.dp gives a nice slight curve to remove the harsh sharpness
val RetroCornerShape = RoundedCornerShape(12.dp)
val RetroBorderWidth = 1.dp

@Composable
fun RetroSnackbarHost(hostState: SnackbarHostState, modifier: Modifier = Modifier) {
    SnackbarHost(
        hostState = hostState,
        modifier = modifier,
        snackbar = { snackbarData ->
            RetroCard(modifier = Modifier.padding(16.dp).fillMaxWidth()) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_alert),
                        contentDescription = "Notification",
                        tint = MaterialTheme.colorScheme.primary, // Clean primary color
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = snackbarData.visuals.message,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RetroTopAppBar(
    title: String,
    onBackClick: (() -> Unit)? = null,
    actions: @Composable RowScope.() -> Unit = {}
) {
    TopAppBar(
        title = {
            val softBlueTempColor = Color(0xFF74B9FF)
            Text(
                text = title,
                style = MaterialTheme.typography.displaySmall,
                color = softBlueTempColor
            )
        },
        navigationIcon = {
            if (onBackClick != null) {
                IconButton(onClick = onBackClick) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = MaterialTheme.colorScheme.onBackground
                    )
                }
            }
        },
        actions = actions,
        colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
    )
}

@Composable
fun RetroCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    content: @Composable () -> Unit
) {
    val cardColors = CardDefaults.cardColors(
        containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f),
        contentColor = MaterialTheme.colorScheme.onSurface
    )
    // Clean, subtle border
    val cardBorder = BorderStroke(RetroBorderWidth, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f))

    if (onClick != null) {
        Card(onClick = onClick, modifier = modifier, shape = RetroCornerShape, colors = cardColors, border = cardBorder) { content() }
    } else {
        Card(modifier = modifier, shape = RetroCornerShape, colors = cardColors, border = cardBorder) { content() }
    }
}

@Composable
fun RetroFAB(contentDescription: String, onClick: () -> Unit) {
    FloatingActionButton(
        onClick = onClick,
        containerColor = MaterialTheme.colorScheme.primary,
        contentColor = MaterialTheme.colorScheme.onPrimary,
        shape = RetroCornerShape,
        modifier = Modifier.border(RetroBorderWidth, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f), RetroCornerShape)
    ) {
        Icon(painter = painterResource(id = R.drawable.ic_add), contentDescription = contentDescription, modifier = Modifier.size(32.dp), tint = Color.Unspecified)
    }
}

@Composable
fun RetroAlertDialog(title: String, message: String, onConfirm: () -> Unit, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f),
        shape = RetroCornerShape,
        modifier = Modifier.border(RetroBorderWidth, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f), RetroCornerShape),
        title = { Text(text = title, style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.onSurface) },
        text = { Text(text = message, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurface) },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                shape = RetroCornerShape
            ) { Text(stringResource(id = R.string.delete), color = MaterialTheme.colorScheme.onPrimary, style = MaterialTheme.typography.bodyLarge) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(id = R.string.cancel), color = MaterialTheme.colorScheme.onSurface, style = MaterialTheme.typography.bodyLarge) }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RetroSwipeToDeleteContainer(
    onDelete: () -> Unit,
    content: @Composable () -> Unit
) {
    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = {
            if (it == SwipeToDismissBoxValue.EndToStart) {
                onDelete()
                false
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
                    .background(MaterialTheme.colorScheme.error, RetroCornerShape)
                    .border(RetroBorderWidth, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f), RetroCornerShape)
                    .padding(horizontal = 24.dp),
                contentAlignment = Alignment.CenterEnd
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_delete),
                    contentDescription = stringResource(id = R.string.delete),
                    tint = Color.White,
                    modifier = Modifier.size(36.dp)
                )
            }
        },
        content = { content() }
    )
}

@Composable
fun AnimatedWeatherIcon(
    @DrawableRes iconRes: Int,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "weather_anim")
    val rotation by infiniteTransition.animateFloat(initialValue = 0f, targetValue = 360f, animationSpec = infiniteRepeatable(animation = tween(8000, easing = LinearEasing), repeatMode = RepeatMode.Restart), label = "rotation")
    val bobbing by infiniteTransition.animateFloat(initialValue = -6f, targetValue = 6f, animationSpec = infiniteRepeatable(animation = tween(1500, easing = FastOutSlowInEasing), repeatMode = RepeatMode.Reverse), label = "bobbing")
    val raining by infiniteTransition.animateFloat(initialValue = -12f, targetValue = 12f, animationSpec = infiniteRepeatable(animation = tween(700, easing = LinearEasing), repeatMode = RepeatMode.Restart), label = "raining")
    val drifting by infiniteTransition.animateFloat(initialValue = -10f, targetValue = 10f, animationSpec = infiniteRepeatable(animation = tween(3000, easing = FastOutSlowInEasing), repeatMode = RepeatMode.Reverse), label = "drifting")
    val flashAlpha by infiniteTransition.animateFloat(initialValue = 0.3f, targetValue = 1f, animationSpec = infiniteRepeatable(animation = tween(300, easing = LinearEasing), repeatMode = RepeatMode.Reverse), label = "flash")
    val shake by infiniteTransition.animateFloat(initialValue = -3f, targetValue = 3f, animationSpec = infiniteRepeatable(animation = tween(80, easing = LinearEasing), repeatMode = RepeatMode.Reverse), label = "shake")
    val pulseScale by infiniteTransition.animateFloat(initialValue = 0.95f, targetValue = 1.05f, animationSpec = infiniteRepeatable(animation = tween(1200, easing = FastOutSlowInEasing), repeatMode = RepeatMode.Reverse), label = "pulse")

    val animatedModifier = modifier.then(
        when (iconRes) {
            R.drawable.ic_sunny -> Modifier.rotate(rotation)
            R.drawable.ic_clear_night -> Modifier.alpha(flashAlpha.coerceAtLeast(0.7f)).rotate(rotation * 0.1f)
            R.drawable.ic_snow -> Modifier.rotate(rotation * 0.5f).offset(y = bobbing.dp)
            R.drawable.ic_cloudy, R.drawable.ic_few_clouds -> Modifier.offset(y = bobbing.dp)
            R.drawable.ic_mist -> Modifier.offset(x = drifting.dp).alpha(flashAlpha.coerceAtLeast(0.5f))
            R.drawable.ic_rainbow -> Modifier.offset(y = bobbing.dp).scale(pulseScale)
            R.drawable.ic_rain, R.drawable.ic_rain_showers -> Modifier.offset(y = raining.dp)
            R.drawable.ic_thunderstorm -> Modifier.offset(x = shake.dp, y = shake.dp).alpha(flashAlpha)
            else -> Modifier
        }
    )

    Icon(painter = painterResource(id = iconRes), contentDescription = "Weather Icon", modifier = animatedModifier, tint = Color.Unspecified)
}