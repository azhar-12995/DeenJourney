package com.deenjourney.app.data.content

import com.deenjourney.app.core.L
import com.deenjourney.app.res.Res
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.builtins.MapSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.json.Json
import org.jetbrains.compose.resources.ExperimentalResourceApi

// ---------------------------------------------------------------- models (docs/CONTENT_SCHEMA.md)

@Serializable data class Ref(val type: String = "note", val label: String = "", val key: String? = null, val verify: Boolean = false)

@Serializable data class DuaCategory(val id: String, val title: L, val subtitle: L = L(), val glyph: String = "dua", val duas: List<String> = emptyList())
@Serializable data class Dua(
    val id: String, val title: L = L(), val arabic: String = "", val translit: String = "", val meaning: L = L(), val refs: List<Ref> = emptyList(),
    val repeat: Int = 1, val `when`: L? = null, val tags: List<String> = emptyList(),
)
@Serializable data class DuaFile(val categories: List<DuaCategory> = emptyList(), val duas: List<Dua> = emptyList())

@Serializable data class Dhikr(val id: String, val arabic: String = "", val translit: String = "", val meaning: L = L(), val count: Int = 1, val refs: List<Ref> = emptyList(), val virtue: L? = null)
@Serializable data class AdhkarFile(val morning: List<Dhikr> = emptyList(), val evening: List<Dhikr> = emptyList(), val after_salah: List<Dhikr> = emptyList(), val sleep: List<Dhikr> = emptyList())

@Serializable data class Kalima(val id: String, val n: Int = 0, val name: String = "", val title: L = L(), val arabic: String = "", val translit: String = "", val meaning: L = L(), val refs: List<Ref> = emptyList())

@Serializable data class Name99(val n: Int, val arabic: String, val translit: String, val meaning: L = L(), val explanation: L = L(), val reflection: L = L(), val quran: List<String> = emptyList())

@Serializable data class Fiqh(val hanafi: L? = null, val shafii: L? = null, val maliki: L? = null, val hanbali: L? = null) {
    fun forSchool(s: String): L? = when (s) { "shafii" -> shafii; "maliki" -> maliki; "hanbali" -> hanbali; else -> hanafi }
}
@Serializable data class Step(
    val title: L = L(), val body: L = L(), val glyph: String? = null, val arabic: String? = null, val translit: String? = null, val meaning: L? = null,
    val fiqh: Fiqh? = null, val `when`: L? = null, val day: String? = null,
)
@Serializable data class Section(val title: L = L(), val body: L = L(), val bullets: List<L> = emptyList(), val refs: List<Ref> = emptyList())
@Serializable data class Guide(
    val title: L = L(), val subtitle: L = L(), val intro: L = L(), val steps: List<Step> = emptyList(), val about: List<Section> = emptyList(),
    val duas: List<String> = emptyList(), val refs: List<Ref> = emptyList(),
)

@Serializable data class Posture(val id: String, val pose: String = "qiyam", val title: L = L(), val bullets: List<L> = emptyList(), val arabic: String? = null, val translit: String? = null, val meaning: L? = null, val fiqh: Fiqh? = null)
@Serializable data class SalahFile(val postures: List<Posture> = emptyList(), val detail: List<Section> = emptyList(), val mistakes: List<Section> = emptyList(), val times: List<Section> = emptyList())

@Serializable data class Quiz(val q: L, val options: List<L>, val answer: Int, val explain: L = L())
@Serializable data class Lesson(
    val id: String, val title: L = L(), val subtitle: L = L(), val minutes: Int = 4, val ages: String = "all", val points: List<L> = emptyList(),
    val body: L = L(), val refs: List<Ref> = emptyList(), val quiz: List<Quiz> = emptyList(),
)
@Serializable data class Track(val id: String, val n: Int = 0, val title: L = L(), val subtitle: L = L(), val glyph: String = "lesson", val lessons: List<Lesson> = emptyList())
@Serializable data class LessonFile(val tracks: List<Track> = emptyList())

@Serializable data class TextRef(val text: L = L(), val ref: Ref = Ref())
@Serializable data class Scenario(val q: L = L(), val options: List<L> = emptyList(), val answer: Int = 0, val feedback: L = L())
@Serializable data class Akhlaq(
    val id: String, val title: L = L(), val subtitle: L = L(), val glyph: String = "akhlaq", val body: L = L(), val quran: TextRef? = null, val hadith: TextRef? = null,
    val reflect: List<L> = emptyList(), val practice: List<L> = emptyList(), val scenario: Scenario? = null,
)

@Serializable data class Chapter(val title: L = L(), val body: L = L(), val refs: List<Ref> = emptyList())
@Serializable data class Prophet(
    val id: String, val name: L = L(), val arabic: String = "", val honorific: String = "AS", val vignette: String = "generic", val era: String = "early",
    val summary: L = L(), val quran: List<String> = emptyList(), val adults: List<Chapter> = emptyList(), val children: List<Chapter> = emptyList(), val lessons: List<L> = emptyList(),
)
@Serializable data class SeerahEvent(val year: Int = 0, val hijri: String = "", val title: L = L(), val body: L = L(), val refs: List<Ref> = emptyList())
@Serializable data class SeerahFile(val timeline: List<SeerahEvent> = emptyList(), val chapters: List<Chapter> = emptyList())

