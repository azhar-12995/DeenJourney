package com.deenjourney.app.feature.quran

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Slider
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.deenjourney.app.core.*
import com.deenjourney.app.data.net.QuranFoundation
import com.deenjourney.app.data.net.Reciters as AudioReciters
import com.deenjourney.app.data.net.download
import com.deenjourney.app.data.net.htmlToText
import com.deenjourney.app.data.quran.*
import com.deenjourney.app.data.settings.*
import com.deenjourney.app.data.user.UserRepo
import com.deenjourney.app.data.user.nowMs
import com.deenjourney.app.design.*
import com.deenjourney.app.feature.*
import com.deenjourney.app.nav.*
import com.deenjourney.app.platform.*
import io.ktor.client.HttpClient
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import org.koin.compose.koinInject

suspend fun playAyat(ayat: List<Ayah>, settings: SettingsRepo, player: RecitationPlayer, start: Int = 0, autoplay: Boolean = true, resume: Boolean = false) {
    val s = settings.get()
    val reciter = AudioReciters.of(s.quran.reciter)
    val queue = ayat.map { a ->
        val path = AudioReciters.localPath(reciter, a.sura, a.aya)
        AudioItem(a.key, AudioReciters.everyAyahUrl(reciter, a.sura, a.aya), AudioReciters.fallbackUrl(reciter, a.id),
            path.takeIf { Platform.fileExists(it) }, QuranNames.tr[a.sura - 1] + " · " + a.key, reciter.name, a.sura, a.aya)
    }
    if (queue.isNotEmpty()) {
        val index = start.coerceIn(queue.indices)
        val last = s.lastRead?.takeIf { resume && it.sura == queue[index].sura && it.aya == queue[index].aya }
        player.load(queue, index, last?.audioMs ?: 0, autoplay)
        player.setSpeed(s.quran.speed)
        player.setRepeat(RepeatMode.entries.firstOrNull { it.name.equals(s.quran.repeat, ignoreCase = true) } ?: RepeatMode.Off)
    }
}

@Composable
fun QuranLibraryScreen(initial: Int) {
    val nav = LocalNavigator.current
    val repo = koinInject<QuranRepo>(); val settings = koinInject<SettingsRepo>(); val users = koinInject<UserRepo>()
    val s by settings.flow.collectAsState()
    val saved by users.saved().collectAsState(emptyList())
    var tab by remember { mutableStateOf(initial.coerceIn(0, 3)) }
    var readerSettings by remember { mutableStateOf(false) }
    FeaturePage(t("Quran", "قرآن", "القرآن"), Tab.Quran, actions = {
        IconBtn("bookmark", { nav.go(Bookmarks()) }); IconBtn("settings", { readerSettings = true })
    }) {
        s.lastRead?.let { last -> CardRow(t("Continue reading", "پڑھنا جاری رکھیں", "متابعة القراءة"),
            sub = "${QuranNames.tr[last.sura - 1]} · ${last.sura}:${last.aya}", glyph = "quran", fill = Dj.c.goldTint,
            onClick = { nav.go(Reader(last.sura, last.aya)) }) }
        SearchLink(t("Search surah, juz or keyword", "سورۃ، پارہ یا لفظ تلاش کریں", "ابحث عن سورة أو جزء أو كلمة"), { nav.go(QuranSearch()) })
        UTabs(listOf(t("Surah", "سورۃ", "السور"), t("Juz", "پارہ", "الأجزاء"), t("Favorites", "پسندیدہ", "المفضلة"), t("Recent", "حالیہ", "الأخيرة")), tab, { tab = it })
        Loaded(Unit, { repo.surahs() to repo.juzList() }) { (surahs, juz) ->
            when (tab) {
                1 -> juz.forEach { j -> CardRow("${j.n} · ${j.nameTr}", sub = "${j.startSura}:${j.startAya} → ${j.endSura}:${j.endAya}",
                    lead = { AyahMarker(j.n) }, trailing = { ArabicText(j.nameAr, Dj.type.arabicS) }, onClick = { nav.go(Reader(j.startSura, j.startAya)) }) }
                2 -> {
                    val favorites = saved.filter { it.type == "surah" || it.type == "ayah" }
                    if (favorites.isEmpty()) NoItems()
                    favorites.forEach { item -> CardRow(item.title, sub = item.sub, glyph = "bookmark", onClick = { nav.go(routeForQuranKey(item.key)) }) }
                }
                3 -> { if (s.lastRead == null) NoItems() else s.lastRead?.let { last -> CardRow(QuranNames.tr[last.sura - 1], sub = "${last.sura}:${last.aya}", glyph = "quran", onClick = { nav.go(Reader(last.sura, last.aya)) }) } }
                else -> surahs.forEach { surah -> CardRow(surah.nameTr, sub = "${surah.meaning} · ${surah.ayas} · ${if (surah.makki) "Makki" else "Madani"}",
                    lead = { AyahMarker(surah.id) }, trailing = { Row(verticalAlignment = Alignment.CenterVertically) { ArabicText(surah.nameAr, Dj.type.arabicS); SaveAction("surah", surah.id.toString(), surah.nameTr) } },
                    onClick = { nav.go(Reader(surah.id)) }) }
            }
        }
    }
    if (readerSettings) QuranSettingsSheet { readerSettings = false }
}

