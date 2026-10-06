package com.deenjourney.app.data.quran

import com.deenjourney.app.data.db.ReadOnlyDb
import com.deenjourney.app.data.db.int
import com.deenjourney.app.data.db.str
import com.deenjourney.app.data.db.strOrNull

data class Surah(val id: Int, val nameAr: String, val nameTr: String, val meaning: String, val type: String, val ayas: Int, val revOrder: Int) {
    val makki: Boolean get() = type.startsWith("Mecc")
}

data class Ayah(
    val id: Int, val sura: Int, val aya: Int, val juz: Int, val page: Int, val hizbQ: Int, val sajda: String?,
    val ar: String, val en: String, val enNotes: String?, val ur: String,
) {
    val key: String get() = "$sura:$aya"
}

data class Juz(val n: Int, val nameTr: String, val nameAr: String, val startSura: Int, val startAya: Int, val endSura: Int, val endAya: Int)

data class SearchHit(val ayah: Ayah, val matchIn: String)

object QuranNames {
    /** Common English transliterations (Tanzil's are phonetic, e.g. "Al-Faatiha"). */
    val tr = listOf(
        "Al-Fatiha", "Al-Baqarah", "Aal-e-Imran", "An-Nisa", "Al-Ma’idah", "Al-An‘am", "Al-A‘raf", "Al-Anfal", "At-Tawbah", "Yunus",
        "Hud", "Yusuf", "Ar-Ra‘d", "Ibrahim", "Al-Hijr", "An-Nahl", "Al-Isra", "Al-Kahf", "Maryam", "Ta-Ha",
        "Al-Anbiya", "Al-Hajj", "Al-Mu’minun", "An-Nur", "Al-Furqan", "Ash-Shu‘ara", "An-Naml", "Al-Qasas", "Al-‘Ankabut", "Ar-Rum",
        "Luqman", "As-Sajdah", "Al-Ahzab", "Saba", "Fatir", "Ya-Sin", "As-Saffat", "Sad", "Az-Zumar", "Ghafir",
        "Fussilat", "Ash-Shura", "Az-Zukhruf", "Ad-Dukhan", "Al-Jathiyah", "Al-Ahqaf", "Muhammad", "Al-Fath", "Al-Hujurat", "Qaf",
        "Adh-Dhariyat", "At-Tur", "An-Najm", "Al-Qamar", "Ar-Rahman", "Al-Waqi‘ah", "Al-Hadid", "Al-Mujadilah", "Al-Hashr", "Al-Mumtahanah",
        "As-Saff", "Al-Jumu‘ah", "Al-Munafiqun", "At-Taghabun", "At-Talaq", "At-Tahrim", "Al-Mulk", "Al-Qalam", "Al-Haqqah", "Al-Ma‘arij",
        "Nuh", "Al-Jinn", "Al-Muzzammil", "Al-Muddaththir", "Al-Qiyamah", "Al-Insan", "Al-Mursalat", "An-Naba", "An-Nazi‘at", "‘Abasa",
        "At-Takwir", "Al-Infitar", "Al-Mutaffifin", "Al-Inshiqaq", "Al-Buruj", "At-Tariq", "Al-A‘la", "Al-Ghashiyah", "Al-Fajr", "Al-Balad",
        "Ash-Shams", "Al-Layl", "Ad-Duha", "Ash-Sharh", "At-Tin", "Al-‘Alaq", "Al-Qadr", "Al-Bayyinah", "Az-Zalzalah", "Al-‘Adiyat",
        "Al-Qari‘ah", "At-Takathur", "Al-‘Asr", "Al-Humazah", "Al-Fil", "Quraysh", "Al-Ma‘un", "Al-Kawthar", "Al-Kafirun", "An-Nasr",
        "Al-Masad", "Al-Ikhlas", "Al-Falaq", "An-Nas",
    )
    val juzTr = listOf(
        "Alif Lam Mim", "Sayaqul", "Tilka ar-Rusul", "Lan Tanalu", "Wal-Muhsanat", "La Yuhibbullah", "Wa Idha Sami‘u", "Wa Law Annana",
        "Qal al-Mala’", "Wa‘lamu", "Ya‘tadhirun", "Wa Ma Min Dabbah", "Wa Ma Ubarri’u", "Rubama", "Subhan alladhi", "Qal Alam",
        "Iqtaraba", "Qad Aflaha", "Wa Qal alladhina", "A’man Khalaq", "Utlu Ma Uhiya", "Wa Man Yaqnut", "Wa Mali", "Fa Man Azlamu",
        "Ilayhi Yuraddu", "Ha Mim", "Qala Fa Ma Khatbukum", "Qad Sami‘allah", "Tabarak alladhi", "‘Amma",
    )
    val juzAr = listOf(
        "الٓمٓ", "سَيَقُولُ", "تِلْكَ ٱلرُّسُلُ", "لَن تَنَالُوا۟", "وَٱلْمُحْصَنَٰتُ", "لَا يُحِبُّ ٱللَّهُ", "وَإِذَا سَمِعُوا۟", "وَلَوْ أَنَّنَا",
        "قَالَ ٱلْمَلَأُ", "وَٱعْلَمُوٓا۟", "يَعْتَذِرُونَ", "وَمَا مِن دَآبَّةٍ", "وَمَآ أُبَرِّئُ", "رُبَمَا", "سُبْحَٰنَ ٱلَّذِىٓ", "قَالَ أَلَمْ",
        "ٱقْتَرَبَ", "قَدْ أَفْلَحَ", "وَقَالَ ٱلَّذِينَ", "أَمَّنْ خَلَقَ", "ٱتْلُ مَآ أُوحِىَ", "وَمَن يَقْنُتْ", "وَمَا لِىَ", "فَمَنْ أَظْلَمُ",
        "إِلَيْهِ يُرَدُّ", "حمٓ", "قَالَ فَمَا خَطْبُكُمْ", "قَدْ سَمِعَ ٱللَّهُ", "تَبَٰرَكَ ٱلَّذِى", "عَمَّ",
    )
}

