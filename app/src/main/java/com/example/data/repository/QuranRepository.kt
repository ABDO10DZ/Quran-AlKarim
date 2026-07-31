package com.example.data.repository

import com.example.data.model.Ayah
import com.example.data.model.RevelationType
import com.example.data.model.Surah
import com.example.data.model.TajweedRule
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import java.util.concurrent.ConcurrentHashMap

object QuranRepository {

    private val fetchedSurahCache = ConcurrentHashMap<Int, List<Ayah>>()

    val surahsList: List<Surah> = listOf(
        Surah(1, "الفاتحة", "Al-Fatihah", "Al-Fatihah", 7, RevelationType.MECCAN, 1, 1),
        Surah(2, "البقرة", "Al-Baqarah", "Al-Baqarah", 286, RevelationType.MEDINAN, 2, 1),
        Surah(3, "آل عمران", "Ali 'Imran", "Ali 'Imran", 200, RevelationType.MEDINAN, 50, 3),
        Surah(4, "النساء", "An-Nisa", "An-Nisa", 176, RevelationType.MEDINAN, 77, 4),
        Surah(5, "المائدة", "Al-Ma'idah", "Al-Ma'idah", 120, RevelationType.MEDINAN, 106, 6),
        Surah(6, "الأنعام", "Al-An'am", "Al-An'am", 165, RevelationType.MECCAN, 128, 7),
        Surah(7, "الأعراف", "Al-A'raf", "Al-A'raf", 206, RevelationType.MECCAN, 151, 8),
        Surah(8, "الأنفال", "Al-Anfal", "Al-Anfal", 75, RevelationType.MEDINAN, 177, 9),
        Surah(9, "التوبة", "At-Tawbah", "At-Tawbah", 129, RevelationType.MEDINAN, 187, 10),
        Surah(10, "يونس", "Yunus", "Yunus", 109, RevelationType.MECCAN, 208, 11),
        Surah(11, "هود", "Hud", "Hud", 123, RevelationType.MECCAN, 221, 11),
        Surah(12, "يوسف", "Yusuf", "Yusuf", 111, RevelationType.MECCAN, 235, 12),
        Surah(13, "الرعد", "Ar-Ra'd", "Ar-Ra'd", 43, RevelationType.MEDINAN, 249, 13),
        Surah(14, "إبراهيم", "Ibrahim", "Ibrahim", 52, RevelationType.MECCAN, 255, 13),
        Surah(15, "الحجر", "Al-Hijr", "Al-Hijr", 99, RevelationType.MECCAN, 262, 14),
        Surah(16, "النحل", "An-Nahl", "An-Nahl", 128, RevelationType.MECCAN, 267, 14),
        Surah(17, "الإسراء", "Al-Isra", "Al-Isra", 111, RevelationType.MECCAN, 282, 15),
        Surah(18, "الكهف", "Al-Kahf", "Al-Kahf", 110, RevelationType.MECCAN, 293, 15),
        Surah(19, "مريم", "Maryam", "Maryam", 98, RevelationType.MECCAN, 305, 16),
        Surah(20, "طه", "Taha", "Taha", 135, RevelationType.MECCAN, 312, 16),
        Surah(21, "الأنبياء", "Al-Anbiya", "Al-Anbiya", 112, RevelationType.MECCAN, 322, 17),
        Surah(22, "الحج", "Al-Hajj", "Al-Hajj", 78, RevelationType.MEDINAN, 332, 17),
        Surah(23, "المؤمنون", "Al-Mu'minun", "Al-Mu'minun", 118, RevelationType.MECCAN, 342, 18),
        Surah(24, "النور", "An-Nur", "An-Nur", 64, RevelationType.MEDINAN, 350, 18),
        Surah(25, "الفرقان", "Al-Furqan", "Al-Furqan", 77, RevelationType.MECCAN, 359, 18),
        Surah(26, "الشعراء", "Ash-Shu'ara", "Ash-Shu'ara", 227, RevelationType.MECCAN, 367, 19),
        Surah(27, "النمل", "An-Naml", "An-Naml", 93, RevelationType.MECCAN, 377, 19),
        Surah(28, "القصص", "Al-Qasas", "Al-Qasas", 88, RevelationType.MECCAN, 385, 20),
        Surah(29, "العنكبوت", "Al-'Ankabut", "Al-'Ankabut", 69, RevelationType.MECCAN, 396, 20),
        Surah(30, "الروم", "Ar-Rum", "Ar-Rum", 60, RevelationType.MECCAN, 404, 21),
        Surah(31, "لقمان", "Luqman", "Luqman", 34, RevelationType.MECCAN, 411, 21),
        Surah(32, "السجدة", "As-Sajdah", "As-Sajdah", 30, RevelationType.MECCAN, 415, 21),
        Surah(33, "الأحزاب", "Al-Ahzab", "Al-Ahzab", 73, RevelationType.MEDINAN, 418, 21),
        Surah(34, "سبإ", "Saba", "Saba", 54, RevelationType.MECCAN, 428, 22),
        Surah(35, "فاطر", "Fatir", "Fatir", 45, RevelationType.MECCAN, 434, 22),
        Surah(36, "يس", "Ya-Sin", "Ya-Sin", 83, RevelationType.MECCAN, 440, 22),
        Surah(37, "الصافات", "As-Saffat", "As-Saffat", 182, RevelationType.MECCAN, 446, 23),
        Surah(38, "ص", "Sad", "Sad", 88, RevelationType.MECCAN, 453, 23),
        Surah(39, "الزمر", "Az-Zumar", "Az-Zumar", 75, RevelationType.MECCAN, 458, 23),
        Surah(40, "غافر", "Ghafir", "Ghafir", 85, RevelationType.MECCAN, 467, 24),
        Surah(41, "فصلت", "Fussilat", "Fussilat", 54, RevelationType.MECCAN, 477, 24),
        Surah(42, "الشورى", "Ash-Shura", "Ash-Shura", 53, RevelationType.MECCAN, 483, 25),
        Surah(43, "الزخرف", "Az-Zukhruf", "Az-Zukhruf", 89, RevelationType.MECCAN, 489, 25),
        Surah(44, "الدخان", "Ad-Dukhan", "Ad-Dukhan", 59, RevelationType.MECCAN, 496, 25),
        Surah(45, "الجاثية", "Al-Jathiyah", "Al-Jathiyah", 37, RevelationType.MECCAN, 499, 25),
        Surah(46, "الأحقاف", "Al-Ahqaf", "Al-Ahqaf", 35, RevelationType.MECCAN, 502, 26),
        Surah(47, "محمد", "Muhammad", "Muhammad", 38, RevelationType.MEDINAN, 507, 26),
        Surah(48, "الفتح", "Al-Fath", "Al-Fath", 29, RevelationType.MEDINAN, 511, 26),
        Surah(49, "الحجرات", "Al-Hujurat", "Al-Hujurat", 18, RevelationType.MEDINAN, 515, 26),
        Surah(50, "ق", "Qaf", "Qaf", 45, RevelationType.MECCAN, 518, 26),
        Surah(51, "الذاريات", "Adh-Dhariyat", "Adh-Dhariyat", 60, RevelationType.MECCAN, 520, 26),
        Surah(52, "الطور", "At-Tur", "At-Tur", 49, RevelationType.MECCAN, 523, 27),
        Surah(53, "النجم", "An-Najm", "An-Najm", 62, RevelationType.MECCAN, 526, 27),
        Surah(54, "القمر", "Al-Qamar", "Al-Qamar", 55, RevelationType.MECCAN, 528, 27),
        Surah(55, "الرحمن", "Ar-Rahman", "Ar-Rahman", 78, RevelationType.MEDINAN, 531, 27),
        Surah(56, "الواقعة", "Al-Waqi'ah", "Al-Waqi'ah", 96, RevelationType.MECCAN, 534, 27),
        Surah(57, "الحديد", "Al-Hadid", "Al-Hadid", 29, RevelationType.MEDINAN, 537, 27),
        Surah(58, "المجادلة", "Al-Mujadila", "Al-Mujadila", 22, RevelationType.MEDINAN, 542, 28),
        Surah(59, "الحشر", "Al-Hashr", "Al-Hashr", 24, RevelationType.MEDINAN, 545, 28),
        Surah(60, "الممتحنة", "Al-Mumtahanah", "Al-Mumtahanah", 13, RevelationType.MEDINAN, 549, 28),
        Surah(61, "الصف", "As-Saff", "As-Saff", 14, RevelationType.MEDINAN, 551, 28),
        Surah(62, "الجمعة", "Al-Jumu'ah", "Al-Jumu'ah", 11, RevelationType.MEDINAN, 553, 28),
        Surah(63, "المنافقون", "Al-Munafiqun", "Al-Munafiqun", 11, RevelationType.MEDINAN, 554, 28),
        Surah(64, "التغابن", "At-Taghabun", "At-Taghabun", 18, RevelationType.MEDINAN, 556, 28),
        Surah(65, "الطلاق", "At-Talaq", "At-Talaq", 12, RevelationType.MEDINAN, 558, 28),
        Surah(66, "التحريم", "At-Tahrim", "At-Tahrim", 12, RevelationType.MEDINAN, 560, 28),
        Surah(67, "الملك", "Al-Mulk", "Al-Mulk", 30, RevelationType.MECCAN, 562, 29),
        Surah(68, "القلم", "Al-Qalam", "Al-Qalam", 52, RevelationType.MECCAN, 564, 29),
        Surah(69, "الحاقة", "Al-Haqqah", "Al-Haqqah", 52, RevelationType.MECCAN, 566, 29),
        Surah(70, "المعارج", "Al-Ma'arij", "Al-Ma'arij", 44, RevelationType.MECCAN, 568, 29),
        Surah(71, "نوح", "Nuh", "Nuh", 28, RevelationType.MECCAN, 570, 29),
        Surah(72, "الجن", "Al-Jinn", "Al-Jinn", 28, RevelationType.MECCAN, 572, 29),
        Surah(73, "المزمل", "Al-Muzzammil", "Al-Muzzammil", 20, RevelationType.MECCAN, 574, 29),
        Surah(74, "المدثر", "Al-Muddaththir", "Al-Muddaththir", 56, RevelationType.MECCAN, 575, 29),
        Surah(75, "القيامة", "Al-Qiyamah", "Al-Qiyamah", 40, RevelationType.MECCAN, 577, 29),
        Surah(76, "الإنسان", "Al-Insan", "Al-Insan", 31, RevelationType.MEDINAN, 578, 29),
        Surah(77, "المرسلات", "Al-Mursalat", "Al-Mursalat", 50, RevelationType.MECCAN, 580, 29),
        Surah(78, "النبإ", "An-Naba", "An-Naba", 40, RevelationType.MECCAN, 582, 30),
        Surah(79, "النازعات", "An-Nazi'at", "An-Nazi'at", 46, RevelationType.MECCAN, 583, 30),
        Surah(80, "عبس", "Abasa", "Abasa", 42, RevelationType.MECCAN, 585, 30),
        Surah(81, "التكوير", "At-Takwir", "At-Takwir", 29, RevelationType.MECCAN, 586, 30),
        Surah(82, "الانفطار", "Al-Infitar", "Al-Infitar", 19, RevelationType.MECCAN, 587, 30),
        Surah(83, "المطففين", "Al-Mutaffifin", "Al-Mutaffifin", 36, RevelationType.MECCAN, 587, 30),
        Surah(84, "الانشقاق", "Al-Inshiqaq", "Al-Inshiqaq", 25, RevelationType.MECCAN, 589, 30),
        Surah(85, "البروج", "Al-Buruj", "Al-Buruj", 22, RevelationType.MECCAN, 590, 30),
        Surah(86, "الطارق", "At-Tariq", "At-Tariq", 17, RevelationType.MECCAN, 591, 30),
        Surah(87, "الأعلى", "Al-A'la", "Al-A'la", 19, RevelationType.MECCAN, 591, 30),
        Surah(88, "الغاشية", "Al-Ghashiyah", "Al-Ghashiyah", 26, RevelationType.MECCAN, 592, 30),
        Surah(89, "الفجر", "Al-Fajr", "Al-Fajr", 30, RevelationType.MECCAN, 593, 30),
        Surah(90, "البلد", "Al-Balad", "Al-Balad", 20, RevelationType.MECCAN, 594, 30),
        Surah(91, "الشمس", "Ash-Shams", "Ash-Shams", 15, RevelationType.MECCAN, 595, 30),
        Surah(92, "الليل", "Al-Layl", "Al-Layl", 21, RevelationType.MECCAN, 595, 30),
        Surah(93, "الضحى", "Ad-Duha", "Ad-Duha", 11, RevelationType.MECCAN, 596, 30),
        Surah(94, "الشرح", "Ash-Sharh", "Ash-Sharh", 8, RevelationType.MECCAN, 596, 30),
        Surah(95, "التين", "At-Tin", "At-Tin", 8, RevelationType.MECCAN, 597, 30),
        Surah(96, "العلق", "Al-'Alaq", "Al-'Alaq", 19, RevelationType.MECCAN, 597, 30),
        Surah(97, "القدر", "Al-Qadr", "Al-Qadr", 5, RevelationType.MECCAN, 598, 30),
        Surah(98, "البينة", "Al-Bayyinah", "Al-Bayyinah", 8, RevelationType.MEDINAN, 598, 30),
        Surah(99, "الزلزلة", "Az-Zalzalah", "Az-Zalzalah", 8, RevelationType.MEDINAN, 599, 30),
        Surah(100, "العاديات", "Al-'Adiyat", "Al-'Adiyat", 11, RevelationType.MECCAN, 599, 30),
        Surah(101, "القارعة", "Al-Qari'ah", "Al-Qari'ah", 11, RevelationType.MECCAN, 600, 30),
        Surah(102, "التكاثر", "At-Takathur", "At-Takathur", 8, RevelationType.MECCAN, 600, 30),
        Surah(103, "العصر", "Al-'Asr", "Al-'Asr", 3, RevelationType.MECCAN, 601, 30),
        Surah(104, "الهمزة", "Al-Humazah", "Al-Humazah", 9, RevelationType.MECCAN, 601, 30),
        Surah(105, "الفيل", "Al-Fil", "Al-Fil", 5, RevelationType.MECCAN, 601, 30),
        Surah(106, "قريش", "Quraysh", "Quraysh", 4, RevelationType.MECCAN, 602, 30),
        Surah(107, "الماعون", "Al-Ma'un", "Al-Ma'un", 7, RevelationType.MECCAN, 602, 30),
        Surah(108, "الكوثر", "Al-Kawthar", "Al-Kawthar", 3, RevelationType.MECCAN, 602, 30),
        Surah(109, "الكافرون", "Al-Kafirun", "Al-Kafirun", 6, RevelationType.MECCAN, 603, 30),
        Surah(110, "النصر", "An-Nasr", "An-Nasr", 3, RevelationType.MEDINAN, 603, 30),
        Surah(111, "المسد", "Al-Masad", "Al-Masad", 5, RevelationType.MECCAN, 603, 30),
        Surah(112, "الإخلاص", "Al-Ikhlas", "Al-Ikhlas", 4, RevelationType.MECCAN, 604, 30),
        Surah(113, "الفلق", "Al-Falaq", "Al-Falaq", 5, RevelationType.MECCAN, 604, 30),
        Surah(114, "الناس", "An-Nas", "An-Nas", 6, RevelationType.MECCAN, 604, 30)
    )

