package com.deenjourney.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.deenjourney.app.nav.*

/** Declared only by the debug manifest; isolates design QA from login/signup. */
class DesignPreviewActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (!BuildConfig.DEBUG) { finish(); return }
        enableEdgeToEdge()
        val route: com.deenjourney.app.nav.R = when (intent.getStringExtra("screen")) {
            "home" -> Home
            "quran" -> QuranHome()
            "prayers" -> PrayerTimes
            "duas" -> Duas
            "learn" -> LearnHub
            "tasbih" -> Tasbih()
            "ramadan" -> Ramadan
            "reader" -> Reader(2, 11)
            "player" -> Player
            "qibla" -> Qibla(1)
            "qaida" -> Qaida
            "settings" -> SettingsRoute
            else -> ShareAyah(2, intent.getIntExtra("ayah", 153))
        }
        setContent { App(route) }
    }
}
