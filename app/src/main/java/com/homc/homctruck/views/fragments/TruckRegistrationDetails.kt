package com.homc.homctruck.views.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.homc.homctruck.R
import com.homc.homctruck.data.models.ApiMessage
import com.homc.homctruck.data.models.TruckRegistrationInfo
import com.homc.homctruck.data.repositories.TruckRepository
import com.homc.homctruck.data.sourceremote.TruckRemoteDataSource
import com.homc.homctruck.restapi.AppApiInstance
import com.homc.homctruck.restapi.DataBound
import com.homc.homctruck.utils.DebugLog
import com.homc.homctruck.utils.getAuthToken
import com.homc.homctruck.utils.isInternetAvailable
import com.homc.homctruck.viewmodels.TruckViewModel
import com.homc.homctruck.viewmodels.TruckViewModelFactory
import kotlinx.android.synthetic.main.fragment_add_truck.*
import kotlinx.android.synthetic.main.fragment_truck_registration_details.*
import kotlinx.android.synthetic.main.fragment_truck_registration_details.progressBar
import kotlinx.android.synthetic.main.fragment_truck_registration_details.saveButton
import kotlinx.android.synthetic.main.message_view.*
import java.net.HttpURLConnection

class TruckRegistrationDetails : BaseAppFragment() {

    private var viewModel: TruckViewModel? = null

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
        getTruckRegistrationInfo()
        saveButton.setOnClickListener { onSaveClicked() }
    }

    private fun getTruckRegistrationInfo() {
        if (!isInternetAvailable()) {
            showMessage(getString(R.string.msg_no_internet))
            return
        }

        viewModel?.getTruckRegistrationInfoList()
            ?.observe(requireActivity(), observeTruckRegistrationInfoList)
    }

    private val observeTruckRegistrationInfoList =
        Observer<DataBound<MutableList<TruckRegistrationInfo>>> {
            if (it == null) {
                DebugLog.e("ApiMessage is null")
                return@Observer
            }

            it.let { dataBound ->
                when (dataBound) {
                    is DataBound.Success -> {
                        progressBar.visibility = View.GONE
                        val data = dataBound.data
                        if (data.size > 0) {
                            showRegistrationData(data[0])
                        } else {
                            showRegistrationData()
                        }
                    }
                    is DataBound.Error -> {
                        progressBar.visibility = View.GONE
                        showMessageView(getString(R.string.error_something_went_wrong))
                    }
                    is DataBound.Loading -> {
                        progressBar.visibility = View.VISIBLE
                    }
                }
            }
        }

    private fun showMessageView(message: String) {
        emptyView.visibility = View.VISIBLE
        messageTitle.text = message
    }

    private fun showRegistrationData(truckRegistrationInfo: TruckRegistrationInfo? = null) {
        saveButton.tag = truckRegistrationInfo
        scrollView.visibility = View.VISIBLE
        registrationPeriodEditText.setText(truckRegistrationInfo?.period)
        amountEditText.setText(truckRegistrationInfo?.amount?.toString())
        descriptionEditText.setText(truckRegistrationInfo?.details)
    }

    private fun onSaveClicked() {
        val truckRegistrationInfo = getValidatedTruckRegistrationInfo()
        if (truckRegistrationInfo == null) {
            DebugLog.e("TruckRegistrationInfo found null")
            return
        }

        saveTruckRegistrationInfo(truckRegistrationInfo)
    }

    private fun getValidatedTruckRegistrationInfo(): TruckRegistrationInfo? {
        val registrationPeriod = registrationPeriodEditText.text.toString().trim()
        if (registrationPeriod.isNullOrBlank()) {
            registrationPeriodEditText.error = getString(R.string.msg_enter_registration_period)
            registrationPeriodEditText.requestFocus()
            return null
        }

        val amount = amountEditText.text.toString().trim()
        if (amount.isNullOrBlank()) {
            amountEditText.error = getString(R.string.msg_enter_registration_amount)
            amountEditText.requestFocus()
            return null
        }

        val description = descriptionEditText.text.toString().trim()
        if (description.isNullOrBlank()) {
            descriptionEditText.error = getString(R.string.msg_enter_description)
            descriptionEditText.requestFocus()
            return null
        }

        val truckRegistrationInfo: TruckRegistrationInfo =
            if (saveButton.tag is TruckRegistrationInfo) {
                saveButton.tag as TruckRegistrationInfo
            } else {
                TruckRegistrationInfo()
            }

        truckRegistrationInfo.details = description
        truckRegistrationInfo.amount = amount as Int
        truckRegistrationInfo.period = registrationPeriod

        return truckRegistrationInfo

    }

    private fun saveTruckRegistrationInfo(truckRegistrationInfo: TruckRegistrationInfo) {
        if (!isInternetAvailable()) {
            showMessage(getString(R.string.msg_no_internet))
            return
        }

        val id = truckRegistrationInfo.id
        if (id.isNullOrBlank()) {
            viewModel?.addTruckRegistrationInfo(truckRegistrationInfo)
                ?.observe(viewLifecycleOwner, observeSaveTruckDetails)
        } else {
            viewModel?.updateTruckRegistrationInfo(id, truckRegistrationInfo)
                ?.observe(viewLifecycleOwner, observeSaveTruckDetails)
        }
    }

    private val observeSaveTruckDetails = Observer<DataBound<ApiMessage>> {
        if (it == null) {
            DebugLog.e("ApiMessage is null")
            return@Observer
        }

        it.let { dataBound ->
            when (dataBound) {
                is DataBound.Success -> {
                    saveButton.isEnabled = true
                    progressBar.visibility = View.GONE
                    showMessage(getString(R.string.msg_truck_registration_details_saved_successfully))
                }
                is DataBound.Error -> {
                    saveButton.isEnabled = true
                    progressBar.visibility = View.GONE
                    showMessage(getString(R.string.error_something_went_wrong))
                }
                is DataBound.Loading -> {
                    progressBar.visibility = View.VISIBLE
                    saveButton.isEnabled = false
                }
            }
        }
    }
}