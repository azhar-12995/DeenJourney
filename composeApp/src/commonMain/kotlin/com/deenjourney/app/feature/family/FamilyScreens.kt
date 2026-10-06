package com.deenjourney.app.feature.family

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.deenjourney.app.core.LocalLang
import com.deenjourney.app.core.Platform
import com.deenjourney.app.core.digits
import com.deenjourney.app.core.num
import com.deenjourney.app.core.t
import com.deenjourney.app.core.text
import com.deenjourney.app.data.content.ContentRepo
import com.deenjourney.app.data.content.KidCard
import com.deenjourney.app.data.settings.SettingsRepo
import com.deenjourney.app.data.user.ProfileE
import com.deenjourney.app.data.user.UserRepo
import com.deenjourney.app.data.user.newId
import com.deenjourney.app.design.*
import com.deenjourney.app.nav.*
import com.deenjourney.app.nav.R
import com.deenjourney.app.platform.Scheduler
import com.deenjourney.app.platform.SoundFx
import kotlinx.coroutines.launch
import org.koin.compose.koinInject

fun kindAvatar(kind: String) = when (kind) { "child" -> "boy"; "parent" -> "woman"; "senior" -> "grandpa"; else -> "man" }

@Composable
fun ageLabel(g: String): String = when (g) {
    "2-5" -> t("Age 2–5", "عمر 2–5", "العمر 2–5"); "6-9" -> t("Age 6–9", "عمر 6–9", "العمر 6–9"); "10-12" -> t("Age 10–12", "عمر 10–12", "العمر 10–12")
    "teen" -> t("Teen", "نوجوان", "مراهق"); "senior" -> t("Senior", "بزرگ", "كبير السن"); else -> t("Adult", "بالغ", "بالغ")
}

// ---------------------------------------------------------------- B01 Family profiles

@Composable
fun FamilySetupScreen(onboarding: Boolean) {
    val nav = LocalNavigator.current
    val users = koinInject<UserRepo>()
    val profiles by users.profiles.collectAsState()
    @Composable
    fun card(kind: String, avatar: String, title: String, sub: String, tint: Color, modifier: Modifier) {
        Column(
            modifier.clip(RoundedCornerShape(18.dp)).background(tint).border(1.dp, Dj.c.border, RoundedCornerShape(18.dp))
                .clickable { nav.go(AddMember(kind)) }.padding(start = 10.dp, end = 10.dp, top = 16.dp, bottom = 14.dp),
            horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Avatar(avatar, 76.dp)
            Txt(title, Dj.type.titleM, align = TextAlign.Center)
            Txt(sub, Dj.type.caption, Dj.c.text2, align = TextAlign.Center)
            Box(Modifier.size(30.dp).clip(CircleShape).background(Dj.c.primary), contentAlignment = Alignment.Center) { DjIcon("plus", 16.dp, Dj.c.onPrimary) }
        }
    }
    Screen(top = {
        AppBar(t("Create family profiles", "فیملی پروفائلز بنائیں", "إنشاء ملفات العائلة"), onBack = { nav.back() }, center = true,
            actions = { if (onboarding) TextLink(t("Skip", "چھوڑیں", "تخطٍ"), { nav.go(Goals(true)) }, color = Dj.c.text2) })
    }, bottom = { Footer { DjButton(t("Continue", "جاری رکھیں", "متابعة"), { if (onboarding) nav.go(Goals(true)) else nav.back() }, Modifier.fillMaxWidth()); if (onboarding) Pager(4, 0) } }) {
        Body(gap = 12.dp) {
            Txt(t("Add profiles for your family members so everyone gets a personalised learning experience.", "فیملی کے ہر فرد کی پروفائل بنائیں تاکہ سب کو اپنے مطابق سیکھنے کا تجربہ ملے۔", "أضف ملفات لأفراد عائلتك ليحصل كل منهم على تجربة تعلم مخصصة."), Dj.type.bodyM, Dj.c.text2, align = TextAlign.Center, modifier = Modifier.fillMaxWidth())
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                card("child", "boy", t("Child", "بچہ", "طفل"), t("Ages 2–12 · fun & interactive learning", "عمر 2–12 · دلچسپ انداز میں سیکھنا", "من 2 إلى 12 · تعلم ممتع وتفاعلي"), Dj.c.primaryTint, Modifier.weight(1f))
                card("parent", "woman", t("Parent", "والدین", "والد"), t("Guidance & family tools", "رہنمائی اور فیملی ٹولز", "إرشاد وأدوات للعائلة"), Dj.c.roseTint, Modifier.weight(1f))
            }
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                card("adult", "man", t("Adult", "بالغ", "بالغ"), t("Continue learning at your pace", "اپنی رفتار سے سیکھیں", "تعلّم بالسرعة التي تناسبك"), Dj.c.goldTint, Modifier.weight(1f))
                card("senior", "grandpa", t("Senior", "بزرگ", "كبير السن"), t("Simplified learning & larger text", "آسان انداز اور بڑا متن", "تعلم مبسط ونص أكبر"), Dj.c.surface2, Modifier.weight(1f))
            }
            if (profiles.isNotEmpty()) {
                Overline(t("Your family", "آپ کی فیملی", "عائلتك"))
                profiles.forEach { p ->
                    CardRow(p.name, sub = ageLabel(p.ageGroup) + (if (p.childMode) " · " + t("Kids mode", "بچوں کا موڈ", "وضع الأطفال") else "") + (if (p.owner) " · " + t("you", "آپ", "أنت") else ""),
                        lead = { Avatar(p.avatar, 42.dp) }, onClick = { nav.go(AddMember(p.kind, p.id)) })
                }
            }
        }
    }
}

