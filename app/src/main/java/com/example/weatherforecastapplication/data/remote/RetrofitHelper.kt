package com.example.weatherforecastapplication.data.remote

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitHelper {
    private const val BASE_URL = "https://api.openweathermap.org/"

    val retrofitInstance: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    val weatherApiService: WeatherApiService by lazy {
        retrofitInstance.create(WeatherApiService::class.java)
    }
}