@Serializable data class DailyHadith(
    val id: String, val collection: String = "", val number: Int = 0, val book: L = L(), val arabic: String = "", val en: String = "", val ur: String = "",
    val narrator: L = L(), val grade: String = "", val short: L = L(), val explanation: L = L(), val topic: String = "",
)
@Serializable data class Reflection(
    val id: String, val headline_ru: String = "", val body_ru: String = "", val headline: L = L(), val body: L = L(), val quote: L = L(), val ref: Ref = Ref(),
    val deed: L = L(), val deed_ru: String = "", val kids: L = L(),
)
@Serializable data class KidCard(val id: String, val title: String = "", val arabic: String = "", val meaning: L = L(), val avatar: String = "boy", val tip: L = L())
@Serializable data class KidsFile(val greetings: List<KidCard> = emptyList(), val manners: List<KidCard> = emptyList(), val first_words: List<KidCard> = emptyList())
@Serializable data class IslamicEvent(val id: String, val month: Int = 0, val day: Int = 0, val title: L = L(), val body: L = L(), val kind: String = "observance", val disputed: Boolean = false, val refs: List<Ref> = emptyList())
@Serializable data class Pillar(val id: String, val n: Int = 0, val title: L = L(), val subtitle: L = L(), val glyph: String = "pillars", val arabic: String = "", val body: L = L(), val refs: List<Ref> = emptyList(), val link: String? = null)
@Serializable data class SectionsFile(val sections: List<Section> = emptyList())
@Serializable data class CheckItem(val id: String, val text: L = L(), val sub: L? = null)
@Serializable data class CheckGroup(val group: L = L(), val items: List<CheckItem> = emptyList())
@Serializable data class Talbiyah(val arabic: String = "", val translit: String = "", val meaning: L = L(), val ref: Ref = Ref())
@Serializable data class HajjExtra(val checklist: List<CheckGroup> = emptyList(), val talbiyah: Talbiyah = Talbiyah())
@Serializable data class Faq(val q: L = L(), val a: L = L(), val link: String? = null)
@Serializable data class SourceItem(val area: L = L(), val source: String = "", val license: String = "", val url: String = "")

/** Loads the authored content bundled in composeResources/files/content (lazy + cached). Missing files → empty. */
class ContentRepo {
    private val json = Json { ignoreUnknownKeys = true; isLenient = true; coerceInputValues = true }
    private val cache = HashMap<String, Any>()
    private val lock = Mutex()

    @OptIn(ExperimentalResourceApi::class)
    private suspend fun <T : Any> load(file: String, ser: KSerializer<T>, empty: T): T = lock.withLock {
        @Suppress("UNCHECKED_CAST")
        cache[file]?.let { return it as T }
        val v = runCatching { json.decodeFromString(ser, Res.readBytes("files/content/$file").decodeToString()) }
            .onFailure { println("ContentRepo: $file → ${it.message}") }
            .getOrDefault(empty)
        cache[file] = v
        v
    }

    suspend fun duas() = load("duas.json", DuaFile.serializer(), DuaFile())
    suspend fun adhkar() = load("adhkar.json", AdhkarFile.serializer(), AdhkarFile())
    suspend fun kalimas() = load("kalimas.json", ListSerializer(Kalima.serializer()), emptyList())
    suspend fun names() = load("names.json", ListSerializer(Name99.serializer()), emptyList())
    suspend fun guides() = load("guides.json", MapSerializer(String.serializer(), Guide.serializer()), emptyMap())
    suspend fun salah() = load("salah.json", SalahFile.serializer(), SalahFile())
    suspend fun lessons() = load("lessons.json", LessonFile.serializer(), LessonFile())
    suspend fun akhlaq() = load("akhlaq.json", ListSerializer(Akhlaq.serializer()), emptyList())
    suspend fun prophets() = load("prophets.json", ListSerializer(Prophet.serializer()), emptyList())
    suspend fun seerah() = load("seerah.json", SeerahFile.serializer(), SeerahFile())
    suspend fun dailyHadith() = load("hadith_daily.json", ListSerializer(DailyHadith.serializer()), emptyList())
    suspend fun reflections() = load("reflections.json", ListSerializer(Reflection.serializer()), emptyList())
    suspend fun kids() = load("kids.json", KidsFile.serializer(), KidsFile())
    suspend fun events() = load("events.json", ListSerializer(IslamicEvent.serializer()), emptyList())
    suspend fun pillars() = load("pillars.json", ListSerializer(Pillar.serializer()), emptyList())
    suspend fun fasting() = load("fasting.json", SectionsFile.serializer(), SectionsFile())
    suspend fun zakat() = load("zakat.json", SectionsFile.serializer(), SectionsFile())
    suspend fun hajjExtra() = load("hajj_extra.json", HajjExtra.serializer(), HajjExtra())
    suspend fun faq() = load("faq.json", ListSerializer(Faq.serializer()), emptyList())
    suspend fun sources() = load("sources.json", ListSerializer(SourceItem.serializer()), emptyList())

    suspend fun dua(id: String): Dua? = duas().duas.firstOrNull { it.id == id }
    suspend fun lesson(id: String): Pair<Track, Lesson>? = lessons().tracks.firstNotNullOfOrNull { t -> t.lessons.firstOrNull { it.id == id }?.let { t to it } }
    suspend fun allLessons(): List<Pair<Track, Lesson>> = lessons().tracks.flatMap { t -> t.lessons.map { t to it } }
}
