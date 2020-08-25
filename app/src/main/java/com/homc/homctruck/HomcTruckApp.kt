package com.homc.homctruck

import android.content.Context
import androidx.multidex.MultiDex
import androidx.multidex.MultiDexApplication
import com.crashlytics.android.Crashlytics
import com.google.firebase.analytics.FirebaseAnalytics
import com.homc.homctruck.utils.AppConfig
import io.fabric.sdk.android.Fabric
import java.util.concurrent.TimeUnit

class HomcTruckApp : MultiDexApplication() {


    override fun onCreate() {
        super.onCreate()
        initAppConfig()
        // To enable Crashlytics debugger manually
        initFabric()

        if (!BuildConfig.DEBUG) {
            initFirebaseAnalytics()
        }
    }

    private fun initAppConfig() {
        AppConfig.serverUrl = BuildConfig.SERVER_URL
    }

    /**
     * To enable Crashlytics debugger manually
     */
    private fun initFabric() {
        val fabric = Fabric.Builder(this)
            .kits(Crashlytics())
            .debuggable(false)
            .build()
        Fabric.with(fabric)
        // setFirebaseUserId()
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