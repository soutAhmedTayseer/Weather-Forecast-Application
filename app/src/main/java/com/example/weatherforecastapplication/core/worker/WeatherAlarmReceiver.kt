package com.example.weatherforecastapplication.core.worker

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.example.weatherforecastapplication.core.utils.NotificationUtils
import com.example.weatherforecastapplication.data.local.db.CityDatabase
import com.example.weatherforecastapplication.data.local.localDataSource.CityLocationLocalDataSourceImpl
import com.example.weatherforecastapplication.data.models.stateManagement.ResponseState
import com.example.weatherforecastapplication.data.remote.network.RetrofitHelper
import com.example.weatherforecastapplication.data.remote.remoteDataSource.WeatherRemoteDataSourceImpl
import com.example.weatherforecastapplication.data.repository.WeatherRepositoryImpl
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class WeatherAlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val alertId = intent.getIntExtra("ALERT_ID", -1)
        if (alertId == -1) return

        val pendingResult = goAsync()

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val db = CityDatabase.getDatabase(context)
                val alert = db.alertDao().getAlertById(alertId) ?: return@launch

                val repo = WeatherRepositoryImpl(
                    WeatherRemoteDataSourceImpl(RetrofitHelper.weatherApiService),
                    CityLocationLocalDataSourceImpl(db.cityLocationDao()),
                    db.weatherDao(),
                    db.alertDao()
                )

                var liveIcon = "01d"
                var liveDesc = "Unknown conditions"
                var liveTemp = "0"

                repo.getFiveDayForecast(alert.lat, alert.lon, "metric", "en").collect { state ->
                    if (state is ResponseState.Success) {
                        val nowWeather = state.data.list.firstOrNull()
                        liveIcon = nowWeather?.weather?.firstOrNull()?.icon ?: "01d"
                        liveDesc = nowWeather?.weather?.firstOrNull()?.description ?: "Clear"
                        liveTemp = nowWeather?.main?.temp?.toInt()?.toString() ?: "0"
                    }
                }

                val alertMessage = "Current Weather in ${alert.cityName}: $liveDesc at $liveTemp°C"

                // IMPORTANT: We update the DB with the live weather so the UI card looks fresh
                db.alertDao().insertAlert(alert.copy(currentIcon = liveIcon, currentDescription = "$liveDesc, $liveTemp°C"))

                if (alert.isAlarm) {
                    NotificationUtils.sendFullScreenAlarm(context, "Weather Update!", alertMessage, alert.id, alert.alarmToneUri)
                    val alarmIntent = Intent(context, AlarmActivity::class.java).apply {
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP)
                        putExtra("ALERT_MESSAGE", alertMessage)
                        putExtra("ALERT_ID", alert.id)
                    }
                    try { context.startActivity(alarmIntent) } catch (e: Exception) {}

                    // ⚠️ DO NOT DELETE THE ALERT HERE! AlarmActivity will handle deletion/snoozing!

                } else if (alert.isNotification) {
                    NotificationUtils.sendNotification(context, "Weather Update!", alertMessage, alert.notificationToneUri)
                    // If it's ONLY a notification, we delete it because there's no UI to dismiss it.
                    db.alertDao().deleteAlertById(alertId)
                }

            } catch (e: Exception) {
                Log.e("WeatherAlarm", "Failed to process alarm", e)
            } finally {
                pendingResult.finish()
            }
        }
    }
}