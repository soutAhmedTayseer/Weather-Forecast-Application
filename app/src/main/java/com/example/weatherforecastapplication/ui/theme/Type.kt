package com.example.weatherforecastapplication.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.example.weatherforecastapplication.R

val MinecraftFontFamily = FontFamily(
    Font(R.font.press_start_2p, FontWeight.Normal)
)

val Typography = Typography(
    displayLarge = TextStyle(
        fontFamily = MinecraftFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 48.sp, // Reduced to prevent wrapping
        lineHeight = 56.sp
    ),
    displaySmall = TextStyle(
        fontFamily = MinecraftFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 22.sp, // Reduced
    ),
    titleLarge = TextStyle(
        fontFamily = MinecraftFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp, // Reduced
    ),
    titleMedium = TextStyle(
        fontFamily = MinecraftFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp, // Used for medium headers
    ),
    bodyLarge = TextStyle(
        fontFamily = MinecraftFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 10.sp, // Reduced for Grid Data / Forecasts
    ),
    labelMedium = TextStyle(
        fontFamily = MinecraftFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 8.sp, // Reduced for small labels (Bottom Nav, Tile Labels)
    )
)