package com.deenjourney.app.feature.tools

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.deenjourney.app.core.*
import com.deenjourney.app.data.content.ContentRepo
import com.deenjourney.app.data.hijri.*
import com.deenjourney.app.data.net.*
import com.deenjourney.app.data.quran.QuranRepo
import com.deenjourney.app.data.settings.SettingsRepo
import com.deenjourney.app.data.user.*
import com.deenjourney.app.design.*
import com.deenjourney.app.feature.*
import com.deenjourney.app.feature.home.rememberPrayerNow
import com.deenjourney.app.feature.worship.GuideContent
import com.deenjourney.app.nav.*
import kotlinx.coroutines.launch
import org.koin.compose.koinInject
import kotlin.math.roundToLong

/** Amounts are minor currency units; eligibility gates the entire net wealth. */
data class ZakatEstimate(val net: Long, val eligible: Boolean, val due: Long)
fun estimateZakat(assets: Long, debts: Long, nisab: Long, hawl: Boolean): ZakatEstimate {
    val net = (assets.coerceAtLeast(0) - debts.coerceAtLeast(0)).coerceAtLeast(0)
    val eligible = hawl && nisab > 0 && net >= nisab
    return ZakatEstimate(net, eligible, if (eligible) (net / 40.0).roundToLong() else 0)
}
private fun amount(text: String): Long? = text.trim().replace(",", "").toDoubleOrNull()?.takeIf { it.isFinite() && it >= 0 && it <= 1e12 }?.let { (it * 100).roundToLong() }
private fun money(cents: Long): String = "${cents / 100}.${(cents % 100).toString().padStart(2, '0')}"

@Composable
fun RamadanScreen() {
    val nav = LocalNavigator.current; val pn = rememberPrayerNow(); val lang = LocalLang.current
    FeaturePage(t("Ramadan", "رمضان", "رمضان")) {
        Art("hero_ramadan", Modifier.fillMaxWidth().height(165.dp)); pn?.let { Txt(Hijri.format(it.hijri, lang), Dj.type.titleM) }
        pn?.times?.let { times -> Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            StatBox(lang.time(times.local(com.deenjourney.app.data.prayer.PrayerName.Fajr), Platform.is24Hour()), t("Sehri ends", "سحری ختم", "نهاية السحور"), Modifier.weight(1f))
            StatBox(lang.time(times.local(com.deenjourney.app.data.prayer.PrayerName.Maghrib), Platform.is24Hour()), t("Iftar", "افطار", "الإفطار"), Modifier.weight(1f))
        } } ?: CardRow(t("Set location for Sehri/Iftar", "سحری اور افطار کے لیے مقام", "حدد موقع السحور والإفطار"), icon = "map-pin", onClick = { nav.go(ChooseLocation()) })
        CardRow(t("Quran plan", "قرآن کا منصوبہ", "خطة القرآن"), glyph = "quran", onClick = { nav.go(QuranPlan) })
        CardRow(t("Fasting guide", "روزے کی رہنمائی", "دليل الصيام"), glyph = "fasting", onClick = { nav.go(FastingGuide) })
        CardRow(t("Daily duas", "روزانہ دعائیں", "الأدعية اليومية"), glyph = "dua", onClick = { nav.go(Duas) })
        CardRow(t("Ramadan tracker", "رمضان ٹریکر", "متابعة رمضان"), glyph = "tracker", onClick = { nav.go(RamadanTracker) })
    }
}

@Composable
fun QuranPlanScreen() {
    val repo = koinInject<QuranRepo>(); val users = koinInject<UserRepo>(); val done by users.progress("quran_juz").collectAsState(emptyList()); val scope = rememberCoroutineScope(); val nav = LocalNavigator.current
    FeaturePage(t("Quran khatam plan", "قرآن ختم کا منصوبہ", "خطة ختم القرآن")) {
        NoteBox(t("Read one juz each day for a 30-day plan. Mark each juz after completing it.", "30 دن کے لیے روزانہ ایک پارہ پڑھیں۔ مکمل ہونے پر نشان لگائیں۔", "اقرأ جزءًا كل يوم لخطة من ٣٠ يومًا، وحدده بعد إكماله."))
        DjProgress(done.count { it.value > 0 } / 30f)
        Loaded(Unit, { repo.juzList() }) { list -> list.forEach { j -> CardRow("${j.n} · ${j.nameTr}", sub = "${j.startSura}:${j.startAya} → ${j.endSura}:${j.endAya}", glyph = "quran", onClick = { nav.go(Reader(j.startSura, j.startAya)) }, trailing = { DjCheck(done.any { it.key == j.n.toString() && it.value > 0 }, { on -> scope.launch { users.setProgress("quran_juz", j.n.toString(), if (on) 1 else 0) } }) }) } }
    }
}

