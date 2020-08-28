package com.homc.homctruck.views.fragments

import androidx.fragment.app.Fragment
import com.google.android.material.snackbar.Snackbar
import com.homc.homctruck.utils.DebugLog

open class BaseAppFragment : Fragment() {

    protected fun showMessage(message: String) {
        if (view != null) {
            Snackbar.make(requireView(), message, Snackbar.LENGTH_SHORT).show()
        } else {
            DebugLog.e("Not able to show a message!")
        }
    }

}