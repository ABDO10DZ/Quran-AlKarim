package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.TajweedRule
import com.example.ui.viewmodel.QuranViewModel

@Composable
fun TajweedGuideScreen(
    viewModel: QuranViewModel,
    modifier: Modifier = Modifier
) {
    val tajweedRules = viewModel.tajweedRules
    val appSettings by viewModel.appSettings.collectAsState()

    val isArabic = appSettings?.isArabicDefault ?: true

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
            .testTag("tajweed_guide_screen"),
        contentPadding = PaddingValues(bottom = 80.dp)
    ) {
        item {
            Text(
                text = if (isArabic) "دليل أحكام التجويد الملون" else "Tajweed Rules & Pronunciation Guide",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = if (isArabic) "تعلم قواعد النطق الصحيح وتلاوة القرآن الكريم بالتلوين التوضيحي" else "Learn proper Quranic pronunciation with color-coded rules & examples",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Color Legend Header Grid
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = if (isArabic) "مفتاح الألوان التوضيحي 🎨" else "Tajweed Color Legend 🎨",
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        LegendBadge("الغنة", Color(0xFF2E7D32))
                        LegendBadge("الإقلاب", Color(0xFF1565C0))
                        LegendBadge("الإدغام", Color(0xFF7B1FA2))
                        LegendBadge("الإخفاء", Color(0xFFE65100))
                        LegendBadge("القلقلة", Color(0xFFC62828))
                        LegendBadge("المدود", Color(0xFFF57F17))
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }

        items(tajweedRules) { rule ->
            TajweedRuleCard(
                rule = rule,
                isArabic = isArabic,
                onPlaySample = {
                    // Audio sample playback action
                }
            )
        }
    }
}

@Composable
fun TajweedRuleCard(
    rule: TajweedRule,
    isArabic: Boolean,
    onPlaySample: () -> Unit
) {
    val ruleColor = try {
        Color(android.graphics.Color.parseColor(rule.colorHex))
    } catch (e: Exception) {
        MaterialTheme.colorScheme.primary
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
            .testTag("tajweed_rule_${rule.id}"),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .clip(CircleShape)
                            .background(ruleColor)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = if (isArabic) rule.nameArabic else rule.nameEnglish,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = ruleColor
                    )
                }

                IconButton(onClick = onPlaySample) {
                    Icon(
                        imageVector = Icons.Default.VolumeUp,
                        contentDescription = "Play Audio Sample",
                        tint = ruleColor
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = if (isArabic) rule.descriptionArabic else rule.descriptionEnglish,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Practice Example Box
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(ruleColor.copy(alpha = 0.1f))
                    .padding(12.dp)
            ) {
                Column {
                    Text(
                        text = if (isArabic) "مثال توضيحي (${rule.exampleSurah}):" else "Example (${rule.exampleSurah}):",
                        style = MaterialTheme.typography.labelSmall,
                        color = ruleColor,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = rule.exampleArabic,
                        fontFamily = FontFamily.Serif,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    }
}

@Composable
fun LegendBadge(name: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .size(16.dp)
                .clip(CircleShape)
                .background(color)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = name,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold
        )
    }
}