@Composable
fun Pager(n: Int, i: Int) = Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
    repeat(n) { k -> Box(Modifier.padding(horizontal = 3.dp).size(if (k == i) 18.dp else 6.dp, 6.dp).clip(CircleShape).background(if (k == i) Dj.c.primary else Dj.c.border)) }
}

// ---------------------------------------------------------------- B04 Add / edit family member

@Composable
fun AddMemberScreen(kind: String, profileId: String?) {
    val nav = LocalNavigator.current
    val users = koinInject<UserRepo>()
    val scope = rememberCoroutineScope()
    val lang = LocalLang.current
    var existing by remember { mutableStateOf<ProfileE?>(null) }
    var name by remember { mutableStateOf("") }
    var avatar by remember { mutableStateOf(kindAvatar(kind)) }
    var relation by remember { mutableStateOf(when (kind) { "child" -> "child"; "parent" -> "spouse"; "senior" -> "parent"; else -> "sibling" }) }
    var age by remember { mutableStateOf(when (kind) { "child" -> "6-9"; "senior" -> "senior"; else -> "adult" }) }
    var childMode by remember { mutableStateOf(kind == "child") }
    var largeText by remember { mutableStateOf(kind == "senior") }
    var error by remember { mutableStateOf(false) }
    LaunchedEffect(profileId) {
        if (profileId != null) users.dao.profile(profileId)?.let { p ->
            existing = p; name = p.name; avatar = p.avatar; relation = p.relation; age = p.ageGroup; childMode = p.childMode; largeText = p.largeText
        }
    }
    val relations = listOf("self" to t("Me", "میں", "أنا"), "spouse" to t("Spouse", "شریکِ حیات", "الزوج/الزوجة"), "child" to t("Son / daughter", "بیٹا / بیٹی", "ابن / ابنة"),
        "parent" to t("Parent", "والدین", "أحد الوالدين"), "sibling" to t("Sibling", "بہن / بھائی", "أخ / أخت"), "other" to t("Other", "دیگر", "آخر"))
    Screen(top = { AppBar(if (existing != null) t("Edit profile", "پروفائل میں تبدیلی", "تعديل الملف") else t("Add family member", "فیملی ممبر شامل کریں", "إضافة فرد من العائلة"), onBack = { nav.back() }) },
        bottom = {
            Footer {
                DjButton(t("Save profile", "پروفائل محفوظ کریں", "حفظ الملف"), {
                    if (name.isBlank()) { error = true; return@DjButton }
                    scope.launch {
                        val p = (existing ?: ProfileE(id = newId(), name = name, kind = kind, updatedAt = 0)).copy(
                            name = name.trim(), avatar = avatar, relation = relation, ageGroup = age, childMode = childMode, largeText = largeText,
                            kind = if (age in setOf("2-5", "6-9", "10-12")) "child" else existing?.kind ?: kind,
                        )
                        users.saveProfile(p)
                        if (existing == null && p.kind == "child") nav.go(AgeLevel(p.id, true)) else nav.back()
                    }
                }, Modifier.fillMaxWidth())
                if (existing != null && !existing!!.owner) DjButton(t("Remove profile", "پروفائل ہٹائیں", "حذف الملف"), { scope.launch { users.deleteProfile(existing!!); nav.back() } }, Modifier.fillMaxWidth(), style = BtnStyle.DangerSoft)
            }
        }) {
        Body(gap = 12.dp) {
            Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                listOf("boy", "girl", "man", "woman", "grandpa", "grandma").forEach { a ->
                    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(4.dp), modifier = Modifier.clickable { avatar = a }) {
                        Avatar(a, 52.dp, ring = if (a == avatar) Dj.c.primary else null, ringWidth = 2.5.dp)
                        Box(Modifier.size(6.dp).clip(CircleShape).background(if (a == avatar) Dj.c.primary else Color.Transparent))
                    }
                }
            }
            DjField(name, { name = it; error = false }, label = t("Name", "نام", "الاسم"), icon = "user", error = if (error) t("Please enter a name.", "نام درج کریں۔", "أدخل الاسم.") else null)
            Txt(t("Relation", "رشتہ", "صلة القرابة"), Dj.type.labelM)
            ChipRow { relations.forEach { (k, l) -> DjChip(l, relation == k, { relation = k }) } }
            Txt(t("Age group", "عمر", "الفئة العمرية"), Dj.type.labelM)
            ChipRow { listOf("2-5", "6-9", "10-12", "teen", "adult", "senior").forEach { g -> DjChip(ageLabel(g), age == g, { age = g; childMode = g in setOf("2-5", "6-9", "10-12"); largeText = g == "senior" }) } }
            DjCard(padding = PaddingValues(horizontal = 14.dp, vertical = 4.dp)) {
                ListRow(t("Child mode", "بچوں کا موڈ", "وضع الأطفال"), sub = t("Kid-safe content, no account settings", "بچوں کے لیے محفوظ مواد، اکاؤنٹ سیٹنگز نہیں", "محتوى آمن للأطفال دون إعدادات الحساب"), lead = { IconTile("shield-check") }, trailing = { DjSwitch(childMode, { childMode = it }) })
                Hr()
                ListRow(t("Larger text", "بڑا متن", "نص أكبر"), sub = t("Recommended for seniors", "بزرگوں کے لیے بہتر", "موصى به لكبار السن"), lead = { IconTile("a-large-small") }, trailing = { DjSwitch(largeText, { largeText = it }) })
            }
        }
    }
}

