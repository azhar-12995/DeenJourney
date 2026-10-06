package com.deenjourney.app.data.user

import androidx.room.ConstructedBy
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.RoomDatabase
import androidx.room.RoomDatabaseConstructor
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow
import kotlinx.serialization.Serializable

/** Family member under the signed-in account. kind: parent | adult | senior | child. */
@Serializable
@Entity(tableName = "profile")
data class ProfileE(
    @PrimaryKey val id: String, val name: String, val kind: String, val relation: String = "", val avatar: String = "man",
    val ageGroup: String = "adult", val level: String = "beginner", val childMode: Boolean = false, val largeText: Boolean = false,
    val goal: Int = 10, val focus: String = "", val owner: Boolean = false,
    val updatedAt: Long, val deleted: Boolean = false,
)

/** Anything the user saved: ayah | hadith | dua | lesson | name | story. */
@Serializable
@Entity(tableName = "saved")
data class SavedE(
    @PrimaryKey val id: String, val profileId: String, val type: String, val key: String, val title: String, val sub: String = "",
    val createdAt: Long, val updatedAt: Long, val deleted: Boolean = false,
)

@Serializable
@Entity(tableName = "note")
data class NoteE(
    @PrimaryKey val id: String, val profileId: String, val ayahKey: String, val text: String, val tag: String = "",
    val createdAt: Long, val updatedAt: Long, val deleted: Boolean = false,
)

@Serializable
@Entity(tableName = "highlight")
data class HighlightE(@PrimaryKey val id: String, val profileId: String, val ayahKey: String, val color: Int, val updatedAt: Long, val deleted: Boolean = false)

/** Learning progress: kind = lesson | name | story | hifz | qaida | track; value = score / status. */
@Serializable
@Entity(tableName = "progress")
data class ProgressE(@PrimaryKey val id: String, val profileId: String, val kind: String, val key: String, val value: Int, val extra: String = "", val updatedAt: Long, val deleted: Boolean = false)

@Serializable
@Entity(tableName = "counter")
data class CounterE(
    @PrimaryKey val id: String, val profileId: String, val label: String, val arabic: String, val target: Int, val count: Int,
    val sort: Int = 0, val updatedAt: Long, val deleted: Boolean = false,
)

/**
 * One value per profile per day per kind: tasbih | adhkar_m | adhkar_e | minutes | quran_pages | fast | taraweeh | sadaqah | deed | lesson.
 * fast: 1 kept, 2 missed, 3 exempt.
 */
@Serializable
@Entity(tableName = "daily")
data class DailyE(@PrimaryKey val id: String, val profileId: String, val date: String, val kind: String, val value: Int, val note: String = "", val updatedAt: Long, val deleted: Boolean = false)

@Serializable
@Entity(tableName = "inbox")
data class InboxE(@PrimaryKey val id: String, val kind: String, val title: String, val body: String, val at: Long, val read: Boolean = false, val link: String? = null)

@Serializable
@Entity(tableName = "zakat")
data class ZakatE(
    @PrimaryKey val id: String, val createdAt: Long, val currency: String, val assets: String, val liabilities: Long, val nisab: Long,
    val total: Long, val zakat: Long, val paid: Boolean = false, val updatedAt: Long, val deleted: Boolean = false,
)

@Dao
interface UserDao {
    // profiles
    @Query("SELECT * FROM profile WHERE deleted = 0 ORDER BY owner DESC, updatedAt ASC") fun profiles(): Flow<List<ProfileE>>
    @Query("SELECT * FROM profile WHERE deleted = 0 ORDER BY owner DESC, updatedAt ASC") suspend fun profilesNow(): List<ProfileE>
    @Query("SELECT * FROM profile WHERE id = :id") suspend fun profile(id: String): ProfileE?
    @Upsert suspend fun upsertProfile(p: ProfileE)

    // saved
    @Query("SELECT * FROM saved WHERE profileId = :p AND deleted = 0 ORDER BY createdAt DESC") fun saved(p: String): Flow<List<SavedE>>
    @Query("SELECT * FROM saved WHERE id = :id AND deleted = 0") fun savedOne(id: String): Flow<SavedE?>
    @Query("SELECT * FROM saved WHERE id = :id") suspend fun savedById(id: String): SavedE?
    @Upsert suspend fun upsertSaved(s: SavedE)

    // notes
    @Query("SELECT * FROM note WHERE profileId = :p AND deleted = 0 ORDER BY updatedAt DESC") fun notes(p: String): Flow<List<NoteE>>
    @Query("SELECT * FROM note WHERE profileId = :p AND ayahKey = :k AND deleted = 0 ORDER BY updatedAt DESC") fun notesFor(p: String, k: String): Flow<List<NoteE>>
    @Upsert suspend fun upsertNote(n: NoteE)

    // highlights
    @Query("SELECT * FROM highlight WHERE profileId = :p AND deleted = 0") fun highlights(p: String): Flow<List<HighlightE>>
    @Upsert suspend fun upsertHighlight(h: HighlightE)

