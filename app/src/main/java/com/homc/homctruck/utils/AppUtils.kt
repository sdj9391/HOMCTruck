package com.homc.homctruck.utils

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.view.inputmethod.InputMethodManager
import com.homc.homctruck.R
import com.homc.homctruck.data.models.ApiMessage
import com.homc.homctruck.restapi.AppApiInstance
import retrofit2.Response
import java.io.IOException
import java.net.HttpURLConnection
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
    val converter = AppApiInstance.retrofit
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