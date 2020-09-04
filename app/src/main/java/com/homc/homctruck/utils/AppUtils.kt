package com.homc.homctruck.utils

import com.homc.homctruck.R
import com.homc.homctruck.data.models.ApiMessage
import com.homc.homctruck.restapi.AppApiInstance
import retrofit2.Response
import java.io.IOException
import java.net.HttpURLConnection

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
        R.string.error_general
    }
}