package com.example.weatherforecastapplication.presentation.alertsScreen.view

import android.annotation.SuppressLint
import android.app.Activity
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.weatherforecastapplication.R
import com.example.weatherforecastapplication.core.theme.component.AnimatedWeatherIcon
import com.example.weatherforecastapplication.core.theme.component.EmptyStateComponent
import com.example.weatherforecastapplication.core.theme.component.RetroAlertDialog
import com.example.weatherforecastapplication.core.theme.component.RetroCard
import com.example.weatherforecastapplication.core.theme.component.RetroCornerShape
import com.example.weatherforecastapplication.core.theme.component.RetroFAB
import com.example.weatherforecastapplication.core.theme.component.RetroSnackbarHost
import com.example.weatherforecastapplication.core.theme.component.RetroSwipeToDeleteContainer
import com.example.weatherforecastapplication.core.theme.component.RetroTopAppBar
import com.example.weatherforecastapplication.core.theme.component.SolidSwipeRefreshLayout
import com.example.weatherforecastapplication.core.theme.component.getWeatherIcon
import com.example.weatherforecastapplication.data.models.entities.WeatherAlert
import com.example.weatherforecastapplication.presentation.alertsScreen.viewmodel.AlertsViewModel
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@Composable
fun AlertsScreen(viewModel: AlertsViewModel) {
    val context = LocalContext.current
    val alerts by viewModel.alertsList.collectAsState()
    val activeLocation by viewModel.locationFlow.collectAsState()

    var showAddDialog by remember { mutableStateOf(false) }
    var alertToEdit by remember { mutableStateOf<WeatherAlert?>(null) }
    var alertToDelete by remember { mutableStateOf<WeatherAlert?>(null) }

    val snackbarHostState = remember { SnackbarHostState() }

    if (showAddDialog) {
        AddAlertDialog(
            existingAlert = alertToEdit, lat = activeLocation.first, lon = activeLocation.second,
            onDismiss = { showAddDialog = false; alertToEdit = null },
            onSave = { newAlert -> viewModel.saveAlert(context, newAlert); showAddDialog = false; alertToEdit = null }
        )
    }

    if (alertToDelete != null) {
        RetroAlertDialog(
            title = stringResource(id = R.string.delete_alert_title),
            message = stringResource(id = R.string.delete_alert_message),
            onConfirm = { viewModel.deleteAlert(alertToDelete!!); alertToDelete = null },
            onDismiss = { alertToDelete = null }
        )
    }

    Scaffold(
        containerColor = Color.Transparent,
        snackbarHost = { RetroSnackbarHost(snackbarHostState) },
        topBar = { RetroTopAppBar(title = stringResource(id = R.string.weather_alerts)) },
        floatingActionButton = {
            RetroFAB(
                contentDescription = stringResource(id = R.string.add_alert),
                onClick = { alertToEdit = null; showAddDialog = true }
            )
        }
    ) { paddingValues ->
        SolidSwipeRefreshLayout(
            onRefresh = { viewModel.refreshAlerts(isManualRefresh = true) },
            loadingMessage = stringResource(id = R.string.checking_active_alerts),
            gifRes = R.drawable.finnloading, // Finn ASSIGNED HERE
            modifier = Modifier.fillMaxSize().padding(paddingValues)
        ) {
            val softBlueTempColor = Color(0xFF74B9FF)

            if (alerts.isEmpty()) {
                // NEW EMPTY STATE WITH Finn GIF
                EmptyStateComponent(
                    message = stringResource(id = R.string.no_active_alerts),
                    gifRes = R.drawable.finnloading,
                    textColor = softBlueTempColor
                )
            }else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 100.dp)
                ) {
                    items(alerts, key = { it.id }) { alert ->
                        AlertItemCard(
                            alert = alert,
                            onDeleteClick = { alertToDelete = alert },
                            onClick = { alertToEdit = alert; showAddDialog = true }
                        )
                    }
                }
            }
        }
    }
}

@SuppressLint("LocalContextConfigurationRead")
@Composable
fun AlertItemCard(alert: WeatherAlert, onDeleteClick: () -> Unit, onClick: () -> Unit) {
    val context = LocalContext.current
    val currentLocale = context.resources.configuration.locales.get(0) ?: Locale.getDefault()
    val dateFormat = SimpleDateFormat("MMM dd, h:mm a", currentLocale)

    RetroSwipeToDeleteContainer(onDelete = onDeleteClick) {
        RetroCard(onClick = onClick) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    AnimatedWeatherIcon(iconRes = getWeatherIcon(alert.currentIcon), modifier = Modifier.size(50.dp))

                    Spacer(modifier = Modifier.width(16.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(text = alert.cityName, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurface)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(text = alert.currentDescription.replaceFirstChar { it.uppercase() }, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    IconButton(onClick = onDeleteClick, modifier = Modifier.size(48.dp)) {
                        Icon(painter = painterResource(id = R.drawable.ic_delete), contentDescription = stringResource(id = R.string.delete), tint = Color.Unspecified, modifier = Modifier.size(40.dp))
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))
                HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f))
                Spacer(modifier = Modifier.height(12.dp))

                Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                    Column {
                        Text(text = "${stringResource(id = R.string.from)} ${dateFormat.format(Date(alert.startTime))}", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(text = "${stringResource(id = R.string.to)} ${dateFormat.format(Date(alert.endTime))}", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                        if (alert.isAlarm) {
                            Icon(painter = painterResource(id = R.drawable.ic_alarm), contentDescription = stringResource(id = R.string.alarm), modifier = Modifier.size(50.dp), tint = Color.Unspecified)
                        }
                        if (alert.isNotification) {
                            Icon(painter = painterResource(id = R.drawable.ic_notification), contentDescription = stringResource(id = R.string.notification), modifier = Modifier.size(50.dp), tint = Color.Unspecified)
                        }
                    }
                }
            }
        }
    }
}

