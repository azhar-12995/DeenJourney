package com.deenjourney.app.platform

import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import org.junit.Assert.assertEquals
import org.junit.Assume.assumeTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class LetterSpeechTest {
    @Test fun installedArabicVoiceSpeaksAndStops() = runBlocking {
        val speech = withContext(Dispatchers.Main) { LetterSpeech() }
        try {
            val initialized = withTimeout(30_000) { speech.state.first { it != SpeechState.Loading } }
            assumeTrue("An Arabic device voice must be installed for this playback test", initialized == SpeechState.Ready)
            withContext(Dispatchers.Main) { speech.speakArabic("بَاء") }
            val playing = withTimeout(15_000) { speech.state.first { it == SpeechState.Speaking || it == SpeechState.Error } }
            assertEquals(SpeechState.Speaking, playing)
            val completed = withTimeout(15_000) { speech.state.first { it == SpeechState.Ready || it == SpeechState.Error } }
            assertEquals(SpeechState.Ready, completed)
            withContext(Dispatchers.Main) { speech.speakArabic("تَاء"); speech.stop() }
            assertEquals(SpeechState.Ready, speech.state.value)
        } finally { withContext(Dispatchers.Main) { speech.close() } }
    }
}
