package com.deenjourney.app.feature.start

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.deenjourney.app.core.Lang
import com.deenjourney.app.core.Legal
import com.deenjourney.app.core.LocalLang
import com.deenjourney.app.core.Perm
import com.deenjourney.app.core.Platform
import com.deenjourney.app.core.hasPermission
import com.deenjourney.app.core.rememberPermission
import com.deenjourney.app.core.t
import com.deenjourney.app.data.auth.AuthError
import com.deenjourney.app.data.auth.AuthRepo
import com.deenjourney.app.data.auth.SocialResult
import com.deenjourney.app.data.auth.Validation
import com.deenjourney.app.data.auth.appleSignInAvailable
import com.deenjourney.app.data.auth.googleSignInAvailable
import com.deenjourney.app.data.auth.rememberAppleSignIn
import com.deenjourney.app.data.auth.rememberGoogleSignIn
import com.deenjourney.app.data.db.BundledDb
import com.deenjourney.app.data.db.DbInstaller
import com.deenjourney.app.data.location.CitiesRepo
import com.deenjourney.app.data.prayer.AsrMadhab
import com.deenjourney.app.data.prayer.Method
import com.deenjourney.app.data.prayer.MethodDefaults
import com.deenjourney.app.data.settings.SavedLocation
import com.deenjourney.app.data.settings.SettingsRepo
import com.deenjourney.app.data.sync.SyncRepo
import kotlinx.coroutines.withTimeoutOrNull
import com.deenjourney.app.data.user.UserRepo
import com.deenjourney.app.design.*
import com.deenjourney.app.nav.*
import com.deenjourney.app.platform.LocationService
import com.deenjourney.app.platform.Scheduler
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import org.koin.compose.koinInject

// ---------------------------------------------------------------- A01 Splash

@Composable
fun SplashScreen() {
    val nav = LocalNavigator.current
    val settings = koinInject<SettingsRepo>()
    val auth = koinInject<AuthRepo>()
    val installer = koinInject<DbInstaller>()
    val users = koinInject<UserRepo>(); val sync = koinInject<SyncRepo>()
    LaunchedEffect(Unit) {
        val start = kotlin.time.TimeSource.Monotonic.markNow()
        runCatching { installer.ensure(BundledDb.QURAN); installer.ensure(BundledDb.CITIES) }
        withTimeoutOrNull(5000) { auth.ready.first { it } }
        val left = 900 - start.elapsedNow().inWholeMilliseconds
        if (left > 0) delay(left)
        val user = auth.user.value
        if (user != null) sync.syncNow(restore = true)
        val s = settings.get()
        when {
            user == null -> nav.reset(Welcome)
            users.dao.profilesNow().isEmpty() -> nav.reset(SignIn)
            !s.setupDone -> nav.reset(PrayerSetup())
            else -> nav.reset(Home)
        }
    }
    val pulse = rememberInfiniteTransition().animateFloat(0.3f, 1f, infiniteRepeatable(tween(700), RepeatMode.Reverse))
    Box(Modifier.fillMaxSize().background(Color(0xFF06261C))) {
        Art("hero_prayer_night", Modifier.fillMaxWidth().height(420.dp).align(Alignment.BottomCenter), alignment = Alignment.BottomCenter)
        Box(Modifier.fillMaxWidth().height(170.dp).align(Alignment.BottomCenter).offset(y = (-250).dp).background(Brush.verticalGradient(listOf(Color(0xFF06261C), Color(0x0006261C)))))
        Column(Modifier.fillMaxSize().padding(horizontal = 30.dp).padding(bottom = 140.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
            Art("logo_mark", Modifier.size(128.dp), contentScale = ContentScale.Fit)
            Gap(18.dp)
            Txt("Deen Journey", Dj.type.displayL.copy(fontFamily = Dj.fonts.playfair), Color.White, align = TextAlign.Center)
            Txt(t("Faith · Knowledge · Better habits", "ایمان · علم · اچھی عادات", "إيمان · علم · عادات طيبة"), Dj.type.bodyM, Color(0xFFE8D5A6), align = TextAlign.Center)
            Gap(18.dp)
            ArabicText("بِسْمِ ٱللَّهِ ٱلرَّحْمَٰنِ ٱلرَّحِيمِ", Dj.type.quranM, Color(0xFFF3E3BC), center = true)
            Gap(14.dp)
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) { repeat(3) { i -> Box(Modifier.size(7.dp).alpha(if (i == 0) pulse.value else 1.3f - pulse.value).clip(CircleShape).background(Color(0xFFE8C77A))) } }
        }
    }
}

// ---------------------------------------------------------------- A02 Welcome & language