fun routeForQuranKey(key: String): Reader {
    val parts = key.split(':')
    return Reader((parts.getOrNull(0)?.toIntOrNull() ?: 1).coerceIn(1, 114), parts.getOrNull(1)?.toIntOrNull() ?: 1)
}

@Composable
fun AyahContent(a: Ayah, prefs: QuranPrefs) {
    ArabicText(a.ar, Dj.type.quranL.copy(fontSize = Dj.type.quranL.fontSize * prefs.arabicScale), modifier = Modifier.fillMaxWidth())
    if (prefs.showUrdu) {
        Txt(t("Urdu · Fateh Muhammad Jalandhry", "اردو · فتح محمد جالندھری", "الأردية · فتح محمد جالندهري"), Dj.type.labelS, Dj.c.goldText)
        ArabicText(a.ur, Dj.type.urduM.copy(fontSize = Dj.type.urduM.fontSize * prefs.translationScale))
    }
    if (prefs.showEnglish) {
        Txt(t("English · Saheeh International", "انگریزی · صحیح انٹرنیشنل", "الإنجليزية · صحيح إنترناشيونال"), Dj.type.labelS, Dj.c.goldText)
        Txt(a.en, Dj.type.bodyM.copy(fontSize = Dj.type.bodyM.fontSize * prefs.translationScale))
    }
}

