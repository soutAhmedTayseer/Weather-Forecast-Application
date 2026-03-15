package com.example.weatherforecastapplication.data.models.dataClasses

import com.google.gson.annotations.SerializedName

data class LocationData(
    val name: String,
    @SerializedName("local_names") val localNames: Map<String, String>?,
    val lat: Double,
    val lon: Double,
    val country: String,
    val state: String?
) {
    // Helper to format the display name nicely in the dropdown
    val displayName: String
        get() = if (state != null) "$name, $state, $country" else "$name, $country"
}