package com.homc.homctruck.views.fragments

import androidx.fragment.app.Fragment
import com.homc.homctruck.R

class UserTabFragment : BaseTabFragment() {
    override fun getTabFragmentList(): MutableList<Pair<String, Fragment>> {
        val fragments = mutableListOf<Pair<String, Fragment>>()
        fragments.add(Pair(getString(R.string.label_pending), PendingUserListFragment()))
        fragments.add(Pair(getString(R.string.label_approved), ConfirmedUserListFragment()))
        fragments.add(Pair(getString(R.string.label_rejected), RejectedUserListFragment()))
        return fragments
    }
}