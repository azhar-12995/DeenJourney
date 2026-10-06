package com.deenjourney.app.feature.learn

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.deenjourney.app.core.*
import com.deenjourney.app.data.content.ContentRepo
import com.deenjourney.app.data.content.DailyHadith
import com.deenjourney.app.data.hadith.Collections
import com.deenjourney.app.data.hadith.HadithStore
import com.deenjourney.app.data.quran.QuranRepo
import com.deenjourney.app.data.settings.SettingsRepo
import com.deenjourney.app.data.user.UserRepo
import com.deenjourney.app.design.*
import com.deenjourney.app.feature.*
import com.deenjourney.app.nav.*
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate
import org.koin.compose.koinInject

@Composable
fun HadithDayScreen() {
    val content = koinInject<ContentRepo>(); val nav = LocalNavigator.current
    FeaturePage(t("Hadith of the day", "آج کی حدیث", "حديث اليوم")) { Loaded(today(), { content.dailyHadith().forDay(today()) }) { h ->
        if (h == null) NoItems() else {
            Art("hero_madinah", Modifier.fillMaxWidth().height(125.dp)); DailyHadithContent(h)
            DjButton(t("Read in detail", "تفصیل پڑھیں", "اقرأ التفاصيل"), { nav.go(HadithDetail(h.collection, h.number, h.id)) }, Modifier.fillMaxWidth())
        }
    } }
}

@Composable
private fun DailyHadithContent(h: DailyHadith) {
    DjCard(Modifier.fillMaxWidth()) { ArabicText(h.arabic, Dj.type.arabicL); Gap(10.dp); Txt(h.en, Dj.type.bodyM); if (h.ur.isNotBlank()) ArabicText(h.ur, Dj.type.urduM); Pill(h.grade); Txt(h.book.text(), Dj.type.labelM) }
    SectionHeader(t("Narrator", "راوی", "الراوي")); Txt(h.narrator.text(), Dj.type.bodyM)
    SectionHeader(t("Explanation", "تشریح", "الشرح")); Txt(h.explanation.text(), Dj.type.bodyM)
    SaveAction("hadith", "${h.collection}:${h.number}", h.book.text(), h.short.text())
}

@Composable
fun HadithLibraryScreen() {
    val store = koinInject<HadithStore>(); val nav = LocalNavigator.current; val scope = rememberCoroutineScope()
    val downloads by store.progress.collectAsState(); var installed by remember { mutableStateOf(emptySet<String>()) }; var error by remember { mutableStateOf<String?>(null) }; var query by remember { mutableStateOf("") }
    LaunchedEffect(Unit) { installed = store.installed() }
    FeaturePage(t("Hadith library", "حدیث کی لائبریری", "مكتبة الحديث")) {
        SearchBox(query, { query = it }, t("Search downloaded hadith", "ڈاؤن لوڈ شدہ حدیث تلاش کریں", "ابحث في الأحاديث المنزلة"))
        if (query.isNotBlank()) Loaded(query, { kotlinx.coroutines.delay(250); store.search(query) }) { rows ->
            if (rows.isEmpty()) NoItems(); rows.forEach { h -> CardRow("${Collections.of(h.collection)?.name?.text()} · ${h.number}", sub = h.en, glyph = "hadith", onClick = { nav.go(HadithDetail(h.collection, h.number)) }) }
        } else Collections.all.forEach { c ->
            CardRow(c.name.text(), sub = "${c.books} books · ${c.sizeMb} MB", glyph = "hadith", onClick = { if (c.id in installed) nav.go(HadithBooks(c.id)) }, trailing = {
                if (c.id in installed) DjIcon("check") else if (downloads[c.id] == null) IconBtn("download", { scope.launch { if (!store.install(c.id)) error = "Download failed. Check the connection and retry."; installed = store.installed() } })
            })
            downloads[c.id]?.let { DjProgress(it) }
        }
        error?.let { NoteBox(it, tone = NoteTone.Red) }
        CardRow(t("Hadith of the day", "آج کی حدیث", "حديث اليوم"), glyph = "hadith_day", onClick = { nav.go(HadithDay) })
    }
}

