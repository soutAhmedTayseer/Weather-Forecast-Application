package com.example.weatherforecastapplication.navigation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState

@Composable
fun BottomNavigationBar(navController: NavController) {
    val items = listOf(
        ScreenRoute.Home,
        ScreenRoute.Favorites,
        ScreenRoute.Alerts,
        ScreenRoute.Settings
    )

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    // Floating Pill-Shaped Background
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 24.dp, end = 24.dp, bottom = 24.dp) // Pushes it up from the bottom
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(elevation = 8.dp, shape = CircleShape) // Cartoonish drop shadow
                .background(color = MaterialTheme.colorScheme.surface, shape = CircleShape)
                .padding(horizontal = 8.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            items.forEach { item ->
                val isSelected = currentRoute == item.route
                CustomBottomNavItem(
                    item = item,
                    isSelected = isSelected,
                    onClick = {
                        navController.navigate(item.route) {
                            navController.graph.startDestinationRoute?.let { route ->
                                popUpTo(route) { saveState = true }
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                )
            }
        }
    }
}

@Composable
fun CustomBottomNavItem(
    item: ScreenRoute,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    // Removes the default gray ripple effect for a cleaner look
    val interactionSource = remember { MutableInteractionSource() }

    Row(
        modifier = Modifier
            .clip(CircleShape)
            .clickable(
                interactionSource = interactionSource,
                indication = null, // No standard ripple
                onClick = onClick
            )
            // Soft background pill for the active item
            .background(
                color = if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.15f) else Color.Transparent,
                shape = CircleShape
            )
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
// Custom SVG Icon
        item.icon?.let { iconRes ->
            Icon(
                painter = painterResource(id = iconRes),
                contentDescription = item.title,
                // Tell Compose to use the original colors of your SVG!
                tint = Color.Unspecified,
                modifier = Modifier.size(24.dp)
            )
        }

        // Animated Title Reveal
        AnimatedVisibility(
            visible = isSelected
        ) {
            Text(
                text = item.title,
                modifier = Modifier.padding(start = 8.dp),
                color = MaterialTheme.colorScheme.primary,
                // Uses the Baloo font we set up earlier!
                style = MaterialTheme.typography.labelMedium
            )
        }
    }
}