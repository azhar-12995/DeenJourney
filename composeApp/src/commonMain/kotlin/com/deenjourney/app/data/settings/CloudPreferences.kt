package com.deenjourney.app.data.settings

import com.deenjourney.app.data.prayer.PrayerConfig
import kotlinx.serialization.Serializable

/** Account preferences only: GPS, permissions, PIN, downloads and sync cursors stay on the device. */
@Serializable
data class CloudPreferences(
    val lang: String? = null, val theme: String = "system", val textScale: Float = 1f,
    val prayer: PrayerConfig = PrayerConfig(), val methodChosen: Boolean = false,
    val hijriOffset: Int = 0, val fiqh: String = "hanafi",
    val quran: QuranPrefs = QuranPrefs(), val lastRead: LastRead? = null,
    val activeProfile: String? = null, val setupDone: Boolean = false, val familyDone: Boolean = false,
    val dailyGoal: Int = 10, val focus: List<String> = listOf("quran", "character"),
    val nisab: String = "silver", val currency: String = "PKR",
    val tasbihHaptic: Boolean = true, val tasbihSound: Boolean = false, val tasbihAutoReset: Boolean = false,
    val adhan: Map<String, AdhanPref> = AppSettings().adhan,
    val reminderBefore: Int = 0, val jumuahReminder: Boolean = true,
    val lessonReminder: Boolean = true, val lessonReminderTime: String = "20:00",
    val adhkarReminder: Boolean = false,
    val qaidaAudioEnabled: Boolean = true,
) {
    fun applyTo(local: AppSettings): AppSettings = local.copy(
        lang = lang, theme = theme, textScale = textScale, prayer = prayer, methodChosen = methodChosen,
        hijriOffset = hijriOffset, fiqh = fiqh, quran = quran, lastRead = lastRead,
        activeProfile = activeProfile, setupDone = setupDone, familyDone = familyDone,
        dailyGoal = dailyGoal, focus = focus, nisab = nisab, currency = currency,
        tasbihHaptic = tasbihHaptic, tasbihSound = tasbihSound, tasbihAutoReset = tasbihAutoReset,
        adhan = adhan, reminderBefore = reminderBefore, jumuahReminder = jumuahReminder,
        lessonReminder = lessonReminder, lessonReminderTime = lessonReminderTime, adhkarReminder = adhkarReminder,
        qaidaAudioEnabled = qaidaAudioEnabled,
    )

    companion object {
        fun from(s: AppSettings) = CloudPreferences(
            s.lang, s.theme, s.textScale, s.prayer, s.methodChosen, s.hijriOffset, s.fiqh,
            s.quran, s.lastRead, s.activeProfile, s.setupDone, s.familyDone, s.dailyGoal, s.focus,
            s.nisab, s.currency, s.tasbihHaptic, s.tasbihSound, s.tasbihAutoReset,
            s.adhan, s.reminderBefore, s.jumuahReminder, s.lessonReminder, s.lessonReminderTime, s.adhkarReminder,
            s.qaidaAudioEnabled,
        )
    }
}

@Serializable
data class PreferencesBackup(val preferences: CloudPreferences, val updatedAt: Long)