@Composable
fun HadithBooksScreen(collection: String) {
    val store = koinInject<HadithStore>(); val nav = LocalNavigator.current
    FeaturePage(Collections.of(collection)?.name?.text() ?: collection) { Loaded(collection, { store.books(collection) }) { books ->
        if (books.isEmpty()) NoItems(); books.forEach { b -> CardRow("${b.n} · ${b.name}", sub = "${b.first}–${b.last}", glyph = "hadith", onClick = { nav.go(HadithList(collection, b.n, b.name)) }) }
    } }
}

@Composable
fun HadithListScreen(collection: String, book: Int, title: String) {
    val store = koinInject<HadithStore>(); val nav = LocalNavigator.current
    FeaturePage(title) { Loaded(collection to book, { store.inBook(collection, book) }) { list ->
        if (list.isEmpty()) NoItems(); list.forEach { h -> CardRow("Hadith ${h.number}", sub = h.en, onClick = { nav.go(HadithDetail(collection, h.number)) }) }
    } }
}

@Composable
fun HadithDetailScreen(collection: String, number: Int, daily: String?) {
    val content = koinInject<ContentRepo>(); val store = koinInject<HadithStore>(); val users = koinInject<UserRepo>(); val scope = rememberCoroutineScope()
    var note by remember { mutableStateOf("") }; var savedNote by remember { mutableStateOf(false) }
    FeaturePage(t("Hadith detail", "حدیث کی تفصیل", "تفاصيل الحديث")) {
        Loaded(Triple(collection, number, daily), { content.dailyHadith().firstOrNull { it.id == daily || it.collection == collection && it.number == number } to store.get(collection, number) }) { (h, downloaded) ->
            when {
                h != null -> DailyHadithContent(h)
                downloaded != null -> { Pill("${collection} · ${downloaded.number} · ${downloaded.grade}"); ArabicText(downloaded.ar, Dj.type.arabicL); Txt(downloaded.en, Dj.type.bodyM); ArabicText(downloaded.ur, Dj.type.urduM); SaveAction("hadith", downloaded.key, "$collection $number") }
                else -> NoteBox(t("Download this collection from the Hadith library to read it offline.", "یہ مجموعہ حدیث لائبریری سے ڈاؤن لوڈ کریں۔", "نزّل المجموعة من مكتبة الحديث لقراءتها دون اتصال."))
            }
            val text = h?.let { it.arabic + "\n" + it.en } ?: downloaded?.let { it.ar + "\n" + it.en }
            if (text != null) {
                DjButton(t("Share", "شیئر", "مشاركة"), { Platform.shareText(text + "\n$collection $number") }, style = BtnStyle.Soft)
                DjField(note, { note = it; savedNote = false }, label = t("Personal note", "ذاتی نوٹ", "ملاحظة شخصية"))
                DjButton(t("Save note", "نوٹ محفوظ کریں", "حفظ الملاحظة"), { scope.launch { users.saveNote("hadith:$collection:$number", note.trim(), "hadith"); savedNote = true } }, enabled = note.isNotBlank())
                if (savedNote) Pill(t("Saved", "محفوظ", "تم الحفظ"))
            }
        }
    }
}

@Composable
fun ReflectionScreen() {
    val content = koinInject<ContentRepo>(); val users = koinInject<UserRepo>(); val scope = rememberCoroutineScope(); val logs by users.day(today()).collectAsState(emptyList())
    FeaturePage(t("Aaj ki achi baat", "آج کی اچھی بات", "كلمة طيبة اليوم")) { Loaded(today(), { content.reflections().forDay(today()) }) { r -> if (r != null) {
        Art("hero_sprout", Modifier.fillMaxWidth().height(170.dp)); SectionHeader(r.headline.text()); Txt(r.body.text(), Dj.type.bodyL)
        NoteBox(r.quote.text(), glyph = "heart", tone = NoteTone.Gold); References(listOf(r.ref))
        val done = logs.any { it.kind == "deed" && it.value > 0 }
        DjCard(Modifier.fillMaxWidth(), fill = Dj.c.primaryTint) { SwitchRow(r.deed.text(), done, { on -> scope.launch { users.setDaily(today(), "deed", if (on) 1 else 0) } }) }
    } else NoItems() } }
}

