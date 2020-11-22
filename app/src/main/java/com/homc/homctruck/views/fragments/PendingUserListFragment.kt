package com.homc.homctruck.views.fragments

import android.os.Bundle
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.homc.homctruck.R
import com.homc.homctruck.data.models.ApiMessage
import com.homc.homctruck.data.models.Truck
import com.homc.homctruck.data.models.User
import com.homc.homctruck.data.repositories.AuthenticationRepository
import com.homc.homctruck.data.sourceremote.AuthenticationRemoteDataSource
import com.homc.homctruck.restapi.AppApiInstance
import com.homc.homctruck.restapi.DataBound
import com.homc.homctruck.utils.*
import com.homc.homctruck.viewmodels.AuthenticationViewModel
import com.homc.homctruck.viewmodels.AuthenticationViewModelFactory
import com.homc.homctruck.views.activities.RefreshListener
import com.homc.homctruck.views.activities.RetryListener
import com.homc.homctruck.views.activities.StatusChangedListener
import com.homc.homctruck.views.adapters.UserListAdapter
import com.homc.homctruck.views.dialogs.BottomSheetListDialogFragment
import com.homc.homctruck.views.dialogs.BottomSheetViewData
import com.homc.homctruck.views.dialogs.BottomSheetViewItem
import com.homc.homctruck.views.dialogs.BottomSheetViewSection
import kotlinx.android.synthetic.main.item_common_list_layout.*
import kotlinx.android.synthetic.main.item_search_view.view.*
import java.net.HttpURLConnection

open class PendingUserListFragment : BaseAppFragment() {

    protected var viewModel: AuthenticationViewModel? = null
    private var userAdapter: UserListAdapter? = null
    private var bottomSheetListDialogFragment: BottomSheetListDialogFragment? = null

    private val textWatcher: TextWatcher? = object : TextWatcher {
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            val length = s?.length ?: 0
            if (length > 2 || length == 0) {
                getData(s.toString())
            }
        }