@Composable
fun FastingGuideScreen() {
    val content = koinInject<ContentRepo>()
    FeaturePage(t("Fasting guide", "روزے کی رہنمائی", "دليل الصيام")) { Loaded(Unit, { content.fasting() }) { file -> file.sections.forEach { SectionContent(it) } } }
}

@Composable
fun RamadanTrackerScreen() {
    val users = koinInject<UserRepo>(); val scope = rememberCoroutineScope(); var date by remember { mutableStateOf(today()) }; val logs by remember(date) { users.day(date) }.collectAsState(emptyList()); val lang = LocalLang.current
    FeaturePage(t("Ramadan tracker", "رمضان ٹریکر", "متابعة رمضان")) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) { IconBtn("chevron-left", { date = kotlinx.datetime.LocalDate.fromEpochDays(date.toEpochDays() - 1) }); Txt(lang.date(date), Dj.type.titleM); IconBtn("chevron-right", { date = kotlinx.datetime.LocalDate.fromEpochDays(date.toEpochDays() + 1) }) }
        SectionHeader(t("Fasting", "روزہ", "الصيام")); val fast = logs.firstOrNull { it.kind == "fast" }?.value ?: 0
        ChipRow { listOf(t("Kept", "رکھا", "صمت"), t("Missed", "چھوٹا", "فاتني"), t("Exempt", "رخصت", "معذور")).forEachIndexed { i, label -> DjChip(label, fast == i + 1, { scope.launch { users.setDaily(date, "fast", if (fast == i + 1) 0 else i + 1) } }) } }
        listOf("taraweeh" to t("Taraweeh", "تراویح", "التراويح"), "sadaqah" to t("Charity", "صدقہ", "صدقة"), "deed" to t("Good deed", "نیکی", "عمل صالح")).forEach { (key, label) -> SwitchRow(label, logs.any { it.kind == key && it.value > 0 }, { on -> scope.launch { users.setDaily(date, key, if (on) 1 else 0) } }) }
        val pages = logs.firstOrNull { it.kind == "quran_pages" }?.value ?: 0
        ListRow(t("Quran pages", "قرآن کے صفحات", "صفحات القرآن"), trailing = { Row { IconBtn("minus", { scope.launch { users.setDaily(date, "quran_pages", (pages - 1).coerceAtLeast(0)) } }); Txt(pages.toString(), Dj.type.titleM); IconBtn("plus", { scope.launch { users.setDaily(date, "quran_pages", pages + 1) } }) } })
    }
}

