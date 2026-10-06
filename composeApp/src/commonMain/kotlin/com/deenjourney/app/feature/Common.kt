package com.deenjourney.app.feature

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.deenjourney.app.core.Lang
import com.deenjourney.app.core.Platform
import com.deenjourney.app.core.date
import com.deenjourney.app.core.timeShort
import com.deenjourney.app.data.content.Lesson
import com.deenjourney.app.data.user.ProfileE
import kotlinx.coroutines.delay
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Clock
import kotlin.time.Instant

fun today(zone: TimeZone = TimeZone.currentSystemDefault()): LocalDate = Clock.System.now().toLocalDateTime(zone).date

/** A stable "item of the day" from a list (same for everyone on the same date). */
fun <T> List<T>.forDay(d: LocalDate, salt: Int = 0): T? = if (isEmpty()) null else this[((d.toEpochDays() + salt) % size).toInt().let { if (it < 0) it + size else it }]

/** Lessons marked "kids" are hidden from adult profiles' roadmap suggestions and "adult" ones from children. */
fun ProfileE?.allows(l: Lesson): Boolean = when (l.ages) { "kids" -> this?.childMode != false; "adult" -> this?.childMode != true; else -> true }

/** "Today, 6:40 AM" / "Yesterday" / "3 Oct". */
fun Lang.relative(ms: Long, todayWord: Boolean = true): String {
    val zone = TimeZone.currentSystemDefault()
    val dt = Instant.fromEpochMilliseconds(ms).toLocalDateTime(zone)
    val t = today(zone)
    return when (dt.date) {
        t -> (if (todayWord) pick("Today, ", "آج، ", "اليوم، ") else "") + timeShort(dt.time, Platform.is24Hour())
        LocalDate.fromEpochDays(t.toEpochDays() - 1) -> pick("Yesterday", "کل", "أمس")
        else -> date(dt.date, withYear = dt.date.year != t.year, withDay = false)
    }
}

/** Re-composes every [periodMs] (for clocks that don't need a flow). */
@Composable
fun rememberTicker(periodMs: Long = 60_000): Long {
    var tick by remember { mutableStateOf(Clock.System.now().toEpochMilliseconds()) }
    LaunchedEffect(periodMs) { while (true) { delay(periodMs); tick = Clock.System.now().toEpochMilliseconds() } }
    return tick
}
