package com.homc.homctruck.restapi

sealed class DataBound<out T> {
    class Success<out T>(val data: T) : DataBound<T>()
    class Error<out T>(val error: String?,val code: Int?) : DataBound<T>()
    class Loading<out T> : DataBound<T>()
}