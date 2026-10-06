package com.deenjourney.app.data.prayer

import com.batoulapps.adhan2.CalculationMethod
import com.batoulapps.adhan2.CalculationParameters
import com.batoulapps.adhan2.Coordinates
import com.batoulapps.adhan2.HighLatitudeRule
import com.batoulapps.adhan2.Madhab
import com.batoulapps.adhan2.PrayerAdjustments
import com.batoulapps.adhan2.PrayerTimes
import com.batoulapps.adhan2.Qibla
import com.batoulapps.adhan2.SunnahTimes
import com.batoulapps.adhan2.data.DateComponents
import com.deenjourney.app.core.L
import com.deenjourney.app.data.hijri.Hijri
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.plus
import kotlinx.datetime.toLocalDateTime
import kotlinx.serialization.Serializable
import kotlin.math.abs
import kotlin.math.asin
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt
import kotlin.time.Instant

enum class PrayerName { Fajr, Sunrise, Dhuhr, Asr, Maghrib, Isha;
    val hasAdhan: Boolean get() = this != Sunrise
}

/** Calculation methods. Those Adhan doesn't ship are built from their published angles (see docs/DATA_SOURCES.md §7). */
enum class Method(val label: L, val fajr: Double = 0.0, val isha: Double = 0.0, val ishaInterval: Int = 0, val maghribAdj: Int = 0, val builtIn: CalculationMethod? = null) {
    MWL(L("Muslim World League", "مسلم ورلڈ لیگ", "رابطة العالم الإسلامي"), builtIn = CalculationMethod.MUSLIM_WORLD_LEAGUE),
    KARACHI(L("University of Islamic Sciences, Karachi", "جامعہ علوم اسلامیہ، کراچی", "جامعة العلوم الإسلامية، كراتشي"), builtIn = CalculationMethod.KARACHI),
    UMM_AL_QURA(L("Umm al-Qura, Makkah", "ام القریٰ، مکہ", "أم القرى، مكة"), builtIn = CalculationMethod.UMM_AL_QURA),
    EGYPTIAN(L("Egyptian General Authority", "مصری جنرل اتھارٹی", "الهيئة المصرية العامة للمساحة"), builtIn = CalculationMethod.EGYPTIAN),
    ISNA(L("ISNA (North America)", "اسنا (شمالی امریکہ)", "الجمعية الإسلامية لأمريكا الشمالية"), builtIn = CalculationMethod.NORTH_AMERICA),
    MOONSIGHTING(L("Moonsighting Committee", "مون سائٹنگ کمیٹی", "لجنة رؤية الهلال"), builtIn = CalculationMethod.MOON_SIGHTING_COMMITTEE),
    DUBAI(L("Dubai", "دبئی", "دبي"), builtIn = CalculationMethod.DUBAI),
    KUWAIT(L("Kuwait", "کویت", "الكويت"), builtIn = CalculationMethod.KUWAIT),
    QATAR(L("Qatar", "قطر", "قطر"), builtIn = CalculationMethod.QATAR),
    SINGAPORE(L("Singapore", "سنگاپور", "سنغافورة"), builtIn = CalculationMethod.SINGAPORE),
    TURKEY(L("Turkey (Diyanet)", "ترکی (دیانت)", "تركيا (ديانت)"), builtIn = CalculationMethod.TURKEY),
    GULF(L("Gulf region", "خلیجی ممالک", "منطقة الخليج"), fajr = 19.5, ishaInterval = 90),
    JAKIM(L("JAKIM (Malaysia)", "جاکم (ملائیشیا)", "جاكيم (ماليزيا)"), fajr = 20.0, isha = 18.0),
    KEMENAG(L("Kemenag (Indonesia)", "کیمیناگ (انڈونیشیا)", "وزارة الشؤون الدينية (إندونيسيا)"), fajr = 20.0, isha = 18.0),
    FRANCE(L("UOIF (France)", "یو او آئی ایف (فرانس)", "اتحاد المنظمات الإسلامية (فرنسا)"), fajr = 12.0, isha = 12.0),
    RUSSIA(L("Spiritual Administration of Russia", "روس", "الإدارة الدينية لمسلمي روسيا"), fajr = 16.0, isha = 15.0),
    MOROCCO(L("Morocco", "مراکش", "المغرب"), fajr = 19.0, isha = 17.0),
    ALGERIA(L("Algeria", "الجزائر", "الجزائر"), fajr = 18.0, isha = 17.0),
    TUNISIA(L("Tunisia", "تیونس", "تونس"), fajr = 18.0, isha = 18.0),
    JORDAN(L("Jordan", "اردن", "الأردن"), fajr = 18.0, isha = 18.0, maghribAdj = 5);

