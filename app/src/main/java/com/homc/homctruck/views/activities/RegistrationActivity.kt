package com.homc.homctruck.views.activities

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.google.gson.Gson
import com.homc.homctruck.R
import com.homc.homctruck.data.models.ApiMessage
import com.homc.homctruck.data.models.TransactionDetails
import com.homc.homctruck.data.models.TransactionStatus
import com.homc.homctruck.data.models.Truck
import com.homc.homctruck.data.repositories.TruckRepository
import com.homc.homctruck.data.sourceremote.TruckRemoteDataSource
import com.homc.homctruck.restapi.AppApiInstance
import com.homc.homctruck.restapi.DataBound
import com.homc.homctruck.utils.*
import com.homc.homctruck.viewmodels.TruckViewModel
import com.homc.homctruck.viewmodels.TruckViewModelFactory
import com.homc.homctruck.views.fragments.EditTruckFragment
import com.homc.homctruck.views.fragments.MyTruckListFragment
import kotlinx.android.synthetic.main.activity_registration.*
import java.net.HttpURLConnection
import java.util.*

class RegistrationActivity : BaseAppActivity() {

    private var viewModel: TruckViewModel? = null
    private var truck: Truck? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_registration)
        initToolbar(true)
        setToolBarTitle(getString(R.string.label_truck_registration))
        getTruckDetails()
        initViewModel()
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

    private fun initViewModel() {
        val repository =
            TruckRepository(TruckRemoteDataSource(AppApiInstance.api(getAuthToken(this))))
        val viewModelFactory = TruckViewModelFactory(application, repository)
        viewModel = ViewModelProvider(this, viewModelFactory)[TruckViewModel::class.java]
    }

    private fun getTruckDetails() {
        val dataItem = TemporaryCache[EditTruckFragment.EXTRA_TRUCK_DETAILS]
        if (dataItem !is Truck) {
            DebugLog.e("Wrong instance found. Expected: ${Truck::class.java.simpleName} Found: $dataItem")
            finish()
            return
        }
        truck = dataItem
        showTruckData()
    }

    private fun showTruckData() {
        truckNumberTextView.text = truck?.truckNumber?.toUpperCase()
        registrationPeriodTextView.text = "1 Year"
        payButton.text = getString(R.string.label_pay_x_rs, "2500.00")
        payButton.setOnClickListener {
            onPayClick()
        }
    }

    private fun onPayClick() {
        try {
            val uri: Uri = Uri.Builder().scheme("upi").authority("pay")
                .appendQueryParameter("pa", "asd@upi") // Payee Vpa
                .appendQueryParameter("pn", "asd") // Payee Name
                .appendQueryParameter("mc", "asd") // Payee Merchant Code
                .appendQueryParameter("tr", "REFT12345") // Transaction Ref Id
                .appendQueryParameter("tn", "Test transaction") // Description
                .appendQueryParameter("am", "10.00") // Amount
                .appendQueryParameter("cu", "INR") // Currency
                .build()
            val intent = Intent(Intent.ACTION_VIEW)
            intent.data = uri
            if (intent.resolveActivity(packageManager) == null) {
                showMessage("No any payment app found to handle UPI payment")
            } else {
                startActivityForResult(intent, PAY_REQUEST_CODE)
            }
        } catch (e: Exception) {
            showMessage("${e.message}")
            e.printStackTrace()
            DebugLog.e(Gson().toJson(e))
        }
    }

    private fun getTransactionDetails(response: String): TransactionDetails {
        DebugLog.v("Transaction Response: ${Gson().toJson(response)}")
        return with(getMapFromQuery(response)) {
            TransactionDetails(
                transactionId = get("txnId"),
                responseCode = get("responseCode"),
                approvalRefNo = get("ApprovalRefNo"),
                transactionRefId = get("txnRef"),
                amount = "2500.00",
                transactionStatus = TransactionStatus.valueOf(
                    get("Status")?.toUpperCase(Locale.getDefault())
                        ?: TransactionStatus.FAILURE.name
                )
            )
        }
    }

    private fun getMapFromQuery(queryString: String): Map<String, String> {
        val map = mutableMapOf<String, String>()
        val keyValuePairs = queryString
            .split("&")
            .map { param ->
                param.split("=").let { Pair(it[0], it[1]) }
            }
        map.putAll(keyValuePairs)
        return map
    }

    private fun onTransactionCancelled() {
        showMessage("Payment Canceled")
    }

    private fun onTransactionCompleted(transactionDetails: TransactionDetails) {
        when (transactionDetails.transactionStatus) {
            TransactionStatus.SUCCESS -> {
                showMessage("Payment SUCCESS")
                truck?.transactionDetails = transactionDetails
                truck?.transactionStatus = Truck.TRANSACTION_STATUS_SUCCESS
                saveTruckDetails()
                DebugLog.v("TransactionDetails: ${Gson().toJson(transactionDetails)}")
            }
            TransactionStatus.SUBMITTED -> {
                showMessage("Payment SUBMITTED")
                truck?.transactionDetails = transactionDetails
                truck?.transactionStatus = Truck.TRANSACTION_STATUS_SUBMITTED
                saveTruckDetails(true)
                DebugLog.v("TransactionDetails: ${Gson().toJson(transactionDetails)}")
            }
            TransactionStatus.FAILURE -> {
                showMessage("Payment FAILURE")
                DebugLog.v("TransactionDetails: ${Gson().toJson(transactionDetails)}")
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == PAY_REQUEST_CODE) {
            if (data != null) {
                // Get Response from activity intent
                val response = data.getStringExtra("response")

                if (response == null) {
                    onTransactionCancelled()
                    DebugLog.v("Payment Response is null")
                } else {
                    infoTextView.text = response
                    runCatching {
                        // Get transactions details from response.
                        val transactionDetails = getTransactionDetails(response)

                        // Update Listener onTransactionCompleted()
                        onTransactionCompleted(transactionDetails)
                    }.getOrElse {
                        onTransactionCancelled()
                    }
                }
            } else {
                DebugLog.v("Intent Data is null. User cancelled")
                onTransactionCancelled()
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data)
        }
    }

    private fun saveTruckDetails(isSubmitted: Boolean = false) {
        if (!isInternetAvailable()) {
            showMessage(getString(R.string.msg_no_internet))
            return
        }

        val truckId = truck?.id
        if (truckId.isNullOrBlank()) {
            DebugLog.e("Truck Id found null")
            return
        }

        truck?.let {
            viewModel?.updateTruckDetails(truckId, it)
                ?.observe(this, observeSaveTruckDetails(isSubmitted))
        }
    }

    private fun observeSaveTruckDetails(isSubmitted: Boolean) = Observer<DataBound<ApiMessage>> {
        if (it == null) {
            DebugLog.e("ApiMessage is null")
            return@Observer
        }

        it.let { dataBound ->
            when (dataBound) {
                is DataBound.Success -> {
                    progressBar.visibility = View.GONE
                    if (isSubmitted) {
                        setResult(Activity.RESULT_OK)
                    } else {
                        setResult(MyTruckListFragment.RESULT_SUBMITTED)
                    }
                    finish()
                }
                is DataBound.Error -> {
                    progressBar.visibility = View.GONE
                    if (dataBound.code == HttpURLConnection.HTTP_NOT_FOUND) {
                        showMessage(getString(R.string.error_something_went_wrong))
                    } else {
                        DebugLog.e("Error: ${dataBound.message}")
                        showMessage("${dataBound.message}")
                    }
                }
                is DataBound.Retry -> {
                    if (canRetryApiCall) {
                        getAuthTokenFromFirebase(this, object : RetryListener {
                            override fun retry() {
                                initViewModel()
                                progressBar.visibility = View.GONE
                                showMessage(getString(R.string.error_something_went_wrong_try_again))
                            }
                        })
                    } else {
                        canRetryApiCall = false
                        progressBar.visibility = View.GONE
                        showMessage(getString(R.string.error_something_went_wrong))
                    }
                }
                is DataBound.Loading -> {
                    progressBar.visibility = View.VISIBLE
                }
            }
        }
    }

    companion object {
        const val PAY_REQUEST_CODE = 121
    }
}