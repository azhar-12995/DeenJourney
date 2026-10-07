package com.deenjourney.app.platform

import android.content.Intent
import android.media.AudioAttributes
import android.os.Handler
import android.os.Looper
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import com.deenjourney.app.core.AndroidPlatform
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.util.Locale

actual class LetterSpeech actual constructor() {
    private val status = MutableStateFlow(SpeechState.Loading)
    actual val state: StateFlow<SpeechState> = status
    private val handler = Handler(Looper.getMainLooper())
    private var closed = false
    private var initialized = false
    private var pending: String? = null
    private var currentId: String? = null
    private var sequence = 0L
    private val engine = TextToSpeech(AndroidPlatform.context) { result ->
        handler.post {
            if (!closed) {
                initialized = result == TextToSpeech.SUCCESS
                if (initialized) {
                    configureVoice()
                    pending?.let { text -> pending = null; speakArabic(text) }
                } else status.value = SpeechState.Unavailable
            }
        }
    }

    init {
        engine.setAudioAttributes(AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_MEDIA)
            .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH).build())
        engine.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
            override fun onStart(id: String?) = report(id, SpeechState.Speaking)
            override fun onDone(id: String?) = report(id, SpeechState.Ready)
            @Deprecated("Required by Android's listener")
            override fun onError(id: String?) = report(id, SpeechState.Error)
            @Deprecated("Required by Android's listener")
            override fun onError(id: String?, errorCode: Int) = report(id, SpeechState.Error)
        })
    }

    private fun report(id: String?, value: SpeechState) {
        handler.post { if (!closed && id == currentId) status.value = value }
    }

    private fun configureVoice() {
        val availability = engine.setLanguage(Locale.forLanguageTag("ar-SA"))
        if (availability < TextToSpeech.LANG_AVAILABLE) { status.value = SpeechState.Unavailable; return }
        // Prefer installed Arabic data. Never fall back to an English pronunciation.
        engine.voices?.filter { it.locale.language == "ar" && !it.isNetworkConnectionRequired }
            ?.maxByOrNull { it.quality + if (it.locale.country == "SA") 1000 else 0 }?.let { engine.voice = it }
        engine.setSpeechRate(0.75f)
        status.value = SpeechState.Ready
    }

    actual fun speakArabic(text: String) {
        if (closed || text.isBlank()) return
        if (!initialized) {
            if (status.value == SpeechState.Loading) pending = text
            return
        }
        if (status.value == SpeechState.Unavailable) return
        currentId = "qaida.${++sequence}"
        if (engine.speak(text, TextToSpeech.QUEUE_FLUSH, null, currentId) == TextToSpeech.ERROR)
            status.value = SpeechState.Error
    }

    actual fun refreshVoice() { if (!closed && initialized && status.value != SpeechState.Speaking) configureVoice() }
    actual fun stop() {
        pending = null
        currentId = null
        engine.stop()
        if (initialized && status.value != SpeechState.Unavailable) status.value = SpeechState.Ready
    }
    actual fun close() { if (closed) return; stop(); closed = true; engine.shutdown() }

    actual fun openVoiceSettings() {
        val context = AndroidPlatform.context
        val install = Intent(TextToSpeech.Engine.ACTION_INSTALL_TTS_DATA).setPackage(engine.defaultEngine)
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        if (runCatching { context.startActivity(install) }.isFailure)
            runCatching { context.startActivity(Intent("com.android.settings.TTS_SETTINGS").addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)) }
    }
}
