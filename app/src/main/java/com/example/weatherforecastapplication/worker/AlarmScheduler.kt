package com.example.weatherforecastapplication.worker

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.example.weatherforecastapplication.data.models.WeatherAlert

object AlarmScheduler {
    fun scheduleAlarm(context: Context, alert: WeatherAlert) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, WeatherAlarmReceiver::class.java).apply {
            putExtra("ALERT_ID", alert.id)
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context, alert.id, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (alarmManager.canScheduleExactAlarms()) {
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, alert.startTime, pendingIntent)
            } else {
                alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, alert.startTime, pendingIntent)
            }
        } else {
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, alert.startTime, pendingIntent)
        }
    }
}