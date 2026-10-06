package com.deenjourney.app.feature.home

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.deenjourney.app.core.*
import com.deenjourney.app.data.auth.AuthRepo
import com.deenjourney.app.data.content.ContentRepo
import com.deenjourney.app.data.content.DailyHadith
import com.deenjourney.app.data.content.Lesson
import com.deenjourney.app.data.content.Reflection
import com.deenjourney.app.data.content.Track
import com.deenjourney.app.data.hadith.HadithItem
import com.deenjourney.app.data.hadith.HadithStore
import com.deenjourney.app.data.hadith.Collections
import com.deenjourney.app.data.hijri.Hijri
import com.deenjourney.app.data.prayer.PrayerClock
import com.deenjourney.app.data.prayer.PrayerName
import com.deenjourney.app.data.prayer.PrayerNow
import com.deenjourney.app.data.quran.QuranRepo
import com.deenjourney.app.data.quran.SearchHit
import com.deenjourney.app.data.settings.SettingsRepo
import com.deenjourney.app.data.user.InboxE
import com.deenjourney.app.data.user.ProfileE
import com.deenjourney.app.data.user.ProgressE
import com.deenjourney.app.data.user.UserRepo
import com.deenjourney.app.design.*
import com.deenjourney.app.feature.allows
import com.deenjourney.app.feature.family.KidsHomeContent
import com.deenjourney.app.feature.family.SwitchProfileSheet
import com.deenjourney.app.feature.forDay
import com.deenjourney.app.feature.relative
import com.deenjourney.app.feature.today
import com.deenjourney.app.nav.*
import com.deenjourney.app.nav.R
import com.deenjourney.app.platform.AlarmPlanner
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.datetime.toLocalDateTime
import org.koin.compose.koinInject

// ---------------------------------------------------------------- shared bits

/** The next lesson for a profile: first not-yet-completed lesson (roadmap order) that suits its age. */
data class NextLesson(val track: Track, val lesson: Lesson, val index: Int, val doneInTrack: Int)

fun nextLesson(tracks: List<Track>, done: Set<String>, p: ProfileE?): NextLesson? {
    for (tr in tracks) {
        val ls = tr.lessons.filter { p.allows(it) }
        val i = ls.indexOfFirst { it.id !in done }
        if (i >= 0) return NextLesson(tr, ls[i], i, ls.count { it.id in done })
    }
    return null
}

@Composable
fun rememberPrayerNow(): PrayerNow? {
    val clock = koinInject<PrayerClock>()
    val flow = remember(clock) { clock.live() }
    val pn by flow.collectAsState(null)
    return pn
}

@Composable
fun Wordmark() = Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
    Art("logo_mark", Modifier.size(32.dp), contentScale = ContentScale.Fit)
    Txt(t("Deen Journey", "دین جرنی", "رحلة الدين"), Dj.type.titleL, Dj.c.primary, maxLines = 1)
}

@Composable
fun prayerLabel(p: PrayerName) = AlarmPlanner.prayerName(p, LocalLang.current)

// ---------------------------------------------------------------- C01 Home

@Composable
fun HomeScreen() {
    val users = koinInject<UserRepo>()
    val active by users.active.collectAsState()
    var sheet by remember { mutableStateOf(false) }
    val p = active
    if (p?.childMode == true) Screen { KidsHomeContent(p) { sheet = true } }
    else AdultHome(p) { sheet = true }
    if (sheet) SwitchProfileSheet { sheet = false }
}

@Composable
fun KidsHomeScreen() {
    val nav = LocalNavigator.current
    val users = koinInject<UserRepo>()
    val profiles by users.profiles.collectAsState()
    val active by users.active.collectAsState()
    var sheet by remember { mutableStateOf(false) }
    val p = active?.takeIf { it.childMode } ?: profiles.firstOrNull { it.childMode }
    if (p == null) Screen(top = { AppBar(t("Kids", "بچے", "الأطفال"), onBack = { nav.back() }) }) {
        EmptyState("empty_search", t("No child profile yet", "ابھی کوئی بچے کی پروفائل نہیں", "لا يوجد ملف طفل بعد"), t("Add a child to open their own learning space.", "بچے کی پروفائل بنائیں تاکہ ان کا اپنا سیکھنے کا حصہ کھلے۔", "أضف طفلاً لفتح مساحة تعلم خاصة به."),
            primary = t("Add a child", "بچہ شامل کریں", "إضافة طفل"), primaryIcon = "user-plus", onPrimary = { nav.go(AddMember("child")) })
    } else Screen { KidsHomeContent(p) { sheet = true } }
    if (sheet) SwitchProfileSheet { sheet = false }
}

