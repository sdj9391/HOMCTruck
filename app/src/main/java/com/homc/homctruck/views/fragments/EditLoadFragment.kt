package com.homc.homctruck.views.fragments

import android.os.Bundle
import android.view.View
import com.homc.homctruck.R
import com.homc.homctruck.data.models.Load
import com.homc.homctruck.utils.DebugLog
import com.homc.homctruck.utils.TemporaryCache
import com.homc.homctruck.utils.isInternetAvailable
import kotlinx.android.synthetic.main.fragment_add_load.*

class EditLoadFragment : AddLoadFragment() {
    private var loadDetails: Load? = null
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        getLoadDetails()
        super.onViewCreated(view, savedInstanceState)
        setToolBarTitle(getString(R.string.label_edit_load))
        showLoadDetails()
    }

    private fun getLoadDetails() {
        val dataItem = TemporaryCache[EXTRA_LOAD_DETAILS]
        if (dataItem !is Load) {
            DebugLog.e("Wrong instance found. Expected: ${Load::class.java.simpleName} Found: $dataItem")
            return
        }
        loadDetails = dataItem
    }

    private fun showLoadDetails() {
        fromPlace = loadDetails?.fromPlace
        toPlace = loadDetails?.toPlace

        goodsNameEditText.setText(loadDetails?.nameOfGoods)
        fromCityEditText.setText(fromPlace?.city)
        toCityEditText.setText(toPlace?.city)
        ratePerTonEditText.setText(loadDetails?.perTonRate?.toString())
        totalLoadInTonsEditText.setText(loadDetails?.totalLoadInTons?.toString())
        totalAmountEditText.setText(loadDetails?.totalAmount?.toString())
        transitDaysEditText.setText(loadDetails?.transitDaysForTruck?.toString())
        isDirty = false
    }

    override fun showMaterialTypes() {
        materialTypeDropDown.setText(loadDetails?.typeOfMaterial)
        super.showMaterialTypes()
    }

    override fun showTruckTypes() {
        truckTypeDropDown.setText(loadDetails?.typeOfTruck)
        super.showTruckTypes()
    }

    override fun saveLoadDetails(load: Load) {
        if (!isInternetAvailable()) {
            showMessage(getString(R.string.msg_no_internet))
            return
        }

        val loadId = loadDetails?.id
        if (loadId.isNullOrBlank()) {
            DebugLog.e("Load Id found null")
            return
        }

        viewModel?.updateLoadDetails(loadId, load)
            ?.observe(viewLifecycleOwner, observeSaveLoadDetails)
    }

    companion object {
        const val EXTRA_LOAD_DETAILS = "extra_load_details"
    }
}