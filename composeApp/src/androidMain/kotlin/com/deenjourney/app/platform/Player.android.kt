package com.deenjourney.app.platform

import android.media.MediaPlayer
import android.net.Uri
import android.os.Handler
import android.os.Looper
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import com.deenjourney.app.core.AndroidPlatform
import com.deenjourney.app.platform.audio.ControllerHolder
import com.deenjourney.app.platform.audio.safeDuration
import com.deenjourney.app.platform.audio.short
import com.deenjourney.app.platform.audio.toMediaItem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

actual class RecitationPlayer actual constructor() {
    private val _state = MutableStateFlow(PlayerState())
    actual val state: StateFlow<PlayerState> = _state
    private val main = Handler(Looper.getMainLooper())
    private var items: List<AudioItem> = emptyList()
    private val retried = HashSet<String>()
    private var sleepRunnable: Runnable? = null
    private var fadeRunnable: Runnable? = null
    private var sleepGeneration = 0
    private var stopAtEnd = false
    private var listenerAdded = false

    private val ticker = object : Runnable {
        override fun run() {
            ControllerHolder.controller?.let { publish(it) }
            main.postDelayed(this, 300)
        }
    }

    private fun publish(c: Player) {
        if (stopAtEnd && c.playbackState == Player.STATE_ENDED) {
            stopAtEnd = false
            _state.value = _state.value.copy(sleepEndOfQueue = false)
        }
        _state.value = _state.value.copy(
            items = items, index = if (items.isEmpty()) -1 else c.currentMediaItemIndex, playing = c.isPlaying,
            buffering = c.playbackState == Player.STATE_BUFFERING, positionMs = c.currentPosition.coerceAtLeast(0), durationMs = c.safeDuration,
            speed = c.playbackParameters.speed,
            repeat = when (c.repeatMode) { Player.REPEAT_MODE_ONE -> RepeatMode.One; Player.REPEAT_MODE_ALL -> RepeatMode.All; else -> RepeatMode.Off },
        )
    }

    private fun ensureListener(c: Player) {
        if (listenerAdded) return
        listenerAdded = true
        c.addListener(object : Player.Listener {
            override fun onEvents(player: Player, events: Player.Events) = publish(player)
            override fun onPlayerError(error: PlaybackException) {
                // streaming host failed: retry the current ayah once from the fallback CDN
                val i = c.currentMediaItemIndex
                val item = items.getOrNull(i)
                if (item != null && item.fallbackUrl != null && retried.add(item.id)) {
                    c.replaceMediaItem(i, MediaItem.Builder().setMediaId(item.id).setUri(Uri.parse(item.fallbackUrl)).setMediaMetadata(c.getMediaItemAt(i).mediaMetadata).build())
                    c.prepare(); c.play()
                } else _state.value = _state.value.copy(error = error.short(), playing = false)
            }
            override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
                if (stopAtEnd && reason == Player.MEDIA_ITEM_TRANSITION_REASON_AUTO && c.currentMediaItemIndex == 0) { c.pause(); stopAtEnd = false }
            }
        })
        main.removeCallbacks(ticker); main.post(ticker)
    }

    actual fun load(items: List<AudioItem>, startIndex: Int, startMs: Long, autoplay: Boolean) {
        this.items = items
        retried.clear()
        _state.value = _state.value.copy(items = items, index = startIndex, positionMs = startMs, durationMs = 0, playing = false, error = null)
        ControllerHolder.with { c ->
            ensureListener(c)
            c.setMediaItems(items.map { it.toMediaItem() }, startIndex.coerceIn(0, (items.size - 1).coerceAtLeast(0)), startMs)
            c.prepare()
            c.playWhenReady = autoplay
        }
    }

    actual fun play() = ControllerHolder.with { it.play() }
    actual fun pause() = ControllerHolder.with { it.pause() }
    actual fun toggle() = ControllerHolder.with { if (it.isPlaying) it.pause() else { if (it.playbackState == Player.STATE_ENDED) it.seekTo(0, 0); it.play() } }
    actual fun seekTo(ms: Long) = ControllerHolder.with { it.seekTo(ms) }
    actual fun next() = ControllerHolder.with { if (it.hasNextMediaItem()) it.seekToNextMediaItem() }
    actual fun previous() = ControllerHolder.with { if (it.currentPosition > 3000 || !it.hasPreviousMediaItem()) it.seekTo(0) else it.seekToPreviousMediaItem() }
    actual fun jumpTo(index: Int) = ControllerHolder.with { if (index in 0 until it.mediaItemCount) { it.seekTo(index, 0); it.play() } }
    actual fun setSpeed(speed: Float) = ControllerHolder.with { it.setPlaybackSpeed(speed.coerceIn(0.5f, 2f)) }
    actual fun setRepeat(mode: RepeatMode) = ControllerHolder.with {
        it.repeatMode = when (mode) { RepeatMode.One -> Player.REPEAT_MODE_ONE; RepeatMode.All -> Player.REPEAT_MODE_ALL; RepeatMode.Off -> Player.REPEAT_MODE_OFF }
    }
    actual fun stop() { sleepAfter(0); ControllerHolder.with { it.stop(); it.clearMediaItems(); items = emptyList(); _state.value = PlayerState() } }

    actual fun sleepAfter(minutes: Int, fade: Boolean) {
        sleepRunnable?.let { main.removeCallbacks(it) }
        fadeRunnable?.let { main.removeCallbacks(it) }; fadeRunnable = null
        val generation = ++sleepGeneration
        ControllerHolder.with { it.volume = 1f }
        stopAtEnd = false
        _state.value = _state.value.copy(sleepEndOfQueue = false)
        if (minutes <= 0) { _state.value = _state.value.copy(sleepAtMs = null); return }
        val at = System.currentTimeMillis() + minutes * 60_000L
        _state.value = _state.value.copy(sleepAtMs = at)
        val r = Runnable {
            ControllerHolder.with { c ->
                if (generation != sleepGeneration) return@with
                if (fade) {
                    val step = object : Runnable { override fun run() {
                        if (generation != sleepGeneration) return
                        val remaining = at - System.currentTimeMillis()
                        if (remaining <= 0) { c.pause(); c.volume = 1f; _state.value = _state.value.copy(sleepAtMs = null); fadeRunnable = null }
                        else { c.volume = (remaining / 10000f).coerceIn(0f, 1f); main.postDelayed(this, 250) }
                    } }
                    fadeRunnable = step
                    main.post(step)
                } else { c.pause(); _state.value = _state.value.copy(sleepAtMs = null) }
            }
        }
        sleepRunnable = r
        main.postDelayed(r, minutes * 60_000L - if (fade) 10_000L else 0L)
    }

    actual fun sleepAtEndOfQueue() { sleepAfter(0); stopAtEnd = true; _state.value = _state.value.copy(sleepEndOfQueue = true); ControllerHolder.with { it.repeatMode = Player.REPEAT_MODE_OFF } }
}

actual object SoundFx {
    private var mp: MediaPlayer? = null
    actual fun playUrl(url: String) {
        stop()
        runCatching {
            mp = MediaPlayer().apply { setDataSource(url); setOnPreparedListener { it.start() }; setOnCompletionListener { stop() }; prepareAsync() }
        }
    }
    actual fun playResource(path: String) {
        stop()
        runCatching {
            val fd = AndroidPlatform.context.assets.openFd("composeResources/com.deenjourney.app.res/$path")
            mp = MediaPlayer().apply { setDataSource(fd.fileDescriptor, fd.startOffset, fd.length); setOnCompletionListener { stop() }; prepare(); start() }
            fd.close()
        }
    }
    actual fun stop() { runCatching { mp?.release() }; mp = null }
}
