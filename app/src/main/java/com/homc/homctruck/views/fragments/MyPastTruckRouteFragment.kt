package com.homc.homctruck.views.fragments

import android.os.Bundle
import android.view.View
import com.homc.homctruck.R
import com.homc.homctruck.data.models.TruckRoute
import com.homc.homctruck.utils.TemporaryCache
import com.homc.homctruck.utils.isInternetAvailable
import kotlinx.android.synthetic.main.item_common_list_layout.*

class MyPastTruckRouteFragment : MyTruckRouteFragment() {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setToolBarTitle(getString(R.string.label_past_truck_routes))
        buttonBackView.visibility = View.GONE
        bottomButton.visibility = View.GONE
    }

    override fun getData() {
        if (!isInternetAvailable()) {
            showMessage(getString(R.string.msg_no_internet))
            return
        }

        viewModel?.getMyPastTruckRouteList()
            ?.observe(viewLifecycleOwner, observeTruckRouteList)
    }

    override fun editTruckRouteItem(dataItem: TruckRoute) {
        TemporaryCache.put(EditTruckRouteFragment.EXTRA_TRUCK_ROUTE_DETAILS, dataItem)
        navigationController?.navigate(R.id.action_myPastTruckRouteFragment_to_editTruckRouteFragment)
    }

    override fun addPastTruckRoutePlank(data: MutableList<Any>) {
        // No need to add plank
    }

    override fun getPickCountToShowError(): Int {
        return 0
    }
}