package com.company.hostaldekho.util

import android.annotation.SuppressLint
import android.content.Context
import android.location.Geocoder
import android.location.Location
import android.os.Looper
import com.google.android.gms.location.*
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

data class UserLocation(
    val latitude: Double,
    val longitude: Double,
    val accuracy: Float,
    val city: String? = null,
    val state: String? = null,
    val country: String? = null
)

@Singleton
class LocationManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val client: FusedLocationProviderClient = 
        LocationServices.getFusedLocationProviderClient(context)
    private val geocoder = Geocoder(context, Locale.getDefault())

    @SuppressLint("MissingPermission")
    fun getLocationUpdates(): Flow<UserLocation> = callbackFlow {
        val locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 10000)
            .setMinUpdateIntervalMillis(5000)
            .build()

        val callback = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                result.lastLocation?.let { location ->
                    val userLocation = getAddressFromLocation(location)
                    trySend(userLocation)
                }
            }
        }

        client.requestLocationUpdates(locationRequest, callback, Looper.getMainLooper())
        
        awaitClose {
            client.removeLocationUpdates(callback)
        }
    }

    private fun getAddressFromLocation(location: Location): UserLocation {
        return try {
            val addresses = geocoder.getFromLocation(location.latitude, location.longitude, 1)
            val address = addresses?.firstOrNull()
            UserLocation(
                latitude = location.latitude,
                longitude = location.longitude,
                accuracy = location.accuracy,
                city = address?.locality ?: address?.subAdminArea,
                state = address?.adminArea,
                country = address?.countryName
            )
        } catch (e: Exception) {
            UserLocation(
                latitude = location.latitude,
                longitude = location.longitude,
                accuracy = location.accuracy
            )
        }
    }
}