// ---------------------------------------------------------------- B02 Age & learning level

@Composable
fun AgeLevelScreen(profileId: String, onboarding: Boolean) {
    val nav = LocalNavigator.current
    val users = koinInject<UserRepo>()
    val scope = rememberCoroutineScope()
    var p by remember { mutableStateOf<ProfileE?>(null) }
    var age by remember { mutableStateOf("2-5") }
    var level by remember { mutableStateOf("beginner") }
    LaunchedEffect(profileId) { users.dao.profile(profileId)?.let { p = it; age = if (it.ageGroup in setOf("2-5", "6-9", "10-12")) it.ageGroup else "6-9"; level = it.level } }
    val prof = p
    Screen(top = { AppBar(t("Set age and learning level", "عمر اور سطح منتخب کریں", "حدد العمر ومستوى التعلم"), onBack = { nav.back() }) },
        bottom = { Footer { DjButton(t("Save and continue", "محفوظ کریں اور آگے بڑھیں", "حفظ ومتابعة"), { scope.launch { prof?.let { users.saveProfile(it.copy(ageGroup = age, level = level, childMode = true)) }; nav.back() } }, Modifier.fillMaxWidth()) } }) {
        Body(gap = 12.dp) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                Avatar(prof?.avatar ?: "boy", 72.dp)
                Column(Modifier.weight(1f)) {
                    Txt(t("Child profile", "بچے کی پروفائل", "ملف الطفل") + (prof?.name?.let { " · $it" } ?: ""), Dj.type.titleM)
                    Txt(t("Tell us a bit more so we can personalise their learning.", "تھوڑا مزید بتائیں تاکہ ہم سیکھنے کو ان کے مطابق بنا سکیں۔", "أخبرنا أكثر لنخصص تعلمه."), Dj.type.bodyS, Dj.c.text2)
                }
            }
            Txt(t("Age group", "عمر", "الفئة العمرية"), Dj.type.titleS)
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                listOf("2-5" to t("Early years", "ابتدائی عمر", "السنوات الأولى"), "6-9" to t("Primary", "پرائمری", "ابتدائي"), "10-12" to t("Pre-teen", "قبل از نوجوانی", "ما قبل المراهقة")).forEach { (g, sub) ->
                    BorderBox(Modifier.weight(1f), selected = age == g, onClick = { age = g }) {
                        Column(Modifier.fillMaxWidth().padding(vertical = 12.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            Txt(LocalLang.current.digits(g.replace("-", "–")), Dj.type.titleM, if (age == g) Dj.c.primary else Dj.c.text); Txt(sub, Dj.type.caption, Dj.c.text2, align = TextAlign.Center)
                            if (age == g) DjIcon("circle-check", 16.dp, Dj.c.primary) else Spacer(Modifier.height(16.dp))
                        }
                    }
                }
            }
            Txt(t("Learning level", "سیکھنے کی سطح", "مستوى التعلم"), Dj.type.titleS)
            listOf(Triple("beginner", t("Beginner", "ابتدائی", "مبتدئ"), t("Just starting out", "ابھی شروعات", "في البداية")), Triple("intermediate", t("Intermediate", "درمیانہ", "متوسط"), t("Building knowledge", "علم بڑھا رہے ہیں", "يبني معرفته")), Triple("advanced", t("Advanced", "اعلیٰ", "متقدم"), t("Ready for deeper learning", "گہرائی سے سیکھنے کے لیے تیار", "مستعد لتعلم أعمق")))
                .forEachIndexed { i, (k, title, sub) ->
                    BorderBox(Modifier.fillMaxWidth(), selected = level == k, onClick = { level = k }) {
                        Row(Modifier.padding(horizontal = 14.dp, vertical = 12.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            Row(Modifier.size(40.dp).clip(RoundedCornerShape(12.dp)).background(Dj.c.goldTint).padding(bottom = 10.dp), horizontalArrangement = Arrangement.spacedBy(3.dp, Alignment.CenterHorizontally), verticalAlignment = Alignment.Bottom) {
                                (1..3).forEach { b -> Box(Modifier.size(4.dp, (6 + b * 5).dp).clip(RoundedCornerShape(2.dp)).background(if (b <= i + 1) Dj.c.gold else Dj.c.goldSoft)) }
                            }
                            Column(Modifier.weight(1f)) { Txt(title, Dj.type.titleS); Txt(sub, Dj.type.bodyS, Dj.c.text2) }
                            if (level == k) DjIcon("circle-check", 22.dp, Dj.c.primary) else DjRadio(false)
                        }
                    }
                }
        }
    }
}

// ---------------------------------------------------------------- B03 Learning goals

