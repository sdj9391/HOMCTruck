package com.homc.homctruck.views.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.NavController
import androidx.navigation.Navigation
import com.homc.homctruck.R
import kotlinx.android.synthetic.main.fragment_home.*

class HomeFragment : BaseAppFragment() {

    private var navigationController: NavController? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setToolBarTitle(getString(R.string.menu_home))
        navigationController = Navigation.findNavController(requireView())

        addTruckRoute.setOnClickListener {
                navigationController?.navigate(R.id.action_homeFragment_to_addTruckRouteFragment)
        }
        findTruckRoute.setOnClickListener {
                navigationController?.navigate(R.id.action_homeFragment_to_findTruckRouteFragment)
        }
        myTruckRoute.setOnClickListener {
                navigationController?.navigate(R.id.action_homeFragment_to_myTruckRouteFragment)
        }

        addLoad.setOnClickListener {
                navigationController?.navigate(R.id.action_homeFragment_to_addLoadFragment)
        }
        findLoad.setOnClickListener {
                navigationController?.navigate(R.id.action_homeFragment_to_findLoadFragment)
        }
        myLoad.setOnClickListener {
                navigationController?.navigate(R.id.action_homeFragment_to_myLoadFragment)
        }
    }
}