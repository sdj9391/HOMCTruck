package com.homc.homctruck.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.google.firebase.auth.FirebaseAuth
import com.homc.homctruck.utils.DebugLog
import com.homc.homctruck.utils.account.BaseAccountManager
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope


class UpdateFirebaseToken(context: Context, params: WorkerParameters) :
    CoroutineWorker(context, params) {

    override suspend fun doWork(): Result = coroutineScope {
        val jobs = async {
            val isMobileVerified = BaseAccountManager(applicationContext).isMobileVerified ?: false
            if (isMobileVerified) {
                return@async Result.failure()
            }

            val firebaseUser = FirebaseAuth.getInstance().currentUser
            if (firebaseUser == null) {
                return@async Result.retry()
            } else {
                val tokenTask = firebaseUser.getIdToken(true)
                if (tokenTask.isSuccessful) {
                    val idToken: String? = tokenTask.result.token
                    BaseAccountManager(applicationContext).userAuthToken = idToken
                    return@async Result.success()
                } else {
                    DebugLog.e("getFirebaseAuthIdToken task fail")
                    return@async Result.failure()
                }
            }
        }

        return@coroutineScope jobs.await()
    }
}