@Composable
fun WelcomeScreen() {
    val nav = LocalNavigator.current
    val settings = koinInject<SettingsRepo>()
    val scope = rememberCoroutineScope()
    val lang = LocalLang.current
    LaunchedEffect(Unit) {
        // first launch: pre-select the device language when we support it
        val s = settings.get()
        if (s.lang == null) settings.update { it.copy(lang = Lang.entries.firstOrNull { l -> l.code == Platform.deviceLanguage() }?.code ?: "en") }
    }
    Screen(statusBarPadding = false) {
        Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
            Box(Modifier.fillMaxWidth().height(360.dp).background(Color(0xFFFBF4E4))) {
                Art("hero_welcome", Modifier.fillMaxWidth().height(240.dp).align(Alignment.BottomCenter), alignment = Alignment.BottomCenter)
                Box(Modifier.fillMaxWidth().height(90.dp).offset(y = 120.dp).background(Brush.verticalGradient(listOf(Color(0xFFFBF4E4), Color(0x00FBF4E4)))))
                Column(Modifier.fillMaxWidth().statusBarsPadding().padding(top = 16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Art("logo_mark", Modifier.size(64.dp), contentScale = ContentScale.Fit)
                    Txt("Deen Journey", Dj.type.displayM.copy(fontFamily = Dj.fonts.playfair), Color(0xFF17533C))
                    Txt(t("A lifelong journey of faith and good character", "ایمان اور اچھے اخلاق کا زندگی بھر کا سفر", "رحلة العمر في الإيمان وحسن الخلق"), Dj.type.bodyS, Color(0xFF5A625B), align = TextAlign.Center, modifier = Modifier.padding(horizontal = 60.dp))
                }
            }
            Column(
                Modifier.fillMaxWidth().offset(y = (-24).dp).clip(RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)).background(Dj.c.surface).padding(horizontal = 18.dp, vertical = 20.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Txt(t("Choose your language", "اپنی زبان منتخب کریں", "اختر لغتك"), Dj.type.titleM)
                Lang.entries.forEach { l ->
                    val on = l == lang
                    BorderBox(Modifier.fillMaxWidth(), selected = on, onClick = { scope.launch { settings.update { it.copy(lang = l.code) } } }) {
                        Row(Modifier.padding(horizontal = 14.dp, vertical = 12.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            Box(Modifier.size(44.dp).clip(CircleShape).background(if (on) Dj.c.primary else Dj.c.goldTint), contentAlignment = Alignment.Center) {
                                Txt(when (l) { Lang.EN -> "En"; Lang.UR -> "اُردو"; Lang.AR -> "عربي" }, if (l == Lang.EN) Dj.type.titleS else Dj.type.arabicS, if (on) Color.White else Dj.c.goldText)
                            }
                            Column(Modifier.weight(1f)) {
                                Txt(l.nativeName, Dj.type.titleS)
                                Txt(when (l) { Lang.EN -> "Continue in English"; Lang.UR -> "اردو میں جاری رکھیں"; Lang.AR -> "المتابعة بالعربية" }, Dj.type.bodyS, Dj.c.text2)
                            }
                            DjRadio(on)
                        }
                    }
                }
                Gap(2.dp)
                DjButton(t("Create account", "اکاؤنٹ بنائیں", "إنشاء حساب"), { nav.go(SignUp) }, Modifier.fillMaxWidth())
                DjButton(t("I already have an account", "میرا اکاؤنٹ پہلے سے ہے", "لدي حساب بالفعل"), { nav.go(SignIn) }, Modifier.fillMaxWidth(), style = BtnStyle.Secondary, lead = "log-out")
                Txt(t("You can change the language anytime in Settings", "زبان کسی بھی وقت سیٹنگز میں بدلی جا سکتی ہے", "يمكنك تغيير اللغة في أي وقت من الإعدادات"), Dj.type.caption, Dj.c.text3, align = TextAlign.Center, modifier = Modifier.fillMaxWidth())
                Spacer(Modifier.navigationBarsPadding())
            }
        }
    }
}

/** Restore the account before deciding whether onboarding is needed. */
private suspend fun afterSignIn(nav: Navigator, settings: SettingsRepo, sync: SyncRepo) {
    if (!sync.syncNow(restore = true)) throw AuthError("restore")
    Scheduler.reschedule()
    if (settings.get().setupDone) nav.reset(Home) else nav.reset(PrayerSetup())
}

@Composable
private fun RetryAccountRestore() {
    val nav = LocalNavigator.current
    val settings = koinInject<SettingsRepo>()
    val sync = koinInject<SyncRepo>()
    val scope = rememberCoroutineScope()
    var busy by remember { mutableStateOf(false) }
    DjButton(t("Retry restoring my account", "میرا اکاؤنٹ دوبارہ بحال کریں", "إعادة محاولة استعادة حسابي"), {
        scope.launch {
            busy = true
            runCatching { afterSignIn(nav, settings, sync) }
            busy = false
        }
    }, Modifier.fillMaxWidth(), loading = busy, style = BtnStyle.Secondary)
}
@Composable
private fun SocialButtons(onResult: (SocialResult) -> Unit) {
    val google = rememberGoogleSignIn(onResult)
    val apple = rememberAppleSignIn(onResult)
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        Hr(Modifier.weight(1f)); Txt(t("or continue with", "یا جاری رکھیں", "أو تابع باستخدام"), Dj.type.caption, Dj.c.text3); Hr(Modifier.weight(1f))
    }
    if (googleSignInAvailable || !appleSignInAvailable) DjButton(t("Continue with Google", "گوگل کے ساتھ جاری رکھیں", "المتابعة باستخدام Google"), google, Modifier.fillMaxWidth(), style = BtnStyle.Light, lead = "brand-google")
    if (appleSignInAvailable) DjButton(t("Continue with Apple", "ایپل کے ساتھ جاری رکھیں", "المتابعة باستخدام Apple"), apple, Modifier.fillMaxWidth(), style = BtnStyle.Light, lead = "brand-apple")
}

