package com.example.weatherforecastapplication.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.doublePreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_settings")

class SettingsRepository(private val context: Context) {

    companion object {
        val LOCATION_METHOD = stringPreferencesKey("location_method")
        val HOME_LAT = doublePreferencesKey("home_lat")
        val HOME_LON = doublePreferencesKey("home_lon")

        // ADD THESE FOR REAL GPS
        val GPS_LAT = doublePreferencesKey("gps_lat")
        val GPS_LON = doublePreferencesKey("gps_lon")

        val TEMP_UNIT = stringPreferencesKey("temp_unit")
        val WIND_UNIT = stringPreferencesKey("wind_unit")
        val LANGUAGE = stringPreferencesKey("language")
    }

    val locationMethodFlow: Flow<String> = context.dataStore.data.map { it[LOCATION_METHOD] ?: "gps" }

    val homeLatFlow: Flow<Double> = context.dataStore.data.map { it[HOME_LAT] ?: 31.2001 }
    val homeLonFlow: Flow<Double> = context.dataStore.data.map { it[HOME_LON] ?: 29.9187 }

    // READ REAL GPS
    val gpsLatFlow: Flow<Double> = context.dataStore.data.map { it[GPS_LAT] ?: 31.2001 }
    val gpsLonFlow: Flow<Double> = context.dataStore.data.map { it[GPS_LON] ?: 29.9187 }

    val tempUnitFlow: Flow<String> = context.dataStore.data.map { it[TEMP_UNIT] ?: "metric" }
    val windUnitFlow: Flow<String> = context.dataStore.data.map { it[WIND_UNIT] ?: "m/s" }
    val languageFlow: Flow<String> = context.dataStore.data.map { it[LANGUAGE] ?: "en" }

    suspend fun saveLocationMethod(method: String) {
        context.dataStore.edit { it[LOCATION_METHOD] = method }
    }

    suspend fun saveHomeLocation(lat: Double, lon: Double) {
        context.dataStore.edit {
            it[HOME_LAT] = lat
            it[HOME_LON] = lon
            it[LOCATION_METHOD] = "map"
        }
    }

    // WRITE REAL GPS
    suspend fun saveGpsLocation(lat: Double, lon: Double) {
        context.dataStore.edit {
            it[GPS_LAT] = lat
            it[GPS_LON] = lon
        }
    }

    suspend fun saveTempUnit(unit: String) { context.dataStore.edit { it[TEMP_UNIT] = unit } }
    suspend fun saveWindUnit(unit: String) { context.dataStore.edit { it[WIND_UNIT] = unit } }
    suspend fun saveLanguage(lang: String) { context.dataStore.edit { it[LANGUAGE] = lang } }
}