package com.homc.homctruck

import android.content.Context
import android.content.pm.PackageManager
import androidx.multidex.MultiDex
import androidx.multidex.MultiDexApplication
import androidx.work.Constraints
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequest
import androidx.work.WorkManager
import com.google.android.libraries.places.api.Places
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.auth.FirebaseAuth
import com.homc.homctruck.utils.DebugLog
import com.homc.homctruck.utils.account.BaseAccountManager
import com.homc.homctruck.worker.UpdateFirebaseAuthTokenWorker
import java.util.*
import java.util.concurrent.TimeUnit

class HomcTruckApp : MultiDexApplication() {
    override fun onCreate() {
        super.onCreate()
        if (!BuildConfig.DEBUG) {
            initFirebaseAnalytics()
        }
        initPlacesSDK()
    }

    /**
     * This is the new Places api to resolve the problem of City search
     */
    private fun initPlacesSDK() {
        val key: String? = getLocationKeyFromManifest()
        if (!Places.isInitialized() && !key.isNullOrBlank()) {
            Places.initialize(applicationContext, key, Locale.US)
        }
    }

    private fun getLocationKeyFromManifest(): String? {
        var key: String? = null
        try {
            val applicationInfo =
                packageManager.getApplicationInfo(packageName, PackageManager.GET_META_DATA)
            val value = applicationInfo.metaData["com.google.android.geo.API_KEY"]
            if (value is String) {
                key = value
            }
        } catch (e: PackageManager.NameNotFoundException) {
            e.printStackTrace()
        }
        return key
    }

    private fun initFirebaseAnalytics() {
        val firebaseAnalytics = FirebaseAnalytics.getInstance(this)
        firebaseAnalytics.setAnalyticsCollectionEnabled(false)
        firebaseAnalytics.setSessionTimeoutDuration(TimeUnit.MINUTES.toMillis(3))
    }

    override fun attachBaseContext(base: Context?) {
        super.attachBaseContext(base)
        MultiDex.install(applicationContext)
    }
}