// ---------------------------------------------------------------- A03 Sign in

@Composable
fun SignInScreen() {
    val nav = LocalNavigator.current
    val auth = koinInject<AuthRepo>(); val settings = koinInject<SettingsRepo>(); val users = koinInject<UserRepo>(); val sync = koinInject<SyncRepo>()
    val lang = LocalLang.current
    val scope = rememberCoroutineScope()
    var email by remember { mutableStateOf("") }
    var pass by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }
    var busy by remember { mutableStateOf(false) }
    fun submit() {
        error = when { !Validation.email(email) -> AuthError("email").message(lang); pass.isEmpty() -> AuthError("invalid").message(lang); else -> null }
        if (error != null) return
        busy = true
        scope.launch {
            auth.signIn(email, pass).mapCatching { afterSignIn(nav, settings, sync) }.onFailure { error = AuthError.from(it).message(lang) }
            busy = false
        }
    }
    val social: (SocialResult) -> Unit = { r ->
        scope.launch {
            busy = true
            val res = when (r) { is SocialResult.Google -> auth.signInWithGoogle(r.idToken); is SocialResult.Apple -> auth.signInWithApple(r.idToken, r.rawNonce); is SocialResult.Failed -> Result.failure(AuthError(r.code)) }
            res.mapCatching { afterSignIn(nav, settings, sync) }.onFailure { error = AuthError.from(it).message(lang) }
            busy = false
        }
    }
    Screen(top = { AppBar("", onBack = { nav.back() }) }) {
        Body(gap = 14.dp) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) { Art("logo_mark", Modifier.size(32.dp), contentScale = ContentScale.Fit); Txt("Deen Journey", Dj.type.titleL.copy(fontFamily = Dj.fonts.playfair), Dj.c.primary) }
            Txt(t("Welcome back", "خوش آمدید", "مرحبًا بعودتك"), Dj.type.displayM)
            Txt(t("Sign in to sync your family, bookmarks and progress across devices.", "سائن اِن کریں تاکہ فیملی، بک مارکس اور پیش رفت تمام ڈیوائسز پر محفوظ رہیں۔", "سجّل الدخول لمزامنة عائلتك وإشاراتك المرجعية وتقدمك عبر الأجهزة."), Dj.type.bodyM, Dj.c.text2)
            DjField(email, { email = it; error = null }, label = t("Email", "ای میل", "البريد الإلكتروني"), placeholder = "name@email.com", icon = "mail", keyboard = KeyboardType.Email)
            DjField(pass, { pass = it; error = null }, label = t("Password", "پاس ورڈ", "كلمة المرور"), placeholder = "••••••••", icon = "lock", password = true, ime = ImeAction.Done, onIme = ::submit, error = error)
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) { TextLink(t("Forgot password?", "پاس ورڈ بھول گئے؟", "نسيت كلمة المرور؟"), { nav.go(Forgot) }) }
            DjButton(t("Sign in", "سائن اِن", "تسجيل الدخول"), ::submit, Modifier.fillMaxWidth(), loading = busy)
            if (error == AuthError("restore").message(lang)) RetryAccountRestore()
            SocialButtons(social)
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
                Txt(t("New to Deen Journey?", "نئے ہیں؟", "جديد هنا؟"), Dj.type.bodyM, Dj.c.text2)
                TextLink(t("Create account", "اکاؤنٹ بنائیں", "إنشاء حساب"), { nav.go(SignUp) })
            }
        }
    }
}

