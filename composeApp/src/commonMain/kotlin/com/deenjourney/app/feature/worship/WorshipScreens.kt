package com.deenjourney.app.feature.worship

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import com.deenjourney.app.core.*
import com.deenjourney.app.data.content.ContentRepo
import com.deenjourney.app.data.content.Guide
import com.deenjourney.app.data.content.Dhikr
import com.deenjourney.app.data.location.CitiesRepo
import com.deenjourney.app.data.prayer.*
import com.deenjourney.app.data.settings.*
import com.deenjourney.app.data.user.UserRepo
import com.deenjourney.app.design.*
import com.deenjourney.app.feature.*
import com.deenjourney.app.feature.home.rememberPrayerNow
import com.deenjourney.app.feature.home.prayerLabel
import com.deenjourney.app.feature.start.applyLocation
import com.deenjourney.app.feature.start.detectLocation
import com.deenjourney.app.nav.*
import com.deenjourney.app.platform.*
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import org.koin.compose.koinInject
import kotlin.math.abs

@Composable
fun PrayerTimesScreen() {
    val nav = LocalNavigator.current; val settings = koinInject<SettingsRepo>(); val scope = rememberCoroutineScope(); val lang = LocalLang.current
    val pn = rememberPrayerNow()
    FeaturePage(t("Prayer times", "اوقاتِ نماز", "مواقيت الصلاة"), actions = { IconBtn("settings", { nav.go(PrayerSettings) }) }) {
        Art("hero_prayer_${pn?.period ?: "day"}", Modifier.fillMaxWidth().height(145.dp))
        if (pn?.times == null) {
            CardRow(t("Set your location", "مقام منتخب کریں", "حدد موقعك"), glyph = "prayer_time", onClick = { nav.go(ChooseLocation()) })
        } else {
            val times = pn.times
            SectionHeader(pn.settings.location?.name.orEmpty()); Txt(lang.date(pn.today), Dj.type.bodyS)
            pn.next?.let { next -> NoteBox(prayerLabel(next) + " · " + lang.countdown(pn.secondsToNext), glyph = "prayer_time") }
            PrayerName.entries.forEach { p ->
                DjCard(Modifier.fillMaxWidth(), fill = if (pn.current == p) Dj.c.primaryTint else Dj.c.surface) {
                    ListRow(prayerLabel(p), sub = lang.time(times.local(p), Platform.is24Hour()), lead = { GlyphTile(if (p == PrayerName.Fajr) "sunrise" else if (p == PrayerName.Isha) "isha" else "sun") }, trailing = {
                        DjSwitch(pn.settings.adhan[p.name]?.on == true, { on -> scope.launch { settings.update { it.copy(adhan = it.adhan + (p.name to (it.adhan[p.name] ?: AdhanPref()).copy(on = on))) }; Scheduler.reschedule() } }, enabled = p.hasAdhan)
                    })
                }
            }
            CardRow(t("Change location", "مقام تبدیل کریں", "تغيير الموقع"), icon = "map-pin", onClick = { nav.go(ChooseLocation()) })
        }
    }
}

