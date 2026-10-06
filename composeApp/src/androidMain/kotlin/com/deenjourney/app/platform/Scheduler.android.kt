package com.deenjourney.app.platform

import android.Manifest
import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ServiceInfo
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.net.Uri
import android.os.Build
import android.os.IBinder
import android.provider.Settings
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.deenjourney.app.MainActivity
import com.deenjourney.app.R
import com.deenjourney.app.core.AndroidPlatform
import com.deenjourney.app.data.settings.SettingsRepo
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import org.koin.mp.KoinPlatform

object Channels {
    const val ADHAN_CHIME = "adhan_chime"
    const val ADHAN_TONE = "adhan_tone"
    const val ADHAN_SILENT = "adhan_silent"
    const val REMINDERS = "reminders"

    fun create(ctx: Context) {
        if (Build.VERSION.SDK_INT < 26) return
        val nm = ctx.getSystemService(NotificationManager::class.java)
        val attrs = AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_NOTIFICATION_EVENT).setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION).build()
        fun ch(id: String, name: String, importance: Int, sound: Int?) = NotificationChannel(id, name, importance).apply {
            if (sound != null) setSound(Uri.parse("android.resource://${ctx.packageName}/$sound"), attrs) else setSound(null, null)
            enableVibration(true)
        }
        val adhanName = ctx.getString(R.string.channel_adhan)
        nm.createNotificationChannels(listOf(
            ch(ADHAN_CHIME, "$adhanName · chime", NotificationManager.IMPORTANCE_HIGH, R.raw.adhan_chime),
            ch(ADHAN_TONE, "$adhanName · tone", NotificationManager.IMPORTANCE_HIGH, R.raw.adhan_tone),
            ch(ADHAN_SILENT, "$adhanName · silent", NotificationManager.IMPORTANCE_DEFAULT, null).apply { enableVibration(false) },
            NotificationChannel(REMINDERS, ctx.getString(R.string.channel_reminders), NotificationManager.IMPORTANCE_DEFAULT),
        ))
    }

    fun forSound(sound: String) = when (sound) { "tone" -> ADHAN_TONE; "silent" -> ADHAN_SILENT; else -> ADHAN_CHIME }
}

actual object Scheduler {
    private val ctx get() = AndroidPlatform.context
    private val am get() = ctx.getSystemService(Context.ALARM_SERVICE) as AlarmManager
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private const val PREFS = "alarms"

    actual fun canScheduleExact(): Boolean = Build.VERSION.SDK_INT < 31 || am.canScheduleExactAlarms()

    actual fun openExactAlarmSettings() {
        if (Build.VERSION.SDK_INT >= 31) runCatching {
            ctx.startActivity(Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM, Uri.parse("package:${ctx.packageName}")).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
        }
    }

    actual fun openBatterySettings() {
        runCatching { ctx.startActivity(Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)) }
    }

    actual fun reschedule() {
        scope.launch { runCatching { rescheduleNow() }.onFailure { println("Scheduler: ${it.message}") } }
    }

    suspend fun rescheduleNow() {
        Channels.create(ctx)
        val settings = KoinPlatform.getKoin().get<SettingsRepo>().get()
        val plan = AlarmPlanner.plan(settings, days = 2)
        val prefs = ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        // cancel what we scheduled last time
        prefs.getStringSet("ids", emptySet())!!.mapNotNull { it.toIntOrNull() }.forEach { id -> am.cancel(pending(id, null)) }
        val exact = canScheduleExact()
        for (a in plan) {
            val pi = pending(a.id, a)
            val t = a.at.toEpochMilliseconds()
            when {
                exact && a.kind == "adhan" -> am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, t, pi)
                exact -> am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, t, pi)
                else -> am.setWindow(AlarmManager.RTC_WAKEUP, t, 60_000L, pi)
            }
        }
        prefs.edit().putStringSet("ids", plan.map { it.id.toString() }.toSet()).apply()
    }

    private fun pending(id: Int, a: PlannedAlarm?): PendingIntent {
        val i = Intent(ctx, AdhanAlarmReceiver::class.java).setAction("dj.alarm.$id")
        if (a != null) i.putExtra("id", a.id).putExtra("kind", a.kind).putExtra("title", a.title).putExtra("body", a.body).putExtra("sound", a.sound)
        return PendingIntent.getBroadcast(ctx, id, i, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
    }

    actual fun notify(id: Int, title: String, body: String, channel: String) = post(ctx, id, title, body, channel, "reminder")

    fun post(c: Context, id: Int, title: String, body: String, channel: String, kind: String) {
        Channels.create(c)
        if (Build.VERSION.SDK_INT >= 33 && ContextCompat.checkSelfPermission(c, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) return
        val open = PendingIntent.getActivity(c, id, Intent(c, MainActivity::class.java).putExtra("open", if (kind == "adhan" || kind == "pre") "prayer" else kind)
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP), PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)
        val n = NotificationCompat.Builder(c, channel)
            .setSmallIcon(R.drawable.ic_stat_dj)
            .setColor(0xFF17533C.toInt())
            .setContentTitle(title).setContentText(body).setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setCategory(if (kind == "adhan") NotificationCompat.CATEGORY_ALARM else NotificationCompat.CATEGORY_REMINDER)
            .setPriority(if (kind == "adhan") NotificationCompat.PRIORITY_HIGH else NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true).setContentIntent(open)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .build()
        runCatching { NotificationManagerCompat.from(c).notify(id, n) }
    }
}

