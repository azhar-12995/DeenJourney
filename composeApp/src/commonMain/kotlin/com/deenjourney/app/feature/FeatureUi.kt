package com.deenjourney.app.feature

import androidx.compose.foundation.layout.*
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.deenjourney.app.core.*
import com.deenjourney.app.data.content.Ref
import com.deenjourney.app.data.content.Section
import com.deenjourney.app.data.user.UserRepo
import com.deenjourney.app.design.*
import com.deenjourney.app.nav.LocalNavigator
import com.deenjourney.app.nav.Reader
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch
import org.koin.compose.koinInject

/** Async content with explicit loading, retry, and cancellation handling. */
@Composable
fun <T> Loaded(key: Any?, load: suspend () -> T, content: @Composable (T) -> Unit) {
    var attempt by remember(key) { mutableStateOf(0) }
    var result by remember(key, attempt) { mutableStateOf<Result<T>?>(null) }
    LaunchedEffect(key, attempt) {
        result = try { Result.success(load()) } catch (e: CancellationException) { throw e } catch (e: Exception) { Result.failure(e) }
    }
    val r = result
    when {
        r == null -> CircularProgressIndicator(Modifier.padding(24.dp), color = Dj.c.primary)
        r.isFailure -> EmptyState("empty_error", t("Unable to load", "لوڈ نہیں ہو سکا", "تعذر التحميل"),
            t("Check your connection and try again.", "کنکشن چیک کر کے دوبارہ کوشش کریں۔", "تحقق من الاتصال وحاول مجددًا."),
            primary = t("Retry", "دوبارہ", "إعادة المحاولة"), onPrimary = { attempt++ })
        else -> content(r.getOrThrow())
    }
}

@Composable
fun FeaturePage(title: String, tab: Tab? = null, actions: @Composable RowScope.() -> Unit = {}, content: @Composable ColumnScope.() -> Unit) {
    val nav = LocalNavigator.current
    Screen(top = { AppBar(title, onBack = if (tab == null) ({ nav.back() }) else null, big = tab != null, actions = actions) },
        bottom = tab?.let { { BottomNav(it) { next -> nav.tab(next) } } }) { Body(content = content) }
}

@Composable
fun References(refs: List<Ref>) {
    val nav = LocalNavigator.current
    if (refs.isNotEmpty()) {
        Overline(t("Sources", "ماخذ", "المصادر"))
        refs.forEach { ref ->
            TextLink(ref.label, {
                if (ref.type == "quran") {
                    val parts = ref.key?.split(':')
                    val sura = parts?.getOrNull(0)?.toIntOrNull()
                    val aya = parts?.getOrNull(1)?.substringBefore('-')?.toIntOrNull()
                    if (sura != null && aya != null) nav.go(Reader(sura, aya))
                } else if (ref.type == "hadith" && ref.key != null) {
                    Platform.openUrl("https://sunnah.com/${ref.key}")
                }
            })
        }
    }
}

@Composable
fun SectionContent(section: Section) {
    DjCard(Modifier.fillMaxWidth()) {
        Txt(section.title.text(), Dj.type.titleM)
        Gap(8.dp)
        Txt(section.body.text().replace("\\n", "\n"), Dj.type.bodyM)
        section.bullets.forEach { Txt("• " + it.text(), Dj.type.bodyM) }
        References(section.refs)
    }
}

@Composable
fun SaveAction(type: String, key: String, title: String, sub: String = "") {
    val users = koinInject<UserRepo>()
    val saved by remember(type, key) { users.isSaved(type, key) }.collectAsState(false)
    val scope = rememberCoroutineScope()
    IconBtn(if (saved) "bookmark-check" else "bookmark", { scope.launch { users.toggleSaved(type, key, title, sub) } },
        description = t("Save item", "محفوظ کریں", "حفظ"))
}

@Composable
fun SwitchRow(title: String, value: Boolean, onChange: (Boolean) -> Unit, sub: String? = null) =
    ListRow(title, sub = sub, trailing = { DjSwitch(value, onChange) })

@Composable
fun NoItems() = EmptyState("empty_bookmarks", t("Nothing here yet", "ابھی کوئی آئٹم نہیں", "لا توجد عناصر بعد"),
    t("Saved items and your progress will appear here.", "محفوظ آئٹمز اور پیش رفت یہاں نظر آئیں گے۔", "ستظهر هنا العناصر المحفوظة وتقدمك."))