@Composable
fun PrayerSettingsScreen() {
    val settings = koinInject<SettingsRepo>(); val s by settings.flow.collectAsState(); val scope = rememberCoroutineScope(); val lang = LocalLang.current
    fun update(block: (AppSettings) -> AppSettings) { scope.launch { settings.update(block); Scheduler.reschedule() } }
    FeaturePage(t("Prayer & adhan settings", "نماز اور اذان کی سیٹنگز", "إعدادات الصلاة والأذان")) {
        SectionHeader(t("Calculation method", "حساب کا طریقہ", "طريقة الحساب"))
        Method.entries.forEach { method -> ListRow(method.label.get(lang), sub = method.fajrIsha, trailing = { DjRadio(s.prayer.method == method.name) }, onClick = { update { it.copy(prayer = it.prayer.copy(method = method.name), methodChosen = true) } }) }
        SectionHeader(t("Asr calculation", "عصر کا حساب", "حساب العصر"))
        Segmented(listOf(t("Standard", "عام", "الجمهور"), t("Hanafi", "حنفی", "حنفي")), if (s.prayer.asr == "Hanafi") 1 else 0, { i -> update { it.copy(prayer = it.prayer.copy(asr = if (i == 1) "Hanafi" else "Standard"), methodChosen = true) } })
        SectionHeader(t("High latitude", "بلند عرض بلد", "خطوط العرض العليا"))
        ChipRow { HighLat.entries.forEach { high -> DjChip(high.name, s.prayer.highLat == high.name, { update { it.copy(prayer = it.prayer.copy(highLat = high.name)) } }) } }
        SectionHeader(t("Minute adjustments", "منٹوں کی کمی بیشی", "تعديل الدقائق"))
        PrayerName.entries.forEachIndexed { index, prayer ->
            ListRow(prayerLabel(prayer), trailing = { Row(verticalAlignment = Alignment.CenterVertically) {
                IconBtn("minus", { update { val offsets = it.prayer.adjust.toMutableList(); offsets[index] = (offsets[index] - 1).coerceAtLeast(-30); it.copy(prayer = it.prayer.copy(adjust = offsets)) } })
                Txt(s.prayer.adjust.getOrElse(index) { 0 }.toString(), Dj.type.labelM)
                IconBtn("plus", { update { val offsets = it.prayer.adjust.toMutableList(); offsets[index] = (offsets[index] + 1).coerceAtMost(30); it.copy(prayer = it.prayer.copy(adjust = offsets)) } })
            } })
        }
        SectionHeader(t("Adhan sound", "اذان کی آواز", "صوت الأذان"))
        ChipRow { listOf("chime", "tone", "silent").forEach { sound -> DjChip(sound, s.adhan["Fajr"]?.sound == sound, { update { it.copy(adhan = it.adhan.mapValues { row -> row.value.copy(sound = sound) }) }; if (sound != "silent") SoundFx.playResource("files/sounds/adhan_$sound.wav") }) } }
        SwitchRow(t("Jumu‘ah reminder", "جمعہ کی یاد دہانی", "تذكير الجمعة"), s.jumuahReminder, { v -> update { it.copy(jumuahReminder = v) } })
        SwitchRow(t("Adhkar reminder", "اذکار کی یاد دہانی", "تذكير الأذكار"), s.adhkarReminder, { v -> update { it.copy(adhkarReminder = v) } })
        ChipRow { listOf(0, 5, 10, 15).forEach { min -> DjChip("$min min", s.reminderBefore == min, { update { it.copy(reminderBefore = min) } }) } }
        if (!Scheduler.canScheduleExact()) NoteBox(t("Allow exact alarms for timely prayer reminders.", "بروقت یاد دہانی کے لیے درست الارم کی اجازت دیں۔", "اسمح بالمنبهات الدقيقة للتذكير في وقته."), onClick = { Scheduler.openExactAlarmSettings() })
        TextLink(t("Battery settings", "بیٹری سیٹنگز", "إعدادات البطارية"), { Scheduler.openBatterySettings() })
    }
}

@Composable
fun ChooseLocationScreen(onboarding: Boolean) {
    val nav = LocalNavigator.current; val cities = koinInject<CitiesRepo>(); val loc = koinInject<LocationService>(); val settings = koinInject<SettingsRepo>(); val lang = LocalLang.current; val scope = rememberCoroutineScope()
    var query by remember { mutableStateOf("") }; var busy by remember { mutableStateOf(false) }; var error by remember { mutableStateOf<String?>(null) }
    val ask = rememberPermission(Perm.Location) { granted ->
        if (!granted) error = lang.pick("Choose a city or allow location in settings.", "شہر منتخب کریں یا سیٹنگز میں مقام کی اجازت دیں۔", "اختر مدينة أو اسمح بالموقع من الإعدادات.")
        else scope.launch { busy = true; val found = detectLocation(loc, cities, settings, lang); busy = false; if (found != null) { if (onboarding) nav.go(NotifSetup) else nav.back() } else error = lang.pick("Location unavailable. Choose a city.", "مقام دستیاب نہیں۔ شہر منتخب کریں۔", "الموقع غير متاح. اختر مدينة.") }
    }
    FeaturePage(t("Choose location", "مقام منتخب کریں", "اختر الموقع")) {
        DjButton(t("Use current location", "موجودہ مقام", "استخدام موقعي"), { ask() }, Modifier.fillMaxWidth(), lead = "locate-fixed", loading = busy)
        error?.let { NoteBox(it, tone = NoteTone.Gold, onClick = { Platform.openAppSettings() }) }
        SearchBox(query, { query = it }, t("Search city", "شہر تلاش کریں", "ابحث عن مدينة"))
        Loaded(query to lang, { kotlinx.coroutines.delay(250); cities.search(query, lang) }) { rows -> rows.forEach { city -> CardRow(city.name, sub = "${city.country} · ${city.tz}", icon = "map-pin", onClick = {
            scope.launch { applyLocation(settings, SavedLocation(city.name, city.country, city.cc, city.lat, city.lng, city.tz)); if (onboarding) nav.go(NotifSetup) else nav.back() }
        }) } }
    }
}

