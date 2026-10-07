package com.deenjourney.app.platform

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.deenjourney.app.data.quran.qaidaLetters
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class QaidaAudioPlaybackTest {
    @Test fun everyBundledLetterPlaysOfflineAndStops() = runBlocking {
        val audio = withContext(Dispatchers.Main) { QaidaAudio() }
        try {
            for (letter in qaidaLetters) {
                withContext(Dispatchers.Main) { audio.play(letter.audioFile) }
                val playing = withTimeout(15_000) { audio.state.first { it == QaidaAudioState.Playing || it == QaidaAudioState.Error } }
                assertEquals("Recording for ${letter.glyph} must play", QaidaAudioState.Playing, playing)
                val completed = withTimeout(15_000) { audio.state.first { it == QaidaAudioState.Ready || it == QaidaAudioState.Error } }
                assertEquals("Recording for ${letter.glyph} must finish", QaidaAudioState.Ready, completed)
            }
            withContext(Dispatchers.Main) {
                audio.play(qaidaLetters[0].audioFile)
                audio.play(qaidaLetters[1].audioFile)
                audio.stop()
            }
            assertEquals(QaidaAudioState.Ready, audio.state.value)
            withContext(Dispatchers.Main) { audio.play("missing.mp3") }
            assertEquals(QaidaAudioState.Error, audio.state.value)
        } finally { withContext(Dispatchers.Main) { audio.close() } }
    }
}