    val fajrIsha: String get() = when (builtIn) {
        CalculationMethod.UMM_AL_QURA -> "18.5° / 90 min"
        CalculationMethod.QATAR -> "18° / 90 min"
        null -> if (ishaInterval > 0) "$fajr° / $ishaInterval min" else "$fajr° / $isha°"
        else -> builtIn.parameters.let { if (it.ishaInterval > 0) "${it.fajrAngle}° / ${it.ishaInterval} min" else "${it.fajrAngle}° / ${it.ishaAngle}°" }
    }
}

enum class AsrMadhab { Standard, Hanafi }

enum class HighLat { Auto, MiddleOfNight, SeventhOfNight, TwilightAngle }

@Serializable
data class PrayerConfig(
    val method: String = Method.MWL.name,
    val asr: String = AsrMadhab.Standard.name,
    val highLat: String = HighLat.Auto.name,
    /** minutes per prayer: Fajr, Sunrise, Dhuhr, Asr, Maghrib, Isha */
    val adjust: List<Int> = listOf(0, 0, 0, 0, 0, 0),
)

data class DayTimes(val date: LocalDate, val zone: TimeZone, val at: Map<PrayerName, Instant>) {
    fun local(p: PrayerName): LocalTime = at.getValue(p).toLocalDateTime(zone).time
}

object PrayerEngine {
    val KAABA_LAT = 21.4225
    val KAABA_LNG = 39.8262

    fun params(cfg: PrayerConfig, lat: Double, ramadan: Boolean): CalculationParameters {
        val m = runCatching { Method.valueOf(cfg.method) }.getOrDefault(Method.MWL)
        val madhab = if (cfg.asr == AsrMadhab.Hanafi.name) Madhab.HANAFI else Madhab.SHAFI
        val hl = when (runCatching { HighLat.valueOf(cfg.highLat) }.getOrDefault(HighLat.Auto)) {
            HighLat.Auto -> if (abs(lat) >= 48.0) HighLatitudeRule.SEVENTH_OF_THE_NIGHT else HighLatitudeRule.MIDDLE_OF_THE_NIGHT
            HighLat.MiddleOfNight -> HighLatitudeRule.MIDDLE_OF_THE_NIGHT
            HighLat.SeventhOfNight -> HighLatitudeRule.SEVENTH_OF_THE_NIGHT
            HighLat.TwilightAngle -> HighLatitudeRule.TWILIGHT_ANGLE
        }
        val a = cfg.adjust + List((6 - cfg.adjust.size).coerceAtLeast(0)) { 0 }
        val ishaExtra = if (m == Method.UMM_AL_QURA && ramadan) 30 else 0
        val adj = PrayerAdjustments(a[0], a[1], a[2], a[3], a[4] + m.maghribAdj, a[5] + ishaExtra)
        val base = m.builtIn?.parameters ?: CalculationParameters(fajrAngle = m.fajr, ishaAngle = m.isha, ishaInterval = m.ishaInterval, method = CalculationMethod.OTHER)
        return base.copy(madhab = madhab, highLatitudeRule = hl, prayerAdjustments = adj)
    }

    fun day(date: LocalDate, lat: Double, lng: Double, zone: TimeZone, cfg: PrayerConfig, hijriOffset: Int = 0): DayTimes {
        val ramadan = Hijri.fromGregorian(date, hijriOffset).month == 9
        val pt = PrayerTimes(Coordinates(lat, lng), DateComponents(date.year, date.month.ordinal + 1, date.day), params(cfg, lat, ramadan))
        return DayTimes(date, zone, mapOf(
            PrayerName.Fajr to pt.fajr, PrayerName.Sunrise to pt.sunrise, PrayerName.Dhuhr to pt.dhuhr,
            PrayerName.Asr to pt.asr, PrayerName.Maghrib to pt.maghrib, PrayerName.Isha to pt.isha,
        ))
    }

    /** The next adhan prayer after [now] (rolls over to tomorrow's Fajr). */
    fun next(now: Instant, lat: Double, lng: Double, zone: TimeZone, cfg: PrayerConfig, hijriOffset: Int = 0, includeSunrise: Boolean = false): Pair<PrayerName, Instant> {
        val today = now.toLocalDateTime(zone).date
        val d = day(today, lat, lng, zone, cfg, hijriOffset)
        val order = PrayerName.entries.filter { includeSunrise || it.hasAdhan }
        for (p in order) { val t = d.at.getValue(p); if (t > now) return p to t }
        val tm = day(today.plus(DatePeriod(days = 1)), lat, lng, zone, cfg, hijriOffset)
        return PrayerName.Fajr to tm.at.getValue(PrayerName.Fajr)
    }

