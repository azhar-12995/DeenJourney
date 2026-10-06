package com.deenjourney.app.core

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asSkiaBitmap
import org.jetbrains.skia.EncodedImageFormat
import org.jetbrains.skia.Image
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.usePinned
import platform.AudioToolbox.AudioServicesPlaySystemSound
import platform.CoreLocation.CLLocationManager
import platform.CoreLocation.kCLAuthorizationStatusAuthorizedAlways
import platform.CoreLocation.kCLAuthorizationStatusAuthorizedWhenInUse
import platform.Foundation.NSBundle
import platform.Foundation.NSData
import platform.Foundation.NSDocumentDirectory
import platform.Foundation.NSFileManager
import platform.Foundation.NSFileSize
import platform.Foundation.NSFileSystemFreeSize
import platform.Foundation.NSLocale
import platform.Foundation.NSSearchPathForDirectoriesInDomains
import platform.Foundation.NSTimeZone
import platform.Foundation.NSURL
import platform.Foundation.NSUserDomainMask
import platform.Foundation.NSCachesDirectory
import platform.Foundation.countryCode
import platform.Foundation.create
import platform.Foundation.dataWithContentsOfFile
import platform.Foundation.languageCode
import platform.Foundation.localTimeZone
import platform.Foundation.currentLocale
import platform.Foundation.writeToFile
import platform.UIKit.UIActivityViewController
import platform.UIKit.UIApplication
import platform.UIKit.UIApplicationOpenSettingsURLString
import platform.UIKit.UIPasteboard
import platform.UserNotifications.UNAuthorizationOptionAlert
import platform.UserNotifications.UNAuthorizationOptionBadge
import platform.UserNotifications.UNAuthorizationOptionSound
import platform.UserNotifications.UNUserNotificationCenter
import platform.posix.memcpy

@OptIn(ExperimentalForeignApi::class)
actual object Platform {
    actual val isIos: Boolean = true
    actual val appVersion: String get() = (NSBundle.mainBundle.infoDictionary?.get("CFBundleShortVersionString") as? String) ?: "1.0"

    private fun dir(kind: ULong): String = (NSSearchPathForDirectoriesInDomains(kind, NSUserDomainMask, true).firstOrNull() as? String) ?: ""
    actual fun filesDir(): String = dir(NSDocumentDirectory)
    actual fun cacheDir(): String = dir(NSCachesDirectory)
    actual fun fileExists(path: String): Boolean = NSFileManager.defaultManager.fileExistsAtPath(path)
    actual fun fileSize(path: String): Long = (NSFileManager.defaultManager.attributesOfItemAtPath(path, null)?.get(NSFileSize) as? Long) ?: 0L

    actual fun writeFile(path: String, bytes: ByteArray) {
        val parent = path.substringBeforeLast('/')
        NSFileManager.defaultManager.createDirectoryAtPath(parent, true, null, null)
        val data = bytes.usePinned { NSData.create(bytes = it.addressOf(0), length = bytes.size.toULong()) }
        data.writeToFile(path, true)
    }

    actual fun readFile(path: String): ByteArray? {
        val data = NSData.dataWithContentsOfFile(path) ?: return null
        val out = ByteArray(data.length.toInt())
        if (out.isNotEmpty()) out.usePinned { memcpy(it.addressOf(0), data.bytes, data.length) }
        return out
    }

    actual fun deleteFile(path: String) { NSFileManager.defaultManager.removeItemAtPath(path, null) }
    actual fun freeSpaceBytes(): Long = (NSFileManager.defaultManager.attributesOfFileSystemForPath(filesDir(), null)?.get(NSFileSystemFreeSize) as? Long) ?: 0L

    actual fun openUrl(url: String) { NSURL.URLWithString(url)?.let { UIApplication.sharedApplication.openURL(it, emptyMap<Any?, Any>(), null) } }
    actual fun openEmail(to: String, subject: String, body: String) {
        val s = subject.replace(" ", "%20"); val b = body.replace(" ", "%20")
        openUrl("mailto:$to?subject=$s&body=$b")
    }

    actual fun shareText(text: String) {
        val vc = UIActivityViewController(listOf(text), null)
        UIApplication.sharedApplication.keyWindow?.rootViewController?.presentViewController(vc, true, null)
    }

    actual fun shareFile(path: String, mime: String) {
        val vc = UIActivityViewController(listOf(NSURL.fileURLWithPath(path)), null)
        UIApplication.sharedApplication.keyWindow?.rootViewController?.presentViewController(vc, true, null)
    }

    actual fun pngBytes(bitmap: ImageBitmap): ByteArray =
        Image.makeFromBitmap(bitmap.asSkiaBitmap()).use { image ->
            checkNotNull(image.encodeToData(EncodedImageFormat.PNG)).use { it.bytes }
        }

    actual fun copyToClipboard(text: String) { UIPasteboard.generalPasteboard.string = text }
    actual fun vibrate(strong: Boolean) { AudioServicesPlaySystemSound(if (strong) 1520u else 1519u) }
    actual fun openAppSettings() { openUrl(UIApplicationOpenSettingsURLString) }
    actual fun deviceTimeZoneId(): String = NSTimeZone.localTimeZone.name
    actual fun is24Hour(): Boolean = false
    actual fun countryCode(): String? = NSLocale.currentLocale.countryCode
    actual fun deviceLanguage(): String = NSLocale.currentLocale.languageCode
    actual fun isOnline(): Boolean = true
}

actual fun hasPermission(perm: Perm): Boolean = when (perm) {
    Perm.Location -> CLLocationManager.authorizationStatus().let { it == kCLAuthorizationStatusAuthorizedWhenInUse || it == kCLAuthorizationStatusAuthorizedAlways }
    Perm.Notifications -> true
}

private val locationManager by lazy { CLLocationManager() }

@Composable
actual fun rememberPermission(perm: Perm, onResult: (Boolean) -> Unit): () -> Unit = {
    when (perm) {
        Perm.Location -> { locationManager.requestWhenInUseAuthorization(); onResult(hasPermission(Perm.Location)) }
        Perm.Notifications -> UNUserNotificationCenter.currentNotificationCenter().requestAuthorizationWithOptions(
            UNAuthorizationOptionAlert or UNAuthorizationOptionSound or UNAuthorizationOptionBadge
        ) { granted, _ -> onResult(granted) }
    }
}
