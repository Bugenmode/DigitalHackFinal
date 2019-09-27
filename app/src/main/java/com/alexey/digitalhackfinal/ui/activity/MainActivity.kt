package com.alexey.digitalhackfinal.ui.activity

import android.annotation.SuppressLint
import android.content.Context
import android.net.ConnectivityManager
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.telephony.CellInfoGsm
import android.telephony.TelephonyManager
import com.alexey.digitalhackfinal.R
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    @SuppressLint("SetTextI18n", "MissingPermission")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val telephonyManager = applicationContext.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
        val operatorName = telephonyManager.networkOperatorName

        val connectivityManager = applicationContext.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val networkCapabilities = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            connectivityManager.getNetworkCapabilities(connectivityManager.activeNetwork)
        } else {
            TODO("VERSION.SDK_INT < M")
        }

        val cellInfoGsm = telephonyManager.allCellInfo[0] as CellInfoGsm
        val cellSignalStrengthGsm = cellInfoGsm.cellSignalStrength

        cellSignalStrengthGsm.dbm

        val downSpeed = networkCapabilities.linkDownstreamBandwidthKbps
        val upSpeed = networkCapabilities.linkUpstreamBandwidthKbps



        txtOperatorName.text = "$operatorName $downSpeed $upSpeed"
    }
}
