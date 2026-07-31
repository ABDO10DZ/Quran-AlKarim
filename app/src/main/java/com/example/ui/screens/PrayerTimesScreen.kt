package com.example.ui.screens

import android.Manifest
import android.content.Context
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CompassCalibration
import androidx.compose.material.icons.filled.GpsFixed
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.NotificationsOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.model.PrayerTime
import com.example.data.repository.PrayerRepository
import com.example.ui.viewmodel.PrayerViewModel
import com.example.ui.viewmodel.QuranViewModel
import java.util.Locale

@Composable
fun PrayerTimesScreen(
    prayerViewModel: PrayerViewModel,
    quranViewModel: QuranViewModel,
    onOpenQibla: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val selectedCity by prayerViewModel.selectedCity.collectAsState()
    val prayerTimes by prayerViewModel.prayerTimes.collectAsState()
    val isLoading by prayerViewModel.isLoadingPrayerTimes.collectAsState()
    val simulatedNotification by prayerViewModel.simulatedNotification.collectAsState()

    val appSettings by quranViewModel.appSettings.collectAsState()
    val isArabic = appSettings?.isArabicDefault ?: true

    var showCityDropdown by remember { mutableStateOf(false) }

    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val granted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        if (granted) {
            triggerGpsLocationFetch(context, prayerViewModel)
        }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
            .testTag("prayer_times_screen"),
        contentPadding = PaddingValues(bottom = 80.dp)
    ) {
        // Notification Toast Banner
        simulatedNotification?.let { alert ->
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp)
                        .clickable { prayerViewModel.clearNotificationBanner() },
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.NotificationsActive, contentDescription = "Alert", tint = MaterialTheme.colorScheme.onPrimary)
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = alert,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }

        // Header Title & City Selector
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = if (isArabic) "مواقيت الصلاة اليومية" else "Daily Prayer Times & Alerts",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = if (isArabic) "التوقيت الدقيق للأذان والتنبيهات" else "Accurate Athan times & daily alerts",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                IconButton(onClick = onOpenQibla) {
                    Icon(
                        imageVector = Icons.Default.CompassCalibration,
                        contentDescription = "Qibla Finder",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(32.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Location Selector Dropdown Bar & GPS Button
            Box(modifier = Modifier.fillMaxWidth()) {
                Surface(
                    onClick = { showCityDropdown = true },
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.LocationOn, contentDescription = "City", tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = selectedCity,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.weight(1f)
                        )
                        if (isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(18.dp),
                                strokeWidth = 2.dp,
                                color = MaterialTheme.colorScheme.primary
                            )
                        } else {
                            Text(
                                text = if (isArabic) "تغيير المدينة ▾" else "Change City ▾",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }

                DropdownMenu(
                    expanded = showCityDropdown,
                    onDismissRequest = { showCityDropdown = false }
                ) {
                    DropdownMenuItem(
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.GpsFixed, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(if (isArabic) "تحديد الموقع الحالي باستخدام GPS" else "Use My GPS Location", fontWeight = FontWeight.Bold)
                            }
                        },
                        onClick = {
                            showCityDropdown = false
                            locationPermissionLauncher.launch(
                                arrayOf(
                                    Manifest.permission.ACCESS_FINE_LOCATION,
                                    Manifest.permission.ACCESS_COARSE_LOCATION
                                )
                            )
                        }
                    )
                    DropdownMenuItem(
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.LocationOn, contentDescription = null, tint = MaterialTheme.colorScheme.secondary, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(if (isArabic) "التحديد التلقائي حسب المنطقة والتوقيت" else "Auto-Detect via Timezone")
                            }
                        },
                        onClick = {
                            prayerViewModel.detectAndSetLocationFromTimeZone()
                            showCityDropdown = false
                        }
                    )
                    HorizontalDivider()
                    PrayerRepository.citiesList.forEach { city ->
                        DropdownMenuItem(
                            text = { Text(city.first) },
                            onClick = {
                                prayerViewModel.updateCity(city.first)
                                showCityDropdown = false
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Next Prayer Countdown Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = if (isArabic) "الصلاة القادمة: الظهر" else "Next Prayer: Dhuhr",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "12:18 PM",
                        style = MaterialTheme.typography.displaySmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = if (isArabic) "متبقي 01 ساعة و 24 دقيقة" else "In 01 hour and 24 minutes",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = if (isArabic) "جدول الصلوات الخمس" else "Daily Prayers Schedule",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))
        }

        items(prayerTimes) { prayer ->
            PrayerTimeCardItem(
                prayer = prayer,
                isArabic = isArabic,
                onToggleAlert = { prayerViewModel.togglePrayerAlert(prayer.nameEnglish) }
            )
        }
    }
}

@Composable
fun PrayerTimeCardItem(
    prayer: PrayerTime,
    isArabic: Boolean,
    onToggleAlert: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .testTag("prayer_item_${prayer.nameEnglish}"),
        colors = CardDefaults.cardColors(
            containerColor = if (prayer.isNext) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
            else MaterialTheme.colorScheme.surface
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (isArabic) prayer.nameArabic.take(1) else prayer.nameEnglish.take(1),
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = if (isArabic) prayer.nameArabic else prayer.nameEnglish,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                if (prayer.isNext) {
                    Text(
                        text = if (isArabic) "الصلاة القادمة ●" else "Next Prayer ●",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            Text(
                text = prayer.time,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.width(12.dp))

            IconButton(onClick = onToggleAlert) {
                Icon(
                    imageVector = if (prayer.alertEnabled) Icons.Default.NotificationsActive else Icons.Default.NotificationsOff,
                    contentDescription = "Prayer Alert",
                    tint = if (prayer.alertEnabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

fun triggerGpsLocationFetch(context: Context, viewModel: PrayerViewModel) {
    try {
        val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        val gpsLoc = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
        val networkLoc = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
        val passiveLoc = locationManager.getLastKnownLocation(LocationManager.PASSIVE_PROVIDER)

        val loc: Location? = gpsLoc ?: networkLoc ?: passiveLoc

        if (loc != null) {
            val label = String.format(Locale.US, "GPS (%.2f°N, %.2f°E)", loc.latitude, loc.longitude)
            viewModel.updateGpsLocation(loc.latitude, loc.longitude, label)
        } else {
            locationManager.requestLocationUpdates(
                LocationManager.NETWORK_PROVIDER, 0L, 0f,
                object : LocationListener {
                    override fun onLocationChanged(l: Location) {
                        val label = String.format(Locale.US, "GPS (%.2f°N, %.2f°E)", l.latitude, l.longitude)
                        viewModel.updateGpsLocation(l.latitude, l.longitude, label)
                        locationManager.removeUpdates(this)
                    }
                    override fun onProviderEnabled(provider: String) {}
                    override fun onProviderDisabled(provider: String) {}
                }
            )
        }
    } catch (e: SecurityException) {
        e.printStackTrace()
    } catch (e: Exception) {
        e.printStackTrace()
    }
}
