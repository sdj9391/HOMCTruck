package com.homc.homctruck.views.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.homc.homctruck.R
import kotlinx.android.synthetic.main.fragment_find_truck_route.*

class FindTruckRouteFragment : MyTruckRouteFragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_find_truck_route, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setToolBarTitle(getString(R.string.menu_find_truck_route))
        demo_text.text = getString(R.string.menu_find_truck_route)
    }
}