    private val detailedAyahsMap: Map<Int, List<Ayah>> = mapOf(
        // Surah 1: Al-Fatihah
        1 to listOf(
            Ayah(1, 1, 1, "بِسْمِ ٱللَّهِ ٱلرَّحْمَٰنِ ٱلرَّحِيمِ", "In the name of Allah, the Entirely Merciful, the Especially Merciful.", 1, 1, tajweedAnnotated = "بِسْمِ <g>ٱللَّهِ</g> ٱلرَّحْمَٰنِ ٱلرَّحِيمِ"),
            Ayah(2, 1, 2, "ٱلْحَمْدُ لِلَّهِ رَبِّ ٱلْعَالَمِينَ", "[All] praise is [due] to Allah, Lord of the worlds -", 1, 1),
            Ayah(3, 1, 3, "ٱلرَّحْمَٰنِ ٱلرَّحِيمِ", "The Entirely Merciful, the Especially Merciful,", 1, 1),
            Ayah(4, 1, 4, "مَالِكِ يَوْمِ ٱلدِّينِ", "Sovereign of the Day of Recompense.", 1, 1),
            Ayah(5, 1, 5, "إِيَّاكَ نَعْبُدُ وَإِيَّاكَ نَسْتَعِينُ", "It is You we worship and You we ask for help.", 1, 1),
            Ayah(6, 1, 6, "ٱهْدِنَا ٱلصِّرَٰطَ ٱلْمُسْتَقِيمَ", "Guide us to the straight path -", 1, 1),
            Ayah(7, 1, 7, "صِرَٰطَ ٱلَّذِينَ أَنْعَمْتَ عَلَيْهِمْ غَيْرِ ٱلْمَغْضُوبِ عَلَيْهِمْ وَلَا ٱلضَّآلِّينَ", "The path of those upon whom You have bestowed favor, not of those who have evoked [Your] anger or of those who are astray.", 1, 1, tajweedAnnotated = "صِرَٰطَ ٱلَّذِينَ أَنْعَمْتَ عَلَيْهِمْ غَيْرِ ٱلْمَغْضُوبِ عَلَيْهِمْ وَلَا <m>ٱلضَّآلِّينَ</m>")
        ),
        // Surah 2: Al-Baqarah (Selection: 1-5 & Ayat Al-Kursi 255)
        2 to listOf(
            Ayah(8, 2, 1, "الم", "Alif, Lam, Meem.", 1, 2, tajweedAnnotated = "<m>الم</m>"),
            Ayah(9, 2, 2, "ذَٰلِكَ ٱلْكِتَٰبُ لَا رَيْبَ ۛ فِيهِ ۛ هُدًى لِّلْمُتَّقِينَ", "This is the Book about which there is no doubt, a guidance for those conscious of Allah -", 1, 2, tajweedAnnotated = "ذَٰلِكَ ٱلْكِتَٰبُ لَا رَيْبَ ۛ فِيهِ ۛ هُدًى <id>لِّلْمُتَّقِينَ</id>"),
            Ayah(10, 2, 3, "ٱلَّذِينَ يُؤْمِنُونَ بِٱلْغَيْبِ وَيُقِيمُونَ ٱلصَّلَوٰةَ وَمِمَّا رَزَقْنَٰهُمْ يُنفِقُونَ", "Who believe in the unseen, establish prayer, and spend out of what We have provided for them,", 1, 2, tajweedAnnotated = "ٱلَّذِينَ يُؤْمِنُونَ بِٱلْغَيْبِ وَيُقِيمُونَ ٱلصَّلَوٰةَ وَمِمَّا رَزَقْنَٰهُمْ <ik>يُنفِقُونَ</ik>"),
            Ayah(11, 2, 4, "وَٱلَّذِينَ يُؤْمِنُونَ بِمَآ أُنزِلَ إِلَيْكَ وَمَآ أُنزِلَ مِن قَبْلِكَ وَبِٱلْءَاخِرَةِ هُمْ يُوقِنُونَ", "And who believe in what has been revealed to you, [O Muhammad], and what was revealed before you, and of the Hereafter they are certain [in faith].", 1, 2, tajweedAnnotated = "وَٱلَّذِينَ يُؤْمِنُونَ <m>بِمَآ</m> <ik>أُنزِلَ</ik> إِلَيْكَ وَمَآ <ik>أُنزِلَ</ik> مِن <ik>قَبْلِكَ</ik> وَبِٱلْءَاخِرَةِ هُمْ يُوقِنُونَ"),
            Ayah(12, 2, 5, "أُو۟لَٰٓئِكَ عَلَىٰ هُدًى مِّن رَّبِّهِمْ ۖ وَأُو۟لَٰٓئِكَ هُمُ ٱلْمُفْلِحُونَ", "Those are upon [right] guidance from their Lord, and it is those who are the successful.", 1, 2, tajweedAnnotated = "<m>أُو۟لَٰٓئِكَ</m> عَلَىٰ هُدًى <id>مِّن</id> رَّبِّهِمْ ۖ وَأُو۟لَٰٓئِكَ هُمُ ٱلْمُفْلِحُونَ"),
            Ayah(13, 2, 255, "ٱللَّهُ لَآ إِلَٰهَ إِلَّا هُوَ ٱلْحَىُّ ٱلْقَيُّومُ ۚ لَا تَأْخُذُهُۥ سِنَةٌ وَلَا نَوْمٌ ۚ لَّهُۥ مَا فِى ٱلسَّمَٰوَٰتِ وَمَا فِى ٱلْأَرْضِ ۗ مَن ذَا ٱلَّذِى يَشْفَعُ عِندَهُۥٓ إِلَّا بِإِذْنِهِۦ ۚ يَعْلَمُ مَا بَيْنَ أَيْدِيهِمْ وَمَا خَلْفَهُمْ ۖ وَلَا يُحِيطُونَ بِشَىْءٍ مِّنْ عِلْمِهِۦٓ إِلَّا بِمَا شَآءَ ۚ وَسِعَ كُرْسِيُّهُ ٱلسَّمَٰوَٰتِ وَٱلْأَرْضَ ۖ وَلَا يَـَُٔودُهُۥ حِفْظُهُمَا ۚ وَهُوَ ٱلْعَلِىُّ ٱلْعَظِيمُ", "Allah! There is no deity except Him, the Ever-Living, the Sustainer of [all] existence. Neither drowsiness overtakes Him nor sleep. To Him belongs whatever is in the heavens and whatever is on the earth. Who is it that can intercede with Him except by His permission? He knows what is [presently] before them and what will be after them, and they encompass not a thing of His knowledge except for what He wills. His Kursi extends over the heavens and the earth, and their preservation tires Him not. And He is the Most High, the Most Great.", 3, 42, tajweedAnnotated = "<g>ٱللَّهُ</g> لَآ إِلَٰهَ إِلَّا هُوَ ٱلْحَىُّ ٱلْقَيُّومُ ۚ لَا تَأْخُذُهُۥ سِنَةٌ وَلَا <id>نَوْمٌ</id> ۚ لَّهُۥ مَا فِى ٱلسَّمَٰوَٰتِ وَمَا فِى ٱلْأَرْضِ ۗ مَن <ik>ذَا</ik> ٱلَّذِى يَشْفَعُ <ik>عِندَهُۥٓ</ik> إِلَّا بِإِذْنِهِۦ ۚ يَعْلَمُ مَا بَيْنَ أَيْدِيهِمْ وَمَا خَلْفَهُمْ ۖ وَلَا يُحِيطُونَ بِشَىْءٍ <id>مِّنْ</id> عِلْمِهِۦٓ إِلَّا بِمَا <m>شَآءَ</m> ۚ وَسِعَ كُرْسِيُّهُ ٱلسَّمَٰوَٰتِ وَٱلْأَرْضَ ۖ وَلَا يَـَُٔودُهُۥ حِفْظُهُمَا ۚ وَهُوَ ٱلْعَلِىُّ ٱلْعَظِيمُ")
        ),
        // Surah 18: Al-Kahf (Selection 1-4)
        18 to listOf(
            Ayah(14, 18, 1, "ٱلْحَمْدُ لِلَّهِ ٱلَّذِى أَنزَلَ عَلَىٰ عَبْدِهِ ٱلْكِتَٰبَ وَلَمْ يَجْعَل لَّهُۥ عِوَجَا ۜ", "[All] praise is [due] to Allah, who has sent down upon His Servant the Book and has not made therein any deviance.", 15, 293, tajweedAnnotated = "ٱلْحَمْدُ لِلَّهِ ٱلَّذِى <ik>أَنزَلَ</ik> عَلَىٰ عَبْدِهِ ٱلْكِتَٰبَ وَلَمْ يَجْعَل <id>لَّهُۥ</id> عِوَجَا ۜ"),
            Ayah(15, 18, 2, "قَيِّمًا لِّيُنذِرَ بَأْسًا شَدِيدًا مِّن لَّدُنْهُ وَيُبَشِّرَ ٱلْمُؤْمِنِينَ ٱلَّذِينَ يَعْمَلُونَ ٱلصَّٰلِحَٰتِ أَنَّ لَهُمْ أَجْرًا حَسَنًا", "[He has made it] straight, to warn of severe punishment from Him and to give good tidings to the believers who do righteous deeds that they will have a good reward", 15, 293, tajweedAnnotated = "قَيِّمًا <id>لِّيُنذِرَ</id> بَأْسًا <ik>شَدِيدًا</ik> <id>مِّن</id> لَّدُنْهُ وَيُبَشِّرَ ٱلْمُؤْمِنِينَ ٱلَّذِينَ يَعْمَلُونَ ٱلصَّٰلِحَٰتِ <g>أَنَّ</g> لَهُمْ أَجْرًا حَسَنًا"),
            Ayah(16, 18, 3, "مَّٰكِثِينَ فِيهِ أَبَدًا", "In which they will remain forever", 15, 293),
            Ayah(17, 18, 4, "وَيُنذِرَ ٱلَّذِينَ قَالُوا۟ ٱتَّخَذَ ٱللَّهُ وَلَدًا", "And to warn those who say, \"Allah has taken a son.\"", 15, 293)
        ),
        // Surah 36: Ya-Sin (Selection 1-5)
        36 to listOf(
            Ayah(18, 36, 1, "يس", "Ya, Seen.", 22, 440, tajweedAnnotated = "<m>يس</m>"),
            Ayah(19, 36, 2, "وَٱلْقُرْءَانِ ٱلْحَكِيمِ", "By the wise Qur'an.", 22, 440),
            Ayah(20, 36, 3, "إِنَّكَ لَمِنَ ٱلْمُرْسَلِينَ", "Indeed you, [O Muhammad], are from among the messengers,", 22, 440, tajweedAnnotated = "<g>إِنَّكَ</g> لَمِنَ ٱلْمُرْسَلِينَ"),
            Ayah(21, 36, 4, "عَلَىٰ صِرَٰطٍ مُّسْتَقِيمٍ", "On a straight path.", 22, 440, tajweedAnnotated = "عَلَىٰ صِرَٰطٍ <id>مُّسْتَقِيمٍ</id>"),
            Ayah(22, 36, 5, "تَنزِيلَ ٱلْعَزِيزِ ٱلرَّحِيمِ", "[This is] a revelation of the Exalted in Might, the Especially Merciful,", 22, 440, tajweedAnnotated = "<ik>تَنزِيلَ</ik> ٱلْعَزِيزِ ٱلرَّحِيمِ")
        ),
        // Surah 55: Ar-Rahman (Selection 1-5)
        55 to listOf(
            Ayah(23, 55, 1, "ٱلرَّحْمَٰنُ", "The Ar-Rahman (Entirely Merciful)", 27, 531),
            Ayah(24, 55, 2, "عَلَّمَ ٱلْقُرْءَانَ", "Taught the Qur'an,", 27, 531),
            Ayah(25, 55, 3, "خَلَقَ ٱلْإِنسَٰنَ", "Created man,", 27, 531, tajweedAnnotated = "خَلَقَ <ik>ٱلْإِنسَٰنَ</ik>"),
            Ayah(26, 55, 4, "عَلَّمَهُ ٱلْبَيَانَ", "[And] taught him eloquence.", 27, 531),
            Ayah(27, 55, 5, "ٱلشَّمْسُ وَٱلْقَمَرُ بِحُسْبَانٍ", "The sun and the moon [move] by precise calculation,", 27, 531)
        ),
        // Surah 67: Al-Mulk (Selection 1-4)
        67 to listOf(
            Ayah(28, 67, 1, "تَبَٰرَكَ ٱلَّذِى بِيَدِهِ ٱلْمُلْكُ وَهُوَ عَلَىٰ كُلِّ شَىْءٍ قَدِيرٌ", "Blessed is He in whose hand is dominion, and He is over all things competent -", 29, 562, tajweedAnnotated = "تَبَٰرَكَ ٱلَّذِى بِيَدِهِ ٱلْمُلْكُ وَهُوَ عَلَىٰ كُلِّ شَىْءٍ <q>قَدِيرٌ</q>"),
            Ayah(29, 67, 2, "ٱلَّذِى خَلَقَ ٱلْمَوْتَ وَٱلْحَيَوٰةَ لِيَبْلُوَكُمْ أَيُّكُمْ أَحْسَنُ عَمَلًا ۚ وَهُوَ ٱلْعَزِيزُ ٱلْغَفُورُ", "[He] who created death and life to test you [as to] which of you is best in deed - and He is the Exalted in Might, the Forgiving -", 29, 562, tajweedAnnotated = "ٱلَّذِى خَلَقَ ٱلْمَوْتَ وَٱلْحَيَوٰةَ <ik>لِيَبْلُوَكُمْ</ik> أَيُّكُمْ أَحْسَنُ عَمَلًا ۚ وَهُوَ ٱلْعَزِيزُ ٱلْغَفُورُ"),
            Ayah(30, 67, 3, "ٱلَّذِى خَلَقَ سَبْعَ سَمَٰوَٰتٍ طِبَاقًا ۖ مَّا تَرَىٰ فِى خَلْقِ ٱلرَّحْمَٰنِ مِن تَفَٰوُتٍ ۖ فَٱرْجِعِ ٱلْبَصَرَ هَلْ تَرَىٰ مِن فُطُورٍ", "[And] who created seven heavens in layers. You do not see in the creation of the Most Merciful any inconsistency. So return [your] vision; do you see any breaks?", 29, 562, tajweedAnnotated = "ٱلَّذِى خَلَقَ سَبْعَ سَمَٰوَٰتٍ <ik>طِبَاقًا</ ۖ مَّا تَرَىٰ فِى خَلْقِ ٱلرَّحْمَٰنِ مِن <ik>تَفَٰوُتٍ</ik> ۖ فَٱرْجِعِ ٱلْبَصَرَ هَلْ تَرَىٰ مِن <ik>فُطُورٍ</ik>"),
            Ayah(31, 67, 4, "ثُمَّ ٱرْجِعِ ٱلْبَصَرَ كَرَّتَيْنِ يَنقَلِبْ إِلَيْكَ ٱلْبَصَرُ خَاسِئًا وَهُوَ حَسِيرٌ", "Then return [your] vision twice again. [Your] vision will return to you humbled while it is fatigued.", 29, 562)
        ),
        // Surah 112: Al-Ikhlas
        112 to listOf(
            Ayah(32, 112, 1, "قُلْ هُوَ ٱللَّهُ أَحَدٌ", "Say, \"He is Allah, [who is] One,", 30, 604, tajweedAnnotated = "قُلْ هُوَ <g>ٱللَّهُ</g> <q>أَحَدٌ</q>"),
            Ayah(33, 112, 2, "ٱللَّهُ ٱلصَّمَدُ", "Allah, the Eternal Refuge.", 30, 604, tajweedAnnotated = "<g>ٱللَّهُ</g> ٱلصَّمَدُ"),
            Ayah(34, 112, 3, "لَمْ يَلِدْ وَلَمْ يُولَدْ", "He neither begets nor is born,", 30, 604, tajweedAnnotated = "لَمْ <q>يَلِدْ</q> وَلَمْ <q>يُولَدْ</q>"),
            Ayah(35, 112, 4, "وَلَمْ يَكُن لَّهُۥ كُفُوًا أَحَدٌ", "Nor is there to Him any equivalent.\"", 30, 604, tajweedAnnotated = "وَلَمْ يَكُن <id>لَّهُۥ</id> كُفُوًا <q>أَحَدٌ</q>")
        ),
        // Surah 113: Al-Falaq
        113 to listOf(
            Ayah(36, 113, 1, "قُلْ أَعُوذُ بِرَبِّ ٱلْفَلَقِ", "Say, \"I seek refuge in the Lord of daybreak", 30, 604, tajweedAnnotated = "قُلْ أَعُوذُ بِرَبِّ <q>ٱلْفَلَقِ</q>"),
            Ayah(37, 113, 2, "مِن شَرِّ مَا خَلَقَ", "From the evil of that which He created", 30, 604, tajweedAnnotated = "مِن <ik>شَرِّ</ik> مَا خَلَقَ"),
            Ayah(38, 113, 3, "وَمِن شَرِّ غَاسِقٍ إِذَا وَقَبَ", "And from the evil of darkness when it settles", 30, 604, tajweedAnnotated = "وَمِن <ik>شَرِّ</ik> غَاسِقٍ إِذَا <q>وَقَبَ</q>"),
            Ayah(39, 113, 4, "وَمِن شَرِّ ٱلنَّفَّٰثَٰتِ فِى ٱلْعُقَدِ", "And from the evil of the blowers in knots", 30, 604, tajweedAnnotated = "وَمِن <ik>شَرِّ</ik> <g>ٱلنَّفَّٰثَٰتِ</g> فِى <q>ٱلْعُقَدِ</q>"),
            Ayah(40, 113, 5, "وَمِن شَرِّ حَاسِدٍ إِذَا حَسَدَ", "And from the evil of an envier when he envies.\"", 30, 604, tajweedAnnotated = "وَمِن <ik>شَرِّ</ik> حَاسِدٍ إِذَا <q>حَسَدَ</q>")
        ),
        // Surah 114: An-Nas
        114 to listOf(
            Ayah(41, 114, 1, "قُلْ أَعُوذُ بِرَبِّ ٱلنَّاسِ", "Say, \"I seek refuge in the Lord of mankind,", 30, 604, tajweedAnnotated = "قُلْ أَعُوذُ بِرَبِّ <g>ٱلنَّاسِ</g>"),
            Ayah(42, 114, 2, "مَلِكِ ٱلنَّاسِ", "The Sovereign of mankind,", 30, 604, tajweedAnnotated = "مَلِكِ <g>ٱلنَّاسِ</g>"),
            Ayah(43, 114, 3, "إِلَٰهِ ٱلنَّاسِ", "The God of mankind,", 30, 604, tajweedAnnotated = "إِلَٰهِ <g>ٱلنَّاسِ</g>"),
            Ayah(44, 114, 4, "مِن شَرِّ ٱلْوَسْوَاسِ ٱلْخَنَّاسِ", "From the evil of the meeker who whispers [evil]", 30, 604, tajweedAnnotated = "مِن <ik>شَرِّ</ik> ٱلْوَسْوَاسِ <g>ٱلْخَنَّاسِ</g>"),
            Ayah(45, 114, 5, "ٱلَّذِى يُوَسْوِسُ فِى صُدُورِ ٱلنَّاسِ", "Who whispers in the breasts of mankind -", 30, 604, tajweedAnnotated = "ٱلَّذِى يُوَسْوِسُ فِى صُدُورِ <g>ٱلنَّاسِ</g>"),
            Ayah(46, 114, 6, "مِنَ ٱلْجِنَّةِ وَٱلنَّاسِ", "From among the jinn and mankind.\"", 30, 604, tajweedAnnotated = "مِنَ ٱلْجِنَّةِ <g>وَٱلنَّاسِ</g>")
        ),
        // Surah 48: Al-Fath (Ayah 29 - Muhammad Rasul Allah)
        48 to listOf(
            Ayah(4801, 48, 1, "إِنَّا فَتَحْنَا لَكَ فَتْحًا مُّبِينًا", "Indeed, We have given you, [O Muhammad], a clear conquest", 26, 511, tajweedAnnotated = "<g>إِنَّا</g> فَتَحْنَا لَكَ فَتْحًا <id>مُّبِينًا</id>"),
            Ayah(4829, 48, 29, "مُحَمَّدٌ رَّسُولُ ٱللَّهِ ۚ وَٱلَّذِينَ مَعَهُۥٓ أَشِدَّآءُ عَلَى ٱلْكُفَّارِ رُحَمَآءُ بَيْنَهُمْ ۖ تَرَىٰهُمْ رُكَّعًا سُجَّدًا يَبْتَغُونَ فَضْلًا مِّنَ ٱللَّهِ وَرِضْوَٰنًا ۖ سِيمَاهُمْ فِى وُجُوهِهِم مِّنْ أَثَرِ ٱلسُّجُودِ ۚ ذَٰلِكَ مَثَلُهُمْ فِى ٱلتَّوْرَىٰةِ ۚ وَمَثَلُهُمْ فِى ٱلْإِنجِيلِ كَزَرْعٍ أَخْرَجَ شَطْـَٔهُۥ فَـَٔازَرَهُۥ فَٱسْتَغْلَظَ فَٱسْتَوَىٰ عَلَىٰ سُوقِهِۦ يُعْجِبُ ٱلزُّرَّاعَ لِيَغِيظَ بِهِمُ ٱلْكُفَّارَ ۗ وَعَدَ ٱللَّهُ ٱلَّذِينَ ءَامَنُوا۟ وَعَمِلُوا۟ ٱلصَّٰلِحَٰتِ مِنْهُم مَّغْفِرَةً وَأَجْرًا عَظِيمًا", "Muhammad is the Messenger of Allah; and those with him are forceful against the disbelievers, merciful among themselves. You see them bowing and prostrating [in prayer], seeking bounty from Allah and [His] pleasure. Their mark is on their faces from the trace of prostration. That is their description in the Torah. And their description in the Gospel is as a plant which produces its offshoots and strengthens them so they grow firm and stand upon their stalks, delighting the sowers - so that Allah may enrage by them the disbelievers. Allah has promised those who believe and do righteous deeds among them forgiveness and a great reward.", 26, 515, tajweedAnnotated = "<g>مُحَمَّدٌ</g> <id>رَّسُولُ</id> <g>ٱللَّهِ</g> ۚ وَٱلَّذِينَ مَعَهُۥٓ أَشِدَّآءُ عَلَى ٱلْكُفَّارِ رُحَمَآءُ بَيْنَهُمْ ۖ تَرَىٰهُمْ رُكَّعًا سُجَّدًا يَبْتَغُونَ فَضْلًا <id>مِّنَ</id> <g>ٱللَّهِ</g> وَرِضْوَٰنًا ۖ سِيمَاهُمْ فِى وُجُوهِهِم <ik>مِّنْ</ik> أَثَرِ ٱلسُّجُودِ ۚ ذَٰلِكَ مَثَلُهُمْ فِى ٱلتَّوْرَىٰةِ ۚ وَمَثَلُهُمْ فِى ٱلْإِنجِيلِ كَزَرْعٍ أَخْرَجَ شَطْـَٔهُۥ فَـَٔازَرَهُۥ فَٱسْتَغْلَظَ فَٱسْتَوَىٰ عَلَىٰ سُوقِهِۦ يُعْجِبُ ٱلزُّرَّاعَ لِيَغِيظَ بِهِمُ ٱلْكُفَّارَ ۗ وَعَدَ <g>ٱللَّهِ</g> ٱلَّذِينَ ءَامَنُوا۟ وَعَمِلُوا۟ ٱلصَّٰلِحَٰتِ مِنْهُم <id>مَّغْفِرَةً</id> وَأَجْرًا عَظِيمًا")
        ),
        // Surah 47: Muhammad
        47 to listOf(
            Ayah(4701, 47, 1, "ٱلَّذِينَ كَفَرُوا۟ وَصَدُّوا۟ عَن سَبِيلِ ٱللَّهِ أَضَلَّ أَعْمَٰلَهُمْ", "Those who disbelieve and avert [people] from the way of Allah - He will waste their deeds.", 26, 507, tajweedAnnotated = "ٱلَّذِينَ كَفَرُوا۟ وَصَدُّوا۟ عَن سَبِيلِ <g>ٱللَّهِ</g> أَضَلَّ أَعْمَٰلَهُمْ"),
            Ayah(4702, 47, 2, "وَٱلَّذِينَ ءَامَنُوا۟ وَعَمِلُوا۟ ٱلصَّٰلِحَٰتِ وَءَامَنُوا۟ بِمَا نُزِّلَ عَلَىٰ مُحَمَّدٍ وَهُوَ ٱلْحَقُّ مِن رَّبِّهِمْ ۙ كَفَّرَ عَنْهُمْ سَيِّـَٔاتِهِمْ وَأَصْلَحَ بَالَهُمْ", "And those who believe and do righteous deeds and believe in what has been sent down upon Muhammad - and it is the truth from their Lord - He will remove from them their misdeeds and amend their condition.", 26, 507, tajweedAnnotated = "وَٱلَّذِينَ ءَامَنُوا۟ وَعَمِلُوا۟ ٱلصَّٰلِحَٰتِ وَءَامَنُوا۟ بِمَا نُزِّلَ عَلَىٰ <g>مُحَمَّدٍ</g> وَهُوَ ٱلْحَقُّ <id>مِن</id> رَّبِّهِمْ ۙ كَفَّرَ عَنْهُمْ سَيِّـَٔاتِهِمْ وَأَصْلَحَ بَالَهُمْ")
        ),
        // Surah 33: Al-Ahzab (Ayah 40 & 56)
        33 to listOf(
            Ayah(3340, 33, 40, "مَّا كَانَ مُحَمَّدٌ أَبَآ أَحَدٍ مِّن رِّجَالِكُمْ وَلَٰكِن رَّسُولَ ٱللَّهِ وَخَاتَمَ ٱلنَّبِيِّۦنَ ۗ وَكَانَ ٱللَّهُ بِكُلِّ شَىْءٍ عَلِيمًا", "Muhammad is not the father of [any] one of your men, but [he is] the Messenger of Allah and seal of the prophets. And ever is Allah, of all things, Knowing.", 22, 423, tajweedAnnotated = "مَّا كَانَ <g>مُحَمَّدٌ</g> أَبَآ <ik>أَحَدٍ</ik> <id>مِّن</id> رِّجَالِكُمْ وَلَٰكِن <id>رَّسُولَ</id> <g>ٱللَّهِ</g> وَخَاتَمَ ٱلنَّبِيِّۦنَ ۗ وَكَانَ <g>ٱللَّهُ</g> بِكُلِّ شَىْءٍ عَلِيمًا"),
            Ayah(3356, 33, 56, "إِنَّ ٱللَّهَ وَمَلَٰٓئِكَتَهُۥ يُصَلُّونَ عَلَى ٱلنَّبِيِّ ۚ يَٰٓأَيُّهَا ٱلَّذِينَ ءَامَنُوا۟ صَلُّوا۟ عَلَيْهِ وَسَلِّمُوا۟ تَسليمًا", "Indeed, Allah confers blessing upon the Prophet, and His angels [ask Him to do so]. O you who have believed, ask [Allah to confer] blessing upon him and ask [Allah to grant him] peace.", 22, 426, tajweedAnnotated = "<g>إِنَّ</g> <g>ٱللَّهَ</g> وَمَلَٰٓئِكَتَهُۥ يُصَلُّونَ عَلَى ٱلنَّبِيِّ ۚ يَٰٓأَيُّهَا ٱلَّذِينَ ءَامَنُوا۟ صَلُّوا۟ عَلَيْهِ وَسَلِّمُوا۟ تَسْلِيمًا")
        ),
        // Surah 3: Ali 'Imran (Ayah 144 & 190)
        3 to listOf(
            Ayah(3144, 3, 144, "وَمَا مُحَمَّدٌ إِلَّا رَسُولٌ قَدْ خَلَتْ مِن قَبْلِهِ ٱلرُّسُلُ ۚ أَفَإِيْن مَّاتَ أَوْ قُتِلَ ٱنقَلَبْتُمْ عَلَىٰٓ أَعْقَٰبِكُمْ", "Muhammad is not but a messenger; [other] messengers have passed on before him. So if he was to die or be killed, would you turn back on your heels?", 4, 68, tajweedAnnotated = "وَمَا <g>مُحَمَّدٌ</g> إِلَّا رَسُولٌ <ik>قَدْ</ik> خَلَتْ مِن <ik>قَبْلِهِ</ik> ٱلرُّسُلُ"),
            Ayah(3190, 3, 190, "إِنَّ فِى خَلْقِ ٱلسَّمَٰوَٰتِ وَٱلْأَرْضِ وَٱخْتِلَٰفِ ٱلَّيْلِ وَٱلنَّهَارِ لَءَايَٰتٍ لِّأُو۟لِى ٱلْأَلْبَٰبِ", "Indeed, in the creation of the heavens and the earth and the alternation of the night and the day are signs for those of understanding -", 4, 75, tajweedAnnotated = "<g>إِنَّ</g> فِى خَلْقِ ٱلسَّمَٰوَٰتِ وَٱلْأَرْضِ وَٱخْتِلَٰفِ ٱلَّيْلِ وَٱلنَّهَارِ لَءَايَٰتٍ <id>لِّأُو۟لِى</id> ٱلْأَلْبَٰبِ")
        ),
        // Surah 24: An-Nur (Ayah 35 - Ayat An-Nur)
        24 to listOf(
            Ayah(2435, 24, 35, "ٱللَّهُ نُورُ ٱلسَّمَٰوَٰتِ وَٱلْأَرْضِ ۚ مَثَلُ نُورِهِۦ كَمِشْكَوٰةٍ فِيهَا مِصْبَاحٌ ۖ ٱلْمِصْبَاحُ فِى زُجَاجَةٍ ۖ ٱلزُّجَاجَةُ كَأَنَّهَا كَوْكَبٌ دُرِّىٌّ يُوقَدُ مِن شَجَرَةٍ مُّبَٰرَكَةٍ زَيْتُونَةٍ لَّا شَرْقِيَّةٍ وَلَا غَرْبِيَّةٍ يَكَادُ زَيْتُهَا يُضِيٓءُ وَلَوْ لَمْ تَمْسَسْهُ نَارٌ ۚ نُّورٌ عَلَىٰ نُورٍ ۗ يَهْدِى ٱللَّهُ لِنُورِهِۦ مَن يَشَآءُ ۚ وَيَضْرِبُ ٱللَّهُ ٱلْأَمْثَٰلَ لِلنَّاسِ ۗ وَٱللَّهُ بِكُلِّ شَىْءٍ عَلِيمٌ", "Allah is the Light of the heavens and the earth. The example of His light is like a niche within which is a lamp, the lamp is within glass, the glass as if it were a pearly [white] star lit from [the oil of] a blessed olive tree, neither of the east nor of the west, whose oil would almost glow even if untouched by fire. Light upon light. Allah guides to His light whom He wills. And Allah presents examples for the people, and Allah is Knowing of all things.", 18, 354, tajweedAnnotated = "<g>ٱللَّهُ</g> نُورُ ٱلسَّمَٰوَٰتِ وَٱلْأَرْضِ ۚ مَثَلُ نُورِهِۦ كَمِشْكَوٰةٍ فِيهَا مِصْبَاحٌ ۖ ٱلْمِصْبَاحُ فِى زُجَاجَةٍ...")
        ),
        // Surah 59: Al-Hashr (Ayahs 21-24)
        59 to listOf(
            Ayah(5921, 59, 21, "لَوْ أَنزَلْنَا هَٰذَا ٱلْقُرْءَانَ عَلَىٰ جَبَلٍ لَّرَأَيْتَهُۥ خَٰشِعًا مُّتَصَدِّعًا مِّنْ خَشْيَةِ ٱللَّهِ ۚ وَتِلْكَ ٱلْأَمْثَٰلُ نَضْرِبُهَا لِلنَّاسِ لَعَلَّهُمْ يَتَفَكَّرُونَ", "If We had sent down this Qur'an upon a mountain, you would have seen it humbled and coming apart from fear of Allah. And these examples We present to the people that perhaps they will give thought.", 28, 548, tajweedAnnotated = "لَوْ <ik>أَنزَلْنَا</ik> هَٰذَا ٱلْقُرْءَانَ عَلَىٰ جَبَلٍ <id>لَّرَأَيْتَهُۥ</id> خَٰشِعًا <id>مُّتَصَدِّعًا</id> <ik>مِّنْ</ik> خَشْيَةِ <g>ٱللَّهِ</g>"),
            Ayah(5922, 59, 22, "هُوَ ٱللَّهُ ٱلَّذِى لَآ إِلَٰهَ إِلَّا هُوَ ۖ عَٰلِمُ ٱلْغَيْبِ وَٱلشَّهَٰدَةِ ۖ هُوَ ٱلرَّحْمَٰنُ ٱلرَّحِيمُ", "He is Allah, other than whom there is no deity, Knower of the unseen and the witnessed. He is the Entirely Merciful, the Especially Merciful.", 28, 548, tajweedAnnotated = "هُوَ <g>ٱللَّهُ</g> ٱلَّذِى لَآ إِلَٰهَ إِلَّا هُوَ"),
            Ayah(5923, 59, 23, "هُوَ ٱللَّهُ ٱلَّذِى لَآ إِلَٰهَ إِلَّا هُوَ ٱلْمَلِكُ ٱلْقُدُّوسُ ٱلسَّلَٰمُ ٱلْمُؤْمِنُ ٱلْمُهَيْمِنُ ٱلْعَزِيزُ ٱلْجَبَّارُ ٱلْمُتَكَبِّرُ ۚ سُبْحَٰنَ ٱللَّهِ عَمَّا يُشْرِكُونَ", "He is Allah, other than whom there is no deity, the Sovereign, the Pure, the Perfection, the Bestower of Faith, the Overseer, the Exalted in Might, the Compeller, the Superior. Exalted is Allah above whatever they associate with Him.", 28, 548, tajweedAnnotated = "هُوَ <g>ٱللَّهُ</g> ٱلَّذِى لَآ إِلَٰهَ إِلَّا هُوَ ٱلْمَلِكُ ٱلْقُدُّوسُ..."),
            Ayah(5924, 59, 24, "هُوَ ٱللَّهُ ٱلْخَٰلِقُ ٱلْبَارِئُ ٱلْمُصَوِّرُ ۖ لَهُ ٱلْأَسْمَآءُ ٱلْحُسْنَىٰ ۚ يُسَبِّحُ لَهُۥ مَا فِى ٱلسَّمَٰوَٰتِ وَٱلْأَرْضِ ۖ وَهُوَ ٱلْعَزِيزُ ٱلْحَكِيمُ", "He is Allah, the Creator, the Inventor, the Fashioner; to Him belong the best names. Whatever is in the heavens and earth is exalting Him. And He is the Exalted in Might, the Wise.", 28, 548, tajweedAnnotated = "هُوَ <g>ٱللَّهُ</g> ٱلْخَٰلِقُ ٱلْبَارِئُ ٱلْمُصَوِّرُ...")
        )
    )

