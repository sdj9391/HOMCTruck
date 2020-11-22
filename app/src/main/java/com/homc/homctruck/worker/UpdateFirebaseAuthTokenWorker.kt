package com.homc.homctruck.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.google.firebase.auth.FirebaseAuth
import com.homc.homctruck.utils.DebugLog
import com.homc.homctruck.utils.account.BaseAccountManager
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.tasks.await

class UpdateFirebaseAuthTokenWorker(val context: Context, params: WorkerParameters) :
    CoroutineWorker(context, params) {

    override suspend fun doWork(): Result = coroutineScope {
        val isMobileVerified = BaseAccountManager(context).isMobileVerified ?: false
        if (!isMobileVerified) {
            return@coroutineScope Result.failure()
        }

        val firebaseUser = FirebaseAuth.getInstance().currentUser
        if (firebaseUser == null) {
            DebugLog.e("Result.retry")
            return@coroutineScope Result.retry()
        } else {
            try {
                val tokenTask = firebaseUser.getIdToken(true).await()
                val idToken = tokenTask.token
                if (idToken.isNullOrBlank()) {
                    DebugLog.e("idToken is found null")
                    return@coroutineScope Result.failure()
                }

                BaseAccountManager(context).userAuthToken = idToken
                return@coroutineScope Result.success()
            } catch (e: Exception) {
                DebugLog.e("getFirebaseAuthIdToken task fail $e")
                e.printStackTrace()
                return@coroutineScope Result.failure()
            }
        }
    }
}