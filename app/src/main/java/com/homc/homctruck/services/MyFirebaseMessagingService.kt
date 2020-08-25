package com.homc.homctruck.services

import android.app.NotificationManager
import android.content.Context
import com.google.firebase.messaging.FirebaseMessagingService
import com.homc.homctruck.utils.DebugLog

/**
 * Created by Ritz on 29/8/18.
 */
open class MyFirebaseMessagingService : FirebaseMessagingService() {

    override fun onNewToken(token: String) {
        /*val deviceInfoUploader = DeviceInfoUploader(applicationContext)

        if (!BaseAccountManager.getInstance(this).isUserLoggedIn) {
            DebugLog.w("User not logged in. Return")
            return
        }

        val info = deviceInfoUploader.getInfo(token)
        deviceInfoUploader.uploadInfo(info)*/
    }
}