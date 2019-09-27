package com.alexey.digitalhackfinal.ui.activity

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.net.ConnectivityManager
import android.os.Build
import android.os.Bundle
import android.telephony.TelephonyManager
import com.alexey.digitalhackfinal.R
import com.alexey.digitalhackfinal.ui.base.BaseActivity
import com.here.android.mpa.common.*
import com.here.android.mpa.mapping.*
import com.here.android.mpa.mapping.Map
import kotlinx.android.synthetic.main.activity_container.*
import timber.log.Timber
import java.lang.ref.WeakReference

class ContainerActivity : BaseActivity() {

//    private lateinit var mapFragment: SupportMapFragment
//
//    private var positioningManager: PositioningManager? = null
//
//    private lateinit var map : com.here.android.mpa.mapping.Map

    @SuppressLint("SetTextI18n", "MissingPermission")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_container)

//        val onPositionListener = object : PositioningManager.OnPositionChangedListener {
//            override fun onPositionFixChanged(
//                p0: PositioningManager.LocationMethod?,
//                p1: PositioningManager.LocationStatus?
//            ) {
//            }
//
//            override fun onPositionUpdated(p0: PositioningManager.LocationMethod?, position: GeoPosition?, p2: Boolean) {
//                map.setCenter(position?.coordinate, Map.Animation.NONE)
//            }
//
//        }

//        val telephonyManager = applicationContext.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
//        val operatorName = telephonyManager.networkOperatorName
//
//        val connectivityManager = applicationContext.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
//        val networkCapabilities = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
//            connectivityManager.getNetworkCapabilities(connectivityManager.activeNetwork)
//        } else {
//            TODO("VERSION.SDK_INT < M")
//        }
//
//        val downSpeed = networkCapabilities.linkDownstreamBandwidthKbps
//        val upSpeed = networkCapabilities.linkUpstreamBandwidthKbps
//
//        txtOperatorName.text = "$operatorName $downSpeed $upSpeed"

//        positioningManager = PositioningManager.getInstance()
//
//        PositioningManager.getInstance().addListener(WeakReference(onPositionListener))


        //initialize()
    }

//    override fun onResume() {
//        super.onResume()
//
//        if (positioningManager != null) {
//            positioningManager?.start(PositioningManager.LocationMethod.GPS_NETWORK)
//        }
//    }
//
//    private fun initialize() {
//        // Search for the map fragment to finish setup by calling init().
//        mapFragment = supportFragmentManager.findFragmentById(R.id.mapfragment) as SupportMapFragment
//
//        val testPoints = ArrayList<GeoCoordinate>()
//
//        testPoints.add(GeoCoordinate(49.163, 49.106324, 10.0))
//        testPoints.add(GeoCoordinate(59.163, 41.106324, 10.0))
//        testPoints.add(GeoCoordinate(60.163, 59.106324, 10.0))
//
//        val polyline = GeoPolyline(testPoints)
//
//        //TODO NEED INFO
//        val mapPolyline = MapPolyline(polyline)
//
//        val myImage = Image()
//
//        val image = getDrawable(R.drawable.arrow) as BitmapDrawable
//        myImage.bitmap = image.bitmap
//
//        val mapMarker = MapMarker(GeoCoordinate(59.163, 41.106324), myImage)
//
//        mapFragment.init { error ->
//            if (error == OnEngineInitListener.Error.NONE) {
//                map = mapFragment.map
//                // Set the map center to the Vancouver region (no animation)
//                map.setCenter(
//                    GeoCoordinate(55.798551, 49.106324), Map.Animation.LINEAR)
//                // Set the zoom level to the average between min and max
//                map.zoomLevel = (map.maxZoomLevel + map.minZoomLevel) / 2
//
//                map.addMapObject(mapMarker)
//            } else {
//                Timber.d("ERROR: Cannot initialize Map Fragment")
//            }
//        }
//    }
}