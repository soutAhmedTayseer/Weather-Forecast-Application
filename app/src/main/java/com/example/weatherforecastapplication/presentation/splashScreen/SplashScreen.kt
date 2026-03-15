package com.example.weatherforecastapplication.presentation.splashScreen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.weatherforecastapplication.R
import com.example.weatherforecastapplication.core.navigation.ScreenRoute
import com.example.weatherforecastapplication.core.navigation.ScreenRoute.*
import com.example.weatherforecastapplication.core.theme.component.SplashAnimation
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(navController: NavController) {
    LaunchedEffect(key1 = true) {
        delay(3000)
        navController.navigate(Home.route) {
            popUpTo(Splash.route) { inclusive = true }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        SplashAnimation()

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = stringResource(id = R.string.slogan_text),
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.primary
        )
    }
}