@Composable
private fun AdultHome(p: ProfileE?, onSwitch: () -> Unit) {
    val nav = LocalNavigator.current
    val lang = LocalLang.current
    val users = koinInject<UserRepo>(); val content = koinInject<ContentRepo>(); val quran = koinInject<QuranRepo>(); val settings = koinInject<SettingsRepo>()
    val s by settings.flow.collectAsState()
    val unread by users.unread.collectAsState(0)
    val pn = rememberPrayerNow()
    val progress by users.progress("lesson").collectAsState(emptyList())
    var tracks by remember { mutableStateOf(emptyList<Track>()) }
    var hadith by remember { mutableStateOf<DailyHadith?>(null) }
    var deed by remember { mutableStateOf<Reflection?>(null) }
    var surahName by remember { mutableStateOf("") }
    val d = today()
    LaunchedEffect(d) {
        tracks = content.lessons().tracks
        hadith = content.dailyHadith().forDay(d)
        deed = content.reflections().forDay(d)
    }
    val last = s.lastRead
    LaunchedEffect(last?.sura) { surahName = quran.surah(last?.sura ?: 1).nameTr }
    val next = remember(tracks, progress, p?.id) { nextLesson(tracks, progress.filter { it.value > 0 }.map { it.key }.toSet(), p) }

    Screen(bottom = { BottomNav(Tab.Home) { nav.tab(it) } }) {
        Row(Modifier.fillMaxWidth().padding(start = 18.dp, end = 14.dp, top = 4.dp, bottom = 6.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            Box(Modifier.weight(1f)) { Wordmark() }
            IconBtn("search", { nav.go(Search()) }, description = t("Search", "تلاش", "بحث"))
            IconBtn("bell", { nav.go(Notifications) }, tint = Dj.c.primary, badge = unread > 0, description = t("Notifications", "اطلاعات", "الإشعارات"))
            Box(Modifier.clip(CircleShape).clickable(onClick = onSwitch).padding(3.dp)) { Avatar(p?.avatar ?: "man", 34.dp, ring = Dj.c.gold) }
        }
        Body(gap = 12.dp, padding = PaddingValues(start = 18.dp, end = 18.dp, top = 2.dp, bottom = 18.dp)) {
            Column {
                Txt(t("Assalamu alaikum", "السلام علیکم", "السلام عليكم") + (p?.name?.let { lang.pick(", $it", "، $it", " يا $it") } ?: ""), Dj.type.titleM)
                pn?.let { Txt(Hijri.format(it.hijri, lang), Dj.type.bodyS, Dj.c.goldText) }
            }
            NextPrayerCard(pn)
            // continue Quran
            Row(
                Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).background(Dj.c.mintGradient).border(1.dp, Dj.c.primarySoft, RoundedCornerShape(16.dp))
                    .clickable { nav.go(Reader(last?.sura ?: 1, last?.aya ?: 1)) }.padding(start = 12.dp, end = 14.dp, top = 12.dp, bottom = 12.dp),
                verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Art("quran_rehal", Modifier.size(66.dp, 54.dp), contentScale = ContentScale.Fit)
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(1.dp)) {
                    Txt(if (last != null) t("Continue Quran", "قرآن جاری رکھیں", "متابعة القرآن") else t("Start reading the Quran", "قرآن پڑھنا شروع کریں", "ابدأ قراءة القرآن"), Dj.type.titleM)
                    Txt(t("Surah $surahName · Ayah ${last?.aya ?: 1}", "سورۃ $surahName · آیت ${lang.num(last?.aya ?: 1)}", "سورة $surahName · الآية ${lang.num(last?.aya ?: 1)}"), Dj.type.labelM, Dj.c.primary)
                    if (last != null && last.at > 0) Txt(t("Last read ", "آخری بار ", "آخر قراءة ") + lang.relative(last.at).lowercase(), Dj.type.caption, Dj.c.text2)
                }
                IconBtn("play", { nav.go(Reader(last?.sura ?: 1, last?.aya ?: 1, play = true)) }, bg = Dj.c.primary, tint = Dj.c.onPrimary, size = 36.dp, iconSize = 16.dp, description = t("Play", "چلائیں", "تشغيل"))
            }
            // today's lesson
            next?.let { n ->
                val count = n.track.lessons.count { p.allows(it) }.coerceAtLeast(1)
                CardRow(
                    t("Today’s lesson · ", "آج کا سبق · ", "درس اليوم · ") + n.track.title.text(),
                    sub = n.lesson.title.text() + " · " + t("${n.lesson.minutes} min", "${n.lesson.minutes} منٹ", "${lang.num(n.lesson.minutes)} دقائق"),
                    lead = { Art("hero_sprout", Modifier.size(66.dp, 54.dp).clip(RoundedCornerShape(12.dp))) },
                    extra = { Gap(4.dp); DjProgress(n.doneInTrack.toFloat() / count, Modifier.fillMaxWidth(0.8f), height = 5.dp) },
                    onClick = { nav.go(LessonRoute(n.lesson.id)) },
                )
            }
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.height(IntrinsicSize.Max)) {
                MiniCard("hadith", Tone.Gold, t("Daily hadith", "روزانہ حدیث", "حديث اليوم"), hadith?.short?.text() ?: "…", Modifier.weight(1f).fillMaxHeight()) { nav.go(HadithDay) }
                MiniCard("heart", Tone.Rose, t("Good deed today", "آج کی نیکی", "عمل صالح اليوم"), deed?.deed?.text() ?: "…", Modifier.weight(1f).fillMaxHeight(), fill = Dj.c.roseTint) { nav.go(AajKiBaat) }
            }
        }
    }
}

@Composable
private fun MiniCard(glyph: String, tone: Tone, title: String, sub: String, modifier: Modifier, fill: Color = Dj.c.surface, onClick: () -> Unit) {
    Column(modifier.clip(RoundedCornerShape(16.dp)).background(fill).border(1.dp, Dj.c.border, RoundedCornerShape(16.dp)).clickable(onClick = onClick).padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) { GlyphTile(glyph, 36.dp, tone); Txt(title, Dj.type.titleS, modifier = Modifier.weight(1f)) }
        Txt(sub, Dj.type.bodyS, Dj.c.text2, maxLines = 3)
    }
}

