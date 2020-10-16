package com.homc.homctruck.views.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.viewpager.widget.ViewPager
import com.google.android.material.tabs.TabLayout
import com.homc.homctruck.R
import com.homc.homctruck.views.adapters.FragmentPagerAdapter
import kotlinx.android.synthetic.main.fragment_tabs_viewpager.*

abstract class BaseTabFragment : BaseAppFragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_tabs_viewpager, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val fragmentPagerAdapter = FragmentPagerAdapter(requireActivity().supportFragmentManager, getTabFragmentList())
        viewPager.offscreenPageLimit = 2
        viewPager.adapter = fragmentPagerAdapter
        tabLayout.setupWithViewPager(viewPager)
    }

    abstract fun getTabFragmentList(): MutableList<Pair<String, Fragment>>
}