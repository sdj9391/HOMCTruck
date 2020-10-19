package com.homc.homctruck.views.fragments

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import com.homc.homctruck.R

class TruckTabFragment : BaseTabFragment() {
    private val pendingFragment = PendingTruckListFragment()
    private val confirmedFragment = ConfirmedTruckListFragment()
    private val rejectedFragment = RejectedTruckListFragment()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setToolBarTitle(getString(R.string.label_manage_trucks))
    }

    override fun getTabFragmentList(): MutableList<Pair<String, Fragment>> {
        val fragments = mutableListOf<Pair<String, Fragment>>()
        pendingFragment.statusChangedListener = statusChangedListener
        confirmedFragment.statusChangedListener = statusChangedListener
        rejectedFragment.statusChangedListener = statusChangedListener
        fragments.add(Pair(getString(R.string.label_pending), pendingFragment))
        fragments.add(Pair(getString(R.string.label_approved), confirmedFragment))
        fragments.add(Pair(getString(R.string.label_rejected), rejectedFragment))
        return fragments
    }

    private val statusChangedListener = object : StatusChangedListener {
        override fun onPending(dataItem: Any) {
            pendingFragment.refreshListener.onRefresh(dataItem)
        }

        override fun onConfirmed(dataItem: Any) {
            confirmedFragment.refreshListener.onRefresh(dataItem)
        }

        override fun onRejected(dataItem: Any) {
            rejectedFragment.refreshListener.onRefresh(dataItem)
        }
    }
}