/** Search key used in the bundled DB (same rules as tools/data/build_quran.py normalise()). */
fun normaliseArabic(s: String): String {
    val sb = StringBuilder()
    for (ch in s) {
        val c = ch.code
        val drop = c in 0x0610..0x061A || c in 0x064B..0x065F || c == 0x0670 || c in 0x06D6..0x06ED || c == 0x0640 || c in 0x08D3..0x08FF
        if (drop) continue
        sb.append(
            when (ch) {
                'ٱ', 'أ', 'إ', 'آ' -> 'ا'
                'ى', 'ئ' -> 'ي'
                'ة' -> 'ه'
                'ؤ' -> 'و'
                else -> ch
            }
        )
    }
    return sb.toString().replace(Regex("\\s+"), " ").trim()
}

class QuranRepo(private val db: ReadOnlyDb) {
    private var surahCache: List<Surah>? = null
    private var juzCache: List<Juz>? = null

    suspend fun surahs(): List<Surah> = surahCache ?: db.query("SELECT id,name_ar,name_tr,name_en,type,ayas,rev_order FROM surah ORDER BY id") {
        val id = it.int(0)
        Surah(id, it.str(1), QuranNames.tr.getOrElse(id - 1) { _ -> it.str(2) }, it.str(3), it.str(4), it.int(5), it.int(6))
    }.also { surahCache = it }

    suspend fun surah(id: Int): Surah = surahs()[id - 1]

    suspend fun juzList(): List<Juz> = juzCache ?: run {
        val starts = db.query("SELECT id,sura,aya FROM juz ORDER BY id") { Triple(it.int(0), it.int(1), it.int(2)) }
        val s = surahs()
        starts.mapIndexed { i, (n, sura, aya) ->
            val (es, ea) = if (i + 1 < starts.size) {
                val (_, ns, na) = starts[i + 1]
                if (na > 1) ns to na - 1 else (ns - 1) to s[ns - 2].ayas
            } else 114 to 6
            Juz(n, QuranNames.juzTr[n - 1], QuranNames.juzAr[n - 1], sura, aya, es, ea)
        }.also { juzCache = it }
    }

