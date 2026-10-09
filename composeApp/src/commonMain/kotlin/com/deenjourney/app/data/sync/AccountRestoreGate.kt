package com.deenjourney.app.data.sync

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/** Shares one restore per auth session. Screen cancellation must not cancel account sync. */
internal class AccountRestoreGate(private val scope: CoroutineScope) {
    private class Attempt(val uid: String) {
        val ready = CompletableDeferred<Boolean>()
        val restored = MutableStateFlow(false)
        var worker: Job? = null
    }

    private val lock = Mutex()
    private var current: Attempt? = null

    suspend fun await(uid: String, restore: suspend (onRestored: () -> Unit) -> Boolean): Boolean {
        val attempt = lock.withLock {
            val previous = current
            if (previous?.uid == uid && (!previous.ready.isCompleted || previous.restored.value)) {
                previous
            } else {
                previous?.ready?.cancel()
                previous?.worker?.cancel()
                Attempt(uid).also { next ->
                    current = next
                    next.worker = scope.launch {
                        try {
                            val ok = restore {
                                next.restored.value = true
                                next.ready.complete(true)
                            }
                            if (ok) next.restored.value = true
                            next.ready.complete(ok)
                        } catch (e: CancellationException) {
                            next.ready.cancel(e)
                            throw e
                        } catch (e: Exception) {
                            next.ready.complete(false)
                        }
                    }
                }
            }
        }
        return attempt.ready.await()
    }

    suspend fun reset() = lock.withLock {
        current?.ready?.cancel()
        current?.worker?.cancel()
        current = null
    }
}