@Composable
fun QuranReaderScreen(sura: Int, startAya: Int, autoplay: Boolean) {
    val nav = LocalNavigator.current
    val repo = koinInject<QuranRepo>(); val settings = koinInject<SettingsRepo>(); val users = koinInject<UserRepo>(); val player = koinInject<RecitationPlayer>()
    val s by settings.flow.collectAsState(); val ps by player.state.collectAsState()
    val highlights by users.highlights().collectAsState(emptyList())
    val scope = rememberCoroutineScope()
    var selected by remember { mutableStateOf<Ayah?>(null) }
    var showSettings by remember { mutableStateOf(false) }
    val suraId = sura.coerceIn(1, 114)
    var metadata by remember(suraId) { mutableStateOf<Surah?>(null) }
    LaunchedEffect(suraId) { metadata = repo.surah(suraId) }
    val currentPlayback by rememberUpdatedState(ps)
    Screen(top = { AppBar(QuranNames.tr[suraId - 1], sub = metadata?.let { surahSubtitle(it) }, onBack = { nav.back() }, actions = {
        IconBtn("bookmark-check", { nav.go(Bookmarks()) }); IconBtn("sliders-horizontal", { showSettings = true })
    }) }, bottom = { PlayerBar() }) {
        Loaded(suraId, { repo.ayat(suraId) }) { ayat ->
            val listState = rememberLazyListState((startAya - 1).coerceIn(0, (ayat.size - 1).coerceAtLeast(0)))
            LaunchedEffect(suraId, ayat) {
                if (autoplay || player.state.value.current == null) playAyat(ayat, settings, player, startAya - 1, autoplay, resume = true)
                snapshotFlow { listState.firstVisibleItemIndex }.distinctUntilChanged().collect { index ->
                    ayat.getOrNull(index)?.let { a -> val playback = currentPlayback; settings.update { it.copy(lastRead = LastRead(suraId, a.aya, if (playback.current?.id == a.key) playback.positionMs else 0, nowMs())) } }
                }
            }
            LaunchedEffect(ps.current?.id) {
                val a = ps.current
                if (a?.sura == suraId && ps.playing && s.quran.followAudio) listState.animateScrollToItem((a.aya - 1).coerceIn(0, (ayat.size - 1).coerceAtLeast(0)))
            }
            val visible = ayat.getOrNull(listState.firstVisibleItemIndex)
            fun goPage(delta: Int) { scope.launch { visible?.let { current -> repo.pageAyat((current.page + delta).coerceIn(1, 604)).firstOrNull()?.let { a -> if (a.sura == suraId) listState.animateScrollToItem(a.aya - 1) else nav.go(Reader(a.sura, a.aya)) } } } }
            Row(Modifier.fillMaxWidth().padding(horizontal = 18.dp, vertical = 6.dp), horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
                IconBtn("chevron-left", { goPage(-1) }, size = 40.dp, iconSize = 16.dp)
                visible?.let { Pill("${t("Juz", "پارہ", "الجزء")} ${it.juz} · ${t("Page", "صفحہ", "الصفحة")} ${it.page} · ${t("Ayah", "آیت", "الآية")} ${it.aya}") }
                IconBtn("chevron-right", { goPage(1) }, size = 40.dp, iconSize = 16.dp)
            }
            if (suraId != 1 && suraId != 9) ArabicText("بِسْمِ ٱللَّهِ ٱلرَّحْمَٰنِ ٱلرَّحِيمِ", Dj.type.quranM, Dj.c.primary, Modifier.fillMaxWidth().padding(horizontal = 18.dp), center = true)
            LazyColumn(Modifier.weight(1f), state = listState, contentPadding = PaddingValues(horizontal = 18.dp, vertical = 10.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                items(ayat, key = { it.key }) { a ->
                    val marked = highlights.firstOrNull { it.ayahKey == a.key }
                    val active = ps.current?.id == a.key && s.quran.followAudio
                    DjCard(Modifier.fillMaxWidth(), onClick = { selected = a }, radius = 18.dp, elevated = false, border = if (active) Dj.c.primarySoft else if (marked != null) Dj.c.goldSoft else null,
                        fill = if (active) Dj.c.primaryTint else if (marked != null) Dj.c.goldTint else Color.Transparent) {
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Pill(a.key)
                            if (active && ps.playing) Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) { DjIcon("volume-2", 16.dp); Txt(t("Playing", "تلاوت جاری", "قيد التشغيل"), Dj.type.labelS, Dj.c.primary) }
                        }
                        AyahContent(a, s.quran)
                        if (s.quran.wordByWord && active) WordByWord(a.key)
                    }
                }
            }
        }
    }
    if (showSettings) QuranSettingsSheet { showSettings = false }
    selected?.let { a -> AyahActionsSheet(a, { selected = null }, {
        scope.launch { playAyat(repo.ayat(a.sura), settings, player, a.aya - 1) }
    }) }
}

