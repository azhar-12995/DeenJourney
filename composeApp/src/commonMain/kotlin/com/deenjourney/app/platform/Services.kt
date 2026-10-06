package com.deenjourney.app.platform

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

data class GeoFix(val lat: Double, val lng: Double, val accuracyM: Float = 0f)

/** One-shot location (GPS / network). Returns null on timeout, no permission or services off. */
expect class LocationService() {
    suspend fun current(timeoutMs: Long = 15_000): GeoFix?
    fun servicesEnabled(): Boolean
}

/** azimuth: degrees from TRUE north (0..360) the top of the phone points to; accuracy 0 (unreliable) .. 3 (high). */
data class Heading(val azimuth: Float, val accuracy: Int)

expect class CompassService() {
    val available: Boolean
    /** Emits headings while collected. [lat]/[lng] are used to correct magnetic declination. */
    fun headings(lat: Double, lng: Double): Flow<Heading>
}

// ---------------------------------------------------------------- recitation player

data class AudioItem(val id: String, val url: String, val fallbackUrl: String?, val localPath: String?, val title: String, val subtitle: String, val sura: Int = 0, val aya: Int = 0)

enum class RepeatMode { Off, One, All }

data class PlayerState(
    val items: List<AudioItem> = emptyList(), val index: Int = -1, val playing: Boolean = false, val buffering: Boolean = false,
    val positionMs: Long = 0, val durationMs: Long = 0, val speed: Float = 1f, val repeat: RepeatMode = RepeatMode.Off,
    val error: String? = null, val sleepAtMs: Long? = null, val sleepEndOfQueue: Boolean = false,
) {
    val current: AudioItem? get() = items.getOrNull(index)
}

/** Background recitation with lock-screen controls (Media3 MediaSession on Android, AVPlayer + MPNowPlaying on iOS). */
expect class RecitationPlayer() {
    val state: StateFlow<PlayerState>
    fun load(items: List<AudioItem>, startIndex: Int = 0, startMs: Long = 0, autoplay: Boolean = true)
    fun play()
    fun pause()
    fun toggle()
    fun seekTo(ms: Long)
    fun next()
    fun previous()
    fun jumpTo(index: Int)
    fun setSpeed(speed: Float)
    fun setRepeat(mode: RepeatMode)
    fun stop()
    /** Pauses after [minutes] (0 = cancel); [fade] lowers the volume over the last 10 s. */
    fun sleepAfter(minutes: Int, fade: Boolean = true)
    fun sleepAtEndOfQueue()
}

/** Short one-off sounds (letter pronunciation, chime preview). */
expect object SoundFx {
    fun playUrl(url: String)
    fun playResource(path: String)
    fun stop()
}

// ---------------------------------------------------------------- alarms & notifications

/** Re-plans every local alarm/notification from current settings (prayer times, reminders). Safe to call often. */
expect object Scheduler {
    fun reschedule()
    fun canScheduleExact(): Boolean
    fun openExactAlarmSettings()
    fun openBatterySettings()
    fun notify(id: Int, title: String, body: String, channel: String = "reminders")
}