@Composable
fun NextPrayerCard(pn: PrayerNow?) {
    val nav = LocalNavigator.current
    val lang = LocalLang.current
    val loc = pn?.settings?.location
    val shape = RoundedCornerShape(18.dp)
    if (pn != null && loc == null) {
        DjCard(Modifier.fillMaxWidth(), onClick = { nav.go(PrayerSetup(true)) }, radius = 18.dp) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                GlyphTile("prayer_time", 48.dp)
                Column(Modifier.weight(1f)) {
                    Txt(t("Set your location", "اپنا مقام منتخب کریں", "حدد موقعك"), Dj.type.titleM)
                    Txt(t("Prayer times, adhan and Qibla need your city.", "نماز کے اوقات، اذان اور قبلہ کے لیے شہر ضروری ہے۔", "مواقيت الصلاة والأذان والقبلة تحتاج مدينتك."), Dj.type.bodyS, Dj.c.text2)
                }
                DjIcon("chevron-right", 18.dp, Dj.c.text3)
            }
        }
        return
    }
    val bg = if (Dj.c.isDark) Color(0xFF1E2A24) else Color(0xFFFBF1DA)
    val period = pn?.period ?: "day"
    Column(Modifier.fillMaxWidth().clip(shape).border(1.dp, Dj.c.border, shape).clickable { nav.go(PrayerTimes) }) {
        Box(Modifier.fillMaxWidth().height(112.dp).background(bg)) {
            Art("skyline_$period", Modifier.align(Alignment.BottomEnd).fillMaxWidth(0.68f).height(104.dp), contentScale = ContentScale.Fit, alignment = Alignment.BottomEnd)
            Box(Modifier.fillMaxHeight().fillMaxWidth(0.6f).background(Brush.horizontalGradient(listOf(bg, bg, bg.copy(alpha = 0f)))))
            Column(Modifier.padding(start = 16.dp, top = 10.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Glyph(when (pn?.next) { PrayerName.Fajr -> "fajr"; PrayerName.Sunrise -> "sunrise"; PrayerName.Maghrib -> "sunset"; PrayerName.Isha -> "isha"; PrayerName.Asr -> "sun_low"; else -> "sun" }, 22.dp)
                    Txt(t("Next prayer", "اگلی نماز", "الصلاة القادمة"), Dj.type.labelS, Dj.c.text2)
                }
                Txt(pn?.next?.let { prayerLabel(it) } ?: "—", Dj.type.headline)
                androidx.compose.runtime.CompositionLocalProvider(androidx.compose.ui.platform.LocalLayoutDirection provides androidx.compose.ui.unit.LayoutDirection.Ltr) {
                    Txt(pn?.let { lang.countdown(it.secondsToNext) } ?: "", Dj.type.numberL, Dj.c.primary)
                }
                Txt(t("remaining", "باقی", "متبقٍ") + (loc?.name?.let { " · $it" } ?: ""), Dj.type.caption, Dj.c.text2, maxLines = 1)
            }
            pn?.let { Pill(lang.date(it.today, withYear = false), Modifier.align(Alignment.TopEnd).padding(10.dp), icon = "calendar", fill = Dj.c.surface, color = Dj.c.text2) }
        }
        Row(Modifier.fillMaxWidth().background(Dj.c.surface).padding(start = 6.dp, end = 6.dp, top = 10.dp, bottom = 8.dp)) {
            listOf(PrayerName.Fajr, PrayerName.Dhuhr, PrayerName.Asr, PrayerName.Maghrib, PrayerName.Isha).forEach { pr ->
                val on = pr == pn?.next
                Column(Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Txt(prayerLabel(pr), Dj.type.caption, if (on) Dj.c.primary else Dj.c.text2, maxLines = 1)
                    Txt(pn?.times?.let { lang.timeShort(it.local(pr), Platform.is24Hour()) } ?: "--:--", if (on) Dj.type.titleS else Dj.type.labelM, if (on) Dj.c.primary else Dj.c.text, maxLines = 1)
                    Box(Modifier.size(26.dp, 2.5.dp).clip(RoundedCornerShape(2.dp)).background(if (on) Dj.c.primary else Color.Transparent))
                }
            }
        }
    }
}

// ---------------------------------------------------------------- C02 Notifications

private fun inboxGroup(kind: String) = when (kind) {
    "prayer", "adhan", "pre", "jumuah" -> 1
    "lesson", "adhkar", "event", "download" -> 2
    "family", "sync" -> 3
    else -> 0
}

private fun inboxGlyph(kind: String) = when (kind) {
    "prayer", "adhan", "pre" -> "prayer_time"; "jumuah" -> "mosque"; "lesson" -> "lesson"; "adhkar" -> "adhkar"; "event" -> "telescope"
    "download" -> "download"; "family" -> "family"; "sync" -> "sync"; else -> "bell"
}

