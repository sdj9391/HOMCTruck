package com.homc.homctruck.views.fragments

import com.homc.homctruck.R
import com.homc.homctruck.data.models.User
import com.homc.homctruck.utils.isInternetAvailable
import com.homc.homctruck.views.dialogs.BottomSheetViewItem

class ConfirmedUserListFragment : PendingUserListFragment() {
    override fun getData() {
        if (!isInternetAvailable()) {
            showMessage(getString(R.string.msg_no_internet))
            return
        }

        viewModel?.getUserList(User.USER_STATUS_CONFIRMED)
            ?.observe(viewLifecycleOwner, observeUserList)
    }

    override fun getBottomSheetOption(dataItem: User): MutableList<BottomSheetViewItem> {
        val sectionItems = mutableListOf<BottomSheetViewItem>()
        sectionItems.add(getBottomSheetViewForPendingAction(dataItem))
        sectionItems.add(getBottomSheetViewForRejectAction(dataItem))
        return sectionItems
    }
}