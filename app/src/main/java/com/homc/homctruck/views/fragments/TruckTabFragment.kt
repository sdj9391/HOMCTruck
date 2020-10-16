package com.homc.homctruck.views.fragments

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import com.homc.homctruck.R

class TruckTabFragment : BaseTabFragment() {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setToolBarTitle(getString(R.string.label_manage_trucks))
    }
    override fun getTabFragmentList(): MutableList<Pair<String, Fragment>> {
        val fragments = mutableListOf<Pair<String, Fragment>>()
        fragments.add(Pair(getString(R.string.label_pending), PendingTruckListFragment()))
        fragments.add(Pair(getString(R.string.label_approved), ConfirmedTruckListFragment()))
        fragments.add(Pair(getString(R.string.label_rejected), RejectedTruckListFragment()))
        return fragments
    }
}