package com.deenjourney.app.data.hijri

import com.deenjourney.app.core.Lang
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.LocalDate
import kotlinx.datetime.plus

data class HijriDate(val year: Int, val month: Int, val day: Int) : Comparable<HijriDate> {
    override fun compareTo(other: HijriDate): Int = compareValuesBy(this, other, { it.year }, { it.month }, { it.day })
}

/**
 * Umm al-Qura calendar (official Saudi tables, 1400–1500 AH) with the tabular (Kuwaiti) algorithm as fallback
 * outside the table. [offset] shifts the result by whole days for local moon sighting (e.g. −1 in Pakistan).
 */
object Hijri {
    private val monthStarts: LongArray by lazy {
        val out = LongArray(UAQ_MONTHS.size * 12 + 1)
        var j = UAQ_FIRST_JDN
        var i = 0
        for (y in UAQ_MONTHS.indices) for (m in 0 until 12) {
            out[i++] = j
            j += if (UAQ_MONTHS[y] and (1 shl m) != 0) 30 else 29
        }
        out[i] = j
        out
    }

    private fun LocalDate.jdn(): Long = toEpochDays().toLong() + 2440588L
    private fun fromJdn(j: Long): LocalDate = LocalDate.fromEpochDays((j - 2440588L).toInt())

    fun fromGregorian(date: LocalDate, offset: Int = 0): HijriDate {
        val j = date.plus(DatePeriod(days = offset)).jdn()
        val starts = monthStarts
        if (j >= starts[0] && j < starts[starts.size - 1]) {
            // binary search for the month containing j
            var lo = 0
            var hi = starts.size - 2
            while (lo < hi) {
                val mid = (lo + hi + 1) / 2
                if (starts[mid] <= j) lo = mid else hi = mid - 1
            }
            return HijriDate(UAQ_FIRST_YEAR + lo / 12, lo % 12 + 1, (j - starts[lo]).toInt() + 1)
        }
        return tabularFromJdn(j)
    }

    fun toGregorian(h: HijriDate, offset: Int = 0): LocalDate {
        val idx = (h.year - UAQ_FIRST_YEAR) * 12 + (h.month - 1)
        val j = if (idx >= 0 && idx < monthStarts.size - 1) monthStarts[idx] + h.day - 1 else tabularToJdn(h)
        return fromJdn(j).plus(DatePeriod(days = -offset))
    }

    fun monthLength(year: Int, month: Int): Int {
        val idx = (year - UAQ_FIRST_YEAR) * 12 + (month - 1)
        return if (idx >= 0 && idx < monthStarts.size - 1) (monthStarts[idx + 1] - monthStarts[idx]).toInt()
        else if (month % 2 == 1 || (month == 12 && isTabularLeap(year))) 30 else 29
    }

    // ---- tabular Islamic calendar (civil epoch, 2,5,7,10,13,16,18,21,24,26,29 leap years) ----
    private fun isTabularLeap(y: Int) = ((11 * y + 14) % 30) < 11
    private fun tabularToJdn(h: HijriDate): Long =
        h.day + kotlin.math.ceil(29.5 * (h.month - 1)).toLong() + (h.year - 1) * 354L + ((3 + 11 * h.year) / 30).toLong() + 1948439L
    private fun tabularFromJdn(j: Long): HijriDate {
        val y = ((30 * (j - 1948439L) + 10646) / 10631).toInt()
        var m = 1
        while (m < 12 && j >= tabularToJdn(HijriDate(y, m + 1, 1))) m++
        val d = (j - tabularToJdn(HijriDate(y, m, 1))).toInt() + 1
        return HijriDate(y, m, d)
    }

    private val EN = listOf("Muharram", "Safar", "Rabi‘ al-Awwal", "Rabi‘ al-Thani", "Jumada al-Ula", "Jumada al-Akhirah", "Rajab", "Sha‘ban", "Ramadan", "Shawwal", "Dhul Qa‘dah", "Dhul Hijjah")
    private val UR = listOf("محرم", "صفر", "ربیع الاول", "ربیع الثانی", "جمادی الاول", "جمادی الثانی", "رجب", "شعبان", "رمضان", "شوال", "ذوالقعدہ", "ذوالحجہ")
    private val AR = listOf("محرم", "صفر", "ربيع الأول", "ربيع الآخر", "جمادى الأولى", "جمادى الآخرة", "رجب", "شعبان", "رمضان", "شوال", "ذو القعدة", "ذو الحجة")

    fun monthName(month: Int, lang: Lang): String = when (lang) {
        Lang.EN -> EN[month - 1]
        Lang.UR -> UR[month - 1]
        Lang.AR -> AR[month - 1]
    }

    /** "22 Rabi‘ al-Thani 1448 AH" / "22 ربیع الثانی 1448ھ" / "٢٢ ربيع الآخر ١٤٤٨هـ" */
    fun format(h: HijriDate, lang: Lang, withYear: Boolean = true, suffix: Boolean = true): String {
        val y = if (!withYear) "" else when (lang) {
            Lang.EN -> " ${h.year}" + if (suffix) " AH" else ""
            Lang.UR -> " ${h.year}" + if (suffix) "ھ" else ""
            Lang.AR -> " ${h.year}" + if (suffix) "هـ" else ""
        }
        return lang.digitsSafe("${h.day} ${monthName(h.month, lang)}$y")
    }

    private fun Lang.digitsSafe(s: String) = if (this == Lang.AR) s.map { if (it in '0'..'9') "٠١٢٣٤٥٦٧٨٩"[it - '0'] else it }.joinToString("") else s
}
