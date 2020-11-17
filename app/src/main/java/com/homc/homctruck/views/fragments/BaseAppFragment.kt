package com.homc.homctruck.views.fragments

import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.android.material.snackbar.Snackbar
import com.homc.homctruck.utils.DebugLog
import com.homc.homctruck.views.activities.BaseAppActivity

open class BaseAppFragment : Fragment() {

    var canRetryApiCall: Boolean = true

    protected fun setToolBarTitle(string: String?) {
        if (!isAdded) {
            DebugLog.e("fragment is not added")
            return
        }

        if (activity is BaseAppActivity) {
            val baseSocialActivity = activity as BaseAppActivity
            baseSocialActivity.setToolBarTitle(string)
        } else {
            DebugLog.v("Wrong instance! Expected: ${BaseAppActivity::class.java.simpleName} Found: $activity")
        }
    }

    protected fun setToolBarSubTitle(string: String?) {
        if (!isAdded) {
            DebugLog.e("fragment is not added")
            return
        }

        if (activity is BaseAppActivity) {
            val baseSocialActivity = activity as BaseAppActivity
            baseSocialActivity.setToolBarSubTitle(string)
        } else {
            DebugLog.v("Wrong instance! Expected: ${BaseAppActivity::class.java.simpleName} Found: $activity")
        }
    }

    protected fun setSubTitleVisibility(isShow: Boolean = false) {
        if (!isAdded) {
            DebugLog.e("fragment is not added")
            return
        }

        if (activity is BaseAppActivity) {
            val baseSocialActivity = activity as BaseAppActivity
            baseSocialActivity.setSubTitleVisibility(isShow)
        } else {
            DebugLog.v("Wrong instance! Expected: ${BaseAppActivity::class.java.simpleName} Found: $activity")
        }
    }

    protected fun showMessage(message: String) {
        if (view != null) {
            Snackbar.make(requireView(), message, Snackbar.LENGTH_SHORT).show()
        } else {
            DebugLog.e("Not able to show a message!")
        }
    }

    protected fun showToastMessage(message: String, duration: Int = Toast.LENGTH_SHORT) {
        if (view != null) {
            val toast: Toast = Toast.makeText(requireActivity(), message, duration)
            toast.show()
        } else {
            DebugLog.e("Not able to show a message!")
        }
    }
}