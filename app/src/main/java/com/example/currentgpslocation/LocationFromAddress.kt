package com.example.currentgpslocation

import android.app.Activity
import android.location.Address
import android.location.Geocoder
import android.widget.Toast
import com.mapbox.mapboxsdk.geometry.LatLng
import java.io.IOException

fun Activity.getLocationFromAddress(strAddress: String?): LatLng? {
    val coder = Geocoder(this)
    val address: List<Address>?
    val p1: LatLng?
    try {
        address = coder.getFromLocationName(strAddress, 5)
        if (address == null) {
            Toast.makeText(this, "Address is null", Toast.LENGTH_SHORT).show()
            return null
        }

        val location: Address = address[0]
        location.latitude
        location.longitude
        p1 = LatLng((location.latitude), (location.longitude))
        return p1
    } catch (e: IOException) {
        e.printStackTrace()
    }
    return null

}


