package com.deenjourney.app.feature.quran

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.BiasAlignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.deenjourney.app.core.*
import com.deenjourney.app.data.net.Reciters as AudioReciters
import com.deenjourney.app.data.net.download
import com.deenjourney.app.data.quran.*
import com.deenjourney.app.data.settings.SettingsRepo
import com.deenjourney.app.data.user.UserRepo
import com.deenjourney.app.design.*
import com.deenjourney.app.feature.*
import com.deenjourney.app.nav.*
import com.deenjourney.app.platform.*
import io.ktor.client.HttpClient
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch
import org.koin.compose.koinInject

@Composable
fun repeatLabel(mode: RepeatMode): String = when (mode) {
    RepeatMode.Off -> t("Repeat off", "دہرائی بند", "بدون تكرار")
    RepeatMode.One -> t("Repeat ayah", "آیت دہرائیں", "تكرار الآية")
    RepeatMode.All -> t("Repeat surah", "سورۃ دہرائیں", "تكرار السورة")
}

@Composable
fun QuranPlayerScreen() {
    DarkPlayerSystemBars()
    val player = koinInject<RecitationPlayer>(); val p by player.state.collectAsState()
    val nav = LocalNavigator.current; val settings = koinInject<SettingsRepo>(); val users = koinInject<UserRepo>()
    val http = koinInject<HttpClient>(); val repo = koinInject<QuranRepo>(); val scope = rememberCoroutineScope()
    var sheet by remember { mutableStateOf<String?>(null) }
    var progress by remember { mutableStateOf<Float?>(null) }; var result by remember { mutableStateOf<Int?>(null) }
    var metadata by remember(p.current?.sura) { mutableStateOf<Surah?>(null) }
    val prefs by settings.flow.collectAsState()
    val highlight = prefs.quran.followAudio
    val item = p.current; val gold = Color(0xFFE8C77A); val ink = Color(0xFFE4EFE6)
    val saved by users.saved().collectAsState(emptyList())
    LaunchedEffect(item?.sura) { item?.sura?.takeIf { it in 1..114 }?.let { metadata = repo.surah(it) } }
    fun downloadQueue() {
        if (progress != null || p.items.isEmpty()) return
        val queue = p.items.toList()
        scope.launch {
            progress = 0f; result = null
            try {
                // The queue's reciter may differ from a preference changed in another screen.
                val reciter = AudioReciters.all.firstOrNull { it.name == queue.first().subtitle }
                    ?: AudioReciters.of(settings.get().quran.reciter)
                var failures = 0
                queue.forEachIndexed { i, a ->
                    val path = AudioReciters.localPath(reciter, a.sura, a.aya)
                    if (!Platform.fileExists(path) && !http.download(a.url, path) &&
                        (a.fallbackUrl == null || !http.download(a.fallbackUrl, path))) failures++
                    progress = (i + 1f) / queue.size
                }
                result = failures
            } catch (e: CancellationException) { throw e } catch (_: Exception) { result = -1 }
            finally { progress = null }
        }
    }
    Screen(modifier = Modifier.background(LightColors.nightGradient), background = Color.Transparent,
        top = { AppBar(metadata?.nameTr ?: t("Recitation", "تلاوت", "التلاوة"), sub = metadata?.let { surahSubtitle(it) },
            onBack = { nav.back() }, light = true, center = true, actions = {
                IconBtn("heart", {
                    item?.let { scope.launch { users.toggleSaved("surah", it.sura.toString(), QuranNames.tr[it.sura - 1]) } }
                }, tint = if (saved.any { it.type == "surah" && it.key == item?.sura.toString() }) gold else Color.White, description = t("Favorite surah", "پسندیدہ سورۃ", "سورة مفضلة"))
                IconBtn("ellipsis-vertical", { sheet = "options" }, tint = Color.White)
            }) }) {
        if (item == null) Body {
            Txt(t("Choose a surah to start listening.", "سننے کے لیے سورۃ منتخب کریں۔", "اختر سورة لبدء الاستماع."), Dj.type.bodyM, ink)
            DjButton(t("Quran library", "قرآن لائبریری", "مكتبة القرآن"), { nav.go(QuranHome()) }, Modifier.fillMaxWidth())
        } else Body(gap = 22.dp, padding = PaddingValues(horizontal = 22.dp, vertical = 10.dp)) {
            Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                Box(Modifier.widthIn(max = 300.dp).fillMaxWidth().aspectRatio(1f).clip(RoundedCornerShape(26.dp))) {
                    // Figma crops a 600×260 illustration at x=-98.08 in a 300×300 cover.
                    Art("hero_ramadan", Modifier.fillMaxSize(), ContentScale.Crop, BiasAlignment(-0.5f, 0f))
                    Box(Modifier.align(Alignment.BottomCenter).fillMaxWidth().fillMaxHeight(0.5f)
                        .background(Brush.verticalGradient(listOf(Color.Transparent, Color(0xFF06261C)))))
                    Column(Modifier.align(Alignment.BottomCenter).fillMaxWidth().padding(horizontal = 14.dp, vertical = 24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Txt(metadata?.nameTr ?: item.title, Dj.type.headline, Color.White, align = TextAlign.Center)
                        Txt(item.subtitle, Dj.type.bodyS, Color(0xFFE8D5A6), align = TextAlign.Center)
                    }
                }
            }
            PlaybackSeek(p, player, dark = true)
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                PlayerAction("repeat", repeatLabel(p.repeat), { sheet = "repeat" }, ink)
                IconBtn("skip-back", { player.previous() }, tint = Color.White, iconSize = 28.dp)
                IconBtn(if (p.playing) "pause" else "play", { player.toggle() }, Modifier.border(2.5.dp, Color.White, CircleShape), tint = Color.White, size = 76.dp, iconSize = 34.dp)
                IconBtn("skip-forward", { player.next() }, tint = Color.White, iconSize = 28.dp)
                PlayerAction("gauge", "${p.speed}×", { sheet = "speed" }, ink)
            }
            Row(Modifier.fillMaxWidth().padding(top = 10.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                PlayerAction("highlighter", t("Highlight", "نمایاں", "تمييز"), { val enabled = !highlight; scope.launch { settings.update { it.copy(quran = it.quran.copy(followAudio = enabled)) } } }, if (highlight) ink else ink.copy(alpha = 0.45f))
                PlayerAction("timer", t("Sleep timer", "ٹائمر", "مؤقت النوم"), { sheet = "sleep" }, if (p.sleepAtMs != null || p.sleepEndOfQueue) gold else ink)
                PlayerAction("list", t("Verses", "آیات", "الآيات"), { sheet = "verses" }, ink)
                PlayerAction("download", t("Download", "ڈاؤن لوڈ", "تنزيل"), { downloadQueue() }, ink)
            }
            progress?.let { DjProgress(it) }
            if (result != null) Txt(if (result == 0) t("Download complete", "ڈاؤن لوڈ مکمل", "اكتمل التنزيل") else t("Some audio could not be downloaded. Please retry.", "کچھ آڈیو ڈاؤن لوڈ نہیں ہو سکی۔ دوبارہ کوشش کریں۔", "تعذر تنزيل بعض الصوتيات. حاول مجددًا."), Dj.type.bodyS, ink)
            p.error?.let { Txt(it, Dj.type.bodyS, Color(0xFFF2B8B5)) }
            if (p.buffering) Txt(t("Buffering…", "لوڈ ہو رہا ہے…", "جارٍ التحميل…"), Dj.type.caption, ink)
        }
    }
    when (sheet) {
        "sleep" -> SleepTimerSheet(player) { sheet = null }
        "speed" -> DjSheet({ sheet = null }) {
            SectionHeader(t("Playback speed", "تلاوت کی رفتار", "سرعة التشغيل"))
            ChipRow { listOf(0.5f, 0.75f, 1f, 1.25f, 1.5f, 1.75f, 2f).forEach { speed -> DjChip("${speed}×", p.speed == speed, { player.setSpeed(speed); scope.launch { settings.update { it.copy(quran = it.quran.copy(speed = speed)) } }; sheet = null }) } }
        }
        "repeat" -> DjSheet({ sheet = null }) {
            SectionHeader(t("Repeat", "دہرائی", "التكرار"))
            RepeatMode.entries.forEach { mode -> ListRow(repeatLabel(mode), trailing = { DjRadio(p.repeat == mode) }, onClick = { player.setRepeat(mode); scope.launch { settings.update { it.copy(quran = it.quran.copy(repeat = mode.name.lowercase())) } }; sheet = null }) }
        }
        "verses" -> DjSheet({ sheet = null }) {
            SectionHeader(t("Verses", "آیات", "الآيات"))
            androidx.compose.foundation.lazy.LazyColumn(Modifier.heightIn(max = 420.dp)) {
                items(p.items.size) { i -> val verse = p.items[i]; ListRow(verse.title, onClick = { player.jumpTo(i); sheet = null }, trailing = { if (i == p.index) DjIcon("volume-2") }) }
            }
        }
        "options" -> DjSheet({ sheet = null }) {
            ListRow(t("Change reciter", "قاری تبدیل کریں", "تغيير القارئ"), onClick = { sheet = null; nav.go(Reciters) })
            ListRow(t("Open Quran reader", "قرآن ریڈر کھولیں", "فتح قارئ القرآن"), onClick = { item?.let { nav.go(Reader(it.sura, it.aya)) }; sheet = null })
            ListRow(t("Cancel sleep timer", "ٹائمر بند کریں", "إلغاء مؤقت النوم"), onClick = { player.sleepAfter(0); sheet = null })
            ListRow(t("Offline downloads", "آف لائن ڈاؤن لوڈز", "التنزيلات"), onClick = { sheet = null; nav.go(Downloads) })
        }
    }
}

