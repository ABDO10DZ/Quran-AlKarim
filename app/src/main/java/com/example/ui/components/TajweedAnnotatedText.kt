package com.example.ui.components

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp

val TajweedSilentColor = Color(0xFF9E9E9E)        // Grey (Hamzat Wasl / Lam Shamsiyyah)
val ColorMaddObligatory = Color(0xFFD32F2F)      // Crimson Red (Madd Obligatory 4-6)
val ColorMaddPermissible = Color(0xFFE65100)     // Dark Orange (Madd Permissible 2-4)
val ColorMaddNormal = Color(0xFFF57C00)          // Orange (Madd Normal 2)
val ColorQalqalahVibrant = Color(0xFF0288D1)     // Blue/Cyan (Qalqalah)
val ColorGhunnahGreen = Color(0xFF2E7D32)        // Emerald Green (Ghunnah)
val ColorIkhfaTeal = Color(0xFF00897B)           // Teal (Ikhfa)
val ColorIdghamAmber = Color(0xFFF57F17)         // Amber/Gold (Idgham)
val ColorIqlabPurple = Color(0xFF8E24AA)         // Purple (Iqlab)

val ColorGhunnah = ColorGhunnahGreen
val ColorIqlab = ColorIqlabPurple
val ColorIdgham = ColorIdghamAmber
val ColorIkhfa = ColorIkhfaTeal
val ColorQalqalah = ColorQalqalahVibrant
val ColorMadd = ColorMaddNormal
val ColorVerseMarker = Color(0xFFD4AF37)   // Golden Verse Circle Marker
val ColorAllahBrightRed = Color(0xFFFF0000) // Bright Red for Allah & Al-Ilah
val ColorMuhammadEmeraldGreen = Color(0xFF2E7D32) // Noble Emerald Green for Prophet Muhammad ﷺ

fun Int.toArabicDigits(): String {
    val digits = arrayOf('٠', '١', '٢', '٣', '٤', '٥', '٦', '٧', '٨', '٩')
    return this.toString().map { char ->
        if (char in '0'..'9') digits[char - '0'] else char
    }.joinToString("")
}

/**
 * Normalizes all verse markers in raw text to ensure that at every Ayah end,
 * ONLY the symbol ۝ is used with the Ayah number attached inside, and no outer brackets
 * (like ﴿...﴾ or {...}) or loose numbers exist anywhere outside.
 */
fun normalizeVerseMarkers(rawText: String): String {
    val regex = Regex("""(?:۝\s*﴿?|\{?﴿?)\s*([٠-٩0-9]+)\s*(?:﴾|\}?)""")
    return regex.replace(rawText) { match ->
        val numStr = match.groupValues[1]
        val arabicNum = numStr.map { ch ->
            if (ch in '0'..'9') {
                arrayOf('٠', '١', '٢', '٣', '٤', '٥', '٦', '٧', '٨', '٩')[ch - '0']
            } else ch
        }.joinToString("")
        " ۝$arabicNum "
    }
}

@Composable
fun TajweedAnnotatedText(
    annotatedText: String,
    fontSizeSp: Float,
    isTajweedEnabled: Boolean,
    modifier: Modifier = Modifier,
    textColor: Color = MaterialTheme.colorScheme.onBackground
) {
    val markerColor = MaterialTheme.colorScheme.primary
    val normalizedText = normalizeVerseMarkers(annotatedText)

    val baseAnnotated = if (isTajweedEnabled) {
        parseTajweedTags(normalizedText, textColor)
    } else {
        AnnotatedString(normalizedText.replace(Regex("<.*?>"), ""))
    }

    val finalAnnotatedString = parseVerseMarkersAndSacredNames(baseAnnotated, markerColor)

    Text(
        text = finalAnnotatedString,
        fontSize = fontSizeSp.sp,
        lineHeight = (fontSizeSp * 1.85f).sp,
        color = textColor,
        fontFamily = FontFamily.Serif,
        fontWeight = FontWeight.Normal,
        textAlign = TextAlign.Right,
        modifier = modifier.testTag("tajweed_ayah_text")
    )
}