@SuppressLint("LocalContextConfigurationRead")
@Composable
fun AddAlertDialog(existingAlert: WeatherAlert?, lat: Double, lon: Double, onDismiss: () -> Unit, onSave: (WeatherAlert) -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val scrollState = rememberScrollState()
    val dialogSnackbarHostState = remember { SnackbarHostState() }

    val now = System.currentTimeMillis()
    var startTime by remember { mutableLongStateOf(existingAlert?.startTime ?: now) }
    var endTime by remember { mutableLongStateOf(existingAlert?.endTime ?: (now + 60000)) }
    var isNotification by remember { mutableStateOf(existingAlert?.isNotification ?: true) }
    var isAlarm by remember { mutableStateOf(existingAlert?.isAlarm ?: false) }
    var notificationTone by remember { mutableStateOf(existingAlert?.notificationToneUri ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION).toString()) }
    var alarmTone by remember { mutableStateOf(existingAlert?.alarmToneUri ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM).toString()) }

    val notifSavedMsg = stringResource(id = R.string.notification_sound_saved)
    val alarmSavedMsg = stringResource(id = R.string.alarm_sound_saved)

    val notifToneLauncher = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.getParcelableExtra<Uri>(RingtoneManager.EXTRA_RINGTONE_PICKED_URI)?.let {
                notificationTone = it.toString(); scope.launch { dialogSnackbarHostState.showSnackbar(notifSavedMsg) }
            }
        }
    }

    val alarmToneLauncher = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.getParcelableExtra<Uri>(RingtoneManager.EXTRA_RINGTONE_PICKED_URI)?.let {
                alarmTone = it.toString(); scope.launch { dialogSnackbarHostState.showSnackbar(alarmSavedMsg) }
            }
        }
    }

    val currentLocale = context.resources.configuration.locales.get(0) ?: Locale.getDefault()
    val dateFormat = SimpleDateFormat("MMM dd, yyyy\nh:mm a", currentLocale)
    val maxFutureTime = now + (5L * 24 * 60 * 60 * 1000)
    val isEditMode = existingAlert != null

    Dialog(onDismissRequest = onDismiss, properties = DialogProperties(dismissOnClickOutside = false, usePlatformDefaultWidth = false)) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            RetroCard(modifier = Modifier.fillMaxWidth(0.95f).fillMaxHeight(0.85f)) {
                Column(modifier = Modifier.padding(24.dp).fillMaxWidth().verticalScroll(scrollState)) {
                    Text(text = if (isEditMode) stringResource(id = R.string.edit_live_monitor) else stringResource(id = R.string.new_live_monitor), style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.onSurface)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(stringResource(id = R.string.app_fetches_real_weather), style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))
                    Spacer(modifier = Modifier.height(24.dp))

                    Text(stringResource(id = R.string.possible_outcomes), style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurface)
                    Spacer(modifier = Modifier.height(12.dp))

                    LazyRow(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        val conditions = listOf("01d" to R.string.sunny, "03d" to R.string.cloudy, "10d" to R.string.rain, "13d" to R.string.snow, "11d" to R.string.storm)
                        items(conditions) { (icon, titleResId) ->
                            Column(
                                modifier = Modifier.size(72.dp).background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f), RetroCornerShape),
                                horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center
                            ) {
                                AnimatedWeatherIcon(iconRes = getWeatherIcon(icon), modifier = Modifier.size(28.dp))
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(stringResource(id = titleResId), style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurface)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(32.dp))
                    Text(stringResource(id = R.string.active_duration), style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurface)
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        OutlinedButton(onClick = { pickDateTime(context) { startTime = it } }, modifier = Modifier.weight(1f), shape = RetroCornerShape, contentPadding = PaddingValues(16.dp)) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(Icons.Default.DateRange, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                Spacer(Modifier.height(6.dp))
                                Text(stringResource(id = R.string.starts), style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurface)
                                Spacer(Modifier.height(6.dp))
                                Text(dateFormat.format(Date(startTime)), style = MaterialTheme.typography.labelMedium, textAlign = TextAlign.Center, color = MaterialTheme.colorScheme.onSurface)
                            }
                        }
                        OutlinedButton(onClick = { pickDateTime(context) { endTime = it } }, modifier = Modifier.weight(1f), shape = RetroCornerShape, contentPadding = PaddingValues(16.dp)) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(Icons.Default.DateRange, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                Spacer(Modifier.height(6.dp))
                                Text(stringResource(id = R.string.ends), style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurface)
                                Spacer(Modifier.height(6.dp))
                                Text(dateFormat.format(Date(endTime)), style = MaterialTheme.typography.labelMedium, textAlign = TextAlign.Center, color = MaterialTheme.colorScheme.onSurface)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(32.dp))
                    Text(stringResource(id = R.string.alert_options_sounds), style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurface)
                    Spacer(modifier = Modifier.height(8.dp))

                    RetroCard {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                                Checkbox(checked = isNotification, onCheckedChange = { isNotification = it }, colors = CheckboxDefaults.colors(checkedColor = MaterialTheme.colorScheme.primary))
                                Text(stringResource(id = R.string.standard_notification), style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurface, modifier = Modifier.weight(1f))
                                if (isNotification) {
                                    FilledTonalIconButton(onClick = { notifToneLauncher.launch(Intent(RingtoneManager.ACTION_RINGTONE_PICKER).apply { putExtra(RingtoneManager.EXTRA_RINGTONE_TYPE, RingtoneManager.TYPE_NOTIFICATION); putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_DEFAULT, true) }) }) { Icon(Icons.Default.PlayArrow, contentDescription = stringResource(id = R.string.select_tone)) }
                                }
                            }
                            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f))
                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                                Checkbox(checked = isAlarm, onCheckedChange = { isAlarm = it }, colors = CheckboxDefaults.colors(checkedColor = MaterialTheme.colorScheme.primary))
                                Text(stringResource(id = R.string.loud_full_screen_alarm), style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurface, modifier = Modifier.weight(1f))
                                if (isAlarm) {
                                    FilledTonalIconButton(onClick = { alarmToneLauncher.launch(Intent(RingtoneManager.ACTION_RINGTONE_PICKER).apply { putExtra(RingtoneManager.EXTRA_RINGTONE_TYPE, RingtoneManager.TYPE_ALARM); putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_DEFAULT, true) }) }) { Icon(Icons.Default.PlayArrow, contentDescription = stringResource(id = R.string.select_tone)) }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(32.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                        TextButton(onClick = onDismiss) { Text(stringResource(id = R.string.cancel), style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurface) }
                        Spacer(modifier = Modifier.width(12.dp))

                        val startPastErr = context.getString(R.string.start_time_past_error)
                        val endTimeErr = context.getString(R.string.end_time_before_start_error)
                        val exceedDaysErr = context.getString(R.string.alert_exceed_days_error)
                        val selectMethodErr = context.getString(R.string.select_alert_method_error)

                        Button(
                            onClick = {
                                val currentTime = System.currentTimeMillis()
                                if (startTime < currentTime - 120000 && !isEditMode) { scope.launch { dialogSnackbarHostState.showSnackbar(startPastErr) }; return@Button }
                                if (endTime <= startTime) { scope.launch { dialogSnackbarHostState.showSnackbar(endTimeErr) }; return@Button }
                                if (endTime > maxFutureTime) { scope.launch { dialogSnackbarHostState.showSnackbar(exceedDaysErr) }; return@Button }
                                if (!isNotification && !isAlarm) { scope.launch { dialogSnackbarHostState.showSnackbar(selectMethodErr) }; return@Button }

                                val alertIdToSave = existingAlert?.id ?: 0
                                val currentIcon = existingAlert?.currentIcon ?: "01d"
                                val currentDesc = existingAlert?.currentDescription ?: "Fetching..."

                                onSave(WeatherAlert(alertIdToSave, startTime, endTime, isAlarm, isNotification, lat, lon, "", notificationTone, alarmTone, currentIcon, currentDesc))
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary, contentColor = MaterialTheme.colorScheme.onPrimary),
                            shape = RetroCornerShape
                        ) { Text(if (isEditMode) stringResource(id = R.string.save_changes) else stringResource(id = R.string.start_monitor), style = MaterialTheme.typography.bodyLarge) }
                    }
                }
            }
            RetroSnackbarHost(hostState = dialogSnackbarHostState, modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 16.dp))
        }
    }
}

fun pickDateTime(context: Context, onTimeSelected: (Long) -> Unit) {
    val c = Calendar.getInstance()
    DatePickerDialog(context, { _, y, m, d -> TimePickerDialog(context, { _, h, min -> val s = Calendar.getInstance(); s.set(y, m, d, h, min, 0); onTimeSelected(s.timeInMillis) }, c.get(Calendar.HOUR_OF_DAY), c.get(Calendar.MINUTE), false).show() }, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH)).show()
}