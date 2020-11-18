package com.homc.homctruck.views.fragments

import android.os.Bundle
import android.view.View
import com.homc.homctruck.R
import com.homc.homctruck.data.models.Load
import com.homc.homctruck.utils.TemporaryCache
import com.homc.homctruck.utils.isInternetAvailable
import kotlinx.android.synthetic.main.item_common_list_layout.*

class MyPastLoadFragment : MyLoadFragment() {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setToolBarTitle(getString(R.string.label_past_loads))
        bottomButton.visibility = View.GONE
        buttonBackView.visibility = View.GONE
    }

    override fun getData(materialKeyword: String?) {
        if (!isInternetAvailable()) {
            showMessage(getString(R.string.msg_no_internet))
            return
        }

        viewModel?.getMyPastLoadList(materialKeyword)
            ?.observe(viewLifecycleOwner, observeLoadList)
    }

    override fun editLoadItem(dataItem: Load) {
        TemporaryCache.put(EditLoadFragment.EXTRA_LOAD_DETAILS, dataItem)
        navigationController?.navigate(R.id.action_myPastLoadFragment_to_editLoadFragment)
    }

    override fun addPastLoadPlank(data: MutableList<Any>) {
        // No need to add plank
    }

    override fun getPickCountToShowError(): Int {
        return 0
    }
}