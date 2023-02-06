package com.example.currentgpslocation

import android.Manifest
import android.content.ContentValues
import android.content.Context
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.util.Log
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationToken
import com.google.android.gms.tasks.CancellationTokenSource
import com.google.android.gms.tasks.OnTokenCanceledListener
import java.io.IOException
import java.util.*

object Location {
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    var let: Double = 0.0000
    var long: Double = 0.0000
    var geocoderMaxResults = 1

    fun location_fun(context: Context) {
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
        if (ActivityCompat.checkSelfPermission(context,
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED
        ) {

            return
        }
        fusedLocationClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY,
            object : CancellationToken() {
                override fun onCanceledRequested(p0: OnTokenCanceledListener) =
                    CancellationTokenSource().token

                override fun isCancellationRequested() = false
            })
            .addOnSuccessListener { location: Location? ->
                if (location != null)
                    let = location.latitude
                if (location != null) {
                   long = location.longitude
                }
            }

    }
    fun getAddressLine(context: Context?): String? {

        val addresses = getGeocoderAddress(context)
        return if (addresses != null && addresses.isNotEmpty()) {
            val address = addresses[0]
            address?.getAddressLine(0)
        } else {
            val address = addresses?.get(0)
            address?.getAddressLine(0)
        }
    }
    private fun getGeocoderAddress(context: Context?): List<Address?>? {
        val geocoder = Geocoder(context, Locale.ENGLISH)
        try {
            return geocoder.getFromLocation(let, long, geocoderMaxResults)
        } catch (e: IOException) {
            //e.printStackTrace();
            Log.e(ContentValues.TAG, "Impossible to connect to Geocoder", e)
        }
        return null
    }
}