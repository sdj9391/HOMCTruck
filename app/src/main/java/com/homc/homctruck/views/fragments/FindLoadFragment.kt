package com.homc.homctruck.views.fragments

import android.app.Activity
import android.app.DatePickerDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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
import com.homc.homctruck.data.models.Load
import com.homc.homctruck.data.models.User
import com.homc.homctruck.data.models.getName
import com.homc.homctruck.data.repositories.AuthenticationRepository
import com.homc.homctruck.data.repositories.LoadRepository
import com.homc.homctruck.data.sourceremote.AuthenticationRemoteDataSource
import com.homc.homctruck.data.sourceremote.LoadRemoteDataSource
import com.homc.homctruck.restapi.AppApiInstance
import com.homc.homctruck.restapi.DataBound
import com.homc.homctruck.utils.*
import com.homc.homctruck.viewmodels.AuthenticationViewModel
import com.homc.homctruck.viewmodels.AuthenticationViewModelFactory
import com.homc.homctruck.viewmodels.LoadViewModel
import com.homc.homctruck.viewmodels.LoadViewModelFactory
import com.homc.homctruck.views.adapters.FindLoadListAdapter
import kotlinx.android.synthetic.main.fragment_find_load.*
import kotlinx.android.synthetic.main.item_search_view.view.*
import java.net.HttpURLConnection
import java.util.*
import java.util.concurrent.TimeUnit

class FindLoadFragment : BaseAppFragment() {

    private var viewModel: LoadViewModel? = null
    private var viewModelUser: AuthenticationViewModel? = null
    private var loadAdapter: FindLoadListAdapter? = null

    private var startMillis: Long? = null
    private var isFromCitySelected: Boolean = false
    private var isFilterApplied: Boolean = false

    private val onEnquiryClickListener = View.OnClickListener {
        if (!canHaveFeatureAccess((requireContext()))) return@OnClickListener

        val ownerId = it.tag
        if (ownerId !is String) {
            return@OnClickListener
        }

        getUserDetails(ownerId)
    }

    private val textWatcher: TextWatcher? = object : TextWatcher {
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            val length = s?.length ?: 0
            if (length > 2 || length == 0) {
                onApplyButtonClick(s.toString())
            }
        }

        override fun afterTextChanged(s: android.text.Editable?) {}
    }

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
        val repository =
            LoadRepository(LoadRemoteDataSource(AppApiInstance.api(getAuthToken(requireActivity()))))
        val viewModelFactory = LoadViewModelFactory(requireActivity().application, repository)
        viewModel = ViewModelProvider(this, viewModelFactory)[LoadViewModel::class.java]
        val repositoryAuth = AuthenticationRepository(
            AuthenticationRemoteDataSource(
                AppApiInstance.api(getAuthToken(requireActivity())),
                AppApiInstance.apiPostal(getAuthToken(requireActivity()))
            )
        )
        val viewModelFactoryAuth =
            AuthenticationViewModelFactory(requireActivity().application, repositoryAuth)
        viewModelUser =
            ViewModelProvider(this, viewModelFactoryAuth)[AuthenticationViewModel::class.java]
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setToolBarTitle(getString(R.string.menu_find_load))
        recyclerview.layoutManager = LinearLayoutManager(activity, RecyclerView.VERTICAL, false)
        val offsetPx = resources.getDimension(R.dimen.default_space)
        val topOffsetDecoration = TopAndBottomOffset(offsetPx.toInt(), offsetPx.toInt())
        recyclerview.addItemDecoration(topOffsetDecoration)
        showFilterLayout()
        searchView.searchEditText.hint = getString(R.string.msg_search_load_by_material_type)
        searchView.searchEditText.addTextChangedListener(textWatcher)
    }

    private fun showFilterLayout() {
        editFilterButton.setImageResource(R.drawable.ic_close_black)
        titleTextView1.visibility = View.GONE
        titleTextView2.visibility = View.GONE
        emptyView.visibility = View.GONE
        recyclerview.visibility = View.GONE
        searchView.visibility = View.GONE
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
            searchView.searchEditText.text = null
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
                    startMillis = getMillis(year, monthOfYear, dayOfMonth)
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

    private fun onApplyButtonClick(materialKeyword: String? = null) {
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

        viewModel?.findLoadList(
            fromPlace, toPlace,
            ((startMillis ?: 0) + TimeUnit.DAYS.toMillis(10)), materialKeyword
        )?.observe(viewLifecycleOwner, observeLoadList(fromPlace, toPlace))
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
                        isFilterApplied = true
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
                            DebugLog.e("Error: ${dataBound.message}")
                            showMessage("${dataBound.message}")
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
        loadAdapter?.onMoreClickListener = onEnquiryClickListener
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
        searchView.visibility = View.VISIBLE
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

    private fun getUserDetails(ownerId: String) {
        viewModelUser?.getUserDetails(ownerId)
            ?.observe(viewLifecycleOwner, observeGetUserDetails)
    }

    private val observeGetUserDetails = Observer<DataBound<User>> {
        if (it == null) {
            DebugLog.e("ApiMessage is null")
            return@Observer
        }

        it.let { dataBound ->
            when (dataBound) {
                is DataBound.Success -> {
                    progressBar.visibility = View.GONE
                    val user = dataBound.data
                    val name = user.getName()
                    val mobileNumber = user.mobileNumber

                    if (mobileNumber.isNullOrBlank()) {
                        return@Observer
                    }

                    val msg = if (name.isNullOrBlank()) {
                        mobileNumber
                    } else {
                        "$name ($mobileNumber)"
                    }

                    showConfirmDialog(
                        requireContext(), getString(R.string.msg_call_x, msg),
                        { _, _ -> callClicked(mobileNumber) }, null,
                        getString(R.string.label_call), getString(R.string.label_cancel)
                    )

                }
                is DataBound.Error -> {
                    DebugLog.w("Error: ${dataBound.message}")
                    progressBar.visibility = View.GONE
                    showMessageView(getString(R.string.error_something_went_wrong))
                }
                is DataBound.Loading -> {
                    progressBar.visibility = View.VISIBLE
                }
            }
        }
    }

    private fun callClicked(mobileNumber: String) {
        val intent = Intent()
        intent.action = Intent.ACTION_DIAL
        intent.data = Uri.parse("tel: $mobileNumber")
        startActivity(intent)
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