@Composable
fun NotificationsScreen() {
    val nav = LocalNavigator.current
    val lang = LocalLang.current
    val users = koinInject<UserRepo>()
    val scope = rememberCoroutineScope()
    val list by users.inbox.collectAsState(emptyList())
    var filter by remember { mutableStateOf(0) }
    var confirmClear by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { delay(1200); users.dao.markAllRead() }
    val shown = list.filter { filter == 0 || inboxGroup(it.kind) == filter }
    val d = today()
    val (todayList, earlier) = shown.partition { kotlin.time.Instant.fromEpochMilliseconds(it.at).toLocalDateTime(kotlinx.datetime.TimeZone.currentSystemDefault()).date == d }
    Screen(top = {
        AppBar(t("Notifications", "اطلاعات", "الإشعارات"), onBack = { nav.back() }, actions = {
            if (list.isNotEmpty()) IconBtn("trash-2", { confirmClear = true }, tint = Dj.c.text, description = t("Clear all", "سب صاف کریں", "مسح الكل"))
            IconBtn("settings", { nav.go(PrayerSettings) }, tint = Dj.c.text, description = t("Settings", "سیٹنگز", "الإعدادات"))
        })
    }) {
        Body(gap = 6.dp) {
            ChipRow {
                listOf(t("All", "سب", "الكل"), t("Prayer", "نماز", "الصلاة"), t("Learning", "سیکھنا", "التعلم"), t("Family", "فیملی", "العائلة")).forEachIndexed { i, l -> DjChip(l, filter == i, { filter = i }) }
            }
            if (shown.isEmpty()) EmptyState("empty_bookmarks", t("You’re all caught up", "کوئی نئی اطلاع نہیں", "لا توجد إشعارات جديدة"),
                t("Adhan, lesson reminders and family updates will appear here.", "اذان، اسباق کی یاد دہانیاں اور فیملی اپ ڈیٹس یہاں آئیں گی۔", "ستظهر هنا تنبيهات الأذان والدروس وتحديثات العائلة."),
                primary = t("Notification settings", "اطلاعات کی سیٹنگز", "إعدادات الإشعارات"), onPrimary = { nav.go(PrayerSettings) })
            if (todayList.isNotEmpty()) { Overline(t("Today", "آج", "اليوم")); todayList.forEach { InboxRow(it, lang) } }
            if (earlier.isNotEmpty()) { if (todayList.isNotEmpty()) Hr(); Overline(t("Earlier", "پہلے", "سابقًا")); earlier.forEach { InboxRow(it, lang) } }
        }
    }
    if (confirmClear) ConfirmDialog(t("Clear notifications?", "اطلاعات صاف کریں؟", "مسح الإشعارات؟"), t("This removes them from this device only.", "یہ صرف اس ڈیوائس سے ہٹیں گی۔", "سيتم حذفها من هذا الجهاز فقط."),
        t("Clear", "صاف کریں", "مسح"), { scope.launch { list.forEach { users.dao.deleteInbox(it.id) } }; confirmClear = false }, { confirmClear = false }, danger = true)
}

@Composable
private fun InboxRow(i: InboxE, lang: Lang) {
    val nav = LocalNavigator.current
    val users = koinInject<UserRepo>()
    val scope = rememberCoroutineScope()
    ListRow(i.title, sub = i.body, lead = { GlyphTile(inboxGlyph(i.kind), 42.dp, if (inboxGroup(i.kind) == 2) Tone.Green else Tone.Gold) },
        trailing = {
            Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Txt(lang.relative(i.at, todayWord = false), Dj.type.caption, Dj.c.text3)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (!i.read) Box(Modifier.size(8.dp).clip(CircleShape).background(Dj.c.primary))
                    IconBtn("x", { scope.launch { users.dao.deleteInbox(i.id) } }, size = 28.dp, iconSize = 14.dp, tint = Dj.c.text3, description = t("Remove", "ہٹائیں", "إزالة"))
                }
            }
        },
        padding = PaddingValues(vertical = 8.dp), onClick = { i.link?.let { nav.open(it) } })
}

// ---------------------------------------------------------------- C04 Learn hub