// ---------------------------------------------------------------- A04 Create account

@Composable
fun SignUpScreen() {
    val nav = LocalNavigator.current
    val auth = koinInject<AuthRepo>(); val settings = koinInject<SettingsRepo>(); val users = koinInject<UserRepo>(); val sync = koinInject<SyncRepo>()
    val lang = LocalLang.current
    val scope = rememberCoroutineScope()
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var pass by remember { mutableStateOf("") }
    var agree by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    var fieldErr by remember { mutableStateOf<Pair<String, String>?>(null) }
    var busy by remember { mutableStateOf(false) }
    fun submit() {
        fieldErr = when {
            name.isBlank() -> "name" to t0(lang, "Please enter your name.", "اپنا نام درج کریں۔", "أدخل اسمك.")
            !Validation.email(email) -> "email" to AuthError("email").message(lang)
            !Validation.passwordOk(pass) -> "pass" to AuthError("weak").message(lang)
            !agree -> "agree" to t0(lang, "Please read the Privacy Policy and confirm.", "پرائیویسی پالیسی پڑھ کر تصدیق کریں۔", "يرجى قراءة سياسة الخصوصية والتأكيد.")
            else -> null
        }
        if (fieldErr != null) return
        busy = true
        scope.launch {
            auth.signUp(name, email, pass).mapCatching { afterSignIn(nav, settings, sync) }.onFailure { error = AuthError.from(it).message(lang) }
            busy = false
        }
    }
    val social: (SocialResult) -> Unit = { r ->
        scope.launch {
            busy = true
            val res = when (r) { is SocialResult.Google -> auth.signInWithGoogle(r.idToken); is SocialResult.Apple -> auth.signInWithApple(r.idToken, r.rawNonce); is SocialResult.Failed -> Result.failure(AuthError(r.code)) }
            res.mapCatching { afterSignIn(nav, settings, sync) }.onFailure { error = AuthError.from(it).message(lang) }
            busy = false
        }
    }
    val strength = Validation.strength(pass)
    Screen(top = { AppBar("", onBack = { nav.back() }) }) {
        Body(gap = 13.dp) {
            Txt(t("Create your account", "اپنا اکاؤنٹ بنائیں", "أنشئ حسابك"), Dj.type.displayM)
            Txt(t("One account for your whole family — every profile, bookmark and lesson stays in sync.", "پوری فیملی کے لیے ایک اکاؤنٹ — ہر پروفائل، بک مارک اور سبق محفوظ اور ہم آہنگ۔", "حساب واحد لكل العائلة — كل ملف وإشارة ودرس يبقى متزامنًا."), Dj.type.bodyM, Dj.c.text2)
            DjField(name, { name = it; fieldErr = null }, label = t("Full name", "پورا نام", "الاسم الكامل"), icon = "user", error = fieldErr?.takeIf { it.first == "name" }?.second)
            DjField(email, { email = it; fieldErr = null; error = null }, label = t("Email", "ای میل", "البريد الإلكتروني"), placeholder = "name@email.com", icon = "mail", keyboard = KeyboardType.Email, error = fieldErr?.takeIf { it.first == "email" }?.second)
            DjField(pass, { pass = it; fieldErr = null }, label = t("Password", "پاس ورڈ", "كلمة المرور"), icon = "lock", password = true, ime = ImeAction.Done, error = fieldErr?.takeIf { it.first == "pass" }?.second,
                help = t("At least 8 characters with letters and numbers", "کم از کم 8 حروف، حروف اور ہندسوں کے ساتھ", "8 أحرف على الأقل مع حروف وأرقام"))
            if (pass.isNotEmpty()) Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                repeat(4) { i -> Box(Modifier.weight(1f).height(4.dp).clip(CircleShape).background(if (i < strength) (if (strength >= 3) Dj.c.primary else Dj.c.warning) else Dj.c.border)) }
                Txt(when (strength) { 0, 1 -> t("Weak", "کمزور", "ضعيفة"); 2 -> t("Fair", "درمیانہ", "متوسطة"); else -> t("Strong", "مضبوط", "قوية") }, Dj.type.labelS, if (strength >= 3) Dj.c.primary else Dj.c.warning)
            }
            Row(verticalAlignment = Alignment.Top, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                DjCheck(agree, { agree = it; fieldErr = null })
                Column(Modifier.weight(1f)) {
                    Txt(t("I have read the Privacy Policy", "میں نے پرائیویسی پالیسی پڑھ لی ہے", "لقد قرأت سياسة الخصوصية"), Dj.type.bodyS, Dj.c.text2)
                    TextLink(t("Read privacy policy", "پرائیویسی پالیسی پڑھیں", "اقرأ سياسة الخصوصية"), { Platform.openUrl(Legal.privacyPolicyUrl) }, style = Dj.type.labelS)
                    if (fieldErr?.first == "agree") Txt(fieldErr!!.second, Dj.type.caption, Dj.c.danger)
                }
            }
            if (error != null) NoteBox(error!!, icon = "circle-alert", tone = NoteTone.Red)
            if (error == AuthError("restore").message(lang)) RetryAccountRestore()
            DjButton(t("Create account", "اکاؤنٹ بنائیں", "إنشاء حساب"), ::submit, Modifier.fillMaxWidth(), loading = busy)
            SocialButtons(social)
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
                Txt(t("Already have an account?", "پہلے سے اکاؤنٹ ہے؟", "لديك حساب؟"), Dj.type.bodyM, Dj.c.text2)
                TextLink(t("Sign in", "سائن اِن", "تسجيل الدخول"), { nav.go(SignIn) })
            }
        }
    }
}

