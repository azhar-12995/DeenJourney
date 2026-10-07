package com.deenjourney.app.data.sync

import com.deenjourney.app.data.settings.AppSettings
import com.deenjourney.app.data.settings.CloudPreferences
import com.deenjourney.app.data.settings.LastRead
import com.deenjourney.app.data.settings.PreferencesBackup
import com.deenjourney.app.data.settings.SavedLocation
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class AccountRestoreTest {
    @Test fun freshInstallationRestoresSetupAndReadingPosition() {
        val old = AppSettings(lang = "ur", theme = "dark", setupDone = true, familyDone = true,
            activeProfile = "restored-owner", dailyGoal = 25, lastRead = LastRead(2, 42, 1300))
        val json = Json { encodeDefaults = true }
        val encoded = json.encodeToString(PreferencesBackup.serializer(), PreferencesBackup(CloudPreferences.from(old), 1234))
        val restored = json.decodeFromString(PreferencesBackup.serializer(), encoded).preferences.applyTo(AppSettings())
        assertTrue(restored.setupDone)
        assertTrue(restored.familyDone)
        assertEquals("restored-owner", restored.activeProfile)
        assertEquals(old.lastRead, restored.lastRead)
        assertEquals(25, restored.dailyGoal)
        assertEquals("ur", restored.lang)
        assertEquals("dark", restored.theme)
    }

    @Test fun deviceSecretsAndSyncCursorNeverEnterCloudBackup() {
        val source = AppSettings(pinHash = "private-pin", location = SavedLocation("Private city", lat = 1.0, lng = 2.0, tz = "UTC"),
            notificationsAsked = true, syncAccountUid = "old-user", lastSyncAt = 999, dbVersions = mapOf("quran" to 3))
        val encoded = Json.encodeToString(CloudPreferences.serializer(), CloudPreferences.from(source))
        listOf("private-pin", "Private city", "old-user", "lastSyncAt", "dbVersions", "notificationsAsked").forEach {
            assertFalse(encoded.contains(it), "Device-only value leaked: $it")
        }
        val device = source.copy(pinHash = "new-device-pin", lastSyncAt = 777)
        val restored = CloudPreferences(theme = "dark").applyTo(device)
        assertEquals(device.pinHash, restored.pinHash)
        assertEquals(device.location, restored.location)
        assertEquals(777L, restored.lastSyncAt)
        assertTrue(restored.notificationsAsked)
    }

    @Test fun newerOfflineEditsSurviveRestoreAndNewRowsAreRestored() {
        assertTrue(shouldRestoreRow(null, 10))
        assertTrue(shouldRestoreRow(10, 20))
        assertFalse(shouldRestoreRow(20, 10))
        assertFalse(shouldRestoreRow(20, 20))
    }
}