@Composable
fun ZakatScreen() {
    val settings = koinInject<SettingsRepo>(); val s by settings.flow.collectAsState(); val remote = koinInject<PriceRemote>(); val users = koinInject<UserRepo>(); val scope = rememberCoroutineScope(); val nav = LocalNavigator.current
    var cash by remember { mutableStateOf("") }; var metals by remember { mutableStateOf("") }; var investments by remember { mutableStateOf("") }; var other by remember { mutableStateOf("") }; var debts by remember { mutableStateOf("") }; var threshold by remember { mutableStateOf("") }
    var hawl by remember { mutableStateOf(false) }; var busy by remember { mutableStateOf(false) }; var message by remember { mutableStateOf<String?>(null) }; var priceAt by remember { mutableStateOf<Long?>(null) }
    var prices by remember { mutableStateOf<MetalPrices?>(null) }
    fun setThreshold(p: MetalPrices, currency: String, basis: String) {
        val fx = p.usdTo[currency] ?: return
        val grams = if (basis == "gold") NISAB_GOLD_G else NISAB_SILVER_G
        val perOz = if (basis == "gold") p.goldUsdPerOz else p.silverUsdPerOz
        threshold = money((perOz / TROY_OUNCE_GRAMS * grams * fx * 100).roundToLong()); priceAt = p.at
    }
    LaunchedEffect(Unit) { Platform.readFile("${Platform.filesDir()}/metal-prices.json")?.let { bytes -> runCatching { AppJson.decodeFromString(MetalPrices.serializer(), bytes.decodeToString()) }.getOrNull()?.let { prices = it; setThreshold(it, s.currency, s.nisab) } } }
    FeaturePage(t("Zakat calculator", "زکوٰۃ کیلکولیٹر", "حاسبة الزكاة")) {
        Art("hero_kaaba", Modifier.fillMaxWidth().height(90.dp))
        ChipRow { listOf("PKR", "USD", "GBP", "EUR", "SAR", "AED").forEach { currency -> DjChip(currency, s.currency == currency, { scope.launch { settings.update { it.copy(currency = currency) } }; threshold = ""; prices?.let { setThreshold(it, currency, s.nisab) } }) } }
        Segmented(listOf(t("Silver · 612.36 g", "چاندی · 612.36 گرام", "الفضة · ٦١٢٫٣٦ غ"), t("Gold · 87.48 g", "سونا · 87.48 گرام", "الذهب · ٨٧٫٤٨ غ")), if (s.nisab == "gold") 1 else 0, { index -> val basis = if (index == 1) "gold" else "silver"; scope.launch { settings.update { it.copy(nisab = basis) } }; threshold = ""; prices?.let { setThreshold(it, s.currency, basis) } })
        DjButton(t("Refresh metal prices", "دھات کی قیمتیں تازہ کریں", "تحديث أسعار المعادن"), { scope.launch { busy = true; val p = remote.fetch(); busy = false; if (p == null) message = "Prices unavailable. Enter today's nisab manually." else { prices = p; Platform.writeFile("${Platform.filesDir()}/metal-prices.json", AppJson.encodeToString(MetalPrices.serializer(), p).encodeToByteArray()); setThreshold(p, settings.get().currency, settings.get().nisab); message = null } } }, Modifier.fillMaxWidth(), style = BtnStyle.Soft, loading = busy)
        priceAt?.let { Txt("Price timestamp: ${kotlin.time.Instant.fromEpochMilliseconds(it)}", Dj.type.caption) }
        DjField(threshold, { threshold = it }, label = t("Nisab value", "نصاب کی مالیت", "قيمة النصاب") + " (${s.currency})", keyboard = KeyboardType.Decimal)
        SectionHeader(t("Your assets", "آپ کے اثاثے", "أموالك"))
        DjField(cash, { cash = it }, label = t("Cash & bank balance", "نقد اور بینک بیلنس", "النقد والحسابات"), keyboard = KeyboardType.Decimal)
        DjField(metals, { metals = it }, label = t("Gold & silver market value", "سونے اور چاندی کی مالیت", "قيمة الذهب والفضة"), keyboard = KeyboardType.Decimal)
        DjField(investments, { investments = it }, label = t("Investments & savings", "سرمایہ اور بچت", "الاستثمارات والمدخرات"), keyboard = KeyboardType.Decimal)
        DjField(other, { other = it }, label = t("Other zakatable assets", "دیگر قابل زکوٰۃ اثاثے", "الأموال الزكوية الأخرى"), keyboard = KeyboardType.Decimal)
        DjField(debts, { debts = it }, label = t("Deductible debts due", "واجب الادا قابل کٹوتی قرض", "الديون المستحقة القابلة للخصم"), keyboard = KeyboardType.Decimal)
        SwitchRow(t("A full lunar year has passed", "ایک قمری سال گزر چکا ہے", "مرّ حول قمري كامل"), hawl, { hawl = it })
        val inputs = listOf(cash, metals, investments, other, debts).map { amount(it.ifBlank { "0" }) }
        val nisab = amount(threshold)
        val valid = inputs.all { it != null } && nisab != null && nisab > 0
        if (valid) {
            val assets = inputs.take(4).sumOf { it!! }; val estimate = estimateZakat(assets, inputs[4]!!, nisab!!, hawl)
            DjCard(Modifier.fillMaxWidth(), fill = Dj.c.primaryTint) { StatBox("${s.currency} ${money(estimate.due)}", t("Estimated zakat due · 2.5%", "تخمینی زکوٰۃ · 2.5%", "الزكاة المقدرة · ٢٫٥٪")); Txt("Net: ${money(estimate.net)} · Nisab: ${money(nisab)}", Dj.type.bodyS) }
            DjButton(t("Save calculation", "حساب محفوظ کریں", "حفظ الحساب"), { scope.launch { val now = nowMs(); users.saveZakat(ZakatE(newId(), now, s.currency, "cash=$cash;metals=$metals;investments=$investments;other=$other", inputs[4]!!, nisab, estimate.net, estimate.due, updatedAt = now)); message = "Calculation saved" } }, Modifier.fillMaxWidth())
        } else NoteBox(t("Enter a valid nisab and non-negative amounts.", "درست نصاب اور مثبت رقوم درج کریں۔", "أدخل نصابًا صحيحًا ومبالغ غير سالبة."), tone = NoteTone.Gold)
        message?.let { NoteBox(it) }
        CardRow(t("Understand zakat", "زکوٰۃ سمجھیں", "فهم الزكاة"), glyph = "zakat", onClick = { nav.go(ZakatLearn) })
        TextLink(t("Calculation reference", "حساب کا ماخذ", "مرجع الحساب"), { Platform.openUrl("https://islamic-relief.org/zakat-faq/") })
    }
}

