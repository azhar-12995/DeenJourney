package com.deenjourney.app.data.quran

import com.deenjourney.app.data.settings.AppSettings
import com.deenjourney.app.data.settings.CloudPreferences
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

class QaidaAudioTest {
    @Test fun similarLookingLettersHaveDistinctArabicNames() {
        assertEquals(29, qaidaLetters.size)
        assertEquals(29, qaidaLetters.map { it.glyph }.toSet().size)
        fun voice(glyph: String) = qaidaLetters.single { it.glyph == glyph }.spokenArabic
        assertNotEquals(voice("ح"), voice("ه"))
        assertNotEquals(voice("ت"), voice("ط"))
        assertNotEquals(voice("ذ"), voice("ظ"))
        assertTrue(qaidaLetters.all { it.spokenArabic.isNotBlank() && it.spokenArabic.none { c -> c in 'A'..'Z' || c in 'a'..'z' } })
    }

    @Test fun mutedQaidaRestoresMutedOnAnotherInstallation() {
        val muted = CloudPreferences.from(AppSettings(qaidaAudioEnabled = false))
        val restored = Json.decodeFromString(CloudPreferences.serializer(), Json.encodeToString(CloudPreferences.serializer(), muted))
            .applyTo(AppSettings())
        assertFalse(restored.qaidaAudioEnabled)
        // Old backups without this new setting remain readable.
        assertTrue(Json.decodeFromString(CloudPreferences.serializer(), "{}").qaidaAudioEnabled)
    }
}
