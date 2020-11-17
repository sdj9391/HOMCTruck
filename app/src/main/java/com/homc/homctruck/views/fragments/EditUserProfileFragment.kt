package com.homc.homctruck.views.fragments

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.homc.homctruck.R
import com.homc.homctruck.data.models.Address
import com.homc.homctruck.data.models.ApiMessage
import com.homc.homctruck.data.models.ResponsePostalAddress
import com.homc.homctruck.data.models.User
import com.homc.homctruck.data.repositories.AuthenticationRepository
import com.homc.homctruck.data.sourceremote.AuthenticationRemoteDataSource
import com.homc.homctruck.restapi.AppApiInstance
import com.homc.homctruck.restapi.DataBound
import com.homc.homctruck.utils.*
import com.homc.homctruck.utils.account.BaseAccountManager
import com.homc.homctruck.viewmodels.AuthenticationViewModel
import com.homc.homctruck.viewmodels.AuthenticationViewModelFactory
import kotlinx.android.synthetic.main.fragment_edit_user_profile.*
import kotlinx.android.synthetic.main.fragment_edit_user_profile.progressBar
import kotlinx.android.synthetic.main.fragment_edit_user_profile.saveButton
import java.net.HttpURLConnection

class EditUserProfileFragment : BaseAppFragment() {

    private var viewModel: AuthenticationViewModel? = null
    private var isDirty = false

