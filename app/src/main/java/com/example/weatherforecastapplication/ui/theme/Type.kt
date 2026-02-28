package com.example.weatherforecastapplication.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.example.weatherforecastapplication.R

val CartoonFontFamily = FontFamily(
    Font(R.font.baloo_regular, FontWeight.Normal)
)

val Typography = Typography(
    displayLarge = TextStyle(
        fontFamily = CartoonFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 64.sp, // Big temperature text
        lineHeight = 72.sp
    ),
    titleLarge = TextStyle(
        fontFamily = CartoonFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 24.sp, // Screen headers
    ),
    bodyLarge = TextStyle(
        fontFamily = CartoonFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 18.sp, // Main reading text
    ),
    labelMedium = TextStyle(
        fontFamily = CartoonFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp, // Bottom Nav text
    )
)