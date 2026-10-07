package com.deenjourney.app.feature.account

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.deenjourney.app.core.*
import com.deenjourney.app.data.auth.AuthRepo
import com.deenjourney.app.data.auth.AuthError
import com.deenjourney.app.data.content.ContentRepo
import com.deenjourney.app.data.hadith.Collections
import com.deenjourney.app.data.hadith.HadithStore
import com.deenjourney.app.data.net.Reciters as AudioReciters
import com.deenjourney.app.data.net.AppJson
import com.deenjourney.app.data.net.download
import com.deenjourney.app.data.quran.QuranRepo
import com.deenjourney.app.data.quran.QuranNames
import com.deenjourney.app.data.settings.SettingsRepo
import com.deenjourney.app.data.sync.SyncRepo
import com.deenjourney.app.data.sync.SyncState
import com.deenjourney.app.data.user.*
import com.deenjourney.app.design.*
import com.deenjourney.app.feature.*
import com.deenjourney.app.feature.quran.routeForQuranKey
import com.deenjourney.app.nav.*
import com.deenjourney.app.nav.R
import com.deenjourney.app.platform.Scheduler
import com.deenjourney.app.platform.RecitationPlayer
import com.deenjourney.app.data.sync.accountFirestore
import io.ktor.client.HttpClient
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.serialization.json.*
import org.koin.compose.koinInject

@Composable
fun ProfileScreen() {
    val auth = koinInject<AuthRepo>(); val user by auth.user.collectAsState(); val users = koinInject<UserRepo>(); val p by users.active.collectAsState(); val nav = LocalNavigator.current
    val settings = koinInject<SettingsRepo>(); val s by settings.flow.collectAsState(); val sync = koinInject<SyncRepo>(); val state by sync.state.collectAsState(); val scope = rememberCoroutineScope()
    FeaturePage(t("Profile", "پروفائل", "الملف الشخصي"), actions = { IconBtn("settings", { nav.go(SettingsRoute) }) }) {
        Avatar(p?.avatar ?: "man", 84.dp); Txt(user?.name ?: p?.name.orEmpty(), Dj.type.headline); Txt(user?.email.orEmpty(), Dj.type.bodyS)
        CardRow(t("Edit profile", "پروفائل تبدیل کریں", "تعديل الملف"), icon = "user", onClick = { nav.go(EditProfile) })
        CardRow(t("Family", "فیملی", "العائلة"), glyph = "family", onClick = { nav.go(FamilySetup(false)) })
        CardRow(t("Saved items", "محفوظ آئٹمز", "المحفوظات"), glyph = "bookmark", onClick = { nav.go(Saved) })
        SwitchRow(t("Sync & backup", "سنک اور بیک اپ", "المزامنة والنسخ الاحتياطي"), s.syncEnabled, { on -> scope.launch { settings.update { it.copy(syncEnabled = on) }; if (on) sync.syncNow() } }, state.name)
        DjButton(t("Sync now", "ابھی سنک کریں", "مزامنة الآن"), { scope.launch { sync.syncNow() } }, enabled = s.syncEnabled, style = BtnStyle.Soft)
        if (state == SyncState.Error || state == SyncState.Offline)
            Txt(t("Backup incomplete. Connect to the internet and tap Sync now before reinstalling or changing devices.", "بیک اپ مکمل نہیں ہوا۔ ایپ دوبارہ انسٹال کرنے یا ڈیوائس بدلنے سے پہلے انٹرنیٹ سے جڑ کر ابھی سنک کریں۔", "النسخ الاحتياطي غير مكتمل. اتصل بالإنترنت واضغط مزامنة الآن قبل إعادة التثبيت أو تغيير الجهاز."), Dj.type.bodyS, Dj.c.text2)
        CardRow(t("Privacy & data", "پرائیویسی اور ڈیٹا", "الخصوصية والبيانات"), icon = "shield", onClick = { nav.go(Privacy) })
        CardRow(t("Help & FAQ", "مدد اور سوالات", "المساعدة والأسئلة"), icon = "help", onClick = { nav.go(Help) })
        CardRow(t("Report a correction", "غلطی کی نشاندہی", "الإبلاغ عن تصحيح"), icon = "flag", onClick = { nav.go(Correction()) })
    }
}

