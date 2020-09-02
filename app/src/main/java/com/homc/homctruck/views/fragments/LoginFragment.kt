package com.homc.homctruck.views.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.google.firebase.FirebaseException
import com.google.firebase.FirebaseTooManyRequestsException
import com.google.firebase.auth.*
import com.homc.homctruck.HomcTruckApp
import com.homc.homctruck.R
import com.homc.homctruck.data.models.ApiMessage
import com.homc.homctruck.data.models.User
import com.homc.homctruck.di.DaggerAppComponent
import com.homc.homctruck.di.modules.AppModule
import com.homc.homctruck.di.modules.ViewModelModule
import com.homc.homctruck.restapi.DataBound
import com.homc.homctruck.data.models.AppConfig
import com.homc.homctruck.utils.account.BaseAccountManager
import com.homc.homctruck.utils.DebugLog
import com.homc.homctruck.utils.isInternetAvailable
import com.homc.homctruck.viewmodels.AuthenticationViewModel
import com.homc.homctruck.views.activities.MainDrawerActivity
import kotlinx.android.synthetic.main.fragment_login.*
import java.net.HttpURLConnection
import java.util.concurrent.TimeUnit
import javax.inject.Inject


class LoginFragment : BaseFullScreenFragment() {

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory
    private var viewModel: AuthenticationViewModel? = null

    private var resendToken: PhoneAuthProvider.ForceResendingToken? = null
    private var verificationId: String? = null
    private var firebaseAuth: FirebaseAuth? = null

    private val onVerifyClickListener = View.OnClickListener {
        onVerifyClicked()
    }
    private val onSendOtpClickListener = View.OnClickListener {
        onSendOtpClicked()
    }

    private val verifyMobileNumberCallback =
        object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

