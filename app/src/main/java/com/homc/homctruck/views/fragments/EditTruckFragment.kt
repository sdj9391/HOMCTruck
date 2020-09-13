package com.homc.homctruck.views.fragments

import android.os.Bundle
import android.view.View
import com.homc.homctruck.R
import com.homc.homctruck.data.models.Truck
import com.homc.homctruck.utils.DebugLog
import com.homc.homctruck.utils.TemporaryCache
import com.homc.homctruck.utils.isInternetAvailable
import kotlinx.android.synthetic.main.fragment_add_truck.*

class EditTruckFragment : AddTruckFragment() {
    private var truckDetails: Truck? = null
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        getTruckDetails()
        super.onViewCreated(view, savedInstanceState)
        setToolBarTitle(getString(R.string.label_edit_truck))
        showTruckDetails()
    }

    private fun getTruckDetails() {
        val dataItem = TemporaryCache[EXTRA_TRUCK_DETAILS]
        if (dataItem !is Truck) {
            DebugLog.e("Wrong instance found. Expected: ${Truck::class.java.simpleName} Found: $dataItem")
            return
        }
        truckDetails = dataItem
    }

    private fun showTruckDetails() {
        truckNumberEditText.setText(truckDetails?.truckNumber)
        chesseNumberEditText.setText(truckDetails?.chesseNumber)
        isDirty = false
    }

    override fun showTruckTypes() {
        truckTypeDropDown.setText(truckDetails?.type)
        super.showTruckTypes()
    }

    override fun saveTruckDetails(truck: Truck) {
        if (!isInternetAvailable()) {
            showMessage(getString(R.string.msg_no_internet))
            return
        }

        val truckId = truckDetails?.id
        if (truckId.isNullOrBlank()) {
            DebugLog.e("Truck Id found null")
            return
        }

        viewModel?.updateTruckDetails(truckId, truck)
            ?.observe(viewLifecycleOwner, observeSaveTruckDetails)
    }

    companion object {
        const val EXTRA_TRUCK_DETAILS = "extra_truck_details"
    }
}