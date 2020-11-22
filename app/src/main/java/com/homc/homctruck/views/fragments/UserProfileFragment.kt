package com.homc.homctruck.views.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.Navigation
import com.homc.homctruck.R
import com.homc.homctruck.data.models.User
import com.homc.homctruck.data.models.getFullAddress
import com.homc.homctruck.data.models.getName
import com.homc.homctruck.data.models.isNullOrEmpty
import com.homc.homctruck.data.repositories.AuthenticationRepository
import com.homc.homctruck.data.sourceremote.AuthenticationRemoteDataSource
import com.homc.homctruck.restapi.AppApiInstance
import com.homc.homctruck.restapi.DataBound
import com.homc.homctruck.services.MyFirebaseMessagingService
import com.homc.homctruck.services.cancelNotification
import com.homc.homctruck.utils.*
import com.homc.homctruck.utils.account.BaseAccountManager
import com.homc.homctruck.viewmodels.AuthenticationViewModel
import com.homc.homctruck.viewmodels.AuthenticationViewModelFactory
import com.homc.homctruck.views.activities.AuthenticationActivity
import com.homc.homctruck.views.activities.RetryListener
import com.homc.homctruck.views.dialogs.BottomSheetListDialogFragment
import com.homc.homctruck.views.dialogs.BottomSheetViewData
import com.homc.homctruck.views.dialogs.BottomSheetViewItem
import com.homc.homctruck.views.dialogs.BottomSheetViewSection
import kotlinx.android.synthetic.main.fragment_user_profile.*
import kotlinx.android.synthetic.main.item_user_details.view.*
import java.net.HttpURLConnection

class UserProfileFragment : BaseAppFragment() {

    private var viewModel: AuthenticationViewModel? = null
    private var bottomSheetListDialogFragment: BottomSheetListDialogFragment? = null

    private var navigationController: NavController? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_user_profile, container, false)
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
        cancelNotification(requireActivity(), MyFirebaseMessagingService.NOTIFICATION_ID_USER_STATUS)
        setToolBarTitle(getString(R.string.menu_user_profile))
        getUserDetails()
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

    private fun getUserDetails() {
        if (!isInternetAvailable()) {
            DebugLog.e(getString(R.string.msg_no_internet))
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
                    setUserDetails()
                    setUserContractorProfileDetails()
                }
                is DataBound.Error -> {
                    DebugLog.w("Error: ${dataBound.message}")
                    if (dataBound.code == HttpURLConnection.HTTP_NOT_FOUND) {
                        openLoginScreen()
                    }
                }
                is DataBound.Retry -> {
                    if (canRetryApiCall) {
                        getAuthTokenFromFirebase(requireActivity(), object : RetryListener {
                            override fun retry() {
                                initViewModel()
                                showMessage(getString(R.string.error_something_went_wrong_try_again))
                            }
                        })
                    } else {
                        canRetryApiCall = false
                        showMessage(getString(R.string.error_something_went_wrong))
                    }
                }
                is DataBound.Loading -> {
                }
            }
        }
    }

    private fun openLoginScreen() {
        BaseAccountManager(requireActivity()).userDetails = null
        BaseAccountManager(requireActivity()).userAuthToken = null
        BaseAccountManager(requireActivity()).isMobileVerified = null
        val intent = Intent(requireActivity(), AuthenticationActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(intent)
        requireActivity().finish()
    }

    private fun setUserDetails() {
        val user = BaseAccountManager(requireActivity()).userDetails
        if (user == null) {
            DebugLog.e("User Found null")
            return
        }

        if (user.role.equals(User.ROLE_ADMIN, true)) {
            adminToolsButton.visibility = View.VISIBLE
            adminToolsButton.setOnClickListener {
                showAdminTools()
            }
        } else {
            adminToolsButton.visibility = View.GONE
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

    private fun showAdminTools() {
        val sectionItems = mutableListOf<BottomSheetViewItem>()
        sectionItems.add(
            BottomSheetViewItem(
                ACTION_ID_MANAGE_USER, R.drawable.ic_user_black,
                getString(R.string.label_manage_users), null, null
            )
        )
        sectionItems.add(
            BottomSheetViewItem(
                ACTION_ID_MANAGE_TRUCK, R.drawable.ic_truck_black,
                getString(R.string.label_manage_trucks), null, null
            )
        )
        val sections = mutableListOf<BottomSheetViewSection>()
        sections.add(BottomSheetViewSection(viewItems = sectionItems))
        val bottomSheetViewData = BottomSheetViewData(bottomSheetViewSections = sections)
        bottomSheetListDialogFragment =
            BottomSheetListDialogFragment(bottomSheetViewData, onMoreOptionClickListener)
        parentFragmentManager.let { bottomSheetListDialogFragment?.show(it, "ADMIN_TOOLS") }
    }

    private val onMoreOptionClickListener: View.OnClickListener = View.OnClickListener {
        bottomSheetListDialogFragment?.dismiss()
        when (it.id) {
            ACTION_ID_MANAGE_USER -> navigationController?.navigate(R.id.action_userProfileFragment_to_userTabFragment)
            ACTION_ID_MANAGE_TRUCK -> navigationController?.navigate(R.id.action_userProfileFragment_to_truckTabFragment)
            else -> DebugLog.e("Id not matched")
        }
    }

    companion object {
        const val ACTION_ID_MANAGE_USER = 111
        const val ACTION_ID_MANAGE_TRUCK = 222
    }
}