@Composable
fun GoalsScreen(onboarding: Boolean) {
    val nav = LocalNavigator.current
    val settings = koinInject<SettingsRepo>()
    val s by settings.flow.collectAsState()
    val scope = rememberCoroutineScope()
    var goal by remember(s.dailyGoal) { mutableStateOf(s.dailyGoal) }
    var focus by remember(s.focus) { mutableStateOf(s.focus.toSet()) }
    val areas = listOf("quran" to ("quran" to t("Quran", "قرآن", "القرآن")), "prayers" to ("mat" to t("Daily prayers", "روزانہ نمازیں", "الصلوات اليومية")), "knowledge" to ("lesson" to t("Islamic knowledge", "دینی علم", "العلم الشرعي")),
        "character" to ("akhlaq" to t("Good character", "اچھے اخلاق", "حسن الخلق")), "duas" to ("dua" to t("Duas & dhikr", "دعائیں اور ذکر", "الأدعية والأذكار")), "family" to ("family" to t("For my family", "میری فیملی کے لیے", "لعائلتي")))
    Screen(top = { AppBar(t("Set your learning goals", "سیکھنے کے اہداف", "حدد أهداف تعلمك"), onBack = { nav.back() }) },
        bottom = {
            Footer {
                DjButton(t("Continue", "جاری رکھیں", "متابعة"), { scope.launch { settings.update { it.copy(dailyGoal = goal, focus = focus.toList()) }; if (onboarding) nav.go(AllSet) else nav.back() } }, Modifier.fillMaxWidth())
                if (onboarding) Pager(4, 2)
            }
        }) {
        Body(gap = 14.dp) {
            Txt(t("Choose how much time you can spend daily. You can change this anytime.", "روزانہ کتنا وقت دے سکتے ہیں؟ یہ کبھی بھی بدلا جا سکتا ہے۔", "اختر الوقت الذي يمكنك تخصيصه يوميًا. يمكنك تغييره في أي وقت."), Dj.type.bodyM, Dj.c.text2, align = TextAlign.Center, modifier = Modifier.fillMaxWidth())
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                listOf(5 to t("A small step daily", "روز ایک چھوٹا قدم", "خطوة صغيرة يوميًا"), 10 to t("A balanced plan", "متوازن منصوبہ", "خطة متوازنة"), 15 to t("Go a little deeper", "تھوڑا گہرائی سے", "تعمق أكثر")).forEach { (m, sub) ->
                    BorderBox(Modifier.weight(1f), selected = goal == m, onClick = { goal = m }, radius = 16.dp) {
                        Column(Modifier.fillMaxWidth().padding(vertical = 14.dp, horizontal = 4.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            if (goal == m) Box(Modifier.size(34.dp).clip(CircleShape).background(Dj.c.primary), contentAlignment = Alignment.Center) { DjIcon("check", 18.dp, Dj.c.onPrimary) } else DjIcon("clock", 30.dp, Dj.c.goldText)
                            Txt(t("${m} minutes", "$m منٹ", "${LocalLang.current.num(m)} دقائق"), Dj.type.titleS, if (goal == m) Dj.c.primary else Dj.c.text)
                            Txt(sub, Dj.type.caption, Dj.c.text2, align = TextAlign.Center)
                        }
                    }
                }
            }
            Txt(t("What would you like to focus on?", "آپ کس چیز پر توجہ دینا چاہتے ہیں؟", "على ماذا تود التركيز؟"), Dj.type.titleS)
            areas.chunked(2).forEach { row ->
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    row.forEach { (k, v) ->
                        val on = k in focus
                        BorderBox(Modifier.weight(1f), selected = on, onClick = { focus = if (on) focus - k else focus + k }, radius = 12.dp) {
                            Row(Modifier.padding(horizontal = 12.dp, vertical = 12.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                if (on) DjIcon("circle-check", 18.dp, Dj.c.primary) else Glyph(v.first, 22.dp)
                                Txt(v.second, Dj.type.labelM, if (on) Dj.c.primary else Dj.c.text)
                            }
                        }
                    }
                }
            }
        }
    }
}

// ---------------------------------------------------------------- B05 All set

