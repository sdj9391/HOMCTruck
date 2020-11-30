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
import com.google.firebase.messaging.FirebaseMessaging
import com.homc.homctruck.R
import com.homc.homctruck.data.models.User
import com.homc.homctruck.data.repositories.AuthenticationRepository
import com.homc.homctruck.data.sourceremote.AuthenticationRemoteDataSource
import com.homc.homctruck.restapi.AppApiInstance
import com.homc.homctruck.restapi.DataBound
import com.homc.homctruck.services.sendTokenToServer
import com.homc.homctruck.utils.DebugLog
import com.homc.homctruck.utils.account.BaseAccountManager
import com.homc.homctruck.utils.getAuthToken
import com.homc.homctruck.utils.isInternetAvailable
import com.homc.homctruck.viewmodels.AuthenticationViewModel
import com.homc.homctruck.viewmodels.AuthenticationViewModelFactory
import com.homc.homctruck.views.activities.MainDrawerActivity
import kotlinx.android.synthetic.main.fragment_login.*
import java.net.HttpURLConnection
import java.util.concurrent.TimeUnit

class LoginFragment : BaseFullScreenFragment() {

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
                        showMessage(getString(R.string.msg_unable_send_sms_quota_exceeded))
                        DebugLog.e("The SMS quota for the project has been exceeded")
                    }
                    else -> {
                        showMessage(getString(R.string.error_something_went_wrong))
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initViewModel()
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
                    checkForNewUserApiCall(firebaseUser)
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
                        showMessage(getString(R.string.error_something_went_wrong))
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
            showMessage(getString(R.string.error_something_went_wrong))
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

    private fun openMainDrawerActivity() {
        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            if (!task.isSuccessful) {
                DebugLog.e("Fetching FCM registration token failed ${task.exception}")
            } else {
                val token = task.result
                sendTokenToServer(token, requireActivity())
            }
            startActivity(Intent(requireActivity(), MainDrawerActivity::class.java))
            requireActivity().finish()
        }
    }

    private fun checkForNewUserApiCall(firebaseUser: FirebaseUser?) {
        if (firebaseUser == null) {
            DebugLog.e("Firebase user is null.")
            onEditMobileNumberClick()
            showMessage(getString(R.string.error_something_went_wrong))
            return
        }

        if (!isInternetAvailable()) {
            onEditMobileNumberClick()
            showMessage(getString(R.string.msg_no_internet))
            return
        }

        viewModel?.getUserDetails(firebaseUser.uid)
            ?.observe(viewLifecycleOwner, observeGetUserDetails(firebaseUser.uid))
    }

    private fun observeGetUserDetails(uid: String) = Observer<DataBound<User>> {
        if (it == null) {
            DebugLog.e("ApiMessage is null")
            return@Observer
        }

        it.let { dataBound ->
            when (dataBound) {
                is DataBound.Success -> {
                    addUserAccountDetails(dataBound.data)
                    openMainDrawerActivity()
                }
                is DataBound.Error -> {
                    if (dataBound.code == HttpURLConnection.HTTP_NOT_FOUND) {
                        DebugLog.w("Error: ${dataBound.message}")
                        val user = User()
                        user.verificationStatus = User.USER_STATUS_PENDING
                        user.role = User.ROLE_USER
                        user.id = uid
                        viewModel?.addNewUser(user)
                            ?.observe(viewLifecycleOwner, observeAddUserDetails)
                    } else {
                        DebugLog.e("Error: ${dataBound.message}")
                        onEditMobileNumberClick()
                        showMessage(getString(R.string.error_something_went_wrong_try_again))
                    }
                }
                is DataBound.Loading -> {
                }
            }
        }
    }

    private val observeAddUserDetails = Observer<DataBound<User>> {
        if (it == null) {
            DebugLog.e("ApiMessage is null")
            return@Observer
        }

        it.let { dataBound ->
            when (dataBound) {
                is DataBound.Success -> {
                    addUserAccountDetails(dataBound.data)
                    openMainDrawerActivity()
                }
                is DataBound.Error -> {
                    onEditMobileNumberClick()
                    showMessage(getString(R.string.error_something_went_wrong))
                }
                is DataBound.Loading -> {
                }
            }
        }
    }

    private fun addUserAccountDetails(user: User) {
        BaseAccountManager(requireActivity()).createAccount()
        BaseAccountManager(requireActivity()).userDetails = user
        BaseAccountManager(requireActivity()).userAuthToken = user.authToken
        BaseAccountManager(requireActivity()).isMobileVerified = true
    }

    companion object {
        const val OTP_WAIT_DURATION_SEC = 5L
    }
}