@Composable
fun ZakatLearnScreen() {
    val content = koinInject<ContentRepo>(); val users = koinInject<UserRepo>(); val history by users.zakat.collectAsState(emptyList())
    FeaturePage(t("Zakat · learn & history", "زکوٰۃ کی رہنمائی اور تاریخ", "الزكاة والسجل")) {
        Loaded(Unit, { content.zakat() }) { file -> file.sections.forEach { SectionContent(it) } }
        SectionHeader(t("Saved calculations", "محفوظ حساب", "الحسابات المحفوظة")); history.forEach { row -> CardRow("${row.currency} ${money(row.zakat)}", sub = "Net ${money(row.total)} · Nisab ${money(row.nisab)}", glyph = "zakat", chevron = false) }
    }
}

@Composable
fun HajjScreen(initial: Int) {
    val content = koinInject<ContentRepo>(); val nav = LocalNavigator.current; var tab by remember { mutableStateOf(initial.coerceIn(0, 1)) }
    FeaturePage(t("Hajj & Umrah", "حج اور عمرہ", "الحج والعمرة")) {
        Art("hero_kaaba", Modifier.fillMaxWidth().height(150.dp)); Segmented(listOf(t("Hajj", "حج", "الحج"), t("Umrah", "عمرہ", "العمرة")), tab, { tab = it })
        val key = if (tab == 0) "hajj" else "umrah"
        Loaded(key, { content.guides()[key] }) { guide -> if (guide != null) { Txt(guide.intro.text(), Dj.type.bodyM); guide.steps.forEachIndexed { index, step -> CardRow(step.title.text(), sub = step.body.text(), lead = { NumBadge(index + 1) }, onClick = { nav.go(HajjStage(key, index)) }) }; References(guide.refs) } }
        CardRow(t("Travel checklist", "سفر کی چیک لسٹ", "قائمة السفر"), glyph = "luggage", onClick = { nav.go(HajjChecklist) })
        Loaded(Unit, { content.hajjExtra() }) { extra -> DjCard(Modifier.fillMaxWidth()) { SectionHeader(t("Talbiyah", "تلبیہ", "التلبية")); ArabicText(extra.talbiyah.arabic, Dj.type.arabicM); Txt(extra.talbiyah.meaning.text(), Dj.type.bodyM); References(listOf(extra.talbiyah.ref)) } }
    }
}

@Composable
fun HajjChecklistScreen() {
    val content = koinInject<ContentRepo>(); val users = koinInject<UserRepo>(); val done by users.progress("hajj_check").collectAsState(emptyList()); val scope = rememberCoroutineScope()
    FeaturePage(t("Hajj checklist", "حج کی چیک لسٹ", "قائمة الحج")) { Loaded(Unit, { content.hajjExtra() }) { extra -> extra.checklist.forEach { group ->
        SectionHeader(group.group.text()); group.items.forEach { item -> ListRow(item.text.text(), sub = item.sub?.text(), trailing = { DjCheck(done.any { it.key == item.id && it.value > 0 }, { on -> scope.launch { users.setProgress("hajj_check", item.id, if (on) 1 else 0) } }) }) }
    } } }
}

