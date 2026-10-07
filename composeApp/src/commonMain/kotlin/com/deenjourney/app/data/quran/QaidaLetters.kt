package com.deenjourney.app.data.quran

data class QaidaLetter(val glyph: String, val name: String, val spokenArabic: String)

val qaidaLetters = listOf(
    QaidaLetter("ا", "Alif", "أَلِف"), QaidaLetter("ب", "Baa", "بَاء"),
    QaidaLetter("ت", "Taa", "تَاء"), QaidaLetter("ث", "Thaa", "ثَاء"),
    QaidaLetter("ج", "Jeem", "جِيم"), QaidaLetter("ح", "Haa", "حَاء"),
    QaidaLetter("خ", "Khaa", "خَاء"), QaidaLetter("د", "Daal", "دَال"),
    QaidaLetter("ذ", "Dhaal", "ذَال"), QaidaLetter("ر", "Raa", "رَاء"),
    QaidaLetter("ز", "Zaay", "زَاي"), QaidaLetter("س", "Seen", "سِين"),
    QaidaLetter("ش", "Sheen", "شِين"), QaidaLetter("ص", "Saad", "صَاد"),
    QaidaLetter("ض", "Daad", "ضَاد"), QaidaLetter("ط", "Taa", "طَاء"),
    QaidaLetter("ظ", "Dhaa", "ظَاء"), QaidaLetter("ع", "Ayn", "عَيْن"),
    QaidaLetter("غ", "Ghayn", "غَيْن"), QaidaLetter("ف", "Faa", "فَاء"),
    QaidaLetter("ق", "Qaaf", "قَاف"), QaidaLetter("ك", "Kaaf", "كَاف"),
    QaidaLetter("ل", "Laam", "لَام"), QaidaLetter("م", "Meem", "مِيم"),
    QaidaLetter("ن", "Noon", "نُون"), QaidaLetter("ه", "Haa", "هَاء"),
    QaidaLetter("و", "Waaw", "وَاو"), QaidaLetter("ي", "Yaa", "يَاء"),
    QaidaLetter("ء", "Hamzah", "هَمْزَة"),
)
