package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessibilityNew
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.TextFields
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.viewmodel.QuranViewModel

@Composable
fun SettingsAccessibilityScreen(
    viewModel: QuranViewModel,
    modifier: Modifier = Modifier
) {
    val appSettings by viewModel.appSettings.collectAsState()
    val settings = appSettings ?: com.example.data.model.AppSettings()

    val isArabic = settings.isArabicDefault
    val fontSizeSp = settings.fontSizeSp

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
            .testTag("settings_accessibility_screen"),
        contentPadding = PaddingValues(bottom = 80.dp)
    ) {
        item {
            Text(
                text = if (isArabic) "الإعدادات وإمكانيات الوصول" else "Settings & Accessibility",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = if (isArabic) "تخصيص السمة، الحجم، اللغة، ودعم ضعاف البصر" else "Customize theme, text scaling, language & accessibility features",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(16.dp))

            // 1. Language Preference Section
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Language, contentDescription = "Language", tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = if (isArabic) "لغة التطبيق (App Language)" else "App Primary Language",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        FilterChip(
                            selected = isArabic,
                            onClick = { viewModel.updateSettings { it.copy(isArabicDefault = true) } },
                            label = { Text("العربية (Default 🇸🇦)") },
                            modifier = Modifier.weight(1f)
                        )
                        FilterChip(
                            selected = !isArabic,
                            onClick = { viewModel.updateSettings { it.copy(isArabicDefault = false) } },
                            label = { Text("English (Optional 🇬🇧)") },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 2. Customizable Dark Mode & Theme Modes Section
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Palette, contentDescription = "Theme", tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = if (isArabic) "السمة والنمط البصري (Theme Mode)" else "Theme & Color Scheme",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        ThemeOptionRow(
                            title = if (isArabic) "الزمردي الداكن (Emerald Dark)" else "Emerald Luxury Dark (Default)",
                            isSelected = settings.themeMode == "EMERALD_DARK",
                            onSelect = { viewModel.updateSettings { it.copy(themeMode = "EMERALD_DARK") } }
                        )
                        ThemeOptionRow(
                            title = if (isArabic) "الورقي الفاتح (Classic Light)" else "Classic Light Parchment",
                            isSelected = settings.themeMode == "CLASSIC_LIGHT",
                            onSelect = { viewModel.updateSettings { it.copy(themeMode = "CLASSIC_LIGHT") } }
                        )
                        ThemeOptionRow(
                            title = if (isArabic) "عالي التباين - لضعاف البصر (High Contrast)" else "High Contrast (Black & Gold for Visually Impaired)",
                            isSelected = settings.themeMode == "HIGH_CONTRAST",
                            onSelect = { viewModel.updateSettings { it.copy(themeMode = "HIGH_CONTRAST") } }
                        )
                        ThemeOptionRow(
                            title = if (isArabic) "الكتب القديمة (Sepia Warm)" else "Sepia Warm Book Page",
                            isSelected = settings.themeMode == "SEPIA",
                            onSelect = { viewModel.updateSettings { it.copy(themeMode = "SEPIA") } }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 3. Scalable Typography & Font Size Slider
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.TextFields, contentDescription = "Font Size", tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = if (isArabic) "حجم خط آيات القرآن الكريم" else "Verse Typography & Scalability",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "${if (isArabic) "حجم الخط الحقيقي" else "Current Font Size"}: ${fontSizeSp.toInt()} sp",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Slider(
                        value = fontSizeSp,
                        onValueChange = { newSize -> viewModel.updateSettings { it.copy(fontSizeSp = newSize) } },
                        valueRange = 18f..42f,
                        steps = 12,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("font_size_slider")
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Live Scalable Font Preview Box
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .padding(16.dp)
                    ) {
                        Text(
                            text = "بِسْمِ ٱللَّهِ ٱلرَّحْمَٰنِ ٱلرَّحِيمِ ۝",
                            fontSize = fontSizeSp.sp,
                            fontFamily = FontFamily.Serif,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 4. Visually Impaired Accessibility Features Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.AccessibilityNew, contentDescription = "Accessibility", tint = MaterialTheme.colorScheme.onPrimaryContainer)
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = if (isArabic) "تسهيلات إمكانية الوصول (Accessibility)" else "Accessibility Features for Visually Impaired",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = if (isArabic) "• خطوط مكبرة فائقة الوضوح (Scalable Fonts)\n• تباين عالي جداً للمكونات والأزرار (High Contrast)\n• دعم التلاوة الصوتية الناطقة بكل الأزرار (TalkBack Compatible)\n• أزرار ومجالات لمس واسعة (>= 48dp Touch Targets)" else "• Ultra-clear scalable Uthmani typography\n• High contrast theme mode support\n• Full screen reader / TalkBack semantic integration\n• Generous touch target sizes >= 48dp",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.9f)
                    )
                }
            }
        }
    }
}

@Composable
fun ThemeOptionRow(
    title: String,
    isSelected: Boolean,
    onSelect: () -> Unit
) {
    Surface(
        onClick = onSelect,
        shape = RoundedCornerShape(10.dp),
        color = if (isSelected) MaterialTheme.colorScheme.primaryContainer
        else MaterialTheme.colorScheme.surfaceVariant,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            RadioButton(selected = isSelected, onClick = onSelect)
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
            )
        }
    }
}
