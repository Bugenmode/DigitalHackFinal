package com.alexey.digitalhackfinal.ui.activity

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.net.ConnectivityManager
import android.os.Build
import android.os.Bundle
import android.telephony.TelephonyManager
import androidx.navigation.NavController
import androidx.navigation.NavGraph
import androidx.navigation.Navigation
import com.alexey.digitalhackfinal.R
import com.alexey.digitalhackfinal.ui.base.BaseActivity
import com.here.android.mpa.common.*
import com.here.android.mpa.mapping.*
import com.here.android.mpa.mapping.Map
import kotlinx.android.synthetic.main.activity_container.*
import timber.log.Timber
import java.lang.ref.WeakReference

class ContainerActivity : BaseActivity() {

    lateinit var navController: NavController

    lateinit var graph : NavGraph

    @SuppressLint("SetTextI18n", "MissingPermission")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_container)

        navController = Navigation.findNavController(this, R.id.navHost)
        graph = navController.navInflater.inflate(R.navigation.navigation)
    }
}