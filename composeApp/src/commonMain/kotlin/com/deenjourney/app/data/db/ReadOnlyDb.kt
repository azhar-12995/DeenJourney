package com.deenjourney.app.data.db

import androidx.sqlite.SQLiteConnection
import androidx.sqlite.SQLiteStatement
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import androidx.sqlite.driver.bundled.SQLITE_OPEN_READONLY
import com.deenjourney.app.core.Platform
import com.deenjourney.app.data.settings.SettingsRepo
import com.deenjourney.app.res.Res
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import org.jetbrains.compose.resources.ExperimentalResourceApi

/** Bundled read-only databases and their content versions (bump when the file in composeResources changes). */
enum class BundledDb(val file: String, val version: Int) {
    QURAN("quran.db", 1),
    CITIES("cities.db", 1),
}

/** Copies bundled DBs out of the app resources on first launch / after an update. */
class DbInstaller(private val settings: SettingsRepo) {
    private val lock = Mutex()

    fun path(db: BundledDb): String = "${Platform.filesDir()}/db/${db.file}"

    @OptIn(ExperimentalResourceApi::class)
    suspend fun ensure(db: BundledDb): String = lock.withLock {
        val p = path(db)
        val have = settings.get().dbVersions[db.name]
        if (!Platform.fileExists(p) || have != db.version) {
            withContext(Dispatchers.IO) {
                val bytes = Res.readBytes("files/db/${db.file}")
                Platform.writeFile(p, bytes)
            }
            settings.update { it.copy(dbVersions = it.dbVersions + (db.name to db.version)) }
        }
        p
    }
}

/** Thin wrapper over a single read-only SQLite connection (bundled SQLite, same on Android and iOS). */
class ReadOnlyDb(private val open: suspend () -> String) {
    private var conn: SQLiteConnection? = null
    private val lock = Mutex()

    suspend fun <T> query(sql: String, vararg args: Any?, map: (SQLiteStatement) -> T): List<T> = withContext(Dispatchers.IO) {
        lock.withLock {
            val c = conn ?: BundledSQLiteDriver().open(open(), SQLITE_OPEN_READONLY).also { conn = it }
            val st = c.prepare(sql)
            try {
                args.forEachIndexed { i, a ->
                    when (a) {
                        null -> st.bindNull(i + 1)
                        is Int -> st.bindLong(i + 1, a.toLong())
                        is Long -> st.bindLong(i + 1, a)
                        is Double -> st.bindDouble(i + 1, a)
                        is Float -> st.bindDouble(i + 1, a.toDouble())
                        is Boolean -> st.bindLong(i + 1, if (a) 1 else 0)
                        else -> st.bindText(i + 1, a.toString())
                    }
                }
                val out = ArrayList<T>()
                while (st.step()) out.add(map(st))
                out
            } finally {
                st.close()
            }
        }
    }

    suspend fun <T> one(sql: String, vararg args: Any?, map: (SQLiteStatement) -> T): T? = query(sql, *args, map = map).firstOrNull()
}

fun SQLiteStatement.str(i: Int): String = if (isNull(i)) "" else getText(i)
fun SQLiteStatement.strOrNull(i: Int): String? = if (isNull(i)) null else getText(i)
fun SQLiteStatement.int(i: Int): Int = getLong(i).toInt()
