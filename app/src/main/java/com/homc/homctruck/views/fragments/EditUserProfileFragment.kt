package com.homc.homctruck.views.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.google.firebase.auth.FirebaseUser
import com.homc.homctruck.R
import com.homc.homctruck.data.models.User
import com.homc.homctruck.di.DaggerAppComponent
import com.homc.homctruck.di.modules.AppModule
import com.homc.homctruck.di.modules.ViewModelModule
import com.homc.homctruck.restapi.DataBound
import com.homc.homctruck.utils.DebugLog
import com.homc.homctruck.utils.account.BaseAccountManager
import com.homc.homctruck.utils.isInternetAvailable
import com.homc.homctruck.viewmodels.AuthenticationViewModel
import kotlinx.android.synthetic.main.fragment_edit_user_profile.*
import java.net.HttpURLConnection
import javax.inject.Inject

class EditUserProfileFragment : BaseAppFragment() {

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory
    private var viewModel: AuthenticationViewModel? = null

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
        DaggerAppComponent.builder().viewModelModule(ViewModelModule())
            .appModule(AppModule(requireActivity().application)).build().inject(this)
        viewModel = ViewModelProvider(this, viewModelFactory)[AuthenticationViewModel::class.java]
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
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

        saveButton.setOnClickListener {
            validateData()
        }
    }

    private fun validateData() {
        val firstName = firstNameEditText.text.toString().trim()
        val lastName = firstNameEditText.text.toString().trim()
        val mobileNumber = firstNameEditText.text.toString().trim()
        val panCardNumber = firstNameEditText.text.toString().trim()
        val aadharCardNumber = firstNameEditText.text.toString().trim()
        val addressLine1 = firstNameEditText.text.toString().trim()
        val addressLine2 = firstNameEditText.text.toString().trim()
        val pinCode = firstNameEditText.text.toString().trim()
        val city = firstNameEditText.text.toString().trim()
        val taluka = firstNameEditText.text.toString().trim()
        val district = firstNameEditText.text.toString().trim()
        val state = firstNameEditText.text.toString().trim()
    }



    private fun checkForNewUserApiCall(firebaseUser: FirebaseUser) {
        if (!isInternetAvailable()) {
            showMessage(getString(R.string.msg_no_internet))
            return
        }

        viewModel?.getUserDetails(firebaseUser.uid)
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
                    val user = dataBound.data
                    BaseAccountManager(requireActivity()).userDetails = user
                }
                is DataBound.Error -> {
                    if (dataBound.code == HttpURLConnection.HTTP_NOT_FOUND) {

                    } else {
                        DebugLog.e("Error: ${dataBound.error}")
                    }

                }
                is DataBound.Loading -> {
                }
            }
        }
    }
}