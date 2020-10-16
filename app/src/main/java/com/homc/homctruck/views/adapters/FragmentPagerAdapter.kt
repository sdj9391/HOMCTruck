package com.homc.homctruck.views.adapters

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter

class FragmentPagerAdapter(
    fragmentManager: FragmentManager,
    private val data: MutableList<Pair<String, Fragment>>
) : FragmentStatePagerAdapter(fragmentManager, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {
    override fun getCount(): Int {
        return data.size
    }

    override fun getPageTitle(position: Int): CharSequence? {
        return data[position].first
    }

    override fun getItem(position: Int): Fragment {
        return data[position].second
    }
}