@Composable
fun AyahActionsSheet(a: Ayah, dismiss: () -> Unit, play: () -> Unit) {
    val nav = LocalNavigator.current; val users = koinInject<UserRepo>(); val scope = rememberCoroutineScope()
    var note by remember { mutableStateOf(false) }; var text by remember { mutableStateOf("") }
    DjSheet(dismiss) {
        SectionHeader(t("Ayah", "آیت", "الآية") + " " + a.key)
        ListRow(t("Play this ayah", "آیت سنیں", "تشغيل الآية"), lead = { IconTile("play") }, onClick = { play(); dismiss() })
        ListRow(t("Add to bookmarks", "بک مارک کریں", "إضافة للمحفوظات"), trailing = { SaveAction("ayah", a.key, a.key, a.en.take(90)) })
        ListRow(t("Add a note", "نوٹ لکھیں", "إضافة ملاحظة"), lead = { IconTile("notebook-pen") }, onClick = { note = true })
        if (note) {
            DjField(text, { text = it }, label = t("Note", "نوٹ", "ملاحظة"))
            DjButton(t("Save note", "نوٹ محفوظ کریں", "حفظ الملاحظة"), { scope.launch { users.saveNote(a.key, text.trim(), ""); dismiss() } }, enabled = text.isNotBlank())
        }
        ListRow(t("Share ayah", "آیت شیئر کریں", "مشاركة الآية"), lead = { IconTile("share-2") }, onClick = { dismiss(); nav.go(ShareAyah(a.sura, a.aya)) })
        ListRow(t("View tafsir", "تفسیر دیکھیں", "عرض التفسير"), onClick = { dismiss(); nav.go(Tafsir(a.sura, a.aya)) })
        ListRow(t("Word meanings", "لفظی معنی", "معاني الكلمات"), onClick = { dismiss(); nav.go(Tafsir(a.sura, a.aya, 1)) })
        ListRow(t("Practice memorizing this passage", "اس حصے کے حفظ کی مشق", "تدريب حفظ هذا المقطع"), onClick = { dismiss(); nav.go(Hifz(a.sura, a.aya, a.aya + 4)) })
        ListRow(t("Highlight", "نمایاں کریں", "تمييز"), onClick = { scope.launch { users.highlight(a.key, 1); dismiss() } })
        ListRow(t("Remove highlight", "نمایاں رنگ ہٹائیں", "إزالة التمييز"), onClick = { scope.launch { users.highlight(a.key, null); dismiss() } })
        ListRow(t("Copy Arabic text", "عربی متن کاپی کریں", "نسخ النص العربي"), onClick = { Platform.copyToClipboard(a.ar); dismiss() })
    }
}

@Composable
fun QuranSettingsSheet(dismiss: () -> Unit) {
    val settings = koinInject<SettingsRepo>(); val s by settings.flow.collectAsState(); val scope = rememberCoroutineScope(); val nav = LocalNavigator.current
    fun update(block: (QuranPrefs) -> QuranPrefs) { scope.launch { settings.update { it.copy(quran = block(it.quran)) } } }
    DjSheet(dismiss) {
        SectionHeader(t("Reader settings", "پڑھنے کی سیٹنگز", "إعدادات القراءة"))
        SwitchRow(t("Urdu translation", "اردو ترجمہ", "الترجمة الأردية"), s.quran.showUrdu, { v -> update { it.copy(showUrdu = v) } })
        SwitchRow(t("English translation", "انگریزی ترجمہ", "الترجمة الإنجليزية"), s.quran.showEnglish, { v -> update { it.copy(showEnglish = v) } })
        Txt(t("Arabic text size", "عربی متن کا سائز", "حجم النص العربي"), Dj.type.labelM)
        Slider(s.quran.arabicScale, { v -> update { it.copy(arabicScale = v) } }, valueRange = 0.8f..1.8f)
        Txt(t("Translation text size", "ترجمے کا سائز", "حجم الترجمة"), Dj.type.labelM)
        Slider(s.quran.translationScale, { v -> update { it.copy(translationScale = v) } }, valueRange = 0.8f..1.5f)
        CardRow(t("Reciter", "قاری", "القارئ"), sub = AudioReciters.of(s.quran.reciter).name, icon = "headphones", onClick = { dismiss(); nav.go(Reciters) })
        SwitchRow(t("Follow audio", "تلاوت کے ساتھ آگے بڑھیں", "متابعة التلاوة"), s.quran.followAudio, { v -> update { it.copy(followAudio = v) } })
        SwitchRow(t("Word-by-word under Arabic", "عربی کے نیچے لفظی معنی", "معاني الكلمات تحت العربية"), s.quran.wordByWord, { v -> update { it.copy(wordByWord = v) } })
    }
}