@Composable
fun LearnHubScreen() {
    val nav = LocalNavigator.current
    val lang = LocalLang.current
    val users = koinInject<UserRepo>(); val content = koinInject<ContentRepo>()
    val p by users.active.collectAsState()
    val progress by users.progress("lesson").collectAsState(emptyList())
    var tracks by remember { mutableStateOf(emptyList<Track>()) }
    LaunchedEffect(Unit) { tracks = content.lessons().tracks }
    val done = remember(progress) { progress.filter { it.value > 0 }.map { it.key }.toSet() }
    val next = remember(tracks, done, p?.id) { nextLesson(tracks, done, p) }
    @Composable
    fun RowScope.tile(g: String, title: String, sub: String, r: R) = Column(
        Modifier.weight(1f).heightIn(min = 112.dp).clip(RoundedCornerShape(16.dp)).background(Dj.c.surface).border(1.dp, Dj.c.border, RoundedCornerShape(16.dp)).clickable { nav.go(r) }.padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) { GlyphTile(g, 40.dp); Txt(title, Dj.type.titleS, maxLines = 1); Txt(sub, Dj.type.caption, Dj.c.text2, maxLines = 1) }
    Screen(top = { AppBar(t("Learn", "سیکھیں", "تعلّم"), big = true, actions = { IconBtn("search", { nav.go(Search()) }) }) }, bottom = { BottomNav(Tab.Learn) { nav.tab(it) } }) {
        Body(gap = 12.dp) {
            if (next != null) {
                val count = next.track.lessons.count { p.allows(it) }.coerceAtLeast(1)
                Row(Modifier.fillMaxWidth().clip(RoundedCornerShape(18.dp)).background(Dj.c.greenGradient).clickable { nav.go(LessonRoute(next.lesson.id)) }.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Txt(t("Lesson ${next.index + 1} of $count · ", "سبق ${lang.num(next.index + 1)} از ${lang.num(count)} · ", "الدرس ${lang.num(next.index + 1)} من ${lang.num(count)} · ") + next.track.title.text(), Dj.type.labelS, Color(0xFFE8D5A6), maxLines = 1)
                        Txt(next.lesson.title.text(), Dj.type.titleM, Color.White)
                        DjProgress(next.doneInTrack.toFloat() / count, Modifier.fillMaxWidth(0.8f), height = 5.dp, color = Color(0xFFE8C77A), track = Color(0xFF2E8060))
                        Txt(t("${next.lesson.minutes} min", "${next.lesson.minutes} منٹ", "${lang.num(next.lesson.minutes)} دقائق"), Dj.type.caption, Color(0xFFE4EFE6))
                    }
                    Box(Modifier.size(44.dp).clip(CircleShape).background(Color.White.copy(alpha = 0.16f)), contentAlignment = Alignment.Center) { DjIcon("play", 20.dp, Color.White) }
                }
            } else if (tracks.isNotEmpty()) NoteBox(t("MashaAllah — you have completed every lesson!", "ماشاءاللہ — آپ نے تمام اسباق مکمل کر لیے!", "ما شاء الله — أكملت جميع الدروس!"), glyph = "medal", tone = NoteTone.Gold)
            SectionHeader(t("Your roadmap", "آپ کا روڈ میپ", "خريطة طريقك"), action = t("View", "دیکھیں", "عرض"), onAction = { nav.go(Roadmap) })
            Row(Modifier.fillMaxWidth().clickable { nav.go(Roadmap) }, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                tracks.forEachIndexed { i, tr ->
                    val ls = tr.lessons.filter { p.allows(it) }
                    val started = ls.any { it.id in done }
                    Column(Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        NumBadge(i + 1, 32.dp, fill = if (started) Dj.c.primary else Dj.c.goldTint, color = if (started) Dj.c.onPrimary else Dj.c.goldText)
                        Txt(tr.title.text(), Dj.type.caption, if (started) Dj.c.text else Dj.c.text2, align = TextAlign.Center, maxLines = 2)
                    }
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                tile("hadith", t("Hadith", "حدیث", "الحديث"), t("Daily & six books", "روزانہ اور صحاح ستہ", "اليومي والكتب الستة"), HadithLibrary)
                tile("akhlaq", t("Akhlaq", "اخلاق", "الأخلاق"), t("Good character", "اچھے اخلاق", "حسن الخلق"), Akhlaq)
                tile("prophets", t("Prophets", "انبیاء", "الأنبياء"), t("Stories & Seerah", "قصے اور سیرت", "القصص والسيرة"), Prophets)
            }
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                tile("names", t("99 Names", "اسمائے حسنیٰ", "الأسماء الحسنى"), "Asma ul Husna", Names())
                tile("pillars", t("5 Pillars", "ارکانِ اسلام", "أركان الإسلام"), t("Foundations", "بنیادیں", "الأسس"), Pillars)
                tile("heart", t("Achi baat", "اچھی بات", "كلمة طيبة"), t("Daily reflection", "روزانہ سوچ", "تأمل يومي"), AajKiBaat)
            }
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                tile("child", t("Kids corner", "بچوں کا گوشہ", "ركن الأطفال"), t("Ages 2–12", "عمر 2–12", "من 2 إلى 12"), KidsHome)
                tile("progress", t("Progress", "پیش رفت", "التقدم"), t("Streaks, badges", "تسلسل، بیجز", "السلاسل والشارات"), Progress)
                tile("qaida", t("Qaida", "قاعدہ", "القاعدة"), t("Learn to read", "پڑھنا سیکھیں", "تعلم القراءة"), Qaida)
            }
        }
    }
}

// ---------------------------------------------------------------- C05 Worship hub

