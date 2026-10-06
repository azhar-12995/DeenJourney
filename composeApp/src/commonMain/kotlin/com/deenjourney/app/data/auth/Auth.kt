package com.deenjourney.app.data.auth

import com.deenjourney.app.core.Lang
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.auth.EmailAuthProvider
import dev.gitlive.firebase.auth.FirebaseUser
import dev.gitlive.firebase.auth.GoogleAuthProvider
import dev.gitlive.firebase.auth.OAuthProvider
import dev.gitlive.firebase.auth.auth
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class AuthUser(val uid: String, val email: String?, val name: String?, val verified: Boolean, val providers: List<String>)

/** User-facing auth errors (localised in [message]). */
class AuthError(val code: String) : Exception(code) {
    fun message(lang: Lang): String = when (code) {
        "invalid" -> lang.pick("Email or password is incorrect.", "ای میل یا پاس ورڈ درست نہیں۔", "البريد الإلكتروني أو كلمة المرور غير صحيحة.")
        "no-user" -> lang.pick("No account found with this email.", "اس ای میل سے کوئی اکاؤنٹ نہیں ملا۔", "لا يوجد حساب بهذا البريد.")
        "in-use" -> lang.pick("An account already exists with this email. Sign in instead.", "اس ای میل سے اکاؤنٹ پہلے سے موجود ہے۔ سائن اِن کریں۔", "يوجد حساب بهذا البريد بالفعل. سجّل الدخول.")
        "weak" -> lang.pick("Use at least 8 characters with letters and numbers.", "کم از کم 8 حروف، جن میں حروف اور ہندسے ہوں۔", "استخدم 8 أحرف على الأقل تتضمن حروفًا وأرقامًا.")
        "network" -> lang.pick("No internet connection. Please try again.", "انٹرنیٹ دستیاب نہیں۔ دوبارہ کوشش کریں۔", "لا يوجد اتصال بالإنترنت. حاول مجددًا.")
        "too-many" -> lang.pick("Too many attempts. Please wait a minute and try again.", "بہت زیادہ کوششیں۔ ایک منٹ بعد دوبارہ کوشش کریں۔", "محاولات كثيرة. انتظر دقيقة ثم حاول مجددًا.")
        "recent-login" -> lang.pick("For your security, please sign in again and retry.", "حفاظت کے لیے دوبارہ سائن اِن کر کے کوشش کریں۔", "لأمانك، سجّل الدخول مرة أخرى ثم أعد المحاولة.")
        "email" -> lang.pick("Please enter a valid email address.", "درست ای میل درج کریں۔", "أدخل بريدًا إلكترونيًا صحيحًا.")
        "cancelled" -> lang.pick("Sign-in was cancelled.", "سائن اِن منسوخ کر دیا گیا۔", "تم إلغاء تسجيل الدخول.")
        "unavailable" -> lang.pick("This sign-in option isn’t set up yet.", "یہ سائن اِن طریقہ ابھی دستیاب نہیں۔", "طريقة الدخول هذه غير مهيأة بعد.")
        else -> lang.pick("Something went wrong. Please try again.", "کچھ غلط ہو گیا۔ دوبارہ کوشش کریں۔", "حدث خطأ ما. حاول مجددًا.")
    }

    companion object {
        fun from(e: Throwable): AuthError {
            if (e is AuthError) return e
            val n = e::class.simpleName.orEmpty()
            val m = (e.message ?: "").lowercase()
            val code = when {
                "InvalidCredentials" in n || "invalid-credential" in m || "wrong-password" in m || "password is invalid" in m -> "invalid"
                "InvalidUser" in n || "user-not-found" in m || "no user record" in m -> "no-user"
                "UserCollision" in n || "email-already" in m || "already in use" in m -> "in-use"
                "WeakPassword" in n || "weak-password" in m -> "weak"
                "RecentLogin" in n || "recent" in m && "login" in m -> "recent-login"
                "Network" in n || "network" in m || "unreachable" in m || "timeout" in m -> "network"
                "TooManyRequests" in n || "too-many" in m || "blocked all requests" in m -> "too-many"
                "badly formatted" in m || "invalid-email" in m -> "email"
                else -> "unknown"
            }
            return AuthError(code)
        }
    }
}

