package com.homc.homctruck.views.fragments

import android.app.Activity
import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.DatePicker
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.Navigation
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
import com.homc.homctruck.data.models.*
import com.homc.homctruck.di.DaggerAppComponent
import com.homc.homctruck.di.modules.AppModule
import com.homc.homctruck.di.modules.ViewModelModule
import com.homc.homctruck.restapi.DataBound
import com.homc.homctruck.utils.DebugLog
import com.homc.homctruck.utils.formatDateForDisplay
import com.homc.homctruck.utils.isInternetAvailable
import com.homc.homctruck.viewmodels.TruckViewModel
import kotlinx.android.synthetic.main.fragment_add_truck_route.*
import java.net.HttpURLConnection
import java.util.*
import javax.inject.Inject

open class AddTruckRouteFragment : BaseAppFragment() {

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory
    protected var viewModel: TruckViewModel? = null

    private var isFromCitySelected = true
    protected var startMillis: Long? = null
    protected var endMillis: Long? = null
    var isDirty = false
    private var trucks: MutableMap<String, Truck>? = null
    private var navigationController: NavController? = null

    private val onTextChangeListener = object : TextWatcher {
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

        }

        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            isDirty = true
        }

        override fun afterTextChanged(s: Editable?) {

        }
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

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_add_truck_route, container, false)
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
        setToolBarTitle(getString(R.string.menu_add_truck_route))
        navigationController = Navigation.findNavController(requireView())
        initView()
        getTruckData()
    }

    private fun initView() {
        truckNumberDropDown.addTextChangedListener(onTextChangeListener)
        fromCityEditText.addTextChangedListener(onTextChangeListener)
        toCityEditText.addTextChangedListener(onTextChangeListener)
        startJourneyDateEditText.addTextChangedListener(onTextChangeListener)
        endJourneyDateEditText.addTextChangedListener(onTextChangeListener)
        saveButton.setOnClickListener { onSaveClicked() }

        fromCityEditText.setOnClickListener {
            isFromCitySelected = true
            searchCity()
        }
        toCityEditText.setOnClickListener {
            isFromCitySelected = false
            searchCity()
        }
        startJourneyDateEditText.setOnClickListener {
            val datePickerDialog = DatePickerDialog(
                requireActivity(),
                { view: DatePicker?, year: Int, monthOfYear: Int, dayOfMonth: Int ->
                    val calendar = Calendar.getInstance()
                    calendar[Calendar.YEAR] = year
                    calendar[Calendar.MONTH] = monthOfYear
                    calendar[Calendar.DAY_OF_MONTH] = dayOfMonth
                    startMillis = calendar.timeInMillis
                    startJourneyDateEditText.setText(formatDateForDisplay(calendar.timeInMillis))
                },
                Calendar.getInstance()[Calendar.YEAR],
                Calendar.getInstance()[Calendar.MONTH],
                Calendar.getInstance()[Calendar.DAY_OF_MONTH]
            )
            datePickerDialog.datePicker.minDate = Calendar.getInstance().timeInMillis
            datePickerDialog.show()
        }
        endJourneyDateEditText.setOnClickListener {
            val datePickerDialog = DatePickerDialog(
                requireActivity(),
                { view: DatePicker?, year: Int, monthOfYear: Int, dayOfMonth: Int ->
                    val calendar = Calendar.getInstance()
                    calendar[Calendar.YEAR] = year
                    calendar[Calendar.MONTH] = monthOfYear
                    calendar[Calendar.DAY_OF_MONTH] = dayOfMonth
                    endMillis = calendar.timeInMillis
                    endJourneyDateEditText.setText(formatDateForDisplay(calendar.timeInMillis))
                },
                Calendar.getInstance()[Calendar.YEAR],
                Calendar.getInstance()[Calendar.MONTH],
                Calendar.getInstance()[Calendar.DAY_OF_MONTH]
            )
            datePickerDialog.datePicker.minDate = Calendar.getInstance().timeInMillis
            datePickerDialog.show()
        }
    }

    private fun getTruckData() {
        if (!isInternetAvailable()) {
            showMessage(getString(R.string.msg_no_internet))
            return
        }

        viewModel?.getMyTruckList()
            ?.observe(viewLifecycleOwner, observeTruckList)
    }

    private var observeTruckList = Observer<DataBound<MutableList<Truck>>> {
        if (it == null) {
            DebugLog.e("ApiMessage is null")
            return@Observer
        }

        it.let { dataBound ->
            when (dataBound) {
                is DataBound.Success -> {
                    truckProgressBar.visibility = View.GONE
                    showTruckData(dataBound.data)
                }
                is DataBound.Error -> {
                    truckProgressBar.visibility = View.GONE
                    if (dataBound.code == HttpURLConnection.HTTP_NOT_FOUND) {
                        showMessage(getString(R.string.error_something_went_wrong))
                    } else {
                        DebugLog.e("Error: ${dataBound.error}")
                        showMessage("${dataBound.error}")
                    }
                }
                is DataBound.Loading -> {
                    truckProgressBar.visibility = View.VISIBLE
                }
            }
        }
    }

    private fun showTruckData(data: MutableList<Truck>) {
        if (data.isNullOrEmpty()) {
            showToastMessage(getString(R.string.mag_add_truck_route))
            navigationController?.navigate(R.id.action_addTruckRouteFragment_to_addTruckFragment)
            return
        }

        trucks = mutableMapOf()
        val items: MutableSet<String> = mutableSetOf()
        data.forEach { truck ->
            truck.truckNumber?.let { truckNumber ->
                trucks?.put(truckNumber, truck)
                items.add(truckNumber)
            }
        }

        val adapter = ArrayAdapter(requireContext(), R.layout.item_drop_down, items.toList())
        truckNumberDropDown.setAdapter(adapter)
    }

    private fun searchLocation() {
        try {
            val fields = listOf(Place.Field.ID, Place.Field.NAME)
            val intent = Autocomplete.IntentBuilder(AutocompleteActivityMode.OVERLAY, fields)
                .setTypeFilter(TypeFilter.CITIES)
                .build(requireActivity())
            startActivityForResult(intent, REQUEST_CODE_PLACE_AUTOCOMPLETE)
        } catch (e: GooglePlayServicesRepairableException) {
            DebugLog.e("Google Play Services Repairable")
        } catch (e: GooglePlayServicesNotAvailableException) {
            DebugLog.e("Google Play Service Not Available")
        }
    }

    private fun setCityName(place: Place?) {
        if (place == null) {
            DebugLog.e("Selected place is empty")
            return
        }

        val location = place.name
        if (location.isNullOrBlank()) {
            DebugLog.e("Location found null")
            return
        }

        DebugLog.v("Place: " + Gson().toJson(place))
        if (isFromCitySelected) {
            fromCityEditText.setText(location)
        } else {
            toCityEditText.setText(location)
        }
    }

    private fun onSaveClicked() {
        val truckRoute = getValidatedTruckRouteData()
        if (truckRoute == null) {
            DebugLog.e("Truck Route Details found null")
            return
        }

        saveTruckRouteDetails(truckRoute)
    }

    private fun getValidatedTruckRouteData(): TruckRoute? {
        val truckNumber = truckNumberDropDown.text.toString().trim()
        if (truckNumber.isNullOrBlank()) {
            showMessage(getString(R.string.msg_select_truck_number))
            return null
        }

        val fromCity = fromCityEditText.text.toString().trim()
        if (fromCity.isNullOrBlank()) {
            showMessage(getString(R.string.msg_select_from_city))
            return null
        }

        val toCity = toCityEditText.text.toString().trim()
        if (toCity.isNullOrBlank()) {
            showMessage(getString(R.string.msg_select_to_city))
            return null
        }

        if (startMillis == null) {
            showMessage(getString(R.string.msg_select_journey_start_date))
            return null
        }

        if (endMillis == null) {
            showMessage(getString(R.string.msg_select_journey_end_date))
            return null
        }


        if (startMillis!! > endMillis!!) {
            showMessage(getString(R.string.msg_journey_end_date_greater_journey_start_date))
            return null
        }

        val truck = trucks?.get(truckNumber)
        if (truck == null) {
            showMessage(getString(R.string.error_something_went_wrong))
            return null
        }

        val truckRoute = TruckRoute()
        truckRoute.truckId = truck.id
        truckRoute.fromCity = fromCity
        truckRoute.fromCity = fromCity
        truckRoute.startJourneyDate = startMillis
        truckRoute.endJourneyDate = endMillis
        return truckRoute
    }

    protected open fun saveTruckRouteDetails(truckRoute: TruckRoute) {
        if (!isInternetAvailable()) {
            showMessage(getString(R.string.msg_no_internet))
            return
        }

        viewModel?.addNewTruckRoute(truckRoute)
            ?.observe(viewLifecycleOwner, observeSaveTruckRouteDetails)
    }

    protected val observeSaveTruckRouteDetails = Observer<DataBound<ApiMessage>> {
        if (it == null) {
            DebugLog.e("ApiMessage is null")
            return@Observer
        }

        it.let { dataBound ->
            when (dataBound) {
                is DataBound.Success -> {
                    saveButton.isEnabled = true
                    progressBar.visibility = View.GONE
                    showMessage(getString(R.string.msg_truck_route_details_saved_successfully))
                    isDirty = false
                }
                is DataBound.Error -> {
                    saveButton.isEnabled = true
                    progressBar.visibility = View.GONE
                    if (dataBound.code == HttpURLConnection.HTTP_NOT_FOUND) {
                        showMessage(getString(R.string.error_something_went_wrong))
                    } else {
                        DebugLog.e("Error: ${dataBound.error}")
                        showMessage("${dataBound.error}")
                    }
                }
                is DataBound.Loading -> {
                    progressBar.visibility = View.VISIBLE
                    saveButton.isEnabled = false
                }
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == REQUEST_CODE_PLACE_AUTOCOMPLETE) {
            when (resultCode) {
                Activity.RESULT_OK -> {
                    val place = data?.let { Autocomplete.getPlaceFromIntent(it) };
                    setCityName(place)
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

    companion object {
        const val REQUEST_CODE_PLACE_AUTOCOMPLETE = 111
    }
}