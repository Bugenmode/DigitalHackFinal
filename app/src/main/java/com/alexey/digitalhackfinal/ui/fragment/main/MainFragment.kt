package com.alexey.digitalhackfinal.ui.fragment.main

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
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
import com.alexey.digitalhackfinal.data.remote.model.Coordinates
import com.alexey.digitalhackfinal.data.remote.model.Location
import com.alexey.digitalhackfinal.data.remote.model.PointResponse
import com.alexey.digitalhackfinal.databinding.FragmentMainBinding
import com.alexey.digitalhackfinal.di.injector
import com.alexey.digitalhackfinal.ui.adapter.AddressAdapter
import com.alexey.digitalhackfinal.ui.base.BaseFragment
import com.alexey.digitalhackfinal.utils.EventData
import com.alexey.digitalhackfinal.utils.collapse
import com.alexey.digitalhackfinal.utils.expand
import com.here.android.mpa.common.*
import com.here.android.mpa.mapping.*
import com.here.android.mpa.mapping.Map
import com.here.android.mpa.routing.RouteManager
import com.here.android.mpa.routing.RouteOptions
import com.here.android.mpa.routing.RoutePlan
import com.here.android.mpa.routing.RouteResult
import com.here.android.mpa.search.*
import timber.log.Timber
import java.lang.ref.WeakReference
import java.util.*
import kotlin.collections.ArrayList

