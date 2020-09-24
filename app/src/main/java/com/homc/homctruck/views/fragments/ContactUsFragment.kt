package com.homc.homctruck.views.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.homc.homctruck.R
import com.homc.homctruck.utils.getApplicationVersionCode
import com.homc.homctruck.utils.getApplicationVersionNumber
import kotlinx.android.synthetic.main.fragment_contact_us.*
import java.util.*

class ContactUsFragment : BaseAppFragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_contact_us, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setToolBarTitle(getString(R.string.menu_contact_us))
        val appDetails = String.format(
            Locale.getDefault(),
            "v%s (" + "%d)",
            getApplicationVersionNumber(requireActivity()),
            getApplicationVersionCode(requireActivity())
        )
        versionTextView.text = appDetails
    }
}