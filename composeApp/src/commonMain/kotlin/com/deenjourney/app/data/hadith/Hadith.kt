package com.deenjourney.app.data.hadith

import androidx.sqlite.SQLiteConnection
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import androidx.sqlite.driver.bundled.SQLITE_OPEN_CREATE
import androidx.sqlite.driver.bundled.SQLITE_OPEN_READWRITE
import androidx.sqlite.execSQL
import com.deenjourney.app.core.L
import com.deenjourney.app.core.Platform
import com.deenjourney.app.data.net.AppJson
import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText
import io.ktor.http.isSuccess
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.int
import kotlinx.serialization.json.intOrNull
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

data class Collection(val id: String, val name: L, val arabic: String, val books: Int, val sizeMb: Float, val graded: Boolean)

data class HadithBook(val n: Int, val name: String, val first: Int, val last: Int)

data class HadithItem(val collection: String, val number: Int, val book: Int, val ar: String, val en: String, val ur: String, val grade: String) {
    val key: String get() = "$collection:$number"
}

/** The six books (+ Muwatta Malik), Arabic + English + Urdu from fawazahmed0/hadith-api (jsDelivr, version-pinned). */
object Collections {
    val all = listOf(
        Collection("bukhari", L("Sahih al-Bukhari", "صحیح بخاری", "صحيح البخاري"), "صحيح البخاري", 97, 22f, false),
        Collection("muslim", L("Sahih Muslim", "صحیح مسلم", "صحيح مسلم"), "صحيح مسلم", 56, 20f, false),
        Collection("abudawud", L("Sunan Abi Dawud", "سنن ابی داؤد", "سنن أبي داود"), "سنن أبي داود", 43, 12f, true),
        Collection("tirmidhi", L("Jami‘ at-Tirmidhi", "جامع ترمذی", "جامع الترمذي"), "جامع الترمذي", 49, 10f, true),
        Collection("nasai", L("Sunan an-Nasa’i", "سنن نسائی", "سنن النسائي"), "سنن النسائي", 51, 12f, true),
        Collection("ibnmajah", L("Sunan Ibn Majah", "سنن ابن ماجہ", "سنن ابن ماجه"), "سنن ابن ماجه", 37, 8f, true),
        Collection("malik", L("Muwatta Malik", "موطا امام مالک", "موطأ مالك"), "موطأ مالك", 61, 4f, true),
    )
    fun of(id: String) = all.firstOrNull { it.id == id }
}

/** Local hadith store (downloaded collections), searchable offline. */
class HadithStore(private val http: HttpClient) {
    private val base = "https://cdn.jsdelivr.net/gh/fawazahmed0/hadith-api@1/editions"
    private val lock = Mutex()
    private var conn: SQLiteConnection? = null
    private val _progress = MutableStateFlow<Map<String, Float>>(emptyMap())
    /** Download progress per collection (0..1). */
    val progress: StateFlow<Map<String, Float>> = _progress

    private fun open(): SQLiteConnection = conn ?: BundledSQLiteDriver().open("${Platform.filesDir()}/db/hadith.db", SQLITE_OPEN_READWRITE or SQLITE_OPEN_CREATE).also {
        it.execSQL("CREATE TABLE IF NOT EXISTS hadith (collection TEXT NOT NULL, number INTEGER NOT NULL, book INTEGER NOT NULL, ar TEXT NOT NULL, en TEXT NOT NULL, ur TEXT NOT NULL, grade TEXT NOT NULL, PRIMARY KEY(collection, number))")
        it.execSQL("CREATE TABLE IF NOT EXISTS book (collection TEXT NOT NULL, n INTEGER NOT NULL, name TEXT NOT NULL, first INTEGER NOT NULL, last INTEGER NOT NULL, PRIMARY KEY(collection, n))")
        it.execSQL("CREATE TABLE IF NOT EXISTS installed (collection TEXT PRIMARY KEY, at INTEGER NOT NULL)")
        conn = it
    }

    private suspend fun <T> db(block: (SQLiteConnection) -> T): T = withContext(Dispatchers.IO) { lock.withLock { block(open()) } }

    suspend fun installed(): Set<String> = db { c -> c.prepare("SELECT collection FROM installed").use { st -> buildSet { while (st.step()) add(st.getText(0)) } } }

    suspend fun books(collection: String): List<HadithBook> = db { c ->
        c.prepare("SELECT n,name,first,last FROM book WHERE collection=? ORDER BY n").use { st ->
            st.bindText(1, collection); buildList { while (st.step()) add(HadithBook(st.getLong(0).toInt(), st.getText(1), st.getLong(2).toInt(), st.getLong(3).toInt())) }
        }
    }

    private fun row(st: androidx.sqlite.SQLiteStatement) = HadithItem(st.getText(0), st.getLong(1).toInt(), st.getLong(2).toInt(), st.getText(3), st.getText(4), st.getText(5), st.getText(6))

    suspend fun inBook(collection: String, book: Int): List<HadithItem> = db { c ->
        c.prepare("SELECT collection,number,book,ar,en,ur,grade FROM hadith WHERE collection=? AND book=? ORDER BY number").use { st ->
            st.bindText(1, collection); st.bindLong(2, book.toLong()); buildList { while (st.step()) add(row(st)) }
        }
    }

    suspend fun get(collection: String, number: Int): HadithItem? = db { c ->
        c.prepare("SELECT collection,number,book,ar,en,ur,grade FROM hadith WHERE collection=? AND number=?").use { st ->
            st.bindText(1, collection); st.bindLong(2, number.toLong()); if (st.step()) row(st) else null
        }
    }