@Composable
fun WorshipHubScreen() {
    val nav = LocalNavigator.current
    val lang = LocalLang.current
    val users = koinInject<UserRepo>(); val content = koinInject<ContentRepo>()
    val pn = rememberPrayerNow()
    val d = today()
    val morning = pn?.times?.let { pn.now < it.at.getValue(PrayerName.Asr) } ?: true
    val kind = if (morning) "adhkar_m" else "adhkar_e"
    val doneToday by users.daily(kind, d).collectAsState(emptyList())
    var total by remember { mutableStateOf(0) }
    LaunchedEffect(morning) { val a = content.adhkar(); total = if (morning) a.morning.size else a.evening.size }
    val done = doneToday.firstOrNull { it.date == d.toString() }?.value ?: 0
    @Composable
    fun RowScope.tile(g: String, title: String, r: R) = Column(
        Modifier.weight(1f).clip(RoundedCornerShape(16.dp)).background(Dj.c.surface).border(1.dp, Dj.c.border, RoundedCornerShape(16.dp)).clickable { nav.go(r) }.padding(vertical = 12.dp, horizontal = 6.dp),
        horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp),
    ) { GlyphTile(g, 46.dp); Txt(title, Dj.type.labelM, align = TextAlign.Center, maxLines = 1) }
    Screen(top = { AppBar(t("Worship", "عبادت", "العبادة"), big = true, actions = { IconBtn("compass", { nav.go(Qibla()) }, description = t("Qibla", "قبلہ", "القبلة")) }) }, bottom = { BottomNav(Tab.Worship) { nav.tab(it) } }) {
        Body(gap = 10.dp) {
            Row(Modifier.fillMaxWidth().clip(RoundedCornerShape(18.dp)).background(Dj.c.goldGradient).border(1.dp, Dj.c.border, RoundedCornerShape(18.dp))
                .clickable { nav.go(if (pn?.settings?.location == null) PrayerSetup(true) else PrayerTimes) }.padding(horizontal = 14.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Glyph("sun", 40.dp)
                Column(Modifier.weight(1f)) {
                    if (pn?.times == null) {
                        Txt(t("Set your location", "اپنا مقام منتخب کریں", "حدد موقعك"), Dj.type.titleM)
                        Txt(t("to see today’s prayer times", "تاکہ آج کے اوقاتِ نماز نظر آئیں", "لعرض مواقيت اليوم"), Dj.type.bodyS, Dj.c.text2)
                    } else {
                        val nx = pn.next
                        Txt(t("${nx?.let { prayerLabel(it) }} in ", "${nx?.let { prayerLabel(it) }} ", "${nx?.let { prayerLabel(it) }} بعد ") + lang.countdown(pn.secondsToNext) + lang.pick("", " میں", ""), Dj.type.titleM)
                        val rest = listOf(PrayerName.Fajr, PrayerName.Dhuhr, PrayerName.Asr, PrayerName.Maghrib, PrayerName.Isha).filter { pn.times.at.getValue(it) > pn.now }
                            .map { prayerLabel(it) + " " + lang.timeShort(pn.times.local(it), Platform.is24Hour()) }
                            .joinToString(" · ")
                        Txt(rest.ifEmpty { t("All prayers prayed for today", "آج کی تمام نمازوں کا وقت گزر گیا", "انقضت أوقات صلوات اليوم") }, Dj.type.bodyS, Dj.c.text2, maxLines = 2)
                    }
                }
                DjIcon("chevron-right", 18.dp, Dj.c.text3)
            }
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) { tile("prayer_time", t("Prayer times", "اوقاتِ نماز", "المواقيت"), PrayerTimes); tile("qibla", t("Qibla", "قبلہ", "القبلة"), Qibla()); tile("wudu", t("Wudu", "وضو", "الوضوء"), GuideRoute("wudu")) }
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) { tile("ghusl", t("Ghusl", "غسل", "الغسل"), GuideRoute("ghusl")); tile("salah", t("Learn Salah", "نماز سیکھیں", "تعلم الصلاة"), LearnSalah); tile("kalima", t("Kalimas", "کلمے", "الكلمات"), Kalimas) }
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) { tile("dua", t("Duas", "دعائیں", "الأدعية"), Duas); tile("adhkar", t("Adhkar", "اذکار", "الأذكار"), Adhkar(if (morning) "morning" else "evening")); tile("tasbih", t("Tasbih", "تسبیح", "التسبيح"), Tasbih()) }
            CardRow(t("Special prayers", "خاص نمازیں", "صلوات خاصة"), sub = t("Janazah · Eid · Jumu‘ah · Witr · Istikharah", "جنازہ · عید · جمعہ · وتر · استخارہ", "الجنازة · العيد · الجمعة · الوتر · الاستخارة"), glyph = "mat", onClick = { nav.go(SpecialPrayers()) })
            CardRow(if (morning) t("Morning adhkar", "صبح کے اذکار", "أذكار الصباح") else t("Evening adhkar", "شام کے اذکار", "أذكار المساء"),
                sub = t("${lang.num(done.coerceAtMost(total))} of ${lang.num(total)} done", "${lang.num(total)} میں سے ${lang.num(done.coerceAtMost(total))} مکمل", "${lang.num(done.coerceAtMost(total))} من ${lang.num(total)}") +
                    (if (morning) t(" · until Asr", " · عصر تک", " · حتى العصر") else t(" · until bedtime", " · سونے تک", " · حتى النوم")),
                glyph = if (morning) "sunrise" else "sunset", onClick = { nav.go(Adhkar(if (morning) "morning" else "evening")) },
                trailing = { Pill("${lang.num(done.coerceAtMost(total))} / ${lang.num(total)}", fill = Dj.c.goldTint, color = Dj.c.goldText) })
        }
    }
}

// ---------------------------------------------------------------- C06 More