@Composable
fun AkhlaqScreen(topic: String? = null) {
    val content = koinInject<ContentRepo>(); val nav = LocalNavigator.current; var chosen by remember(topic) { mutableStateOf<Int?>(null) }
    FeaturePage(t("Akhlaq · good character", "اخلاق", "الأخلاق")) { Loaded(topic, { content.akhlaq() }) { list ->
        if (topic == null) list.forEach { a -> CardRow(a.title.text(), sub = a.subtitle.text(), glyph = a.glyph, onClick = { nav.go(AkhlaqTopic(a.id)) }) }
        else list.firstOrNull { it.id == topic }?.let { a ->
            SectionHeader(a.title.text()); Txt(a.body.text().replace("\\n", "\n"), Dj.type.bodyM)
            a.quran?.let { NoteBox(it.text.text(), glyph = "quran"); References(listOf(it.ref)) }; a.hadith?.let { NoteBox(it.text.text(), glyph = "hadith"); References(listOf(it.ref)) }
            a.reflect.forEach { NoteBox(it.text(), tone = NoteTone.Gold) }; a.practice.forEach { Txt("• " + it.text(), Dj.type.bodyM) }
            a.scenario?.let { sc -> SectionHeader(t("Real life scenario", "عملی صورتحال", "موقف من الحياة")); Txt(sc.q.text(), Dj.type.titleM); sc.options.forEachIndexed { i, option -> CardRow(option.text(), trailing = { DjRadio(chosen == i) }, onClick = { chosen = i }) }; chosen?.let { NoteBox(sc.feedback.text(), tone = if (it == sc.answer) NoteTone.Green else NoteTone.Gold) } }
        }
    } }
}

@Composable
fun ProphetsScreen() {
    val content = koinInject<ContentRepo>(); val nav = LocalNavigator.current; var kids by remember { mutableStateOf(false) }
    FeaturePage(t("Prophets & Seerah", "انبیاء اور سیرت", "الأنبياء والسيرة")) {
        Segmented(listOf(t("For adults", "بڑوں کے لیے", "للكبار"), t("For children", "بچوں کے لیے", "للأطفال")), if (kids) 1 else 0, { kids = it == 1 })
        Loaded(Unit, { content.prophets() }) { list -> list.forEach { p -> CardRow(p.name.text(), sub = p.summary.text(), lead = { Art("prophet_${p.vignette}", Modifier.size(64.dp)) }, onClick = { nav.go(Story(p.id, kids)) }) } }
        CardRow(t("Seerah timeline", "سیرت کی ٹائم لائن", "السيرة النبوية"), glyph = "mosque", onClick = { nav.go(Seerah) })
    }
}

@Composable
fun StoryScreen(id: String, kids: Boolean) {
    val content = koinInject<ContentRepo>(); val quran = koinInject<QuranRepo>(); val users = koinInject<UserRepo>(); val scope = rememberCoroutineScope()
    FeaturePage(t("Story reader", "کہانی", "قراءة القصة")) { Loaded(id to kids, { content.prophets().firstOrNull { it.id == id } }) { p -> if (p == null) NoItems() else {
        Art("prophet_${p.vignette}", Modifier.fillMaxWidth().height(180.dp)); SectionHeader(p.name.text()); Txt(p.summary.text(), Dj.type.bodyM)
        (if (kids) p.children else p.adults).forEach { chapter -> SectionHeader(chapter.title.text()); Txt(chapter.body.text().replace("\\n", "\n"), Dj.type.bodyM); References(chapter.refs) }
        p.quran.forEach { key -> Loaded(key, { quran.byRef(key) }) { ayat -> ayat.forEach { a -> DjCard(Modifier.fillMaxWidth()) { Pill(a.key); ArabicText(a.ar, Dj.type.quranM); Txt(a.en, Dj.type.bodyM); if (LocalLang.current == Lang.UR) ArabicText(a.ur, Dj.type.urduM) } } } }
        p.lessons.forEach { NoteBox(it.text()) }; SaveAction("story", p.id, p.name.text())
        DjButton(t("Mark as read", "مطالعہ مکمل", "تمت القراءة"), { scope.launch { users.setProgress("story", id, 1) } }, Modifier.fillMaxWidth())
    } } }
}