@Composable
fun PlayerBar() {
    val settings = koinInject<SettingsRepo>(); val scope = rememberCoroutineScope()
    val player = koinInject<RecitationPlayer>(); val p by player.state.collectAsState(); val nav = LocalNavigator.current
    if (p.current == null) Footer { DjButton(t("Choose a surah to listen", "سننے کے لیے سورۃ منتخب کریں", "اختر سورة للاستماع"), { nav.go(QuranHome()) }, Modifier.fillMaxWidth(), style = BtnStyle.Soft) }
    else Footer {
        PlaybackSeek(p, player)
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Pill("${p.speed}×", onClick = { nav.go(Player) })
            IconBtn("skip-back", { player.previous() })
            IconBtn(if (p.playing) "pause" else "play", { player.toggle() }, tint = Dj.c.onPrimary, size = 54.dp, iconSize = 24.dp, bg = Dj.c.primary)
            IconBtn("skip-forward", { player.next() })
            IconBtn(if (p.repeat == RepeatMode.One) "repeat-1" else "repeat", {
                val mode = RepeatMode.entries[(p.repeat.ordinal + 1) % RepeatMode.entries.size]
                player.setRepeat(mode)
                scope.launch { settings.update { it.copy(quran = it.quran.copy(repeat = mode.name.lowercase())) } }
            }, tint = if (p.repeat == RepeatMode.Off) Dj.c.text2 else Dj.c.primary)
        }
        if (p.error != null) NoteBox(p.error!!, tone = NoteTone.Red)
    }
}

@Composable
fun ReciterPickerScreen() {
    val settings = koinInject<SettingsRepo>(); val s by settings.flow.collectAsState(); val scope = rememberCoroutineScope(); val nav = LocalNavigator.current; val player = koinInject<RecitationPlayer>(); val repo = koinInject<QuranRepo>()
    FeaturePage(t("Reciters", "قراء", "القراء")) { AudioReciters.all.forEach { reciter ->
        CardRow(reciter.name, sub = reciter.style, icon = "headphones", trailing = { DjRadio(s.quran.reciter == reciter.id) }, onClick = {
            scope.launch {
                settings.update { it.copy(quran = it.quran.copy(reciter = reciter.id)) }
                player.state.value.current?.let { item -> if (item.sura > 0) playAyat(repo.ayat(item.sura), settings, player, item.aya - 1) }
                nav.back()
            }
        })
    } }
}

@Composable
fun QuranBookmarksScreen(initial: Int) {
    val users = koinInject<UserRepo>(); val nav = LocalNavigator.current; val scope = rememberCoroutineScope()
    val saved by users.saved().collectAsState(emptyList()); val notes by users.notes().collectAsState(emptyList()); val highlights by users.highlights().collectAsState(emptyList())
    var tab by remember { mutableStateOf(initial.coerceIn(0, 2)) }
    FeaturePage(t("Bookmarks & last read", "بک مارکس اور آخری مطالعہ", "المحفوظات وآخر قراءة")) {
        UTabs(listOf(t("Bookmarks", "بک مارکس", "المحفوظات"), t("Notes", "نوٹس", "الملاحظات"), t("Highlights", "نمایاں", "التمييز")), tab, { tab = it })
        when (tab) {
            0 -> { val list = saved.filter { it.type == "ayah" || it.type == "surah" }; if (list.isEmpty()) NoItems(); list.forEach { row -> CardRow(row.title, sub = row.sub, glyph = "bookmark", onClick = { nav.go(routeForQuranKey(row.key)) }, trailing = { SaveAction(row.type, row.key, row.title, row.sub) }) } }
            1 -> { val quranNotes = notes.filter { Regex("[0-9]+:[0-9]+").matches(it.ayahKey) }; if (quranNotes.isEmpty()) NoItems(); quranNotes.forEach { row -> CardRow(row.ayahKey, sub = row.text, icon = "notebook-pen", onClick = { nav.go(routeForQuranKey(row.ayahKey)) }, trailing = { IconBtn("trash-2", { scope.launch { users.deleteNote(row) } }) }) } }
            else -> { if (highlights.isEmpty()) NoItems(); highlights.forEach { row -> CardRow(row.ayahKey, glyph = "quran", fill = Dj.c.goldTint, onClick = { nav.go(routeForQuranKey(row.ayahKey)) }, trailing = { IconBtn("x", { scope.launch { users.highlight(row.ayahKey, null) } }) }) } }
        }
    }
}

