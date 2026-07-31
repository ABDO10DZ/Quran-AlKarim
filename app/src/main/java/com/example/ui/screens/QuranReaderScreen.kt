package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Ayah
import com.example.data.model.RevelationType
import com.example.data.model.Surah
import com.example.ui.components.TajweedAnnotatedText
import com.example.ui.components.toArabicDigits
import com.example.ui.viewmodel.QuranViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuranReaderScreen(
    viewModel: QuranViewModel,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val surah by viewModel.selectedSurah.collectAsState()
    val ayahs by viewModel.currentAyahs.collectAsState()
    val isLoadingAyahs by viewModel.isLoadingAyahs.collectAsState()
    val ayahsErrorMessage by viewModel.ayahsErrorMessage.collectAsState()
    val appSettings by viewModel.appSettings.collectAsState()
    val bookmarks by viewModel.bookmarksState.collectAsState()
    val activePlayingAyah by viewModel.activePlayingAyahNumber.collectAsState()
    val isPlayingAudio by viewModel.isPlayingAudio.collectAsState()

    val showTafsirSheet by viewModel.showTafsirSheet.collectAsState()
    val currentAyahTafsir by viewModel.currentAyahTafsir.collectAsState()
    val selectedTafsirSourceId by viewModel.selectedTafsirSourceId.collectAsState()

    var showFontSliderSheet by remember { mutableStateOf(false) }

    val settings = appSettings ?: com.example.data.model.AppSettings()
    val isArabic = settings.isArabicDefault
    val fontSizeSp = settings.fontSizeSp
    val translationFontSizeSp = settings.translationFontSizeSp
    val isTajweed = settings.showTajweedColor
    val showEnglish = settings.showEnglishTranslation
    val isBookMode = settings.isBookMode

    val pageSize = 10
    var isPagedMode by remember { mutableStateOf(true) }
    var currentPage by remember(surah.id) { mutableIntStateOf(1) }

    val totalPages = remember(ayahs.size, pageSize) {
        if (ayahs.isEmpty()) 1 else (ayahs.size + pageSize - 1) / pageSize
    }

    LaunchedEffect(activePlayingAyah) {
        activePlayingAyah?.let { ayahNum ->
            val targetPage = (ayahNum - 1) / pageSize + 1
            if (targetPage in 1..totalPages) {
                currentPage = targetPage
            }
        }
    }

    val pageAyahs = remember(ayahs, currentPage, isPagedMode) {
        if (!isPagedMode) {
            ayahs
        } else {
            val startIndex = (currentPage - 1) * pageSize
            val endIndex = minOf(startIndex + pageSize, ayahs.size)
            if (startIndex in ayahs.indices) ayahs.subList(startIndex, endIndex) else ayahs
        }
    }

    val startAyahNum = if (pageAyahs.isNotEmpty()) pageAyahs.first().ayahNumber else 1
    val endAyahNum = if (pageAyahs.isNotEmpty()) pageAyahs.last().ayahNumber else 1

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "${surah.nameArabic} - ${surah.nameEnglish}",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "${if (isArabic) "الجزء" else "Juz"} ${surah.startJuz} • ${surah.totalAyahs} ${if (isArabic) "آية" else "Ayahs"} • ${if (isBookMode) (if (isArabic) "وضع المصحف" else "Book Mode") else (if (isArabic) "وضع القائمة" else "List Mode")}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    // Audio Recitation Toggle Button
                    IconButton(
                        onClick = {
                            if (ayahs.isNotEmpty()) {
                                viewModel.playAyahAudio(ayahs.first())
                            }
                        }
                    ) {
                        Icon(
                            imageVector = if (isPlayingAudio) Icons.Default.PauseCircle else Icons.Default.PlayCircle,
                            contentDescription = "Recite Surah",
                            tint = if (isPlayingAudio) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                        )
                    }

                    // Reader Switch Mode (Book Mode vs List Mode)
                    IconButton(
                        onClick = {
                            viewModel.updateSettings { it.copy(isBookMode = !it.isBookMode) }
                        }
                    ) {
                        Icon(
                            imageVector = if (isBookMode) Icons.Default.MenuBook else Icons.Default.ViewList,
                            contentDescription = "Switch View Mode",
                            tint = if (isBookMode) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                        )
                    }

                    // Tajweed Color Toggle
                    IconButton(
                        onClick = {
                            viewModel.updateSettings { it.copy(showTajweedColor = !it.showTajweedColor) }
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Palette,
                            contentDescription = "Toggle Tajweed",
                            tint = if (isTajweed) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                        )
                    }

                    // Tafsir Action Button
                    IconButton(
                        onClick = { viewModel.openTafsirForCurrentSurah() }
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoStories,
                            contentDescription = "Tafsir",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }

                    // Font Size Slider Sheet Toggle
                    IconButton(onClick = { showFontSliderSheet = true }) {
                        Icon(Icons.Default.FormatSize, contentDescription = "Font Size")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        modifier = modifier.testTag("quran_reader_screen")
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Top Page Navigation Control Bar
            QuranPageNavigationBar(
                currentPage = currentPage,
                totalPages = totalPages,
                startAyahNum = startAyahNum,
                endAyahNum = endAyahNum,
                totalAyahs = surah.totalAyahs,
                isPagedMode = isPagedMode,
                isArabic = isArabic,
                onPreviousPage = { if (currentPage > 1) currentPage-- },
                onNextPage = { if (currentPage < totalPages) currentPage++ },
                onTogglePagedMode = { isPagedMode = !isPagedMode },
                onPageSelected = { currentPage = it },
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
            )

            Box(modifier = Modifier.weight(1f)) {
                if (isLoadingAyahs) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = if (isArabic) "جاري تحميل الآيات المباركة..." else "Loading Holy Verses...",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                } else if (ayahsErrorMessage != null) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Warning,
                                contentDescription = null,
                                modifier = Modifier.size(48.dp),
                                tint = MaterialTheme.colorScheme.error
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = ayahsErrorMessage ?: "",
                                style = MaterialTheme.typography.bodyMedium,
                                textAlign = TextAlign.Center,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Button(
                                onClick = { viewModel.retryLoadCurrentSurah() }
                            ) {
                                Text(if (isArabic) "إعادة المحاولة" else "Retry")
                            }
                        }
                    }
                } else if (isBookMode) {
                    // Book Mode Layout (Continuous Physical Mushaf Look)
                    BookModeView(
                        surah = surah,
                        ayahs = pageAyahs,
                        fontSizeSp = fontSizeSp,
                        translationFontSizeSp = translationFontSizeSp,
                        isTajweedEnabled = isTajweed,
                        showEnglishTranslation = showEnglish,
                        isArabic = isArabic,
                        bookmarks = bookmarks,
                        activePlayingAyah = activePlayingAyah,
                        onBookmarkToggle = { ayah -> viewModel.toggleBookmark(ayah) },
                        onPlayAudio = { ayah -> viewModel.playAyahAudio(ayah) },
                        onCopyText = { ayah ->
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            val clip = ClipData.newPlainText("Ayah Text", "${ayah.textArabic}\n${ayah.textEnglish}")
                            clipboard.setPrimaryClip(clip)
                            Toast.makeText(context, if (isArabic) "تم نسخ الآية 📋" else "Ayah copied 📋", Toast.LENGTH_SHORT).show()
                        },
                        onShareText = { ayah ->
                            val shareIntent = Intent().apply {
                                action = Intent.ACTION_SEND
                                putExtra(Intent.EXTRA_TEXT, "📖 ${surah.nameArabic} (${surah.nameEnglish}) - آية ${ayah.ayahNumber}:\n\n${ayah.textArabic}\n\n${ayah.textEnglish}")
                                type = "text/plain"
                            }
                            context.startActivity(Intent.createChooser(shareIntent, "Share Ayah"))
                        },
                        onOpenTafsir = { ayah -> viewModel.openTafsirForAyah(ayah) },
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    // Standard Ayah-by-Ayah List Mode Layout
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp),
                        contentPadding = PaddingValues(bottom = 90.dp)
                    ) {
                        // Bismillah Header (Except At-Tawbah Surah 9, shown on Page 1 or full view)
                        if (surah.id != 9 && (!isPagedMode || currentPage == 1)) {
                            item {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 12.dp)
                                        .clip(RoundedCornerShape(16.dp))
                                        .background(MaterialTheme.colorScheme.surfaceVariant)
                                        .border(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f), RoundedCornerShape(16.dp))
                                        .padding(16.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "بِسْمِ ٱللَّهِ ٱلرَّحْمَٰنِ ٱلرَّحِيمِ",
                                        fontSize = (fontSizeSp + 4).sp,
                                        fontFamily = FontFamily.Serif,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary,
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }
                        }

                        // Ayah Verses List
                        items(pageAyahs) { ayah ->
                            val isBookmarked = remember(bookmarks, ayah) {
                                bookmarks.any { it.surahId == ayah.surahId && it.ayahNumber == ayah.ayahNumber && !it.isLastRead }
                            }
                            val isCurrentlyPlaying = activePlayingAyah == ayah.ayahNumber

                            AyahCardItem(
                                ayah = ayah,
                                fontSizeSp = fontSizeSp,
                                translationFontSizeSp = translationFontSizeSp,
                                isTajweedEnabled = isTajweed,
                                showEnglishTranslation = showEnglish,
                                isBookmarked = isBookmarked,
                                isPlaying = isCurrentlyPlaying,
                                onBookmarkToggle = { viewModel.toggleBookmark(ayah) },
                                onPlayAudio = { viewModel.playAyahAudio(ayah) },
                                onCopyText = {
                                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                    val clip = ClipData.newPlainText("Ayah Text", "${ayah.textArabic}\n${ayah.textEnglish}")
                                    clipboard.setPrimaryClip(clip)
                                    Toast.makeText(context, if (isArabic) "تم نسخ الآية 📋" else "Ayah copied 📋", Toast.LENGTH_SHORT).show()
                                },
                                onShareText = {
                                    val shareIntent = Intent().apply {
                                        action = Intent.ACTION_SEND
                                        putExtra(Intent.EXTRA_TEXT, "📖 ${surah.nameArabic} (${surah.nameEnglish}) - آية ${ayah.ayahNumber}:\n\n${ayah.textArabic}\n\n${ayah.textEnglish}")
                                        type = "text/plain"
                                    }
                                    context.startActivity(Intent.createChooser(shareIntent, "Share Ayah"))
                                },
                                onOpenTafsir = { viewModel.openTafsirForAyah(ayah) }
                            )
                        }

                        // Bottom Page Control Bar
                        if (isPagedMode && totalPages > 1) {
                            item {
                                Spacer(modifier = Modifier.height(12.dp))
                                QuranPageNavigationBar(
                                    currentPage = currentPage,
                                    totalPages = totalPages,
                                    startAyahNum = startAyahNum,
                                    endAyahNum = endAyahNum,
                                    totalAyahs = surah.totalAyahs,
                                    isPagedMode = isPagedMode,
                                    isArabic = isArabic,
                                    onPreviousPage = { if (currentPage > 1) currentPage-- },
                                    onNextPage = { if (currentPage < totalPages) currentPage++ },
                                    onTogglePagedMode = { isPagedMode = !isPagedMode },
                                    onPageSelected = { currentPage = it },
                                    modifier = Modifier.padding(vertical = 8.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        if (showTafsirSheet) {
            com.example.ui.components.TafsirBottomSheet(
                tafsir = currentAyahTafsir,
                currentAyahs = ayahs,
                selectedSourceId = selectedTafsirSourceId,
                isArabic = isArabic,
                onSourceSelected = { viewModel.setTafsirSource(it) },
                onAyahSelected = { viewModel.openTafsirForAyah(it) },
                onDismiss = { viewModel.closeTafsirSheet() }
            )
        }

        // Font Size & Settings Modal Sheet
        if (showFontSliderSheet) {
            ModalBottomSheet(
                onDismissRequest = { showFontSliderSheet = false },
                sheetState = rememberModalBottomSheetState()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp)
                ) {
                    Text(
                        text = if (isArabic) "إعدادات حجم الخط ونمط العرض" else "Reading & Font Options",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    // Reading View Mode Toggle (Book vs List)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = if (isBookMode) Icons.Default.MenuBook else Icons.Default.ViewList,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = if (isArabic) "نمط كتاب المصحف" else "Book Mushaf View",
                                    fontWeight = FontWeight.Bold,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                                Text(
                                    text = if (isArabic) "عرض الآيات متصلة كصفحة المصحف" else "Continuous verses like a physical Quran",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        Switch(
                            checked = isBookMode,
                            onCheckedChange = { checked ->
                                viewModel.updateSettings { it.copy(isBookMode = checked) }
                            }
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // 1. Arabic Font Size Slider
                    Text(
                        text = "${if (isArabic) "حجم خط النص العربي" else "Arabic Text Size"}: ${fontSizeSp.toInt()} sp",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Slider(
                        value = fontSizeSp,
                        onValueChange = { newSize ->
                            viewModel.updateSettings { it.copy(fontSizeSp = newSize) }
                        },
                        valueRange = 18f..44f,
                        steps = 13,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("arabic_font_size_slider")
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // 2. Translation Font Size Slider
                    Text(
                        text = "${if (isArabic) "حجم خط الترجمة الإنجليزية" else "Translation Font Size"}: ${translationFontSizeSp.toInt()} sp",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Slider(
                        value = translationFontSizeSp,
                        onValueChange = { newSize ->
                            viewModel.updateSettings { it.copy(translationFontSizeSp = newSize) }
                        },
                        valueRange = 12f..28f,
                        steps = 16,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("translation_font_size_slider")
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Live Interactive Preview Card
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .padding(16.dp)
                    ) {
                        Column {
                            TajweedAnnotatedText(
                                annotatedText = "ٱلْحَمْدُ لِلَّهِ رَبِّ ٱلْعَالَمِينَ ۝١",
                                fontSizeSp = fontSizeSp,
                                isTajweedEnabled = isTajweed,
                                modifier = Modifier.fillMaxWidth()
                            )
                            if (showEnglish) {
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "[All] praise is [due] to Allah, Lord of the worlds.",
                                    fontSize = translationFontSizeSp.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    lineHeight = (translationFontSizeSp * 1.4f).sp
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // English Translation Switch
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(if (isArabic) "إظهار الترجمة الإنجليزية" else "Show English Translation")
                        Switch(
                            checked = showEnglish,
                            onCheckedChange = { checked ->
                                viewModel.updateSettings { it.copy(showEnglishTranslation = checked) }
                            }
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = { showFontSliderSheet = false },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(if (isArabic) "تم" else "Done")
                    }
                }
            }
        }
    }
}

@Composable
fun BookModeView(
    surah: Surah,
    ayahs: List<Ayah>,
    fontSizeSp: Float,
    translationFontSizeSp: Float,
    isTajweedEnabled: Boolean,
    showEnglishTranslation: Boolean,
    isArabic: Boolean,
    bookmarks: List<com.example.data.model.Bookmark>,
    activePlayingAyah: Int?,
    onBookmarkToggle: (Ayah) -> Unit,
    onPlayAudio: (Ayah) -> Unit,
    onCopyText: (Ayah) -> Unit,
    onShareText: (Ayah) -> Unit,
    onOpenTafsir: (Ayah) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedAyahForActions by remember { mutableStateOf<Ayah?>(null) }

    val scrollState = rememberScrollState()

    // Build joined continuous Mushaf text using ۝ without outer brackets
    val fullMushafText = remember(ayahs, isTajweedEnabled) {
        ayahs.joinToString(separator = " ") { ayah ->
            val cleanOrTajweed = if (isTajweedEnabled && ayah.tajweedAnnotated.isNotEmpty()) {
                ayah.tajweedAnnotated
            } else {
                ayah.textArabic
            }
            "$cleanOrTajweed ۝${ayah.ayahNumber.toArabicDigits()}"
        }
    }

    Column(
        modifier = modifier
            .verticalScroll(scrollState)
            .padding(16.dp)
            .padding(bottom = 90.dp)
    ) {
        // Physical Mushaf Decorative Page Frame
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(MaterialTheme.colorScheme.surface)
                .border(
                    2.dp,
                    MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
                    RoundedCornerShape(16.dp)
                )
                .padding(4.dp)
                .border(
                    1.dp,
                    MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                    RoundedCornerShape(12.dp)
                )
                .padding(16.dp)
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                // Ornate Surah Title Banner
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.primaryContainer)
                        .padding(vertical = 12.dp, horizontal = 16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "سُورَةُ ${surah.nameArabic}",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            fontFamily = FontFamily.Serif
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "آيَاتُهَا ${surah.totalAyahs.toArabicDigits()} • ${if (surah.revelationType == RevelationType.MECCAN) "مَكِّيَّةٌ" else "مَدَنِيَّةٌ"} • الجُزْءُ ${surah.startJuz.toArabicDigits()}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Bismillah Header (Except At-Tawbah Surah 9)
                if (surah.id != 9) {
                    Text(
                        text = "بِسْمِ ٱللَّهِ ٱلرَّحْمَٰنِ ٱلرَّحِيمِ",
                        fontSize = (fontSizeSp + 4).sp,
                        fontFamily = FontFamily.Serif,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                }

                // Continuous Flowing Text Block for all Ayahs in Book Mode
                TajweedAnnotatedText(
                    annotatedText = fullMushafText,
                    fontSizeSp = fontSizeSp,
                    isTajweedEnabled = isTajweedEnabled,
                    textColor = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Quick Verse Selection Chips for Audio / Bookmark / Actions in Book Mode
        Text(
            text = if (isArabic) "اختر آية للإنصات أو التحديد:" else "Select Ayah for options:",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(ayahs) { ayah ->
                val isPlaying = activePlayingAyah == ayah.ayahNumber
                val isBookmarked = bookmarks.any { it.surahId == ayah.surahId && it.ayahNumber == ayah.ayahNumber && !it.isLastRead }

                FilterChip(
                    selected = isPlaying || selectedAyahForActions?.ayahNumber == ayah.ayahNumber,
                    onClick = {
                        selectedAyahForActions = ayah
                    },
                    label = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("آية ${ayah.ayahNumber.toArabicDigits()}")
                            if (isBookmarked) {
                                Spacer(modifier = Modifier.width(4.dp))
                                Icon(Icons.Default.Bookmark, contentDescription = null, modifier = Modifier.size(14.dp), tint = MaterialTheme.colorScheme.primary)
                            }
                        }
                    },
                    leadingIcon = if (isPlaying) {
                        { Icon(Icons.Default.VolumeUp, contentDescription = null, modifier = Modifier.size(16.dp)) }
                    } else null
                )
            }
        }

        // Action Sheet / Dialog for Selected Ayah in Book Mode
        selectedAyahForActions?.let { ayah ->
            Spacer(modifier = Modifier.height(12.dp))
            val isBookmarked = bookmarks.any { it.surahId == ayah.surahId && it.ayahNumber == ayah.ayahNumber && !it.isLastRead }
            val isPlaying = activePlayingAyah == ayah.ayahNumber

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "آية ${ayah.ayahNumber.toArabicDigits()} (Ayah ${ayah.ayahNumber})",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        IconButton(onClick = { selectedAyahForActions = null }) {
                            Icon(Icons.Default.Close, contentDescription = "Close")
                        }
                    }

                    TajweedAnnotatedText(
                        annotatedText = ayah.textArabic,
                        fontSizeSp = (fontSizeSp - 2),
                        isTajweedEnabled = isTajweedEnabled,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        FilledTonalButton(onClick = { onPlayAudio(ayah) }) {
                            Icon(if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow, contentDescription = null)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(if (isPlaying) "إيقاف" else "استماع")
                        }

                        Button(onClick = { onOpenTafsir(ayah) }) {
                            Icon(Icons.Default.AutoStories, contentDescription = null)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(if (isArabic) "التفسير" else "Tafsir")
                        }

                        OutlinedButton(onClick = { onBookmarkToggle(ayah) }) {
                            Icon(if (isBookmarked) Icons.Default.Bookmark else Icons.Default.BookmarkBorder, contentDescription = null)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(if (isBookmarked) "حفظ" else "علامة")
                        }

                        IconButton(onClick = { onCopyText(ayah) }) {
                            Icon(Icons.Default.ContentCopy, contentDescription = "Copy")
                        }

                        IconButton(onClick = { onShareText(ayah) }) {
                            Icon(Icons.Default.Share, contentDescription = "Share")
                        }
                    }
                }
            }
        }

        // English Translations Breakdown List if toggled on
        if (showEnglishTranslation) {
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = if (isArabic) "الترجمة الإنجليزية (English Translation):" else "English Translation:",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(12.dp))

            ayahs.forEach { ayah ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.7f))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.primaryContainer,
                            modifier = Modifier.size(28.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    text = "${ayah.ayahNumber}",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = ayah.textEnglish,
                            fontSize = translationFontSizeSp.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            lineHeight = (translationFontSizeSp * 1.45f).sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun AyahCardItem(
    ayah: Ayah,
    fontSizeSp: Float,
    translationFontSizeSp: Float,
    isTajweedEnabled: Boolean,
    showEnglishTranslation: Boolean,
    isBookmarked: Boolean,
    isPlaying: Boolean,
    onBookmarkToggle: () -> Unit,
    onPlayAudio: () -> Unit,
    onCopyText: () -> Unit,
    onShareText: () -> Unit,
    onOpenTafsir: () -> Unit
) {
    val animatedBgColor by animateColorAsState(
        targetValue = if (isPlaying) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f)
        else MaterialTheme.colorScheme.surface,
        label = "AyahHighlight"
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
            .testTag("ayah_item_${ayah.ayahNumber}"),
        colors = CardDefaults.cardColors(containerColor = animatedBgColor),
        shape = RoundedCornerShape(16.dp),
        border = if (isPlaying) androidx.compose.foundation.BorderStroke(2.dp, MaterialTheme.colorScheme.primary) else null
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Verse Header Actions Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Verse Number Badge (with Arabic numerals inside circle)
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f))
                        .border(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.4f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = ayah.ayahNumber.toArabicDigits(),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                // Action Icons Row
                Row {
                    IconButton(onClick = onPlayAudio) {
                        Icon(
                            imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.VolumeUp,
                            contentDescription = "Audio Play",
                            tint = if (isPlaying) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    IconButton(onClick = onOpenTafsir) {
                        Icon(
                            imageVector = Icons.Default.AutoStories,
                            contentDescription = "Tafsir",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                    IconButton(onClick = onBookmarkToggle) {
                        Icon(
                            imageVector = if (isBookmarked) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                            contentDescription = "Bookmark",
                            tint = if (isBookmarked) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    IconButton(onClick = onCopyText) {
                        Icon(
                            imageVector = Icons.Default.ContentCopy,
                            contentDescription = "Copy Ayah",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    IconButton(onClick = onShareText) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = "Share Ayah",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Formatted Arabic Ayah Text with Tajweed & Verse Marker Symbol ۝ (No outer brackets)
            val cleanOrTajweed = if (isTajweedEnabled && ayah.tajweedAnnotated.isNotEmpty()) {
                ayah.tajweedAnnotated
            } else {
                ayah.textArabic
            }
            val fullAnnotatedText = "$cleanOrTajweed ۝${ayah.ayahNumber.toArabicDigits()}"

            TajweedAnnotatedText(
                annotatedText = fullAnnotatedText,
                fontSizeSp = fontSizeSp,
                isTajweedEnabled = isTajweedEnabled,
                modifier = Modifier.fillMaxWidth()
            )

            // Optional English Translation Text with instant font size slider adjustment
            if (showEnglishTranslation && ayah.textEnglish.isNotEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))
                HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f))
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = ayah.textEnglish,
                    fontSize = translationFontSizeSp.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = (translationFontSizeSp * 1.45f).sp
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuranPageNavigationBar(
    currentPage: Int,
    totalPages: Int,
    startAyahNum: Int,
    endAyahNum: Int,
    totalAyahs: Int,
    isPagedMode: Boolean,
    isArabic: Boolean,
    onPreviousPage: () -> Unit,
    onNextPage: () -> Unit,
    onTogglePagedMode: () -> Unit,
    onPageSelected: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    var showPageMenu by remember { mutableStateOf(false) }

    Surface(
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.85f),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)),
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onTogglePagedMode,
                modifier = Modifier.size(36.dp)
            ) {
                Icon(
                    imageVector = if (isPagedMode) Icons.Default.MenuBook else Icons.Default.ViewList,
                    contentDescription = "Toggle Paged Mode",
                    tint = MaterialTheme.colorScheme.primary
                )
            }

            if (isPagedMode && totalPages > 1) {
                IconButton(
                    onClick = onPreviousPage,
                    enabled = currentPage > 1,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = if (isArabic) Icons.AutoMirrored.Filled.ArrowForward else Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Previous Page"
                    )
                }

                Box {
                    FilterChip(
                        selected = true,
                        onClick = { showPageMenu = true },
                        label = {
                            Text(
                                text = if (isArabic)
                                    "صفحة $currentPage من $totalPages (الآيات $startAyahNum - $endAyahNum)"
                                else
                                    "Page $currentPage of $totalPages (v $startAyahNum - $endAyahNum)",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    )

                    DropdownMenu(
                        expanded = showPageMenu,
                        onDismissRequest = { showPageMenu = false }
                    ) {
                        (1..totalPages).forEach { pageNum ->
                            val pStart = (pageNum - 1) * 10 + 1
                            val pEnd = minOf(pageNum * 10, totalAyahs)
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        if (isArabic) "صفحة $pageNum (آية $pStart - $pEnd)"
                                        else "Page $pageNum (v $pStart - $pEnd)",
                                        fontWeight = if (pageNum == currentPage) FontWeight.Bold else FontWeight.Normal
                                    )
                                },
                                onClick = {
                                    onPageSelected(pageNum)
                                    showPageMenu = false
                                }
                            )
                        }
                    }
                }

                IconButton(
                    onClick = onNextPage,
                    enabled = currentPage < totalPages,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = if (isArabic) Icons.AutoMirrored.Filled.ArrowBack else Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = "Next Page"
                    )
                }
            } else {
                Text(
                    text = if (isArabic) "جميع الآيات ($totalAyahs آية)" else "All Verses ($totalAyahs Ayahs)",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}
