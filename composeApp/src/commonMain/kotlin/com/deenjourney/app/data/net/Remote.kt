package com.deenjourney.app.data.net

import com.deenjourney.app.core.Platform
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.get
import io.ktor.client.request.prepareGet
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import io.ktor.http.contentLength
import io.ktor.http.isSuccess
import io.ktor.serialization.kotlinx.json.json
import io.ktor.utils.io.readAvailable
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.double
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlin.time.Clock

val AppJson = Json { ignoreUnknownKeys = true; isLenient = true; coerceInputValues = true; explicitNulls = false }

fun buildHttp(): HttpClient = HttpClient {
    install(ContentNegotiation) { json(AppJson) }
    install(HttpTimeout) { requestTimeoutMillis = 60_000; connectTimeoutMillis = 15_000; socketTimeoutMillis = 30_000 }
    followRedirects = true
}

/** Downloads [url] to [path] in chunks, reporting progress 0..1. Returns false on failure. */
suspend fun HttpClient.download(url: String, path: String, onProgress: (Float) -> Unit = {}): Boolean = runCatching {
    prepareGet(url).execute { resp: HttpResponse ->
        if (!resp.status.isSuccess()) error("HTTP ${resp.status}")
        val total = resp.contentLength() ?: -1L
        val ch = resp.body<io.ktor.utils.io.ByteReadChannel>()
        val out = ArrayList<ByteArray>()
        var read = 0L
        val buf = ByteArray(64 * 1024)
        while (true) {
            val n = ch.readAvailable(buf, 0, buf.size)
            if (n <= 0) { if (ch.isClosedForRead) break else continue }
            out.add(buf.copyOf(n)); read += n
            if (total > 0) onProgress((read.toFloat() / total).coerceAtMost(1f))
        }
        val all = ByteArray(read.toInt()); var o = 0
        for (b in out) { b.copyInto(all, o); o += b.size }
        Platform.writeFile(path, all)
    }
}.isSuccess

// ---------------------------------------------------------------- Quran.Foundation (tafsir & word-by-word)

data class WordGloss(val arabic: String, val gloss: String)
data class TafsirText(val source: String, val html: String)

/**
 * Tafsir and word-by-word are shown at runtime only (Quran.Foundation terms: display, no bundling, cache ≤ 7 days).
 * Uses the public v4 endpoint today; switch [base] to your backend proxy once Quran.Foundation credentials are issued.
 */
class QuranFoundation(private val http: HttpClient, private val base: String = "https://api.quran.com/api/v4") {
    private val tafsirCache = HashMap<String, Pair<Long, TafsirText>>()
    private val wordCache = HashMap<String, Pair<Long, List<WordGloss>>>()
    private val week = 7L * 24 * 3600 * 1000

    /** Tafsir ids: 169 Ibn Kathir (EN, abridged), 168 Ma'arif al-Qur'an (EN), 160 Ibn Kathir (UR), 159 Bayan ul Quran (UR), 16 Al-Muyassar (AR), 14 Ibn Kathir (AR). */
    suspend fun tafsir(id: Int, key: String): TafsirText? {
        val k = "$id|$key"; val now = Clock.System.now().toEpochMilliseconds()
        tafsirCache[k]?.let { if (now - it.first < week) return it.second }
        return runCatching {
            val o = http.get("$base/tafsirs/$id/by_ayah/$key").body<JsonObject>()["tafsir"]!!.jsonObject
            TafsirText(o["resource_name"]?.jsonPrimitive?.contentOrNull ?: "", o["text"]?.jsonPrimitive?.contentOrNull ?: "")
        }.getOrNull()?.also { tafsirCache[k] = now to it }
    }

    suspend fun words(key: String, lang: String): List<WordGloss> {
        val k = "$key|$lang"; val now = Clock.System.now().toEpochMilliseconds()
        wordCache[k]?.let { if (now - it.first < week) return it.second }
        return runCatching {
            val v = http.get("$base/verses/by_key/$key?words=true&language=$lang&word_fields=text_uthmani").body<JsonObject>()["verse"]!!.jsonObject
            v["words"]!!.jsonArray.map { it.jsonObject }.filter { it["char_type_name"]?.jsonPrimitive?.contentOrNull == "word" }.map {
                WordGloss(it["text_uthmani"]?.jsonPrimitive?.contentOrNull ?: "", it["translation"]?.jsonObject?.get("text")?.jsonPrimitive?.contentOrNull ?: "")
            }
        }.getOrDefault(emptyList()).also { if (it.isNotEmpty()) wordCache[k] = now to it }
    }
}

