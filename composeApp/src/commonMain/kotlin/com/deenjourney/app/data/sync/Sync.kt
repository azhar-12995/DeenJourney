package com.deenjourney.app.data.sync

import com.deenjourney.app.data.auth.AuthRepo
import com.deenjourney.app.data.settings.SettingsRepo
import com.deenjourney.app.data.user.CounterE
import com.deenjourney.app.data.user.DailyE
import com.deenjourney.app.data.user.HighlightE
import com.deenjourney.app.data.user.NoteE
import com.deenjourney.app.data.user.ProfileE
import com.deenjourney.app.data.user.ProgressE
import com.deenjourney.app.data.user.SavedE
import com.deenjourney.app.data.user.UserRepo
import com.deenjourney.app.data.user.ZakatE
import com.deenjourney.app.data.user.nowMs
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.firestore.CollectionReference
import dev.gitlive.firebase.firestore.firestore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.KSerializer

enum class SyncState { Idle, Syncing, Done, Offline, Error }

/**
 * Offline-first sync: Room is the source of truth; every row carries updatedAt + deleted.
 * Push = rows changed since the last push; pull = cloud docs changed since the last pull; last write wins.
 * Layout: users/{uid}/{table}/{rowId}. Firestore rules restrict each user to their own subtree.
 */
@OptIn(FlowPreview::class)
class SyncRepo(private val users: UserRepo, private val auth: AuthRepo, private val settings: SettingsRepo, private val scope: CoroutineScope) {
    private val lock = Mutex()
    private val _state = MutableStateFlow(SyncState.Idle)
    val state: StateFlow<SyncState> = _state
    private val dao = users.dao

    fun start() {
        scope.launch { auth.user.map { it?.uid }.distinctUntilChanged().collect { uid -> if (uid != null) syncNow() } }
        scope.launch { users.changes.debounce(4000).collect { syncNow() } }
    }

    private fun col(uid: String, name: String): CollectionReference = Firebase.firestore.collection("users").document(uid).collection(name)

    suspend fun syncNow(): Boolean = lock.withLock {
        val uid = auth.user.value?.uid ?: return false
        val s = settings.get()
        if (!s.syncEnabled) return false
        _state.value = SyncState.Syncing
        val started = nowMs()
        val ok = runCatching {
            val since = s.lastSyncAt
            push(uid, "profile", ProfileE.serializer(), dao.profilesSince(since)) { it.id }
            push(uid, "saved", SavedE.serializer(), dao.savedSince(since)) { it.id }
            push(uid, "note", NoteE.serializer(), dao.notesSince(since)) { it.id }
            push(uid, "highlight", HighlightE.serializer(), dao.highlightsSince(since)) { it.id }
            push(uid, "progress", ProgressE.serializer(), dao.progressSince(since)) { it.id }
            push(uid, "counter", CounterE.serializer(), dao.countersSince(since)) { it.id }
            push(uid, "daily", DailyE.serializer(), dao.dailySince(since)) { it.id }
            push(uid, "zakat", ZakatE.serializer(), dao.zakatSince(since)) { it.id }
            pull(uid, "profile", ProfileE.serializer(), since) { r -> val l = dao.profile(r.id); if (l == null || l.updatedAt < r.updatedAt) dao.upsertProfile(r) }
            pull(uid, "saved", SavedE.serializer(), since) { r -> val l = dao.savedById(r.id); if (l == null || l.updatedAt < r.updatedAt) dao.upsertSaved(r) }
            pull(uid, "note", NoteE.serializer(), since) { dao.upsertNote(it) }
            pull(uid, "highlight", HighlightE.serializer(), since) { dao.upsertHighlight(it) }
            pull(uid, "progress", ProgressE.serializer(), since) { dao.upsertProgress(it) }
            pull(uid, "counter", CounterE.serializer(), since) { dao.upsertCounter(it) }
            pull(uid, "daily", DailyE.serializer(), since) { r -> val l = dao.dailyById(r.id); if (l == null || l.updatedAt < r.updatedAt) dao.upsertDaily(r) }
            pull(uid, "zakat", ZakatE.serializer(), since) { dao.upsertZakat(it) }
        }.isSuccess
        if (ok) settings.update { it.copy(lastSyncAt = started - 1000) }
        _state.value = if (ok) SyncState.Done else SyncState.Error
        ok
    }

    private suspend fun <T : Any> push(uid: String, name: String, ser: KSerializer<T>, rows: List<T>, id: (T) -> String) {
        if (rows.isEmpty()) return
        val c = col(uid, name)
        rows.chunked(400).forEach { chunk ->
            val b = Firebase.firestore.batch()
            chunk.forEach { b.set(c.document(id(it).replace('/', '_')), ser, it, merge = false) }
            b.commit()
        }
    }

    private suspend fun <T> pull(uid: String, name: String, ser: KSerializer<T>, since: Long, apply: suspend (T) -> Unit) {
        val snap = col(uid, name).where { "updatedAt".greaterThan(since) }.get()
        for (d in snap.documents) runCatching { apply(d.data(ser)) }
    }

    /** Removes every document of the signed-in user (account deletion). */
    suspend fun deleteCloudData(): Boolean {
        val uid = auth.user.value?.uid ?: return false
        return runCatching {
            for (name in listOf("profile", "saved", "note", "highlight", "progress", "counter", "daily", "zakat")) {
                val docs = col(uid, name).get().documents
                docs.chunked(400).forEach { chunk -> val b = Firebase.firestore.batch(); chunk.forEach { b.delete(it.reference) }; b.commit() }
            }
            Firebase.firestore.collection("users").document(uid).delete()
        }.isSuccess
    }
}