    fun normalizeArabicText(text: String): String {
        if (text.isEmpty()) return ""
        var s = text
        // Remove HTML or Tajweed tags <...>
        s = s.replace(Regex("<.*?>"), "")
        // Remove diacritics / harakat / tashkeel / quranic marks
        s = s.replace(Regex("[\u064B-\u065F\u0670\u06D6-\u06ED]"), "")
        // Normalize Alifs: أ, إ, آ, ٱ, ٲ, ٳ, ٰ -> ا
        s = s.replace(Regex("[أإآٱٲٳٰ]"), "ا")
        // Normalize Ta Marbuta: ة -> ه
        s = s.replace('ة', 'ه')
        // Normalize Alif Maqsura: ى -> ي
        s = s.replace('ى', 'ي')
        // Normalize Hamza forms: ئ, ؤ -> ء
        s = s.replace(Regex("[ئؤ]"), "ء")
        // Remove Quranic punctuation & symbols: ۝, ﴿, ﴾, ۚ, ۗ, ۖ, ۛ, ۜ, ۦ, ۨ, ۪, ۬, ۫
        s = s.replace(Regex("[۝﴿﴾ۚۗۖۛۜۦۨ۬۫]"), "")
        // Connect prefixes if written with space e.g., "و الذين" -> "والذين", "ف الذين" -> "فالذين"
        s = s.replace(Regex("(?:^|\\s)([وفبلك])\\s+"), " $1")
        // Collapse multi-spaces
        return s.replace(Regex("\\s+"), " ").trim().lowercase()
    }