@Composable
fun SeerahScreen() {
    val content = koinInject<ContentRepo>()
    FeaturePage(t("Seerah timeline", "سیرت کی ٹائم لائن", "السيرة النبوية")) { Loaded(Unit, { content.seerah() }) { file ->
        Art("hero_madinah", Modifier.fillMaxWidth().height(140.dp))
        file.timeline.forEach { event -> DjCard(Modifier.fillMaxWidth()) { Pill(event.hijri.ifBlank { event.year.toString() }); Txt(event.title.text(), Dj.type.titleM); Txt(event.body.text(), Dj.type.bodyM); References(event.refs) } }
        file.chapters.forEach { chapter -> SectionHeader(chapter.title.text()); Txt(chapter.body.text(), Dj.type.bodyM); References(chapter.refs) }
    } }
}

@Composable
fun NamesScreen(number: Int? = null) {
    val content = koinInject<ContentRepo>(); val nav = LocalNavigator.current; val users = koinInject<UserRepo>(); val scope = rememberCoroutineScope(); var selected by remember { mutableStateOf(number ?: 1) }
    FeaturePage(t("99 names of Allah", "اللہ کے 99 نام", "أسماء الله الحسنى"), actions = { IconBtn("grid-2x2", { nav.go(NamesGrid) }) }) { Loaded(Unit, { content.names() }) { list ->
        if (number == null) list.chunked(2).forEach { row -> Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) { row.forEach { name -> DjCard(Modifier.weight(1f), onClick = { nav.go(Names(name.n)) }) { ArabicText(name.arabic, Dj.type.arabicM); Txt(name.translit, Dj.type.titleS); Txt(name.meaning.text(), Dj.type.bodyS) } } } }
        else list.firstOrNull { it.n == selected }?.let { name ->
            Pill("${name.n} / ${list.size}"); ArabicText(name.arabic, Dj.type.arabicL, center = true, modifier = Modifier.fillMaxWidth()); Txt(name.translit, Dj.type.headline); Txt(name.meaning.text(), Dj.type.titleM)
            Txt(name.explanation.text(), Dj.type.bodyM); NoteBox(name.reflection.text(), tone = NoteTone.Gold)
            References(name.quran.map { com.deenjourney.app.data.content.Ref("quran", "Quran $it", it) }); SaveAction("name", name.n.toString(), name.translit, name.meaning.text())
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) { DjButton(t("Previous", "پچھلا", "السابق"), { selected-- }, enabled = selected > 1, style = BtnStyle.Soft); DjButton(t("Next", "اگلا", "التالي"), { selected++ }, enabled = selected < list.size) }
            DjButton(t("Mark learned", "سیکھ لیا", "تم التعلم"), { val key = name.n.toString(); scope.launch { users.setProgress("name", key, 1) } }, Modifier.fillMaxWidth())
        }
    } }
}

@Composable
fun RoadmapScreen() {
    val content = koinInject<ContentRepo>(); val users = koinInject<UserRepo>(); val p by users.active.collectAsState(); val done by users.progress("lesson").collectAsState(emptyList()); val nav = LocalNavigator.current
    FeaturePage(t("Learning roadmap", "سیکھنے کا روڈ میپ", "خريطة التعلم")) { Art("hero_roadmap", Modifier.fillMaxWidth().height(140.dp)); Loaded(Unit, { content.lessons() }) { file -> file.tracks.forEach { track ->
        SectionHeader(track.title.text()); Txt(track.subtitle.text(), Dj.type.bodyS)
        track.lessons.filter { p.allows(it) }.forEachIndexed { index, lesson -> CardRow(lesson.title.text(), sub = "${lesson.minutes} min · ${lesson.subtitle.text()}", lead = { NumBadge(index + 1) }, trailing = { if (done.any { it.key == lesson.id && it.value > 0 }) DjIcon("circle-check") }, onClick = { nav.go(LessonRoute(lesson.id)) }) }
    } } }
}