@Composable
fun MoreScreen() {
    val nav = LocalNavigator.current
    val users = koinInject<UserRepo>(); val auth = koinInject<AuthRepo>()
    val user by auth.user.collectAsState()
    val profiles by users.profiles.collectAsState()
    val active by users.active.collectAsState()
    val owner = profiles.firstOrNull { it.owner }
    val lang = LocalLang.current
    @Composable
    fun group(title: String, content: @Composable ColumnScope.() -> Unit) = Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Overline(title); DjCard(Modifier.fillMaxWidth(), padding = PaddingValues(horizontal = 14.dp, vertical = 2.dp), content = content)
    }
    @Composable
    fun row(g: String, title: String, r: R, last: Boolean = false) {
        ListRow(title, lead = { GlyphTile(g, 36.dp) }, chevron = true, onClick = { nav.go(r) }, padding = PaddingValues(vertical = 8.dp))
        if (!last) Hr()
    }
    Screen(top = { AppBar(t("More", "مزید", "المزيد"), big = true) }, bottom = { BottomNav(Tab.More) { nav.tab(it) } }) {
        Body(gap = 12.dp) {
            DjCard(Modifier.fillMaxWidth(), onClick = { nav.go(Profile) }, radius = 18.dp) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Avatar((active ?: owner)?.avatar ?: "man", 52.dp, ring = Dj.c.gold)
                    Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(1.dp)) {
                        Txt(user?.name?.takeIf { it.isNotBlank() } ?: owner?.name ?: "", Dj.type.titleM, maxLines = 1)
                        Txt(user?.email ?: "", Dj.type.bodyS, Dj.c.text2, maxLines = 1)
                        Pill(t("${profiles.size} family profiles", "${lang.num(profiles.size)} فیملی پروفائلز", "${lang.num(profiles.size)} ملفات للعائلة"), icon = "users")
                    }
                    DjIcon("chevron-right", 18.dp, Dj.c.text3)
                }
            }
            group(t("Seasons & tools", "موسم اور ٹولز", "المواسم والأدوات")) {
                row("sehri", t("Ramadan", "رمضان", "رمضان"), Ramadan); row("zakat", t("Zakat calculator", "زکوٰۃ کیلکولیٹر", "حاسبة الزكاة"), Zakat)
                row("kaaba", t("Hajj & Umrah", "حج و عمرہ", "الحج والعمرة"), Hajj()); row("calendar", t("Islamic calendar", "اسلامی کیلنڈر", "التقويم الهجري"), Calendar, last = true)
            }
            group(t("Family", "فیملی", "العائلة")) {
                row("family", t("Parent dashboard", "والدین کا ڈیش بورڈ", "لوحة الوالدين"), ParentDashboard); row("child", t("Kids corner", "بچوں کا گوشہ", "ركن الأطفال"), KidsHome, last = true)
            }
            group(t("App", "ایپ", "التطبيق")) {
                row("download", t("Downloads", "ڈاؤن لوڈز", "التنزيلات"), Downloads); row("bookmark", t("Saved items", "محفوظ آئٹمز", "المحفوظات"), Saved)
                row("settings", t("Settings", "سیٹنگز", "الإعدادات"), SettingsRoute, last = true)
            }
            group(t("Support", "مدد", "الدعم")) {
                row("help", t("Help & FAQ", "مدد اور سوالات", "المساعدة والأسئلة"), Help); row("flag", t("Report a correction", "غلطی کی نشاندہی", "الإبلاغ عن تصحيح"), Correction())
                row("shield", t("About & sources", "تعارف اور ماخذ", "حول التطبيق والمصادر"), About, last = true)
            }
            Txt("Deen Journey " + Platform.appVersion, Dj.type.caption, Dj.c.text3, align = TextAlign.Center, modifier = Modifier.fillMaxWidth())
        }
    }
}

// ---------------------------------------------------------------- C07 Global search

private data class Hit(val kind: Int, val glyph: String, val label: String, val title: String, val sub: String, val route: R)

