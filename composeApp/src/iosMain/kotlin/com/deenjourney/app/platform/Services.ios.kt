package com.deenjourney.app.platform

import com.deenjourney.app.core.Platform
import com.deenjourney.app.data.settings.SettingsRepo
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.useContents
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withTimeoutOrNull
import org.koin.mp.KoinPlatform
import platform.AVFAudio.AVAudioSession
import platform.AVFAudio.AVAudioSessionCategoryPlayback
import platform.AVFAudio.setActive
import platform.AVFoundation.AVPlayer
import platform.AVFoundation.AVPlayerItem
import platform.AVFoundation.AVPlayerItemDidPlayToEndTimeNotification
import platform.AVFoundation.currentItem
import platform.AVFoundation.currentTime
import platform.AVFoundation.duration
import platform.AVFoundation.pause
import platform.AVFoundation.play
import platform.AVFoundation.rate
import platform.AVFoundation.replaceCurrentItemWithPlayerItem
import platform.AVFoundation.seekToTime
import platform.AVFoundation.setRate
import platform.AVFoundation.setVolume
import platform.CoreLocation.CLHeading
import platform.CoreLocation.CLLocation
import platform.CoreLocation.CLLocationManager
import platform.CoreLocation.CLLocationManagerDelegateProtocol
import platform.CoreLocation.kCLLocationAccuracyHundredMeters
import platform.CoreMedia.CMTimeGetSeconds
import platform.CoreMedia.CMTimeMakeWithSeconds
import platform.Foundation.NSDate
import platform.Foundation.NSDateComponents
import platform.Foundation.NSError
import platform.Foundation.NSNotificationCenter
import platform.Foundation.NSOperationQueue
import platform.Foundation.NSTimer
import platform.Foundation.NSURL
import platform.Foundation.timeIntervalSince1970
import platform.MediaPlayer.MPMediaItemPropertyArtist
import platform.MediaPlayer.MPMediaItemPropertyTitle
import platform.MediaPlayer.MPNowPlayingInfoCenter
import platform.MediaPlayer.MPRemoteCommandCenter
import platform.MediaPlayer.MPRemoteCommandHandlerStatusSuccess
import platform.UserNotifications.UNCalendarNotificationTrigger
import platform.UserNotifications.UNMutableNotificationContent
import platform.UserNotifications.UNNotificationRequest
import platform.UserNotifications.UNNotificationSound
import platform.UserNotifications.UNTimeIntervalNotificationTrigger
import platform.UserNotifications.UNUserNotificationCenter
import platform.darwin.NSObject
import kotlin.coroutines.resume

// ---------------------------------------------------------------- location

@OptIn(ExperimentalForeignApi::class)
actual class LocationService actual constructor() {
    private val manager = CLLocationManager()
    actual fun servicesEnabled(): Boolean = CLLocationManager.locationServicesEnabled()

    actual suspend fun current(timeoutMs: Long): GeoFix? = withTimeoutOrNull(timeoutMs) {
        suspendCancellableCoroutine { cont ->
            val delegate = object : NSObject(), CLLocationManagerDelegateProtocol {
                override fun locationManager(manager: CLLocationManager, didUpdateLocations: List<*>) {
                    val loc = didUpdateLocations.lastOrNull() as? CLLocation ?: return
                    manager.stopUpdatingLocation()
                    loc.coordinate.useContents { if (cont.isActive) cont.resume(GeoFix(latitude, longitude, loc.horizontalAccuracy.toFloat())) }
                }
                override fun locationManager(manager: CLLocationManager, didFailWithError: NSError) { if (cont.isActive) cont.resume(null) }
            }
            manager.delegate = delegate
            manager.desiredAccuracy = kCLLocationAccuracyHundredMeters
            manager.requestWhenInUseAuthorization()
            manager.startUpdatingLocation()
            cont.invokeOnCancellation { manager.stopUpdatingLocation() }
        }
    }
}

actual class CompassService actual constructor() {
    private val manager = CLLocationManager()
    actual val available: Boolean get() = CLLocationManager.headingAvailable()

    actual fun headings(lat: Double, lng: Double): Flow<Heading> = callbackFlow {
        val delegate = object : NSObject(), CLLocationManagerDelegateProtocol {
            override fun locationManager(manager: CLLocationManager, didUpdateHeading: CLHeading) {
                val az = if (didUpdateHeading.trueHeading >= 0) didUpdateHeading.trueHeading else didUpdateHeading.magneticHeading
                val acc = didUpdateHeading.headingAccuracy
                trySend(Heading(az.toFloat(), when { acc < 0 -> 0; acc > 25 -> 1; acc > 10 -> 2; else -> 3 }))
            }
            override fun locationManagerShouldDisplayHeadingCalibration(manager: CLLocationManager): Boolean = true
        }
        manager.delegate = delegate
        manager.startUpdatingLocation()
        manager.startUpdatingHeading()
        awaitClose { manager.stopUpdatingHeading(); manager.stopUpdatingLocation() }
    }
}

