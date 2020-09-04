package com.homc.homctruck.views.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.homc.homctruck.R
import kotlinx.android.synthetic.main.fragment_home.demo_text

class HomeFragment : BaseAppFragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setToolBarTitle(getString(R.string.menu_home))
        demo_text.text = getString(R.string.menu_home)
        demo_text.setOnClickListener {
            // findNavController().navigate(R.id.action_nav_share_to_nav_details)
        }
    }
}