@Composable
fun AllSetScreen() {
    val nav = LocalNavigator.current
    val settings = koinInject<SettingsRepo>(); val users = koinInject<UserRepo>()
    val s by settings.flow.collectAsState()
    val profiles by users.profiles.collectAsState()
    val owner = profiles.firstOrNull { it.owner }
    val scope = rememberCoroutineScope()
    Screen(statusBarPadding = false) {
        Box(Modifier.fillMaxSize()) {
            Art("hero_roadmap", Modifier.fillMaxWidth().height(420.dp), alignment = Alignment.Center)
            Column(Modifier.fillMaxWidth().align(Alignment.BottomCenter).clip(RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)).background(Dj.c.bg).padding(horizontal = 18.dp, vertical = 22.dp).navigationBarsPadding(),
                verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Txt(t("You’re all set, ${owner?.name ?: ""}!", "سب تیار ہے، ${owner?.name ?: ""}!", "كل شيء جاهز يا ${owner?.name ?: ""}!"), Dj.type.headline)
                Txt(t("Your family’s journey starts today. Small steps, every day.", "آپ کی فیملی کا سفر آج سے شروع۔ روز چھوٹے قدم۔", "رحلة عائلتك تبدأ اليوم. خطوات صغيرة كل يوم."), Dj.type.bodyM, Dj.c.text2)
                DjCard(padding = PaddingValues(horizontal = 14.dp, vertical = 4.dp)) {
                    ListRow(t("Family", "فیملی", "العائلة"), sub = t("${profiles.size} profiles", "${profiles.size} پروفائلز", "${profiles.size} ملفات") + " · " + profiles.joinToString(", ") { it.name }, lead = { GlyphTile("family", 40.dp) }, trailing = { DjIcon("circle-check", 20.dp, Dj.c.primary) })
                    ListRow(t("Daily goal", "روزانہ ہدف", "الهدف اليومي"), sub = t("${s.dailyGoal} minutes", "${s.dailyGoal} منٹ", "${s.dailyGoal} دقائق"), lead = { GlyphTile("progress", 40.dp) }, trailing = { DjIcon("circle-check", 20.dp, Dj.c.primary) })
                    ListRow(t("Prayer times", "نماز کے اوقات", "مواقيت الصلاة"), sub = s.location?.name ?: t("Location not set", "مقام منتخب نہیں", "لم يحدد الموقع"), lead = { GlyphTile("prayer_time", 40.dp) },
                        trailing = { if (s.location != null) DjIcon("circle-check", 20.dp, Dj.c.primary) else TextLink(t("Set", "منتخب", "حدد"), { nav.go(PrayerSetup(true)) }) })
                }
                Gap(2.dp)
                DjButton(t("Start my journey", "اپنا سفر شروع کریں", "ابدأ رحلتي"), {
                    scope.launch { settings.update { it.copy(setupDone = true, familyDone = true) }; Scheduler.reschedule(); nav.reset(Home) }
                }, Modifier.fillMaxWidth(), trail = "arrow-right")
            }
        }
    }
}

// ---------------------------------------------------------------- C03 Switch profile (sheet)

@Composable
fun SwitchProfileSheet(onDismiss: () -> Unit) {
    val nav = LocalNavigator.current
    val users = koinInject<UserRepo>(); val settings = koinInject<SettingsRepo>()
    val profiles by users.profiles.collectAsState()
    val active by users.active.collectAsState()
    val s by settings.flow.collectAsState()
    val scope = rememberCoroutineScope()
    var askPin by remember { mutableStateOf<ProfileE?>(null) }
    DjSheet(onDismiss) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Txt(t("Who is learning?", "کون سیکھ رہا ہے؟", "من يتعلم الآن؟"), Dj.type.titleL, modifier = Modifier.weight(1f))
            TextLink(t("Manage", "انتظام", "إدارة"), { onDismiss(); nav.go(ParentDashboard) })
        }
        profiles.forEach { p ->
            val on = p.id == active?.id
            ListRow(p.name, sub = (if (p.owner) t("Account owner", "اکاؤنٹ ہولڈر", "صاحب الحساب") else ageLabel(p.ageGroup)) + if (p.childMode) " · " + t("Kids mode", "بچوں کا موڈ", "وضع الأطفال") else "",
                lead = { Avatar(p.avatar, 44.dp, ring = if (on) Dj.c.primary else null) }, trailing = { if (on) DjIcon("circle-check", 22.dp, Dj.c.primary) else DjRadio(false) },
                onClick = {
                    if (on) onDismiss()
                    else if (active?.childMode == true && s.childLock && s.pinHash != null) askPin = p
                    else scope.launch { users.setActive(p.id); onDismiss() }
                })
        }
        DjButton(t("Add family member", "فیملی ممبر شامل کریں", "إضافة فرد"), { onDismiss(); nav.go(AddMember()) }, Modifier.fillMaxWidth(), style = BtnStyle.Soft, lead = "user-plus")
        if (s.childLock) NoteBox(t("Child mode lock is on — a PIN is needed to leave a child profile.", "چائلڈ لاک آن ہے — بچے کی پروفائل چھوڑنے کے لیے پن درکار ہے۔", "قفل الأطفال مفعّل — يلزم رمز للخروج من ملف الطفل."), icon = "lock", tone = NoteTone.Gold)
    }
    askPin?.let { target ->
        PinDialog(onDismiss = { askPin = null }, onOk = { pin ->
            if (hashPin(pin) == s.pinHash) { scope.launch { users.setActive(target.id); askPin = null; onDismiss() } ; true } else false
        })
    }
}

fun hashPin(pin: String): String {
    var h = 1125899906842597L
    for (c in "dj:$pin") h = 31 * h + c.code
    return h.toString(16)
}

@Composable
fun PinDialog(onDismiss: () -> Unit, onOk: (String) -> Boolean, title: String = t("Enter parent PIN", "والدین کا پن درج کریں", "أدخل رمز الوالدين")) {
    var pin by remember { mutableStateOf("") }
    var wrong by remember { mutableStateOf(false) }
    androidx.compose.material3.AlertDialog(onDismissRequest = onDismiss, containerColor = Dj.c.surface, shape = RoundedCornerShape(24.dp),
        title = { Txt(title, Dj.type.titleL) },
        text = { DjField(pin, { if (it.length <= 6 && it.all(Char::isDigit)) { pin = it; wrong = false } }, placeholder = "••••", keyboard = KeyboardType.NumberPassword, password = true, error = if (wrong) t("Wrong PIN", "غلط پن", "رمز خاطئ") else null) },
        confirmButton = { DjButton(t("Unlock", "کھولیں", "فتح"), { wrong = !onOk(pin) }, small = true) },
        dismissButton = { DjButton(t("Cancel", "منسوخ", "إلغاء"), onDismiss, style = BtnStyle.Ghost, small = true) })
}

