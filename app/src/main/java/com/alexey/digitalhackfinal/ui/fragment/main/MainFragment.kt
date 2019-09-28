package com.alexey.digitalhackfinal.ui.fragment.main

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import android.net.ConnectivityManager
import android.os.Build
import android.os.Bundle
import android.telephony.TelephonyManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProviders
import com.alexey.digitalhackfinal.R
import com.alexey.digitalhackfinal.databinding.FragmentMainBinding
import com.alexey.digitalhackfinal.di.injector
import com.alexey.digitalhackfinal.ui.base.BaseFragment
import com.alexey.digitalhackfinal.utils.collapse
import com.alexey.digitalhackfinal.utils.expand
import com.here.android.mpa.common.*
import com.here.android.mpa.mapping.*
import com.here.android.mpa.mapping.Map
import com.here.android.mpa.search.*
import timber.log.Timber
import java.lang.Exception
import java.lang.StringBuilder
import java.lang.ref.WeakReference
import java.util.*
import kotlin.collections.ArrayList

@Suppress("DEPRECATION")
class MainFragment : BaseFragment(), PositioningManager.OnPositionChangedListener, Map.OnTransformListener {

    private lateinit var b: FragmentMainBinding

    private var mapFragment: SupportMapFragment? = null

    private var positioningManager: PositioningManager? = null

    private lateinit var map: Map

    private var changed = false

    private var mapTransforming: Boolean = false

    private var pendingUpdated: Runnable? = null

    private var objectList = ArrayList<MapObject>()

    private val viewModel by lazy {
        ViewModelProviders.of(this, injector.vmMain()).get(MainViewModel::class.java)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        b = DataBindingUtil.inflate(inflater, R.layout.fragment_main, container, false)
        b.lifecycleOwner = viewLifecycleOwner
        b.vm = viewModel
        return b.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        b.detailsLayout.setOnClickListener {
            changed = if (changed) {
                expand()
                false
            } else {
                collapse()
                true
            }
        }

        if (hasPermission()) {
            initialize()
        } else {
            requestPermission()
        }

        setListener()
    }

    private fun expand() {
        expand(b.detailsLayout, 300, resources.getDimension(R.dimen.size_100).toInt())
        b.imgWayPoint.visibility = View.INVISIBLE
        b.etWayA.visibility = View.INVISIBLE
        b.etWayB.visibility = View.INVISIBLE
        b.btnSearch.visibility = View.INVISIBLE
    }

    private fun collapse() {
        collapse(b.detailsLayout, 500, resources.getDimension(R.dimen.size_900).toInt())
        b.imgWayPoint.visibility = View.VISIBLE
        b.etWayA.visibility = View.VISIBLE
        b.etWayB.visibility = View.VISIBLE
        b.btnSearch.visibility = View.VISIBLE
    }

