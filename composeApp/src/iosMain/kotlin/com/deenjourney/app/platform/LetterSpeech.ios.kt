package com.deenjourney.app.platform

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import platform.AVFAudio.AVSpeechBoundaryImmediate
import platform.AVFAudio.AVSpeechSynthesisVoice
import platform.AVFAudio.AVSpeechSynthesizer
import platform.AVFAudio.AVSpeechUtterance
import platform.Foundation.NSURL
import platform.UIKit.UIApplication

@OptIn(ExperimentalForeignApi::class)
actual class LetterSpeech actual constructor() {
    private val synth = AVSpeechSynthesizer()
    private val status = MutableStateFlow(SpeechState.Ready)
    actual val state: StateFlow<SpeechState> = status
    private var closed = false

    init { refreshVoice() }
    actual fun refreshVoice() {
        if (!closed) status.value = if (AVSpeechSynthesisVoice.voiceWithLanguage("ar-SA") != null) SpeechState.Ready else SpeechState.Unavailable
    }
    actual fun speakArabic(text: String) {
        if (closed || text.isBlank()) return
        val voice = AVSpeechSynthesisVoice.voiceWithLanguage("ar-SA")
        if (voice == null) { status.value = SpeechState.Unavailable; return }
        synth.stopSpeakingAtBoundary(AVSpeechBoundaryImmediate)
        val utterance = AVSpeechUtterance.speechUtteranceWithString(text)
        utterance.voice = voice
        utterance.rate = 0.4f
        synth.speakUtterance(utterance)
    }
    actual fun stop() { synth.stopSpeakingAtBoundary(AVSpeechBoundaryImmediate) }
    actual fun close() { stop(); closed = true }
    actual fun openVoiceSettings() {
        NSURL.URLWithString("app-settings:")?.let { UIApplication.sharedApplication.openURL(it) }
    }
}