private fun t0(lang: Lang, en: String, ur: String, ar: String) = lang.pick(en, ur, ar)

// ---------------------------------------------------------------- A05 Forgot / A06 Check email

@Composable
fun ForgotScreen() {
    val nav = LocalNavigator.current
    val auth = koinInject<AuthRepo>()
    val lang = LocalLang.current
    val scope = rememberCoroutineScope()
    var email by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }
    var busy by remember { mutableStateOf(false) }
    fun send() {
        if (!Validation.email(email)) { error = AuthError("email").message(lang); return }
        busy = true
        scope.launch {
            // same confirmation whether or not the account exists (no account enumeration)
            val r = auth.sendReset(email)
            busy = false
            if (r.isFailure && AuthError.from(r.exceptionOrNull()!!).code == "network") error = AuthError("network").message(lang) else nav.go(CheckEmail(email.trim()))
        }
    }
    Screen(top = { AppBar("", onBack = { nav.back() }) }) {
        Body(gap = 16.dp, padding = PaddingValues(18.dp, 12.dp, 18.dp, 24.dp)) {
            GlyphTile("shield", 84.dp)
            Txt(t("Reset your password", "پاس ورڈ دوبارہ بنائیں", "إعادة تعيين كلمة المرور"), Dj.type.displayM)
            Txt(t("Enter the email you signed up with. We will send you a secure link to set a new password.", "وہ ای میل درج کریں جس سے اکاؤنٹ بنایا تھا۔ ہم نیا پاس ورڈ بنانے کا محفوظ لنک بھیجیں گے۔", "أدخل البريد الذي سجلت به. سنرسل لك رابطًا آمنًا لتعيين كلمة مرور جديدة."), Dj.type.bodyM, Dj.c.text2)
            DjField(email, { email = it; error = null }, label = t("Email", "ای میل", "البريد الإلكتروني"), icon = "mail", keyboard = KeyboardType.Email, ime = ImeAction.Done, onIme = ::send, error = error)
            DjButton(t("Send reset link", "ری سیٹ لنک بھیجیں", "إرسال رابط إعادة التعيين"), ::send, Modifier.fillMaxWidth(), loading = busy)
            NoteBox(t("The link expires in 1 hour. Check your spam folder if it does not arrive.", "لنک 1 گھنٹے میں ختم ہو جاتا ہے۔ نہ ملے تو اسپیم فولڈر دیکھیں۔", "تنتهي صلاحية الرابط خلال ساعة. تحقق من مجلد الرسائل غير المرغوبة."), tone = NoteTone.Gold)
        }
    }
}

