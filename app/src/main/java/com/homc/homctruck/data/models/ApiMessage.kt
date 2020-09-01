package com.homc.homctruck.data.models

import androidx.annotation.StringRes
import com.homc.homctruck.R

class ApiMessage() {
    var success : String? = null
    var message: String? = null
    @StringRes
    var messageResId : Int = R.string.error_general
}