@Composable
fun QiblaScreen(initial: Int) {
    val settings = koinInject<SettingsRepo>(); val s by settings.flow.collectAsState(); val compass = koinInject<CompassService>(); val nav = LocalNavigator.current
    var tab by remember { mutableStateOf(initial.coerceIn(0, 1)) }
    val loc = s.location
    val headingFlow = remember(loc, tab) { if (loc != null && compass.available && tab == 0) compass.headings(loc.lat, loc.lng) else flowOf(Heading(0f, 0)) }
    val heading by headingFlow.collectAsState(Heading(0f, 0))
    FeaturePage(t("Qibla finder", "قبلہ کی سمت", "اتجاه القبلة")) {
        UTabs(listOf(t("Compass", "کمپاس", "البوصلة"), t("Map", "نقشہ", "الخريطة")), tab, { tab = it })
        if (loc == null) CardRow(t("Choose location", "مقام منتخب کریں", "اختر الموقع"), icon = "map-pin", onClick = { nav.go(ChooseLocation()) }) else {
            val bearing = PrayerEngine.qibla(loc.lat, loc.lng).toFloat()
            if (tab == 0) {
                Box(Modifier.fillMaxWidth().height(290.dp), contentAlignment = Alignment.Center) {
                    Art("compass_dial", Modifier.size(260.dp).rotate(-heading.azimuth))
                    DjIcon("navigation", 80.dp, Dj.c.primary, Modifier.rotate(bearing - heading.azimuth), mirror = false)
                }
                Txt("${bearing.toInt()}°", Dj.type.numberL)
                if (!compass.available || heading.accuracy == 0) NoteBox(t("Compass unavailable or needs calibration. Use the bearing from true north, away from metal and magnets.", "کمپاس دستیاب نہیں یا کیلیبریشن درکار ہے۔ دھات اور مقناطیس سے دور رہیں۔", "البوصلة غير متاحة أو تحتاج معايرة. ابتعد عن المعادن والمغناطيس."), tone = NoteTone.Gold)
                else if (abs(((bearing - heading.azimuth + 540) % 360) - 180) < 5) Pill(t("Facing Qibla", "قبلہ کی سمت", "تواجه القبلة"), icon = "check")
            } else {
                val points = remember(loc) { PrayerEngine.greatCircle(loc.lat, loc.lng) }
                val color = Dj.c.primary; val grid = Dj.c.border
                Canvas(Modifier.fillMaxWidth().height(240.dp)) {
                    fun project(lat: Double, lng: Double) = Offset(((lng + 180) / 360 * size.width).toFloat(), ((90 - lat) / 180 * size.height).toFloat())
                    for (i in 0..6) drawLine(grid, Offset(i * size.width / 6, 0f), Offset(i * size.width / 6, size.height))
                    for (i in 0..4) drawLine(grid, Offset(0f, i * size.height / 4), Offset(size.width, i * size.height / 4))
                    val path = Path(); var previous: Offset? = null
                    points.forEach { (lat, lng) -> val p = project(lat, lng); if (previous == null || abs(p.x - previous!!.x) > size.width / 2) path.moveTo(p.x, p.y) else path.lineTo(p.x, p.y); previous = p }
                    drawPath(path, color, style = Stroke(4f)); drawCircle(color, 7f, project(loc.lat, loc.lng)); drawCircle(color, 9f, project(PrayerEngine.KAABA_LAT, PrayerEngine.KAABA_LNG))
                }
                Txt(t("Great-circle route · longitude / latitude", "قبلہ تک مختصر راستہ · طول و عرض بلد", "المسار الأقصر · خطوط الطول والعرض"), Dj.type.caption)
                Txt("${PrayerEngine.distanceToKaabaKm(loc.lat, loc.lng).toInt()} km · Kaaba", Dj.type.titleM)
            }
            CardRow(loc.name, sub = "${loc.lat}, ${loc.lng}", icon = "map-pin", onClick = { nav.go(ChooseLocation()) })
        }
    }
}

