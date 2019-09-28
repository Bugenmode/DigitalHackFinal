package com.alexey.digitalhackfinal.ui.fragment.main

import android.content.Context
import android.graphics.drawable.BitmapDrawable
import android.net.ConnectivityManager
import android.os.Build
import android.os.Bundle
import android.telephony.TelephonyManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProviders
import com.alexey.digitalhackfinal.R
import com.alexey.digitalhackfinal.databinding.FragmentMainBinding
import com.alexey.digitalhackfinal.di.injector
import com.alexey.digitalhackfinal.ui.base.BaseFragment
import com.here.android.mpa.common.*
import com.here.android.mpa.mapping.Map
import com.here.android.mpa.mapping.MapMarker
import com.here.android.mpa.mapping.MapPolyline
import com.here.android.mpa.mapping.SupportMapFragment
import timber.log.Timber
import java.lang.ref.WeakReference

class MainFragment : BaseFragment() {

    private lateinit var b: FragmentMainBinding

    private var mapFragment: SupportMapFragment? = null

    private var positioningManager: PositioningManager? = null

    private lateinit var map: Map

    val viewModel by lazy {
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
        initialize()
    }

    private fun initialize() {
        // Search for the map fragment to finish setup by calling init().
        mapFragment = childFragmentManager.findFragmentById(R.id.mapfragment) as SupportMapFragment?

        val testPoints = ArrayList<GeoCoordinate>()

        testPoints.add(GeoCoordinate(49.163, 49.106324, 10.0))
        testPoints.add(GeoCoordinate(59.163, 41.106324, 10.0))
        testPoints.add(GeoCoordinate(60.163, 59.106324, 10.0))

        val polyline = GeoPolyline(testPoints)

        //TODO NEED INFO
        val mapPolyline = MapPolyline(polyline)

        val myImage = Image()

        val image = requireActivity().getDrawable(R.drawable.arrow) as BitmapDrawable
        myImage.bitmap = image.bitmap

        val mapMarker = MapMarker(GeoCoordinate(59.163, 41.106324), myImage)

        mapFragment?.init { error ->
            if (error == OnEngineInitListener.Error.NONE) {
                map = mapFragment!!.map
                // Set the map center to the Vancouver region (no animation)
                map.setCenter(
                    GeoCoordinate(55.798551, 49.106324), Map.Animation.NONE)
                // Set the zoom level to the average between min and max
                map.zoomLevel = (map.maxZoomLevel + map.minZoomLevel) / 2

                map.addMapObject(mapMarker)
            } else {
                Timber.d("ERROR: Cannot initialize Map Fragment")
            }
        }

        val onPositionListener = object : PositioningManager.OnPositionChangedListener {
            override fun onPositionFixChanged(
                p0: PositioningManager.LocationMethod?,
                p1: PositioningManager.LocationStatus?
            ) {
            }

            override fun onPositionUpdated(
                p0: PositioningManager.LocationMethod?,
                position: GeoPosition?,
                p2: Boolean
            ) {
                map.setCenter(position?.coordinate, Map.Animation.NONE)
            }

        }

        positioningManager = PositioningManager.getInstance()

        PositioningManager.getInstance().addListener(WeakReference(onPositionListener))
    }

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
}