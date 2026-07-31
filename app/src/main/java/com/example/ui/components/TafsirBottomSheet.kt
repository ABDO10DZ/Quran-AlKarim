package com.example.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoStories
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.FormatSize
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Ayah
import com.example.data.repository.AyahTafsir
import com.example.data.repository.TafsirRepository

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TafsirBottomSheet(
    tafsir: AyahTafsir?,
    currentAyahs: List<Ayah>,
    selectedSourceId: String,
    isArabic: Boolean,
    onSourceSelected: (String) -> Unit,
    onAyahSelected: (Ayah) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var tafsirFontSizeSp by remember { mutableStateOf(16f) }
    val scrollState = rememberScrollState()

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ) {
        Column(
            modifier = modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 12.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.AutoStories,
                        contentDescription = "Tafsir",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(28.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = if (isArabic) "كتب التفسير المعتمدة" else "Trusted Quranic Tafsir",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        tafsir?.let {
                            Text(
                                text = "آية ${it.ayahNumber.toArabicDigits()} (Ayah ${it.ayahNumber})",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }

                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = "Close")
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Tafsir Sources Selection Chips (Ibn Kathir, Tabari, Jalalayn, Muyassar)
            Text(
                text = if (isArabic) "اختر الكتاب أو المفسّر:" else "Select Tafsir Book:",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(6.dp))

            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(TafsirRepository.availableTafsirSources) { source ->
                    FilterChip(
                        selected = source.id == selectedSourceId,
                        onClick = { onSourceSelected(source.id) },
                        label = { Text(if (isArabic) source.nameArabic else source.nameEnglish) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Ayah Verse Navigator Strip (Scroll through all verses in current Surah)
            if (currentAyahs.isNotEmpty()) {
                Text(
                    text = if (isArabic) "التنقل بين آيات السورة:" else "Navigate Verses:",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(6.dp))

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(currentAyahs) { ayah ->
                        val isSelected = tafsir?.ayahNumber == ayah.ayahNumber
                        SuggestionChip(
                            onClick = { onAyahSelected(ayah) },
                            label = { Text("آية ${ayah.ayahNumber.toArabicDigits()}") },
                            colors = SuggestionChipDefaults.suggestionChipColors(
                                containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant
                            )
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Main Scrollable Tafsir Content Block
            Box(
                modifier = Modifier
                    .weight(1f, fill = false)
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                    .border(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.2f), RoundedCornerShape(16.dp))
                    .padding(16.dp)
            ) {
                Column(modifier = Modifier.verticalScroll(scrollState)) {
                    tafsir?.let { item ->
                        // Original Ayah snippet
                        Text(
                            text = item.ayahTextArabic.replace(Regex("<.*?>"), ""),
                            fontSize = 18.sp,
                            fontFamily = FontFamily.Serif,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(12.dp))
                        HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f))
                        Spacer(modifier = Modifier.height(12.dp))

                        // Scholar description
                        val source = TafsirRepository.availableTafsirSources.find { s -> s.id == item.sourceId }
                        source?.let { s ->
                            Text(
                                text = "📖 ${if (isArabic) s.nameArabic else s.nameEnglish} - ${s.scholar}",
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.secondary,
                                fontWeight = FontWeight.SemiBold
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                        }

                        // Full Tafsir Text Body
                        Text(
                            text = item.tafsirText,
                            fontSize = tafsirFontSizeSp.sp,
                            lineHeight = (tafsirFontSizeSp * 1.55f).sp,
                            color = MaterialTheme.colorScheme.onSurface,
                            fontFamily = FontFamily.Serif
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Bottom controls: Font size adjustment & Copy Tafsir button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.FormatSize, contentDescription = "Font size", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(modifier = Modifier.width(6.dp))
                    Slider(
                        value = tafsirFontSizeSp,
                        onValueChange = { tafsirFontSizeSp = it },
                        valueRange = 14f..26f,
                        modifier = Modifier.width(130.dp)
                    )
                }

                Button(
                    onClick = {
                        tafsir?.let { item ->
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            val clip = ClipData.newPlainText("Tafsir", "${item.ayahTextArabic}\n\nتفسير:\n${item.tafsirText}")
                            clipboard.setPrimaryClip(clip)
                            Toast.makeText(context, if (isArabic) "تم نسخ التفسير 📋" else "Tafsir copied 📋", Toast.LENGTH_SHORT).show()
                        }
                    }
                ) {
                    Icon(Icons.Default.ContentCopy, contentDescription = "Copy")
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(if (isArabic) "نسخ التفسير" else "Copy Tafsir")
                }
            }
        }
    }
}