@Composable
fun CheckEmailScreen(email: String) {
    val nav = LocalNavigator.current
    val auth = koinInject<AuthRepo>()
    val scope = rememberCoroutineScope()
    var wait by remember { mutableStateOf(45) }
    LaunchedEffect(wait) { if (wait > 0) { delay(1000); wait-- } }
    Screen(top = { AppBar("", onBack = { nav.back() }) }) {
        Body(gap = 14.dp) {
            Column(Modifier.fillMaxWidth().padding(top = 30.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(14.dp)) {
                Box(Modifier.size(120.dp).clip(CircleShape).background(Dj.c.primaryTint), contentAlignment = Alignment.Center) { DjIcon("mail", 54.dp, Dj.c.primary) }
                Txt(t("Check your email", "اپنی ای میل دیکھیں", "تحقق من بريدك"), Dj.type.displayM, align = TextAlign.Center)
                Txt(t("We sent a link to $email. Open it on this phone to continue.", "ہم نے $email پر لنک بھیجا ہے۔ جاری رکھنے کے لیے اسے اسی فون پر کھولیں۔", "أرسلنا رابطًا إلى $email. افتحه على هذا الهاتف للمتابعة."), Dj.type.bodyM, Dj.c.text2, align = TextAlign.Center)
                Gap(6.dp)
                DjButton(t("Open email app", "ای میل ایپ کھولیں", "فتح تطبيق البريد"), { Platform.openUrl("mailto:") }, Modifier.fillMaxWidth(), lead = "external-link")
                DjButton(if (wait > 0) t("Resend in 0:${wait.toString().padStart(2, '0')}", "دوبارہ بھیجیں 0:${wait.toString().padStart(2, '0')}", "إعادة الإرسال خلال 0:${wait.toString().padStart(2, '0')}") else t("Resend link", "لنک دوبارہ بھیجیں", "إعادة إرسال الرابط"),
                    { scope.launch { auth.sendReset(email); wait = 60 } }, Modifier.fillMaxWidth(), style = BtnStyle.Ghost, enabled = wait == 0)
                TextLink(t("Back to sign in", "سائن اِن پر واپس", "العودة لتسجيل الدخول"), { nav.reset(SignIn) })
            }
        }
    }
}

// ---------------------------------------------------------------- A07 Location & prayer setup

/** Shared by onboarding and settings: GPS → nearest city (offline DB) → time zone + default method. */
suspend fun detectLocation(loc: LocationService, cities: CitiesRepo, settings: SettingsRepo, lang: Lang): SavedLocation? {
    val fix = loc.current() ?: return null
    val city = cities.nearest(fix.lat, fix.lng, lang)
    val tz = city?.tz ?: Platform.deviceTimeZoneId()
    val saved = SavedLocation(city?.name ?: lang.pick("My location", "میرا مقام", "موقعي"), city?.country, city?.cc ?: Platform.countryCode(), fix.lat, fix.lng, tz, gps = true)
    applyLocation(settings, saved)
    return saved
}

suspend fun applyLocation(settings: SettingsRepo, saved: SavedLocation) {
    settings.update { s ->
        val defaults = MethodDefaults.forCountry(saved.cc)
        s.copy(
            location = saved,
            prayer = if (s.methodChosen) s.prayer else s.prayer.copy(method = defaults.method, asr = defaults.asr),
            hijriOffset = if (s.methodChosen) s.hijriOffset else MethodDefaults.hijriOffsetFor(saved.cc),
            fiqh = if (s.methodChosen) s.fiqh else if (defaults.asr == AsrMadhab.Hanafi.name) "hanafi" else s.fiqh,
        )
    }
    Scheduler.reschedule()
}

@Composable
fun PrayerSetupScreen(fromSettings: Boolean) {
    val nav = LocalNavigator.current
    val settings = koinInject<SettingsRepo>(); val loc = koinInject<LocationService>(); val cities = koinInject<CitiesRepo>()
    val s by settings.flow.collectAsState()
    val lang = LocalLang.current
    val scope = rememberCoroutineScope()
    var busy by remember { mutableStateOf(false) }
    var problem by remember { mutableStateOf<String?>(null) }
    val ask = rememberPermission(Perm.Location) { granted ->
        if (!granted) problem = "denied"
        else scope.launch {
            busy = true
            val r = detectLocation(loc, cities, settings, lang)
            busy = false
            problem = if (r == null) (if (!loc.servicesEnabled()) "off" else "nofix") else null
        }
    }
    val location = s.location
    val method = runCatching { Method.valueOf(s.prayer.method) }.getOrDefault(Method.MWL)
    Screen(top = { AppBar(t("Prayer setup", "نماز کی ترتیب", "إعداد الصلاة"), onBack = { nav.back() }, actions = { if (!fromSettings) Txt(t("Step 1 of 2", "مرحلہ 1 از 2", "الخطوة 1 من 2"), Dj.type.labelS, Dj.c.text3, Modifier.padding(end = 8.dp)) }) },
        bottom = {
            Footer {
                if (location == null) {
                    DjButton(t("Use my current location", "میرا موجودہ مقام استعمال کریں", "استخدم موقعي الحالي"), { problem = null; ask() }, Modifier.fillMaxWidth(), lead = "locate-fixed", loading = busy)
                    DjButton(t("Choose city manually", "شہر خود منتخب کریں", "اختر المدينة يدويًا"), { nav.go(ChooseLocation(onboarding = !fromSettings)) }, Modifier.fillMaxWidth(), style = BtnStyle.Secondary)
                } else {
                    DjButton(t("Continue", "جاری رکھیں", "متابعة"), { if (fromSettings) nav.back() else nav.go(NotifSetup) }, Modifier.fillMaxWidth())
                    DjButton(t("Use my current location", "میرا موجودہ مقام استعمال کریں", "استخدم موقعي الحالي"), { problem = null; ask() }, Modifier.fillMaxWidth(), style = BtnStyle.Ghost, lead = "locate-fixed", loading = busy)
                }
            }
        }) {
        Body(gap = 12.dp) {
            Art("skyline_day", Modifier.fillMaxWidth().height(120.dp).clip(RoundedCornerShape(18.dp)).background(Dj.c.goldTint), alignment = Alignment.BottomCenter)
            Txt(t("Accurate prayer times, anywhere", "ہر جگہ درست نماز کے اوقات", "مواقيت صلاة دقيقة في أي مكان"), Dj.type.headline)
            Txt(t("Your location sets prayer times, Qibla direction and Sehri/Iftar. It is used on your phone only.", "آپ کا مقام نماز کے اوقات، قبلہ اور سحری/افطار کے لیے استعمال ہوتا ہے — صرف آپ کے فون پر۔", "يحدد موقعك مواقيت الصلاة واتجاه القبلة والسحور والإفطار، ويُستخدم على هاتفك فقط."), Dj.type.bodyM, Dj.c.text2)
            when (problem) {
                "denied" -> NoteBox(t("Location permission was not given. You can choose your city manually instead.", "مقام کی اجازت نہیں دی گئی۔ آپ شہر خود منتخب کر سکتے ہیں۔", "لم يُمنح إذن الموقع. يمكنك اختيار مدينتك يدويًا."), icon = "map-pin", tone = NoteTone.Gold, trailing = { TextLink(t("Settings", "سیٹنگز", "الإعدادات"), { Platform.openAppSettings() }) })
                "off" -> NoteBox(t("Location services are off. Turn them on or choose your city manually.", "لوکیشن سروسز بند ہیں۔ انہیں آن کریں یا شہر خود منتخب کریں۔", "خدمات الموقع متوقفة. شغّلها أو اختر مدينتك يدويًا."), icon = "map-pin", tone = NoteTone.Gold)
                "nofix" -> NoteBox(t("Couldn’t get a location fix. Try again near a window, or choose your city.", "مقام معلوم نہ ہو سکا۔ کھڑکی کے قریب دوبارہ کوشش کریں یا شہر منتخب کریں۔", "تعذر تحديد الموقع. حاول قرب نافذة أو اختر مدينتك."), icon = "map-pin", tone = NoteTone.Gold)
            }
            if (location != null) DjCard(padding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)) {
                ListRow(location.name + (location.country?.let { ", $it" } ?: ""), sub = (if (location.gps) t("Detected", "خودکار", "تم الاكتشاف") else t("Chosen", "منتخب", "مختار")) + " · ${fmtCoord(location.lat, true)}, ${fmtCoord(location.lng, false)}",
                    lead = { IconTile("map-pin") }, trailing = { Pill(t("Change", "تبدیل", "تغيير"), fill = Dj.c.surface2, color = Dj.c.text, onClick = { nav.go(ChooseLocation(onboarding = !fromSettings)) }) })
                Hr()
                ListRow(method.label.get(lang), sub = t("Calculation method · tap to change", "حساب کا طریقہ · تبدیل کرنے کے لیے ٹیپ کریں", "طريقة الحساب · اضغط للتغيير"), lead = { IconTile("settings-2") }, chevron = true, onClick = { nav.go(PrayerSettings) })
                Hr()
                ListRow(if (s.prayer.asr == AsrMadhab.Hanafi.name) t("Asr: Hanafi (later)", "عصر: حنفی (بعد میں)", "العصر: حنفي (متأخر)") else t("Asr: Standard (earlier)", "عصر: عام (پہلے)", "العصر: الجمهور (مبكر)"),
                    sub = t("Shafi‘i, Maliki and Hanbali use the earlier Asr", "شافعی، مالکی اور حنبلی میں عصر پہلے ہوتی ہے", "الشافعية والمالكية والحنابلة: العصر المبكر"), lead = { IconTile("sun") }, chevron = true, onClick = { nav.go(PrayerSettings) })
            }
        }
    }
}

