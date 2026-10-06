package com.deenjourney.app.core

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.ImageBitmap

/** Small platform services used across the app (actuals in androidMain / iosMain). */
expect object Platform {
    val isIos: Boolean
    val appVersion: String
    /** Private, persistent app directory (no trailing slash). */
    fun filesDir(): String
    fun cacheDir(): String
    fun fileExists(path: String): Boolean
    fun fileSize(path: String): Long
    fun writeFile(path: String, bytes: ByteArray)
    fun readFile(path: String): ByteArray?
    fun deleteFile(path: String)
    fun freeSpaceBytes(): Long
    fun openUrl(url: String)
    fun openEmail(to: String, subject: String, body: String = "")
    fun shareText(text: String)
    fun shareFile(path: String, mime: String)
    fun pngBytes(bitmap: ImageBitmap): ByteArray
    fun copyToClipboard(text: String)
    fun vibrate(strong: Boolean = false)
    fun openAppSettings()
    fun deviceTimeZoneId(): String
    fun is24Hour(): Boolean
    /** Best-guess ISO country of the device (SIM/network, then locale). */
    fun countryCode(): String?
    fun deviceLanguage(): String
    fun isOnline(): Boolean
}

enum class Perm { Location, Notifications }

/** Returns a launcher that asks for [perm] (if not granted) and reports the result. */
@Composable
expect fun rememberPermission(perm: Perm, onResult: (Boolean) -> Unit): () -> Unit

expect fun hasPermission(perm: Perm): Boolean
