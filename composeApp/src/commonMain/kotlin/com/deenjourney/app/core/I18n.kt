package com.deenjourney.app.core

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import kotlinx.datetime.Month
import kotlinx.serialization.Serializable

/** UI languages. Urdu and Arabic are right-to-left. */
enum class Lang(val code: String, val rtl: Boolean, val nativeName: String, val englishName: String) {
    EN("en", false, "English", "English"),
    UR("ur", true, "اردو", "Urdu"),
    AR("ar", true, "العربية", "Arabic");

    fun pick(en: String, ur: String?, ar: String?): String = when (this) {
        EN -> en
        UR -> ur?.takeIf { it.isNotBlank() } ?: en
        AR -> ar?.takeIf { it.isNotBlank() } ?: en
    }

    companion object {
        fun of(code: String?): Lang = entries.firstOrNull { it.code == code } ?: EN
    }
}

val LocalLang = staticCompositionLocalOf { Lang.EN }

/** Inline translation: t("Prayer times", "نماز کے اوقات", "مواقيت الصلاة"). Missing translations fall back to English. */
@Composable
@ReadOnlyComposable
fun t(en: String, ur: String? = null, ar: String? = null): String = LocalLang.current.pick(en, ur, ar)

/** Localised content string from JSON: {"en": "...", "ur": "...", "ar": "..."}. */
@Serializable
data class L(val en: String = "", val ur: String? = null, val ar: String? = null) {
    fun get(lang: Lang): String = lang.pick(en, ur, ar)
    val isEmpty: Boolean get() = en.isBlank() && ur.isNullOrBlank() && ar.isNullOrBlank()
}

@Composable
@ReadOnlyComposable
fun L.text(): String = get(LocalLang.current)

@Composable
@ReadOnlyComposable
fun L?.textOrNull(): String? = this?.get(LocalLang.current)?.takeIf { it.isNotBlank() }

// ---------------------------------------------------------------- numbers, dates & times

private const val ARABIC_DIGITS = "٠١٢٣٤٥٦٧٨٩"

/** Arabic UI uses Arabic-Indic digits; English and Urdu (Pakistan) use Western digits. */
fun Lang.digits(s: String): String =
    if (this == Lang.AR) s.map { if (it in '0'..'9') ARABIC_DIGITS[it - '0'] else it }.joinToString("") else s

fun Lang.num(n: Int): String = digits(n.toString())

fun Lang.num(n: Long): String = digits(n.toString())

/** 1,234,567 grouping (no locale APIs needed). */
fun Lang.grouped(n: Long): String {
    val neg = n < 0
    val s = kotlin.math.abs(n).toString().reversed().chunked(3).joinToString(",").reversed()
    return digits(if (neg) "-$s" else s)
}

fun Lang.time(t: LocalTime, use24h: Boolean = false): String {
    val m = t.minute.toString().padStart(2, '0')
    if (use24h) return digits("${t.hour.toString().padStart(2, '0')}:$m")
    val h12 = if (t.hour % 12 == 0) 12 else t.hour % 12
    val am = t.hour < 12
    val suffix = when (this) {
        Lang.EN -> if (am) "AM" else "PM"
        Lang.UR -> if (am) "صبح" else when (t.hour) { in 12..15 -> "دوپہر"; in 16..18 -> "شام"; else -> "رات" }
        Lang.AR -> if (am) "ص" else "م"
    }
    return digits("$h12:$m") + " " + suffix
}

/** Short time without suffix, e.g. "5:09" — used in compact prayer strips. */
fun Lang.timeShort(t: LocalTime, use24h: Boolean = false): String {
    val m = t.minute.toString().padStart(2, '0')
    return if (use24h) digits("${t.hour.toString().padStart(2, '0')}:$m") else digits("${if (t.hour % 12 == 0) 12 else t.hour % 12}:$m")
}

fun Lang.monthName(m: Month, short: Boolean = false): String {
    val i = m.ordinal
    val en = listOf("January", "February", "March", "April", "May", "June", "July", "August", "September", "October", "November", "December")
    val ur = listOf("جنوری", "فروری", "مارچ", "اپریل", "مئی", "جون", "جولائی", "اگست", "ستمبر", "اکتوبر", "نومبر", "دسمبر")
    val ar = listOf("يناير", "فبراير", "مارس", "أبريل", "مايو", "يونيو", "يوليو", "أغسطس", "سبتمبر", "أكتوبر", "نوفمبر", "ديسمبر")
    return when (this) {
        Lang.EN -> if (short) en[i].take(3) else en[i]
        Lang.UR -> ur[i]
        Lang.AR -> ar[i]
    }
}

fun Lang.dayName(d: DayOfWeek, short: Boolean = false): String {
    val i = d.ordinal // MONDAY = 0
    val en = listOf("Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday")
    val ur = listOf("پیر", "منگل", "بدھ", "جمعرات", "جمعہ", "ہفتہ", "اتوار")
    val ar = listOf("الاثنين", "الثلاثاء", "الأربعاء", "الخميس", "الجمعة", "السبت", "الأحد")
    return when (this) {
        Lang.EN -> if (short) en[i].take(3) else en[i]
        Lang.UR -> ur[i]
        Lang.AR -> ar[i]
    }
}

/** "Sat, 3 Oct 2026" / "ہفتہ، 3 اکتوبر 2026" / "السبت، ٣ أكتوبر ٢٠٢٦" */
fun Lang.date(d: LocalDate, withYear: Boolean = true, withDay: Boolean = true): String {
    val day = if (withDay) dayName(d.dayOfWeek, short = this == Lang.EN) + (if (this == Lang.EN) ", " else "، ") else ""
    val y = if (withYear) " ${d.year}" else ""
    return digits("$day${d.day} ${monthName(d.month, short = this == Lang.EN)}$y")
}

/** "1h 24m" style durations. */
fun Lang.duration(totalMinutes: Long): String {
    val h = totalMinutes / 60
    val m = totalMinutes % 60
    return when (this) {
        Lang.EN -> if (h > 0) "${h}h ${m}m" else "${m}m"
        Lang.UR -> if (h > 0) "$h گھنٹے $m منٹ" else "$m منٹ"
        Lang.AR -> digits(if (h > 0) "$h س $m د" else "$m د")
    }
}

/** h:mm:ss countdown, always Western/Arabic digits per language. */
fun Lang.countdown(totalSeconds: Long): String {
    val s = totalSeconds.coerceAtLeast(0)
    val h = s / 3600
    val m = (s % 3600) / 60
    val sec = s % 60
    return digits("$h:${m.toString().padStart(2, '0')}:${sec.toString().padStart(2, '0')}")
}
