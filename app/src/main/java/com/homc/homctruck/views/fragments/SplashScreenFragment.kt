package com.homc.homctruck.views.fragments

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.Navigation
import com.google.firebase.auth.FirebaseAuth
import com.homc.homctruck.R
import com.homc.homctruck.data.models.User
import com.homc.homctruck.data.repositories.AuthenticationRepository
import com.homc.homctruck.data.sourceremote.AuthenticationRemoteDataSource
import com.homc.homctruck.restapi.AppApiInstance
import com.homc.homctruck.restapi.DataBound
import com.homc.homctruck.utils.*
import com.homc.homctruck.utils.account.BaseAccountManager
import com.homc.homctruck.viewmodels.AuthenticationViewModel
import com.homc.homctruck.viewmodels.AuthenticationViewModelFactory
import com.homc.homctruck.views.activities.MainDrawerActivity
import com.homc.homctruck.views.activities.RetryListener
import kotlinx.android.synthetic.main.fragment_splash_screen.*
import java.net.HttpURLConnection

class SplashScreenFragment : BaseFullScreenFragment() {

    private var viewModel: AuthenticationViewModel? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_splash_screen, container, false)
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
        retryButton.setOnClickListener {
            retryButton.visibility = View.GONE
            getUserDetails()
        }
        // sleepAndContinue()

        val user = FirebaseAuth.getInstance().currentUser
        if (user == null || !isInternetAvailable()) {
            DebugLog.e("User is null or internet is not available")
            openLoginScreen()
            return
        }

        user.getIdToken(true).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val firebaseToken = task.result.token
                if (firebaseToken.isNullOrBlank()) {
                    DebugLog.w("Setting token null.")
                    openLoginScreen()
                } else {
                    DebugLog.w("Setting token firebaseToken")
                    BaseAccountManager(requireActivity()).userAuthToken = firebaseToken
                    getUserDetails()
                }
            } else {
                DebugLog.w("Setting token null.")
                openLoginScreen()
            }
        }
        // getUserDetails()
    }

    private fun sleepAndContinue() {
        val activityStart = Runnable { dispatchActivity() }
        Handler().postDelayed(activityStart, DELAY_MS.toLong())
    }

    private fun dispatchActivity() {
        if (!isAdded) {
            return
        }

        val isMobileVerified = BaseAccountManager(requireActivity()).isMobileVerified
        if (isMobileVerified == null || !isMobileVerified) {
            openLoginScreen()
        } else {
            if (!canHaveAppAccess(requireContext())) return
            startActivity(Intent(requireActivity(), MainDrawerActivity::class.java))
            requireActivity().finish()
        }
    }

    private fun getUserDetails() {
        if (!isInternetAvailable()) {
            DebugLog.e(getString(R.string.msg_no_internet))
            retryButton.visibility = View.VISIBLE
            return
        }

        val userId = BaseAccountManager(requireActivity()).userDetails?.id
        if (userId.isNullOrBlank()) {
            DebugLog.e("User Id found null")
            openLoginScreen()
            return
        }

        viewModel?.getUserDetails(userId)
            ?.observe(viewLifecycleOwner, observeGetUserDetails)
    }

    private fun openLoginScreen() {
        BaseAccountManager(requireActivity()).removeAccount(requireActivity())
        val navigationController = Navigation.findNavController(requireView())
        navigationController.navigate(R.id.action_splashScreenFragment_to_loginFragment)
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
                    dispatchActivity()
                    retryButton.visibility = View.GONE
                    progressBar.visibility = View.GONE
                }
                is DataBound.Error -> {
                    DebugLog.w("Error: ${dataBound.message}")
                    if (dataBound.code == HttpURLConnection.HTTP_NOT_FOUND) {
                        openLoginScreen()
                    }
                    retryButton.visibility = View.VISIBLE
                    progressBar.visibility = View.GONE
                }
                is DataBound.Retry -> {
                    getAuthTokenFromFirebase(requireActivity(), object : RetryListener {
                        override fun retry() {
                            initViewModel()
                            retryButton.visibility = View.VISIBLE
                            progressBar.visibility = View.GONE
                            showMessage(getString(R.string.error_something_went_wrong_try_again))
                        }
                    })
                }
                is DataBound.Loading -> {
                    retryButton.visibility = View.GONE
                    progressBar.visibility = View.VISIBLE
                }
            }
        }
    }

    companion object {
        private const val DELAY_MS = 1200
    }
}