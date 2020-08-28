package com.homc.homctruck.views.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import com.google.firebase.FirebaseException
import com.google.firebase.FirebaseTooManyRequestsException
import com.google.firebase.auth.*
import com.google.gson.Gson
import com.homc.homctruck.R
import com.homc.homctruck.utils.DebugLog
import kotlinx.android.synthetic.main.fragment_login.*
import java.util.concurrent.TimeUnit


class LoginFragment : BaseFullScreenFragment() {

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
                        mobileNumberEditText.error = "Enter valid mobile number please!"
                    }
                    is FirebaseTooManyRequestsException -> {
                        mobileNumberEditText.error = "Unable to send SMS for now!"
                        DebugLog.e("The SMS quota for the project has been exceeded")
                    }
                    else -> {
                        showMessage("Something went wrong, please try again!")
                    }
                }
            }

            override fun onCodeAutoRetrievalTimeOut(verificationId: String) {
                progressBar.visibility = View.GONE
                otpTextField.visibility = View.VISIBLE
                sendOtpButton.text = getString(R.string.label_continue)
                sendOtpButton.isEnabled = true
                sendOtpButton.setOnClickListener(onVerifyClickListener)
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
            }
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
            mobileNumberEditText.error = "Enter mobile number please!"
            return
        }

        if (mobileNumber.length != 10) {
            mobileNumberEditText.error = "Enter valid mobile number please!"
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
            mobileNumberEditText.error = "Enter mobile number please!"
            return
        }

        if (mobileNumber.length != 10) {
            mobileNumberEditText.error = "Enter valid mobile number please!"
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
                    val user = task.result.user
                    checkForNewUser(user)
                } else {
                    onEditMobileNumberClick()
                    // Sign in failed, display a message and update the UI
                    DebugLog.e("signInWithCredential:failure -> ${task.exception}")
                    if (task.exception is FirebaseAuthInvalidCredentialsException) {
                        showMessage("The verification code entered was invalid, please try again!")
                    } else {
                        showMessage("Something went wrong, please try again!")
                    }
                }
            }
    }

    private fun onVerifyClicked() {
        val code = otpEditText.text.toString().trim()
        if (code.isNullOrBlank()) {
            otpEditText.error = "Enter OTP please!"
            return
        }
        if (verificationId.isNullOrBlank()) {
            onEditMobileNumberClick()
            showMessage("Something went wrong, please try again!")
            return
        }

        verificationId?.let {
            progressBar.visibility = View.VISIBLE
            val credential = PhoneAuthProvider.getCredential(it, code)
            signInWithPhoneAuthCredential(credential)
        }
    }

    private fun checkForNewUser(user: FirebaseUser?) {
        onEditMobileNumberClick()
        DebugLog.v("Login success -> ${Gson().toJson(user)}")
        showMessage("Login successful!")
    }

    companion object {
        const val OTP_WAIT_DURATION_SEC = 30L
    }
}