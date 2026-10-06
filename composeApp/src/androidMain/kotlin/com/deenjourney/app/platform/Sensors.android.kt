package com.deenjourney.app.platform

import android.annotation.SuppressLint
import android.content.Context
import android.hardware.GeomagneticField
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Build
import android.os.CancellationSignal
import android.os.Looper
import android.view.Surface
import android.view.WindowManager
import com.deenjourney.app.core.AndroidPlatform
import com.deenjourney.app.core.Perm
import com.deenjourney.app.core.hasPermission
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withTimeoutOrNull
import kotlin.coroutines.resume

actual class LocationService actual constructor() {
    private val lm get() = AndroidPlatform.context.getSystemService(Context.LOCATION_SERVICE) as LocationManager

    actual fun servicesEnabled(): Boolean = runCatching { lm.isProviderEnabled(LocationManager.GPS_PROVIDER) || lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER) }.getOrDefault(false)

    @SuppressLint("MissingPermission")
    actual suspend fun current(timeoutMs: Long): GeoFix? {
        if (!hasPermission(Perm.Location)) return null
        val providers = listOf(LocationManager.NETWORK_PROVIDER, LocationManager.GPS_PROVIDER, LocationManager.PASSIVE_PROVIDER).filter { runCatching { lm.isProviderEnabled(it) }.getOrDefault(false) }
        // a recent last-known fix is good enough for prayer times (city-level accuracy)
        val recent = providers.mapNotNull { runCatching { lm.getLastKnownLocation(it) }.getOrNull() }
            .filter { System.currentTimeMillis() - it.time < 30 * 60_000 }.minByOrNull { it.accuracy }
        if (recent != null) return recent.toFix()
        val provider = providers.firstOrNull { it != LocationManager.PASSIVE_PROVIDER } ?: return null
        return withTimeoutOrNull(timeoutMs) {
            suspendCancellableCoroutine { cont ->
                if (Build.VERSION.SDK_INT >= 30) {
                    val cancel = CancellationSignal()
                    lm.getCurrentLocation(provider, cancel, AndroidPlatform.context.mainExecutor) { loc -> if (cont.isActive) cont.resume(loc?.toFix()) }
                    cont.invokeOnCancellation { cancel.cancel() }
                } else {
                    val l = object : LocationListener {
                        override fun onLocationChanged(location: Location) { lm.removeUpdates(this); if (cont.isActive) cont.resume(location.toFix()) }
                    }
                    @Suppress("DEPRECATION") lm.requestSingleUpdate(provider, l, Looper.getMainLooper())
                    cont.invokeOnCancellation { lm.removeUpdates(l) }
                }
            }
        } ?: providers.mapNotNull { runCatching { lm.getLastKnownLocation(it) }.getOrNull() }.maxByOrNull { it.time }?.toFix()
    }

    private fun Location.toFix() = GeoFix(latitude, longitude, accuracy)
}

actual class CompassService actual constructor() {
    private val sm get() = AndroidPlatform.context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    actual val available: Boolean get() = sm.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR) != null || (sm.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD) != null && sm.getDefaultSensor(Sensor.TYPE_ACCELEROMETER) != null)

    actual fun headings(lat: Double, lng: Double): Flow<Heading> = callbackFlow {
        val declination = GeomagneticField(lat.toFloat(), lng.toFloat(), 0f, System.currentTimeMillis()).declination
        val rot = FloatArray(9); val remapped = FloatArray(9); val orient = FloatArray(3)
        val grav = FloatArray(3); val geo = FloatArray(3); var haveG = false; var haveM = false
        var accuracy = 3
        var smooth = Float.NaN
        fun emit() {
            val wm = AndroidPlatform.context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
            @Suppress("DEPRECATION") val r = wm.defaultDisplay.rotation
            val (ax, ay) = when (r) {
                Surface.ROTATION_90 -> SensorManager.AXIS_Y to SensorManager.AXIS_MINUS_X
                Surface.ROTATION_180 -> SensorManager.AXIS_MINUS_X to SensorManager.AXIS_MINUS_Y
                Surface.ROTATION_270 -> SensorManager.AXIS_MINUS_Y to SensorManager.AXIS_X
                else -> SensorManager.AXIS_X to SensorManager.AXIS_Y
            }
            SensorManager.remapCoordinateSystem(rot, ax, ay, remapped)
            SensorManager.getOrientation(remapped, orient)
            var az = (Math.toDegrees(orient[0].toDouble()).toFloat() + declination + 360f) % 360f
            // low-pass filter on the circle
            smooth = if (smooth.isNaN()) az else {
                var d = az - smooth; if (d > 180) d -= 360; if (d < -180) d += 360
                (smooth + d * 0.18f + 360f) % 360f
            }
            trySend(Heading(smooth, accuracy))
        }
        val listener = object : SensorEventListener {
            override fun onSensorChanged(e: SensorEvent) {
                when (e.sensor.type) {
                    Sensor.TYPE_ROTATION_VECTOR -> { SensorManager.getRotationMatrixFromVector(rot, e.values); emit() }
                    Sensor.TYPE_ACCELEROMETER -> { System.arraycopy(e.values, 0, grav, 0, 3); haveG = true }
                    Sensor.TYPE_MAGNETIC_FIELD -> {
                        System.arraycopy(e.values, 0, geo, 0, 3); haveM = true
                        if (haveG && SensorManager.getRotationMatrix(rot, null, grav, geo)) emit()
                    }
                }
            }
            override fun onAccuracyChanged(sensor: Sensor, acc: Int) { if (sensor.type == Sensor.TYPE_MAGNETIC_FIELD || sensor.type == Sensor.TYPE_ROTATION_VECTOR) accuracy = acc }
        }
        val rv = sm.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)
        val mag = sm.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)
        if (rv != null) {
            sm.registerListener(listener, rv, SensorManager.SENSOR_DELAY_UI)
            if (mag != null) sm.registerListener(object : SensorEventListener {
                override fun onSensorChanged(e: SensorEvent) {}
                override fun onAccuracyChanged(sensor: Sensor, acc: Int) { accuracy = acc }
            }.also { m -> awaitCloseExtra.add { sm.unregisterListener(m) } }, mag, SensorManager.SENSOR_DELAY_UI)
        } else {
            sm.registerListener(listener, sm.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), SensorManager.SENSOR_DELAY_UI)
            sm.registerListener(listener, mag, SensorManager.SENSOR_DELAY_UI)
        }
        awaitClose { sm.unregisterListener(listener); awaitCloseExtra.forEach { it() }; awaitCloseExtra.clear() }
    }

    private val awaitCloseExtra = ArrayList<() -> Unit>()
}
