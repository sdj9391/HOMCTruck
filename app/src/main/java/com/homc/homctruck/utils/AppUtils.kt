package com.homc.homctruck.utils

import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.content.pm.PackageManager
import android.text.SpannableStringBuilder
import android.text.TextUtils
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.text.bold
import androidx.core.text.color
import com.google.firebase.auth.FirebaseAuth
import com.homc.homctruck.R
import com.homc.homctruck.data.models.ApiMessage
import com.homc.homctruck.data.models.User
import com.homc.homctruck.restapi.AppApiInstance
import com.homc.homctruck.utils.account.BaseAccountManager
import com.homc.homctruck.views.fragments.RetryListener
import retrofit2.Response
import java.io.IOException
import java.net.HttpURLConnection
import java.text.SimpleDateFormat
import java.util.*
import java.util.regex.Pattern

@Throws(InterruptedException::class, IOException::class)
fun isInternetAvailable(): Boolean {
    val command = "ping -c 1 google.com"
    return Runtime.getRuntime().exec(command).waitFor() == 0
}

fun parseApiMessage(response: Response<*>?): ApiMessage {
    var error = ApiMessage()
    if (response == null) {
        error.messageResId = R.string.error_server_no_response
        return error
    }
    val converter = AppApiInstance.retrofit(null)
        .responseBodyConverter<ApiMessage>(ApiMessage::class.java, arrayOfNulls(0))
    try {
        val errorBody = response.errorBody()
        if (errorBody != null) {
            error = converter.convert(errorBody)!!
        }
    } catch (e: IOException) {
        DebugLog.e("Failed to parse error response. " + e.localizedMessage)
    }
    return error
}

fun parse(statusCode: Int): Int {
    val networkStatus: NetworkStatus =
        parseToNetworkStatus(statusCode)
    return parse(networkStatus)
}

enum class NetworkStatus {
    NO_RESPONSE, BAD_REQUEST, NOT_FOUND, UNAUTHORIZED, FORBIDDEN, BAD_METHOD, INTERNAL_ERROR, NOT_IMPLEMENTED, BAD_GATEWAY, UNAVAILABLE, GATEWAY_TIMEOUT, GENERAL_ERROR
}

fun parseToNetworkStatus(statusCode: Int): NetworkStatus {
    if (statusCode == -1) {
        return NetworkStatus.NO_RESPONSE
    }
    return when (statusCode) {
        HttpURLConnection.HTTP_BAD_REQUEST -> {
            NetworkStatus.BAD_REQUEST
        }
        HttpURLConnection.HTTP_NOT_FOUND -> {
            NetworkStatus.NOT_FOUND
        }
        HttpURLConnection.HTTP_UNAUTHORIZED -> {
            NetworkStatus.UNAUTHORIZED
        }
        HttpURLConnection.HTTP_FORBIDDEN -> {
            NetworkStatus.FORBIDDEN
        }
        HttpURLConnection.HTTP_BAD_METHOD -> {
            NetworkStatus.BAD_METHOD
        }
        HttpURLConnection.HTTP_INTERNAL_ERROR -> {
            NetworkStatus.INTERNAL_ERROR
        }
        HttpURLConnection.HTTP_NOT_IMPLEMENTED -> {
            NetworkStatus.NOT_IMPLEMENTED
        }
        HttpURLConnection.HTTP_BAD_GATEWAY -> {
            NetworkStatus.BAD_GATEWAY
        }
        HttpURLConnection.HTTP_UNAVAILABLE -> {
            NetworkStatus.UNAVAILABLE
        }
        HttpURLConnection.HTTP_GATEWAY_TIMEOUT -> {
            NetworkStatus.GATEWAY_TIMEOUT
        }
        else -> {
            NetworkStatus.GENERAL_ERROR
        }
    }
}

