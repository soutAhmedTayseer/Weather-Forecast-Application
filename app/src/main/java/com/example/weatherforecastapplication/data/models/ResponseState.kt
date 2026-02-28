package com.example.weatherforecastapplication.data.models

sealed class ResponseState<out T> {
    data class Success<T>(val data: T) : ResponseState<T>()
    data class Error(val message: String) : ResponseState<Nothing>()
    object Loading : ResponseState<Nothing>()
}