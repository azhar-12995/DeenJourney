package com.deenjourney.app.data.sync

import com.deenjourney.app.data.auth.AuthRepo
import com.deenjourney.app.data.settings.SettingsRepo
import com.deenjourney.app.data.settings.CloudPreferences
import com.deenjourney.app.data.settings.PreferencesBackup
import com.deenjourney.app.core.Platform
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
import dev.gitlive.firebase.firestore.CollectionReference
import dev.gitlive.firebase.firestore.Source
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.delay
import kotlinx.coroutines.withTimeout
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

fun shouldRestoreRow(localUpdatedAt: Long?, remoteUpdatedAt: Long): Boolean =
    localUpdatedAt == null || localUpdatedAt < remoteUpdatedAt

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
    private var started = false
    private val dao = users.dao

    fun start() {
        if (started) return
        started = true
        scope.launch { auth.user.map { it?.uid }.distinctUntilChanged().collect { uid -> if (uid != null) syncNow(restore = true) } }
        scope.launch { users.changes.debounce(4000).collect { syncNow() } }
        scope.launch { settings.flow.map { it.preferencesUpdatedAt }.distinctUntilChanged().debounce(4000).collect { syncNow() } }
        scope.launch {
            while (true) {
                delay(30_000)
                val s = settings.get()
                if (_state.value == SyncState.Error || _state.value == SyncState.Offline || s.preferencesUpdatedAt > s.lastSyncAt)
                    syncNow()
            }
        }
    }

    private fun col(uid: String, name: String): CollectionReference = accountFirestore.collection("users").document(uid).collection(name)

    suspend fun syncNow(restore: Boolean = false): Boolean = lock.withLock {
        val uid = auth.user.value?.uid ?: return false
        val s = settings.get()
        if (!s.syncEnabled) return false
        _state.value = SyncState.Syncing
        val syncStartedAt = nowMs()
        val ok = try {
            withTimeout(25_000) {
                check(s.syncAccountUid == null || s.syncAccountUid == uid) { "Sign out before changing accounts" }
                val firstRestore = s.syncAccountUid == null || s.lastSyncAt == 0L
                val since = if (restore || firstRestore) 0L else s.lastSyncAt
                // Pull before creating a profile or uploading edits; retain newer local changes.
                pull(uid, "profile", ProfileE.serializer(), since) { r -> if (shouldRestoreRow(dao.profile(r.id)?.updatedAt, r.updatedAt)) dao.upsertProfile(r) }
                pull(uid, "saved", SavedE.serializer(), since) { r -> if (shouldRestoreRow(dao.savedById(r.id)?.updatedAt, r.updatedAt)) dao.upsertSaved(r) }
                pull(uid, "note", NoteE.serializer(), since) { r -> if (shouldRestoreRow(dao.noteById(r.id)?.updatedAt, r.updatedAt)) dao.upsertNote(r) }
                pull(uid, "highlight", HighlightE.serializer(), since) { r -> if (shouldRestoreRow(dao.highlightById(r.id)?.updatedAt, r.updatedAt)) dao.upsertHighlight(r) }
                pull(uid, "progress", ProgressE.serializer(), since) { r -> if (shouldRestoreRow(dao.progressById(r.id)?.updatedAt, r.updatedAt)) dao.upsertProgress(r) }
                pull(uid, "counter", CounterE.serializer(), since) { r -> if (shouldRestoreRow(dao.counterById(r.id)?.updatedAt, r.updatedAt)) dao.upsertCounter(r) }
                pull(uid, "daily", DailyE.serializer(), since) { r -> if (shouldRestoreRow(dao.dailyById(r.id)?.updatedAt, r.updatedAt)) dao.upsertDaily(r) }
                pull(uid, "zakat", ZakatE.serializer(), since) { r -> if (shouldRestoreRow(dao.zakatById(r.id)?.updatedAt, r.updatedAt)) dao.upsertZakat(r) }
                val prefsDoc = col(uid, "preferences").document("account")
                val remotePrefs = prefsDoc.get(Source.SERVER)
                check(auth.user.value?.uid == uid)
                val backup = if (remotePrefs.exists) remotePrefs.data(PreferencesBackup.serializer()) else null
                if (backup != null && (firstRestore || backup.updatedAt > settings.get().preferencesUpdatedAt))
                    settings.restorePreferences(backup)
                val accountName = auth.user.value?.name.orEmpty()
                var owner = users.ensureOwner(accountName)
                if (owner.name == "Me" && accountName.isNotBlank() && accountName != "Me")
                    owner = users.saveProfile(owner.copy(name = accountName))
                val restoredSettings = settings.get()
                if (restoredSettings.activeProfile == null || dao.profile(restoredSettings.activeProfile)?.deleted != false)
                    settings.update { it.copy(activeProfile = owner.id) }
                val pushSince = if (firstRestore) 0L else s.lastSyncAt
                push(uid, "profile", ProfileE.serializer(), dao.profilesSince(pushSince)) { it.id }
                push(uid, "saved", SavedE.serializer(), dao.savedSince(pushSince)) { it.id }
                push(uid, "note", NoteE.serializer(), dao.notesSince(pushSince)) { it.id }
                push(uid, "highlight", HighlightE.serializer(), dao.highlightsSince(pushSince)) { it.id }
                push(uid, "progress", ProgressE.serializer(), dao.progressSince(pushSince)) { it.id }
                push(uid, "counter", CounterE.serializer(), dao.countersSince(pushSince)) { it.id }
                push(uid, "daily", DailyE.serializer(), dao.dailySince(pushSince)) { it.id }
                push(uid, "zakat", ZakatE.serializer(), dao.zakatSince(pushSince)) { it.id }
                val current = settings.get()
                if (backup == null || current.preferencesUpdatedAt > backup.updatedAt)
                    prefsDoc.set(PreferencesBackup.serializer(), PreferencesBackup(CloudPreferences.from(current), current.preferencesUpdatedAt))
                check(auth.user.value?.uid == uid)
                settings.update { it.copy(lastSyncAt = syncStartedAt - 1000, syncAccountUid = uid) }
            }
            true
        } catch (e: kotlinx.coroutines.TimeoutCancellationException) {
            false
        } catch (e: CancellationException) {
            _state.value = SyncState.Idle
            throw e
        } catch (e: Exception) {
            false
        }
        _state.value = if (ok) SyncState.Done else if (!Platform.isOnline()) SyncState.Offline else SyncState.Error
        ok
    }
    private suspend fun <T : Any> push(uid: String, name: String, ser: KSerializer<T>, rows: List<T>, id: (T) -> String) {
        if (rows.isEmpty()) return
        val c = col(uid, name)
        rows.chunked(400).forEach { chunk ->
            val b = accountFirestore.batch()
            check(auth.user.value?.uid == uid)
            chunk.forEach { b.set(c.document(id(it).replace('/', '_')), ser, it, merge = false) }
            b.commit()
        }
    }

    private suspend fun <T> pull(uid: String, name: String, ser: KSerializer<T>, since: Long, apply: suspend (T) -> Unit) {
        val c = col(uid, name)
        val snap = if (since == 0L) c.get(Source.SERVER) else c.where { "updatedAt".greaterThan(since) }.get(Source.SERVER)
        for (d in snap.documents) { check(auth.user.value?.uid == uid); apply(d.data(ser)) }
    }

    /** Removes every document of the signed-in user (account deletion). */
    suspend fun deleteCloudData(): Boolean = lock.withLock {
        val uid = auth.user.value?.uid ?: return false
        runCatching {
            for (name in listOf("profile", "saved", "note", "highlight", "progress", "counter", "daily", "zakat", "preferences")) {
                val docs = col(uid, name).get().documents
                docs.chunked(400).forEach { chunk -> val b = accountFirestore.batch(); chunk.forEach { b.delete(it.reference) }; b.commit() }
            }
            accountFirestore.collection("users").document(uid).delete()
        }.isSuccess
    }
}
