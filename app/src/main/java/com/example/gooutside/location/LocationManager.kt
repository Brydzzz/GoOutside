package com.example.gooutside.location

import android.content.Context
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.annotation.RequiresPermission
import com.google.android.gms.location.CurrentLocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume


@Singleton
class LocationManager @Inject constructor(
    @param:ApplicationContext private val context: Context,
) {
    private val fusedLocationClient = LocationServices
        .getFusedLocationProviderClient(context)

    private val geocoder = Geocoder(context)

    @RequiresPermission(anyOf = [android.Manifest.permission.ACCESS_COARSE_LOCATION, android.Manifest.permission.ACCESS_FINE_LOCATION])
    suspend fun getCurrentLocation(): Location? {

        val hasGrantedFineLocationPermission = context.checkSelfPermission(
            android.Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        val priority = if (hasGrantedFineLocationPermission) {
            Priority.PRIORITY_HIGH_ACCURACY
        } else {
            Priority.PRIORITY_BALANCED_POWER_ACCURACY
        }

        val freshLocation = try {
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
                        continuation.resume(location)
                    }
                    .addOnFailureListener { e ->
                        Log.e("LocationManager", "Failed to get fresh location", e)
                        continuation.resume(null)
                    }

                continuation.invokeOnCancellation {
                    Log.e("LocationManager", "Location request was cancelled")
                    cancellationTokenSource.cancel()
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

    suspend fun reverseGeocode(location: Location): LocationDetails? {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            reverseGeocodeModern(location)
        } else {
            reverseGeocodeLegacy(location)
        }
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    suspend fun reverseGeocodeModern(location: Location): LocationDetails? =
        suspendCancellableCoroutine { continuation ->
            geocoder.getFromLocation(
                location.latitude, location.longitude, 1,
                object : Geocoder.GeocodeListener {
                    override fun onGeocode(addresses: MutableList<Address>) {
                        addresses.firstOrNull()?.let { address ->
                            val locationDetails = address.toLocationDetails()
                            Log.d("Geocoder", "Location details: $locationDetails")
                            continuation.resume(locationDetails)
                        } ?: continuation.resume(null)
                    }

                    override fun onError(errorMessage: String?) {
                        Log.e("Geocoder", "Error: $errorMessage")
                        continuation.resume(null)
                    }
                }
            )
        }

    @Suppress("DEPRECATION")
    suspend fun reverseGeocodeLegacy(location: Location): LocationDetails? {
        return try {
            val addresses = withContext(Dispatchers.Default) {
                geocoder.getFromLocation(location.latitude, location.longitude, 1)
            }
            addresses?.firstOrNull()?.toLocationDetails()
        } catch (e: Exception) {
            Log.e("Geocoder", "Error: ${e.message}")
            null
        }
    }

}

private fun Address.toLocationDetails() = LocationDetails(
    street = thoroughfare,
    streetNumber = subThoroughfare,
    city = locality,
    country = countryName
)

data class LocationDetails(
    val street: String? = null,
    val streetNumber: String? = null,
    val city: String? = null,
    val country: String? = null,
)