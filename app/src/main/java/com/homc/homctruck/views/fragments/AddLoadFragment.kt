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
import com.homc.homctruck.data.models.ApiMessage
import com.homc.homctruck.data.models.Load
import com.homc.homctruck.data.repositories.LoadRepository
import com.homc.homctruck.data.sourceremote.LoadRemoteDataSource
import com.homc.homctruck.restapi.AppApiInstance
import com.homc.homctruck.restapi.DataBound
import com.homc.homctruck.utils.*
import com.homc.homctruck.viewmodels.LoadViewModel
import com.homc.homctruck.viewmodels.LoadViewModelFactory
import kotlinx.android.synthetic.main.fragment_add_load.*
import kotlinx.android.synthetic.main.fragment_add_load.expectedPickUpDateEditText
import kotlinx.android.synthetic.main.fragment_add_load.fromCityEditText
import kotlinx.android.synthetic.main.fragment_add_load.progressBar
import kotlinx.android.synthetic.main.fragment_add_load.toCityEditText
import java.net.HttpURLConnection
import java.util.*

open class AddLoadFragment : BaseAppFragment() {

    protected var viewModel: LoadViewModel? = null

    private var isFromCitySelected = true
    protected var startMillis: Long? = null
    var isDirty = false

    private val calculateAmountChangeListener = object : TextWatcher {
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

        }

        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            isDirty = true
        }

        override fun afterTextChanged(s: Editable?) {
            val rateString = ratePerTonEditText.text.toString().trim()
            val rate = if (rateString.isNullOrBlank()) {
                0.0F
            } else {
                rateString.toFloat()
            }
            val totalLoadInTonsString = totalLoadInTonsEditText.text.toString().trim()
            val totalLoadInTons = if (totalLoadInTonsString.isNullOrBlank()) {
                0.0F
            } else {
                totalLoadInTonsString.toFloat()
            }
            val total = rate * totalLoadInTons
            totalAmountEditText.setText(String.format("%.2f", total))
        }
    }

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
        return inflater.inflate(R.layout.fragment_add_load, container, false)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setToolBarTitle(getString(R.string.menu_add_load))
        initViewModel()
    }

    private fun initViewModel() {
        val repository =
            LoadRepository(LoadRemoteDataSource(AppApiInstance.api(getAuthToken(requireActivity()))))
        val viewModelFactory = LoadViewModelFactory(requireActivity().application, repository)
        viewModel = ViewModelProvider(this, viewModelFactory)[LoadViewModel::class.java]
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView()
    }

    private fun initView() {
        goodsNameEditText.addTextChangedListener(onTextChangeListener)
        materialTypeDropDown.addTextChangedListener(onTextChangeListener)
        fromCityEditText.addTextChangedListener(onTextChangeListener)
        toCityEditText.addTextChangedListener(onTextChangeListener)
        expectedPickUpDateEditText.addTextChangedListener(onTextChangeListener)
        truckTypeDropDown.addTextChangedListener(onTextChangeListener)
        ratePerTonEditText.addTextChangedListener(calculateAmountChangeListener)
        totalLoadInTonsEditText.addTextChangedListener(calculateAmountChangeListener)
        totalAmountEditText.addTextChangedListener(onTextChangeListener)
        transitDaysEditText.addTextChangedListener(onTextChangeListener)
        saveButton.setOnClickListener { onSaveClicked() }
        showMaterialTypes()
        showTruckTypes()
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
                    startMillis = getMillis(year, monthOfYear, dayOfMonth)
                    DebugLog.v("startMillis $startMillis")
                    expectedPickUpDateEditText.setText(formatDateForDisplay(startMillis ?: 0))
                },
                Calendar.getInstance()[Calendar.YEAR],
                Calendar.getInstance()[Calendar.MONTH],
                Calendar.getInstance()[Calendar.DAY_OF_MONTH]
            )
            datePickerDialog.datePicker.minDate = Calendar.getInstance().timeInMillis
            datePickerDialog.show()
        }
    }

    protected open fun showMaterialTypes() {
        val items = resources.getStringArray(R.array.load_type)
        val adapter = ArrayAdapter(requireContext(), R.layout.item_drop_down, items)
        materialTypeDropDown.setAdapter(adapter)
    }

    protected open fun showTruckTypes() {
        val items = resources.getStringArray(R.array.truck_types)
        val adapter = ArrayAdapter(requireContext(), R.layout.item_drop_down, items)
        truckTypeDropDown.setAdapter(adapter)
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

        saveLoadDetails(truckRoute)
    }

    private fun getValidatedTruckRouteData(): Load? {
        val goodsName = goodsNameEditText.text.toString().trim()
        if (goodsName.isNullOrBlank()) {
            goodsNameEditText.error = getString(R.string.msg_enter_goods_name)
            goodsNameEditText.requestFocus()
            return null
        }

        val materialType = materialTypeDropDown.text.toString().trim()
        if (materialType.isNullOrBlank()) {
            showMessage(getString(R.string.msg_select_material_ype))
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
            showMessage(getString(R.string.msg_select_expected_pickup_date))
            return null
        }

        val truckType = truckTypeDropDown.text.toString().trim()
        if (truckType.isNullOrBlank()) {
            showMessage(getString(R.string.msg_select_truck_type))
            return null
        }

        val perTonRate = ratePerTonEditText.text.toString().trim()
        if (perTonRate.isNullOrBlank()) {
            ratePerTonEditText.error = getString(R.string.msg_enter_rate_per_ton)
            ratePerTonEditText.requestFocus()
            return null
        }

        val totalLoadInTons = totalLoadInTonsEditText.text.toString().trim()
        if (totalLoadInTons.isNullOrBlank()) {
            totalLoadInTonsEditText.error = getString(R.string.msg_enter_total_load_in_tons)
            totalLoadInTonsEditText.requestFocus()
            return null
        }

        val totalAmount = totalAmountEditText.text.toString().trim()
        if (totalAmount.isNullOrBlank()) {
            showMessage(getString(R.string.msg_unable_to_calculate_total_amount))
            return null
        }

        val transitDays = transitDaysEditText.text.toString().trim()
        if (transitDays.isNullOrBlank()) {
            transitDaysEditText.error = getString(R.string.msg_enter_transit_days)
            transitDaysEditText.requestFocus()
            return null
        }

        val load = Load()
        load.nameOfGoods = goodsName
        load.typeOfMaterial = materialType
        load.fromCity = fromCity
        load.toCity = toCity
        load.expectedPickUpDate = startMillis
        load.typeOfTruck = truckType
        load.perTonRate = String.format("%.2f", perTonRate.toFloat()).toFloat()
        load.totalLoadInTons = String.format("%.2f", totalLoadInTons.toFloat()).toFloat()
        load.totalAmount = String.format("%.2f", totalAmount.toFloat()).toFloat()
        load.transitDaysForTruck = transitDays.toInt()
        return load
    }

    protected open fun saveLoadDetails(load: Load) {
        if (!isInternetAvailable()) {
            showMessage(getString(R.string.msg_no_internet))
            return
        }

        viewModel?.addNewLoad(load)
            ?.observe(viewLifecycleOwner, observeSaveLoadDetails)
    }

    protected val observeSaveLoadDetails = Observer<DataBound<ApiMessage>> {
        if (it == null) {
            DebugLog.e("ApiMessage is null")
            return@Observer
        }

        it.let { dataBound ->
            when (dataBound) {
                is DataBound.Success -> {
                    saveButton.isEnabled = true
                    progressBar.visibility = View.GONE
                    showMessage(getString(R.string.msg_load_details_saved_successfully))
                    isDirty = false
                    requireActivity().onBackPressed()
                }
                is DataBound.Error -> {
                    saveButton.isEnabled = true
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