fun parse(networkStatus: NetworkStatus): Int {
    return if (networkStatus == NetworkStatus.NO_RESPONSE) {
        R.string.error_server_no_response
    } else if (networkStatus == NetworkStatus.BAD_REQUEST) {
        R.string.error_server_400bad_request
    } else if (networkStatus == NetworkStatus.NOT_FOUND) {
        R.string.error_server_404not_found
    } else if (networkStatus == NetworkStatus.UNAUTHORIZED) {
        R.string.error_server_401unauthorized
    } else if (networkStatus == NetworkStatus.FORBIDDEN) {
        R.string.error_server_403forbidden
    } else if (networkStatus == NetworkStatus.BAD_METHOD) {
        R.string.error_server_405not_allowed
    } else if (networkStatus == NetworkStatus.INTERNAL_ERROR) {
        R.string.error_server_500internal_error
    } else if (networkStatus == NetworkStatus.NOT_IMPLEMENTED) {
        R.string.error_server_501not_implemented
    } else if (networkStatus == NetworkStatus.BAD_GATEWAY) {
        R.string.error_server_502bad_gateway
    } else if (networkStatus == NetworkStatus.UNAVAILABLE) {
        R.string.error_server_503unavailable
    } else if (networkStatus == NetworkStatus.GATEWAY_TIMEOUT) {
        R.string.error_server_504gateway_timeout
    } else {
        R.string.error_something_went_wrong
    }
}

/**
 * Checks if the input parameter is a valid email.
 * Accepts +
 *
 * @param email
 * @return
 */
fun String.isValidEmail(): Boolean {
    val emailPattern =
        "^[_A-Za-z0-9-]+(\\.[_A-Za-z0-9-]+)*(\\+[_A-Za-z0-9-]+)*@[A-Za-z0-9]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$"
    val pattern = Pattern.compile(emailPattern, Pattern.CASE_INSENSITIVE)
    val matcher = pattern.matcher(this)
    return matcher.matches()
}

/**
 * Creates a confirmation dialog with Yes-No Button. By default the buttons just dismiss the
 * dialog.
 *
 * @param ctx
 * @param message     Message to be shown in the dialog.
 * @param yesListener Yes click handler
 * @param noListener
 */
fun showConfirmDialog(
    ctx: Context?, message: String?,
    yesListener: DialogInterface.OnClickListener?,
    noListener: DialogInterface.OnClickListener?
) {
    showConfirmDialog(ctx, message, yesListener, noListener, "Yes", "No")
}

/**
 * Creates a confirmation dialog with Yes-No Button. By default the buttons just dismiss the
 * dialog.
 *
 * @param ctx
 * @param message     Message to be shown in the dialog.
 * @param yesListener Yes click handler
 * @param noListener
 * @param yesLabel    Label for yes button
 * @param noLabel     Label for no button
 */
fun showConfirmDialog(
    ctx: Context?,
    message: String?,
    yesListener: DialogInterface.OnClickListener?,
    noListener: DialogInterface.OnClickListener?,
    yesLabel: String?,
    noLabel: String?
) {
    var yesClickListener = yesListener
    var noClickListener = noListener
    val builder = AlertDialog.Builder(ctx)
    if (yesListener == null) {
        yesClickListener = DialogInterface.OnClickListener { dialog, which -> dialog.dismiss() }
    }
    if (noListener == null) {
        noClickListener = DialogInterface.OnClickListener { dialog, which -> dialog.dismiss() }
    }
    builder.setMessage(message).setPositiveButton(yesLabel, yesClickListener)
        .setNegativeButton(noLabel, noClickListener).show()
}

/**
 * Shows an alert dialog with the OK button. When the user presses OK button, the dialog
 * dismisses.
 */
fun showAlertDialog(context: Context, title: String, body: String?) {
    showAlertDialog(context, title, body, null)
}

/**
 * Shows an alert dialog with OK button
 */
fun showAlertDialog(context: Context, title: String, body: String?,
    listener: DialogInterface.OnClickListener?
) {
    var okListener = listener
    if (okListener == null) {
        okListener = DialogInterface.OnClickListener { dialog, which -> dialog.cancel() }
    }
    val builder = AlertDialog.Builder(context)
        .setMessage(body).setPositiveButton("OK", okListener)
    if (!TextUtils.isEmpty(title)) {
        builder.setTitle(title)
    }
    builder.show()
}

fun hideSoftKeyboard(activity: Activity) {
    try {
        val inputMethodManager =
            activity.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
        if (activity.currentFocus != null) {
            inputMethodManager.hideSoftInputFromWindow(activity.currentFocus!!.windowToken, 0)
        }
    } catch (e: Exception) {
        e.printStackTrace()
    }
}

