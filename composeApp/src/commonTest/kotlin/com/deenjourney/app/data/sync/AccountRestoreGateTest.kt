package com.deenjourney.app.data.sync

import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
class AccountRestoreGateTest {
    @Test fun startupAndLoginShareOneRestoreAndDoNotWaitForUploads() = runTest {
        val gate = AccountRestoreGate(backgroundScope)
        var downloads = 0
        val allowUpload = CompletableDeferred<Unit>()
        var uploaded = false
        val restore: suspend (() -> Unit) -> Boolean = { ready ->
            downloads++
            delay(100)
            ready()
            allowUpload.await()
            uploaded = true
            true
        }
        val startup = async { gate.await("user", restore) }
        val login = async { gate.await("user", restore) }
        assertTrue(startup.await())
        assertTrue(login.await())
        assertEquals(1, downloads)
        assertFalse(uploaded)
        assertTrue(gate.await("user", restore))
        assertEquals(1, downloads)
        allowUpload.complete(Unit)
        runCurrent()
        assertTrue(uploaded)
    }

    @Test fun failedDownloadCanBeRetriedWithoutReauthentication() = runTest {
        val gate = AccountRestoreGate(backgroundScope)
        var attempts = 0
        val restore: suspend (() -> Unit) -> Boolean = { ready ->
            attempts++
            if (attempts == 1) false else { ready(); true }
        }
        assertFalse(gate.await("user", restore))
        assertTrue(gate.await("user", restore))
        assertEquals(2, attempts)
    }

    @Test fun cancellingLoginScreenDoesNotCancelBackgroundRestore() = runTest {
        val gate = AccountRestoreGate(backgroundScope)
        val downloaded = CompletableDeferred<Unit>()
        var attempts = 0
        val restore: suspend (() -> Unit) -> Boolean = { ready ->
            attempts++
            downloaded.await()
            ready()
            true
        }
        val screen = async { gate.await("user", restore) }
        runCurrent()
        screen.cancel()
        downloaded.complete(Unit)
        runCurrent()
        assertTrue(gate.await("user", restore))
        assertEquals(1, attempts)
    }

    @Test fun newSessionDoesNotReusePreviousAccountsRestore() = runTest {
        val gate = AccountRestoreGate(backgroundScope)
        var attempts = 0
        val restore: suspend (() -> Unit) -> Boolean = { ready -> attempts++; ready(); true }
        assertTrue(gate.await("first", restore))
        assertTrue(gate.await("second", restore))
        gate.reset()
        assertTrue(gate.await("second", restore))
        assertEquals(3, attempts)
    }

    @Test fun uploadFailureDoesNotUndoSuccessfulLocalRestore() = runTest {
        val gate = AccountRestoreGate(backgroundScope)
        var attempts = 0
        val restore: suspend (() -> Unit) -> Boolean = { ready -> attempts++; ready(); false }
        assertTrue(gate.await("user", restore))
        assertTrue(gate.await("user", restore))
        assertEquals(1, attempts)
    }

    @Test fun signingOutBeforeWorkerStartsReleasesTheWaitingScreen() = runTest {
        val gate = AccountRestoreGate(backgroundScope)
        val waiting = async(start = kotlinx.coroutines.CoroutineStart.UNDISPATCHED) {
            gate.await("old-user") { ready -> ready(); true }
        }
        gate.reset()
        runCurrent()
        assertTrue(waiting.isCancelled)
        assertTrue(gate.await("new-user") { ready -> ready(); true })
    }
}
