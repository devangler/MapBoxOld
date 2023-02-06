package com.example.currentgpslocation

import android.annotation.SuppressLint
import android.app.Activity
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Intent
import android.location.Address
import android.location.Geocoder
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.SystemClock
import android.text.TextUtils
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.currentgpslocation.databinding.ActivityExploreBinding
import com.mapbox.geojson.Point
import com.mapbox.mapboxsdk.Mapbox
import com.mapbox.mapboxsdk.annotations.MarkerOptions
import com.mapbox.mapboxsdk.camera.CameraPosition
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory
import com.mapbox.mapboxsdk.geometry.LatLng
import com.mapbox.mapboxsdk.maps.MapView
import com.mapbox.mapboxsdk.maps.MapboxMap
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback
import com.mapbox.mapboxsdk.maps.Style
import java.util.*

class ExploreActivity : AppCompatActivity(), OnMapReadyCallback {

    private var binding: ActivityExploreBinding? = null
    private var mapboxMap: MapboxMap? = null
    private var mapView: MapView? = null
    private var origin: Point? = null
    private var mLastClickTime: Long = 0
    private var destination: Point? = null
    private var address: String? = null
    private var check = true

    override fun onCreate(savedInstanceState: Bundle?) {
        Mapbox.getInstance(this, resources.getString(R.string.mapbox_access_token))
        super.onCreate(savedInstanceState)
        binding = ActivityExploreBinding.inflate(layoutInflater)
        binding?.myLocationMap?.onCreate(savedInstanceState)
        binding?.myLocationMap?.getMapAsync(this)
        origin = Point.fromLngLat(Location.let, Location.long)

        typeCasting()
        setContentView(binding?.root)
        supportActionBar?.hide()
        binding?.crass?.setOnClickListener {
            binding?.currentLocationTextView?.setText("")
        }

        Location.location_fun(this)
    }

    @SuppressLint("RestrictedApi")
    private fun typeCasting() {
        binding?.run {
            shareCurrentLocation.setOnClickListener {
                if (SystemClock.elapsedRealtime() - mLastClickTime < 1500) {
                    return@setOnClickListener
                } else {
                    mLastClickTime = SystemClock.elapsedRealtime()
                    if (binding?.currentLocationTextView?.text!!.isEmpty()) {
                        origin = Point.fromLngLat(Location.let, Location.long)

                    } else {
                        try {
                            hideKeyboard(this@ExploreActivity)
                            getLocationFromAddress(binding?.currentLocationTextView?.text.toString()).let {
                                destination = Point.fromLngLat(it!!.longitude, it.latitude)
                                getStringAddress(
                                    destination!!.latitude(),
                                    destination!!.longitude()
                                )

                                if (mapboxMap != null) {
                                    mapboxMap!!.clear()
                                }
                                mapboxMap!!.addMarker(
                                    MarkerOptions().position(
                                        LatLng(
                                            destination!!.latitude(),
                                            destination!!.longitude()
                                        )
                                    )
                                )
                                val position = CameraPosition.Builder()
                                    .zoom(15.0)
                                    .target(
                                        LatLng(
                                            destination!!.latitude(),
                                            destination!!.longitude()
                                        )
                                    )
                                    .build()
                                mapboxMap!!.animateCamera(
                                    CameraUpdateFactory.newCameraPosition(
                                        position
                                    ), 5000
                                )

                            }
                        } catch (e: Exception) {
                            Toast.makeText(
                                this@ExploreActivity,
                                "Invalid Address",
                                Toast.LENGTH_SHORT
                            ).show()

                        }
                    }
                }

            }
            copyCurrentLocation.setOnClickListener {
                if (!TextUtils.isEmpty(currentLocationTextView.text.toString())) {
                    val manager = getSystemService(CLIPBOARD_SERVICE) as ClipboardManager
                    val data = ClipData.newPlainText("", currentLocationTextView.text.toString())
                    manager.setPrimaryClip(data)
                    Toast.makeText(this@ExploreActivity, "copy to clipboard", Toast.LENGTH_SHORT)
                        .show()
                }
            }
            searchCurrentLocation.setOnClickListener {
                if (!TextUtils.isEmpty(currentLocationTextView.text.toString())) {
                    val strUri = "http://maps.google.com/maps?q=loc:" + Location.let
                        .toString() + "," + Location.long
                    val intent = Intent(Intent.ACTION_SEND)
                    intent.type = "text/plain"
                    intent.putExtra(Intent.EXTRA_TEXT, "My current location is \n$strUri")
                    startActivity(Intent.createChooser(intent, "Share via"))
                }
            }
            currentCurrentLocation.setOnClickListener {
                val position = CameraPosition.Builder()
                    .zoom(15.0)
                    .target(LatLng(Location.let, Location.long))
                    .build()
                mapboxMap?.animateCamera(CameraUpdateFactory.newCameraPosition(position), 5000)

            }
            mapTypeBtn.setOnClickListener {
                if (check) {
                    check = false
                    imgType.setImageResource(R.drawable.ic_map)
                    mapboxMap?.setStyle(Style.SATELLITE_STREETS)
                } else {
                    check = true
                    imgType.setImageResource(R.drawable.ic_satellite_view)
                    mapboxMap!!.setStyle(Style.MAPBOX_STREETS)

                }
            }
        }

    }

    override fun onMapReady(mapboxMap1: MapboxMap) {
        this.mapboxMap = mapboxMap1
        Handler(Looper.getMainLooper()).postDelayed({
            Location.location_fun(this@ExploreActivity)
            address()
            mapboxMap?.setStyle(Style.MAPBOX_STREETS) {

                mapboxMap?.uiSettings?.isRotateGesturesEnabled = false
                val position = CameraPosition.Builder()
                    .zoom(15.0)
                    .target(LatLng(Location.let, Location.long))
                    .build()
                mapboxMap?.animateCamera(CameraUpdateFactory.newCameraPosition(position), 5000)
            }

        }, 2000)


    }

    private fun address() {
        try {
            val addresses: List<Address>
            val geocoder = Geocoder(this, Locale.getDefault())
            addresses = geocoder.getFromLocation(Location.let, Location.long, 1)
            val address = addresses[0].getAddressLine(0)
            binding?.currentLocationTextView?.setText(address)

        } catch (ex: Exception) {
            Toast.makeText(this@ExploreActivity, "Location Not Found", Toast.LENGTH_SHORT).show()

        }
    }

    override fun onStart() {
        super.onStart()
        mapView?.onStart()
    }

    override fun onResume() {
        super.onResume()
        mapView?.onResume()
    }

    override fun onPause() {
        super.onPause()
        mapView?.onPause()
    }

    override fun onStop() {
        super.onStop()
        mapView?.onStop()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        mapView?.onSaveInstanceState(outState)
    }

    override fun onDestroy() {
        super.onDestroy()
        mapView?.onDestroy()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        mapView?.onLowMemory()
    }

    private fun getStringAddress(latitude: Double, longitude: Double) {
        try {
            val addresses: List<Address>
            val geocoder = Geocoder(this, Locale.getDefault())
            addresses = geocoder.getFromLocation(latitude, longitude, 1)
            address = addresses[0].getAddressLine(0)
            binding?.currentLocationTextView?.setText(address)
        } catch (_: Exception) {

        }

    }

    private fun hideKeyboard(activity: Activity) {
        val imm: InputMethodManager =
            activity.getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        var view: View? = activity.currentFocus
        if (view == null) {
            view = View(activity)
        }
        imm.hideSoftInputFromWindow(view.windowToken, 0)
    }
}