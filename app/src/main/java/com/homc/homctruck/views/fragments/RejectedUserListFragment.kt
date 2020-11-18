package com.homc.homctruck.views.fragments

import com.homc.homctruck.R
import com.homc.homctruck.data.models.User
import com.homc.homctruck.utils.isInternetAvailable
import com.homc.homctruck.views.dialogs.BottomSheetViewItem

class RejectedUserListFragment : PendingUserListFragment() {
    override fun getData(truckNumberKeyword: String?) {
        if (!isInternetAvailable()) {
            showMessage(getString(R.string.msg_no_internet))
            return
        }

        viewModel?.getUserList(User.USER_STATUS_REJECT, truckNumberKeyword)
            ?.observe(viewLifecycleOwner, observeUserList)
    }

    override fun getBottomSheetOption(dataItem: User): MutableList<BottomSheetViewItem> {
        val sectionItems = mutableListOf<BottomSheetViewItem>()
        sectionItems.add(getBottomSheetViewForPendingAction(dataItem))
        sectionItems.add(getBottomSheetViewForApproveAction(dataItem))
        return sectionItems
    }
}