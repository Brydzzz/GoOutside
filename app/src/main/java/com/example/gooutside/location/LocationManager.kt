package com.example.gooutside.location

import android.content.Context
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import com.google.android.gms.location.CurrentLocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine


@Singleton
class LocationManager @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    private val fusedLocationClient = LocationServices
        .getFusedLocationProviderClient(context)

    private val geocoder = Geocoder(context)


    suspend fun getCurrentLocation(usePreciseLocation: Boolean = false): Location? {

        val hasGrantedFineLocationPermission = context.checkSelfPermission(
            android.Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        val hasGrantedCoarseLocationPermission = context.checkSelfPermission(
            android.Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        if (!hasGrantedFineLocationPermission && !hasGrantedCoarseLocationPermission) {
            Log.d("LocationManager", "No location permission")
            return null
        }

        val priority = if (usePreciseLocation) {
            Priority.PRIORITY_HIGH_ACCURACY
        } else {
            Priority.PRIORITY_BALANCED_POWER_ACCURACY
        }

        val freshLocation = try {
            withTimeout(15000L) {
                suspendCancellableCoroutine<Location?> { continuation ->
                    val cancellationTokenSource = CancellationTokenSource()

                    val request = CurrentLocationRequest.Builder()
                        .setPriority(priority)
                        .setDurationMillis(10000)
                        .setMaxUpdateAgeMillis(30000)
                        .build()

                    fusedLocationClient.getCurrentLocation(request, cancellationTokenSource.token)
                        .addOnSuccessListener { location ->
                            Log.d("LocationManager", "Got fresh location: $location")
                            continuation.resume(location) { cause, _, _ -> }
                        }
                        .addOnFailureListener { e ->
                            Log.e("LocationManager", "Failed to get fresh location", e)
                            continuation.resume(null) { cause, _, _ -> }
                        }

                    continuation.invokeOnCancellation {
                        cancellationTokenSource.cancel()
                    }
                }
            }
        } catch (e: Exception) {
            Log.e("LocationManager", "Error getting fresh location: ${e.message}")
            null
        }

        if (freshLocation != null) {
            return freshLocation
        }

        return try {
            fusedLocationClient.lastLocation.await()?.also {
                Log.d("LocationManager", "Got last known location: $it")
            }
        } catch (e: Exception) {
            Log.e("LocationManager", "Failed to get last location", e)
            null
        }
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    suspend fun reverseGeocode(location: Location): LocationDetails? =
        suspendCoroutine { continuation ->

            geocoder.getFromLocation(location.latitude, location.longitude, 1) { addresses ->
                // Handle successful geocoding
                addresses.firstOrNull()?.let { address ->
                    val street = address.thoroughfare
                    val city = address.locality
                    val country = address.countryName
                    val locationDetails = LocationDetails(street, city, country)
                    continuation.resume(locationDetails)
                } ?: continuation.resume(null)
            }
        }

    suspend fun reverseGeocodeLegacy(location: Location): LocationDetails? {
        return try {
            val addresses = withContext(Dispatchers.Default) {
                geocoder.getFromLocation(location.latitude, location.longitude, 1)
            }
            addresses?.firstOrNull()?.let { address ->
                val street = address.thoroughfare
                val city = address.locality
                val country = address.countryName
                val locationDetails = LocationDetails(street, city, country)
                locationDetails
            }
        } catch (e: Exception) {
            Log.e("Geocoder", "Error: ${e.message}")
            null
        }
    }

}

data class LocationDetails(
    val street: String? = null,
    val city: String? = null,
    val country: String? = null,
)