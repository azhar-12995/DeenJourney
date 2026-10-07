package com.deenjourney.app.platform

import kotlinx.coroutines.flow.StateFlow

enum class SpeechState { Loading, Ready, Speaking, Unavailable, Error }

/** Screen-owned Arabic pronunciation: the latest tap replaces the previous utterance. */
expect class LetterSpeech() {
    val state: StateFlow<SpeechState>
    fun speakArabic(text: String)
    fun refreshVoice()
    fun stop()
    fun close()
    fun openVoiceSettings()
}
