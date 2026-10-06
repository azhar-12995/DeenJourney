package com.deenjourney.app.platform

import com.deenjourney.app.core.Lang
import com.deenjourney.app.core.time
import com.deenjourney.app.data.prayer.PrayerEngine
import com.deenjourney.app.data.prayer.PrayerName
import com.deenjourney.app.data.settings.AppSettings
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atTime
import kotlinx.datetime.plus
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Clock
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Instant

/** kind: adhan | pre | jumuah | lesson | adhkar_m | adhkar_e */
data class PlannedAlarm(val id: Int, val at: Instant, val kind: String, val title: String, val body: String, val sound: String, val prayer: PrayerName? = null)

object AlarmPlanner {
    fun prayerName(p: PrayerName, lang: Lang): String = when (p) {
        PrayerName.Fajr -> lang.pick("Fajr", "فجر", "الفجر")
        PrayerName.Sunrise -> lang.pick("Sunrise", "طلوعِ آفتاب", "الشروق")
        PrayerName.Dhuhr -> lang.pick("Dhuhr", "ظہر", "الظهر")
        PrayerName.Asr -> lang.pick("Asr", "عصر", "العصر")
        PrayerName.Maghrib -> lang.pick("Maghrib", "مغرب", "المغرب")
        PrayerName.Isha -> lang.pick("Isha", "عشاء", "العشاء")
    }

    /** Alarms for the next [days] days, sorted by time. iOS keeps ≤ 64 pending, Android schedules them all. */
    fun plan(s: AppSettings, days: Int = 2, now: Instant = Clock.System.now()): List<PlannedAlarm> {
        val loc = s.location ?: return emptyList()
        val lang = s.language
        val zone = runCatching { TimeZone.of(loc.tz) }.getOrDefault(TimeZone.currentSystemDefault())
        val today = now.toLocalDateTime(zone).date
        val out = ArrayList<PlannedAlarm>()
        for (d in 0 until days) {
            val date = today.plus(DatePeriod(days = d))
            val times = PrayerEngine.day(date, loc.lat, loc.lng, zone, s.prayer, s.hijriOffset)
            for (p in PrayerName.entries) {
                val pref = s.adhan[p.name] ?: continue
                if (!pref.on) continue
                val at = times.at.getValue(p)
                val name = prayerName(p, lang)
                val tLocal = lang.time(times.local(p))
                if (at > now) out += PlannedAlarm(
                    id = 1000 + d * 20 + p.ordinal, at = at, kind = "adhan", prayer = p, sound = pref.sound,
                    title = if (p == PrayerName.Sunrise) lang.pick("Sunrise · $tLocal", "طلوعِ آفتاب · $tLocal", "الشروق · $tLocal") else lang.pick("$name · $tLocal", "$name · $tLocal", "$name · $tLocal"),
                    body = if (p == PrayerName.Sunrise) lang.pick("The time for Fajr has ended.", "فجر کا وقت ختم ہو گیا۔", "انتهى وقت الفجر.")
                    else lang.pick("It’s time for $name in ${loc.name}.", "${loc.name} میں $name کا وقت ہو گیا ہے۔", "حان وقت صلاة $name في ${loc.name}."),
                )
                if (s.reminderBefore > 0 && p.hasAdhan) {
                    val pre = at - s.reminderBefore.minutes
                    if (pre > now) out += PlannedAlarm(1500 + d * 20 + p.ordinal, pre, "pre", lang.pick("$name in ${s.reminderBefore} min", "$name ${s.reminderBefore} منٹ میں", "$name بعد ${s.reminderBefore} دقيقة"),
                        lang.pick("Get ready for $name — $tLocal.", "$name کی تیاری کریں — $tLocal", "استعد لصلاة $name — $tLocal"), "chime", p)
                }
            }
            if (s.jumuahReminder && date.dayOfWeek == DayOfWeek.FRIDAY) {
                val at = times.at.getValue(PrayerName.Dhuhr) - 60.minutes
                if (at > now) out += PlannedAlarm(1900 + d, at, "jumuah", lang.pick("Jumu‘ah today", "آج جمعہ ہے", "اليوم الجمعة"),
                    lang.pick("Prepare for Jumu‘ah: ghusl, Surah Al-Kahf and blessings on the Prophet ﷺ.", "جمعہ کی تیاری: غسل، سورۃ الکہف اور درود شریف۔", "استعد للجمعة: الغسل وسورة الكهف والصلاة على النبي ﷺ."), "chime")
            }
            if (s.lessonReminder) {
                val (h, m) = s.lessonReminderTime.split(":").map { it.toIntOrNull() ?: 0 }.let { it.getOrElse(0) { 20 } to it.getOrElse(1) { 0 } }
                val at = date.atTime(LocalTime(h.coerceIn(0, 23), m.coerceIn(0, 59))).toInstant(zone)
                if (at > now) out += PlannedAlarm(1950 + d, at, "lesson", lang.pick("Your daily lesson is ready", "آپ کا روزانہ سبق تیار ہے", "درسك اليومي جاهز"),
                    lang.pick("A little learning each day leads to big change.", "روز تھوڑا سا سیکھنا بڑی تبدیلی لاتا ہے۔", "قليل من التعلم كل يوم يصنع تغييرًا كبيرًا."), "chime")
            }
            if (s.adhkarReminder) {
                val fajr = times.at.getValue(PrayerName.Fajr) + 20.minutes
                val asr = times.at.getValue(PrayerName.Asr) + 20.minutes
                if (fajr > now) out += PlannedAlarm(1960 + d, fajr, "adhkar_m", lang.pick("Morning adhkar", "صبح کے اذکار", "أذكار الصباح"), lang.pick("Start the day with remembrance of Allah.", "دن کا آغاز اللہ کے ذکر سے کریں۔", "ابدأ يومك بذكر الله."), "chime")
                if (asr > now) out += PlannedAlarm(1970 + d, asr, "adhkar_e", lang.pick("Evening adhkar", "شام کے اذکار", "أذكار المساء"), lang.pick("Take a few minutes for the evening adhkar.", "شام کے اذکار کے لیے چند منٹ نکالیں۔", "خصص دقائق لأذكار المساء."), "chime")
            }
        }
        return out.sortedBy { it.at }
    }
}
