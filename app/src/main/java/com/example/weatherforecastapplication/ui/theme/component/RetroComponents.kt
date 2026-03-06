package com.example.weatherforecastapplication.ui.theme.component

import androidx.annotation.DrawableRes
import androidx.compose.animation.core.*
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.offset
import androidx.compose.material3.Icon
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import com.example.weatherforecastapplication.R

// --- REUSABLE RETRO SNACKBAR ---
@Composable
fun RetroSnackbarHost(hostState: SnackbarHostState, modifier: Modifier = Modifier) {
    SnackbarHost(
        hostState = hostState,
        modifier = modifier,
        snackbar = { data ->
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.98f)),
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth()
                    .border(3.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape(12.dp))
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Notifications, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = data.visuals.message,
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    )
}

// --- REUSABLE ANIMATED WEATHER ICON ---
@Composable
fun AnimatedWeatherIcon(
    @DrawableRes iconRes: Int,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "weather_anim")

    // 1. Smooth Continuous Rotation (For Sun & Snow)
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f, targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(8000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ), label = "rotation"
    )

    // 2. Gentle Vertical Bobbing (For Clouds & Rainbow)
    val bobbing by infiniteTransition.animateFloat(
        initialValue = -6f, targetValue = 6f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ), label = "bobbing"
    )

    // 3. Falling Loop (For Rain & Showers)
    val raining by infiniteTransition.animateFloat(
        initialValue = -12f, targetValue = 12f,
        animationSpec = infiniteRepeatable(
            animation = tween(700, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ), label = "raining"
    )

    // 4. Horizontal Drifting (For Mist)
    val drifting by infiniteTransition.animateFloat(
        initialValue = -10f, targetValue = 10f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ), label = "drifting"
    )

    // 5. Opacity Flash (For Lightning & Night Moon)
    val flashAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f, targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(300, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ), label = "flash"
    )

    // 6. Violent Shake (For Thunderstorm)
    val shake by infiniteTransition.animateFloat(
        initialValue = -3f, targetValue = 3f,
        animationSpec = infiniteRepeatable(
            animation = tween(80, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ), label = "shake"
    )

    // 7. Gentle Pulse Scaling (For Rainbow)
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.95f, targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ), label = "pulse"
    )

    // Apply specific animations based on the icon
    val animatedModifier = modifier.then(
        when (iconRes) {
            R.drawable.ic_sunny ->
                Modifier.rotate(rotation)

            R.drawable.ic_clear_night ->
                Modifier.alpha(flashAlpha.coerceAtLeast(0.7f)).rotate(rotation * 0.1f)

            R.drawable.ic_snow ->
                Modifier.rotate(rotation * 0.5f).offset(y = bobbing.dp)

            R.drawable.ic_cloudy,
            R.drawable.ic_few_clouds ->
                Modifier.offset(y = bobbing.dp)

            R.drawable.ic_mist ->
                Modifier.offset(x = drifting.dp).alpha(flashAlpha.coerceAtLeast(0.5f))

            R.drawable.ic_rainbow ->
                Modifier.offset(y = bobbing.dp).scale(pulseScale)

            R.drawable.ic_rain,
            R.drawable.ic_rain_showers ->
                Modifier.offset(y = raining.dp)

            R.drawable.ic_thunderstorm ->
                Modifier.offset(x = shake.dp, y = shake.dp).alpha(flashAlpha)

            else -> Modifier
        }
    )

    Icon(
        painter = painterResource(id = iconRes),
        contentDescription = "Weather Icon",
        modifier = animatedModifier,
        tint = Color.Unspecified
    )
}