/** Strips the HTML of tafsir text into readable paragraphs. */
fun htmlToText(html: String): String = html
    .replace(Regex("(?i)<br\\s*/?>"), "\n").replace(Regex("(?i)</p>|</h\\d>|</li>"), "\n\n").replace(Regex("<[^>]+>"), "")
    .replace("&nbsp;", " ").replace("&amp;", "&").replace("&quot;", "\"").replace("&#39;", "'").replace("&lt;", "<").replace("&gt;", ">")
    .replace(Regex("\n{3,}"), "\n\n").trim()

// ---------------------------------------------------------------- recitation audio

data class Reciter(val id: String, val name: String, val style: String, val everyayah: String, val islamicNetwork: String)

object Reciters {
    val all = listOf(
        Reciter("alafasy", "Mishary Rashid Alafasy", "murattal", "Alafasy_128kbps", "ar.alafasy"),
        Reciter("sudais", "Abdul Rahman Al-Sudais", "murattal", "Abdurrahmaan_As-Sudais_192kbps", "ar.abdurrahmaansudais"),
        Reciter("husary", "Mahmoud Khalil Al-Husary", "murattal", "Husary_128kbps", "ar.husary"),
        Reciter("husary_muallim", "Mahmoud Khalil Al-Husary (Muallim)", "muallim", "Husary_Muallim_128kbps", "ar.husarymujawwad"),
        Reciter("abdulbasit", "Abdul Basit Abdus Samad", "murattal", "Abdul_Basit_Murattal_192kbps", "ar.abdulbasitmurattal"),
        Reciter("minshawi", "Mohamed Siddiq Al-Minshawi", "murattal", "Minshawy_Murattal_128kbps", "ar.minshawi"),
        Reciter("muaiqly", "Maher Al-Muaiqly", "murattal", "MaherAlMuaiqly128kbps", "ar.mahermuaiqly"),
        Reciter("shuraim", "Saud Al-Shuraim", "murattal", "Saood_ash-Shuraym_128kbps", "ar.saoodshuraym"),
    )
    fun of(id: String) = all.firstOrNull { it.id == id } ?: all.first()
    private fun pad(n: Int) = n.toString().padStart(3, '0')
    fun everyAyahUrl(r: Reciter, sura: Int, aya: Int) = "https://everyayah.com/data/${r.everyayah}/${pad(sura)}${pad(aya)}.mp3"
    fun fallbackUrl(r: Reciter, globalAyah: Int) = "https://cdn.islamic.network/quran/audio/128/${r.islamicNetwork}/$globalAyah.mp3"
    fun localPath(r: Reciter, sura: Int, aya: Int) = "${Platform.filesDir()}/audio/${r.id}/${pad(sura)}${pad(aya)}.mp3"
}

// ---------------------------------------------------------------- metal prices & FX (zakat)

@Serializable data class MetalPrices(val goldUsdPerOz: Double, val silverUsdPerOz: Double, val usdTo: Map<String, Double>, val at: Long)

class PriceRemote(private val http: HttpClient) {
    suspend fun fetch(): MetalPrices? = runCatching {
        val gold = http.get("https://api.gold-api.com/price/XAU").body<JsonObject>()["price"]!!.jsonPrimitive.double
        val silver = http.get("https://api.gold-api.com/price/XAG").body<JsonObject>()["price"]!!.jsonPrimitive.double
        val rates = http.get("https://open.er-api.com/v6/latest/USD").body<JsonObject>()["rates"]!!.jsonObject.mapValues { it.value.jsonPrimitive.double }
        MetalPrices(gold, silver, rates, Clock.System.now().toEpochMilliseconds())
    }.getOrNull()
}

const val TROY_OUNCE_GRAMS = 31.1034768
const val NISAB_GOLD_G = 87.48
const val NISAB_SILVER_G = 612.36

suspend fun HttpClient.textOrNull(url: String): String? = runCatching { get(url).let { if (it.status.isSuccess()) it.bodyAsText() else null } }.getOrNull()