        override fun afterTextChanged(s: android.text.Editable?) {}
    }

    var statusChangedListener: StatusChangedListener? = null
    val refreshListener = object : RefreshListener {
        override fun onRefresh(dataItem: Any) {
            userAdapter?.addItem(dataItem)
            hideMessageView()
        }
    }

    private val onMoreClickListener = View.OnClickListener {
        val dataItem = it.tag
        if (dataItem !is User) {
            DebugLog.e("Wrong instance found. Expected: ${Truck::class.java.simpleName} Found: $dataItem")
            return@OnClickListener
        }

        showMoreOptionBottomSheet(dataItem)
    }

    protected fun getBottomSheetViewForApproveAction(dataItem: User): BottomSheetViewItem {
        return BottomSheetViewItem(
            ACTION_APPROVE, R.drawable.ic_user_green,
            getString(R.string.label_approve_user), null, dataItem
        )
    }

    protected fun getBottomSheetViewForRejectAction(dataItem: User): BottomSheetViewItem {
        return BottomSheetViewItem(
            ACTION_REJECT, R.drawable.ic_user_red,
            getString(R.string.label_reject_user), null, dataItem
        )
    }

    protected fun getBottomSheetViewForPendingAction(dataItem: User): BottomSheetViewItem {
        return BottomSheetViewItem(
            ACTION_PENDING, R.drawable.ic_user_yellow,
            getString(R.string.label_move_user_to_pending), null, dataItem
        )
    }

    protected open fun getBottomSheetOption(dataItem: User): MutableList<BottomSheetViewItem> {
        val sectionItems = mutableListOf<BottomSheetViewItem>()
        sectionItems.add(getBottomSheetViewForApproveAction(dataItem))
        sectionItems.add(getBottomSheetViewForRejectAction(dataItem))
        return sectionItems
    }

    private fun showMoreOptionBottomSheet(dataItem: User) {
        val sectionItems = getBottomSheetOption(dataItem)
        val sections = mutableListOf<BottomSheetViewSection>()
        sections.add(BottomSheetViewSection(viewItems = sectionItems))
        val bottomSheetViewData = BottomSheetViewData(bottomSheetViewSections = sections)
        bottomSheetListDialogFragment =
            BottomSheetListDialogFragment(bottomSheetViewData, onMoreOptionClickListener)
        parentFragmentManager.let { bottomSheetListDialogFragment?.show(it, "EDIT/DELETE") }
    }

    private val onMoreOptionClickListener: View.OnClickListener = View.OnClickListener {
        bottomSheetListDialogFragment?.dismiss()
        val dataItem = it.tag
        if (dataItem !is User) {
            DebugLog.e("Wrong instance found. Expected: ${User::class.java.simpleName} Found: $dataItem")
            return@OnClickListener
        }
        when (it.id) {
            ACTION_APPROVE -> changeUserStatus(dataItem, User.USER_STATUS_CONFIRMED)
            ACTION_REJECT -> rejectUser(dataItem)
            ACTION_PENDING -> changeUserStatus(dataItem, User.USER_STATUS_PENDING)
            else -> DebugLog.e("Id not matched")
        }
    }

    private fun changeUserStatus(dataItem: User, userStatus: String) {
        dataItem.verificationStatus = userStatus
        updateUserDetails(dataItem)
    }

    private fun rejectUser(dataItem: User) {
        showConfirmDialog(
            requireActivity(), getString(R.string.msg_confirm_reject),
            { _, _ -> changeUserStatus(dataItem, User.USER_STATUS_REJECT) }, null,
            getString(R.string.label_yes), getString(R.string.label_cancel)
        )
    }

    private fun updateUserDetails(dataItem: User) {
        if (!isInternetAvailable()) {
            showMessage(getString(R.string.msg_no_internet))
            return
        }

        val userId = dataItem.id
        if (userId.isNullOrBlank()) {
            DebugLog.e("Truck id found null")
            return
        }

        viewModel?.updateUserDetails(userId, dataItem)
            ?.observe(viewLifecycleOwner, observeUpdateUser(dataItem.verificationStatus, dataItem))
    }

    private fun observeUpdateUser(verificationStatus: String?, dataItem: User) =
        Observer<DataBound<ApiMessage>> {
            if (it == null) {
                DebugLog.e("ApiMessage is null")
                return@Observer
            }

            it.let { dataBound ->
                when (dataBound) {
                    is DataBound.Success -> {
                        progressBar.visibility = View.GONE
                        showMessage("${dataBound.data.message}")
                        getData()
                        when (verificationStatus) {
                            User.USER_STATUS_REJECT -> statusChangedListener?.onRejected(dataItem)
                            User.USER_STATUS_CONFIRMED -> statusChangedListener?.onConfirmed(
                                dataItem
                            )
                            User.USER_STATUS_PENDING -> statusChangedListener?.onPending(dataItem)
                            else -> DebugLog.e("Wrong Status Found: $verificationStatus")
                        }
                    }
                    is DataBound.Error -> {
                        progressBar.visibility = View.GONE
                        if (dataBound.code == HttpURLConnection.HTTP_NOT_FOUND) {
                            showMessage(getString(R.string.error_something_went_wrong))
                        } else {
                            DebugLog.e("Error: ${dataBound.message}")
                            showMessage("${dataBound.message}")
                        }
                    }
                    is DataBound.Retry -> {
                        if (canRetryApiCall) {
                            getAuthTokenFromFirebase(requireActivity(), object : RetryListener {
                                override fun retry() {
                                    initViewModel()
                                    progressBar.visibility = View.GONE
                                    showMessage(getString(R.string.error_something_went_wrong_try_again))
                                }
                            })
                        } else {
                            canRetryApiCall = false
                            progressBar.visibility = View.GONE
                            showMessage(getString(R.string.error_something_went_wrong))
                        }
                    }
                    is DataBound.Loading -> {
                        progressBar.visibility = View.VISIBLE
                    }
                }
            }
        }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.item_common_list_layout, container, false)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initViewModel()
    }

    private fun showSearchView() {
        searchView.visibility = View.VISIBLE
        searchView.searchEditText.hint = getString(R.string.msg_search_user_by_user_name)
        searchView.searchEditText.addTextChangedListener(textWatcher)
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
        setToolBarTitle(getString(R.string.label_manage_users))
        swipeRefreshLayout.setOnRefreshListener { getData() }
        recyclerview.layoutManager = LinearLayoutManager(activity, RecyclerView.VERTICAL, false)
        val offsetPx = resources.getDimension(R.dimen.default_space)
        val topOffsetDecoration = TopAndBottomOffset(offsetPx.toInt(), offsetPx.toInt())
        recyclerview.addItemDecoration(topOffsetDecoration)
        getData()
        showSearchView()
    }

    protected open fun getData(truckNumberKeyword: String? = null) {
        if (!isInternetAvailable()) {
            showMessage(getString(R.string.msg_no_internet))
            return
        }

        viewModel?.getUserList(User.USER_STATUS_PENDING, truckNumberKeyword)
            ?.observe(viewLifecycleOwner, observeUserList)
    }

    protected var observeUserList = Observer<DataBound<MutableList<User>>> {
        if (it == null) {
            DebugLog.e("ApiMessage is null")
            return@Observer
        }

        it.let { dataBound ->
            when (dataBound) {
                is DataBound.Success -> {
                    swipeRefreshLayout.isRefreshing = false
                    progressBar.visibility = View.GONE
                    showData(dataBound.data)
                }
                is DataBound.Error -> {
                    swipeRefreshLayout.isRefreshing = false
                    progressBar.visibility = View.GONE
                    if (dataBound.code == HttpURLConnection.HTTP_NOT_FOUND) {
                        showMessage(getString(R.string.error_something_went_wrong))
                    } else {
                        DebugLog.e("Error: ${dataBound.message}")
                        showMessage("${dataBound.message}")
                    }
                }
                is DataBound.Retry -> {
                    if (canRetryApiCall) {
                        getAuthTokenFromFirebase(requireActivity(), object : RetryListener {
                            override fun retry() {
                                initViewModel()
                                swipeRefreshLayout.isRefreshing = false
                                progressBar.visibility = View.GONE
                                showMessage(getString(R.string.error_something_went_wrong_try_again))
                            }
                        })
                    } else {
                        canRetryApiCall = false
                        swipeRefreshLayout.isRefreshing = false
                        progressBar.visibility = View.GONE
                        showMessage(getString(R.string.error_something_went_wrong))
                    }
                }
                is DataBound.Loading -> {
                    swipeRefreshLayout.isRefreshing = false
                    progressBar.visibility = View.VISIBLE
                }
            }
        }
    }

    private fun showData(data: MutableList<User>) {
        if (userAdapter == null) {
            userAdapter = UserListAdapter(data as MutableList<Any>)
            userAdapter?.onMoreClickListener = onMoreClickListener
            recyclerview.adapter = userAdapter
        } else {
            userAdapter?.clearList()
            userAdapter?.addAllItems(data as MutableList<Any>)
        }

        if (userAdapter?.itemCount ?: 0 <= 0) {
            showMessageView(getString(R.string.msg_users_not_available))
        } else {
            hideMessageView()
        }
    }

    private fun showMessageView(message: String) {
        emptyView.visibility = View.VISIBLE
        val errorTitle = emptyView.findViewById<TextView>(R.id.messageTitle)
        errorTitle?.text = message
        recyclerview.visibility = View.GONE
    }

    private fun hideMessageView() {
        emptyView?.visibility = View.GONE
        recyclerview.visibility = View.VISIBLE
    }

    companion object {
        const val ACTION_REJECT = 111
        const val ACTION_APPROVE = 222
        const val ACTION_PENDING = 333
    }
}