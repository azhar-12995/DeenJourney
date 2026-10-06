package com.deenjourney.app.data.user

import com.deenjourney.app.data.settings.SettingsRepo
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.datetime.LocalDate
import kotlin.random.Random
import kotlin.time.Clock

fun nowMs(): Long = Clock.System.now().toEpochMilliseconds()
fun newId(): String = buildString { repeat(20) { append("abcdefghijklmnopqrstuvwxyz0123456789"[Random.nextInt(36)]) } }

/** Local, offline-first store of everything the family creates. [changes] pings the sync engine. */
@OptIn(ExperimentalCoroutinesApi::class)
class UserRepo(private val db: UserDb, private val settings: SettingsRepo, scope: CoroutineScope) {
    val dao = db.dao()
    private val counterMutex = Mutex()
    private val dailyMutex = Mutex()
    val changes = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
    private fun touched() { changes.tryEmit(Unit) }

    val profiles: StateFlow<List<ProfileE>> = dao.profiles().stateIn(scope, SharingStarted.Eagerly, emptyList())

    /** The profile currently using the app (falls back to the account owner). */
    val active: StateFlow<ProfileE?> = combine(profiles, settings.flow.map { it.activeProfile }.distinctUntilChanged()) { list, id ->
        list.firstOrNull { it.id == id } ?: list.firstOrNull { it.owner } ?: list.firstOrNull()
    }.stateIn(scope, SharingStarted.Eagerly, null)

    private val activeId: Flow<String> = active.map { it?.id ?: "" }.distinctUntilChanged()

    suspend fun setActive(id: String) = settings.update { it.copy(activeProfile = id) }

    suspend fun saveProfile(p: ProfileE): ProfileE { val x = p.copy(updatedAt = nowMs()); dao.upsertProfile(x); touched(); return x }
    suspend fun deleteProfile(p: ProfileE) { dao.upsertProfile(p.copy(deleted = true, updatedAt = nowMs())); touched() }

    /** Makes sure the signed-in user has an owner profile. */
    suspend fun ensureOwner(name: String): ProfileE {
        dao.profilesNow().firstOrNull { it.owner }?.let { return it }
        return saveProfile(ProfileE(id = newId(), name = name.ifBlank { "Me" }, kind = "parent", relation = "self", avatar = "man", ageGroup = "adult", owner = true, updatedAt = 0))
    }

    private suspend fun pid(): String = active.value?.id ?: ""

    // ---- saved items ----
    fun saved(): Flow<List<SavedE>> = activeId.flatMapLatest { dao.saved(it) }
    fun isSaved(type: String, key: String): Flow<Boolean> = activeId.flatMapLatest { p -> dao.savedOne("$p|$type|$key").map { it != null } }
    suspend fun toggleSaved(type: String, key: String, title: String, sub: String = "") {
        val p = pid(); val id = "$p|$type|$key"
        val now = nowMs()
        val cur = dao.savedById(id)
        dao.upsertSaved(SavedE(id, p, type, key, title, sub, createdAt = cur?.createdAt ?: now, updatedAt = now, deleted = cur != null && !cur.deleted))
        touched()
    }

    // ---- notes & highlights ----
    fun notes(): Flow<List<NoteE>> = activeId.flatMapLatest { dao.notes(it) }
    fun notesFor(key: String): Flow<List<NoteE>> = activeId.flatMapLatest { dao.notesFor(it, key) }
    suspend fun saveNote(ayahKey: String, text: String, tag: String, id: String? = null) {
        val now = nowMs()
        dao.upsertNote(NoteE(id ?: newId(), pid(), ayahKey, text, tag, createdAt = now, updatedAt = now)); touched()
    }
    suspend fun deleteNote(n: NoteE) { dao.upsertNote(n.copy(deleted = true, updatedAt = nowMs())); touched() }
    fun highlights(): Flow<List<HighlightE>> = activeId.flatMapLatest { dao.highlights(it) }
    suspend fun highlight(ayahKey: String, color: Int?) {
        val p = pid()
        dao.upsertHighlight(HighlightE("$p|$ayahKey", p, ayahKey, color ?: 0, nowMs(), deleted = color == null)); touched()
    }

