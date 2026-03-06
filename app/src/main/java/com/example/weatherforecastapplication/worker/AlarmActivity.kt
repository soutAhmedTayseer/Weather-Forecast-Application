package com.example.weatherforecastapplication.worker

import android.app.KeyguardManager
import android.content.Context
import android.media.MediaPlayer
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.weatherforecastapplication.data.local.CityDatabase
import com.example.weatherforecastapplication.ui.theme.WeatherForecastApplicationTheme
import com.example.weatherforecastapplication.ui.theme.component.GlobalWeatherBackground
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.Calendar

class AlarmActivity : ComponentActivity() {
    private var mediaPlayer: MediaPlayer? = null
    private var vibrator: Vibrator? = null
    private var actionHandled = false // Tracks if user clicked a button
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

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true)
            setTurnScreenOn(true)
            val keyguardManager = getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager
            keyguardManager.requestDismissKeyguard(this, null)
        } else {
            @Suppress("DEPRECATION")
            window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON or WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD or WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON)
        }

        val message = intent.getStringExtra("ALERT_MESSAGE") ?: "Weather conditions met!"
        alertId = intent.getIntExtra("ALERT_ID", -1)
        val customTone = intent.getStringExtra("ALARM_TONE")

        try {
            val uri = if (!customTone.isNullOrEmpty()) Uri.parse(customTone) else android.media.RingtoneManager.getDefaultUri(android.media.RingtoneManager.TYPE_ALARM)
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
                    Box(modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.4f)), contentAlignment = Alignment.Center) {
                        Card(
                            shape = RoundedCornerShape(24.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f)),
                            modifier = Modifier.fillMaxWidth(0.9f).border(3.dp, Color(0xFFFF7675), RoundedCornerShape(24.dp))
                        ) {
                            Column(modifier = Modifier.padding(32.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(Icons.Default.Warning, contentDescription = null, tint = Color(0xFFFFD93D), modifier = Modifier.size(100.dp))
                                Spacer(Modifier.height(16.dp))
                                Text("WEATHER ALARM", style = MaterialTheme.typography.displaySmall, color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
                                Spacer(Modifier.height(16.dp))
                                Text(message, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f), textAlign = TextAlign.Center)

                                Spacer(Modifier.height(48.dp))

                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {

                                    // SNOOZE BUTTON (DYNAMIC DATABASE UPDATE)
                                    Button(
                                        onClick = {
                                            actionHandled = true
                                            stopAudioAndVibration()

                                            // UPDATE DATABASE & RESCHEDULE
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
                                        colors = ButtonDefaults.buttonColors(containerColor = Color.Gray),
                                        shape = RoundedCornerShape(12.dp)
                                    ) { Text("Snooze", color = Color.White, fontWeight = FontWeight.Bold) }

                                    // DISMISS BUTTON (DELETES ALARM)
                                    Button(
                                        onClick = {
                                            actionHandled = true
                                            stopAudioAndVibration()

                                            // CLEANUP DATABASE
                                            CoroutineScope(Dispatchers.IO).launch {
                                                CityDatabase.getDatabase(this@AlarmActivity).alertDao().deleteAlertById(alertId)
                                            }

                                            (getSystemService(Context.NOTIFICATION_SERVICE) as android.app.NotificationManager).cancel(alertId)
                                            finish()
                                        },
                                        modifier = Modifier.weight(1f).height(56.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF7675)),
                                        shape = RoundedCornerShape(12.dp)
                                    ) { Text("Dismiss", color = Color.White, fontWeight = FontWeight.Bold) }
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