package com.example.weatherforecastapplication.data.models

data class ForecastResponseApi(
    val cod: String,
    val message: Int,
    val cnt: Int,
    val list: List<ForecastItem>,
    val city: City
)

data class ForecastItem(
    val dt: Long,
    val main: MainData,
    val weather: List<Weather>,
    val wind: Wind,
    val dt_txt: String
)

data class MainData(
    val temp: Double,
    val pressure: Int,
    val humidity: Int
)

data class Weather(
    val id: Int,
    val main: String,
    val description: String,
    val icon: String
)

data class Wind(
    val speed: Double
)

data class City(
    val id: Int,
    val name: String,
    val country: String
)