    // ---- progress ----
    fun progress(kind: String): Flow<List<ProgressE>> = activeId.flatMapLatest { dao.progressOf(it, kind) }
    fun allProgress(): Flow<List<ProgressE>> = activeId.flatMapLatest { dao.progress(it) }
    fun progressFor(profileId: String): Flow<List<ProgressE>> = dao.progress(profileId)
    suspend fun setProgress(kind: String, key: String, value: Int, extra: String = "") {
        val p = pid()
        dao.upsertProgress(ProgressE("$p|$kind|$key", p, kind, key, value, extra, nowMs())); touched()
    }

    // ---- counters (tasbih) ----
    fun counters(): Flow<List<CounterE>> = activeId.flatMapLatest { dao.counters(it) }
    suspend fun incrementCounter(id: String, autoReset: Boolean) = counterMutex.withLock {
        val c = dao.counters(pid()).first().firstOrNull { it.id == id } ?: return@withLock
        saveCounter(c.copy(count = if (autoReset && c.count + 1 >= c.target) 0 else c.count + 1))
    }
    suspend fun saveCounter(c: CounterE) { dao.upsertCounter(c.copy(profileId = c.profileId.ifEmpty { pid() }, updatedAt = nowMs())); touched() }
    suspend fun ensureDefaultCounters() {
        val p = pid(); if (p.isEmpty()) return
        if (dao.counterCount(p) > 0) return
        listOf(
            Triple("SubhanAllah", "سُبْحَانَ ٱللَّهِ", 33), Triple("Alhamdulillah", "ٱلْحَمْدُ لِلَّهِ", 33),
            Triple("Allahu Akbar", "ٱللَّهُ أَكْبَرُ", 34), Triple("Astaghfirullah", "أَسْتَغْفِرُ ٱللَّهَ", 100),
        ).forEachIndexed { i, (l, a, t) -> dao.upsertCounter(CounterE("$p|c$i", p, l, a, t, 0, i, nowMs())) }
        touched()
    }

    // ---- daily logs ----
    fun daily(kind: String, from: LocalDate): Flow<List<DailyE>> = activeId.flatMapLatest { dao.daily(it, kind, from.toString()) }
    fun day(date: LocalDate): Flow<List<DailyE>> = activeId.flatMapLatest { dao.day(it, date.toString()) }
    fun between(from: LocalDate, to: LocalDate): Flow<List<DailyE>> = activeId.flatMapLatest { dao.between(it, from.toString(), to.toString()) }
    fun dailyFor(profileId: String, kind: String, from: LocalDate): Flow<List<DailyE>> = dao.daily(profileId, kind, from.toString())
    suspend fun setDaily(date: LocalDate, kind: String, value: Int, note: String = "") {
        val p = pid()
        dao.upsertDaily(DailyE("$p|$date|$kind", p, date.toString(), kind, value, note, nowMs())); touched()
    }
    suspend fun addDaily(date: LocalDate, kind: String, delta: Int) = dailyMutex.withLock {
        val p = pid(); val id = "$p|$date|$kind"
        val cur = dao.dailyById(id)?.takeIf { !it.deleted }?.value ?: 0
        dao.upsertDaily(DailyE(id, p, date.toString(), kind, cur + delta, "", nowMs())); touched()
    }

    // ---- inbox ----
    val inbox get() = dao.inbox()
    val unread get() = dao.unread()
    suspend fun postInbox(kind: String, title: String, body: String, link: String? = null, id: String = newId()) =
        dao.upsertInbox(InboxE(id, kind, title, body, nowMs(), link = link))

    // ---- zakat ----
    val zakat get() = dao.zakat()
    suspend fun saveZakat(z: ZakatE) { dao.upsertZakat(z.copy(updatedAt = nowMs())); touched() }

    suspend fun wipe() = dao.wipeAll()
}
