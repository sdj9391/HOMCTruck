package com.homc.homctruck.utils.account

import android.accounts.Account
import android.accounts.AccountManager
import android.app.Activity
import android.content.Context
import com.google.gson.Gson
import com.homc.homctruck.BuildConfig
import com.homc.homctruck.data.models.User
import com.homc.homctruck.utils.DebugLog

class BaseAccountManager(context: Context) {

    private val accountManager: AccountManager = AccountManager.get(context)
    private var account: Account?

    init {
        val accounts = accountManager.getAccountsByType(BuildConfig.ACCOUNT_TYPE)
        account = if (accounts.isNotEmpty()) {
            accounts[0]
        } else {
            null
        }
    }

    fun createAccount() {
        val newAccount = Account(BuildConfig.ACCOUNT_NAME, BuildConfig.ACCOUNT_TYPE)
        if (accountManager.addAccountExplicitly(newAccount, null, null)) {
            DebugLog.i("Account added successfully!")
        } else {
            DebugLog.d("Account already added, not adding again...")
        }
    }

    fun removeAccount(activity: Activity) {
        if (account != null) {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP_MR1) {
                accountManager.removeAccount(account, activity, null, null)
            } else {
                accountManager.removeAccount(account, null, null)
            }
        } else {
            DebugLog.e("Account is null")
        }
    }

    var userDetails: User?
        get() {
            if (account == null) {
                DebugLog.d("User account not found!")
                return null
            }

            var userDetails: String? = null
            try {
                userDetails = accountManager.getUserData(account, KEY_USER_DETAILS)
            } catch (e: Exception) {
                e.printStackTrace()
            }

            return if (userDetails != null) {
                Gson().fromJson(userDetails, User::class.java)
            } else {
                null
            }
        }
        set(userInfo) {
            var userData: String? = null
            if (userInfo != null) {
                userData = Gson().toJson(userInfo)
            }
            if (account == null) {
                DebugLog.e("Account is null. Not setting user data")
                return
            }
            accountManager.setUserData(account, KEY_USER_DETAILS, userData)
        }

    var userAuthToken: String?
        get() {
            if (account == null) {
                DebugLog.d("User account not found!")
                return null
            }
            try {
                return accountManager.getUserData(account, KEY_USER_AUTH_TOKEN)
            } catch (e: Exception) {
                e.printStackTrace()
            }
            return null
        }
        set(authToken) {
            if (account == null) {
                DebugLog.e("Account is null. Not setting user data")
                return
            }
            if (authToken.isNullOrBlank()) {
                DebugLog.e("userAuthToken is empty.")
                return
            }
            try {
                accountManager.setUserData(account, KEY_USER_AUTH_TOKEN, authToken)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

    var isMobileVerified: Boolean?
        get() {
            if (account == null) {
                DebugLog.d("User account not found!")
                return null
            }
            var accountVerified: String? = null
            try {
                accountVerified = accountManager.getUserData(account, KEY_IS_MOBILE_VERIFIED)
            } catch (e: Exception) {
                e.printStackTrace()
            }

            return if (accountVerified != null) {
                Gson().fromJson(accountVerified, Boolean::class.java)
            } else {
                null
            }
        }
        set(accountVerified) {
            if (account == null) {
                DebugLog.e("Account is null. Not setting user data")
                return
            }
            if (accountVerified == null) {
                DebugLog.e("accountVerified is empty.")
                return
            }
            try {
                accountManager.setUserData(
                    account,
                    KEY_IS_MOBILE_VERIFIED,
                    accountVerified.toString()
                )
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

    companion object {
        private const val KEY_USER_AUTH_TOKEN = "logged_in_user_auth_token"
        private const val KEY_USER_DETAILS = "logged_in_user_details"
        private const val KEY_IS_MOBILE_VERIFIED = "mobile_verified_status"
        private const val KEY_IS_USER_VERIFIED = "user_verified_status"
    }
}