package com.example.ui.screens

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CompassCalibration
import androidx.compose.material.icons.filled.GpsFixed
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.viewmodel.PrayerViewModel
import com.example.ui.viewmodel.QuranViewModel
import kotlin.math.abs

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QiblaFinderScreen(
    prayerViewModel: PrayerViewModel,
    quranViewModel: QuranViewModel,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val selectedCity by prayerViewModel.selectedCity.collectAsState()
    val qiblaAngle by prayerViewModel.qiblaAngle.collectAsState()
    val distanceKm by prayerViewModel.distanceToKaabaKm.collectAsState()

    val appSettings by quranViewModel.appSettings.collectAsState()
    val isArabic = appSettings?.isArabicDefault ?: true

    var deviceHeading by remember { mutableFloatStateOf(0f) }
    var manualOffset by remember { mutableFloatStateOf(0f) }

    // Register Compass Hardware Sensors (Rotation Vector or Accelerometer + Magnetometer)
    DisposableEffect(Unit) {
        val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as? SensorManager
        val rotationVectorSensor = sensorManager?.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)
        val accelSensor = sensorManager?.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        val magnetSensor = sensorManager?.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)
        val orientationSensor = sensorManager?.getDefaultSensor(Sensor.TYPE_ORIENTATION)

        val gravity = FloatArray(3)
        val geomagnetic = FloatArray(3)

        val listener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent?) {
                if (event == null) return
                when (event.sensor.type) {
                    Sensor.TYPE_ROTATION_VECTOR -> {
                        val rotationMatrix = FloatArray(9)
                        val orientationValues = FloatArray(3)
                        SensorManager.getRotationMatrixFromVector(rotationMatrix, event.values)
                        SensorManager.getOrientation(rotationMatrix, orientationValues)
                        var azimuth = Math.toDegrees(orientationValues[0].toDouble()).toFloat()
                        azimuth = (azimuth + 360) % 360
                        deviceHeading = azimuth
                    }
                    Sensor.TYPE_ACCELEROMETER -> {
                        System.arraycopy(event.values, 0, gravity, 0, 3)
                        updateFromAccelAndMag()
                    }
                    Sensor.TYPE_MAGNETIC_FIELD -> {
                        System.arraycopy(event.values, 0, geomagnetic, 0, 3)
                        updateFromAccelAndMag()
                    }
                    Sensor.TYPE_ORIENTATION -> {
                        deviceHeading = (event.values[0] + 360) % 360
                    }
                }
            }

            private fun updateFromAccelAndMag() {
                val r = FloatArray(9)
                val i = FloatArray(9)
                if (SensorManager.getRotationMatrix(r, i, gravity, geomagnetic)) {
                    val orientation = FloatArray(3)
                    SensorManager.getOrientation(r, orientation)
                    var azimuth = Math.toDegrees(orientation[0].toDouble()).toFloat()
                    azimuth = (azimuth + 360) % 360
                    deviceHeading = azimuth
                }
            }

            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
        }

        if (rotationVectorSensor != null) {
            sensorManager.registerListener(listener, rotationVectorSensor, SensorManager.SENSOR_DELAY_UI)
        } else if (accelSensor != null && magnetSensor != null) {
            sensorManager.registerListener(listener, accelSensor, SensorManager.SENSOR_DELAY_UI)
            sensorManager.registerListener(listener, magnetSensor, SensorManager.SENSOR_DELAY_UI)
        } else if (orientationSensor != null) {
            sensorManager.registerListener(listener, orientationSensor, SensorManager.SENSOR_DELAY_UI)
        }

        onDispose {
            sensorManager?.unregisterListener(listener)
        }
    }

    // Effective heading including manual rotation adjustment for emulators/browsers
    val effectiveHeading = (deviceHeading + manualOffset + 360) % 360
    val targetNeedleRotation = ((qiblaAngle - effectiveHeading + 360) % 360).toFloat()
    val animatedNeedleRotation by animateFloatAsState(targetValue = targetNeedleRotation, label = "QiblaNeedleRotation")

    // Check if facing Kaaba within tolerance
    val isFacingKaaba = abs((animatedNeedleRotation + 180) % 360 - 180) <= 6f
    val dialBorderColor by animateColorAsState(
        targetValue = if (isFacingKaaba) Color(0xFF2E7D32) else MaterialTheme.colorScheme.primary,
        label = "DialBorderColor"
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (isArabic) "محدد اتجاه القبلة الشريفة" else "Qibla Direction Finder") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { triggerGpsLocationFetch(context, prayerViewModel) }) {
                        Icon(Icons.Default.GpsFixed, contentDescription = "GPS Location", tint = MaterialTheme.colorScheme.primary)
                    }
                }
            )
        },
        modifier = modifier.testTag("qibla_finder_screen")
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.LocationOn, contentDescription = "Location", tint = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = selectedCity,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "${qiblaAngle.toInt()}° • ${distanceKm.toInt()} KM ${if (isArabic) "إلى الكعبة المشرفة" else "to Holy Kaaba"}",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )

                if (isFacingKaaba) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Surface(
                        color = Color(0xFF2E7D32),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Text(
                            text = if (isArabic) "🕋 متوجه بضيافة القبلة الشريفة!" else "🕋 Directly Facing Holy Kaaba!",
                            color = Color.White,
                            style = MaterialTheme.typography.labelLarge,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // Interactive 3D Qibla Compass Visual
            Box(
                modifier = Modifier
                    .size(280.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .border(5.dp, dialBorderColor, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                // Compass Dial Background Canvas
                val primaryColor = MaterialTheme.colorScheme.primary
                val goldColor = MaterialTheme.colorScheme.tertiary
                
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val radius = size.width / 2 - 20
                    drawCircle(
                        color = primaryColor.copy(alpha = 0.2f),
                        radius = radius,
                        style = Stroke(width = 4.dp.toPx())
                    )
                }

                // Rotating Qibla Pointer Needle
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .rotate(animatedNeedleRotation),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.offset(y = (-60).dp)
                    ) {
                        // Stylized Kaaba Emblem Indicator
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(Color.Black)
                                .border(2.dp, if (isFacingKaaba) Color.Green else goldColor, RoundedCornerShape(10.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "🕋",
                                fontSize = 24.sp
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Golden Compass Pointer Arrow
                        Box(
                            modifier = Modifier
                                .width(8.dp)
                                .height(65.dp)
                                .background(if (isFacingKaaba) Color.Green else goldColor, RoundedCornerShape(4.dp))
                        )
                    }
                }

                // Center Pivot Node
                Box(
                    modifier = Modifier
                        .size(22.dp)
                        .clip(CircleShape)
                        .background(dialBorderColor)
                )
            }

            // Interactive Phone Rotation Simulator & Calibration Controls
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = if (isArabic) "محاكاة تحريك/تدوير الهاتف:" else "Simulate Device Movement:",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedButton(
                            onClick = { manualOffset = (manualOffset - 30f + 360) % 360 },
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
                        ) {
                            Text("⟲ -30°")
                        }
                        Text(
                            text = "${effectiveHeading.toInt()}°",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        OutlinedButton(
                            onClick = { manualOffset = (manualOffset + 30f) % 360 },
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
                        ) {
                            Text("+30° ⟳")
                        }
                    }
                }
            }

            // Bottom Instructions Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                shape = RoundedCornerShape(16.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.CompassCalibration,
                        contentDescription = "Instruction",
                        tint = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = if (isArabic) "قم بتدوير الجهاز أو استخدام أزرار التدوير حتى تعطي الإبرة اتجاه القبلة بوضوح." else "Rotate device or use the rotation buttons until the golden arrow aligns directly with Kaaba.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
        }
    }
}