    suspend fun getAyahsForSurah(surahId: Int): List<Ayah> = withContext(Dispatchers.IO) {
        if (fetchedSurahCache.containsKey(surahId)) {
            return@withContext fetchedSurahCache[surahId]!!
        }

        val apiAyahs = fetchSurahFromApi(surahId)
        if (!apiAyahs.isNullOrEmpty()) {
            fetchedSurahCache[surahId] = apiAyahs
            return@withContext apiAyahs
        }

        val detailedList = detailedAyahsMap[surahId]
        if (!detailedList.isNullOrEmpty()) {
            return@withContext detailedList
        }

        return@withContext emptyList()
    }

    private suspend fun fetchSurahFromApi(surahId: Int): List<Ayah>? = withContext(Dispatchers.IO) {
        try {
            val url = URL("https://api.alquran.cloud/v1/surah/$surahId/editions/quran-uthmani,en.sahih")
            val conn = url.openConnection() as HttpURLConnection
            conn.requestMethod = "GET"
            conn.connectTimeout = 10000
            conn.readTimeout = 10000

            if (conn.responseCode == 200) {
                val stream = conn.inputStream
                val jsonString = stream.bufferedReader().use { it.readText() }
                conn.disconnect()

                val rootObj = JSONObject(jsonString)
                if (rootObj.optInt("code") == 200) {
                    val dataArray = rootObj.getJSONArray("data")
                    var arObj: JSONObject? = null
                    var enObj: JSONObject? = null

                    for (i in 0 until dataArray.length()) {
                        val obj = dataArray.getJSONObject(i)
                        val edition = obj.optJSONObject("edition")?.optString("identifier")
                        if (edition == "quran-uthmani") {
                            arObj = obj
                        } else if (edition == "en.sahih") {
                            enObj = obj
                        }
                    }

                    if (arObj != null) {
                        val arAyahs = arObj.getJSONArray("ayahs")
                        val enAyahs = enObj?.optJSONArray("ayahs")

                        val surahObj = surahsList.find { it.id == surahId } ?: surahsList.first()

                        val resultList = mutableListOf<Ayah>()
                        val bismillahPrefix = "بِسْمِ ٱللَّهِ ٱلرَّحْمَٰنِ ٱلرَّحِيمِ "

                        for (i in 0 until arAyahs.length()) {
                            val arAyah = arAyahs.getJSONObject(i)
                            val enAyah = if (enAyahs != null && i < enAyahs.length()) enAyahs.getJSONObject(i) else null

                            val ayahNum = arAyah.getInt("numberInSurah")
                            var rawArText = arAyah.getString("text")

                            // Strip leading Bismillah for non-Fatihah, non-Tawbah Surahs on Ayah 1
                            if (surahId != 1 && surahId != 9 && ayahNum == 1) {
                                if (rawArText.startsWith(bismillahPrefix)) {
                                    rawArText = rawArText.substring(bismillahPrefix.length)
                                }
                            }

                            val enText = enAyah?.optString("text") ?: ""
                            val juzNum = arAyah.optInt("juz", surahObj.startJuz)
                            val pageNum = arAyah.optInt("page", surahObj.startPage)

                            resultList.add(
                                Ayah(
                                    id = surahId * 10000 + ayahNum,
                                    surahId = surahId,
                                    ayahNumber = ayahNum,
                                    textArabic = rawArText,
                                    textEnglish = enText,
                                    juz = juzNum,
                                    page = pageNum,
                                    tajweedAnnotated = generateTajweedTags(rawArText)
                                )
                            )
                        }
                        if (resultList.isNotEmpty()) {
                            return@withContext resultList
                        }
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return@withContext null
    }

    private fun generateTajweedTags(rawText: String): String {
        var text = rawText
        text = text.replace("ٱللَّهَ", "<g>ٱللَّهَ</g>")
                   .replace("ٱللَّهُ", "<g>ٱللَّهُ</g>")
                   .replace("ٱللَّهِ", "<g>ٱللَّهِ</g>")
                   .replace("لِلَّهِ", "<g>لِلَّهِ</g>")
                   .replace("مُحَمَّدٌ", "<g>مُحَمَّدٌ</g>")
                   .replace("مُحَمَّدًا", "<g>مُحَمَّدًا</g>")
                   .replace("مُحَمَّدٍ", "<g>مُحَمَّدٍ</g>")
        return text
    }

    suspend fun searchVerses(query: String): List<Ayah> = withContext(Dispatchers.IO) {
        if (query.isBlank()) return@withContext emptyList()
        val rawQuery = query.trim()
        val normQuery = normalizeArabicText(rawQuery)
        val cleanQueryEng = rawQuery.lowercase()
        val queryWords = normQuery.split(" ").filter { it.isNotBlank() }

        val candidateAyahs = mutableListOf<Ayah>()

        // 1. Live Online API Search across entire Quran (All 114 Surahs)
        val apiMatches = searchQuranApi(rawQuery, normQuery)
        if (!apiMatches.isNullOrEmpty()) {
            candidateAyahs.addAll(apiMatches)
        }

        // Collect from cached Surahs
        fetchedSurahCache.values.flatten().forEach { item ->
            if (!candidateAyahs.any { c -> c.surahId == item.surahId && c.ayahNumber == item.ayahNumber }) {
                candidateAyahs.add(item)
            }
        }

        // Collect from detailed map
        detailedAyahsMap.values.flatten().forEach { item ->
            if (!candidateAyahs.any { c -> c.surahId == item.surahId && c.ayahNumber == item.ayahNumber }) {
                candidateAyahs.add(item)
            }
        }

        val scoredResults = mutableListOf<Pair<Ayah, Int>>()

        candidateAyahs.forEach { ayah ->
            val normAyah = normalizeArabicText(ayah.textArabic)
            val normEngAyah = ayah.textEnglish.lowercase()
            var score = 0

            // Exact phrase match
            if (normQuery.isNotEmpty() && normAyah.contains(normQuery)) {
                score += 10000 + (100 - normAyah.length.coerceAtMost(90))
            }

            // Sequential Token Match
            if (queryWords.size > 1 && queryWords.joinToString(" ").let { normAyah.contains(it) }) {
                score += 8000
            }

            // All query words present
            if (queryWords.isNotEmpty() && queryWords.all { normAyah.contains(it) }) {
                score += 6000
            }

            // Partial word matches
            if (queryWords.isNotEmpty()) {
                val matchingCount = queryWords.count { normAyah.contains(it) }
                if (matchingCount > 0) {
                    score += ((matchingCount.toDouble() / queryWords.size) * 4000).toInt()
                }
            }

            // English Translation Match
            if (cleanQueryEng.isNotEmpty() && normEngAyah.contains(cleanQueryEng)) {
                score += 3000
            }

            // Surah Name or ID Match
            val surah = surahsList.find { it.id == ayah.surahId }
            if (surah != null) {
                val normSurahArabic = normalizeArabicText(surah.nameArabic)
                if (normSurahArabic.contains(normQuery) || surah.nameEnglish.lowercase().contains(cleanQueryEng)) {
                    score += 2000
                }
                if ("${surah.id}:${ayah.ayahNumber}" == rawQuery || "${surah.id} ${ayah.ayahNumber}" == rawQuery) {
                    score += 5000
                }
            }

            if (score > 0) {
                scoredResults.add(Pair(ayah, score))
            }
        }

        val sortedAyahs = scoredResults
            .sortedByDescending { it.second }
            .map { it.first }

        return@withContext sortedAyahs
    }

    private fun searchQuranApi(rawQuery: String, normQuery: String): List<Ayah>? {
        try {
            val isArabicQuery = rawQuery.any { it in '\u0600'..'\u06FF' }
            val edition = if (isArabicQuery) "quran-simple-clean" else "en.sahih"
            val queryToUse = if (isArabicQuery) normQuery else rawQuery
            val encodedQuery = URLEncoder.encode(queryToUse, "UTF-8")

            val url = URL("https://api.alquran.cloud/v1/search/$encodedQuery/all/$edition")
            val conn = url.openConnection() as HttpURLConnection
            conn.requestMethod = "GET"
            conn.connectTimeout = 8000
            conn.readTimeout = 8000

            if (conn.responseCode == 200) {
                val jsonString = conn.inputStream.bufferedReader().use { it.readText() }
                conn.disconnect()

                val rootObj = JSONObject(jsonString)
                if (rootObj.optInt("code") == 200) {
                    val dataObj = rootObj.optJSONObject("data") ?: return null
                    val matchesArray = dataObj.optJSONArray("matches") ?: return null

                    val results = mutableListOf<Ayah>()
                    for (i in 0 until matchesArray.length()) {
                        val match = matchesArray.getJSONObject(i)
                        val surahObj = match.getJSONObject("surah")
                        val surahNum = surahObj.getInt("number")
                        val ayahNum = match.getInt("numberInSurah")
                        val matchText = match.getString("text")

                        val localSurah = surahsList.find { it.id == surahNum } ?: surahsList.first()

                        val arText = if (isArabicQuery) matchText else ""
                        val enText = if (!isArabicQuery) matchText else ""

                        results.add(
                            Ayah(
                                id = surahNum * 10000 + ayahNum,
                                surahId = surahNum,
                                ayahNumber = ayahNum,
                                textArabic = arText,
                                textEnglish = enText,
                                juz = localSurah.startJuz,
                                page = localSurah.startPage,
                                tajweedAnnotated = generateTajweedTags(arText)
                            )
                        )
                    }
                    return results
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return null
    }

    val tajweedRules: List<TajweedRule> = listOf(
        TajweedRule(
            id = "ghunnah",
            nameArabic = "الغنة المشددة",
            nameEnglish = "Ghunnah (Nass / Meem Heavy)",
            descriptionArabic = "صوت رخيم يخرج من الخيشوم بمقدار حركتين عند النون والميم المشددتين.",
            descriptionEnglish = "Nasalization sound produced through the nasal cavity held for 2 counts on doubled Noon and Meem.",
            colorHex = "#2E7D32", // Emerald Green
            exampleArabic = "إِنَّ ٱللَّهَ غَفُورٌ رَّحِيمٌ",
            exampleSurah = "Al-Baqarah"
        ),
        TajweedRule(
            id = "iqlab",
            nameArabic = "الإقلاب",
            nameEnglish = "Iqlab (Conversion to Meem)",
            descriptionArabic = "قلب النون الساكنة أو التنوين ميماً مخفاة مع الغنة عند لقائها بحرف الباء.",
            descriptionEnglish = "Converting Nun Sakinah or Tanween into a concealed Meem sound with Ghunnah when preceding Ba.",
            colorHex = "#1565C0", // Sapphire Blue
            exampleArabic = "مِن بَعْدِ مَا جَاءَتْهُمُ ٱلْبَيِّنَاتُ",
            exampleSurah = "Al-Baqarah: 213"
        ),
        TajweedRule(
            id = "idgham",
            nameArabic = "الإدغام بغنة وبغير غنة",
            nameEnglish = "Idgham (Merging With / Without Ghunnah)",
            descriptionArabic = "إدخال حرف ساكن في حرف متحرك بحيث يصيران حرفاً واحداً مشدداً عند حروف (يرملون).",
            descriptionEnglish = "Merging a sukoon letter into a vowel letter forming one doubled letter (Letters: Y-R-M-L-W-N).",
            colorHex = "#7B1FA2", // Deep Purple
            exampleArabic = "مَن يَقُولُ ، مِّن رَّبِّهِمْ",
            exampleSurah = "Al-Baqarah: 8"
        ),
        TajweedRule(
            id = "ikhfa",
            nameArabic = "الإخفاء الحقيقي",
            nameEnglish = "Ikhfa (Concealment)",
            descriptionArabic = "النطق بالحرف بصفة بين الإظهار والإدغام عارٍ عن التشديد مع بقاء الغنة عند 15 حرفاً.",
            descriptionEnglish = "Pronouncing the sound between clarity and merging without doubling, retaining nasalization.",
            colorHex = "#E65100", // Burnt Orange
            exampleArabic = "أُنزِلَ ، كُنتُمْ ، مَن ذَا",
            exampleSurah = "Al-Baqarah: 4"
        ),
        TajweedRule(
            id = "qalqalah",
            nameArabic = "القلقلة",
            nameEnglish = "Qalqalah (Echoing Sound)",
            descriptionArabic = "اضطراب الصوت عند النطق بالحرف الساكن حتى يسمع له نبرة قوية (حروف: قطب جد).",
            descriptionEnglish = "Vibration or echoing resonance sound when pronouncing a letter with Sukoon (Letters: Q-T-B-J-D).",
            colorHex = "#C62828", // Ruby Red
            exampleArabic = "قُلْ هُوَ ٱللَّهُ أَحَدٌ ۝ ٱللَّهُ ٱلصَّمَدُ ۝ لَمْ يَلِدْ",
            exampleSurah = "Al-Ikhlas"
        ),
        TajweedRule(
            id = "madd",
            nameArabic = "أحكام المدود",
            nameEnglish = "Madd (Elongation / Extension)",
            descriptionArabic = "إطالة الصوت بحرف من حروف المد الثلاثة (الأليق، الواو، الياء) عند وجود سبب كالهمز أو السكون.",
            descriptionEnglish = "Prolongation of sound with one of the three vowel letters (Alif, Waw, Ya) 2, 4, or 6 counts.",
            colorHex = "#F57F17", // Amber Gold
            exampleArabic = "السَّمَاءِ ۖ ٱلضَّآلِّينَ ۖ جَاءَ",
            exampleSurah = "Al-Fatihah / Al-Baqarah"
        )
    )
}
