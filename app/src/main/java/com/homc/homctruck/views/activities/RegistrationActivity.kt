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
import com.homc.homctruck.data.models.*
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
import kotlinx.android.synthetic.main.message_view.*
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
        getTruckRegistrationInfo()
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
    }

    private fun getTruckRegistrationInfo() {
        if (!isInternetAvailable()) {
            showMessage(getString(R.string.msg_no_internet))
            return
        }

        viewModel?.getTruckRegistrationInfoList()
            ?.observe(this, observeTruckRegistrationInfoList)
    }

    private val observeTruckRegistrationInfoList = Observer<DataBound<MutableList<TruckRegistrationInfo>>> {
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
                        showTruckData(data[0])
                    } else {
                        showMessageView(getString(R.string.msg_truck_registration_not_available))
                    }
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

    private fun showTruckData(truckRegistrationInfo: TruckRegistrationInfo) {
        infoTextView.visibility = View.VISIBLE
        truckNumberView.visibility = View.VISIBLE
        truckNumberTextView.visibility = View.VISIBLE
        registrationPeriodText.visibility = View.VISIBLE
        registrationPeriodTextView.visibility = View.VISIBLE
        payButton.visibility = View.VISIBLE

        truckNumberTextView.text = truck?.truckNumber?.toUpperCase()
        registrationPeriodTextView.text = truckRegistrationInfo.period
        infoTextView.text = truckRegistrationInfo.details
        payButton.text = getString(R.string.label_pay_x_rs, truckRegistrationInfo.amount)
        payButton.setOnClickListener {
            onPayClick(truckRegistrationInfo)
        }
    }

    private fun onPayClick(truckRegistrationInfo: TruckRegistrationInfo) {
        payButton.tag = "${truckRegistrationInfo.amount}.00"
        try {
            val uri: Uri = Uri.Builder().scheme("upi").authority("pay")
                .appendQueryParameter("pa", getString(R.string.payee_upi))
                .appendQueryParameter("pn", getString(R.string.payee_name))
                .appendQueryParameter("mc", getString(R.string.payee_merchant_code))
                .appendQueryParameter("tr", getString(R.string.placeholder_x_dash_y_dash_z,
                    truck?.id, truck?.truckNumber, truckRegistrationInfo.referenceDate.toString()))
                .appendQueryParameter("tn", getString(R.string.label_truck_registration))
                .appendQueryParameter("am", "${truckRegistrationInfo.amount}.00")
                .appendQueryParameter("cu", getString(R.string.payee_money_currency_code))
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
        val amount = payButton.tag as? String
        DebugLog.v("Transaction Response: ${Gson().toJson(response)}")
        return with(getMapFromQuery(response)) {
            TransactionDetails(
                transactionId = get("txnId"),
                responseCode = get("responseCode"),
                approvalRefNo = get("ApprovalRefNo"),
                transactionRefId = get("txnRef"),
                amount = amount,
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
        showMessage(getString(R.string.msg_payment_canceled))
    }

    private fun onTransactionCompleted(transactionDetails: TransactionDetails) {
        when (transactionDetails.transactionStatus) {
            TransactionStatus.SUCCESS -> {
                showMessage(getString(R.string.msg_payment_successful))
                truck?.transactionDetails = transactionDetails
                truck?.transactionStatus = Truck.TRANSACTION_STATUS_SUCCESS
                saveTruckDetails()
                DebugLog.v("TransactionDetails: ${Gson().toJson(transactionDetails)}")
            }
            TransactionStatus.SUBMITTED -> {
                showMessage(getString(R.string.msg_payment_submitted))
                truck?.transactionDetails = transactionDetails
                truck?.transactionStatus = Truck.TRANSACTION_STATUS_SUBMITTED
                saveTruckDetails(true)
                DebugLog.v("TransactionDetails: ${Gson().toJson(transactionDetails)}")
            }
            TransactionStatus.FAILURE -> {
                showMessage(getString(R.string.msg_payment_failure))
                DebugLog.v("TransactionDetails: ${Gson().toJson(transactionDetails)}")
            }
        }
        showPaymentDetails(transactionDetails)
    }

    private fun showPaymentDetails(transactionDetails: TransactionDetails) {
        payButton.visibility = View.GONE
        statusImageView.visibility = View.VISIBLE
        statusTextView.visibility = View.VISIBLE
        when (transactionDetails.transactionStatus) {
            TransactionStatus.SUCCESS -> {
                statusImageView.setImageResource(R.drawable.ic_payment_success)
                statusTextView.text = getString(R.string.msg_payment_successful)
            }
            TransactionStatus.SUBMITTED -> {
                statusImageView.setImageResource(R.drawable.ic_payment_submitted)
                statusTextView.text = getString(R.string.msg_payment_submitted)
            }
            TransactionStatus.FAILURE -> {
                statusImageView.setImageResource(R.drawable.ic_payment_failure)
                statusTextView.text = getString(R.string.msg_payment_failure)
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