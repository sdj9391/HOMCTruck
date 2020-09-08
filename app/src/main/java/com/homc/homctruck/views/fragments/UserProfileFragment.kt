package com.homc.homctruck.views.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.NavController
import androidx.navigation.Navigation
import com.homc.homctruck.R
import com.homc.homctruck.data.models.getFullAddress
import com.homc.homctruck.data.models.getName
import com.homc.homctruck.utils.DebugLog
import com.homc.homctruck.utils.account.BaseAccountManager
import kotlinx.android.synthetic.main.fragment_user_profile.*
import kotlinx.android.synthetic.main.item_user_details.*

class UserProfileFragment : BaseAppFragment() {

    private var navigationController: NavController? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_user_profile, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setToolBarTitle(getString(R.string.menu_user_profile))
        setUserDetails()
        navigationController = Navigation.findNavController(requireView())
        editProfileButton.setOnClickListener {
            navigationController?.navigate(R.id.action_userProfileFragment_to_editUserProfileFragment)
        }
        truckProfileButton.setOnClickListener {
            navigationController?.navigate(R.id.action_userProfileFragment_to_userTruckListFragment)
        }
        contractorProfileButton.setOnClickListener {
            navigationController?.navigate(R.id.action_userProfileFragment_to_editContractorProfileFragment)
        }
    }

    private fun setUserDetails() {
        val user = BaseAccountManager(requireActivity()).userDetails
        if (user == null) {
            DebugLog.e("User Found null")
            return
        }

        titleTextView.text = if (user.getName().isNullOrBlank()) {
            getString(R.string.label_name_of_the_user)
        } else {
            user.getName()
        }

        subtitleTextView1.text = getString(R.string.placeholder_plus_91, user.mobileNumber)
        val email = user.email
        if (email.isNullOrBlank()) {
            subtitleTextView2.visibility = View.GONE
        } else {
            subtitleTextView2.text = email
            subtitleTextView2.visibility = View.VISIBLE
        }
        val panCardNumber = user.panCardNumber
        if (panCardNumber.isNullOrBlank()) {
            subtitleTextView3.visibility = View.GONE
        } else {
            subtitleTextView3.text = panCardNumber
            subtitleTextView3.visibility = View.VISIBLE
        }
        val aadharCardNumber = user.aadharCardNumber
        if (aadharCardNumber.isNullOrBlank()) {
            subtitleTextView4.visibility = View.GONE
        } else {
            subtitleTextView4.text = aadharCardNumber
            subtitleTextView4.visibility = View.VISIBLE
        }
        val address = user.address
        if (address == null) {
            subtitleTextView5.visibility = View.GONE
        } else {
            val addressString = address.getFullAddress()
            if (addressString.isNullOrBlank()) {
                subtitleTextView5.visibility = View.GONE
            } else {
                subtitleTextView5.text = addressString
                subtitleTextView5.visibility = View.VISIBLE
            }
        }
    }
}