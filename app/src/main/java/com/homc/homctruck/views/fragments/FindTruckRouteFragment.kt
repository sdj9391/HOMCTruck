package com.homc.homctruck.views.fragments

import android.app.Activity
import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.DatePicker
import android.widget.TextView
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
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
import com.homc.homctruck.data.models.TruckRoute
import com.homc.homctruck.di.DaggerAppComponent
import com.homc.homctruck.di.modules.AppModule
import com.homc.homctruck.di.modules.ViewModelModule
import com.homc.homctruck.restapi.DataBound
import com.homc.homctruck.utils.*
import com.homc.homctruck.viewmodels.TruckViewModel
import com.homc.homctruck.views.adapters.FindTruckRouteListAdapter
import kotlinx.android.synthetic.main.fragment_find_truck_route.*
import java.net.HttpURLConnection
import java.util.*
import javax.inject.Inject

class FindTruckRouteFragment : BaseAppFragment() {

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory
    private var viewModel: TruckViewModel? = null
    private var loadAdapter: FindTruckRouteListAdapter? = null

    private var startMillis: Long? = null
    private var endMillis: Long? = null
    private var isFromCitySelected: Boolean = false
    private var isFilterApplied: Boolean = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_find_truck_route, container, false)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initViewModel()
    }

    private fun initViewModel() {
        DaggerAppComponent.builder().viewModelModule(ViewModelModule())
            .appModule(AppModule(requireActivity().application)).build().inject(this)
        viewModel = ViewModelProvider(this, viewModelFactory)[TruckViewModel::class.java]
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setToolBarTitle(getString(R.string.menu_find_truck_route))
        recyclerview.layoutManager = LinearLayoutManager(activity, RecyclerView.VERTICAL, false)
        val offsetPx = resources.getDimension(R.dimen.default_space)
        val topOffsetDecoration = TopAndBottomOffset(offsetPx.toInt(), offsetPx.toInt())
        recyclerview.addItemDecoration(topOffsetDecoration)
        showFilterLayout()
    }

    private fun showFilterLayout() {
        editFilterButton.setImageResource(R.drawable.ic_close_black)
        titleTextView1.visibility = View.GONE
        titleTextView2.visibility = View.GONE
        titleTextView3.visibility = View.GONE
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
        truckTypeDropDownField.visibility = View.VISIBLE
        fromCityTextField.visibility = View.VISIBLE
        toCityTextField.visibility = View.VISIBLE
        fromDateTextField.visibility = View.VISIBLE
        toDateTextField.visibility = View.VISIBLE
        applyButton.visibility = View.VISIBLE

        applyButton.setOnClickListener {
            onApplyButtonClick()
        }

        val items = resources.getStringArray(R.array.truck_types)
        val adapter = ArrayAdapter(requireContext(), R.layout.item_drop_down, items)
        truckTypeDropDown.setAdapter(adapter)

        fromCityEditText.setOnClickListener {
            isFromCitySelected = true
            searchCity()
        }
        toCityEditText.setOnClickListener {
            isFromCitySelected = false
            searchCity()
        }
        fromDateEditText.setOnClickListener {
            val datePickerDialog = DatePickerDialog(
                requireActivity(),
                { view: DatePicker?, year: Int, monthOfYear: Int, dayOfMonth: Int ->
                    startMillis = getMillis(year, monthOfYear, dayOfMonth)
                    fromDateEditText.setText(formatDateForDisplay(startMillis ?: 0))
                },
                Calendar.getInstance()[Calendar.YEAR],
                Calendar.getInstance()[Calendar.MONTH],
                Calendar.getInstance()[Calendar.DAY_OF_MONTH]
            )
            datePickerDialog.datePicker.minDate = Calendar.getInstance().timeInMillis
            datePickerDialog.show()
        }

        toDateEditText.setOnClickListener {
            val datePickerDialog = DatePickerDialog(
                requireActivity(),
                { view: DatePicker?, year: Int, monthOfYear: Int, dayOfMonth: Int ->
                    endMillis = getMillis(year, monthOfYear, dayOfMonth)
                    toDateEditText.setText(formatDateForDisplay(endMillis ?: 0))
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

        val truckType = truckTypeDropDown.text.toString().trim()
        if (truckType.isNullOrBlank()) {
            truckTypeDropDown.error = getString(R.string.msg_select_truck_type)
            truckTypeDropDown.requestFocus()
            return
        }

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
            showMessage(getString(R.string.msg_select_journey_start_date))
            return
        }

        if (endMillis == null) {
            showMessage(getString(R.string.msg_select_journey_end_date))
            return
        }

        if (!isInternetAvailable()) {
            showMessage(getString(R.string.msg_no_internet))
            return
        }

        viewModel?.findTruckRouteList(
            truckType,
            fromPlace,
            toPlace,
            startMillis ?: 0,
            endMillis ?: 0
        )
            ?.observe(viewLifecycleOwner, observeTruckRouteList(truckType, fromPlace, toPlace))
    }

    private fun observeTruckRouteList(truckType: String, fromCity: String, toCity: String) =
        Observer<DataBound<MutableList<TruckRoute>>> {
            if (it == null) {
                DebugLog.e("ApiMessage is null")
                return@Observer
            }

            it.let { dataBound ->
                when (dataBound) {
                    is DataBound.Success -> {
                        isFilterApplied = true
                        swipeRefreshLayout.isRefreshing = false
                        progressBar.visibility = View.GONE
                        showDataLayout()
                        setColorsAndCombineStrings(
                            titleTextView1,
                            getString(R.string.label_truck_type),
                            truckType
                        )
                        setColorsAndCombineStrings(
                            titleTextView2,
                            getString(R.string.label_location),
                            getString(R.string.placeholder_x_to_y, fromCity, toCity)
                        )
                        setColorsAndCombineStrings(
                            titleTextView3,
                            getString(R.string.label_date),
                            getString(
                                R.string.placeholder_x_to_y,
                                formatDateForDisplay(startMillis ?: 0),
                                formatDateForDisplay(endMillis ?: 0)
                            )
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

    private fun showData(data: MutableList<TruckRoute>) {
        loadAdapter = FindTruckRouteListAdapter(data as MutableList<Any>)
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
        titleTextView3.visibility = View.VISIBLE
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
        fromDateTextField.visibility = View.GONE
        toDateTextField.visibility = View.GONE
        applyButton.visibility = View.GONE
        truckTypeDropDownField.visibility = View.GONE
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