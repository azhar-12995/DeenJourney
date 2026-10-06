package com.deenjourney.app.data.prayer

import com.deenjourney.app.data.hijri.Hijri
import com.deenjourney.app.data.hijri.HijriDate
import com.deenjourney.app.data.settings.AppSettings
import com.deenjourney.app.data.settings.SettingsRepo
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flow
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Clock
import kotlin.time.Instant

/** Everything the UI needs about "now": today's times, the next prayer and a live countdown. */
data class PrayerNow(
    val now: Instant, val zone: TimeZone, val today: LocalDate, val hijri: HijriDate, val times: DayTimes?,
    val next: PrayerName?, val nextAt: Instant?, val current: PrayerName?, val settings: AppSettings,
) {
    val secondsToNext: Long get() = nextAt?.let { (it - now).inWholeSeconds } ?: 0
    val period: String get() {
        val t = times ?: return "day"
        return when {
            now < t.at.getValue(PrayerName.Fajr) -> "night"
            now < t.at.getValue(PrayerName.Sunrise) + kotlin.time.Duration.parse("30m") -> "dawn"
            now < t.at.getValue(PrayerName.Asr) -> "day"
            now < t.at.getValue(PrayerName.Isha) -> "dusk"
            else -> "night"
        }
    }
}

class PrayerClock(private val settings: SettingsRepo) {
    private fun ticks(periodMs: Long): Flow<Instant> = flow { while (true) { emit(Clock.System.now()); delay(periodMs) } }

    fun compute(s: AppSettings, now: Instant): PrayerNow {
        val loc = s.location
        val zone = loc?.tz?.let { runCatching { TimeZone.of(it) }.getOrNull() } ?: TimeZone.currentSystemDefault()
        val today = now.toLocalDateTime(zone).date
        val hijri = Hijri.fromGregorian(today, s.hijriOffset)
        if (loc == null) return PrayerNow(now, zone, today, hijri, null, null, null, null, s)
        val times = PrayerEngine.day(today, loc.lat, loc.lng, zone, s.prayer, s.hijriOffset)
        val (next, at) = PrayerEngine.next(now, loc.lat, loc.lng, zone, s.prayer, s.hijriOffset)
        val current = PrayerName.entries.filter { it.hasAdhan }.lastOrNull { times.at.getValue(it) <= now } ?: PrayerName.Isha
        return PrayerNow(now, zone, today, hijri, times, next, at, current, s)
    }

    /** Emits every second while collected. */
    fun live(periodMs: Long = 1000): Flow<PrayerNow> = combine(settings.flow, ticks(periodMs)) { s, now -> compute(s, now) }
}