@Composable
fun EditProfileScreen() {
    val auth = koinInject<AuthRepo>(); val users = koinInject<UserRepo>(); val p by users.active.collectAsState(); val u by auth.user.collectAsState(); val scope = rememberCoroutineScope(); val lang = LocalLang.current
    var name by remember(p?.id, p?.name) { mutableStateOf(p?.name.orEmpty()) }; var avatar by remember(p?.id) { mutableStateOf(p?.avatar ?: "man") }
    var currentPassword by remember { mutableStateOf("") }; var newPassword by remember { mutableStateOf("") }; var message by remember { mutableStateOf<String?>(null) }; var busy by remember { mutableStateOf(false) }
    FeaturePage(t("Edit profile", "پروفائل میں تبدیلی", "تعديل الملف")) {
        Avatar(avatar, 84.dp); ChipRow { listOf("man", "woman", "boy", "girl", "grandpa", "grandma").forEach { a -> DjChip(a, avatar == a, { avatar = a }) } }
        DjField(name, { name = it }, label = t("Name", "نام", "الاسم")); Txt(u?.email.orEmpty(), Dj.type.bodyS)
        DjButton(t("Save profile", "پروفائل محفوظ", "حفظ الملف"), { scope.launch { busy = true; val profile = p; if (profile != null) { val result = if (profile.owner) auth.updateName(name.trim()) else Result.success(Unit); if (result.isSuccess) { users.saveProfile(profile.copy(name = name.trim(), avatar = avatar)); message = lang.pick("Saved", "محفوظ", "تم الحفظ") } else message = AuthError.from(result.exceptionOrNull()!!).message(lang) }; busy = false } }, Modifier.fillMaxWidth(), loading = busy, enabled = name.isNotBlank() && p != null)
        if (u?.verified == false) TextLink(t("Send verification email", "تصدیق کی ای میل", "إرسال بريد التحقق"), { scope.launch { val result = auth.resendVerification(); message = if (result.isSuccess) "Verification email sent" else AuthError.from(result.exceptionOrNull()!!).message(lang) } })
        if (u?.providers?.contains("password") == true) {
            SectionHeader(t("Change password", "پاس ورڈ تبدیل", "تغيير كلمة المرور"))
            DjField(currentPassword, { currentPassword = it }, label = t("Current password", "موجودہ پاس ورڈ", "كلمة المرور الحالية"), password = true)
            DjField(newPassword, { newPassword = it }, label = t("New password", "نیا پاس ورڈ", "كلمة المرور الجديدة"), password = true)
            DjButton(t("Change password", "پاس ورڈ تبدیل", "تغيير كلمة المرور"), { scope.launch { val result = auth.changePassword(currentPassword, newPassword); message = if (result.isSuccess) "Password updated" else AuthError.from(result.exceptionOrNull()!!).message(lang); if (result.isSuccess) { currentPassword = ""; newPassword = "" } } }, enabled = currentPassword.isNotBlank() && com.deenjourney.app.data.auth.Validation.passwordOk(newPassword), style = BtnStyle.Soft)
        }
        message?.let { NoteBox(it) }
    }
}