// ---------------------------------------------------------------- G03 Parent dashboard

@Composable
fun ParentDashboardScreen() {
    val nav = LocalNavigator.current
    val users = koinInject<UserRepo>(); val content = koinInject<ContentRepo>()
    val profiles by users.profiles.collectAsState()
    var selected by remember { mutableStateOf<String?>(null) }
    val kids = profiles.filter { !it.owner }
    val sel = profiles.firstOrNull { it.id == selected } ?: kids.firstOrNull() ?: profiles.firstOrNull()
    val progress by (sel?.let { users.progressFor(it.id) } ?: kotlinx.coroutines.flow.flowOf(emptyList())).collectAsState(emptyList())
    var totalLessons by remember { mutableStateOf(1) }
    LaunchedEffect(Unit) { totalLessons = content.allLessons().size.coerceAtLeast(1) }
    val lessonsDone = progress.count { it.kind == "lesson" && it.value > 0 }
    val namesDone = progress.count { it.kind == "name" }
    val qaidaDone = progress.count { it.kind == "qaida" }
    Screen(top = { AppBar(t("Parent dashboard", "والدین کا ڈیش بورڈ", "لوحة الوالدين"), onBack = { nav.back() }) }) {
        Body(gap = 12.dp) {
            NoteBox(t("A safer, kinder digital space for your family’s Deen journey.", "آپ کی فیملی کے دینی سفر کے لیے محفوظ اور مہربان ڈیجیٹل ماحول۔", "مساحة رقمية أكثر أمانًا ولطفًا لرحلة عائلتك."), icon = "shield-check")
            SectionHeader(t("Family profiles", "فیملی پروفائلز", "ملفات العائلة"), action = t("Manage", "انتظام", "إدارة"), onAction = { nav.go(FamilySetup(false)) })
            Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                profiles.forEach { p ->
                    val on = p.id == sel?.id
                    Column(Modifier.clip(RoundedCornerShape(14.dp)).background(if (on) Dj.c.primaryTint else Color.Transparent).clickable { selected = p.id }.padding(horizontal = 8.dp, vertical = 8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Avatar(p.avatar, 48.dp, ring = if (on) Dj.c.primary else null); Txt(p.name, Dj.type.labelM); Txt(ageLabel(p.ageGroup), Dj.type.caption, Dj.c.text2)
                    }
                }
                Column(Modifier.clickable { nav.go(AddMember()) }.padding(8.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Box(Modifier.size(48.dp).clip(CircleShape).border(1.dp, Dj.c.border, CircleShape), contentAlignment = Alignment.Center) { DjIcon("plus", 20.dp, Dj.c.text2) }
                    Txt(t("Add", "شامل", "إضافة"), Dj.type.labelM, Dj.c.text2)
                }
            }
            if (sel != null) {
                SectionHeader(t("Learning progress · ${sel.name}", "سیکھنے کی پیش رفت · ${sel.name}", "تقدم التعلم · ${sel.name}"))
                DjCard {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceAround) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(6.dp)) { Ring(lessonsDone.toFloat() / totalLessons, 64.dp); Txt(t("Lessons", "اسباق", "الدروس"), Dj.type.labelS, Dj.c.text2) }
                        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(6.dp)) { Ring(namesDone / 99f, 64.dp, color = Dj.c.gold); Txt(t("99 Names", "اسمائے حسنیٰ", "الأسماء الحسنى"), Dj.type.labelS, Dj.c.text2) }
                        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(6.dp)) { Ring(qaidaDone / 29f, 64.dp, color = Dj.c.rose); Txt(t("Qaida", "قاعدہ", "القاعدة"), Dj.type.labelS, Dj.c.text2) }
                    }
                }
                DjCard(padding = PaddingValues(horizontal = 14.dp, vertical = 4.dp)) {
                    ListRow(t("Edit profile", "پروفائل میں تبدیلی", "تعديل الملف"), sub = ageLabel(sel.ageGroup), lead = { IconTile("user") }, chevron = true, onClick = { nav.go(AddMember(sel.kind, sel.id)) })
                    if (sel.childMode) { Hr(); ListRow(t("Child-safe settings", "بچوں کی حفاظت کی سیٹنگز", "إعدادات أمان الطفل"), sub = t("Kid-safe content, PIN", "محفوظ مواد، پن", "محتوى آمن، رمز"), lead = { IconTile("shield-check") }, chevron = true, onClick = { nav.go(ChildSafe(sel.id)) }) }
                    Hr(); ListRow(t("Privacy & controls", "پرائیویسی اور کنٹرول", "الخصوصية والتحكم"), sub = t("Manage data and preferences", "ڈیٹا اور ترجیحات", "إدارة البيانات والتفضيلات"), lead = { IconTile("lock") }, chevron = true, onClick = { nav.go(Privacy) })
                }
            }
        }
    }
}

