package com.deenjourney.app.platform

import kotlinx.coroutines.flow.StateFlow

enum class QaidaAudioState { Loading, Ready, Playing, Error }

/** Screen-owned recorded pronunciation. The latest tap replaces the current clip. */
expect class QaidaAudio() {
    val state: StateFlow<QaidaAudioState>
    fun play(file: String)
    fun stop()
    fun close()
}
