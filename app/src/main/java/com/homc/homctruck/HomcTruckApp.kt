package com.homc.homctruck

import android.content.Context
import androidx.multidex.MultiDex
import androidx.multidex.MultiDexApplication
import com.crashlytics.android.Crashlytics
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.auth.FirebaseAuth
import com.homc.homctruck.di.AppComponent
import com.homc.homctruck.di.DaggerAppComponent
import com.homc.homctruck.di.modules.AppModule
import com.homc.homctruck.utils.AppConfig
import com.homc.homctruck.utils.BaseAccountManager
import com.homc.homctruck.utils.DebugLog
import io.fabric.sdk.android.Fabric
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
    }

    fun initAppConfig() {
        AppConfig.serverUrl = BuildConfig.SERVER_URL
        val baseAccountManager = BaseAccountManager(baseContext)
        val authToken = baseAccountManager.userAuthToken
        if (authToken.isNullOrBlank()) {
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
                    }
                } else {
                    DebugLog.w("Setting token null.")
                    AppConfig.token = null
                }
            }
        } else {
            DebugLog.w("Setting token authToken")
            AppConfig.token = authToken
        }
    }

    fun initAppComponent() {
        appComponent = DaggerAppComponent
            .builder()
            .appModule(AppModule(this))
            .build()
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