@Composable
fun QuranTafsirScreen(sura: Int, aya: Int, initial: Int) {
    val repo = koinInject<QuranRepo>(); val remote = koinInject<QuranFoundation>(); val lang = LocalLang.current
    var tab by remember { mutableStateOf(initial) }
    FeaturePage(t("Tafsir & word meanings", "تفسیر اور لفظی معنی", "التفسير ومعاني الكلمات")) {
        UTabs(listOf(t("Tafsir", "تفسیر", "التفسير"), t("Words", "الفاظ", "الكلمات")), tab, { tab = it })
        Loaded("$sura:$aya", { repo.ayah(sura, aya) }) { a -> a?.let { ArabicText(it.ar, Dj.type.quranM); Pill(it.key) } }
        if (tab == 0) Loaded("tafsir:$sura:$aya:${lang.code}", { remote.tafsir(when (lang) { Lang.UR -> 160; Lang.AR -> 16; else -> 169 }, "$sura:$aya") ?: error("Tafsir unavailable") }) { text ->
            SectionHeader(text.source); Txt(htmlToText(text.html), Dj.type.bodyM)
        } else Loaded("words:$sura:$aya:${lang.code}", { remote.words("$sura:$aya", when (lang) { Lang.UR -> "urdu"; Lang.AR -> "arabic"; else -> "english" }).also { if (it.isEmpty()) error("Words unavailable") } }) { words ->
            words.forEach { word -> CardRow(word.gloss, trailing = { ArabicText(word.arabic, Dj.type.arabicM) }, chevron = false) }
        }
        NoteBox(t("Source: Quran.Foundation. This content needs an internet connection.", "ماخذ: Quran.Foundation۔ انٹرنیٹ درکار ہے۔", "المصدر: Quran.Foundation. يتطلب اتصالًا بالإنترنت."))
    }
}

@Composable
fun QuranSearchScreen(initial: String) {
    val repo = koinInject<QuranRepo>(); val nav = LocalNavigator.current
    var query by remember { mutableStateOf(initial) }; var tab by remember { mutableStateOf(0) }
    val scopes = listOf("all", "arabic", "urdu", "english")
    FeaturePage(t("Search Quran", "قرآن تلاش کریں", "البحث في القرآن")) {
        SearchBox(query, { query = it }, t("Search words or 2:255", "الفاظ یا 2:255 تلاش کریں", "ابحث عن كلمات أو 2:255"))
        UTabs(listOf(t("All", "سب", "الكل"), t("Arabic", "عربی", "العربية"), t("Urdu", "اردو", "الأردية"), t("English", "انگریزی", "الإنجليزية")), tab, { tab = it })
        if (query.trim().length >= 2) Loaded(query to tab, { kotlinx.coroutines.delay(250); val refs = repo.byRef(query); if (refs.isNotEmpty()) refs.map { SearchHit(it, "reference") } else repo.search(query, scopes[tab]) }) { results ->
            if (results.isEmpty()) EmptyState("empty_search", t("No results", "کوئی نتیجہ نہیں", "لا توجد نتائج"), query)
            results.forEach { hit -> CardRow("${QuranNames.tr[hit.ayah.sura - 1]} ${hit.ayah.key}", sub = hit.ayah.en, extra = { ArabicText(hit.ayah.ar, Dj.type.quranS, maxLines = 2) }, onClick = { nav.go(Reader(hit.ayah.sura, hit.ayah.aya)) }) }
        }
    }
}

private val letters = qaidaLetters.map { it.glyph }
private val letterNames = qaidaLetters.map { it.name }