@Composable
fun SettingsScreen() {
    val settings = koinInject<SettingsRepo>(); val s by settings.flow.collectAsState(); val scope = rememberCoroutineScope(); val nav = LocalNavigator.current
    fun update(block: (com.deenjourney.app.data.settings.AppSettings) -> com.deenjourney.app.data.settings.AppSettings) { scope.launch { settings.update(block); Scheduler.reschedule() } }
    FeaturePage(t("Settings", "سیٹنگز", "الإعدادات")) {
        SectionHeader(t("Appearance", "ظاہری انداز", "المظهر"))
        ChipRow { listOf("system", "light", "dark").forEach { theme -> DjChip(theme, s.theme == theme, { update { it.copy(theme = theme) } }) } }
        SectionHeader(t("Text size", "متن کا سائز", "حجم النص"))
        ChipRow { listOf(1f to t("Normal", "عام", "عادي"), 1.15f to t("Large", "بڑا", "كبير"), 1.3f to t("Extra large", "بہت بڑا", "كبير جدًا")).forEach { (scale, label) -> DjChip(label, s.textScale == scale, { update { it.copy(textScale = scale) } }) } }
        SectionHeader(t("Language", "زبان", "اللغة")); ChipRow { Lang.entries.forEach { lang -> DjChip(lang.nativeName, s.language == lang, { update { it.copy(lang = lang.code) } }) } }
        NoteBox(t("Urdu and Arabic use right-to-left layout automatically.", "اردو اور عربی میں دائیں سے بائیں ترتیب خودکار ہے۔", "يُستخدم الاتجاه من اليمين إلى اليسار تلقائيًا بالعربية والأردية."))
        SectionHeader(t("Fiqh school", "فقہ", "المذهب الفقهي")); ChipRow { listOf("hanafi", "shafii", "maliki", "hanbali").forEach { school -> DjChip(school, s.fiqh == school, { update { it.copy(fiqh = school) } }) } }
        CardRow(t("Prayer & adhan settings", "نماز اور اذان", "إعدادات الصلاة والأذان"), glyph = "prayer_time", onClick = { nav.go(PrayerSettings) })
        CardRow(t("Location", "مقام", "الموقع"), sub = s.location?.name, icon = "map-pin", onClick = { nav.go(ChooseLocation()) })
        SectionHeader(t("Hijri adjustment", "ہجری تاریخ کی درستگی", "تعديل التاريخ الهجري")); ChipRow { (-2..2).forEach { offset -> DjChip(if (offset > 0) "+$offset" else offset.toString(), s.hijriOffset == offset, { update { it.copy(hijriOffset = offset) } }) } }
        CardRow(t("Learning goals", "سیکھنے کے اہداف", "أهداف التعلم"), glyph = "roadmap", onClick = { nav.go(Goals(false)) })
        CardRow(t("Downloads", "ڈاؤن لوڈز", "التنزيلات"), glyph = "download", onClick = { nav.go(Downloads) })
        CardRow(t("About & sources", "تعارف اور ماخذ", "حول التطبيق والمصادر"), icon = "info", onClick = { nav.go(About) })
        CardRow(t("Privacy & data", "پرائیویسی اور ڈیٹا", "الخصوصية والبيانات"), icon = "shield", onClick = { nav.go(Privacy) })
    }
}

