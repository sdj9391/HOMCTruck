package com.homc.homctruck.views.fragments

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.Navigation
import com.homc.homctruck.R
import com.homc.homctruck.data.repositories.AuthenticationRepository
import com.homc.homctruck.data.sourceremote.AuthenticationRemoteDataSource
import com.homc.homctruck.restapi.AppApiInstance
import com.homc.homctruck.utils.account.BaseAccountManager
import com.homc.homctruck.utils.canHaveAppAccess
import com.homc.homctruck.utils.getAuthToken
import com.homc.homctruck.viewmodels.AuthenticationViewModel
import com.homc.homctruck.viewmodels.AuthenticationViewModelFactory
import com.homc.homctruck.views.activities.MainDrawerActivity

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
        sleepAndContinue()
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

    private fun openLoginScreen() {
        BaseAccountManager(requireActivity()).removeAccount(requireActivity())
        val navigationController = Navigation.findNavController(requireView())
        navigationController.navigate(R.id.action_splashScreenFragment_to_loginFragment)
    }

    companion object {
        private const val DELAY_MS = 1200
    }
}