@Composable
fun HajjStageScreen(key: String, index: Int) {
    val content = koinInject<ContentRepo>()
    FeaturePage(t("Pilgrimage stage", "حج کا مرحلہ", "مرحلة النسك")) { Loaded(key to index, { content.guides()[key] }) { guide -> if (guide != null) guide.steps.getOrNull(index)?.let { GuideContent(guide.copy(steps = listOf(it))) } else NoItems() } }
}

@Composable
fun CalendarScreen() {
    val settings = koinInject<SettingsRepo>(); val s by settings.flow.collectAsState(); val content = koinInject<ContentRepo>(); val lang = LocalLang.current; val nav = LocalNavigator.current
    val current = Hijri.fromGregorian(today(), s.hijriOffset); var year by remember { mutableStateOf(current.year) }; var month by remember { mutableStateOf(current.month) }; var selected by remember { mutableStateOf(current.day) }
    FeaturePage(t("Islamic calendar", "اسلامی کیلنڈر", "التقويم الهجري")) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) { IconBtn("chevron-left", { if (month == 1) { month = 12; year-- } else month--; selected = 1 }); Txt("${Hijri.monthName(month, lang)} $year", Dj.type.titleL); IconBtn("chevron-right", { if (month == 12) { month = 1; year++ } else month++; selected = 1 }) }
        val start = Hijri.toGregorian(HijriDate(year, month, 1), s.hijriOffset); val padding = start.dayOfWeek.ordinal; val days = Hijri.monthLength(year, month)
        val labels = listOf("M", "T", "W", "T", "F", "S", "S")
        Row { labels.forEach { Txt(it, Dj.type.labelS, modifier = Modifier.weight(1f)) } }
        (List(padding) { 0 } + (1..days).toList()).chunked(7).forEach { week -> Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) { for (i in 0..6) { val day = week.getOrNull(i) ?: 0; if (day == 0) Spacer(Modifier.weight(1f)) else DjChip(lang.num(day), selected == day, { selected = day }, Modifier.weight(1f)) } } }
        Txt(lang.date(Hijri.toGregorian(HijriDate(year, month, selected), s.hijriOffset)), Dj.type.bodyM)
        NoteBox(t("Dates may vary with local moon sighting. Adjust the Hijri date in Settings.", "مقامی چاند کے مطابق تاریخ مختلف ہو سکتی ہے۔ سیٹنگز میں تاریخ درست کریں۔", "قد تختلف التواريخ حسب رؤية الهلال. عدّل التاريخ من الإعدادات."), tone = NoteTone.Gold)
        Loaded(Unit, { content.events() }) { events -> events.filter { it.month == month || it.month == 0 }.forEach { e -> CardRow(e.title.text(), sub = when { e.day == 0 -> t("Every week", "ہر ہفتے", "كل أسبوع"); e.month == 0 -> t("Every month", "ہر مہینے", "كل شهر"); else -> "${e.day} ${Hijri.monthName(month, lang)}" }, glyph = "calendar", onClick = { nav.go(EventDetail(e.id, year)) }) } }
    }
}

@Composable
fun EventDetailScreen(id: String, year: Int) {
    val content = koinInject<ContentRepo>(); val settings = koinInject<SettingsRepo>(); val s by settings.flow.collectAsState(); val lang = LocalLang.current
    FeaturePage(t("Islamic event", "اسلامی موقع", "مناسبة إسلامية")) { Loaded(id, { content.events().firstOrNull { it.id == id } }) { event -> if (event != null) {
        SectionHeader(event.title.text()); if (event.month > 0 && event.day > 0) Txt(lang.date(Hijri.toGregorian(HijriDate(year, event.month, event.day), s.hijriOffset)), Dj.type.bodyS); Txt(event.body.text(), Dj.type.bodyM); if (event.disputed) NoteBox(t("Different scholarly views exist on this date.", "اس تاریخ پر اہل علم کی مختلف آراء ہیں۔", "توجد آراء مختلفة في هذا التاريخ."), tone = NoteTone.Gold); References(event.refs)
    } else NoItems() } }
}
