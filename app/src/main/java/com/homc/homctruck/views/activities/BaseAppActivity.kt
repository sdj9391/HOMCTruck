package com.homc.homctruck.views.activities

import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.google.android.material.snackbar.Snackbar
import com.homc.homctruck.R
import com.homc.homctruck.utils.DebugLog

open class BaseAppActivity : AppCompatActivity() {

    private var toolbar: Toolbar? = null
    var canRetryApiCall: Boolean = true

    protected fun initToolbar(isBackEnable: Boolean = false) {
        toolbar = findViewById(R.id.toolbar)
        if (toolbar != null) {
            setSupportActionBar(toolbar)
        } else {
            DebugLog.w("Toolbar is null")
        }

        if (isBackEnable) {
            actionBar?.setDisplayHomeAsUpEnabled(true);
        }
    }

    private fun setTitle(title: String) {
        if (toolbar == null) {
            supportActionBar?.setTitle(title)
        } else {
            val textViewRegular = toolbar?.findViewById<TextView?>(R.id.toolbar_title)
            if (textViewRegular == null) {
                DebugLog.w("textViewRegular is null")
                toolbar?.title = title
            } else {
                toolbar?.title = ""
                DebugLog.v("Debug title $title")
                textViewRegular.text = title
            }
        }
    }

    fun setToolBarTitle(title: String?) {
        if (toolbar == null) {
            setTitle(title)
            return
        }
        val titleTextView = toolbar?.findViewById<TextView>(R.id.toolbar_title)
        if (titleTextView == null) {
            super.setTitle(title)
        } else {
            setTitle("")
            titleTextView.text = title
        }
    }

    fun setToolBarTitle(resId: Int) {
        setToolBarTitle(getString(resId))
    }

    fun setToolBarSubTitle(title: String?) {
        toolbar = findViewById(R.id.toolbar)
        if (toolbar == null) {
            supportActionBar?.subtitle = title
            return
        }
        val subTitleTextView = toolbar?.findViewById<TextView>(R.id.toolbar_sub_title)
        if (subTitleTextView == null) {
            supportActionBar?.subtitle = title
        } else {
            subTitleTextView.text = title
            subTitleTextView.visibility = View.VISIBLE
        }
    }

    fun setSubTitleVisibility(isVisible: Boolean) {
        toolbar = findViewById(R.id.toolbar)
        if (toolbar == null) {
            DebugLog.v("Toolbar is null")
            return
        }
        val subTitleTextView = toolbar?.findViewById<TextView>(R.id.toolbar_sub_title)
        if (subTitleTextView == null) {
            DebugLog.v("subTitleTextView is null")
            return
        }
        if (isVisible) {
            subTitleTextView.visibility = View.VISIBLE
        } else {
            subTitleTextView.visibility = View.GONE
        }
    }

    protected fun showMessage(message: String) {
        try {
            Snackbar.make(findViewById(android.R.id.content), message, Snackbar.LENGTH_SHORT).show()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}