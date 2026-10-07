package com.deenjourney.app.platform

import android.media.AudioAttributes
import android.media.MediaPlayer
import com.deenjourney.app.core.AndroidPlatform
import com.deenjourney.app.data.quran.qaidaLetters
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

actual class QaidaAudio actual constructor() {
    private val status = MutableStateFlow(QaidaAudioState.Ready)
    actual val state: StateFlow<QaidaAudioState> = status
    private var media: MediaPlayer? = null
    private var closed = false

    actual fun play(file: String) {
        if (closed) return
        stop()
        if (qaidaLetters.none { it.audioFile == file }) { status.value = QaidaAudioState.Error; return }
        val next = MediaPlayer()
        media = next
        status.value = QaidaAudioState.Loading
        runCatching {
            next.setAudioAttributes(AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_MEDIA)
                .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH).build())
            AndroidPlatform.context.assets.openFd("composeResources/com.deenjourney.app.res/files/audio/qaida/$file").use { fd ->
                next.setDataSource(fd.fileDescriptor, fd.startOffset, fd.length)
            }
            next.setOnPreparedListener {
                if (!closed && media === it) {
                    runCatching { it.start() }.onSuccess { status.value = QaidaAudioState.Playing }
                        .onFailure { stop(); status.value = QaidaAudioState.Error }
                }
            }
            next.setOnCompletionListener { if (media === it) stop() }
            next.setOnErrorListener { failed, _, _ ->
                if (media === failed) { stop(); status.value = QaidaAudioState.Error }
                true
            }
            next.prepareAsync()
        }.onFailure { stop(); status.value = QaidaAudioState.Error }
    }

    actual fun stop() {
        val previous = media
        media = null
        runCatching { previous?.release() }
        status.value = QaidaAudioState.Ready
    }
    actual fun close() { if (!closed) { closed = true; stop() } }
}