    suspend fun search(q: String, limit: Int = 100): List<HadithItem> = if (q.trim().length < 2) emptyList() else db { c ->
        c.prepare("SELECT collection,number,book,ar,en,ur,grade FROM hadith WHERE en LIKE ? OR ur LIKE ? OR ar LIKE ? LIMIT ?").use { st ->
            val like = "%${q.trim()}%"
            st.bindText(1, like); st.bindText(2, like); st.bindText(3, like); st.bindLong(4, limit.toLong()); buildList { while (st.step()) add(row(st)) }
        }
    }

    /** Fetches Arabic, English and Urdu editions of [collection] and stores them. */
    suspend fun install(collection: String): Boolean {
        _progress.value = _progress.value + (collection to 0.02f)
        val ok = runCatching {
            suspend fun edition(lang: String): JsonObject? {
                for (suffix in listOf(".min.json", ".json")) {
                    val r = http.get("$base/$lang-$collection$suffix")
                    if (r.status.isSuccess()) return AppJson.parseToJsonElement(r.bodyAsText()).jsonObject
                }
                return null
            }
            val ara = edition("ara") ?: error("no arabic"); _progress.value = _progress.value + (collection to 0.35f)
            val eng = edition("eng") ?: error("no english"); _progress.value = _progress.value + (collection to 0.65f)
            val urd = edition("urd"); _progress.value = _progress.value + (collection to 0.9f)
            fun texts(o: JsonObject?): Map<Int, Pair<String, Int>> = o?.get("hadiths")?.jsonArray?.associate {
                val h = it.jsonObject
                h["hadithnumber"]!!.jsonPrimitive.content.toDouble().toInt() to (h["text"]?.jsonPrimitive?.contentOrNull.orEmpty() to (h["reference"]?.jsonObject?.get("book")?.jsonPrimitive?.intOrNull ?: 0))
            } ?: emptyMap()
            val en = texts(eng); val ur = texts(urd)
            val grades: Map<Int, String> = eng["hadiths"]!!.jsonArray.associate { it ->
                val h = it.jsonObject
                h["hadithnumber"]!!.jsonPrimitive.content.toDouble().toInt() to (h["grades"]?.jsonArray?.firstOrNull()?.jsonObject?.get("grade")?.jsonPrimitive?.contentOrNull ?: if (collection in setOf("bukhari", "muslim")) "Sahih" else "")
            }
            val meta = eng["metadata"]?.jsonObject
            val sections = meta?.get("sections")?.jsonObject
            val details = meta?.get("section_details")?.jsonObject
            db { c ->
                c.execSQL("BEGIN")
                try {
                    c.prepare("DELETE FROM hadith WHERE collection=?").use { it.bindText(1, collection); it.step() }
                    c.prepare("DELETE FROM book WHERE collection=?").use { it.bindText(1, collection); it.step() }
                    c.prepare("INSERT OR REPLACE INTO hadith VALUES (?,?,?,?,?,?,?)").use { st ->
                        for (h in ara["hadiths"]!!.jsonArray) {
                            val o = h.jsonObject
                            val n = o["hadithnumber"]!!.jsonPrimitive.content.toDouble().toInt()
                            val book = o["reference"]?.jsonObject?.get("book")?.jsonPrimitive?.intOrNull ?: en[n]?.second ?: 0
                            st.bindText(1, collection); st.bindLong(2, n.toLong()); st.bindLong(3, book.toLong())
                            st.bindText(4, o["text"]?.jsonPrimitive?.contentOrNull.orEmpty()); st.bindText(5, en[n]?.first.orEmpty()); st.bindText(6, ur[n]?.first.orEmpty())
                            st.bindText(7, grades[n].orEmpty())
                            st.step(); st.reset()
                        }
                    }
                    c.prepare("INSERT OR REPLACE INTO book VALUES (?,?,?,?,?)").use { st ->
                        sections?.forEach { (k, v) ->
                            val n = k.toIntOrNull() ?: return@forEach
                            val name = v.jsonPrimitive.contentOrNull.orEmpty()
                            if (n == 0 || name.isBlank()) return@forEach
                            val d = details?.get(k)?.jsonObject
                            st.bindText(1, collection); st.bindLong(2, n.toLong()); st.bindText(3, name)
                            st.bindLong(4, d?.get("hadithnumber_first")?.jsonPrimitive?.content?.toDoubleOrNull()?.toLong() ?: 0)
                            st.bindLong(5, d?.get("hadithnumber_last")?.jsonPrimitive?.content?.toDoubleOrNull()?.toLong() ?: 0)
                            st.step(); st.reset()
                        }
                    }
                    c.prepare("INSERT OR REPLACE INTO installed VALUES (?,?)").use { it.bindText(1, collection); it.bindLong(2, kotlin.time.Clock.System.now().toEpochMilliseconds()); it.step() }
                    c.execSQL("COMMIT")
                } catch (e: Throwable) { c.execSQL("ROLLBACK"); throw e }
            }
        }.onFailure { println("HadithStore.install($collection): ${it.message}") }.isSuccess
        _progress.value = _progress.value - collection
        return ok
    }

    suspend fun remove(collection: String) = db { c ->
        c.prepare("DELETE FROM hadith WHERE collection=?").use { it.bindText(1, collection); it.step() }
        c.prepare("DELETE FROM book WHERE collection=?").use { it.bindText(1, collection); it.step() }
        c.prepare("DELETE FROM installed WHERE collection=?").use { it.bindText(1, collection); it.step() }
        Unit
    }
}
