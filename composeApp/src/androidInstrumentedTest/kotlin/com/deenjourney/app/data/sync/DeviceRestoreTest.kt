package com.deenjourney.app.data.sync

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.deenjourney.app.BuildConfig
import com.deenjourney.app.data.auth.AuthRepo
import com.deenjourney.app.data.settings.LastRead
import com.deenjourney.app.data.settings.SettingsRepo
import com.deenjourney.app.data.user.UserRepo
import dev.gitlive.firebase.firestore.Source
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.mp.KoinPlatform

/** Run only on a disposable Android emulator with the local Firebase emulator suite. */
@RunWith(AndroidJUnit4::class)
class DeviceRestoreTest {
    private val email = "restore-fixture@example.test"
    private val password = "LocalFixture123!"
    private val koin get() = KoinPlatform.getKoin()

    private fun connectEmulators() {
        check(BuildConfig.USE_EMULATORS) { "Run with -Pdj.useEmulators=true on a disposable Android emulator" }
    }

    @Test fun uploadFixture() = runBlocking {
        connectEmulators()
        val auth = koin.get<AuthRepo>()
        val users = koin.get<UserRepo>()
        val settings = koin.get<SettingsRepo>()
        val sync = koin.get<SyncRepo>()
        auth.signUp("Restore fixture", email, password).getOrThrow()
        assertTrue(sync.restoreForSignIn())
        assertTrue(sync.syncNow(restore = true))
        val owner = users.dao.profilesNow().single()
        users.active.first { it?.id == owner.id }
        users.toggleSaved("dua", "restore-fixture", "Fixture bookmark")
        users.saveNote("2:42", "Fixture note", "", "fixture-note")
        users.setProgress("lesson", "fixture", 75)
        settings.update { it.copy(setupDone = true, familyDone = true, lang = "ur", theme = "dark", lastRead = LastRead(2, 42, 1300)) }
        assertTrue(sync.syncNow())
        assertEquals(1, accountFirestore.collection("users").document(auth.user.value!!.uid).collection("saved").get(Source.SERVER).documents.size)
    }

    @Test fun sessionSurvivesAppRestart() = runBlocking {
        connectEmulators()
        val auth = koin.get<AuthRepo>()
        auth.ready.first { it }
        assertEquals(email, auth.user.value?.email)
        assertTrue(koin.get<SettingsRepo>().get().setupDone)
    }

    /** Invoke after adb pm clear: same cloud account, completely empty local app storage. */
    @Test fun restoreOnFreshInstallation() = runBlocking {
        connectEmulators()
        val auth = koin.get<AuthRepo>()
        val users = koin.get<UserRepo>()
        val settings = koin.get<SettingsRepo>()
        assertTrue(users.dao.profilesNow().isEmpty())
        assertFalse(settings.get().setupDone)
        auth.signIn(email, password).getOrThrow()
        assertTrue(koin.get<SyncRepo>().restoreForSignIn())
        val owner = users.dao.profilesNow().single()
        assertEquals("Restore fixture", owner.name)
        assertEquals("restore-fixture", users.dao.saved(owner.id).first().single().key)
        assertEquals("Fixture note", users.dao.noteById("fixture-note")?.text)
        assertEquals(75, users.dao.progressOf(owner.id, "lesson").first().single().value)
        val restored = settings.get()
        assertTrue(restored.setupDone)
        assertTrue(restored.familyDone)
        assertEquals(owner.id, restored.activeProfile)
        assertEquals("ur", restored.lang)
        assertEquals("dark", restored.theme)
        assertEquals(LastRead(2, 42, 1300), restored.lastRead)
        assertTrue(runCatching { accountFirestore.collection("users").document("another-account").collection("saved").get(Source.SERVER) }.isFailure)
    }
}
