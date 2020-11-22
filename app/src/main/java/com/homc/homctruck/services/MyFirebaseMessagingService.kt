package com.homc.homctruck.services

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.navigation.NavDeepLinkBuilder
import androidx.work.*
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.homc.homctruck.R
import com.homc.homctruck.utils.DebugLog
import com.homc.homctruck.utils.account.BaseAccountManager
import com.homc.homctruck.views.activities.MainDrawerActivity
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
        val notification = remoteMessage.notification
        if (notification == null) {
            DebugLog.d("Notification is null")
            return
        }

        if (remoteMessage.data.isNotEmpty()) {
            DebugLog.d("Reference data is null")
            return
        }

        val actionType = remoteMessage.data[ACTION_TYPE]
        if (actionType.isNullOrBlank()) {
            DebugLog.d("Action type is null")
            return
        }

        val title = notification.title
        if (title.isNullOrBlank()) {
            DebugLog.d("Notification title is null")
            return
        }

        val body = notification.body
        if (body.isNullOrBlank()) {
            DebugLog.d("Notification body is null")
            return
        }

        handleNotification(actionType, title, body)
    }

    private fun handleNotification(actionType: String, title: String, body: String) {
        var channelId: String? = null
        var channelName: String? = null
        var channelInfo: String? = null
        var pendingIntent: PendingIntent? = null
        var notificationId: Int? = null
        when (actionType) {
            ACTION_TYPE_USER -> {
                channelId = ACTION_TYPE_USER
                channelName = "User Account"
                channelInfo = "This channel have user(your) account related notification"
                pendingIntent = NavDeepLinkBuilder(this)
                    .setComponentName(MainDrawerActivity::class.java) // your destination activity
                    .setGraph(R.navigation.drawer_nav_graph)
                    .setDestination(R.id.userProfileFragment)
                    .createPendingIntent()
                notificationId = NOTIFICATION_ID_USER_STATUS
            }
            ACTION_TYPE_TRUCK -> {
                channelId = ACTION_TYPE_TRUCK
                channelName = "Truck Management"
                channelInfo = "This channel have user trucks related notification"
                pendingIntent = NavDeepLinkBuilder(this)
                    .setComponentName(MainDrawerActivity::class.java) // your destination activity
                    .setGraph(R.navigation.drawer_nav_graph)
                    .setDestination(R.id.userTruckListFragment)
                    .createPendingIntent()
                notificationId = NOTIFICATION_ID_TRUCK_STATUS
            }
            else -> DebugLog.e("Action type $actionType not handled")
        }

        if (channelId.isNullOrBlank() || channelName.isNullOrBlank() || channelInfo.isNullOrBlank()) {
            DebugLog.e("channelId or channelName or channelInfo are null")
            return
        }

        val notificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        createNotificationChannel(notificationManager, channelId, channelName, channelInfo)
        if (notificationId == null) {
            DebugLog.e("notificationId found null")
            return
        }
        showNotification(pendingIntent, channelId, title, body, notificationId)
    }

    private fun createNotificationChannel(
        notificationManager: NotificationManager, channelId: String,
        channelName: String, channelInfo: String
    ) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId, channelName,
                NotificationManager.IMPORTANCE_DEFAULT
            )

            channel.enableLights(true)
            channel.enableVibration(true)
            channel.description = channelInfo
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun showNotification(
        pendingIntent: PendingIntent?, channelId: String,
        title: String, body: String, notificationId: Int
    ) {

        val builder = NotificationCompat.Builder(this, channelId)
            .apply {
                setSmallIcon(R.drawable.ic_launcher_foreground)
                setContentIntent(pendingIntent)
                setContentTitle(title)
                setContentText(body)
                setAutoCancel(true)
                priority = NotificationCompat.PRIORITY_DEFAULT
            }

        with(NotificationManagerCompat.from(this)) {
            notify(notificationId, builder.build())
        }
    }

    companion object {
        const val EXTRA_FIREBASE_MESSAGING_TOKEN = "EXTRA_FIREBASE_MESSAGING_TOKEN"
        const val ACTION_TYPE = "actionType"
        const val ACTION_TYPE_USER = "user"
        const val ACTION_TYPE_TRUCK = "truck"
        const val NOTIFICATION_ID_USER_STATUS = 1
        const val NOTIFICATION_ID_TRUCK_STATUS = 2
    }
}

fun cancelNotification(context: Context, notificationId: Int) {
    val notificationManager =
        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    notificationManager.cancel(notificationId)
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