/**
 * Applies styling for:
 * 1. Verse markers ۝[٠-٩]+ in golden markerColor.
 * 2. Words "الله" or "الإله" (and not "إله") in Bright Red.
 * 3. Words "محمد" in Deep Red.
 */
fun parseVerseMarkersAndSacredNames(
    baseAnnotated: AnnotatedString,
    markerColor: Color
): AnnotatedString {
    val text = baseAnnotated.text
    val verseMarkerRegex = Regex("۝[٠-٩0-9]+")
    val wordRegex = Regex("""[\p{L}\u064B-\u0653\u0670\u0651\u0640]+""")

    return buildAnnotatedString {
        append(baseAnnotated)

        // 1. Highlight Verse End Markers (۝ + Ayah Number)
        for (match in verseMarkerRegex.findAll(text)) {
            addStyle(
                style = SpanStyle(color = markerColor, fontWeight = FontWeight.Bold),
                start = match.range.first,
                end = match.range.last + 1
            )
        }

        // 2. Highlight Allah / Al-Ilah & Muhammad
        for (match in wordRegex.findAll(text)) {
            val rawWord = match.value
            val start = match.range.first
            val end = match.range.last + 1

            // Strip diacritics and normalize alefs for matching
            val stripped = rawWord
                .replace(Regex("""[\u064B-\u0653\u0670\u0651\u0640]"""), "")
                .replace('ٱ', 'ا')
                .replace('إ', 'ا')
                .replace('أ', 'ا')
                .replace('آ', 'ا')
                .replace('ٰ', 'ا')
            val clean = stripped.replace(Regex("""[^\p{L}]"""), "")

            if (isAllahOrAlIlah(clean)) {
                addStyle(
                    style = SpanStyle(color = ColorAllahBrightRed, fontWeight = FontWeight.Bold),
                    start = start,
                    end = end
                )
            } else if (isMuhammad(clean)) {
                addStyle(
                    style = SpanStyle(color = ColorMuhammadEmeraldGreen, fontWeight = FontWeight.Bold),
                    start = start,
                    end = end
                )
            }
        }
    }
}

private fun isAllahOrAlIlah(clean: String): Boolean {
    // Explicit exclusions for "إله" (ilah) or forms without "ال" / "ل"
    if (clean == "اله" || clean == "الهكم" || clean == "الهنا" || clean == "الهك" || clean == "الهين" || clean == "الها" || clean == "الهي") {
        return false
    }
    return clean == "الله" || clean == "اللهم" || clean == "الاله" ||
           clean == "لله" || clean == "بالله" || clean == "فالله" || clean == "تالله" || clean == "ولله" ||
           clean == "فلله" || clean == "والله"
}

private fun isMuhammad(clean: String): Boolean {
    return clean == "محمد" || clean == "بمحمد" || clean == "لمحمد" || clean == "فمحمد"
}

fun parseTajweedTags(rawText: String, defaultColor: Color): AnnotatedString {
    return parseTajweedHtmlNotation(rawText, defaultColor)
}

private fun parseTajweedHtmlNotation(rawText: String, defaultColor: Color): AnnotatedString {
    return buildAnnotatedString {
        var text = rawText
        val regex = Regex("<(g|ik|id|iq|q|m)>(.*?)</\\1>")
        var lastIndex = 0

        val matches = regex.findAll(text)
        for (match in matches) {
            val start = match.range.first
            val end = match.range.last + 1
            
            val rawPrefix = text.substring(lastIndex, start)
            append(cleanTags(rawPrefix))

            val tag = match.groupValues[1]
            val content = match.groupValues[2]
            val color = when (tag) {
                "g" -> ColorGhunnahGreen
                "iq" -> ColorIqlabPurple
                "id" -> ColorIdghamAmber
                "ik" -> ColorIkhfaTeal
                "q" -> ColorQalqalahVibrant
                "m" -> ColorMaddNormal
                else -> defaultColor
            }

            pushStyle(SpanStyle(color = color, fontWeight = FontWeight.Bold))
            append(content)
            pop()

            lastIndex = end
        }

        if (lastIndex < text.length) {
            append(cleanTags(text.substring(lastIndex)))
        }
    }
}

private fun cleanTags(text: String): String {
    return text.replace(Regex("<.*?>"), "")
}
