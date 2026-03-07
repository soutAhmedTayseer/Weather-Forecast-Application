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
        fontSize = 56.sp, // Scaled slightly down for Pixel fonts as they are naturally wide
        lineHeight = 64.sp
    ),
    displaySmall = TextStyle(
        fontFamily = MinecraftFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 28.sp,
    ),
    titleLarge = TextStyle(
        fontFamily = MinecraftFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 20.sp,
    ),
    bodyLarge = TextStyle(
        fontFamily = MinecraftFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
    ),
    labelMedium = TextStyle(
        fontFamily = MinecraftFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 10.sp, // Bottom Nav text
    )
)