const val DEFAULT_DATE_FORMAT = "dd-MMM-yyyy"

@SuppressLint("SimpleDateFormat")
fun formatDateForDisplay(date: Long, format: String? = DEFAULT_DATE_FORMAT): String? {
    return try {
        SimpleDateFormat(format).format(Date(date))
    } catch (exception: Exception) {
        exception.printStackTrace()
        null
    }
}

/**
 * Gets the version name of the application. For e.g. 1.9.3
 */
fun getApplicationVersionNumber(context: Context): String? {
    var versionName: String? = null
    try {
        versionName = context.packageManager.getPackageInfo(context.packageName, 0).versionName
    } catch (e: PackageManager.NameNotFoundException) {
        e.printStackTrace()
    }
    return versionName
}

/**
 * Gets the version code of the application. For e.g. Maverick Meerkat or 2013050301
 */
fun getApplicationVersionCode(ctx: Context): Int {
    var versionCode = 0
    try {
        versionCode = ctx.packageManager.getPackageInfo(ctx.packageName, 0).versionCode
    } catch (e: PackageManager.NameNotFoundException) {
        e.printStackTrace()
    }
    return versionCode
}

fun setColorsAndCombineStrings(textView: TextView, string1: String?, string2: String?, color: Int) {
    textView.text = SpannableStringBuilder().color(ContextCompat.getColor(textView.context, color))
    { append(SpannableStringBuilder().bold { append("$string1:") }) }.append(" ").append(string2)
}

fun setColorsAndCombineStrings(textView: TextView, string1: String?, string2: String?) {
    setColorsAndCombineStrings(textView, string1, string2, R.color.title_text)
}

fun getMillis(
    year: Int,
    monthOfYear: Int,
    dayOfMonth: Int,
    hours: Int = 0,
    min: Int = 0,
    sec: Int = 0,
    millis: Int = 0
): Long {
    val calendar = Calendar.getInstance()
    calendar[Calendar.YEAR] = year
    calendar[Calendar.MONTH] = monthOfYear
    calendar[Calendar.DAY_OF_MONTH] = dayOfMonth
    calendar[Calendar.HOUR] = hours
    calendar[Calendar.MINUTE] = min
    calendar[Calendar.SECOND] = sec
    calendar[Calendar.MILLISECOND] = millis
    return calendar.timeInMillis
}

fun canHaveFeatureAccess(context: Context): Boolean {
    return when (BaseAccountManager(context).userDetails?.verificationStatus
        ?: User.USER_STATUS_PENDING) {
        User.USER_STATUS_PENDING -> {
            showAlertDialog(context, context.getString(R.string.msg_action_deny), null, null)
            false
        }
        User.USER_STATUS_REJECT -> {
            showAlertDialog(context, context.getString(R.string.msg_action_deny), null, null)
            DebugLog.e("Status is rejected and still user is trying to access feature.")
            false
        }
        else -> true
    }
}

fun canHaveAppAccess(context: Context): Boolean {
    return when (BaseAccountManager(context).userDetails?.verificationStatus
        ?: User.USER_STATUS_PENDING) {
        User.USER_STATUS_REJECT -> {
            showAlertDialog(context, context.getString(R.string.msg_app_access_deny), null, null)
            false
        }
        else -> true
    }
}

fun getAuthToken(context: Context): String? {
    return BaseAccountManager(context).userAuthToken
}

fun getAuthTokenFromFirebase(context: Context, retryListener: RetryListener) {
    val user = FirebaseAuth.getInstance().currentUser
    if (user == null || !isInternetAvailable()) {
        DebugLog.e("User is null or internet is not available")
        return
    }

    user.getIdToken(true).addOnCompleteListener { task ->
        if (task.isSuccessful) {
            val firebaseToken = task.result.token
            if (firebaseToken.isNullOrBlank()) {
                DebugLog.w("Setting token null.")
            } else {
                DebugLog.w("Setting firebaseToken")
                BaseAccountManager(context).userAuthToken = firebaseToken
            }
        } else {
            DebugLog.w("Setting token null.")
        }
        retryListener.retry()
    }
}