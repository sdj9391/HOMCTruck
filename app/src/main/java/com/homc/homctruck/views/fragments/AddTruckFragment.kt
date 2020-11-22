package com.homc.homctruck.views.fragments

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.homc.homctruck.R
import com.homc.homctruck.data.models.ApiMessage
import com.homc.homctruck.data.models.Truck
import com.homc.homctruck.data.repositories.TruckRepository
import com.homc.homctruck.data.sourceremote.TruckRemoteDataSource
import com.homc.homctruck.restapi.AppApiInstance
import com.homc.homctruck.restapi.DataBound
import com.homc.homctruck.utils.DebugLog
import com.homc.homctruck.utils.getAuthToken
import com.homc.homctruck.utils.getAuthTokenFromFirebase
import com.homc.homctruck.utils.isInternetAvailable
import com.homc.homctruck.viewmodels.TruckViewModel
import com.homc.homctruck.viewmodels.TruckViewModelFactory
import com.homc.homctruck.views.activities.RetryListener
import kotlinx.android.synthetic.main.fragment_add_truck.*
import kotlinx.android.synthetic.main.fragment_add_truck.progressBar
import java.net.HttpURLConnection

open class AddTruckFragment : BaseAppFragment() {

    protected var viewModel: TruckViewModel? = null
    protected var isDirty = false

    private val onTextChangeListener = object : TextWatcher {
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

        }

        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            isDirty = true
        }

        override fun afterTextChanged(s: Editable?) {

        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_add_truck, container, false)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initViewModel()
    }

    private fun initViewModel() {
        val repository =
            TruckRepository(TruckRemoteDataSource(AppApiInstance.api(getAuthToken(requireActivity()))))
        val viewModelFactory = TruckViewModelFactory(requireActivity().application, repository)
        viewModel = ViewModelProvider(this, viewModelFactory)[TruckViewModel::class.java]
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setToolBarTitle(getString(R.string.label_add_truck))
        showTruckTypes()
        saveButton.setOnClickListener { onSaveClicked() }
        ownerNameEditText.addTextChangedListener(onTextChangeListener)
        truckNumberEditText.addTextChangedListener(onTextChangeListener)
        truckTypeDropDown.addTextChangedListener(onTextChangeListener)
        chesseNumberEditText.addTextChangedListener(onTextChangeListener)
    }

    protected open fun showTruckTypes() {
        val items = resources.getStringArray(R.array.truck_types)
        val adapter = ArrayAdapter(requireContext(), R.layout.item_drop_down, items)
        truckTypeDropDown.setAdapter(adapter)
    }

    private fun onSaveClicked() {
        val truck = getValidatedTruckData()
        if (truck == null) {
            DebugLog.e("Truck Details found null")
            return
        }

        saveTruckDetails(truck)
    }

    private fun getValidatedTruckData(): Truck? {
        val ownerName = ownerNameEditText.text.toString().trim()
        if (ownerName.isNullOrBlank()) {
            ownerNameEditText.error = getString(R.string.msg_enter_owner_name)
            ownerNameEditText.requestFocus()
            return null
        }

        val truckNumber = truckNumberEditText.text.toString().trim()
        if (truckNumber.isNullOrBlank()) {
            truckNumberEditText.error = getString(R.string.msg_enter_truck_number)
            truckNumberEditText.requestFocus()
            return null
        }

        val truckType = truckTypeDropDown.text.toString().trim()
        if (truckType.isNullOrBlank()) {
            truckTypeDropDown.error = getString(R.string.msg_select_truck_type)
            truckTypeDropDown.requestFocus()
            return null
        }

        val chesseNumber = chesseNumberEditText.text.toString().trim()
        if (chesseNumber.isNullOrBlank()) {
            chesseNumberEditText.error = getString(R.string.msg_enter_truck_chesse_number)
            chesseNumberEditText.requestFocus()
            return null
        }

        val truck = Truck()
        truck.truckNumber = truckNumber.toUpperCase().replace(" ", "")
        truck.type = truckType
        truck.ownerName = ownerName
        truck.chesseNumber = chesseNumber.toUpperCase()
        truck.verificationStatus = Truck.TRUCK_STATUS_PENDING
        return truck
    }

    protected open fun saveTruckDetails(truck: Truck) {
        if (!isInternetAvailable()) {
            showMessage(getString(R.string.msg_no_internet))
            return
        }

        viewModel?.addNewTruck(truck)
            ?.observe(viewLifecycleOwner, observeSaveTruckDetails)
    }

    protected val observeSaveTruckDetails = Observer<DataBound<ApiMessage>> {
        if (it == null) {
            DebugLog.e("ApiMessage is null")
            return@Observer
        }

        it.let { dataBound ->
            when (dataBound) {
                is DataBound.Success -> {
                    saveButton.isEnabled = true
                    progressBar.visibility = View.GONE
                    showMessage(getString(R.string.msg_truck_details_saved_successfully))
                    isDirty = false
                }
                is DataBound.Error -> {
                    saveButton.isEnabled = true
                    progressBar.visibility = View.GONE
                    if (dataBound.code == HttpURLConnection.HTTP_NOT_FOUND) {
                        showMessage(getString(R.string.error_something_went_wrong))
                    } else if (dataBound.code == HttpURLConnection.HTTP_NOT_ACCEPTABLE) {
                        truckNumberEditText.error = getString(R.string.error_truck_already_added)
                        truckNumberEditText.requestFocus()
                    } else {
                        DebugLog.e("Error: ${dataBound.message}")
                        showMessage("${dataBound.message}")
                    }
                }
                is DataBound.Retry -> {
                    if (canRetryApiCall) {
                        getAuthTokenFromFirebase(requireActivity(), object : RetryListener {
                            override fun retry() {
                                initViewModel()
                                saveButton.isEnabled = true
                                progressBar.visibility = View.GONE
                                showMessage(getString(R.string.error_something_went_wrong_try_again))
                            }
                        })
                    } else {
                        canRetryApiCall = false
                        saveButton.isEnabled = true
                        progressBar.visibility = View.GONE
                        showMessage(getString(R.string.error_something_went_wrong))
                    }
                }
                is DataBound.Loading -> {
                    progressBar.visibility = View.VISIBLE
                    saveButton.isEnabled = false
                }
            }
        }
    }
}