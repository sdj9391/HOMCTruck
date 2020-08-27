package com.homc.homctruck.views.fragments

import android.os.Bundle
import android.os.Handler
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.navigation.Navigation
import com.homc.homctruck.R

class SplashScreenFragment : BaseFullScreenFragment() {
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_splash_screen, container, false)
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

        val navigationController = Navigation.findNavController(requireView())
        navigationController.navigate(R.id.action_splashScreenFragment_to_loginFragment)
    }

    companion object {
        private const val DELAY_MS = 1200
    }
}