@Composable
private fun PlayerAction(icon: String, label: String, onClick: () -> Unit, tint: Color) {
    Column(Modifier.width(64.dp).clickable(onClick = onClick), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(4.dp)) {
        DjIcon(icon, 22.dp, tint); Txt(label, Dj.type.caption, tint, align = TextAlign.Center)
    }
}

@Composable
fun surahSubtitle(s: Surah): String = t("Surah", "سورۃ", "السورة") + " ${s.id} · ${s.ayas} " + t("verses", "آیات", "آيات") + " · " + if (s.makki) t("Makki", "مکی", "مكية") else t("Madani", "مدنی", "مدنية")

fun audioTime(ms: Long): String = "${ms.coerceAtLeast(0) / 60000}:${((ms.coerceAtLeast(0) / 1000) % 60).toString().padStart(2, '0')}"

@Composable
fun PlaybackSeek(p: PlayerState, player: RecitationPlayer, dark: Boolean = false) {
    val ink = if (dark) Color(0xFFE4EFE6) else Dj.c.text2
    val active = if (dark) Color(0xFFE8C77A) else Dj.c.primary
    var drag by remember(p.current?.id) { mutableStateOf<Float?>(null) }
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Txt(audioTime(drag?.toLong() ?: p.positionMs), Dj.type.caption, ink)
        Slider(value = drag ?: p.positionMs.toFloat().coerceIn(0f, p.durationMs.coerceAtLeast(1).toFloat()), onValueChange = { drag = it },
            onValueChangeFinished = { drag?.let { player.seekTo(it.toLong()) }; drag = null }, enabled = p.durationMs > 0,
            valueRange = 0f..p.durationMs.coerceAtLeast(1).toFloat(), modifier = Modifier.weight(1f).height(24.dp),
            colors = SliderDefaults.colors(thumbColor = active, activeTrackColor = active, inactiveTrackColor = if (dark) Color.White.copy(alpha = 0.25f) else Dj.c.primaryTint))
        Txt(audioTime(p.durationMs), Dj.type.caption, ink)
    }
}