/** Fires at prayer / reminder time, posts the notification, records it in the inbox and plans the next alarms. */
class AdhanAlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val id = intent.getIntExtra("id", 0)
        val kind = intent.getStringExtra("kind") ?: "adhan"
        val title = intent.getStringExtra("title") ?: return
        val body = intent.getStringExtra("body") ?: ""
        val sound = intent.getStringExtra("sound") ?: "chime"
        val channel = if (kind == "adhan") Channels.forSound(sound) else Channels.REMINDERS
        Scheduler.post(context, id, title, body, channel, kind)
        val done = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            runCatching {
                KoinPlatform.getKoin().get<com.deenjourney.app.data.user.UserRepo>().postInbox(if (kind == "adhan") "prayer" else kind, title, body, link = if (kind == "adhan") "E01" else null)
                Scheduler.rescheduleNow()
            }
            done.finish()
        }
    }
}

/** Boot / time-zone / clock / exact-alarm permission changes → re-plan. */
class RescheduleReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val done = goAsync()
        CoroutineScope(Dispatchers.IO).launch { runCatching { Scheduler.rescheduleNow() }; done.finish() }
    }
}

/** Plays a long adhan recording as a foreground service with a Stop action (used when a full adhan sound is installed). */
class AdhanPlaybackService : Service() {
    private var mp: MediaPlayer? = null
    override fun onBind(intent: Intent?): IBinder? = null
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == "stop") { stopSelf(); return START_NOT_STICKY }
        val res = intent?.getIntExtra("res", 0) ?: 0
        val stop = PendingIntent.getService(this, 1, Intent(this, AdhanPlaybackService::class.java).setAction("stop"), PendingIntent.FLAG_IMMUTABLE)
        val n = NotificationCompat.Builder(this, Channels.ADHAN_SILENT).setSmallIcon(R.drawable.ic_stat_dj)
            .setContentTitle(intent?.getStringExtra("title") ?: "Adhan").setOngoing(true)
            .addAction(0, intent?.getStringExtra("stopLabel") ?: "Stop", stop).build()
        if (Build.VERSION.SDK_INT >= 29) startForeground(77, n, ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PLAYBACK) else startForeground(77, n)
        runCatching {
            mp?.release()
            mp = MediaPlayer.create(this, res)?.apply { setOnCompletionListener { stopSelf() }; start() } ?: run { stopSelf(); null }
        }
        return START_NOT_STICKY
    }
    override fun onDestroy() { mp?.release(); mp = null; super.onDestroy() }
}