@Composable
fun DownloadsScreen() {
    val store = koinInject<HadithStore>(); val settings = koinInject<SettingsRepo>(); val s by settings.flow.collectAsState(); val repo = koinInject<QuranRepo>(); val http = koinInject<HttpClient>(); val scope = rememberCoroutineScope()
    val downloads by store.progress.collectAsState(); var installed by remember { mutableStateOf(emptySet<String>()) }; var tab by remember { mutableStateOf(0) }; var audioProgress by remember { mutableStateOf<Float?>(null) }; var message by remember { mutableStateOf<String?>(null) }; var revision by remember { mutableStateOf(0) }
    var confirmRemove by remember { mutableStateOf<String?>(null) }
    LaunchedEffect(revision) { installed = store.installed() }
    FeaturePage(t("Downloads", "ڈاؤن لوڈز", "التنزيلات")) {
        UTabs(listOf(t("Quran", "قرآن", "القرآن"), t("Audio", "آڈیو", "الصوت"), t("Hadith", "حدیث", "الحديث")), tab, { tab = it })
        StatBox("${Platform.freeSpaceBytes() / (1024 * 1024)} MB", t("Available storage", "خالی جگہ", "المساحة المتاحة"))
        if (tab == 0) {
            NoteBox(t("All 114 surahs, Arabic text, English/Urdu translations and lessons are included for offline use.", "114 سورتیں، عربی متن، اردو/انگریزی ترجمے اور اسباق آف لائن شامل ہیں۔", "السور الـ١١٤ والنص العربي والترجمات والدروس متاحة دون اتصال."), icon = "circle-check")
        } else if (tab == 2) Collections.all.forEach { c ->
            CardRow(c.name.text(), sub = "${c.sizeMb} MB", glyph = "hadith", chevron = false, trailing = {
                if (c.id in installed) IconBtn("trash-2", { confirmRemove = c.id }) else IconBtn("download", { scope.launch { if (!store.install(c.id)) message = "Download failed. Please retry."; revision++ } })
            }); downloads[c.id]?.let { DjProgress(it) }
        } else {
            audioProgress?.let { DjProgress(it) }; Txt(AudioReciters.of(s.quran.reciter).name, Dj.type.titleM)
            Loaded(revision to s.quran.reciter, { repo.surahs().map { surah -> val reciter = AudioReciters.of(s.quran.reciter); Triple(surah, (1..surah.ayas).count { Platform.fileExists(AudioReciters.localPath(reciter, surah.id, it)) }, reciter) } }) { rows -> rows.forEach { (surah, count, reciter) ->
                CardRow(surah.nameTr, sub = "$count/${surah.ayas}", glyph = "audio", chevron = false, trailing = {
                    Row {
                        IconBtn("download", { if (audioProgress == null) scope.launch { audioProgress = 0f; var ok = true; val ayat = repo.ayat(surah.id); ayat.forEachIndexed { index, a -> val path = AudioReciters.localPath(reciter, a.sura, a.aya); if (!Platform.fileExists(path) && !http.download(AudioReciters.everyAyahUrl(reciter, a.sura, a.aya), path)) ok = false; audioProgress = (index + 1f) / ayat.size }; audioProgress = null; message = if (ok) "Download complete" else "Some audio failed. Retry to resume."; revision++ } })
                        if (count > 0) IconBtn("trash-2", { confirmRemove = "audio:${reciter.id}:${surah.id}" })
                    }
                })
            } }
        }
        message?.let { NoteBox(it) }
    }
    confirmRemove?.let { key -> ConfirmDialog(t("Remove download?", "ڈاؤن لوڈ ہٹائیں؟", "حذف التنزيل؟"), t("You can download it again later.", "بعد میں دوبارہ ڈاؤن لوڈ کر سکتے ہیں۔", "يمكنك تنزيله لاحقًا."), t("Remove", "ہٹائیں", "حذف"), {
        scope.launch { if (key.startsWith("audio:")) { val parts = key.split(':'); val reciter = AudioReciters.of(parts[1]); val surah = repo.surah(parts[2].toInt()); (1..surah.ayas).forEach { Platform.deleteFile(AudioReciters.localPath(reciter, surah.id, it)) } } else store.remove(key); revision++; confirmRemove = null }
    }, { confirmRemove = null }, danger = true) }
}

suspend fun exportUserData(users: UserRepo): String = buildJsonObject {
    fun <T> rows(key: String, serializer: kotlinx.serialization.KSerializer<T>, values: List<T>) { put(key, JsonArray(values.map { AppJson.encodeToJsonElement(serializer, it) })) }
    put("app", "Deen Journey"); put("exportedAt", nowMs())
    rows("profiles", ProfileE.serializer(), users.dao.profilesSince(Long.MIN_VALUE)); rows("saved", SavedE.serializer(), users.dao.savedSince(Long.MIN_VALUE))
    rows("notes", NoteE.serializer(), users.dao.notesSince(Long.MIN_VALUE)); rows("highlights", HighlightE.serializer(), users.dao.highlightsSince(Long.MIN_VALUE))
    rows("progress", ProgressE.serializer(), users.dao.progressSince(Long.MIN_VALUE)); rows("counters", CounterE.serializer(), users.dao.countersSince(Long.MIN_VALUE))
    rows("daily", DailyE.serializer(), users.dao.dailySince(Long.MIN_VALUE)); rows("zakat", ZakatE.serializer(), users.dao.zakatSince(Long.MIN_VALUE))
}.toString()

