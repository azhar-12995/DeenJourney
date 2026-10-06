package com.deenjourney.app.core

import android.Manifest
import android.annotation.SuppressLint
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.Uri
import android.os.Build
import android.os.StatFs
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.provider.Settings
import android.telephony.TelephonyManager
import android.text.format.DateFormat
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asAndroidBitmap
import java.io.ByteArrayOutputStream
import androidx.core.content.ContextCompat
import java.io.File
import java.util.Locale
import java.util.TimeZone

@SuppressLint("StaticFieldLeak") // application context only
object AndroidPlatform {
    lateinit var context: Context
        private set

    fun init(app: Context) { context = app.applicationContext }
}

actual object Platform {
    private val ctx get() = AndroidPlatform.context
    actual val isIos: Boolean = false
    actual val appVersion: String get() = runCatching { ctx.packageManager.getPackageInfo(ctx.packageName, 0).versionName ?: "1.0" }.getOrDefault("1.0")

    actual fun filesDir(): String = ctx.filesDir.absolutePath
    actual fun cacheDir(): String = ctx.cacheDir.absolutePath
    actual fun fileExists(path: String): Boolean = File(path).exists()
    actual fun fileSize(path: String): Long = File(path).takeIf { it.exists() }?.length() ?: 0L
    actual fun writeFile(path: String, bytes: ByteArray) {
        val f = File(path)
        f.parentFile?.mkdirs()
        val tmp = File("$path.part")
        tmp.writeBytes(bytes)
        if (!tmp.renameTo(f)) { f.delete(); tmp.renameTo(f) }
    }
    actual fun readFile(path: String): ByteArray? = File(path).takeIf { it.exists() }?.readBytes()
    actual fun deleteFile(path: String) { File(path).deleteRecursively() }
    actual fun freeSpaceBytes(): Long = runCatching { StatFs(ctx.filesDir.absolutePath).availableBytes }.getOrDefault(0L)

    actual fun openUrl(url: String) {
        runCatching { ctx.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)) }
    }

    actual fun openEmail(to: String, subject: String, body: String) {
        val i = Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:")).apply {
            putExtra(Intent.EXTRA_EMAIL, arrayOf(to)); putExtra(Intent.EXTRA_SUBJECT, subject); putExtra(Intent.EXTRA_TEXT, body)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        runCatching { ctx.startActivity(i) }
    }

    actual fun shareText(text: String) {
        val send = Intent(Intent.ACTION_SEND).apply { type = "text/plain"; putExtra(Intent.EXTRA_TEXT, text) }
        ctx.startActivity(Intent.createChooser(send, null).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
    }

    actual fun shareFile(path: String, mime: String) {
        val uri = androidx.core.content.FileProvider.getUriForFile(ctx, "${ctx.packageName}.files", File(path))
        val send = Intent(Intent.ACTION_SEND).apply {
            type = mime
            putExtra(Intent.EXTRA_STREAM, uri)
            clipData = ClipData.newRawUri("Deen Journey export", uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        ctx.startActivity(Intent.createChooser(send, null).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
    }

    actual fun pngBytes(bitmap: ImageBitmap): ByteArray = ByteArrayOutputStream().use { output ->
        check(bitmap.asAndroidBitmap().compress(android.graphics.Bitmap.CompressFormat.PNG, 100, output))
        output.toByteArray()
    }

    actual fun copyToClipboard(text: String) {
        (ctx.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager).setPrimaryClip(ClipData.newPlainText("Deen Journey", text))
    }

    actual fun vibrate(strong: Boolean) {
        val v: Vibrator = if (Build.VERSION.SDK_INT >= 31) (ctx.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager).defaultVibrator
        else @Suppress("DEPRECATION") (ctx.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator)
        runCatching {
            if (Build.VERSION.SDK_INT >= 29) v.vibrate(VibrationEffect.createPredefined(if (strong) VibrationEffect.EFFECT_HEAVY_CLICK else VibrationEffect.EFFECT_TICK))
            else v.vibrate(VibrationEffect.createOneShot(if (strong) 60 else 15, VibrationEffect.DEFAULT_AMPLITUDE))
        }
    }

    actual fun openAppSettings() {
        runCatching {
            ctx.startActivity(Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.fromParts("package", ctx.packageName, null)).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
        }
    }

    actual fun deviceTimeZoneId(): String = TimeZone.getDefault().id
    actual fun is24Hour(): Boolean = DateFormat.is24HourFormat(ctx)

    actual fun countryCode(): String? {
        val tm = ctx.getSystemService(Context.TELEPHONY_SERVICE) as? TelephonyManager
        return listOfNotNull(tm?.simCountryIso, tm?.networkCountryIso, Locale.getDefault().country)
            .map { it.uppercase() }.firstOrNull { it.length == 2 }
    }

    actual fun deviceLanguage(): String = Locale.getDefault().language

    actual fun isOnline(): Boolean {
        val cm = ctx.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val caps = cm.getNetworkCapabilities(cm.activeNetwork) ?: return false
        return caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }
}

private fun Perm.manifest(): Array<String> = when (this) {
    Perm.Location -> arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION)
    Perm.Notifications -> if (Build.VERSION.SDK_INT >= 33) arrayOf(Manifest.permission.POST_NOTIFICATIONS) else emptyArray()
}

actual fun hasPermission(perm: Perm): Boolean {
    val names = perm.manifest()
    if (names.isEmpty()) return true
    return names.any { ContextCompat.checkSelfPermission(AndroidPlatform.context, it) == PackageManager.PERMISSION_GRANTED }
}

@Composable
actual fun rememberPermission(perm: Perm, onResult: (Boolean) -> Unit): () -> Unit {
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { res -> onResult(res.values.any { it }) }
    return {
        val names = perm.manifest()
        if (names.isEmpty() || hasPermission(perm)) onResult(true) else launcher.launch(names)
    }
}