@Composable
fun PillarsScreen() {
    val content = koinInject<ContentRepo>(); val nav = LocalNavigator.current
    FeaturePage(t("Five pillars", "اسلام کے پانچ ارکان", "أركان الإسلام الخمسة")) { Loaded(Unit, { content.pillars() }) { list -> list.forEach { p -> DjCard(Modifier.fillMaxWidth()) {
        Glyph(p.glyph, 48.dp); SectionHeader(p.title.text()); Txt(p.subtitle.text(), Dj.type.bodyS); p.arabic.takeIf { it.isNotBlank() }?.let { ArabicText(it, Dj.type.arabicM) }; Txt(p.body.text(), Dj.type.bodyM); References(p.refs); p.link?.let { link -> TextLink(t("Learn more", "مزید سیکھیں", "تعلّم المزيد"), { nav.open(link) }) }
    } } } }
}

@Composable
fun LessonScreen(id: String) {
    val content = koinInject<ContentRepo>(); val nav = LocalNavigator.current; val users = koinInject<UserRepo>(); val scope = rememberCoroutineScope()
    FeaturePage(t("Lesson", "سبق", "الدرس")) { Loaded(id, { content.lesson(id) }) { pair -> if (pair == null) NoItems() else {
        val lesson = pair.second
        Art("hero_lesson", Modifier.fillMaxWidth().height(150.dp)); Pill("${lesson.minutes} min"); SectionHeader(lesson.title.text()); Txt(lesson.subtitle.text(), Dj.type.bodyS)
        lesson.points.forEachIndexed { index, point -> ListRow(point.text(), lead = { NumBadge(index + 1) }) }; Txt(lesson.body.text().replace("\\n", "\n"), Dj.type.bodyM); References(lesson.refs); SaveAction("lesson", id, lesson.title.text())
        DjButton(if (lesson.quiz.isNotEmpty()) t("Start quiz", "کوئز شروع", "ابدأ الاختبار") else t("Complete lesson", "سبق مکمل", "إكمال الدرس"), {
            if (lesson.quiz.isNotEmpty()) nav.go(QuizRoute(id)) else scope.launch { users.setProgress("lesson", id, 1); users.addDaily(today(), "lesson", 1); nav.go(LessonDone(id, 0, 0)) }
        }, Modifier.fillMaxWidth())
    } } }
}

@Composable
fun QuizScreen(id: String) {
    val content = koinInject<ContentRepo>(); val users = koinInject<UserRepo>(); val scope = rememberCoroutineScope(); val nav = LocalNavigator.current
    var index by remember(id) { mutableStateOf(0) }; var choice by remember(id, index) { mutableStateOf<Int?>(null) }; var checked by remember(id, index) { mutableStateOf(false) }; var score by remember(id) { mutableStateOf(0) }; var busy by remember { mutableStateOf(false) }
    FeaturePage(t("Lesson quiz", "سبق کا کوئز", "اختبار الدرس")) { Loaded(id, { content.lesson(id)?.second }) { lesson ->
        val quiz = lesson?.quiz.orEmpty()
        if (quiz.isEmpty()) NoItems() else quiz.getOrNull(index)?.let { question ->
            Pill("${index + 1}/${quiz.size}"); DjProgress((index + 1f) / quiz.size); Txt(question.q.text(), Dj.type.titleL)
            question.options.forEachIndexed { i, option -> CardRow(option.text(), trailing = { DjRadio(choice == i) }, onClick = { if (!checked) choice = i }, fill = if (checked && i == question.answer) Dj.c.primaryTint else Dj.c.surface) }
            if (checked) NoteBox(question.explain.text(), tone = if (choice == question.answer) NoteTone.Green else NoteTone.Gold)
            DjButton(if (!checked) t("Check answer", "جواب چیک", "تحقق من الإجابة") else t("Continue", "جاری رکھیں", "متابعة"), {
                if (!checked) { if (choice == question.answer) score++; checked = true }
                else if (index < quiz.lastIndex) index++
                else { busy = true; scope.launch { users.setProgress("lesson", id, 1, "$score/${quiz.size}"); users.addDaily(today(), "lesson", 1); nav.go(LessonDone(id, score, quiz.size)); busy = false } }
            }, Modifier.fillMaxWidth(), enabled = choice != null && !busy)
        }
    } }
}

