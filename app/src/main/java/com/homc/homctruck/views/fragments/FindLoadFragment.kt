package com.homc.homctruck.views.fragments

import android.app.Activity
import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.DatePicker
import android.widget.TextView
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.google.android.gms.common.GooglePlayServicesNotAvailableException
import com.google.android.gms.common.GooglePlayServicesRepairableException
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.model.TypeFilter
import com.google.android.libraries.places.widget.Autocomplete
import com.google.android.libraries.places.widget.AutocompleteActivity
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode
import com.google.gson.Gson
import com.homc.homctruck.R
import com.homc.homctruck.data.models.Load
import com.homc.homctruck.di.DaggerAppComponent
import com.homc.homctruck.di.modules.AppModule
import com.homc.homctruck.di.modules.ViewModelModule
import com.homc.homctruck.restapi.DataBound
import com.homc.homctruck.utils.DebugLog
import com.homc.homctruck.utils.formatDateForDisplay
import com.homc.homctruck.utils.isInternetAvailable
import com.homc.homctruck.utils.setColorsAndCombineStrings
import com.homc.homctruck.viewmodels.LoadViewModel
import com.homc.homctruck.views.adapters.FindLoadListAdapter
import kotlinx.android.synthetic.main.fragment_find_load.*
import java.net.HttpURLConnection
import java.util.*
import javax.inject.Inject

class FindLoadFragment : BaseAppFragment() {

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory
    private var viewModel: LoadViewModel? = null
    private var loadAdapter: FindLoadListAdapter? = null

