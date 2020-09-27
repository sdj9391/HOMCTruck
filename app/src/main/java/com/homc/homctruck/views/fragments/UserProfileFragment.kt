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
import com.homc.homctruck.data.models.isNullOrEmpty
import com.homc.homctruck.utils.DebugLog
import com.homc.homctruck.utils.account.BaseAccountManager
import com.homc.homctruck.utils.hideSoftKeyboard
import com.homc.homctruck.utils.setColorsAndCombineStrings
import kotlinx.android.synthetic.main.fragment_user_profile.*
import kotlinx.android.synthetic.main.item_user_details.view.*

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
        setUserContractorProfileDetails()
        navigationController = Navigation.findNavController(requireView())
        editProfileButton.setOnClickListener {
            navigationController?.navigate(R.id.action_userProfileFragment_to_editUserProfileFragment)
        }
        myTrucksButton.setOnClickListener {
            navigationController?.navigate(R.id.action_userProfileFragment_to_userTruckListFragment)
        }
        editContractorProfileButton.setOnClickListener {
            navigationController?.navigate(R.id.action_userProfileFragment_to_editContractorProfileFragment)
        }
    }

    override fun onResume() {
        super.onResume()
        hideSoftKeyboard(requireActivity())
    }

    private fun setUserDetails() {
        val user = BaseAccountManager(requireActivity()).userDetails
        if (user == null) {
            DebugLog.e("User Found null")
            return
        }

        if (user.getName().isNullOrBlank()) {
            userDetails.titleTextView.text = getString(R.string.label_name_of_the_user)
        } else {
            setColorsAndCombineStrings(
                userDetails.titleTextView,
                getString(R.string.label_name),
                user.getName()
            )
        }

        setColorsAndCombineStrings(
            userDetails.subtitleTextView1,
            getString(R.string.label_mobile_number),
            getString(R.string.placeholder_plus_91, user.mobileNumber)
        )
        val email = user.email
        if (email.isNullOrBlank()) {
            userDetails.subtitleTextView2.visibility = View.GONE
        } else {
            setColorsAndCombineStrings(
                userDetails.subtitleTextView2,
                getString(R.string.label_email),
                email
            )
            userDetails.subtitleTextView2.visibility = View.VISIBLE
        }
        val panCardNumber = user.panCardNumber
        if (panCardNumber.isNullOrBlank()) {
            userDetails.subtitleTextView3.visibility = View.GONE
        } else {
            setColorsAndCombineStrings(
                userDetails.subtitleTextView3,
                getString(R.string.label_pan_card_number),
                panCardNumber
            )
            userDetails.subtitleTextView3.visibility = View.VISIBLE
        }
        val aadharCardNumber = user.aadharCardNumber
        if (aadharCardNumber.isNullOrBlank()) {
            userDetails.subtitleTextView4.visibility = View.GONE
        } else {
            setColorsAndCombineStrings(
                userDetails.subtitleTextView4,
                getString(R.string.label_aadhar_card_number),
                aadharCardNumber
            )
            userDetails.subtitleTextView4.visibility = View.VISIBLE
        }
        val address = user.address
        if (address == null) {
            userDetails.subtitleTextView5.visibility = View.GONE
        } else {
            val addressString = address.getFullAddress()
            if (addressString.isNullOrBlank()) {
                userDetails.subtitleTextView5.visibility = View.GONE
            } else {
                setColorsAndCombineStrings(
                    userDetails.subtitleTextView5,
                    getString(R.string.label_address),
                    addressString
                )
                userDetails.subtitleTextView5.visibility = View.VISIBLE
            }
        }
    }

    private fun setUserContractorProfileDetails() {
        val user = BaseAccountManager(requireActivity()).userDetails
        if (user == null) {
            DebugLog.e("User Found null")
            return
        }

        val contractorProfile = user.contractor
        if (contractorProfile.isNullOrEmpty()) {
            contractorProfileDetails.visibility = View.GONE
            editContractorProfileButton.setImageResource(R.drawable.ic_arrow_forward_black)
            return
        }


        setColorsAndCombineStrings(
            contractorProfileDetails.titleTextView,
            getString(R.string.label_firm_name),
            contractorProfile?.firmName
        )

        val email = contractorProfile?.email
        if (email.isNullOrBlank()) {
            contractorProfileDetails.subtitleTextView1.visibility = View.GONE
        } else {
            setColorsAndCombineStrings(
                contractorProfileDetails.subtitleTextView1,
                getString(R.string.label_email),
                email
            )
            contractorProfileDetails.subtitleTextView1.visibility = View.VISIBLE
        }
        val panCardNumber = contractorProfile?.panCardNumber
        if (panCardNumber.isNullOrBlank()) {
            contractorProfileDetails.subtitleTextView2.visibility = View.GONE
        } else {
            setColorsAndCombineStrings(
                contractorProfileDetails.subtitleTextView2,
                getString(R.string.label_pan_card_number),
                panCardNumber
            )
            contractorProfileDetails.subtitleTextView2.visibility = View.VISIBLE
        }
        val address = contractorProfile?.address
        if (address == null) {
            contractorProfileDetails.subtitleTextView3.visibility = View.GONE
        } else {
            val addressString = address.getFullAddress()
            if (addressString.isNullOrBlank()) {
                contractorProfileDetails.subtitleTextView3.visibility = View.GONE
            } else {
                setColorsAndCombineStrings(
                    contractorProfileDetails.subtitleTextView3,
                    getString(R.string.label_address),
                    addressString
                )
                contractorProfileDetails.subtitleTextView3.visibility = View.VISIBLE
            }
        }

        contractorProfileDetails.subtitleTextView4.visibility = View.GONE
        contractorProfileDetails.subtitleTextView5.visibility = View.GONE
    }
}