    // progress
    @Query("SELECT * FROM progress WHERE profileId = :p AND deleted = 0") fun progress(p: String): Flow<List<ProgressE>>
    @Query("SELECT * FROM progress WHERE profileId = :p AND kind = :kind AND deleted = 0") fun progressOf(p: String, kind: String): Flow<List<ProgressE>>
    @Upsert suspend fun upsertProgress(p: ProgressE)

    // counters
    @Query("SELECT * FROM counter WHERE profileId = :p AND deleted = 0 ORDER BY sort, updatedAt") fun counters(p: String): Flow<List<CounterE>>
    @Query("SELECT COUNT(*) FROM counter WHERE profileId = :p") suspend fun counterCount(p: String): Int
    @Upsert suspend fun upsertCounter(c: CounterE)

    // daily logs
    @Query("SELECT * FROM daily WHERE profileId = :p AND kind = :kind AND date >= :from AND deleted = 0 ORDER BY date") fun daily(p: String, kind: String, from: String): Flow<List<DailyE>>
    @Query("SELECT * FROM daily WHERE profileId = :p AND date = :date AND deleted = 0") fun day(p: String, date: String): Flow<List<DailyE>>
    @Query("SELECT * FROM daily WHERE profileId = :p AND date BETWEEN :from AND :to AND deleted = 0") fun between(p: String, from: String, to: String): Flow<List<DailyE>>
    @Query("SELECT * FROM daily WHERE id = :id") suspend fun dailyById(id: String): DailyE?
    @Upsert suspend fun upsertDaily(d: DailyE)

    // inbox
    @Query("SELECT * FROM inbox ORDER BY at DESC LIMIT 200") fun inbox(): Flow<List<InboxE>>
    @Query("SELECT COUNT(*) FROM inbox WHERE read = 0") fun unread(): Flow<Int>
    @Upsert suspend fun upsertInbox(i: InboxE)
    @Query("UPDATE inbox SET read = 1") suspend fun markAllRead()
    @Query("DELETE FROM inbox WHERE id = :id") suspend fun deleteInbox(id: String)

    // zakat
    @Query("SELECT * FROM zakat WHERE deleted = 0 ORDER BY createdAt DESC") fun zakat(): Flow<List<ZakatE>>
    @Upsert suspend fun upsertZakat(z: ZakatE)

    // sync helpers
    @Query("SELECT * FROM profile WHERE updatedAt > :since") suspend fun profilesSince(since: Long): List<ProfileE>
    @Query("SELECT * FROM saved WHERE updatedAt > :since") suspend fun savedSince(since: Long): List<SavedE>
    @Query("SELECT * FROM note WHERE updatedAt > :since") suspend fun notesSince(since: Long): List<NoteE>
    @Query("SELECT * FROM highlight WHERE updatedAt > :since") suspend fun highlightsSince(since: Long): List<HighlightE>
    @Query("SELECT * FROM progress WHERE updatedAt > :since") suspend fun progressSince(since: Long): List<ProgressE>
    @Query("SELECT * FROM counter WHERE updatedAt > :since") suspend fun countersSince(since: Long): List<CounterE>
    @Query("SELECT * FROM daily WHERE updatedAt > :since") suspend fun dailySince(since: Long): List<DailyE>
    @Query("SELECT * FROM zakat WHERE updatedAt > :since") suspend fun zakatSince(since: Long): List<ZakatE>

    @Query("DELETE FROM profile") suspend fun wipeProfiles()
    @Query("DELETE FROM saved") suspend fun wipeSaved()
    @Query("DELETE FROM note") suspend fun wipeNotes()
    @Query("DELETE FROM highlight") suspend fun wipeHighlights()
    @Query("DELETE FROM progress") suspend fun wipeProgress()
    @Query("DELETE FROM counter") suspend fun wipeCounters()
    @Query("DELETE FROM daily") suspend fun wipeDaily()
    @Query("DELETE FROM inbox") suspend fun wipeInbox()
    @Query("DELETE FROM zakat") suspend fun wipeZakat()
}

@Database(
    entities = [ProfileE::class, SavedE::class, NoteE::class, HighlightE::class, ProgressE::class, CounterE::class, DailyE::class, InboxE::class, ZakatE::class],
    version = 1,
)
@ConstructedBy(UserDbCtor::class)
abstract class UserDb : RoomDatabase() {
    abstract fun dao(): UserDao
}

@Suppress("NO_ACTUAL_FOR_EXPECT", "KotlinNoActualForExpect")
expect object UserDbCtor : RoomDatabaseConstructor<UserDb> {
    override fun initialize(): UserDb
}

expect fun userDbBuilder(): RoomDatabase.Builder<UserDb>

suspend fun UserDao.wipeAll() {
    wipeProfiles(); wipeSaved(); wipeNotes(); wipeHighlights(); wipeProgress(); wipeCounters(); wipeDaily(); wipeInbox(); wipeZakat()
}
