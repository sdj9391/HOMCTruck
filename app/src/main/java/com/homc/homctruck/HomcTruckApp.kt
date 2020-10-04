package com.homc.homctruck

import android.content.Context
import android.content.pm.PackageManager
import androidx.multidex.MultiDex
import androidx.multidex.MultiDexApplication
import androidx.work.Constraints
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequest
import androidx.work.WorkManager
import com.crashlytics.android.Crashlytics
import com.google.android.libraries.places.api.Places
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.auth.FirebaseAuth
import com.homc.homctruck.data.models.AppConfig
import com.homc.homctruck.di.AppComponent
import com.homc.homctruck.di.DaggerAppComponent
import com.homc.homctruck.di.modules.AppModule
import com.homc.homctruck.utils.DebugLog
import com.homc.homctruck.utils.account.BaseAccountManager
import com.homc.homctruck.worker.UpdateFirebaseToken
import io.fabric.sdk.android.Fabric
import java.util.*
import java.util.concurrent.TimeUnit


class HomcTruckApp : MultiDexApplication() {
    lateinit var appComponent: AppComponent

    override fun onCreate() {
        super.onCreate()
        initAppConfig()
        initAppComponent()
        initFabric()
        if (!BuildConfig.DEBUG) {
            initFirebaseAnalytics()
        }
        //}
        // setUpChatSDK(getApplicationContext());
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
            val applicationInfo = packageManager.getApplicationInfo(packageName, PackageManager.GET_META_DATA)
            val value = applicationInfo.metaData["com.google.android.geo.API_KEY"]
            if (value is String) {
                key = value
            }
        } catch (e: PackageManager.NameNotFoundException) {
            e.printStackTrace()
        }
        return key
    }

    fun initAppConfig() {
        AppConfig.serverUrl = BuildConfig.SERVER_URL
        val baseAccountManager = BaseAccountManager(baseContext)
        val isMobileVerified = baseAccountManager.isMobileVerified ?: false
        if (isMobileVerified) {
            val user = FirebaseAuth.getInstance().currentUser
            user?.getIdToken(true)?.addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val firebaseToken = task.result.token
                    if (firebaseToken.isNullOrBlank()) {
                        DebugLog.w("Setting token null.")
                        AppConfig.token = null
                    } else {
                        DebugLog.w("Setting token firebaseToken")
                        baseAccountManager.userAuthToken = firebaseToken
                        AppConfig.token = firebaseToken
                        // DebugLog.e("it ---> $firebaseToken")
                    }
                } else {
                    DebugLog.w("Setting token null.")
                    AppConfig.token = null
                }
            }
        }
        startWorkerForAuthTokenUpdate()
    }

    private fun startWorkerForAuthTokenUpdate() {
        val isMobileVerified = BaseAccountManager(baseContext).isMobileVerified ?: false
        if (!isMobileVerified) {
            return
        }

        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .setRequiresBatteryNotLow(true)
            .build()

        val dataSyncWorker = PeriodicWorkRequest.Builder(
            UpdateFirebaseToken::class.java, 1, TimeUnit.HOURS
        )
            .setConstraints(constraints)
            .build()

        WorkManager.getInstance(baseContext).enqueue(dataSyncWorker)
    }

    private fun initAppComponent() {
        appComponent = DaggerAppComponent.builder().appModule(AppModule(this)).build()
    }

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