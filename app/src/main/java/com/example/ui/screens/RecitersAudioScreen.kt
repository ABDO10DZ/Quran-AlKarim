package com.example.ui.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.model.Reciter
import com.example.ui.viewmodel.AudioViewModel
import com.example.ui.viewmodel.QuranViewModel

@Composable
fun RecitersAudioScreen(
    audioViewModel: AudioViewModel,
    quranViewModel: QuranViewModel,
    modifier: Modifier = Modifier
) {
    val reciters by audioViewModel.reciters.collectAsState()
    val appSettings by quranViewModel.appSettings.collectAsState()

    val settings = appSettings ?: com.example.data.model.AppSettings()
    val isArabic = settings.isArabicDefault
    val selectedReciterId = settings.selectedReciterId

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
            .testTag("reciters_audio_screen"),
        contentPadding = PaddingValues(bottom = 80.dp)
    ) {
        item {
            Text(
                text = if (isArabic) "المقصرون وإدارة التلاوات أوفلاين" else "Audio Reciters & Offline Downloads",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = if (isArabic) "اختر قارئك المفضل وقم بتحميل التلاوة العذبة للاستماع بدون إنترنت" else "Select reciters & download high-quality audio packages for offline listening",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Storage Overview Header Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                shape = RoundedCornerShape(16.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.CloudDownload,
                            contentDescription = "Downloads",
                            tint = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (isArabic) "مساحة التلاوات المحملة أوفلاين" else "Offline Storage Usage",
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        val totalDownloaded = reciters.filter { it.isDownloaded }.sumOf { it.downloadedSizeMb }
                        Text(
                            text = "${totalDownloaded.toInt()} MB ${if (isArabic) "مستخدمة من الذاكرة" else "used for offline audio"}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = if (isArabic) "قائمة كبار القرّاء" else "Famous Quran Reciters",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))
        }

        items(reciters) { reciter ->
            ReciterItemCard(
                reciter = reciter,
                isSelected = reciter.id == selectedReciterId,
                isArabic = isArabic,
                onSelectReciter = {
                    quranViewModel.updateSettings { it.copy(selectedReciterId = reciter.id) }
                },
                onToggleDownload = {
                    audioViewModel.toggleDownload(reciter.id)
                }
            )
        }
    }
}

@Composable
fun ReciterItemCard(
    reciter: Reciter,
    isSelected: Boolean,
    isArabic: Boolean,
    onSelectReciter: () -> Unit,
    onToggleDownload: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
            .clickable { onSelectReciter() }
            .testTag("reciter_item_${reciter.id}"),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer
            else MaterialTheme.colorScheme.surface
        ),
        shape = RoundedCornerShape(16.dp),
        border = if (isSelected) androidx.compose.foundation.BorderStroke(2.dp, MaterialTheme.colorScheme.primary) else null
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            RadioButton(
                selected = isSelected,
                onClick = onSelectReciter
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = if (isArabic) reciter.nameArabic else reciter.nameEnglish,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "${reciter.country} • ${reciter.totalSizeMb.toInt()} MB",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Download Status / Button Action
            Surface(
                onClick = onToggleDownload,
                shape = RoundedCornerShape(20.dp),
                color = if (reciter.isDownloaded) MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                else MaterialTheme.colorScheme.surfaceVariant
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = if (reciter.isDownloaded) Icons.Default.CheckCircle else Icons.Default.Download,
                        contentDescription = "Download Status",
                        modifier = Modifier.size(18.dp),
                        tint = if (reciter.isDownloaded) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (reciter.isDownloaded)
                            if (isArabic) "محمّل أوفلاين" else "Downloaded"
                        else
                            if (isArabic) "تحميل" else "Download",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = if (reciter.isDownloaded) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}