    /** The prayer whose time we are currently in (Isha after midnight belongs to the previous day). */
    fun current(now: Instant, lat: Double, lng: Double, zone: TimeZone, cfg: PrayerConfig): PrayerName? {
        val today = now.toLocalDateTime(zone).date
        val d = day(today, lat, lng, zone, cfg)
        return PrayerName.entries.filter { it.hasAdhan }.lastOrNull { d.at.getValue(it) <= now } ?: PrayerName.Isha
    }

    fun lastThirdOfNight(date: LocalDate, lat: Double, lng: Double, zone: TimeZone, cfg: PrayerConfig): Instant {
        val pt = PrayerTimes(Coordinates(lat, lng), DateComponents(date.year, date.month.ordinal + 1, date.day), params(cfg, lat, false))
        return SunnahTimes(pt).lastThirdOfTheNight
    }

    fun qibla(lat: Double, lng: Double): Double = Qibla(Coordinates(lat, lng)).direction

    /** Great-circle distance to the Kaaba in km. */
    fun distanceToKaabaKm(lat: Double, lng: Double): Double {
        val r = 6371.0
        val p1 = lat.rad(); val p2 = KAABA_LAT.rad()
        val dp = (KAABA_LAT - lat).rad(); val dl = (KAABA_LNG - lng).rad()
        val a = sin(dp / 2) * sin(dp / 2) + cos(p1) * cos(p2) * sin(dl / 2) * sin(dl / 2)
        return 2 * r * asin(sqrt(a))
    }

    /** Points along the great circle from (lat,lng) to the Kaaba, for the Qibla map. */
    fun greatCircle(lat: Double, lng: Double, steps: Int = 64): List<Pair<Double, Double>> {
        val f1 = lat.rad(); val l1 = lng.rad(); val f2 = KAABA_LAT.rad(); val l2 = KAABA_LNG.rad()
        val d = 2 * asin(sqrt(sin((f2 - f1) / 2).let { it * it } + cos(f1) * cos(f2) * sin((l2 - l1) / 2).let { it * it }))
        if (d == 0.0) return listOf(lat to lng)
        return (0..steps).map { i ->
            val f = i.toDouble() / steps
            val a = sin((1 - f) * d) / sin(d); val b = sin(f * d) / sin(d)
            val x = a * cos(f1) * cos(l1) + b * cos(f2) * cos(l2)
            val y = a * cos(f1) * sin(l1) + b * cos(f2) * sin(l2)
            val z = a * sin(f1) + b * sin(f2)
            atan2(z, sqrt(x * x + y * y)).deg() to atan2(y, x).deg()
        }
    }

    private fun Double.rad() = this * kotlin.math.PI / 180.0
    private fun Double.deg() = this * 180.0 / kotlin.math.PI
}

/** Customary method + Asr school by country (docs/DATA_SOURCES.md §7). The user can always change it. */
object MethodDefaults {
    fun forCountry(cc: String?): PrayerConfig {
        val c = cc?.uppercase() ?: ""
        val hanafi = c in setOf("PK", "IN", "BD", "AF", "LK", "NP", "TR", "AZ", "BA", "AL", "XK", "MK", "UZ", "TJ", "KZ", "KG", "TM", "RU")
        val m = when (c) {
            "PK", "IN", "BD", "AF", "LK", "NP" -> Method.KARACHI
            "SA", "YE" -> Method.UMM_AL_QURA
            "AE" -> Method.DUBAI
            "KW" -> Method.KUWAIT
            "QA" -> Method.QATAR
            "BH", "OM" -> Method.GULF
            "EG", "SD", "LY", "SY", "LB", "IQ", "PS" -> Method.EGYPTIAN
            "JO" -> Method.JORDAN
            "TR", "AZ", "BA", "AL", "XK", "MK" -> Method.TURKEY
            "MY", "BN" -> Method.JAKIM
            "SG" -> Method.SINGAPORE
            "ID" -> Method.KEMENAG
            "MA" -> Method.MOROCCO
            "DZ" -> Method.ALGERIA
            "TN" -> Method.TUNISIA
            "RU" -> Method.RUSSIA
            "FR" -> Method.FRANCE
            "US", "CA" -> Method.ISNA
            else -> Method.MWL
        }
        return PrayerConfig(method = m.name, asr = if (hanafi) AsrMadhab.Hanafi.name else AsrMadhab.Standard.name)
    }

    /** Suggested Hijri offset by country (South Asia often a day behind Umm al-Qura). */
    fun hijriOffsetFor(cc: String?): Int = if (cc?.uppercase() in setOf("PK", "IN", "BD")) -1 else 0
}
