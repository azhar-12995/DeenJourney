package com.deenjourney.app.data.quran

data class QaidaLetter(val glyph: String, val name: String, val spokenArabic: String, val audioFile: String)

val qaidaLetters = listOf(
    QaidaLetter("ا", "Alif", "أَلِف", "01003.mp3"), QaidaLetter("ب", "Baa", "بَاء", "01004.mp3"),
    QaidaLetter("ت", "Taa", "تَاء", "01005.mp3"), QaidaLetter("ث", "Thaa", "ثَاء", "01006.mp3"),
    QaidaLetter("ج", "Jeem", "جِيم", "01007.mp3"), QaidaLetter("ح", "Haa", "حَاء", "01008.mp3"),
    QaidaLetter("خ", "Khaa", "خَاء", "01009.mp3"), QaidaLetter("د", "Daal", "دَال", "01010.mp3"),
    QaidaLetter("ذ", "Dhaal", "ذَال", "01011.mp3"), QaidaLetter("ر", "Raa", "رَاء", "01012.mp3"),
    QaidaLetter("ز", "Zaay", "زَاي", "01013.mp3"), QaidaLetter("س", "Seen", "سِين", "01014.mp3"),
    QaidaLetter("ش", "Sheen", "شِين", "01015.mp3"), QaidaLetter("ص", "Saad", "صَاد", "01016.mp3"),
    QaidaLetter("ض", "Daad", "ضَاد", "01017.mp3"), QaidaLetter("ط", "Taa", "طَاء", "01018.mp3"),
    QaidaLetter("ظ", "Dhaa", "ظَاء", "01019.mp3"), QaidaLetter("ع", "Ayn", "عَيْن", "01020.mp3"),
    QaidaLetter("غ", "Ghayn", "غَيْن", "01021.mp3"), QaidaLetter("ف", "Faa", "فَاء", "01022.mp3"),
    QaidaLetter("ق", "Qaaf", "قَاف", "01023.mp3"), QaidaLetter("ك", "Kaaf", "كَاف", "01024.mp3"),
    QaidaLetter("ل", "Laam", "لَام", "01025.mp3"), QaidaLetter("م", "Meem", "مِيم", "01026.mp3"),
    QaidaLetter("ن", "Noon", "نُون", "01027.mp3"), QaidaLetter("ه", "Haa", "هَاء", "01029.mp3"),
    QaidaLetter("و", "Waaw", "وَاو", "01028.mp3"), QaidaLetter("ي", "Yaa", "يَاء", "01031.mp3"),
    QaidaLetter("ء", "Hamzah", "هَمْزَة", "01030.mp3"),
)
