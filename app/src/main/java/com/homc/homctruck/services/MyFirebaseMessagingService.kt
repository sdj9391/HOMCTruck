package com.homc.homctruck.services

import android.content.Context
import androidx.work.*
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.google.gson.Gson
import com.homc.homctruck.utils.DebugLog
import com.homc.homctruck.utils.account.BaseAccountManager
import com.homc.homctruck.worker.UpdateFirebaseMessagingTokenWorker

/**
 * Created by Ritz on 29/8/18.
 */
open class MyFirebaseMessagingService : FirebaseMessagingService() {
    override fun onNewToken(token: String) {
        sendTokenToServer(token, baseContext)
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)
        DebugLog.e("Firebase Notification: ${Gson().toJson(remoteMessage)}")
    }

    companion object {
        const val EXTRA_FIREBASE_MESSAGING_TOKEN = "EXTRA_FIREBASE_MESSAGING_TOKEN"
    }
}

fun sendTokenToServer(token: String, context: Context) {
    val isMobileVerified = BaseAccountManager(context).isMobileVerified ?: false
    if (!isMobileVerified) {
        return
    }

    val constraints = Constraints.Builder()
        .setRequiredNetworkType(NetworkType.CONNECTED)
        .setRequiresBatteryNotLow(true)
        .build()

    val data = Data.Builder()
    data.putString(MyFirebaseMessagingService.EXTRA_FIREBASE_MESSAGING_TOKEN, token)

    val dataSyncWorker =
        OneTimeWorkRequest.Builder(UpdateFirebaseMessagingTokenWorker::class.java)
            .setInputData(data.build())
            .setConstraints(constraints).build()
    WorkManager.getInstance(context).enqueue(dataSyncWorker)
}