@Composable
fun LessonDoneScreen(id: String, score: Int, total: Int) {
    val nav = LocalNavigator.current
    FeaturePage(t("Lesson complete", "سبق مکمل", "اكتمل الدرس")) {
        Glyph("medal", 100.dp); Txt(t("MashaAllah!", "ماشاءاللہ!", "ما شاء الله!"), Dj.type.headline)
        if (total > 0) StatBox("$score/$total", t("Quiz score", "کوئز کا نتیجہ", "نتيجة الاختبار"))
        NoteBox(t("Your progress is saved for your active family profile.", "پیش رفت موجودہ فیملی پروفائل میں محفوظ ہے۔", "حُفظ تقدمك في ملف العائلة النشط."))
        DjButton(t("Continue learning", "سیکھنا جاری رکھیں", "تابع التعلم"), { nav.go(Roadmap) }, Modifier.fillMaxWidth())
        DjButton(t("Review lesson", "سبق دوبارہ پڑھیں", "راجع الدرس"), { nav.go(LessonRoute(id)) }, Modifier.fillMaxWidth(), style = BtnStyle.Soft)
    }
}

@Composable
fun ProgressScreen() {
    val content = koinInject<ContentRepo>(); val users = koinInject<UserRepo>(); val settings = koinInject<SettingsRepo>(); val s by settings.flow.collectAsState(); val scope = rememberCoroutineScope(); val nav = LocalNavigator.current
    val progress by users.allProgress().collectAsState(emptyList()); val logs by users.daily("lesson", LocalDate.fromEpochDays(today().toEpochDays() - 30)).collectAsState(emptyList()); var tab by remember { mutableStateOf(0) }
    FeaturePage(t("My learning journey", "میرا سیکھنے کا سفر", "رحلتي التعليمية")) {
        UTabs(listOf(t("Progress", "پیش رفت", "التقدم"), t("Achievements", "کامیابیاں", "الإنجازات"), t("Reminders", "یاد دہانی", "التذكيرات")), tab, { tab = it })
        Loaded(Unit, { content.allLessons() }) { lessons ->
            val count = progress.count { it.kind == "lesson" && it.value > 0 }
            Ring(count.toFloat() / lessons.size.coerceAtLeast(1), 100.dp); StatBox("$count/${lessons.size}", t("Lessons complete", "مکمل اسباق", "الدروس المكتملة"))
            when (tab) {
                1 -> { listOf(1 to "First lesson", 5 to "Five lessons", 10 to "Ten lessons", 40 to "Complete roadmap").forEach { (threshold, label) -> CardRow(label, glyph = "medal", chevron = false, trailing = { if (count >= threshold) DjIcon("circle-check") else DjIcon("lock") }) }; StatBox(logs.count { it.value > 0 }.toString(), t("Learning days this month", "اس مہینے کے تعلیمی دن", "أيام التعلم هذا الشهر")) }
                2 -> { SwitchRow(t("Daily lesson reminder", "روزانہ سبق کی یاد دہانی", "تذكير يومي بالدرس"), s.lessonReminder, { on -> scope.launch { settings.update { it.copy(lessonReminder = on) }; com.deenjourney.app.platform.Scheduler.reschedule() } }); var time by remember(s.lessonReminderTime) { mutableStateOf(s.lessonReminderTime) }; DjField(time, { time = it }, label = "HH:mm"); DjButton(t("Save time", "وقت محفوظ", "حفظ الوقت"), { scope.launch { settings.update { it.copy(lessonReminderTime = time) }; com.deenjourney.app.platform.Scheduler.reschedule() } }, enabled = Regex("([01]\\d|2[0-3]):[0-5]\\d").matches(time)) }
                else -> { listOf("name" to 99, "qaida" to 29).forEach { (kind, total) -> StatBox("${progress.count { it.kind == kind && it.value > 0 }}/$total", kind) }; CardRow(t("Continue roadmap", "روڈ میپ جاری رکھیں", "تابع خريطة التعلم"), glyph = "roadmap", onClick = { nav.go(Roadmap) }) }
            }
        }
    }
}
