package com.homc.homctruck.views.fragments

import android.os.Bundle
import android.view.View
import com.homc.homctruck.R
import com.homc.homctruck.data.models.Truck
import com.homc.homctruck.data.models.TruckRoute
import com.homc.homctruck.utils.DebugLog
import com.homc.homctruck.utils.TemporaryCache
import com.homc.homctruck.utils.formatDateForDisplay
import com.homc.homctruck.utils.isInternetAvailable
import kotlinx.android.synthetic.main.fragment_add_truck_route.*

class EditTruckRouteFragment : AddTruckRouteFragment() {
    private var truckRouteDetails: TruckRoute? = null
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        getTruckDetails()
        super.onViewCreated(view, savedInstanceState)
        setToolBarTitle(getString(R.string.label_edit_truck_route))
        showTruckRouteDetails()
    }

    private fun getTruckDetails() {
        val dataItem = TemporaryCache[EXTRA_TRUCK_ROUTE_DETAILS]
        if (dataItem !is TruckRoute) {
            DebugLog.e("Wrong instance found. Expected: ${Truck::class.java.simpleName} Found: $dataItem")
            return
        }
        truckRouteDetails = dataItem
    }

    private fun showTruckRouteDetails() {
        // truckNumberDropDown.isEnabled = false
        startMillis = truckRouteDetails?.startJourneyDate
        endMillis = truckRouteDetails?.endJourneyDate

        truckNumberDropDown.setText(truckRouteDetails?.truck?.truckNumber)
        fromCityEditText.setText(truckRouteDetails?.fromCity)
        toCityEditText.setText(truckRouteDetails?.toCity)
        startJourneyDateEditText.setText(formatDateForDisplay(startMillis ?: 0))
        endJourneyDateEditText.setText(formatDateForDisplay(endMillis ?: 0))
        isDirty = false
    }

    override fun saveTruckRouteDetails(truckRoute: TruckRoute) {
        if (!isInternetAvailable()) {
            showMessage(getString(R.string.msg_no_internet))
            return
        }

        val truckRouteId = truckRouteDetails?.id
        if (truckRouteId.isNullOrBlank()) {
            DebugLog.e("Truck Id found null")
            return
        }

        viewModel?.updateTruckRouteDetails(truckRouteId, truckRoute)
            ?.observe(viewLifecycleOwner, observeSaveTruckRouteDetails)
    }

    companion object {
        const val EXTRA_TRUCK_ROUTE_DETAILS = "extra_truck_route_details"
    }
}