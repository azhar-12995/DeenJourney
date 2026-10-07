package com.deenjourney.app.data.settings

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.deenjourney.app.core.Lang
import com.deenjourney.app.core.Platform
import com.deenjourney.app.data.sync.CloudEnvironment
import com.deenjourney.app.data.user.nowMs
import com.deenjourney.app.data.prayer.PrayerConfig
import com.deenjourney.app.data.prayer.PrayerName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import okio.Path.Companion.toPath

@Serializable
data class SavedLocation(
    val name: String, val country: String? = null, val cc: String? = null,
    val lat: Double, val lng: Double, val tz: String, val gps: Boolean = false,
)

@Serializable
data class AdhanPref(val on: Boolean = true, val sound: String = "chime")

@Serializable
data class QuranPrefs(
    val showUrdu: Boolean = true, val showEnglish: Boolean = true, val arabicScale: Float = 1f, val translationScale: Float = 1f,
    val wordByWord: Boolean = false, val reciter: String = "alafasy", val speed: Float = 1f, val repeat: String = "off", val followAudio: Boolean = true,
)

@Serializable
data class LastRead(val sura: Int = 1, val aya: Int = 1, val audioMs: Long = 0, val at: Long = 0)

@Serializable
data class AppSettings(
    val lang: String? = null,
    val theme: String = "system",
    val textScale: Float = 1f,
    val location: SavedLocation? = null,
    val prayer: PrayerConfig = PrayerConfig(),
    val methodChosen: Boolean = false,
    val hijriOffset: Int = 0,
    val adhan: Map<String, AdhanPref> = PrayerName.entries.associate { it.name to AdhanPref(on = it != PrayerName.Sunrise) },
    val reminderBefore: Int = 0,
    val jumuahReminder: Boolean = true,
    val lessonReminder: Boolean = true,
    val lessonReminderTime: String = "20:00",
    val adhkarReminder: Boolean = false,
    val notificationsAsked: Boolean = false,
    val fiqh: String = "hanafi",
    val quran: QuranPrefs = QuranPrefs(),
    val lastRead: LastRead? = null,
    val activeProfile: String? = null,
    val setupDone: Boolean = false,
    val familyDone: Boolean = false,
    val dailyGoal: Int = 10,
    val focus: List<String> = listOf("quran", "character"),
    val pinHash: String? = null,
    val childLock: Boolean = false,
    val analytics: Boolean = false,
    val crashReports: Boolean = true,
    val tasbihHaptic: Boolean = true,
    val tasbihSound: Boolean = false,
    val tasbihAutoReset: Boolean = false,
    val qaidaAudioEnabled: Boolean = true,
    val nisab: String = "silver",
    val currency: String = "PKR",
    val wifiOnly: Boolean = true,
    val dbVersions: Map<String, Int> = emptyMap(),
    val syncEnabled: Boolean = true,
    val lastSyncAt: Long = 0,
    val syncAccountUid: String? = null,
    val preferencesUpdatedAt: Long = 0,
) {
    val language: Lang get() = Lang.of(lang)
}

class SettingsRepo(scope: CoroutineScope) {
    private val json = Json { ignoreUnknownKeys = true; encodeDefaults = true }
    private val key = stringPreferencesKey("settings")
    private val store: DataStore<Preferences> = PreferenceDataStoreFactory.createWithPath(produceFile = { "${Platform.filesDir()}/settings${CloudEnvironment.storageSuffix}.preferences_pb".toPath() })

    val flow: StateFlow<AppSettings> = store.data
        .map { p -> p[key]?.let { runCatching { json.decodeFromString(AppSettings.serializer(), it) }.getOrNull() } ?: AppSettings() }
        .stateIn(scope, SharingStarted.Eagerly, AppSettings())

    /** Current value (waits for the first read from disk). */
    suspend fun get(): AppSettings = store.data.first().let { p -> p[key]?.let { runCatching { json.decodeFromString(AppSettings.serializer(), it) }.getOrNull() } ?: AppSettings() }

    suspend fun update(block: (AppSettings) -> AppSettings) {
        store.edit { p ->
            val cur = p[key]?.let { runCatching { json.decodeFromString(AppSettings.serializer(), it) }.getOrNull() } ?: AppSettings()
            val next = block(cur)
            val stamped = if (CloudPreferences.from(next) != CloudPreferences.from(cur))
                next.copy(preferencesUpdatedAt = nowMs()) else next
            p[key] = json.encodeToString(AppSettings.serializer(), stamped)
        }
    }

    suspend fun restorePreferences(backup: PreferencesBackup) {
        store.edit { p ->
            val cur = p[key]?.let { json.decodeFromString(AppSettings.serializer(), it) } ?: AppSettings()
            val restored = backup.preferences.applyTo(cur).copy(preferencesUpdatedAt = backup.updatedAt)
            p[key] = json.encodeToString(AppSettings.serializer(), restored)
        }
    }
}