    private fun requestPermission() {
        requestPermissions(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), GEO_POSITION_CODE)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        when (requestCode) {
            GEO_POSITION_CODE -> {
                if (grantResults.isNotEmpty() && grantResults.all { gr -> gr == PackageManager.PERMISSION_GRANTED }) {
                    initialize()
                } else {
                    Toast.makeText(requireContext(), "Включите доступ геолокации", Toast.LENGTH_LONG).show()
                }
                return
            }
        }
    }

    override fun onResume() {
        super.onResume()
        if (positioningManager != null) {
            positioningManager!!.start(
                PositioningManager.LocationMethod.GPS_NETWORK)
        }
    }

    private fun setListener() {

        b.btnSearch.setOnClickListener {
            val searchRequest = SearchRequest(b.etWayA.text.toString())
            searchRequest.setSearchCenter(map.center)
            searchRequest.execute(discoveryResultPage)

        }
    }

    override fun onPause() {
        super.onPause()
        if (positioningManager != null) {
            positioningManager!!.stop()
        }
    }


    // MAP INNIT -------------------------------------------------------------------------------------
    private fun initialize() {
        mapFragment = childFragmentManager.findFragmentById(R.id.mapfragment) as SupportMapFragment?

        val myImage = Image()
        val customImage = BitmapFactory.decodeResource(requireContext().resources, R.drawable.placeholder)
        myImage.bitmap = customImage as Bitmap

        mapFragment?.init { error ->
            if (error == OnEngineInitListener.Error.NONE) {
                map = mapFragment!!.map
                map.setCenter(
                    GeoCoordinate(55.798551, 49.106324), Map.Animation.NONE
                )

                map.addTransformListener(this@MainFragment)

                map.zoomLevel = (map.maxZoomLevel + map.minZoomLevel) / 2

                positioningManager = PositioningManager.getInstance()

                positioningManager?.addListener(WeakReference<PositioningManager.OnPositionChangedListener>(this))

                if (positioningManager!!.start(PositioningManager.LocationMethod.GPS_NETWORK)) {
                    map.positionIndicator.isVisible = true
                    map.positionIndicator.marker = myImage
                } else {
                    Toast.makeText(requireContext(), "PositioningManager.start: failed, exiting", Toast.LENGTH_LONG).show();
                }

            } else {
                Timber.d("ERROR: Cannot initialize Map Fragment")
            }
        }


        val testPoints = ArrayList<GeoCoordinate>()

        testPoints.add(GeoCoordinate(49.163, 49.106324, 10.0))
        testPoints.add(GeoCoordinate(59.163, 41.106324, 10.0))
        testPoints.add(GeoCoordinate(60.163, 59.106324, 10.0))

        val polyline = GeoPolyline(testPoints)

        //TODO NEED INFO
        val mapPolyline = MapPolyline(polyline)
    }


    //CONNECTION DATA ----------------------------------------------------------------
    private fun getConnectionData() {
        val telephonyManager = requireContext().getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
        val operatorName = telephonyManager.networkOperatorName

        val connectivityManager =
            requireContext().getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val networkCapabilities = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            connectivityManager.getNetworkCapabilities(connectivityManager.activeNetwork)
        } else {
            TODO("VERSION.SDK_INT < M")
        }

        val downSpeed = networkCapabilities.linkDownstreamBandwidthKbps
        val upSpeed = networkCapabilities.linkUpstreamBandwidthKbps
    }

    private fun hasPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            requireContext(), Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }

    override fun onPositionFixChanged(
        locationMethod: PositioningManager.LocationMethod?,
        locationStatus: PositioningManager.LocationStatus?
    ) {
    }

    override fun onPositionUpdated(
        locationMethod: PositioningManager.LocationMethod?,
        geoPosition: GeoPosition?,
        mapMatched: Boolean
    ) {
        val coordinate = geoPosition?.coordinate
        if (mapTransforming) {
            pendingUpdated = Runnable { onPositionUpdated(locationMethod, geoPosition, mapMatched); }
        } else {
            map.setCenter(coordinate, Map.Animation.LINEAR)
            updateLocationInfo(locationMethod, geoPosition)
        }
    }

    //LOCATION INFO -----------------------------------------------------------------------
    private fun updateLocationInfo(locationMethod: PositioningManager.LocationMethod?, geoPosition: GeoPosition?) {
        val sb = StringBuilder()
        val coordinate = geoPosition?.coordinate

        sb.append("Type: ").append(String.format(Locale.US, "%s\n", locationMethod?.name))
        sb.append("Coordinate:")
            .append(String.format(Locale.US, "%.6f, %.6f\n", coordinate?.latitude, coordinate?.longitude))

        if (coordinate?.altitude != GeoCoordinate.UNKNOWN_ALTITUDE.toDouble()) {
            sb.append("Altitude:").append(String.format(Locale.US, "%.2fm\n", coordinate?.altitude))
        }

        if (geoPosition?.heading != GeoPosition.UNKNOWN.toDouble()) {
            sb.append("Heading:").append(String.format(Locale.US, "%.2f\n", geoPosition?.heading))
        }

        if (geoPosition?.speed != GeoPosition.UNKNOWN.toDouble()) {
            sb.append("Speed:").append(String.format(Locale.US, "%.2fm/s\n", geoPosition?.speed))
        }

        sb.deleteCharAt(sb.length - 1)

        Timber.d(sb.toString())
    }

    override fun onMapTransformStart() {
        mapTransforming = true
    }

    override fun onMapTransformEnd(mapState: MapState?) {
        mapTransforming = false
        if (pendingUpdated != null) {
            pendingUpdated!!.run()
            pendingUpdated = null
        }
    }

    private val discoveryResultPage =
        ResultListener<DiscoveryResultPage> { result, errorCode ->
            if (errorCode == ErrorCode.NONE) {
                for (item in result.items) {
                    if (item.resultType == DiscoveryResult.ResultType.PLACE) {
                        val placeLink = item as PlaceLink
                        addMarkerAtPlace(placeLink)
                    }
                }
            }
        }


    private fun addMarkerAtPlace(placeLink: PlaceLink) {
        val img = Image()

        try {
            img.setImageResource(R.drawable.placeholder)
        } catch (e : Exception) {
            e.printStackTrace()
        }

        val mapMarker = MapMarker()
        mapMarker.icon = img
        mapMarker.coordinate = GeoCoordinate(placeLink.position)

        val revGeoCode = ReverseGeocodeRequest(mapMarker.coordinate)

        revGeoCode.execute(result)

//        map.addMapObject(mapMarker)
//        objectList.add(mapMarker)
    }

    //val result = ResultListener<Location> { location, errorCode -> Timber.d(location?.address.toString()) }

    val result = ResultListener<Address> { address, errorCode ->
        Toast.makeText(requireContext(), address?.text, Toast.LENGTH_LONG).show()
    }

    companion object {
        const val GEO_POSITION_CODE = 200
    }
}