@Composable
fun SleepTimerSheet(player: RecitationPlayer, dismiss: () -> Unit) {
    var minutes by remember { mutableStateOf(30) }; var end by remember { mutableStateOf(false) }; var fade by remember { mutableStateOf(true) }
    DjSheet(dismiss) {
        SectionHeader(t("Sleep timer", "سلیپ ٹائمر", "مؤقت النوم"))
        listOf(15, 30, 45, 60).chunked(2).forEach { row -> Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            row.forEach { value -> DjCard(Modifier.weight(1f), onClick = { minutes = value; end = false }, radius = 12.dp,
                padding = PaddingValues(horizontal = 14.dp, vertical = 12.dp), fill = if (!end && minutes == value) Dj.c.primaryTint else Dj.c.surface,
                border = if (!end && minutes == value) Dj.c.primary else Dj.c.border, elevated = false) {
                Row(verticalAlignment = Alignment.CenterVertically) { Txt("$value " + t("min", "منٹ", "دقيقة"), Dj.type.labelM, modifier = Modifier.weight(1f)); DjRadio(!end && minutes == value) }
            } }
        } }
        CardRow(t("End of this surah", "اس سورۃ کے آخر میں", "نهاية هذه السورة"), trailing = { DjRadio(end) }, chevron = false, onClick = { end = true })
        SwitchRow(t("Fade out gently", "آہستہ آواز کم کریں", "تلاشي الصوت تدريجيًا"), fade, { fade = it })
        DjButton(t("Start timer", "ٹائمر شروع کریں", "بدء المؤقت"), { if (end) player.sleepAtEndOfQueue() else player.sleepAfter(minutes, fade); dismiss() }, Modifier.fillMaxWidth())
    }
}
