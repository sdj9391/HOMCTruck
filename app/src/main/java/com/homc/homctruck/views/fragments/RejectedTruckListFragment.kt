package com.homc.homctruck.views.fragments

import com.homc.homctruck.data.models.Truck
import com.homc.homctruck.views.dialogs.BottomSheetViewItem

class RejectedTruckListFragment : PendingTruckListFragment() {
    override fun getData() {
        viewModel?.getTruckList(Truck.TRUCK_STATUS_REJECT)
            ?.observe(viewLifecycleOwner, observeTruckList)
    }

    override fun getBottomSheetOption(dataItem: Truck): MutableList<BottomSheetViewItem> {
        val sectionItems = mutableListOf<BottomSheetViewItem>()
        sectionItems.add(getBottomSheetViewForPendingAction(dataItem))
        sectionItems.add(getBottomSheetViewForApproveAction(dataItem))
        return sectionItems
    }
}