@Composable
fun QaidaScreen() {
    val users = koinInject<UserRepo>(); val scope = rememberCoroutineScope(); val done by users.progress("qaida").collectAsState(emptyList())
    val settings = koinInject<SettingsRepo>(); val s by settings.flow.collectAsState()
    val player = koinInject<RecitationPlayer>()
    val audio = remember { QaidaAudio() }; val audioState by audio.state.collectAsState()
    val lifecycle = LocalLifecycleOwner.current.lifecycle
    DisposableEffect(audio, lifecycle) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_STOP) audio.stop()
        }
        lifecycle.addObserver(observer)
        onDispose { lifecycle.removeObserver(observer); audio.close() }
    }
    LaunchedEffect(s.qaidaAudioEnabled) { if (!s.qaidaAudioEnabled) audio.stop() }
    var index by remember { mutableStateOf(0) }; var tab by remember { mutableStateOf(0) }
    fun pronounce(letter: Int) { if (s.qaidaAudioEnabled) { player.pause(); audio.play(qaidaLetters[letter].audioFile) } }
    fun select(letter: Int) { index = letter; pronounce(letter) }
    FeaturePage(t("Noorani Qaida", "نورانی قاعدہ", "القاعدة النورانية")) {
        SwitchRow(t("Qaida audio", "قاعدہ آڈیو", "صوت القاعدة"), s.qaidaAudioEnabled, { on ->
            if (!on) audio.stop()
            scope.launch { settings.update { it.copy(qaidaAudioEnabled = on) } }
        }, t("Tap a letter to hear its name.", "حرف کا نام سننے کے لیے اس پر ٹیپ کریں۔", "اضغط على حرف لسماع اسمه."))
        UTabs(listOf(t("Letters", "حروف", "الحروف"), t("Practice", "مشق", "التدريب")), tab, { tab = it })
        Art("quran_rehal", Modifier.fillMaxWidth().height(100.dp))
        DjCard(Modifier.fillMaxWidth(), onClick = { pronounce(index) }) { ArabicText(letters[index], Dj.type.displayL, center = true, modifier = Modifier.fillMaxWidth()); Txt(letterNames[index], Dj.type.titleL) }
        DjButton(t("Listen", "سنیں", "استمع"), { pronounce(index) }, lead = "volume-2", enabled = s.qaidaAudioEnabled, style = BtnStyle.Soft)
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) { DjButton(t("Previous", "پچھلا", "السابق"), { select((index - 1).coerceAtLeast(0)) }, enabled = index > 0, style = BtnStyle.Soft); DjButton(t("Next", "اگلا", "التالي"), { select((index + 1).coerceAtMost(28)) }, enabled = index < 28) }
        if (tab == 0) letters.chunked(5).forEachIndexed { row, group -> Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) { group.forEachIndexed { col, letter -> DjChip(letter, index == row * 5 + col, { select(row * 5 + col) }, Modifier.weight(1f)) } } }
        else {
            ArabicText(letters[index] + "َ  " + letters[index] + "ِ  " + letters[index] + "ُ", Dj.type.quranL, center = true)
            DjButton(t("Mark practiced", "مشق مکمل", "تم التدريب"), { val key = index.toString(); scope.launch { users.setProgress("qaida", key, 1) } }, Modifier.fillMaxWidth())
        }
        DjProgress(done.count { it.value > 0 } / 29f)
        if (s.qaidaAudioEnabled && audioState == QaidaAudioState.Error) {
            NoteBox(t("Recording could not play. Tap Listen to retry.", "ریکارڈنگ نہیں چل سکی۔ دوبارہ سننے کے لیے سنیں دبائیں۔", "تعذّر تشغيل التسجيل. اضغط استمع للمحاولة مجددًا."))
        }
        NoteBox(t("Recorded voice: Mufti Mohammed Ghiyas Mohiuddin · Qamar Apps. Available offline. Practice with a teacher.", "ریکارڈ شدہ آواز: مفتی محمد غیاث محی الدین · Qamar Apps۔ انٹرنیٹ کے بغیر دستیاب۔ استاد کے ساتھ مشق کریں۔", "تسجيل المفتي محمد غياث محي الدين · Qamar Apps. متاح دون اتصال. تدرّب مع معلّم."))
        Txt("Noorani Qaida (Pakistani Edition) · CC BY-SA 4.0", Dj.type.bodyS)
        DjButton(t("Audio source & license", "آڈیو کا ماخذ اور لائسنس", "مصدر الصوت والترخيص"), { Platform.openUrl("https://www.qamarapps.com/license") }, style = BtnStyle.Secondary)

    }
}