// ---------------------------------------------------------------- G04 Child-safe settings

@Composable
fun ChildSafeScreen(profileId: String) {
    val nav = LocalNavigator.current
    val users = koinInject<UserRepo>(); val settings = koinInject<SettingsRepo>()
    val s by settings.flow.collectAsState()
    val scope = rememberCoroutineScope()
    var p by remember { mutableStateOf<ProfileE?>(null) }
    var setPin by remember { mutableStateOf(false) }
    LaunchedEffect(profileId) { p = users.dao.profile(profileId) }
    val prof = p ?: return
    Screen(top = { AppBar(t("Child-safe settings", "بچوں کی حفاظت", "إعدادات أمان الطفل"), sub = prof.name + " · " + ageLabel(prof.ageGroup), onBack = { nav.back() }) }) {
        Body(gap = 12.dp) {
            DjCard(padding = PaddingValues(horizontal = 14.dp, vertical = 4.dp)) {
                ListRow(t("Kid-safe content only", "صرف بچوں کے لیے محفوظ مواد", "محتوى آمن للأطفال فقط"), sub = t("Stories, duas, manners and Qaida", "کہانیاں، دعائیں، آداب اور قاعدہ", "قصص وأدعية وآداب والقاعدة"), lead = { IconTile("shield-check") },
                    trailing = { DjSwitch(prof.childMode, { v -> scope.launch { p = users.saveProfile(prof.copy(childMode = v)) } }) })
                Hr()
                ListRow(t("PIN to leave kids mode", "بچوں کا موڈ چھوڑنے کے لیے پن", "رمز للخروج من وضع الأطفال"), sub = if (s.pinHash != null) t("PIN is set", "پن مقرر ہے", "تم تعيين الرمز") else t("4-digit parent PIN", "4 ہندسوں کا پن", "رمز من 4 أرقام"), lead = { IconTile("key-round") },
                    trailing = { DjSwitch(s.childLock && s.pinHash != null, { v -> if (v && s.pinHash == null) setPin = true else scope.launch { settings.update { it.copy(childLock = v) } } }) })
                Hr()
                ListRow(t("Change PIN", "پن تبدیل کریں", "تغيير الرمز"), lead = { IconTile("lock") }, chevron = true, onClick = { setPin = true })
            }
            NoteBox(t("Children never need their own login. Their progress is stored under your family account.", "بچوں کو الگ لاگ اِن کی ضرورت نہیں۔ ان کی پیش رفت آپ کے فیملی اکاؤنٹ میں محفوظ رہتی ہے۔", "لا يحتاج الأطفال إلى حساب خاص. يُحفظ تقدمهم في حساب عائلتك."), tone = NoteTone.Gold)
        }
    }
    if (setPin) PinDialog(onDismiss = { setPin = false }, title = t("Choose a 4-digit PIN", "4 ہندسوں کا پن منتخب کریں", "اختر رمزًا من 4 أرقام"), onOk = { pin ->
        if (pin.length < 4) false else { scope.launch { settings.update { it.copy(pinHash = hashPin(pin), childLock = true) } }; setPin = false; true }
    })
}

// ---------------------------------------------------------------- G01 Kids 2–5 / G02 Kids home

private val fallbackGreetings = listOf(
    KidCard("salam", "Assalamu alaikum", "ٱلسَّلَامُ عَلَيْكُمْ", com.deenjourney.app.core.L("Peace be upon you", "آپ پر سلامتی ہو", "السلام عليكم"), "boy"),
    KidCard("reply", "Wa alaikumus-salam", "وَعَلَيْكُمُ ٱلسَّلَامُ", com.deenjourney.app.core.L("And upon you be peace", "اور آپ پر بھی سلامتی ہو", "وعليكم السلام"), "girl"),
    KidCard("bismillah", "Bismillah", "بِسْمِ ٱللَّهِ", com.deenjourney.app.core.L("In the name of Allah", "اللہ کے نام سے", "باسم الله"), "grandpa"),
    KidCard("jazak", "JazakAllahu khayran", "جَزَاكَ ٱللَّهُ خَيْرًا", com.deenjourney.app.core.L("May Allah reward you with good", "اللہ آپ کو بہترین بدلہ دے", "جزاك الله خيرًا"), "woman"),
)

