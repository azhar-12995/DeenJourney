package com.deenjourney.app

import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import com.deenjourney.app.core.Lang
import com.deenjourney.app.core.LocalLang
import com.deenjourney.app.data.settings.SettingsRepo
import com.deenjourney.app.design.DjTheme
import com.deenjourney.app.nav.LocalNavigator
import com.deenjourney.app.nav.Navigator
import com.deenjourney.app.nav.PrayerTimes
import com.deenjourney.app.nav.Splash
import com.deenjourney.app.nav.registerScreens
import kotlinx.coroutines.flow.MutableStateFlow
import org.koin.compose.koinInject

/** Set by the platform when the app is opened from a notification ("prayer", "lesson" …). */
object PendingOpen { val target = MutableStateFlow<String?>(null) }

@Composable
fun App(startDestination: com.deenjourney.app.nav.R = Splash) {
    val settings = koinInject<SettingsRepo>()
    val s by settings.flow.collectAsState()
    val lang = Lang.of(s.lang)
    val dark = when (s.theme) { "dark" -> true; "light" -> false; else -> isSystemInDarkTheme() }
    CompositionLocalProvider(LocalLang provides lang, LocalLayoutDirection provides if (lang.rtl) LayoutDirection.Rtl else LayoutDirection.Ltr) {
        DjTheme(dark = dark, textScale = s.textScale) {
            val nav = rememberNavController()
            val navigator = remember(nav) { Navigator(nav) }
            CompositionLocalProvider(LocalNavigator provides navigator) {
                NavHost(nav, startDestination = startDestination, enterTransition = { fadeIn() }, exitTransition = { fadeOut() }) {
                    registerScreens()
                }
            }
            RememberPlaybackProgress(settings)
            val open by PendingOpen.target.collectAsState()
            LaunchedEffect(open, s.setupDone) {
                if (open != null && s.setupDone) {
                    when (open) { "prayer" -> navigator.go(PrayerTimes) }
                    PendingOpen.target.value = null
                }
            }
        }
    }
}

/** Keeps exact playback position even when the reader is no longer visible. */
@Composable
private fun RememberPlaybackProgress(settings: SettingsRepo) {
    val player = koinInject<com.deenjourney.app.platform.RecitationPlayer>()
    val p by player.state.collectAsState()
    LaunchedEffect(p.current?.id, p.positionMs / 2000, p.playing) {
        val a = p.current ?: return@LaunchedEffect
        if (a.sura in 1..114 && a.aya > 0 && (p.playing || settings.get().lastRead?.let { it.sura == a.sura && it.aya == a.aya } == true)) {
            settings.update { it.copy(lastRead = com.deenjourney.app.data.settings.LastRead(a.sura, a.aya, p.positionMs, com.deenjourney.app.data.user.nowMs())) }
        }
    }
}
