package com.deenjourney.app.platform

import com.deenjourney.app.data.quran.qaidaLetters
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import platform.AVFAudio.AVAudioPlayer
import platform.AVFAudio.AVAudioPlayerDelegateProtocol
import platform.Foundation.NSBundle
import platform.Foundation.NSURL
import platform.darwin.NSObject

@OptIn(ExperimentalForeignApi::class)
actual class QaidaAudio actual constructor() {
    private val status = MutableStateFlow(QaidaAudioState.Ready)
    actual val state: StateFlow<QaidaAudioState> = status
    private var media: AVAudioPlayer? = null
    private var closed = false
    private val delegate = object : NSObject(), AVAudioPlayerDelegateProtocol {
        override fun audioPlayerDidFinishPlaying(player: AVAudioPlayer, successfully: Boolean) {
            if (media == player) { stop(); if (!successfully) status.value = QaidaAudioState.Error }
        }
    }

    actual fun play(file: String) {
        if (closed) return
        stop()
        if (qaidaLetters.none { it.audioFile == file }) { status.value = QaidaAudioState.Error; return }
        status.value = QaidaAudioState.Loading
        val full = NSBundle.mainBundle.resourcePath + "/compose-resources/composeResources/com.deenjourney.app.res/files/audio/qaida/" + file
        runCatching {
            val next = AVAudioPlayer(contentsOfURL = NSURL.fileURLWithPath(full), error = null)
            media = next
            next.delegate = delegate
            status.value = if (next.play()) QaidaAudioState.Playing else QaidaAudioState.Error
        }.onFailure { stop(); status.value = QaidaAudioState.Error }
    }
    actual fun stop() { media?.stop(); media = null; status.value = QaidaAudioState.Ready }
    actual fun close() { if (!closed) { closed = true; stop() } }
}