fun fmtCoord(v: Double, lat: Boolean): String {
    val a = kotlin.math.abs(v); val r = (kotlin.math.round(a * 100) / 100).toString()
    return "$r° ${if (lat) (if (v >= 0) "N" else "S") else (if (v >= 0) "E" else "W")}"
}

// ---------------------------------------------------------------- A08 Notifications

@Composable
fun NotifSetupScreen() {
    val nav = LocalNavigator.current
    val settings = koinInject<SettingsRepo>()
    val s by settings.flow.collectAsState()
    val scope = rememberCoroutineScope()
    var exactOk by remember { mutableStateOf(Scheduler.canScheduleExact()) }
    val ask = rememberPermission(Perm.Notifications) { _ ->
        scope.launch { settings.update { it.copy(notificationsAsked = true) }; Scheduler.reschedule(); nav.go(FamilySetup(onboarding = true)) }
    }
    fun setAdhan(on: Boolean) = scope.launch { settings.update { st -> st.copy(adhan = st.adhan.mapValues { (k, v) -> if (k == "Sunrise") v else v.copy(on = on) }) } }
    val adhanOn = s.adhan.filterKeys { it != "Sunrise" }.values.any { it.on }
    Screen(top = { AppBar(t("Prayer setup", "نماز کی ترتیب", "إعداد الصلاة"), onBack = { nav.back() }, actions = { Txt(t("Step 2 of 2", "مرحلہ 2 از 2", "الخطوة 2 من 2"), Dj.type.labelS, Dj.c.text3, Modifier.padding(end = 8.dp)) }) },
        bottom = {
            Footer {
                DjButton(t("Allow notifications", "اطلاعات کی اجازت دیں", "السماح بالإشعارات"), ask, Modifier.fillMaxWidth())
                DjButton(t("Not now", "ابھی نہیں", "ليس الآن"), { nav.go(FamilySetup(onboarding = true)) }, Modifier.fillMaxWidth(), style = BtnStyle.Ghost)
            }
        }) {
        Body(gap = 14.dp) {
            Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(12.dp)) {
                GlyphTile("bell", 104.dp)
                Txt(t("Never miss a prayer", "کوئی نماز نہ چھوٹے", "لا تفوّت صلاة"), Dj.type.headline, align = TextAlign.Center)
                Txt(t("Allow notifications for gentle reminders. You choose which ones in Settings.", "نرم یاد دہانیوں کے لیے اطلاعات کی اجازت دیں۔ کون سی، یہ آپ سیٹنگز میں چنیں۔", "اسمح بالإشعارات للتذكير اللطيف، وتختار أيها من الإعدادات."), Dj.type.bodyM, Dj.c.text2, align = TextAlign.Center)
            }
            DjCard(padding = PaddingValues(horizontal = 14.dp, vertical = 4.dp)) {
                ListRow(t("Adhan at prayer times", "نماز کے وقت اذان", "الأذان عند وقت الصلاة"), sub = t("A soft chime or a simple tone", "ہلکی گھنٹی یا سادہ آواز", "رنين لطيف أو نغمة بسيطة"), lead = { GlyphTile("mosque", 42.dp) }, trailing = { DjSwitch(adhanOn, { setAdhan(it) }) })
                ListRow(t("Jumu‘ah reminder", "جمعہ کی یاد دہانی", "تذكير الجمعة"), sub = t("Friday, an hour before Dhuhr", "جمعہ کو ظہر سے ایک گھنٹہ پہلے", "الجمعة قبل الظهر بساعة"), lead = { GlyphTile("sehri", 42.dp) }, trailing = { DjSwitch(s.jumuahReminder, { v -> scope.launch { settings.update { it.copy(jumuahReminder = v) } } }) })
                ListRow(t("Daily lesson", "روزانہ سبق", "الدرس اليومي"), sub = t("A short reminder at 8 PM", "رات 8 بجے مختصر یاد دہانی", "تذكير قصير الساعة 8 مساءً"), lead = { GlyphTile("lesson", 42.dp) }, trailing = { DjSwitch(s.lessonReminder, { v -> scope.launch { settings.update { it.copy(lessonReminder = v) } } }) })
            }
            if (!exactOk && !Platform.isIos) NoteBox(
                t("For adhan exactly on time, allow “Alarms & reminders” for Deen Journey.", "اذان بالکل وقت پر چلانے کے لیے “الارمز اور یاد دہانیاں” کی اجازت دیں۔", "لأذان في وقته تمامًا، اسمح بـ «المنبهات والتذكيرات» للتطبيق."),
                icon = "alarm-clock", tone = NoteTone.Gold, trailing = { TextLink(t("Allow", "اجازت", "سماح"), { Scheduler.openExactAlarmSettings(); exactOk = Scheduler.canScheduleExact() }) },
            )
        }
    }
}

@Composable
fun hasNotificationPermission(): Boolean = remember { hasPermission(Perm.Notifications) }