@Composable
fun PrivacyScreen() {
    val settings = koinInject<SettingsRepo>(); val s by settings.flow.collectAsState(); val users = koinInject<UserRepo>(); val auth = koinInject<AuthRepo>(); val sync = koinInject<SyncRepo>(); val scope = rememberCoroutineScope(); val nav = LocalNavigator.current; val lang = LocalLang.current
    var deleting by remember { mutableStateOf(false) }; var confirm by remember { mutableStateOf(false) }; var password by remember { mutableStateOf("") }; var message by remember { mutableStateOf<String?>(null) }; var signOut by remember { mutableStateOf(false) }
    FeaturePage(t("Privacy & data", "پرائیویسی اور ڈیٹا", "الخصوصية والبيانات")) {
        TextLink(t("Read privacy policy", "پرائیویسی پالیسی پڑھیں", "اقرأ سياسة الخصوصية"), { Platform.openUrl(Legal.privacyPolicyUrl) })
        TextLink(t("Request account deletion online", "اکاؤنٹ حذف کرنے کی آن لائن درخواست", "طلب حذف الحساب عبر الإنترنت"), { Platform.openUrl(Legal.deletionRequestUrl) })
        NoteBox(t("Family profiles, notes and progress are stored on this device and synced to your signed-in account when sync is enabled. Location is used locally for prayer times and Qibla.", "فیملی پروفائلز، نوٹس اور پیش رفت فون پر محفوظ ہیں اور سنک آن ہو تو اکاؤنٹ میں محفوظ ہوتے ہیں۔ مقام نماز اور قبلہ کے لیے ہے۔", "تُحفظ الملفات والملاحظات والتقدم على الجهاز وتُزامن مع الحساب عند تفعيل المزامنة. يُستخدم الموقع للصلاة والقبلة."))
        SwitchRow(t("Cloud sync", "کلاؤڈ سنک", "المزامنة السحابية"), s.syncEnabled, { on -> scope.launch { settings.update { it.copy(syncEnabled = on) }; if (on) sync.syncNow() } })
        CardRow(t("Location permissions", "مقام کی اجازت", "أذونات الموقع"), icon = "map-pin", onClick = { Platform.openAppSettings() })
        DjButton(t("Export my data", "میرا ڈیٹا ایکسپورٹ", "تصدير بياناتي"), { scope.launch { val path = "${Platform.cacheDir()}/share/deen-journey-${nowMs()}.json"; Platform.writeFile(path, exportUserData(users).encodeToByteArray()); Platform.shareFile(path, "application/json") } }, Modifier.fillMaxWidth(), lead = "download", style = BtnStyle.Soft)
        if (auth.user.value?.providers?.contains("password") == true) DjField(password, { password = it }, label = t("Password to delete account", "اکاؤنٹ حذف کرنے کا پاس ورڈ", "كلمة المرور لحذف الحساب"), password = true)
        DjButton(t("Delete account", "اکاؤنٹ حذف کریں", "حذف الحساب"), { confirm = true }, Modifier.fillMaxWidth(), style = BtnStyle.DangerSoft, loading = deleting, enabled = auth.user.value != null)
        DjButton(t("Sign out", "سائن آؤٹ", "تسجيل الخروج"), { signOut = true }, Modifier.fillMaxWidth(), style = BtnStyle.Ghost)
        message?.let { NoteBox(it, tone = NoteTone.Red) }
    }
    if (confirm) ConfirmDialog(t("Delete account permanently?", "اکاؤنٹ ہمیشہ حذف کریں؟", "حذف الحساب نهائيًا؟"), t("This removes your cloud data, family profiles, notes and progress. Export your data first if you want a copy.", "کلاؤڈ ڈیٹا، فیملی، نوٹس اور پیش رفت حذف ہو جائیں گے۔ کاپی کے لیے پہلے ایکسپورٹ کریں۔", "سيحذف بياناتك وملفات العائلة والملاحظات والتقدم. صدّر نسخة أولًا إذا أردت."), t("Delete", "حذف", "حذف"), {
        confirm = false; deleting = true
        scope.launch {
            val reauth = auth.user.value?.let { u -> if ("password" in u.providers) auth.signIn(u.email.orEmpty(), password) else Result.success(Unit) } ?: Result.failure(AuthError("no-user"))
            if (reauth.isFailure) message = AuthError.from(reauth.exceptionOrNull()!!).message(lang)
            else {
                settings.update { it.copy(syncEnabled = false) }
                if (!sync.deleteCloudData()) { message = lang.pick("Cloud data could not be removed. Your local data has been kept. Please retry.", "کلاؤڈ ڈیٹا حذف نہیں ہوا۔ فون کا ڈیٹا محفوظ ہے۔ دوبارہ کوشش کریں۔", "تعذر حذف بيانات السحابة. بقيت بيانات الجهاز. حاول مجددًا.") }
                else {
                    val deleted = auth.deleteUser()
                    if (deleted.isFailure) message = AuthError.from(deleted.exceptionOrNull()!!).message(lang)
                    else { users.wipe(); settings.update { com.deenjourney.app.data.settings.AppSettings(lang = it.lang) }; Scheduler.reschedule(); nav.reset(Welcome) }
                }
            }
            password = ""; deleting = false
        }
    }, { confirm = false }, danger = true)
    if (signOut) ConfirmDialog(t("Sign out?", "سائن آؤٹ؟", "تسجيل الخروج؟"), t("Local family data will be cleared to protect your account. Export or sync first.", "اکاؤنٹ کی حفاظت کے لیے مقامی فیملی ڈیٹا صاف ہو گا۔ پہلے ایکسپورٹ یا سنک کریں۔", "ستُمسح بيانات العائلة المحلية لحماية حسابك. صدّرها أو زامنها أولًا."), t("Sign out", "سائن آؤٹ", "خروج"), {
        signOut = false; scope.launch { settings.update { it.copy(syncEnabled = false) }; auth.signOut(); users.wipe(); settings.update { com.deenjourney.app.data.settings.AppSettings(lang = it.lang) }; Scheduler.reschedule(); nav.reset(SignIn) }
    }, { signOut = false })
}

