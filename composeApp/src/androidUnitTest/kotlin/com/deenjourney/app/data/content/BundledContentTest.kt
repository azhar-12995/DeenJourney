package com.deenjourney.app.data.content

import java.io.File
import kotlinx.serialization.KSerializer
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.builtins.MapSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class BundledContentTest {
    private val json = Json { ignoreUnknownKeys = true }
    private val dir = File("src/commonMain/composeResources/files/content")
        .takeIf { it.exists() } ?: File("composeApp/src/commonMain/composeResources/files/content")
    private fun <T> read(name: String, serializer: KSerializer<T>): T =
        json.decodeFromString(serializer, File(dir, "$name.json").readText())

    @Test fun packsDecodeWithActualAppModelsAndResolveLinks() {
        val duas = read("duas", DuaFile.serializer())
        assertTrue(duas.duas.isNotEmpty())
        assertTrue(duas.categories.flatMap { it.duas }.all { id -> duas.duas.any { it.id == id } })
        val adhkar = read("adhkar", AdhkarFile.serializer())
        assertTrue(listOf(adhkar.morning, adhkar.evening, adhkar.after_salah, adhkar.sleep).all { list -> list.isNotEmpty() && list.all { it.count > 0 } })
        assertTrue(read("kalimas", ListSerializer(Kalima.serializer())).isNotEmpty())
        assertEquals((1..99).toList(), read("names", ListSerializer(Name99.serializer())).map { it.n }.sorted())
        assertTrue(read("guides", MapSerializer(String.serializer(), Guide.serializer())).keys.containsAll(listOf("wudu", "ghusl", "hajj", "umrah")))
        assertTrue(read("salah", SalahFile.serializer()).postures.isNotEmpty())
        val lessons = read("lessons", LessonFile.serializer()).tracks.flatMap { it.lessons }
        assertTrue(lessons.isNotEmpty())
        assertTrue(lessons.flatMap { it.quiz }.all { it.answer in it.options.indices })
        assertTrue(read("akhlaq", ListSerializer(Akhlaq.serializer())).isNotEmpty())
        assertTrue(read("prophets", ListSerializer(Prophet.serializer())).isNotEmpty())
        assertTrue(read("seerah", SeerahFile.serializer()).timeline.isNotEmpty())
        assertTrue(read("hadith_daily", ListSerializer(DailyHadith.serializer())).isNotEmpty())
        assertTrue(read("reflections", ListSerializer(Reflection.serializer())).isNotEmpty())
        assertTrue(read("kids", KidsFile.serializer()).greetings.isNotEmpty())
        assertTrue(read("events", ListSerializer(IslamicEvent.serializer())).all {
            (it.month in 1..12 && it.day in 1..30) ||
                (it.id == "ayyam-al-bid" && it.month == 0 && it.day == 13) ||
                (it.id == "mon-thu-fast" && it.month == 0 && it.day == 0)
        })
        assertTrue(read("pillars", ListSerializer(Pillar.serializer())).isNotEmpty())
        assertTrue(read("fasting", SectionsFile.serializer()).sections.isNotEmpty())
        assertTrue(read("zakat", SectionsFile.serializer()).sections.isNotEmpty())
        assertTrue(read("hajj_extra", HajjExtra.serializer()).checklist.isNotEmpty())
        assertTrue(read("faq", ListSerializer(Faq.serializer())).isNotEmpty())
        assertTrue(read("sources", ListSerializer(SourceItem.serializer())).isNotEmpty())
    }
}