@Suppress("DEPRECATION")
class MainFragment : BaseFragment(), PositioningManager.OnPositionChangedListener, Map.OnTransformListener,
    AddressAdapter.OnItemClickListener {

    private lateinit var b: FragmentMainBinding

    private var mapFragment: SupportMapFragment? = null

    private var positioningManager: PositioningManager? = null

    private lateinit var map: Map

    private var changed = false

    private var mapTransforming: Boolean = false

    private lateinit var mapRoute: MapRoute

    private var pendingUpdated: Runnable? = null

    private var adapter = AddressAdapter(this)

    private var coordinateA: GeoCoordinate? = null

    private var coordinateB: GeoCoordinate? = null

    private var coordinateList = ArrayList<Double>()

    private var list = ArrayList<Address>()

    private var isActivated: Boolean = false

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
        setObservers()

        viewModel.getPoints()
    }

    private fun expand() {
        expand(b.detailsLayout, 300, resources.getDimension(R.dimen.size_100).toInt())
        b.imgWayPoint.visibility = View.INVISIBLE
        b.etWayA.visibility = View.INVISIBLE
        b.etWayB.visibility = View.INVISIBLE
        b.btnSearch.visibility = View.INVISIBLE
        b.addresses.visibility = View.INVISIBLE
    }

    private fun collapse() {
        collapse(b.detailsLayout, 500, resources.getDimension(R.dimen.size_750).toInt())
        b.imgWayPoint.visibility = View.VISIBLE
        b.etWayA.visibility = View.VISIBLE
        b.etWayB.visibility = View.VISIBLE
        b.btnSearch.visibility = View.VISIBLE
        b.addresses.visibility = View.VISIBLE
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
                PositioningManager.LocationMethod.GPS_NETWORK
            )
        }
    }

    private fun setListener() {

        val routeManager = RouteManager()

        val routePlan = RoutePlan()

        val routeOptions = RouteOptions()

        routeOptions.transportMode = RouteOptions.TransportMode.CAR

        routeOptions.setHighwaysAllowed(false)

        routeOptions.routeType = RouteOptions.Type.FASTEST

        b.btnSearch.setOnClickListener {

            val geocodeRequest =
                GeocodeRequest(b.etWayB.text.toString()).setSearchArea(GeoCoordinate(55.798551, 49.106324), 1000)

            expand()

            geocodeRequest.execute { results, errorCode ->
                if (errorCode == ErrorCode.NONE) {
                    for (i in results!!) {
                        Timber.d(i.location.coordinate.toString())
                        coordinateB = i.location.coordinate

                        routePlan.addWaypoint(coordinateA)
                        routePlan.addWaypoint(coordinateB)

                        routeManager.calculateRoute(routePlan, object : RouteManager.Listener {

                            override fun onCalculateRouteFinished(
                                error: RouteManager.Error?,
                                routeResult: MutableList<RouteResult>?
                            ) {
                                if (error == RouteManager.Error.NONE) {
                                    if (routeResult?.get(0)?.route != null) {
                                        mapRoute = MapRoute(routeResult[0].route)

                                        mapRoute.isManeuverNumberVisible = true

                                        map.addMapObject(mapRoute)

                                        val gbb = routeResult[0].route.boundingBox
                                        map.zoomTo(gbb, Map.Animation.NONE, Map.MOVE_PRESERVE_ORIENTATION)

                                        isActivated = true
                                    } else {
                                        Toast.makeText(
                                            requireContext(),
                                            "Error:route results returned is not valid",
                                            Toast.LENGTH_LONG
                                        ).show()
                                    }
                                }
                            }

                            override fun onProgress(i: Int) {
                            }

                        })
                    }
                }
            }
        }

    }

    private fun setObservers() {
        viewModel.wayB.observe(viewLifecycleOwner, androidx.lifecycle.Observer {
            if (it != null && b.etWayB.isFocused) {
                val searchRequest = SearchRequest(it)
                searchRequest.setSearchCenter(map.center)
                searchRequest.execute(discoveryResultPage)
            }
        })

        viewModel.data.observe(viewLifecycleOwner, androidx.lifecycle.Observer {
            if (it != null) {
                for (i in it) {
                    val image = Image()

                    when {
                        i.signal == "4g" -> {
                            image.setImageResource(R.drawable.circle_blue)
                            val customMarker = MapMarker(GeoCoordinate(i.location.coordinates[0], i.location.coordinates[1]), image)
                            map.addMapObject(customMarker)
                        }
                        i.signal == "3g" -> {
                            image.setImageResource(R.drawable.circle_yellow)
                            val customMarker = MapMarker(GeoCoordinate(i.location.coordinates[0], i.location.coordinates[1]), image)
                            map.addMapObject(customMarker)
                        }
                        else -> {
                            image.setImageResource(R.drawable.circle_red)
                            val customMarker = MapMarker(GeoCoordinate(i.location.coordinates[0], i.location.coordinates[1]), image)
                            map.addMapObject(customMarker)
                        }
                    }
                }
            }
        })
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
                    Toast.makeText(requireContext(), "PositioningManager.start: failed, exiting", Toast.LENGTH_LONG)
                        .show()
                }

            } else {
                Timber.d("ERROR: Cannot initialize Map Fragment")
            }
        }
    }


    //NETWORK TYPE ----------------------------------------------------------------
    private fun getNetworkType(): String {
        val mTelephonyManager = requireContext().getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager

        return when (mTelephonyManager.networkType) {
            TelephonyManager.NETWORK_TYPE_GPRS -> "2g"
            TelephonyManager.NETWORK_TYPE_EDGE -> "2g"
            TelephonyManager.NETWORK_TYPE_CDMA -> "2g"
            TelephonyManager.NETWORK_TYPE_1xRTT -> "2g"
            TelephonyManager.NETWORK_TYPE_IDEN -> "2g"
            TelephonyManager.NETWORK_TYPE_HSDPA -> "3g"
            TelephonyManager.NETWORK_TYPE_HSUPA -> "3g"
            TelephonyManager.NETWORK_TYPE_HSPA -> "3g"
            TelephonyManager.NETWORK_TYPE_EVDO_B -> "3g"
            TelephonyManager.NETWORK_TYPE_EHRPD -> "3g"
            TelephonyManager.NETWORK_TYPE_HSPAP -> "3g"
            TelephonyManager.NETWORK_TYPE_LTE -> "4g"
            else -> "NotFound"
        }
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

        coordinateA = coordinate

        val reverseGeocodeRequest = ReverseGeocodeRequest(coordinate)

        reverseGeocodeRequest.execute { address, errorCode ->
            if (errorCode == ErrorCode.NONE) {
                b.etWayA.setText(address.text)
            }
        }

        sb.append("Type: ").append(String.format(Locale.US, "%s\n", locationMethod?.name))
        sb.append("Coordinate:")
            .append(String.format(Locale.US, "%.6f, %.6f\n", coordinate?.latitude, coordinate?.longitude))

        if (geoPosition?.heading != GeoPosition.UNKNOWN.toDouble()) {
            sb.append("Heading:").append(String.format(Locale.US, "%.2f\n", geoPosition?.heading))
        }

        if (geoPosition?.speed != GeoPosition.UNKNOWN.toDouble()) {
            sb.append("Speed:").append(String.format(Locale.US, "%.2fm/s\n", geoPosition?.speed))
        }

        sb.deleteCharAt(sb.length - 1)

        try {
            if (isActivated) {
                val image = Image()

                when {
                    getNetworkType() == "4g" -> image.setImageResource(R.drawable.circle_blue)
                    getNetworkType() == "3g" -> image.setImageResource(R.drawable.circle_yellow)
                    getNetworkType() == "2g" -> image.setImageResource(R.drawable.circle_red)
                    else -> {
                        //nothing
                    }
                }

                val telephonyManager = requireContext().getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
                val operatorName = telephonyManager.networkOperatorName


                if (coordinate != null) {
                    coordinateList.add(coordinate.latitude)
                    coordinateList.add(coordinate.longitude)
                    viewModel.postPoints(Location("Point", coordinateList), getNetworkType())
                }

                val customMarker = MapMarker(coordinate, image)
                map.addMapObject(customMarker)
            }
        } catch (ex: Exception) {
            Toast.makeText(requireContext(), "Some Error", Toast.LENGTH_LONG).show()
        }

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

                        val mapMarker = MapMarker()
                        mapMarker.coordinate = GeoCoordinate(placeLink.position)

                        val revGeoCode = ReverseGeocodeRequest(mapMarker.coordinate)
                        revGeoCode.execute { address, errorCode ->
                            if (address != null) {
                                list.add(address)

                                adapter.setAddressList(list)

                                b.addresses.adapter = adapter
                            }
                        }
                    }
                }
            }
        }


    override fun onItemClick(item: Address) {
        if (b.etWayA.isFocused) {
            b.etWayA.setText(item.text)
            viewModel.wayA.removeObservers(viewLifecycleOwner)
            adapter.setAddressList(arrayListOf())
        } else {
            b.etWayB.setText(item.text)
            viewModel.wayB.removeObservers(viewLifecycleOwner)
            adapter.setAddressList(arrayListOf())
        }
    }

    companion object {
        const val GEO_POSITION_CODE = 200
    }
}