@Composable
fun HifzScreen(sura: Int, from: Int, to: Int) {
    val repo = koinInject<QuranRepo>(); val settings = koinInject<SettingsRepo>(); val player = koinInject<RecitationPlayer>(); val users = koinInject<UserRepo>(); val scope = rememberCoroutineScope()
    val progress by users.progress("hifz").collectAsState(emptyList())
    var suraText by remember { mutableStateOf(sura.toString()) }; var fromText by remember { mutableStateOf(from.toString()) }; var toText by remember { mutableStateOf(to.toString()) }
    var selection by remember { mutableStateOf(Triple(sura, from, to)) }; var mode by remember { mutableStateOf(0) }; var index by remember(selection) { mutableStateOf(0) }; var hidden by remember(selection, index) { mutableStateOf(false) }
    FeaturePage(t("Hifz practice", "حفظ کی مشق", "تدريب الحفظ")) {
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) { DjField(suraText, { suraText = it }, Modifier.weight(1f), label = t("Surah", "سورۃ", "السورة")); DjField(fromText, { fromText = it }, Modifier.weight(1f), label = t("From", "شروع", "من")); DjField(toText, { toText = it }, Modifier.weight(1f), label = t("To", "آخر", "إلى")) }
        val sid = suraText.toIntOrNull(); val fa = fromText.toIntOrNull(); val ta = toText.toIntOrNull()
        DjButton(t("Load range", "آیات دکھائیں", "تحميل الآيات"), { if (sid != null && fa != null && ta != null) selection = Triple(sid, fa, ta) }, enabled = sid in 1..114 && fa != null && fa > 0 && ta != null && ta >= fa)
        UTabs(listOf(t("Repeat", "دہرائیں", "تكرار"), t("Hide & reveal", "چھپائیں اور دیکھیں", "إخفاء وإظهار"), t("Quiz", "کوئز", "اختبار")), mode, { mode = it; hidden = it > 0 })
        Loaded(selection, { repo.range(selection.first, selection.second, selection.third) }) { ayat ->
            if (ayat.isEmpty()) NoItems() else {
                val a = ayat[index.coerceIn(ayat.indices)]
                DjCard(Modifier.fillMaxWidth()) { Pill(a.key); if (!hidden) ArabicText(a.ar, Dj.type.quranL) else Txt(t("Recite from memory, then reveal.", "یاد سے پڑھیں، پھر دیکھیں۔", "اقرأ من الذاكرة ثم أظهر النص."), Dj.type.bodyM) }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) { DjButton(t("Play", "سنیں", "تشغيل"), { scope.launch { playAyat(listOf(a), settings, player); player.setRepeat(RepeatMode.One) } }, lead = "play"); DjButton(if (hidden) t("Reveal", "دیکھیں", "إظهار") else t("Hide", "چھپائیں", "إخفاء"), { hidden = !hidden }, style = BtnStyle.Soft) }
                DjButton(t("I know this ayah", "یہ آیت یاد ہے", "حفظت هذه الآية"), { val key = a.key; scope.launch { users.setProgress("hifz", key, 1) }; if (index < ayat.lastIndex) { index++; hidden = mode > 0 } }, Modifier.fillMaxWidth())
                Row { IconBtn("chevron-left", { index = (index - 1).coerceAtLeast(0) }); Txt("${index + 1} / ${ayat.size}", Dj.type.titleM); IconBtn("chevron-right", { index = (index + 1).coerceAtMost(ayat.lastIndex) }) }
                DjProgress(ayat.count { a2 -> progress.any { it.key == a2.key && it.value > 0 } }.toFloat() / ayat.size)
            }
        }
    }
    DisposableEffect(Unit) { onDispose { player.setRepeat(RepeatMode.Off) } }
}