@Composable
fun GuideContent(guide: Guide) {
    Txt(guide.intro.text(), Dj.type.bodyM)
    guide.steps.forEachIndexed { index, step -> DjCard(Modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) { NumBadge(index + 1); Txt(step.title.text(), Dj.type.titleM) }
        step.glyph?.let { Glyph(it, 60.dp) }; Gap(8.dp); Txt(step.body.text(), Dj.type.bodyM)
        step.arabic?.let { ArabicText(it, Dj.type.arabicM) }; step.translit?.let { Txt(it, Dj.type.bodyS) }; step.meaning?.let { Txt(it.text(), Dj.type.bodyM) }
        val settings = koinInject<SettingsRepo>(); val s by settings.flow.collectAsState(); step.fiqh?.forSchool(s.fiqh)?.let { NoteBox(it.text(), tone = NoteTone.Gold) }
    } }
    guide.about.forEach { SectionContent(it) }; References(guide.refs)
}

@Composable
fun GuideScreen(key: String) {
    val content = koinInject<ContentRepo>()
    FeaturePage(key.replace('_', ' ').replaceFirstChar { it.uppercase() }) {
        Loaded(key, { content.guides()[key] }) { guide -> if (guide == null) NoItems() else GuideContent(guide) }
    }
}

@Composable
fun LearnSalahScreen() {
    val content = koinInject<ContentRepo>(); val settings = koinInject<SettingsRepo>(); val s by settings.flow.collectAsState(); val nav = LocalNavigator.current
    var tab by remember { mutableStateOf(0) }; var index by remember { mutableStateOf(0) }
    FeaturePage(t("Learn Salah", "نماز سیکھیں", "تعلم الصلاة"), actions = { IconBtn("settings", { nav.go(SettingsRoute) }) }) {
        UTabs(listOf(t("Postures", "ارکان", "الهيئات"), t("In detail", "تفصیل", "التفصيل"), t("Mistakes", "غلطیاں", "الأخطاء")), tab, { tab = it })
        Loaded(Unit, { content.salah() }) { salah ->
            if (tab == 0) salah.postures.getOrNull(index)?.let { p ->
                Art("pose_${p.pose}", Modifier.fillMaxWidth().height(180.dp)); SectionHeader(p.title.text()); p.bullets.forEach { Txt("• " + it.text(), Dj.type.bodyM) }
                p.arabic?.let { ArabicText(it, Dj.type.arabicL) }; p.meaning?.let { Txt(it.text(), Dj.type.bodyM) }; p.fiqh?.forSchool(s.fiqh)?.let { NoteBox(it.text()) }
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) { DjButton(t("Previous", "پچھلا", "السابق"), { index-- }, enabled = index > 0, style = BtnStyle.Soft); Pill("${index + 1}/${salah.postures.size}"); DjButton(t("Next", "اگلا", "التالي"), { index++ }, enabled = index < salah.postures.lastIndex) }
            } else (if (tab == 1) salah.detail else salah.mistakes).forEach { SectionContent(it) }
        }
    }
}

@Composable
fun KalimasScreen() {
    val content = koinInject<ContentRepo>()
    FeaturePage(t("Kalimas", "کلمے", "الكلمات")) { Loaded(Unit, { content.kalimas() }) { list -> list.forEach { k -> DjCard(Modifier.fillMaxWidth()) {
        SectionHeader(k.title.text()); ArabicText(k.arabic, Dj.type.arabicL); Txt(k.translit, Dj.type.bodyS); Txt(k.meaning.text(), Dj.type.bodyM); References(k.refs); SaveAction("kalima", k.id, k.title.text())
    } } } }
}

@Composable
fun DuaLibraryScreen(category: String? = null) {
    val content = koinInject<ContentRepo>(); val nav = LocalNavigator.current; var query by remember { mutableStateOf("") }; val lang = LocalLang.current
    FeaturePage(t("Dua library", "دعاؤں کی لائبریری", "مكتبة الأدعية")) {
        SearchBox(query, { query = it }, t("Search duas", "دعا تلاش کریں", "ابحث عن دعاء"))
        Loaded(Unit, { content.duas() }) { file ->
            if (category == null && query.isBlank()) file.categories.forEach { c -> CardRow(c.title.text(), sub = c.subtitle.text(), glyph = c.glyph, onClick = { nav.go(DuaCategory(c.id)) }) }
            else {
                val ids = file.categories.firstOrNull { it.id == category }?.duas
                val rows = file.duas.filter { (ids == null || it.id in ids) && (query.isBlank() || it.title.get(lang).contains(query, true) || it.arabic.contains(query) || it.meaning.get(lang).contains(query, true)) }
                if (rows.isEmpty()) NoItems()
                rows.forEach { dua -> CardRow(dua.title.text(), sub = dua.meaning.text(), glyph = "dua", onClick = { nav.go(DuaDetail(dua.id)) }) }
            }
        }
    }
}