    private var startMillis: Long? = null
    private var isFromCitySelected: Boolean = false
    private var isFilterApplied: Boolean = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_find_load, container, false)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initViewModel()
    }

    private fun initViewModel() {
        DaggerAppComponent.builder().viewModelModule(ViewModelModule())
            .appModule(AppModule(requireActivity().application)).build().inject(this)
        viewModel = ViewModelProvider(this, viewModelFactory)[LoadViewModel::class.java]
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setToolBarTitle(getString(R.string.menu_find_load))
        showFilterLayout()
    }

    private fun showFilterLayout() {
        editFilterButton.setImageResource(R.drawable.ic_close_black)
        titleTextView1.visibility = View.GONE
        titleTextView2.visibility = View.GONE
        emptyView.visibility = View.GONE
        recyclerview.visibility = View.GONE
        if (isFilterApplied) {
            editFilterButton.visibility = View.VISIBLE
        } else {
            editFilterButton.visibility = View.GONE
        }
        editFilterButton.setOnClickListener {
            if (loadAdapter?.itemCount ?: 0 == 0) {
                showMessageView(getString(R.string.msg_loads_not_added_yet))
            } else {
                hideMessageView()
            }
            showDataLayout()
        }

        backView.visibility = View.VISIBLE
        fromCityTextField.visibility = View.VISIBLE
        toCityTextField.visibility = View.VISIBLE
        expectedPickUpDateTextField.visibility = View.VISIBLE
        applyButton.visibility = View.VISIBLE

        applyButton.setOnClickListener {
            onApplyButtonClick()
        }

        fromCityEditText.setOnClickListener {
            isFromCitySelected = true
            searchCity()
        }
        toCityEditText.setOnClickListener {
            isFromCitySelected = false
            searchCity()
        }
        expectedPickUpDateEditText.setOnClickListener {
            val datePickerDialog = DatePickerDialog(
                requireActivity(),
                { view: DatePicker?, year: Int, monthOfYear: Int, dayOfMonth: Int ->
                    val calendar = Calendar.getInstance()
                    calendar[Calendar.YEAR] = year
                    calendar[Calendar.MONTH] = monthOfYear
                    calendar[Calendar.DAY_OF_MONTH] = dayOfMonth
                    startMillis = calendar.timeInMillis
                    expectedPickUpDateEditText.setText(formatDateForDisplay(calendar.timeInMillis))
                },
                Calendar.getInstance()[Calendar.YEAR],
                Calendar.getInstance()[Calendar.MONTH],
                Calendar.getInstance()[Calendar.DAY_OF_MONTH]
            )
            datePickerDialog.datePicker.minDate = Calendar.getInstance().timeInMillis
            datePickerDialog.show()
        }
    }

    private fun onApplyButtonClick() {

        val fromPlace = fromCityEditText.text.toString()
        if (fromPlace.isNullOrBlank()) {
            showMessage(getString(R.string.msg_select_from_city))
            return
        }

        val toPlace = toCityEditText.text.toString()
        if (toPlace.isNullOrBlank()) {
            showMessage(getString(R.string.msg_select_to_city))
            return
        }

        if (startMillis == null) {
            showMessage(getString(R.string.msg_select_expected_pickup_date))
            return
        }

        if (!isInternetAvailable()) {
            showMessage(getString(R.string.msg_no_internet))
            return
        }

        viewModel?.findLoadList(fromPlace, toPlace, startMillis ?: 0)
            ?.observe(viewLifecycleOwner, observeLoadList(fromPlace, toPlace))
    }

    private fun observeLoadList(fromCity: String, toCity: String) =
        Observer<DataBound<MutableList<Load>>> {
            if (it == null) {
                DebugLog.e("ApiMessage is null")
                return@Observer
            }

            it.let { dataBound ->
                when (dataBound) {
                    is DataBound.Success -> {
                        swipeRefreshLayout.isRefreshing = false
                        progressBar.visibility = View.GONE
                        showDataLayout()
                        setColorsAndCombineStrings(
                            titleTextView1,
                            getString(R.string.label_location),
                            getString(R.string.placeholder_x_to_y, fromCity, toCity)
                        )
                        setColorsAndCombineStrings(
                            titleTextView2,
                            getString(R.string.label_expected_pickup_date),
                            formatDateForDisplay(startMillis ?: 0)
                        )
                        showData(dataBound.data)
                    }
                    is DataBound.Error -> {
                        swipeRefreshLayout.isRefreshing = false
                        progressBar.visibility = View.GONE
                        if (dataBound.code == HttpURLConnection.HTTP_NOT_FOUND) {
                            showMessage(getString(R.string.error_something_went_wrong))
                        } else {
                            DebugLog.e("Error: ${dataBound.error}")
                            showMessage("${dataBound.error}")
                        }
                    }
                    is DataBound.Loading -> {
                        swipeRefreshLayout.isRefreshing = false
                        progressBar.visibility = View.VISIBLE
                    }
                }
            }
        }

    private fun showData(data: MutableList<Load>) {
        loadAdapter = FindLoadListAdapter(data as MutableList<Any>)
        recyclerview.adapter = loadAdapter

        if (loadAdapter?.itemCount ?: 0 <= 0) {
            showMessageView(getString(R.string.msg_data_not_found))
        } else {
            hideMessageView()
        }
    }

    private fun showMessageView(message: String) {
        emptyView.visibility = View.VISIBLE
        val errorTitle = emptyView.findViewById<TextView>(R.id.messageTitle)
        errorTitle?.text = message
    }

    private fun hideMessageView() {
        emptyView?.visibility = View.GONE
    }

    private fun searchCity() {
        val instance = GoogleApiAvailability.getInstance()
        val code = instance.isGooglePlayServicesAvailable(activity)
        if (code == ConnectionResult.SUCCESS) {
            searchLocation()
        } else if (instance.isUserResolvableError(code)) {
            instance.showErrorDialogFragment(activity, code, 1) { dialog ->
                dialog.cancel()
                requireActivity().finish()
            }
        }
    }

    private fun searchLocation() {
        try {
            val fields = listOf(Place.Field.ID, Place.Field.NAME)
            val intent = Autocomplete.IntentBuilder(AutocompleteActivityMode.OVERLAY, fields)
                .setTypeFilter(TypeFilter.CITIES)
                .build(requireActivity())
            startActivityForResult(intent, AddLoadFragment.REQUEST_CODE_PLACE_AUTOCOMPLETE)
        } catch (e: GooglePlayServicesRepairableException) {
            DebugLog.e("Google Play Services Repairable")
        } catch (e: GooglePlayServicesNotAvailableException) {
            DebugLog.e("Google Play Service Not Available")
        }
    }

    private fun showDataLayout() {
        editFilterButton.setImageResource(R.drawable.ic_arrow_down_black)
        titleTextView1.visibility = View.VISIBLE
        titleTextView2.visibility = View.VISIBLE
        recyclerview.visibility = View.VISIBLE
        if (isFilterApplied) {
            editFilterButton.visibility = View.VISIBLE
        } else {
            editFilterButton.visibility = View.GONE
        }
        editFilterButton.setOnClickListener {
            showFilterLayout()
        }

        backView.visibility = View.GONE
        fromCityTextField.visibility = View.GONE
        toCityTextField.visibility = View.GONE
        expectedPickUpDateTextField.visibility = View.GONE
        applyButton.visibility = View.GONE
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == AddLoadFragment.REQUEST_CODE_PLACE_AUTOCOMPLETE) {
            when (resultCode) {
                Activity.RESULT_OK -> {
                    val place = data?.let { Autocomplete.getPlaceFromIntent(it) };
                    if (isFromCitySelected) {
                        fromCityEditText.setText(place?.name)
                    } else {
                        toCityEditText.setText(place?.name)
                    }
                }
                AutocompleteActivity.RESULT_ERROR -> {
                    val status = data?.let { Autocomplete.getStatusFromIntent(it) }
                    DebugLog.v("Place: " + Gson().toJson(status))
                }
                Activity.RESULT_CANCELED -> {
                    DebugLog.v("Place: user cancel the change and return to fragment")
                }
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data)
        }
    }
}