// ---------------------------------------------------------------- recitation (AVPlayer, one item at a time)

@OptIn(ExperimentalForeignApi::class)
actual class RecitationPlayer actual constructor() {
    private val _state = MutableStateFlow(PlayerState())
    actual val state: StateFlow<PlayerState> = _state
    private val player = AVPlayer()
    private var items: List<AudioItem> = emptyList()
    private var index = 0
    private var speed = 1f
    private var repeat = RepeatMode.Off
    private var timer: NSTimer? = null
    private var sleepTimer: NSTimer? = null
    private var stopAtEnd = false
    private var observer: Any? = null

    init {
        runCatching { AVAudioSession.sharedInstance().setCategory(AVAudioSessionCategoryPlayback, null); AVAudioSession.sharedInstance().setActive(true, null) }
        val cc = MPRemoteCommandCenter.sharedCommandCenter()
        cc.playCommand.addTargetWithHandler { play(); MPRemoteCommandHandlerStatusSuccess }
        cc.pauseCommand.addTargetWithHandler { pause(); MPRemoteCommandHandlerStatusSuccess }
        cc.nextTrackCommand.addTargetWithHandler { next(); MPRemoteCommandHandlerStatusSuccess }
        cc.previousTrackCommand.addTargetWithHandler { previous(); MPRemoteCommandHandlerStatusSuccess }
        observer = NSNotificationCenter.defaultCenter.addObserverForName(AVPlayerItemDidPlayToEndTimeNotification, null, NSOperationQueue.mainQueue) { _ -> onEnded() }
        timer = NSTimer.scheduledTimerWithTimeInterval(0.3, true) { _ -> publish() }
    }

    private fun urlFor(i: AudioItem): NSURL? =
        i.localPath?.takeIf { Platform.fileExists(it) }?.let { NSURL.fileURLWithPath(it) } ?: NSURL.URLWithString(i.url)

    private fun startItem(i: Int, ms: Long = 0, autoplay: Boolean = true) {
        val item = items.getOrNull(i) ?: return
        index = i
        val url = urlFor(item) ?: return
        player.replaceCurrentItemWithPlayerItem(AVPlayerItem(uRL = url))
        if (ms > 0) player.seekToTime(CMTimeMakeWithSeconds(ms / 1000.0, 600))
        if (autoplay) { player.play(); player.setRate(speed) }
        MPNowPlayingInfoCenter.defaultCenter().nowPlayingInfo = mapOf<Any?, Any?>(MPMediaItemPropertyTitle to item.title, MPMediaItemPropertyArtist to item.subtitle)
        publish()
    }

    private fun onEnded() {
        when {
            repeat == RepeatMode.One -> startItem(index)
            index + 1 < items.size -> startItem(index + 1)
            repeat == RepeatMode.All && items.isNotEmpty() && !stopAtEnd -> startItem(0)
            else -> { stopAtEnd = false; _state.value = _state.value.copy(sleepEndOfQueue = false); publish() }
        }
    }

    private fun publish() {
        val cur = player.currentItem
        val pos = (CMTimeGetSeconds(player.currentTime()) * 1000).toLong().coerceAtLeast(0)
        val dur = cur?.let { (CMTimeGetSeconds(it.duration) * 1000).let { d -> if (d.isNaN()) 0L else d.toLong() } } ?: 0L
        _state.value = _state.value.copy(items = items, index = if (items.isEmpty()) -1 else index, playing = player.rate > 0f, positionMs = pos, durationMs = dur, speed = speed, repeat = repeat)
    }

    actual fun load(items: List<AudioItem>, startIndex: Int, startMs: Long, autoplay: Boolean) { this.items = items; startItem(startIndex, startMs, autoplay) }
    actual fun play() { if (player.currentItem == null && items.isNotEmpty()) startItem(index) else { player.play(); player.setRate(speed) }; publish() }
    actual fun pause() { player.pause(); publish() }
    actual fun toggle() { if (player.rate > 0f) pause() else play() }
    actual fun seekTo(ms: Long) { player.seekToTime(CMTimeMakeWithSeconds(ms / 1000.0, 600)) }
    actual fun next() { if (index + 1 < items.size) startItem(index + 1) }
    actual fun previous() { if (index > 0) startItem(index - 1) else seekTo(0) }
    actual fun jumpTo(index: Int) = startItem(index)
    actual fun setSpeed(speed: Float) { this.speed = speed.coerceIn(0.5f, 2f); if (player.rate > 0f) player.setRate(this.speed); publish() }
    actual fun setRepeat(mode: RepeatMode) { repeat = mode; publish() }
    actual fun stop() { sleepAfter(0); player.pause(); player.replaceCurrentItemWithPlayerItem(null); items = emptyList(); _state.value = PlayerState() }
    actual fun sleepAfter(minutes: Int, fade: Boolean) {
        sleepTimer?.invalidate(); sleepTimer = null
        player.setVolume(1f); stopAtEnd = false
        _state.value = _state.value.copy(sleepEndOfQueue = false)
        if (minutes <= 0) { _state.value = _state.value.copy(sleepAtMs = null); return }
        val deadline = NSDate().timeIntervalSince1970 + minutes * 60.0
        _state.value = _state.value.copy(sleepAtMs = (deadline * 1000).toLong())
        sleepTimer = NSTimer.scheduledTimerWithTimeInterval(0.25, true) { _ ->
            val remaining = deadline - NSDate().timeIntervalSince1970
            if (remaining <= 0) {
                sleepTimer?.invalidate(); sleepTimer = null; pause(); player.setVolume(1f)
                _state.value = _state.value.copy(sleepAtMs = null)
            } else if (fade && remaining < 10) player.setVolume((remaining / 10).toFloat())
        }
    }
    actual fun sleepAtEndOfQueue() { sleepAfter(0); stopAtEnd = true; repeat = RepeatMode.Off; _state.value = _state.value.copy(sleepEndOfQueue = true); publish() }
}

