package com.example.weatherforecastapplication.core.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.example.weatherforecastapplication.R

// Added ExtraBold to ensure the system can pull the thickest version of the font
val AppFontFamily = FontFamily(
    Font(R.font.handjet, FontWeight.Normal),
    Font(R.font.handjet, FontWeight.Medium),
    Font(R.font.handjet, FontWeight.SemiBold),
    Font(R.font.handjet, FontWeight.Bold),
    Font(R.font.handjet, FontWeight.ExtraBold)
)

val Typography = Typography(
    displayLarge = TextStyle(
        fontFamily = AppFontFamily,
        fontWeight = FontWeight.ExtraBold, // Max thickness for the giant temperature
        fontSize = 88.sp,                  // Enlarged (was 72)
        lineHeight = 96.sp
    ),
    displaySmall = TextStyle(
        fontFamily = AppFontFamily,
        fontWeight = FontWeight.ExtraBold, // Max thickness for App Bar Titles
        fontSize = 40.sp,                  // Enlarged (was 32)
    ),
    titleLarge = TextStyle(
        fontFamily = AppFontFamily,
        fontWeight = FontWeight.Bold,      // Bolder
        fontSize = 28.sp,                  // Enlarged (was 24)
    ),
    titleMedium = TextStyle(
        fontFamily = AppFontFamily,
        fontWeight = FontWeight.Bold,      // Bumped from SemiBold
        fontSize = 24.sp,                  // Enlarged (was 20)
    ),
    bodyLarge = TextStyle(
        fontFamily = AppFontFamily,
        fontWeight = FontWeight.SemiBold,  // Bumped from Medium
        fontSize = 20.sp,                  // Enlarged (was 16)
    ),
    labelMedium = TextStyle(
        fontFamily = AppFontFamily,
        fontWeight = FontWeight.SemiBold,  // Bumped from Medium
        fontSize = 18.sp,                  // Enlarged (was 14)
    )
)