@Composable
fun SearchScreen(initial: String) {
    val nav = LocalNavigator.current
    val lang = LocalLang.current
    val quran = koinInject<QuranRepo>(); val content = koinInject<ContentRepo>(); val hadith = koinInject<HadithStore>()
    var q by remember { mutableStateOf(initial) }
    var hits by remember { mutableStateOf<List<Hit>?>(null) }
    var filter by remember { mutableStateOf(0) }
    var busy by remember { mutableStateOf(false) }
    LaunchedEffect(q, lang) {
        val query = q.trim()
        if (query.length < 2) { hits = null; return@LaunchedEffect }
        delay(300); busy = true
        val out = mutableListOf<Hit>()
        runCatching {
            quran.search(query, limit = 60).forEach { h: SearchHit ->
                val a = h.ayah
                val name = quran.surah(a.sura).nameTr
                out += Hit(1, "quran", lang.pick("Quran · $name ${a.sura}:${a.aya}", "قرآن · $name ${a.sura}:${a.aya}", "القرآن · $name ${a.sura}:${a.aya}"),
                    if (lang == Lang.UR) a.ur else if (lang == Lang.AR) a.ar else a.en, if (h.matchIn == "ar" || lang != Lang.EN) a.en else a.ar, Reader(a.sura, a.aya))
            }
        }
        val needle = query.lowercase()
        fun has(vararg s: String?) = s.any { it != null && (it.lowercase().contains(needle) || normaliseForSearch(it).contains(normaliseForSearch(query))) }
        content.dailyHadith().filter { has(it.en, it.ur, it.arabic, it.short.en, it.short.ur, it.topic) }.forEach {
            out += Hit(2, "hadith", lang.pick("Hadith · ", "حدیث · ", "حديث · ") + it.book.get(lang) + " " + lang.num(it.number), it.short.get(lang), it.narrator.get(lang), HadithDetail(it.collection, it.number, it.id))
        }
        runCatching {
            hadith.search(query, 40).forEach { h: HadithItem ->
                val c = Collections.of(h.collection)
                out += Hit(2, "hadith", lang.pick("Hadith · ", "حدیث · ", "حديث · ") + (c?.name?.get(lang) ?: h.collection) + " " + lang.num(h.number),
                    (if (lang == Lang.UR) h.ur else if (lang == Lang.AR) h.ar else h.en).take(140), h.grade, HadithDetail(h.collection, h.number))
            }
        }
        content.duas().duas.filter { has(it.title.en, it.title.ur, it.title.ar, it.meaning.en, it.meaning.ur, it.translit, it.arabic) }.forEach {
            out += Hit(3, "dua", lang.pick("Dua", "دعا", "دعاء") + (it.refs.firstOrNull()?.label?.let { l -> " · $l" } ?: ""), it.title.get(lang), it.meaning.get(lang).take(120), DuaDetail(it.id))
        }
        content.allLessons().filter { (_, l) -> has(l.title.en, l.title.ur, l.title.ar, l.subtitle.en, l.subtitle.ur, l.body.en, l.body.ur) }.forEach { (tr, l) ->
            out += Hit(4, "lesson", lang.pick("Lesson · ", "سبق · ", "درس · ") + tr.title.get(lang), l.title.get(lang), l.subtitle.get(lang), LessonRoute(l.id))
        }
        content.akhlaq().filter { has(it.title.en, it.title.ur, it.title.ar, it.subtitle.en, it.body.en, it.body.ur) }.forEach {
            out += Hit(4, "akhlaq", lang.pick("Akhlaq", "اخلاق", "الأخلاق"), it.title.get(lang), it.subtitle.get(lang), AkhlaqTopic(it.id))
        }
        content.names().filter { has(it.translit, it.arabic, it.meaning.en, it.meaning.ur, it.meaning.ar) }.forEach {
            out += Hit(4, "names", lang.pick("99 Names · ", "اسمائے حسنیٰ · ", "الأسماء الحسنى · ") + lang.num(it.n), it.translit + " · " + it.arabic, it.meaning.get(lang), Names(it.n))
        }
        hits = out; busy = false
    }
    Screen(top = { AppBar(t("Search", "تلاش", "بحث"), onBack = { nav.back() }, center = true) }) {
        Column(Modifier.padding(horizontal = 18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            SearchBox(q, { q = it }, t("Search Quran, hadith, duas, lessons…", "قرآن، حدیث، دعائیں، اسباق تلاش کریں…", "ابحث في القرآن والحديث والأدعية والدروس…"), autofocus = initial.isEmpty())
            hits?.let { list ->
                val counts = (0..4).map { k -> if (k == 0) list.size else list.count { it.kind == k } }
                ChipRow {
                    listOf(t("All", "سب", "الكل"), t("Quran", "قرآن", "القرآن"), t("Hadith", "حدیث", "الحديث"), t("Duas", "دعائیں", "الأدعية"), t("Learn", "سیکھیں", "تعلّم"))
                        .forEachIndexed { i, l -> DjChip("$l (${lang.num(counts[i])})", filter == i, { filter = i }) }
                }
            }
        }
        Body(gap = 10.dp, padding = PaddingValues(start = 18.dp, end = 18.dp, top = 10.dp, bottom = 24.dp)) {
            val list = hits
            when {
                list == null -> {
                    Overline(t("Try", "آزمائیں", "جرّب"))
                    ChipRow { listOf(t("patience", "صبر", "الصبر"), t("mercy", "رحمت", "الرحمة"), t("parents", "والدین", "الوالدين"), t("forgiveness", "مغفرت", "المغفرة")).forEach { s -> DjChip(s, false, { q = s }, icon = "search") } }
                    NoteBox(t("Search works offline across the Quran (Arabic, Urdu, English), duas, lessons, the 99 Names and any hadith collections you’ve downloaded.", "تلاش آف لائن کام کرتی ہے: قرآن (عربی، اردو، انگریزی)، دعائیں، اسباق، اسمائے حسنیٰ اور ڈاؤن لوڈ شدہ حدیث کی کتابیں۔", "البحث يعمل دون اتصال في القرآن (عربي، أردو، إنجليزي) والأدعية والدروس والأسماء الحسنى وكتب الحديث المنزّلة."), icon = "wifi-off", tone = NoteTone.Grey)
                }
                busy && list.isEmpty() -> repeat(4) { Skeleton(Modifier.fillMaxWidth().height(72.dp)) }
                list.isEmpty() -> EmptyState("empty_search", t("No results", "کوئی نتیجہ نہیں ملا", "لا نتائج"), t("Try a different word or spelling.", "کوئی اور لفظ یا ہجے آزمائیں۔", "جرّب كلمة أو تهجئة أخرى."))
                else -> list.filter { filter == 0 || it.kind == filter }.take(150).forEach { h ->
                    DjCard(Modifier.fillMaxWidth(), onClick = { nav.go(h.route) }, padding = PaddingValues(horizontal = 14.dp, vertical = 12.dp), radius = 14.dp, elevated = false) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            GlyphTile(h.glyph, 42.dp)
                            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(1.dp)) {
                                Txt(h.label, Dj.type.labelS, Dj.c.goldText, maxLines = 1)
                                Txt(h.title, Dj.type.titleS, maxLines = 2)
                                if (h.sub.isNotBlank()) Txt(h.sub, Dj.type.bodyS, Dj.c.text2, maxLines = 2)
                            }
                            DjIcon("chevron-right", 18.dp, Dj.c.text3)
                        }
                    }
                }
            }
        }
    }
}

/** Case/diacritic-insensitive key for matching Arabic and Urdu text. */
fun normaliseForSearch(s: String): String = com.deenjourney.app.data.quran.normaliseArabic(s.lowercase())