actual object SoundFx {
    private val p = AVPlayer()
    actual fun playUrl(url: String) { NSURL.URLWithString(url)?.let { p.replaceCurrentItemWithPlayerItem(AVPlayerItem(uRL = it)); p.play() } }
    actual fun playResource(path: String) {
        val full = platform.Foundation.NSBundle.mainBundle.resourcePath + "/compose-resources/composeResources/com.deenjourney.app.res/" + path
        p.replaceCurrentItemWithPlayerItem(AVPlayerItem(uRL = NSURL.fileURLWithPath(full))); p.play()
    }
    actual fun stop() { p.pause() }
}

// ---------------------------------------------------------------- local notifications (≤ 64 pending on iOS)

actual object Scheduler {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    actual fun canScheduleExact(): Boolean = true
    actual fun openExactAlarmSettings() {}
    actual fun openBatterySettings() {}

    actual fun reschedule() {
        scope.launch {
            val settings = KoinPlatform.getKoin().get<SettingsRepo>().get()
            val plan = AlarmPlanner.plan(settings, days = 10).take(60)
            val center = UNUserNotificationCenter.currentNotificationCenter()
            center.removeAllPendingNotificationRequests()
            val now = NSDate().timeIntervalSince1970
            for (a in plan) {
                val content = UNMutableNotificationContent().apply {
                    setTitle(a.title); setBody(a.body)
                    setSound(if (a.sound == "silent") null else UNNotificationSound.defaultSound)
                }
                val secs = a.at.toEpochMilliseconds() / 1000.0 - now
                if (secs <= 1) continue
                val trigger = UNTimeIntervalNotificationTrigger.triggerWithTimeInterval(secs, false)
                center.addNotificationRequest(UNNotificationRequest.requestWithIdentifier("dj.${a.id}.${a.at.toEpochMilliseconds()}", content, trigger), null)
            }
        }
    }

    actual fun notify(id: Int, title: String, body: String, channel: String) {
        val content = UNMutableNotificationContent().apply { setTitle(title); setBody(body); setSound(UNNotificationSound.defaultSound) }
        UNUserNotificationCenter.currentNotificationCenter().addNotificationRequest(
            UNNotificationRequest.requestWithIdentifier("dj.now.$id", content, UNTimeIntervalNotificationTrigger.triggerWithTimeInterval(1.0, false)), null)
    }
}

@Suppress("unused") private val keepImports = listOf(NSDateComponents::class, UNCalendarNotificationTrigger::class)