    private val cols = "id,sura,aya,juz,page,hizb_q,sajda,ar,en,en_notes,ur"
    private fun map(st: androidx.sqlite.SQLiteStatement) = Ayah(
        st.int(0), st.int(1), st.int(2), st.int(3), st.int(4), st.int(5), st.strOrNull(6), st.str(7), st.str(8), st.strOrNull(9), st.str(10)
    )

    suspend fun ayat(sura: Int): List<Ayah> = db.query("SELECT $cols FROM ayah WHERE sura=? ORDER BY aya", sura) { map(it) }
    suspend fun ayah(sura: Int, aya: Int): Ayah? = db.one("SELECT $cols FROM ayah WHERE sura=? AND aya=?", sura, aya) { map(it) }
    suspend fun ayahById(id: Int): Ayah? = db.one("SELECT $cols FROM ayah WHERE id=?", id) { map(it) }
    suspend fun range(sura: Int, from: Int, to: Int): List<Ayah> = db.query("SELECT $cols FROM ayah WHERE sura=? AND aya BETWEEN ? AND ? ORDER BY aya", sura, from, to) { map(it) }
    suspend fun juzAyat(juz: Int): List<Ayah> = db.query("SELECT $cols FROM ayah WHERE juz=? ORDER BY id", juz) { map(it) }
    suspend fun pageOf(sura: Int, aya: Int): Int = db.one("SELECT page FROM ayah WHERE sura=? AND aya=?", sura, aya) { it.int(0) } ?: 1
    suspend fun pageAyat(page: Int): List<Ayah> = db.query("SELECT $cols FROM ayah WHERE page=? ORDER BY id", page) { map(it) }

    /** Parses "2:153" or "43:13-14" into ayat. */
    suspend fun byRef(ref: String): List<Ayah> {
        val m = Regex("^(\\d+):(\\d+)(?:-(\\d+))?$").find(ref.trim()) ?: return emptyList()
        val s = m.groupValues[1].toInt(); val a = m.groupValues[2].toInt(); val b = m.groupValues[3].toIntOrNull() ?: a
        return range(s, a, b)
    }

    /**
     * Full-text search. Arabic queries are matched diacritics-insensitively; Latin queries against the English
     * translation; Urdu (Arabic script but contains Urdu-only letters) against the Urdu translation.
     */
    suspend fun search(query: String, scope: String = "all", limit: Int = 200): List<SearchHit> {
        val q = query.trim()
        if (q.length < 2) return emptyList()
        val isArabicScript = q.any { it.code in 0x0600..0x06FF }
        val urduOnly = q.any { it in "ٹڈڑںھہےۓکگپچژ" }
        val terms = (if (isArabicScript && !urduOnly) normaliseArabic(q) else q.lowercase())
            .replace("\"", " ").split(' ').filter { it.isNotBlank() }.joinToString(" ") { "$it*" }
        if (terms.isBlank()) return emptyList()
        val column = when (scope) {
            "arabic" -> "ar_norm"
            "english" -> "en"
            "urdu" -> "ur"
            else -> if (!isArabicScript) "en" else if (urduOnly) "ur" else null
        }
        val match = if (column != null) "$column:$terms" else terms
        return runCatching {
            db.query("SELECT a.id,a.sura,a.aya,a.juz,a.page,a.hizb_q,a.sajda,a.ar,a.en,a.en_notes,a.ur FROM ayah_fts f JOIN ayah a ON a.id=f.docid WHERE ayah_fts MATCH ? ORDER BY a.id LIMIT ?", match, limit) {
                SearchHit(map(it), column ?: "all")
            }
        }.getOrElse {
            // FTS unavailable on this SQLite build: fall back to LIKE
            val like = "%${q}%"
            db.query("SELECT $cols FROM ayah WHERE en LIKE ? OR ur LIKE ? OR ar_norm LIKE ? ORDER BY id LIMIT ?", like, like, "%${normaliseArabic(q)}%", limit) { SearchHit(map(it), "all") }
        }
    }

    suspend fun info(key: String): String? = db.one("SELECT v FROM info WHERE k=?", key) { it.str(0) }
}
