package com.homc.homctruck.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.google.firebase.auth.FirebaseAuth
import com.homc.homctruck.restapi.AppApiInstance
import com.homc.homctruck.restapi.AppApiService
import com.homc.homctruck.restapi.DataBound
import com.homc.homctruck.services.MyFirebaseMessagingService
import com.homc.homctruck.utils.DebugLog
import com.homc.homctruck.utils.account.BaseAccountManager
import com.homc.homctruck.utils.getAuthToken
import com.homc.homctruck.utils.parse
import com.homc.homctruck.utils.parseApiMessage
import com.homc.homctruck.viewmodels.AuthenticationViewModel
import kotlinx.coroutines.coroutineScope
import java.net.HttpURLConnection


class UpdateFirebaseMessagingTokenWorker(val context: Context, params: WorkerParameters) :
    CoroutineWorker(context, params) {

    override suspend fun doWork(): Result = coroutineScope {
        val isMobileVerified = BaseAccountManager(context).isMobileVerified ?: false
        if (!isMobileVerified) {
            return@coroutineScope Result.failure()
        }

        val userDetails = BaseAccountManager(context).userDetails
            ?: return@coroutineScope Result.failure()

        val userId = userDetails.id ?: return@coroutineScope Result.failure()

        val firebaseMessagingToken = inputData.getString(MyFirebaseMessagingService.EXTRA_FIREBASE_MESSAGING_TOKEN)
        if (firebaseMessagingToken.isNullOrBlank()) {
            return@coroutineScope Result.failure()
        }

        userDetails.firebaseMessageToken = firebaseMessagingToken

        val response = AppApiInstance.api(getAuthToken(context))
            .updateUserDetail(userId, userDetails)
        val code = response.code()
        if (code == HttpURLConnection.HTTP_UNAUTHORIZED) {
            return@coroutineScope Result.retry()
        }
        if (!response.isSuccessful) {
            return@coroutineScope Result.failure()
        } else {
            BaseAccountManager(context).userDetails = userDetails
        }

        return@coroutineScope Result.success()
    }
}