            override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                // This callback will be invoked in two situations:
                // 1 - Instant verification. In some cases the phone number can be instantly
                //     verified without needing to send or enter a verification code.
                // 2 - Auto-retrieval. On some devices Google Play services can automatically
                //     detect the incoming verification SMS and perform verification without
                //     user action.
                DebugLog.v("onVerificationCompleted:$credential")
                otpEditText.setText(credential.smsCode)
                if (verificationId != null) {
                    signInWithPhoneAuthCredential(credential)
                }
            }

            override fun onVerificationFailed(e: FirebaseException) {
                onEditMobileNumberClick()
                // This callback is invoked in an invalid request for verification is made,
                // for instance if the the phone number format is not valid.
                DebugLog.e("onVerificationFailed $e")

                when (e) {
                    is FirebaseAuthInvalidCredentialsException -> {
                        mobileNumberEditText.error =
                            getString(R.string.msg_enter_valid_mobile_number)
                        mobileNumberEditText.requestFocus()
                    }
                    is FirebaseTooManyRequestsException -> {
                        showMessage(getString(R.string.msg_unable_send_sms_quta_exceeded))
                        DebugLog.e("The SMS quota for the project has been exceeded")
                    }
                    else -> {
                        showMessage(getString(R.string.error_general))
                    }
                }
            }

            override fun onCodeAutoRetrievalTimeOut(verificationId: String) {
                progressBar.visibility = View.GONE
                otpTextField.visibility = View.VISIBLE
                sendOtpButton.text = getString(R.string.label_continue)
                sendOtpButton.isEnabled = true
                sendOtpButton.setOnClickListener(onVerifyClickListener)
                otpTextField.isEnabled = true
                resendButton.visibility = View.VISIBLE
                editMobileNumberButton.visibility = View.VISIBLE
                this@LoginFragment.verificationId = verificationId
            }

            override fun onCodeSent(
                verificationId: String,
                token: PhoneAuthProvider.ForceResendingToken
            ) {
                // The SMS verification code has been sent to the provided phone number, we
                // now need to ask the user to enter the code and then construct a credential
                // by combining the code with a verification ID.
                DebugLog.v("onCodeSent: $verificationId")

                // Save verification ID and resending token so we can use them later
                this@LoginFragment.verificationId = verificationId
                resendToken = token
                progressBar.visibility = View.VISIBLE
                otpTextField.visibility = View.VISIBLE
                otpTextField.isEnabled = false
                sendOtpButton.text = getString(R.string.msg_wait_auto_detecting_otp)
                sendOtpButton.isEnabled = false
                sendOtpButton.setOnClickListener(onVerifyClickListener)
                resendButton.visibility = View.GONE
                editMobileNumberButton.visibility = View.VISIBLE
            }
        }

    private fun initViewModel() {
        DaggerAppComponent.builder().viewModelModule(ViewModelModule())
            .appModule(AppModule(requireActivity().application)).build().inject(this)
        viewModel = ViewModelProvider(this, viewModelFactory)[AuthenticationViewModel::class.java]
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_login, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        firebaseAuth = FirebaseAuth.getInstance()
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    requireActivity().finish()
                }
            })

        sendOtpButton.setOnClickListener(onSendOtpClickListener)

        editMobileNumberButton.setOnClickListener {
            onEditMobileNumberClick()
        }

        resendButton.setOnClickListener {
            onResendClick()
        }
    }

    private fun onEditMobileNumberClick() {
        mobileNumberEditText.isEnabled = true
        editMobileNumberButton.visibility = View.GONE
        otpTextField.visibility = View.GONE
        otpEditText.isEnabled = true
        progressBar.visibility = View.GONE
        sendOtpButton.text = getString(R.string.label_send_otp)
        sendOtpButton.isEnabled = true
        resendButton.visibility = View.GONE
        verificationId = null
        resendToken = null
    }

    private fun onResendClick() {
        val mobileNumber = mobileNumberEditText.text.toString().trim()
        if (mobileNumber.isNullOrBlank()) {
            mobileNumberEditText.error = getString(R.string.msg_enter_mobile_number)
            mobileNumberEditText.requestFocus()
            return
        }

        if (mobileNumber.length != 10) {
            mobileNumberEditText.error = getString(R.string.msg_enter_valid_mobile_number)
            mobileNumberEditText.requestFocus()
            return
        }

        mobileNumberEditText.isEnabled = false
        editMobileNumberButton.visibility = View.VISIBLE
        otpTextField.visibility = View.GONE
        progressBar.visibility = View.VISIBLE
        sendOtpButton.isEnabled = false
        resendButton.visibility = View.GONE
        if (resendToken == null) {
            verificationId = null
            PhoneAuthProvider.getInstance().verifyPhoneNumber(
                getString(R.string.placeholder_plus_91, mobileNumber),
                OTP_WAIT_DURATION_SEC,
                TimeUnit.SECONDS,
                requireActivity(),
                verifyMobileNumberCallback
            )
        } else {
            PhoneAuthProvider.getInstance().verifyPhoneNumber(
                getString(R.string.placeholder_plus_91, mobileNumber),
                OTP_WAIT_DURATION_SEC,
                TimeUnit.SECONDS,
                requireActivity(),
                verifyMobileNumberCallback,
                resendToken
            )
        }
    }

    private fun onSendOtpClicked() {
        val mobileNumber = mobileNumberEditText.text.toString().trim()
        if (mobileNumber.isNullOrBlank()) {
            mobileNumberEditText.error = getString(R.string.msg_enter_mobile_number)
            mobileNumberEditText.requestFocus()
            return
        }

        if (mobileNumber.length != 10) {
            mobileNumberEditText.error = getString(R.string.msg_enter_valid_mobile_number)
            mobileNumberEditText.requestFocus()
            return
        }

        mobileNumberEditText.isEnabled = false
        editMobileNumberButton.visibility = View.VISIBLE
        otpTextField.visibility = View.GONE
        progressBar.visibility = View.VISIBLE
        sendOtpButton.isEnabled = false
        resendButton.visibility = View.GONE
        verificationId = null
        resendToken = null
        PhoneAuthProvider.getInstance().verifyPhoneNumber(
            getString(R.string.placeholder_plus_91, mobileNumber),
            OTP_WAIT_DURATION_SEC,
            TimeUnit.SECONDS,
            requireActivity(),
            verifyMobileNumberCallback
        )
    }

    private fun signInWithPhoneAuthCredential(credential: PhoneAuthCredential) {
        firebaseAuth?.signInWithCredential(credential)
            ?.addOnCompleteListener(requireActivity()) { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    DebugLog.v("signInWithCredential:success")
                    val firebaseUser = task.result.user
                    addUserAccountDetails(firebaseUser)
                } else {
                    // Sign in failed, display a message and update the UI
                    DebugLog.e("signInWithCredential:failure -> ${task.exception}")
                    if (task.exception is FirebaseAuthInvalidCredentialsException) {
                        progressBar.visibility = View.GONE
                        otpTextField.visibility = View.VISIBLE
                        otpTextField.isEnabled = true
                        sendOtpButton.text = getString(R.string.label_continue)
                        sendOtpButton.isEnabled = true
                        sendOtpButton.setOnClickListener(onVerifyClickListener)
                        resendButton.visibility = View.VISIBLE
                        editMobileNumberButton.visibility = View.VISIBLE
                        otpEditText.error = getString(R.string.msg_otp_invalid_try_again)
                        otpEditText.requestFocus()
                    } else {
                        onEditMobileNumberClick()
                        showMessage(getString(R.string.error_general))
                    }
                }
            }
    }

    private fun onVerifyClicked() {
        val code = otpEditText.text.toString().trim()
        if (code.isNullOrBlank()) {
            otpEditText.error = getString(R.string.msg_enter_otp_please)
            otpEditText.requestFocus()
            return
        }
        if (verificationId.isNullOrBlank()) {
            onEditMobileNumberClick()
            showMessage(getString(R.string.error_general))
            return
        }

        verificationId?.let {
            progressBar.visibility = View.VISIBLE
            otpEditText.isEnabled = false
            editMobileNumberButton.visibility = View.GONE
            val credential = PhoneAuthProvider.getCredential(it, code)
            signInWithPhoneAuthCredential(credential)
        }
    }

    private fun addUserAccountDetails(firebaseUser: FirebaseUser?) {
        BaseAccountManager(requireActivity()).createAccount()

        firebaseUser?.getIdToken(true)?.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val firebaseAuthToken = task.result.token
                if (firebaseAuthToken.isNullOrBlank()) {
                    DebugLog.e("Setting token null.")
                    AppConfig.token = null
                    BaseAccountManager(requireActivity()).removeAccount(requireActivity())
                } else {
                    DebugLog.w("Setting token $firebaseAuthToken")
                    val userDetails = User()
                    userDetails.mobileNumber = mobileNumberEditText.text.toString().trim()
                    userDetails.id = firebaseUser.uid
                    userDetails.firebaseAuthToken = firebaseAuthToken
                    BaseAccountManager(requireActivity()).userDetails = userDetails
                    BaseAccountManager(requireActivity()).userAuthToken = firebaseAuthToken
                    BaseAccountManager(requireActivity()).isMobileVerified = true
                    AppConfig.token = firebaseAuthToken
                    (requireActivity().application as HomcTruckApp).initAppConfig()
                    initViewModel()
                    checkForNewUserApiCall(firebaseUser)
                }
            } else {
                DebugLog.w("Setting token null.")
                AppConfig.token = null
                BaseAccountManager(requireActivity()).removeAccount(requireActivity())
            }
        }
    }

    private fun openMainDrawerActivity() {
        startActivity(Intent(requireActivity(), MainDrawerActivity::class.java))
        requireActivity().finish()
    }

    private fun checkForNewUserApiCall(firebaseUser: FirebaseUser) {
        if (!isInternetAvailable()) {
            showMessage(getString(R.string.msg_no_internet))
            openMainDrawerActivity()
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
                    openMainDrawerActivity()
                }
                is DataBound.Error -> {
                    if (dataBound.code == HttpURLConnection.HTTP_NOT_FOUND) {
                        DebugLog.w("Error: ${dataBound.error}")
                        val user = BaseAccountManager(requireActivity()).userDetails
                        user?.isUserVerified = false
                        user?.let {
                            viewModel?.addNewUser(user)?.observe(viewLifecycleOwner, observeAddUserDetails)
                        }
                    } else {
                        DebugLog.e("Error: ${dataBound.error}")
                        openMainDrawerActivity()
                    }

                }
                is DataBound.Loading -> {
                }
            }
        }
    }

    private val observeAddUserDetails = Observer<DataBound<ApiMessage>> {
        if (it == null) {
            DebugLog.e("ApiMessage is null")
            return@Observer
        }

        it.let { dataBound ->
            when (dataBound) {
                is DataBound.Success -> {
                    DebugLog.v("${dataBound.data.message}")
                    openMainDrawerActivity()
                }
                is DataBound.Error -> {
                    DebugLog.e("Error: ${dataBound.error}")
                    openMainDrawerActivity()
                }
                is DataBound.Loading -> {
                }
            }
        }
    }

    companion object {
        const val OTP_WAIT_DURATION_SEC = 5L
    }
}