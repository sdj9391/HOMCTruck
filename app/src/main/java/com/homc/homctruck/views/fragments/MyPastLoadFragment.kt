package com.homc.homctruck.views.fragments

import com.homc.homctruck.R
import com.homc.homctruck.utils.isInternetAvailable

class MyPastLoadFragment : MyLoadFragment() {

    override fun getData() {
        if (!isInternetAvailable()) {
            showMessage(getString(R.string.msg_no_internet))
            return
        }

        viewModel?.getMyPastLoadList()
            ?.observe(viewLifecycleOwner, observeLoadList)
    }

    override fun addPastLoadPlank(data: MutableList<Any>) {
        // No need to add plank
    }

    override fun getPickCountToShowError(): Int {
        return 0
    }
}