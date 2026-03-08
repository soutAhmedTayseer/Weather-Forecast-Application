package com.example.weatherforecastapplication.worker

import android.app.KeyguardManager
import android.content.Context
import android.media.MediaPlayer
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.weatherforecastapplication.R
import com.example.weatherforecastapplication.data.local.CityDatabase
import com.example.weatherforecastapplication.ui.theme.WeatherForecastApplicationTheme
import com.example.weatherforecastapplication.ui.theme.component.GlobalWeatherBackground
import com.example.weatherforecastapplication.ui.theme.component.RetroCard
import com.example.weatherforecastapplication.ui.theme.component.RetroCornerShape
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.Calendar

class AlarmActivity : ComponentActivity() {

    private var mediaPlayer: MediaPlayer? = null
    private var vibrator: Vibrator? = null
    private var actionHandled = false
    private var alertId = -1

    private fun stopAudioAndVibration() {
        try {
            mediaPlayer?.stop()
            mediaPlayer?.release()
            mediaPlayer = null
            vibrator?.cancel()
        } catch (e: Exception) { e.printStackTrace() }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Wake up screen and show over lockscreen
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true)
            setTurnScreenOn(true)
            val keyguardManager = getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager
            keyguardManager.requestDismissKeyguard(this, null)
        } else {
            @Suppress("DEPRECATION")
            window.addFlags(
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON or
                        WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD or
                        WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                        WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
            )
        }

        val alertMessage = intent.getStringExtra("ALERT_MESSAGE") ?: "Weather condition met!"
        alertId = intent.getIntExtra("ALERT_ID", -1)
        val customTone = intent.getStringExtra("ALARM_TONE")

        try {
            val uri = if (!customTone.isNullOrEmpty()) Uri.parse(customTone) else RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
            mediaPlayer = MediaPlayer.create(this, uri)
            mediaPlayer?.isLooping = true
            mediaPlayer?.start()
        } catch (e: Exception) { e.printStackTrace() }

        try {
            vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                (getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager).defaultVibrator
            } else { @Suppress("DEPRECATION") getSystemService(Context.VIBRATOR_SERVICE) as Vibrator }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator?.vibrate(VibrationEffect.createWaveform(longArrayOf(0, 1000, 1000), 0))
            } else { @Suppress("DEPRECATION") vibrator?.vibrate(longArrayOf(0, 1000, 1000), 0) }
        } catch (e: Exception) { e.printStackTrace() }

        setContent {
            val currentHour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
            val isGlobalDayTime = currentHour in 6..18

            WeatherForecastApplicationTheme(isDayTime = isGlobalDayTime) {
                GlobalWeatherBackground(isDayTime = isGlobalDayTime) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        // DRY: Reused RetroCard for the Alarm popup!
                        RetroCard(modifier = Modifier.fillMaxWidth(0.9f)) {
                            Column(
                                modifier = Modifier.padding(32.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(
                                    painter = painterResource(id = R.drawable.ic_alarm),
                                    contentDescription = null,
                                    tint = Color.Unspecified,
                                    modifier = Modifier.size(80.dp)
                                )
                                Spacer(modifier = Modifier.height(24.dp))

                                // Typographic enforcement for Retro Pixel look
                                Text(
                                    text = "WEATHER ALERT",
                                    style = MaterialTheme.typography.displaySmall,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    textAlign = TextAlign.Center
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                Text(
                                    text = alertMessage,
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    textAlign = TextAlign.Center
                                )
                                Spacer(modifier = Modifier.height(32.dp))

                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {

                                    // SNOOZE BUTTON
                                    Button(
                                        onClick = {
                                            actionHandled = true
                                            stopAudioAndVibration()

                                            CoroutineScope(Dispatchers.IO).launch {
                                                val db = CityDatabase.getDatabase(this@AlarmActivity)
                                                val alert = db.alertDao().getAlertById(alertId)
                                                if (alert != null) {
                                                    val snoozeOffset = 10 * 60 * 1000L
                                                    val snoozedAlert = alert.copy(
                                                        startTime = System.currentTimeMillis() + snoozeOffset,
                                                        endTime = alert.endTime + snoozeOffset
                                                    )
                                                    db.alertDao().insertAlert(snoozedAlert)
                                                    AlarmScheduler.scheduleAlarm(this@AlarmActivity, snoozedAlert)
                                                }
                                            }

                                            (getSystemService(Context.NOTIFICATION_SERVICE) as android.app.NotificationManager).cancel(alertId)
                                            finish()
                                        },
                                        modifier = Modifier.weight(1f).height(56.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                                        shape = RetroCornerShape
                                    ) { Text("Snooze", color = MaterialTheme.colorScheme.onSurface, style = MaterialTheme.typography.bodyLarge) }

                                    // DISMISS BUTTON
                                    Button(
                                        onClick = {
                                            actionHandled = true
                                            stopAudioAndVibration()

                                            CoroutineScope(Dispatchers.IO).launch {
                                                CityDatabase.getDatabase(this@AlarmActivity).alertDao().deleteAlertById(alertId)
                                            }

                                            (getSystemService(Context.NOTIFICATION_SERVICE) as android.app.NotificationManager).cancel(alertId)
                                            finish()
                                        },
                                        modifier = Modifier.weight(1f).height(56.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                                        shape = RetroCornerShape
                                    ) { Text("Dismiss", color = Color.White, style = MaterialTheme.typography.bodyLarge) }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        stopAudioAndVibration()
        // If they swipe the app away without clicking a button, auto-dismiss from DB
        if (!actionHandled && alertId != -1) {
            CoroutineScope(Dispatchers.IO).launch {
                CityDatabase.getDatabase(this@AlarmActivity).alertDao().deleteAlertById(alertId)
            }
        }
    }
}