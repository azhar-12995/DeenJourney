package com.deenjourney.app.platform.audio

import android.content.ComponentName
import android.content.Intent
import android.net.Uri
import android.os.Handler
import android.os.Looper
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.MediaController
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService
import androidx.media3.session.SessionToken
import com.deenjourney.app.MainActivity
import com.deenjourney.app.core.AndroidPlatform
import java.io.File

/** Hosts the ExoPlayer + MediaSession so recitation keeps playing in the background with lock-screen controls. */
class RecitationService : MediaSessionService() {
    private var session: MediaSession? = null

    override fun onCreate() {
        super.onCreate()
        val player = ExoPlayer.Builder(this)
            .setAudioAttributes(AudioAttributes.Builder().setUsage(C.USAGE_MEDIA).setContentType(C.AUDIO_CONTENT_TYPE_SPEECH).build(), true)
            .setHandleAudioBecomingNoisy(true)
            .setWakeMode(C.WAKE_MODE_NETWORK)
            .build()
        val open = android.app.PendingIntent.getActivity(this, 0, Intent(this, MainActivity::class.java), android.app.PendingIntent.FLAG_IMMUTABLE or android.app.PendingIntent.FLAG_UPDATE_CURRENT)
        session = MediaSession.Builder(this, player).setSessionActivity(open).build()
    }

    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaSession? = session

    override fun onTaskRemoved(rootIntent: Intent?) {
        val p = session?.player
        if (p == null || !p.playWhenReady || p.mediaItemCount == 0) stopSelf()
    }

    override fun onDestroy() {
        session?.run { player.release(); release() }
        session = null
        super.onDestroy()
    }
}

/** Builds MediaItems; local downloaded files win over streaming URLs. */
internal fun com.deenjourney.app.platform.AudioItem.toMediaItem(): MediaItem {
    val uri = localPath?.takeIf { File(it).exists() }?.let { Uri.fromFile(File(it)) } ?: Uri.parse(url)
    return MediaItem.Builder().setMediaId(id).setUri(uri)
        .setMediaMetadata(MediaMetadata.Builder().setTitle(title).setArtist(subtitle).setDisplayTitle(title).build()).build()
}

internal object ControllerHolder {
    private var future: com.google.common.util.concurrent.ListenableFuture<MediaController>? = null
    var controller: MediaController? = null
        private set
    private val pending = ArrayList<(MediaController) -> Unit>()

    fun with(block: (MediaController) -> Unit) {
        controller?.let { block(it); return }
        pending += block
        if (future == null) {
            val ctx = AndroidPlatform.context
            val token = SessionToken(ctx, ComponentName(ctx, RecitationService::class.java))
            val f = MediaController.Builder(ctx, token).buildAsync()
            future = f
            f.addListener({
                runCatching { f.get() }.onSuccess { c ->
                    controller = c
                    pending.toList().forEach { it(c) }; pending.clear()
                }.onFailure { future = null }
            }, { Handler(Looper.getMainLooper()).post(it) })
        }
    }
}

internal fun PlaybackException.short(): String = errorCodeName.removePrefix("ERROR_CODE_")
internal val Player.safeDuration: Long get() = duration.takeIf { it != C.TIME_UNSET && it > 0 } ?: 0L
