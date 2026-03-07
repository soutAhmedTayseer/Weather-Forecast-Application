package com.example.weatherforecastapplication.alertsScreen.view

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
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.os.ConfigurationCompat
import com.example.weatherforecastapplication.R
import com.example.weatherforecastapplication.alertsScreen.viewmodel.AlertsViewModel
import com.example.weatherforecastapplication.data.models.WeatherAlert
import com.example.weatherforecastapplication.ui.theme.component.AnimatedWeatherIcon
import com.example.weatherforecastapplication.ui.theme.component.RetroAlertDialog
import com.example.weatherforecastapplication.ui.theme.component.RetroSnackbarHost
import com.example.weatherforecastapplication.ui.theme.component.SplashAnimation
import com.example.weatherforecastapplication.ui.theme.component.getWeatherIcon
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlertsScreen(viewModel: AlertsViewModel) {
    val context = LocalContext.current
    val alerts by viewModel.alertsList.collectAsState()
    val activeLocation by viewModel.locationFlow.collectAsState()
    val isRefreshing by viewModel.isRefreshing.collectAsState()

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
            onConfirm = {
                viewModel.deleteAlert(alertToDelete!!)
                alertToDelete = null
            },
            onDismiss = { alertToDelete = null }
        )
    }

    Scaffold(
        containerColor = Color.Transparent,
        snackbarHost = { RetroSnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text(stringResource(id = R.string.weather_alerts), style = MaterialTheme.typography.displaySmall.copy(fontWeight = FontWeight.Bold)) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent, titleContentColor = MaterialTheme.colorScheme.onBackground)
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { alertToEdit = null; showAddDialog = true },
                containerColor = Color(0xFF74B9FF), contentColor = Color.White
            ) { Icon(Icons.Default.Add, contentDescription = stringResource(id = R.string.add_alert)) }
        }
    ) { paddingValues ->
        PullToRefreshBox(
            isRefreshing = isRefreshing, onRefresh = { viewModel.refreshAlerts(isManualRefresh = true) },
            modifier = Modifier.fillMaxSize().padding(paddingValues)
        ) {
            if (isRefreshing) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { SplashAnimation() }
            } else if (alerts.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text(stringResource(id = R.string.no_active_alerts), color = MaterialTheme.colorScheme.onBackground) }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(16.dp),
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlertItemCard(alert: WeatherAlert, onDeleteClick: () -> Unit, onClick: () -> Unit) {
    val context = LocalContext.current

    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = {
            if (it == SwipeToDismissBoxValue.EndToStart) {
                onDeleteClick()
                false
            } else false
        }
    )

    // FIX: Using the dynamic locale from Configuration ensures AM/PM and months translate to Arabic!
    val currentLocale = ConfigurationCompat.getLocales(context.resources.configuration).get(0) ?: Locale.getDefault()
    val dateFormat = SimpleDateFormat("MMM dd, h:mm a", currentLocale)

    SwipeToDismissBox(
        state = dismissState, enableDismissFromStartToEnd = false,
        backgroundContent = {
            Box(Modifier.fillMaxSize().background(Color(0xFFFF7675), RoundedCornerShape(24.dp)).padding(horizontal = 24.dp), contentAlignment = Alignment.CenterEnd) {
                Icon(Icons.Default.Delete, contentDescription = stringResource(id = R.string.delete), tint = Color.White, modifier = Modifier.size(28.dp))
            }
        }
    ) {
        ElevatedCard(
            onClick = onClick, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.98f)), elevation = CardDefaults.elevatedCardElevation(6.dp)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(64.dp).background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f), RoundedCornerShape(16.dp)), contentAlignment = Alignment.Center) {
                        AnimatedWeatherIcon(iconRes = getWeatherIcon(alert.currentIcon), modifier = Modifier.size(48.dp))
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(text = alert.cityName, style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold), color = MaterialTheme.colorScheme.onSurface)
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(text = alert.currentDescription.replaceFirstChar { it.uppercase() }, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.SemiBold)
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
                HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f))
                Spacer(modifier = Modifier.height(16.dp))
                Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                    Column {
                        Text(text = "${stringResource(id = R.string.from)} ${dateFormat.format(Date(alert.startTime))}", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(text = "${stringResource(id = R.string.to)} ${dateFormat.format(Date(alert.endTime))}", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        if (alert.isAlarm) Badge(containerColor = Color(0xFFFFD93D).copy(alpha = 0.2f), contentColor = Color(0xFFD4AC0D)) { Icon(Icons.Default.Warning, contentDescription = stringResource(id = R.string.alarm), modifier = Modifier.size(20.dp).padding(2.dp)) }
                        if (alert.isNotification) Badge(containerColor = Color(0xFF74B9FF).copy(alpha = 0.2f), contentColor = Color(0xFF0984E3)) { Icon(Icons.Default.Notifications, contentDescription = stringResource(id = R.string.notification), modifier = Modifier.size(20.dp).padding(2.dp)) }
                    }
                }
            }
        }
    }
}

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

    val notifSavedMsg = context.getString(R.string.notification_sound_saved)
    val alarmSavedMsg = context.getString(R.string.alarm_sound_saved)

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

    // FIX: Format dates using the properly updated locale!
    val currentLocale = ConfigurationCompat.getLocales(context.resources.configuration).get(0) ?: Locale.getDefault()
    val dateFormat = SimpleDateFormat("MMM dd, yyyy\nh:mm a", currentLocale)
    val maxFutureTime = now + (5L * 24 * 60 * 60 * 1000)
    val isEditMode = existingAlert != null

    Dialog(onDismissRequest = onDismiss, properties = DialogProperties(dismissOnClickOutside = false, usePlatformDefaultWidth = false)) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Card(
                shape = RoundedCornerShape(28.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth(0.95f).fillMaxHeight(0.85f).border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(28.dp))
            ) {
                Column(modifier = Modifier.padding(24.dp).fillMaxWidth().verticalScroll(scrollState)) {
                    Text(text = if (isEditMode) stringResource(id = R.string.edit_live_monitor) else stringResource(id = R.string.new_live_monitor), fontWeight = FontWeight.ExtraBold, style = MaterialTheme.typography.headlineSmall, color = MaterialTheme.colorScheme.onSurface)
                    Text(stringResource(id = R.string.app_fetches_real_weather), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(modifier = Modifier.height(24.dp))

                    Text(stringResource(id = R.string.possible_outcomes), fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(modifier = Modifier.height(12.dp))

                    LazyRow(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        val conditions = listOf("01d" to R.string.sunny, "03d" to R.string.cloudy, "10d" to R.string.rain, "13d" to R.string.snow, "11d" to R.string.storm)
                        items(conditions) { (icon, titleResId) ->
                            Column(
                                modifier = Modifier.size(72.dp).background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f), RoundedCornerShape(16.dp)),
                                horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center
                            ) {
                                AnimatedWeatherIcon(iconRes = getWeatherIcon(icon), modifier = Modifier.size(28.dp))
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(stringResource(id = titleResId), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(32.dp))
                    Text(stringResource(id = R.string.active_duration), fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        OutlinedButton(onClick = { pickDateTime(context) { startTime = it } }, modifier = Modifier.weight(1f), shape = RoundedCornerShape(16.dp), contentPadding = PaddingValues(16.dp)) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(Icons.Default.DateRange, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                Spacer(Modifier.height(4.dp))
                                Text(stringResource(id = R.string.starts), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text(dateFormat.format(Date(startTime)), textAlign = TextAlign.Center, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                            }
                        }
                        OutlinedButton(onClick = { pickDateTime(context) { endTime = it } }, modifier = Modifier.weight(1f), shape = RoundedCornerShape(16.dp), contentPadding = PaddingValues(16.dp)) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(Icons.Default.DateRange, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                Spacer(Modifier.height(4.dp))
                                Text(stringResource(id = R.string.ends), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text(dateFormat.format(Date(endTime)), textAlign = TextAlign.Center, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(32.dp))
                    Text(stringResource(id = R.string.alert_options_sounds), fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(modifier = Modifier.height(8.dp))

                    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)), shape = RoundedCornerShape(16.dp)) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                                Checkbox(checked = isNotification, onCheckedChange = { isNotification = it }, colors = CheckboxDefaults.colors(checkedColor = MaterialTheme.colorScheme.primary))
                                Text(stringResource(id = R.string.standard_notification), color = MaterialTheme.colorScheme.onSurface, modifier = Modifier.weight(1f))
                                if (isNotification) {
                                    FilledTonalIconButton(onClick = { notifToneLauncher.launch(Intent(RingtoneManager.ACTION_RINGTONE_PICKER).apply { putExtra(RingtoneManager.EXTRA_RINGTONE_TYPE, RingtoneManager.TYPE_NOTIFICATION); putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_DEFAULT, true) }) }) { Icon(Icons.Default.PlayArrow, contentDescription = stringResource(id = R.string.select_tone)) }
                                }
                            }
                            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f))
                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                                Checkbox(checked = isAlarm, onCheckedChange = { isAlarm = it }, colors = CheckboxDefaults.colors(checkedColor = Color(0xFFFFD93D)))
                                Text(stringResource(id = R.string.loud_full_screen_alarm), color = MaterialTheme.colorScheme.onSurface, modifier = Modifier.weight(1f))
                                if (isAlarm) {
                                    FilledTonalIconButton(onClick = { alarmToneLauncher.launch(Intent(RingtoneManager.ACTION_RINGTONE_PICKER).apply { putExtra(RingtoneManager.EXTRA_RINGTONE_TYPE, RingtoneManager.TYPE_ALARM); putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_DEFAULT, true) }) }) { Icon(Icons.Default.PlayArrow, contentDescription = stringResource(id = R.string.select_tone)) }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(32.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                        TextButton(onClick = onDismiss) { Text(stringResource(id = R.string.cancel), color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold) }
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
                            }
                        ) { Text(if (isEditMode) stringResource(id = R.string.save_changes) else stringResource(id = R.string.start_monitor), color = Color.White, fontWeight = FontWeight.ExtraBold) }
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