class AuthRepo(scope: CoroutineScope) {
    private val auth get() = Firebase.auth
    private val _user = MutableStateFlow<AuthUser?>(null)
    val user: StateFlow<AuthUser?> = _user
    private val _ready = MutableStateFlow(false)
    /** True once Firebase has reported the initial auth state (avoids flashing the sign-in screen). */
    val ready: StateFlow<Boolean> = _ready

    init {
        _user.value = runCatching { auth.currentUser?.toUser() }.getOrNull()
        scope.launch {
            runCatching { auth.authStateChanged.collect { _user.value = it?.toUser(); _ready.value = true } }
                .onFailure { _ready.value = true }
        }
        _ready.value = _user.value != null || _ready.value
    }

    private fun FirebaseUser.toUser() = AuthUser(uid, email, displayName, isEmailVerified, providerData.map { it.providerId })

    private suspend fun <T> guard(block: suspend () -> T): Result<T> = runCatching { block() }.recoverCatching { throw AuthError.from(it) }

    suspend fun signIn(email: String, password: String): Result<Unit> = guard {
        auth.signInWithEmailAndPassword(email.trim(), password); Unit
    }

    suspend fun signUp(name: String, email: String, password: String): Result<Unit> = guard {
        val r = auth.createUserWithEmailAndPassword(email.trim(), password)
        r.user?.updateProfile(displayName = name.trim())
        runCatching { r.user?.sendEmailVerification() }
        _user.value = auth.currentUser?.toUser()
    }

    suspend fun sendReset(email: String): Result<Unit> = guard { auth.sendPasswordResetEmail(email.trim()) }

    suspend fun signInWithGoogle(idToken: String): Result<Unit> = guard { auth.signInWithCredential(GoogleAuthProvider.credential(idToken, null)); Unit }

    suspend fun signInWithApple(idToken: String, rawNonce: String): Result<Unit> = guard {
        auth.signInWithCredential(OAuthProvider.credential(providerId = "apple.com", idToken = idToken, rawNonce = rawNonce)); Unit
    }

    suspend fun resendVerification(): Result<Unit> = guard { auth.currentUser?.sendEmailVerification(); Unit }

    suspend fun updateName(name: String): Result<Unit> = guard { auth.currentUser?.updateProfile(displayName = name.trim()); _user.value = auth.currentUser?.toUser() }

    suspend fun changePassword(current: String, new: String): Result<Unit> = guard {
        val u = auth.currentUser ?: throw AuthError("no-user")
        u.reauthenticate(EmailAuthProvider.credential(u.email ?: "", current))
        u.updatePassword(new)
    }

    suspend fun signOut() { runCatching { auth.signOut() } ; _user.value = null }

    /** Deletes the Firebase user (call after the cloud data has been removed). */
    suspend fun deleteUser(): Result<Unit> = guard { auth.currentUser?.delete(); _user.value = null }
}

object Validation {
    private val email = Regex("^[^@\\s]+@[^@\\s]+\\.[^@\\s]{2,}$")
    fun email(s: String) = email.matches(s.trim())
    /** 0–4 strength: length ≥ 8, has letter, has digit, has symbol / ≥ 12. */
    fun strength(p: String): Int {
        if (p.isEmpty()) return 0
        var s = 0
        if (p.length >= 8) s++
        if (p.any { it.isLetter() } && p.any { it.isDigit() }) s++
        if (p.any { !it.isLetterOrDigit() } || p.length >= 12) s++
        if (p.any { it.isUpperCase() } && p.any { it.isLowerCase() }) s++
        return s
    }
    fun passwordOk(p: String) = p.length >= 8 && p.any { it.isLetter() } && p.any { it.isDigit() }
}