@Composable
fun DuaDetailScreen(id: String) {
    val content = koinInject<ContentRepo>()
    FeaturePage(t("Dua", "دعا", "دعاء")) { Loaded(id, { content.dua(id) }) { dua -> if (dua == null) NoItems() else {
        SectionHeader(dua.title.text()); ArabicText(dua.arabic, Dj.type.arabicL); Txt(dua.translit, Dj.type.bodyM); Txt(dua.meaning.text(), Dj.type.bodyL)
        dua.`when`?.let { NoteBox(it.text()) }; References(dua.refs); SaveAction("dua", dua.id, dua.title.text())
        DjButton(t("Share", "شیئر", "مشاركة"), { Platform.shareText(dua.arabic + "\n\n" + dua.meaning.get(Lang.EN) + "\n" + dua.refs.joinToString { it.label }) }, Modifier.fillMaxWidth(), style = BtnStyle.Soft, lead = "share-2")
    } } }
}

@Composable
fun AdhkarScreen(initial: String) {
    val content = koinInject<ContentRepo>(); val users = koinInject<UserRepo>(); val scope = rememberCoroutineScope()
    var kind by remember { mutableStateOf(initial) }; var index by remember(kind) { mutableStateOf(0) }; var count by remember(kind, index) { mutableStateOf(0) }
    val day = today(); val logs by users.day(day).collectAsState(emptyList())
    FeaturePage(t("Morning & evening adhkar", "صبح و شام کے اذکار", "أذكار الصباح والمساء")) {
        ChipRow { listOf("morning", "evening", "after_salah", "sleep").forEach { k -> DjChip(k.replace('_', ' '), kind == k, { kind = k }) } }
        Loaded(Unit, { content.adhkar() }) { file ->
            val list = when (kind) { "evening" -> file.evening; "sleep" -> file.sleep; "after_salah" -> file.after_salah; else -> file.morning }
            if (list.isEmpty()) NoItems() else {
                val d = list[index.coerceIn(list.indices)]
                LaunchedEffect(kind, d.id, day, logs) {
                    count = maxOf(count, logs.firstOrNull { it.kind == "dhikr:$kind:${d.id}" }?.value?.coerceIn(0, d.count) ?: 0)
                }
                DjCard(Modifier.fillMaxWidth()) { Pill("${index + 1}/${list.size}"); ArabicText(d.arabic, Dj.type.arabicL); Txt(d.translit, Dj.type.bodyS); Txt(d.meaning.text(), Dj.type.bodyM); References(d.refs); SaveAction("dhikr", d.id, d.translit) }
                Txt("$count / ${d.count}", Dj.type.numberL)
                DjButton(t("Tap to count", "گنتی کے لیے ٹیپ کریں", "اضغط للعد"), {
                    if (count < d.count) {
                        count++; Platform.vibrate()
                        val value = count
                        scope.launch {
                            users.setDaily(day, "dhikr:$kind:${d.id}", value)
                            if (kind == "morning" || kind == "evening") {
                                val complete = list.count { item -> if (item.id == d.id) value >= item.count else logs.any { it.kind == "dhikr:$kind:${item.id}" && it.value >= item.count } }
                                users.setDaily(day, if (kind == "morning") "adhkar_m" else "adhkar_e", if (complete == list.size) 1 else 0)
                            }
                        }
                    }
                }, Modifier.fillMaxWidth(), enabled = count < d.count)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) { DjButton(t("Previous", "پچھلا", "السابق"), { index-- }, enabled = index > 0, style = BtnStyle.Soft); DjButton(t("Next", "اگلا", "التالي"), { index++ }, enabled = index < list.lastIndex) }
                DjProgress(list.count { d2 -> logs.any { it.kind == "dhikr:$kind:${d2.id}" && it.value >= d2.count } }.toFloat() / list.size)
            }
        }
    }
}