    private val onTextChangeListener = object : TextWatcher {
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

        }

        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            isDirty = true
        }

        override fun afterTextChanged(s: Editable?) {

        }
    }
    private val onPinCodeChangeListener = object : TextWatcher {
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

        }

        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            isDirty = true
            s?.let {
                if (it.length == 6) {
                    getPostalAddress(it.toString())
                }
            }
        }

        override fun afterTextChanged(s: Editable?) {

        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_edit_user_profile, container, false)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initViewModel()
    }

    private fun initViewModel() {
        val repository = AuthenticationRepository(
            AuthenticationRemoteDataSource(
                AppApiInstance.api(getAuthToken(requireActivity())),
                AppApiInstance.apiPostal(getAuthToken(requireActivity()))
            )
        )
        val viewModelFactory =
            AuthenticationViewModelFactory(requireActivity().application, repository)
        viewModel = ViewModelProvider(this, viewModelFactory)[AuthenticationViewModel::class.java]
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setToolBarTitle(getString(R.string.label_user_profile))
        setUserDetails()
    }

    private fun setUserDetails() {
        val user = BaseAccountManager(requireActivity()).userDetails
        if (user == null) {
            DebugLog.e("User Found null")
            return
        }

        firstNameEditText.setText(user.firstName)
        lastNameEditText.setText(user.lastName)
        mobileNumberEditText.setText(user.mobileNumber)
        emailEditText.setText(user.email)
        panCardNumberEditText.setText(user.panCardNumber)
        aadharCardNumberEditText.setText(user.aadharCardNumber)

        val address = user.address
        addressLine1EditText.setText(address?.line1)
        addressLine2EditText.setText(address?.line2)
        pinCodeEditText.setText(address?.pinCode)
        cityEditText.setText(address?.city)
        talukaEditText.setText(address?.taluka)
        districtEditText.setText(address?.district)
        stateEditText.setText(address?.state)

        firstNameEditText.addTextChangedListener(onTextChangeListener)
        lastNameEditText.addTextChangedListener(onTextChangeListener)
        panCardNumberEditText.addTextChangedListener(onTextChangeListener)
        aadharCardNumberEditText.addTextChangedListener(onTextChangeListener)
        addressLine1EditText.addTextChangedListener(onTextChangeListener)
        addressLine2EditText.addTextChangedListener(onTextChangeListener)
        cityEditText.addTextChangedListener(onTextChangeListener)
        pinCodeEditText.addTextChangedListener(onPinCodeChangeListener)

        saveButton.setOnClickListener {
            val userDetails = validateData()
            if (userDetails == null) {
                DebugLog.e("Validation failed.")
                return@setOnClickListener
            }

            checkForNewUserApiCall(userDetails)
        }
    }

    private fun validateData(): User? {
        val firstName = firstNameEditText.text.toString().trim()
        if (firstName.isNullOrBlank()) {
            firstNameEditText.error = getString(R.string.msg_enter_first_name)
            firstNameEditText.requestFocus()
            return null
        }

        val lastName = lastNameEditText.text.toString().trim()
        if (lastName.isNullOrBlank()) {
            lastNameEditText.error = getString(R.string.msg_enter_last_name)
            lastNameEditText.requestFocus()
            return null
        }

        val emailId = emailEditText.text.toString().trim()
        if (emailId.isNullOrBlank()) {
            emailEditText.error = getString(R.string.msg_enter_email_id)
            emailEditText.requestFocus()
            return null
        }

        if (!emailId.isValidEmail()) {
            emailEditText.error = getString(R.string.msg_enter_valid_email_id)
            emailEditText.requestFocus()
            return null
        }

        val panCardNumber = panCardNumberEditText.text.toString().trim()
        if (panCardNumber.isNullOrBlank()) {
            panCardNumberEditText.error = getString(R.string.msg_enter_pan_card_number)
            panCardNumberEditText.requestFocus()
            return null
        }

        if (panCardNumber.length != 10) {
            panCardNumberEditText.error = getString(R.string.msg_enter_valid_pan_card_number)
            panCardNumberEditText.requestFocus()
            return null
        }

        val aadharCardNumber = aadharCardNumberEditText.text.toString().trim()
        if (aadharCardNumber.isNullOrBlank()) {
            aadharCardNumberEditText.error = getString(R.string.msg_enter_aadhar_card_number)
            aadharCardNumberEditText.requestFocus()
            return null
        }

        if (aadharCardNumber.length != 12) {
            aadharCardNumberEditText.error = getString(R.string.msg_enter_valid_aadhar_card_number)
            aadharCardNumberEditText.requestFocus()
            return null
        }

        val addressLine1 = addressLine1EditText.text.toString().trim()
        if (addressLine1.isNullOrBlank()) {
            addressLine1EditText.error = getString(R.string.msg_enter_address_line1)
            addressLine1EditText.requestFocus()
            return null
        }

        val addressLine2 = addressLine2EditText.text.toString().trim()
        if (addressLine2.isNullOrBlank()) {
            addressLine2EditText.error = getString(R.string.msg_enter_address_line2)
            addressLine2EditText.requestFocus()
            return null
        }

        val pinCode = pinCodeEditText.text.toString().trim()
        if (pinCode.isNullOrBlank()) {
            pinCodeEditText.error = getString(R.string.msg_enter_pin_code)
            pinCodeEditText.requestFocus()
            return null
        }

        val city = cityEditText.text.toString().trim()
        if (city.isNullOrBlank()) {
            cityEditText.error = getString(R.string.msg_enter_city_name)
            cityEditText.requestFocus()
            return null
        }

        val taluka = talukaEditText.text.toString().trim()
        if (taluka.isNullOrBlank()) {
            pinCodeEditText.error = getString(R.string.msg_taluka_not_found_check_pin_code)
            pinCodeEditText.requestFocus()
            return null
        }

        val district = districtEditText.text.toString().trim()
        if (district.isNullOrBlank()) {
            pinCodeEditText.error = getString(R.string.msg_district_not_found_check_pin_code)
            pinCodeEditText.requestFocus()
            return null
        }

        val state = stateEditText.text.toString().trim()
        if (state.isNullOrBlank()) {
            pinCodeEditText.error = getString(R.string.msg_state_not_found_check_pin_code)
            pinCodeEditText.requestFocus()
            return null
        }

        val user = BaseAccountManager(requireActivity()).userDetails
        user?.firstName = firstName
        user?.lastName = lastName
        user?.email = emailId
        user?.panCardNumber = panCardNumber
        user?.aadharCardNumber = aadharCardNumber

        val address = Address()
        address.line1 = addressLine1
        address.line2 = addressLine2
        address.pinCode = pinCode
        address.city = city
        address.taluka = taluka
        address.district = district
        address.state = state
        user?.address = address

        return user
    }

    private fun checkForNewUserApiCall(user: User) {
        if (!isInternetAvailable()) {
            showMessage(getString(R.string.msg_no_internet))
            return
        }

        val userId = user.id
        if (userId.isNullOrBlank()) {
            DebugLog.e("User id is null")
            return
        }

        viewModel?.updateUserDetails(userId, user)
            ?.observe(viewLifecycleOwner, observeUpdateUserDetails(user))
    }

    private fun observeUpdateUserDetails(user: User) = Observer<DataBound<ApiMessage>> {
        if (it == null) {
            DebugLog.e("ApiMessage is null")
            return@Observer
        }

        it.let { dataBound ->
            when (dataBound) {
                is DataBound.Success -> {
                    saveButton.isEnabled = true
                    progressBar.visibility = View.GONE
                    BaseAccountManager(requireActivity()).userDetails = user
                    showMessage(getString(R.string.msg_user_details_updated_successfully))
                    isDirty = false
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

    private fun getPostalAddress(pinCode: String) {
        if (!isInternetAvailable()) {
            showMessage(getString(R.string.msg_no_internet))
            return
        }

        viewModel?.getPostalAddress(pinCode)
            ?.observe(viewLifecycleOwner, observeGetPostalAddress)
    }

    private val observeGetPostalAddress = Observer<DataBound<ResponsePostalAddress>> {
        if (it == null) {
            DebugLog.e("ApiMessage is null")
            return@Observer
        }

        it.let { dataBound ->
            when (dataBound) {
                is DataBound.Success -> {
                    saveButton.isEnabled = true
                    progressBar.visibility = View.GONE
                    dataBound.data.let { response ->
                        val postalAddressList = response.postalAddressList
                        if (postalAddressList.isNullOrEmpty()) {
                            talukaEditText.text = null
                            districtEditText.text = null
                            stateEditText.text = null
                            pinCodeEditText.error = response.message
                            pinCodeEditText.requestFocus()
                        } else {
                            val postalAddress = postalAddressList[0]
                            talukaEditText.setText(postalAddress.taluka)
                            districtEditText.setText(postalAddress.district)
                            stateEditText.setText(postalAddress.state)
                        }
                    }
                }
                is DataBound.Error -> {
                    saveButton.isEnabled = true
                    progressBar.visibility = View.GONE
                    DebugLog.e("Error: ${dataBound.message}")
                    showMessage("${dataBound.message}")
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