@Composable
fun KidsSmallScreen() {
    val nav = LocalNavigator.current
    val content = koinInject<ContentRepo>()
    var tab by remember { mutableStateOf(0) }
    var greetings by remember { mutableStateOf(fallbackGreetings) }
    var manners by remember { mutableStateOf(emptyList<KidCard>()) }
    LaunchedEffect(Unit) { content.kids().let { k -> if (k.greetings.isNotEmpty()) greetings = k.greetings; manners = k.manners } }
    val cards = if (tab == 0) greetings else manners.ifEmpty { greetings }
    Screen(top = { AppBar(t("Let’s learn together", "آئیں مل کر سیکھیں", "لنتعلم معًا"), onBack = { nav.back() }, actions = { Pill(t("Age 2–5", "عمر 2–5", "العمر 2–5")) }) }) {
        Body(gap = 12.dp) {
            Txt(t("Short, fun lessons for early learners. Best experienced with a parent or caregiver.", "چھوٹے بچوں کے لیے مختصر اور دلچسپ اسباق — والدین کے ساتھ بہترین۔", "دروس قصيرة وممتعة للصغار — الأفضل مع أحد الوالدين."), Dj.type.bodyS, Dj.c.text2, align = TextAlign.Center, modifier = Modifier.fillMaxWidth())
            Segmented(listOf(t("Greetings", "سلام", "التحية"), t("Good manners", "اچھے آداب", "الآداب")), tab, { tab = it })
            cards.chunked(2).forEachIndexed { r, row ->
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    row.forEachIndexed { i, c ->
                        val tint = listOf(Dj.c.primaryTint, Dj.c.roseTint, Dj.c.goldTint, Dj.c.infoTint)[(r * 2 + i) % 4]
                        Column(Modifier.weight(1f).clip(RoundedCornerShape(20.dp)).background(tint).border(1.dp, Dj.c.border, RoundedCornerShape(20.dp)).clickable { Platform.vibrate() }) {
                            Box(Modifier.fillMaxWidth().height(110.dp), contentAlignment = Alignment.Center) { Avatar(c.avatar, 92.dp) }
                            Column(Modifier.fillMaxWidth().background(Dj.c.surface).padding(10.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                Txt(c.title, Dj.type.titleS, align = TextAlign.Center)
                                ArabicText(c.arabic, Dj.type.arabicM, Dj.c.primary, center = true)
                                Txt(c.meaning.text(), Dj.type.caption, Dj.c.text2, align = TextAlign.Center)
                            }
                        }
                    }
                    if (row.size == 1) Spacer(Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
fun KidsHomeContent(p: ProfileE, onSwitch: () -> Unit) {
    val nav = LocalNavigator.current
    val users = koinInject<UserRepo>()
    val stars by users.allProgress().collectAsState(emptyList())
    @Composable
    fun tile(g: String, title: String, tint: Color, r: R, modifier: Modifier) =
        Column(modifier.clip(RoundedCornerShape(20.dp)).background(tint).clickable { nav.go(r) }.padding(vertical = 14.dp, horizontal = 8.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Glyph(g, 46.dp); Txt(title, Dj.type.titleS, align = TextAlign.Center)
        }
    Column(Modifier.fillMaxSize()) {
        Row(Modifier.fillMaxWidth().padding(horizontal = 18.dp, vertical = 6.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Avatar(p.avatar, 52.dp, ring = Dj.c.gold)
            Column(Modifier.weight(1f)) { Txt(t("Hi ${p.name}!", "سلام ${p.name}!", "مرحبًا ${p.name}!"), Dj.type.headline); Txt(t("Ready for today’s adventure?", "آج کی مہم کے لیے تیار؟", "مستعد لمغامرة اليوم؟"), Dj.type.bodyS, Dj.c.text2) }
            Pill(LocalLang.current.num(stars.count { it.value > 0 }), icon = "star", fill = Dj.c.goldTint, color = Dj.c.goldText, style = Dj.type.titleS)
            IconBtn("lock", onSwitch, tint = Dj.c.text3)
        }
        Body(gap = 10.dp) {
            Row(Modifier.fillMaxWidth().clip(RoundedCornerShape(22.dp)).background(Dj.c.greenGradient).clickable { nav.go(Story("nuh", kids = true)) }.padding(14.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Art("prophet_nuh", Modifier.size(84.dp).clip(RoundedCornerShape(16.dp)))
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                    Txt(t("Today’s adventure", "آج کی مہم", "مغامرة اليوم"), Dj.type.labelS, Color(0xFFE8D5A6))
                    Txt(t("Nuh (AS) and the great Ark", "نوح علیہ السلام اور بڑی کشتی", "نوح عليه السلام والسفينة العظيمة"), Dj.type.titleM, Color.White)
                }
                Box(Modifier.size(44.dp).clip(CircleShape).background(Color.White.copy(alpha = 0.16f)), contentAlignment = Alignment.Center) { DjIcon("play", 20.dp, Color.White) }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) { tile("story", t("Prophet stories", "انبیاء کی کہانیاں", "قصص الأنبياء"), Dj.c.goldTint, Prophets, Modifier.weight(1f)); tile("dua", t("My duas", "میری دعائیں", "أدعيتي"), Dj.c.primaryTint, Duas, Modifier.weight(1f)) }
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) { tile("salah", t("Learn Salah", "نماز سیکھیں", "تعلم الصلاة"), Dj.c.infoTint, LearnSalah, Modifier.weight(1f)); tile("names", t("Allah’s names", "اللہ کے نام", "أسماء الله"), Dj.c.roseTint, NamesGrid, Modifier.weight(1f)) }
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) { tile("qaida", t("Qaida", "قاعدہ", "القاعدة"), Dj.c.primaryTint, Qaida, Modifier.weight(1f)); tile("medal", t("My badges", "میرے بیجز", "شاراتي"), Dj.c.goldTint, Progress, Modifier.weight(1f)) }
            if (p.ageGroup == "2-5") DjButton(t("Greetings & manners", "سلام اور آداب", "التحية والآداب"), { nav.go(KidsSmall) }, Modifier.fillMaxWidth(), style = BtnStyle.Soft, lead = "volume-2")
        }
    }
}