@Composable
fun HelpScreen() {
    val content = koinInject<ContentRepo>(); val nav = LocalNavigator.current; val lang = LocalLang.current; var query by remember { mutableStateOf("") }; var open by remember { mutableStateOf<Int?>(null) }
    FeaturePage(t("Help & FAQ", "مدد اور سوالات", "المساعدة والأسئلة")) {
        SearchBox(query, { query = it }, t("Search help", "مدد تلاش کریں", "ابحث في المساعدة"))
        Loaded(Unit, { content.faq() }) { list -> list.filter { query.isBlank() || it.q.get(lang).contains(query, true) || it.a.get(lang).contains(query, true) }.forEachIndexed { index, faq ->
            DjCard(Modifier.fillMaxWidth(), onClick = { open = if (open == index) null else index }) { SectionHeader(faq.q.text()); if (open == index) { Gap(8.dp); Txt(faq.a.text(), Dj.type.bodyM); faq.link?.let { link -> TextLink(t("Open related settings", "متعلقہ سیٹنگز", "افتح الإعدادات"), { nav.open(link) }) } } }
        }
        }
        CardRow(t("Content correction", "غلطی کی نشاندہی", "تصحيح المحتوى"), icon = "flag", onClick = { nav.go(Correction()) })
    }
}

@Composable
fun CorrectionScreen(initial: String) {
    val auth = koinInject<AuthRepo>(); val scope = rememberCoroutineScope(); var ref by remember { mutableStateOf(initial) }; var text by remember { mutableStateOf("") }; var type by remember { mutableStateOf("Quran") }; var busy by remember { mutableStateOf(false) }; var message by remember { mutableStateOf<String?>(null) }; val lang = LocalLang.current
    FeaturePage(t("Report a correction", "غلطی کی نشاندہی", "الإبلاغ عن تصحيح")) {
        ChipRow { listOf("Quran", "Hadith", "Dua", "Lesson", "Prayer").forEach { category -> DjChip(category, type == category, { type = category }) } }
        DjField(ref, { ref = it }, label = t("Reference", "حوالہ", "المرجع")); DjField(text, { text = it }, label = t("Describe the issue", "مسئلہ بیان کریں", "صف المشكلة"))
        DjButton(t("Submit report", "رپورٹ بھیجیں", "إرسال البلاغ"), { scope.launch {
            busy = true
            val uid = auth.user.value?.uid
            val result = runCatching { require(uid != null) { "Please sign in" }; accountFirestore.collection("reports").document(newId()).set(mapOf("uid" to uid, "type" to type, "reference" to ref.trim(), "description" to text.trim(), "createdAt" to nowMs().toString())) }
            message = if (result.isSuccess) lang.pick("Report submitted", "رپورٹ بھیج دی گئی", "تم إرسال البلاغ") else lang.pick("Report could not be sent. You can share it below.", "رپورٹ نہیں بھیجی جا سکی۔ نیچے شیئر کر سکتے ہیں۔", "تعذر إرسال البلاغ. يمكنك مشاركته أدناه.")
            busy = false
        } }, Modifier.fillMaxWidth(), loading = busy, enabled = ref.isNotBlank() && text.trim().length >= 10)
        DjButton(t("Share report", "رپورٹ شیئر کریں", "مشاركة البلاغ"), { Platform.shareText("Deen Journey correction\n$type · $ref\n$text") }, Modifier.fillMaxWidth(), style = BtnStyle.Soft, enabled = text.isNotBlank())
        message?.let { NoteBox(it) }
    }
}

