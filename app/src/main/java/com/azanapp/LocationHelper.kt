package com.azanapp

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Geocoder
import androidx.core.content.ContextCompat
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import kotlinx.coroutines.suspendCancellableCoroutine
import java.util.Locale
import kotlin.coroutines.resume

class LocationHelper(private val context: Context) {
    private val fusedClient = LocationServices.getFusedLocationProviderClient(context)

    suspend fun getCurrentLocation(): Pair<Double, Double>? {
        if (!hasLocationPermission()) return getSavedLocation()
        return suspendCancellableCoroutine { cont ->
            val cts = CancellationTokenSource()
            try {
                fusedClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, cts.token)
                    .addOnSuccessListener { loc ->
                        if (loc != null) { saveLocation(loc.latitude, loc.longitude); cont.resume(Pair(loc.latitude, loc.longitude)) }
                        else cont.resume(getSavedLocation())
                    }.addOnFailureListener { cont.resume(getSavedLocation()) }
                cont.invokeOnCancellation { cts.cancel() }
            } catch (e: SecurityException) { cont.resume(null) }
        }
    }

    fun getCityName(lat: Double, lon: Double): String {
        return try {
            val addresses = Geocoder(context, Locale("ar")).getFromLocation(lat, lon, 1)
            if (!addresses.isNullOrEmpty()) addresses[0].locality ?: addresses[0].adminArea ?: "موقعك الحالي"
            else "موقعك الحالي"
        } catch (e: Exception) { "موقعك الحالي" }
    }

    private fun saveLocation(lat: Double, lon: Double) {
        context.getSharedPreferences("azan_prefs", Context.MODE_PRIVATE).edit()
            .putFloat("lat", lat.toFloat()).putFloat("lon", lon.toFloat()).apply()
    }

    fun getSavedLocation(): Pair<Double, Double>? {
        val p = context.getSharedPreferences("azan_prefs", Context.MODE_PRIVATE)
        val lat = p.getFloat("lat", 0f).toDouble()
        val lon = p.getFloat("lon", 0f).toDouble()
        return if (lat != 0.0 && lon != 0.0) Pair(lat, lon) else null
    }

    fun hasLocationPermission() = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
}