@Composable
fun TasbihScreen(initial: Int) {
    val users = koinInject<UserRepo>(); val settings = koinInject<SettingsRepo>(); val s by settings.flow.collectAsState(); val profile by users.active.collectAsState()
    val counters by users.counters().collectAsState(emptyList()); val history by users.daily("tasbih", kotlinx.datetime.LocalDate.fromEpochDays(today().toEpochDays() - 30)).collectAsState(emptyList()); val scope = rememberCoroutineScope()
    var tab by remember { mutableStateOf(initial.coerceIn(0, 2)) }; var selected by remember { mutableStateOf<String?>(null) }; var reset by remember { mutableStateOf(false) }
    val counter = counters.firstOrNull { it.id == selected } ?: counters.firstOrNull()
    LaunchedEffect(profile?.id) { if (profile != null) users.ensureDefaultCounters() }
    FeaturePage(t("Digital tasbih", "ڈیجیٹل تسبیح", "التسبيح الرقمي")) {
        UTabs(listOf(t("Tasbih", "تسبیح", "التسبيح"), t("Counters", "کاؤنٹرز", "العدادات"), t("History", "تاریخ", "السجل")), tab, { tab = it })
        when (tab) {
            1 -> counters.forEach { c -> CardRow(c.label, sub = "${c.count}/${c.target}", glyph = "tasbih", onClick = { selected = c.id; tab = 0 }) }
            2 -> { if (history.isEmpty()) NoItems(); history.reversed().forEach { row -> CardRow(row.date, sub = row.value.toString(), glyph = "tasbih", chevron = false) } }
            else -> counter?.let { c ->
                ArabicText(c.arabic, Dj.type.arabicL, center = true, modifier = Modifier.fillMaxWidth()); Txt(c.label, Dj.type.titleL)
                Box(Modifier.fillMaxWidth().height(240.dp), contentAlignment = Alignment.Center) { Art("tasbih_ring", Modifier.size(240.dp)); Txt(c.count.toString(), Dj.type.numberL) }
                DjButton(t("Tap to count", "گنتی کے لیے ٹیپ کریں", "اضغط للعد"), { scope.launch { users.incrementCounter(c.id, s.tasbihAutoReset); users.addDaily(today(), "tasbih", 1) }; if (s.tasbihHaptic) Platform.vibrate(); if (s.tasbihSound) SoundFx.playResource("files/sounds/tick.wav") }, Modifier.fillMaxWidth())
                DjProgress(c.count.coerceAtMost(c.target).toFloat() / c.target.coerceAtLeast(1))
                SwitchRow(t("Haptic feedback", "وائبریشن", "اهتزاز"), s.tasbihHaptic, { v -> scope.launch { settings.update { it.copy(tasbihHaptic = v) } } })
                SwitchRow(t("Sound", "آواز", "صوت"), s.tasbihSound, { v -> scope.launch { settings.update { it.copy(tasbihSound = v) } } })
                SwitchRow(t("Auto reset at target", "ہدف پر خودکار ری سیٹ", "إعادة ضبط عند الهدف"), s.tasbihAutoReset, { v -> scope.launch { settings.update { it.copy(tasbihAutoReset = v) } } })
                DjButton(t("Reset counter", "کاؤنٹر ری سیٹ", "إعادة ضبط العداد"), { reset = true }, Modifier.fillMaxWidth(), style = BtnStyle.Soft)
            }
        }
    }
    if (reset && counter != null) ConfirmDialog(t("Reset counter?", "کاؤنٹر ری سیٹ؟", "إعادة ضبط العداد؟"), counter.label, t("Reset", "ری سیٹ", "إعادة ضبط"), { scope.launch { users.saveCounter(counter.copy(count = 0)) }; reset = false }, { reset = false })
}

@Composable
fun SpecialPrayersScreen(initial: Int) {
    val content = koinInject<ContentRepo>(); val nav = LocalNavigator.current
    var selected by remember { mutableStateOf(initial.coerceIn(0, 4)) }; val keys = listOf("janazah", "eid", "jumuah", "witr", "istikharah")
    FeaturePage(t("Special prayers", "خاص نمازیں", "صلوات خاصة")) {
        ChipRow { keys.forEachIndexed { index, key -> DjChip(key.replaceFirstChar { it.uppercase() }, selected == index, { selected = index }) } }
        Art("hero_hills", Modifier.fillMaxWidth().height(120.dp))
        Loaded(keys[selected], { content.guides()[keys[selected]] }) { guide -> if (guide != null) {
            SectionHeader(guide.title.text()); Txt(guide.intro.text(), Dj.type.bodyM)
            CardRow(t("Step-by-step guide", "مرحلہ وار رہنمائی", "دليل خطوة بخطوة"), glyph = "salah", onClick = { nav.go(GuideRoute(keys[selected])) }); References(guide.refs)
        } else NoItems() }
    }
}