@Composable
fun AboutScreen() {
    val content = koinInject<ContentRepo>()
    FeaturePage(t("About & sources", "تعارف اور ماخذ", "حول التطبيق والمصادر")) {
        Art("logo_mark", Modifier.size(80.dp)); Txt("Deen Journey", Dj.type.headline); Txt(Platform.appVersion, Dj.type.bodyS)
        Loaded(Unit, { content.sources() }) { rows -> rows.forEach { source -> DjCard(Modifier.fillMaxWidth()) { SectionHeader(source.area.text()); Txt(source.source, Dj.type.bodyM); Txt(source.license, Dj.type.bodyS); if (source.url.isNotBlank()) TextLink(t("Open source", "ماخذ دیکھیں", "فتح المصدر"), { Platform.openUrl(source.url) }) } } }
    }
}

fun savedDestination(item: SavedE): R? = when (item.type) {
    "surah", "ayah" -> routeForQuranKey(item.key)
    "dua" -> DuaDetail(item.key)
    "lesson" -> LessonRoute(item.key)
    "story" -> Story(item.key)
    "name" -> item.key.toIntOrNull()?.let { Names(it) }
    "kalima" -> Kalimas
    "dhikr" -> Adhkar()
    "hadith" -> item.key.split(':').let { p -> p.getOrNull(1)?.toIntOrNull()?.let { HadithDetail(p[0], it) } }
    else -> null
}

@Composable
fun SavedScreen() {
    val users = koinInject<UserRepo>(); val rows by users.saved().collectAsState(emptyList()); val nav = LocalNavigator.current; var filter by remember { mutableStateOf("all") }
    FeaturePage(t("Saved items", "محفوظ آئٹمز", "المحفوظات")) {
        ChipRow { (listOf("all") + rows.map { it.type }.distinct()).forEach { type -> DjChip(type, filter == type, { filter = type }) } }
        val shown = rows.filter { filter == "all" || it.type == filter }; if (shown.isEmpty()) NoItems()
        shown.forEach { item -> CardRow(item.title, sub = item.sub, glyph = "bookmark", trailing = { SaveAction(item.type, item.key, item.title, item.sub) }, onClick = { savedDestination(item)?.let { nav.go(it) } }) }
    }
}
