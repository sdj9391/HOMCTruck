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
import com.homc.homctruck.data.repositories.TruckRepository
import com.homc.homctruck.data.sourceremote.TruckRemoteDataSource
import com.homc.homctruck.restapi.AppApiInstance
import com.homc.homctruck.restapi.DataBound
import com.homc.homctruck.utils.*
import com.homc.homctruck.viewmodels.TruckViewModel
import com.homc.homctruck.viewmodels.TruckViewModelFactory
import com.homc.homctruck.views.adapters.TruckListAdapter
import com.homc.homctruck.views.dialogs.BottomSheetListDialogFragment
import com.homc.homctruck.views.dialogs.BottomSheetViewData
import com.homc.homctruck.views.dialogs.BottomSheetViewItem
import com.homc.homctruck.views.dialogs.BottomSheetViewSection
import kotlinx.android.synthetic.main.item_common_list_layout.*
import kotlinx.android.synthetic.main.item_search_view.view.*
import java.net.HttpURLConnection

open class PendingTruckListFragment : BaseAppFragment() {

    protected var viewModel: TruckViewModel? = null
    private var truckAdapter: TruckListAdapter? = null
    private var bottomSheetListDialogFragment: BottomSheetListDialogFragment? = null

    private val textWatcher: TextWatcher? = object : TextWatcher {
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            if (count > 2 || count == 0) {
                getData(s.toString())
            }
        }

        override fun afterTextChanged(s: android.text.Editable?) {}
    }

    var statusChangedListener: StatusChangedListener? = null
    val refreshListener = object : RefreshListener {
        override fun onRefresh(dataItem: Any) {
            truckAdapter?.addItem(dataItem)
            hideMessageView()
        }
    }

    private val onMoreClickListener = View.OnClickListener {
        val dataItem = it.tag
        if (dataItem !is Truck) {
            DebugLog.e("Wrong instance found. Expected: ${Truck::class.java.simpleName} Found: $dataItem")
            return@OnClickListener
        }

        showMoreOptionBottomSheet(dataItem)
    }

    protected fun getBottomSheetViewForApproveAction(dataItem: Truck): BottomSheetViewItem {
        return BottomSheetViewItem(
            ACTION_APPROVE, R.drawable.ic_truck_green,
            getString(R.string.label_approve_truck), null, dataItem
        )
    }

    protected fun getBottomSheetViewForRejectAction(dataItem: Truck): BottomSheetViewItem {
        return BottomSheetViewItem(
            ACTION_REJECT, R.drawable.ic_truck_red,
            getString(R.string.label_reject_truck), null, dataItem
        )
    }

    protected fun getBottomSheetViewForPendingAction(dataItem: Truck): BottomSheetViewItem {
        return BottomSheetViewItem(
            ACTION_PENDING, R.drawable.ic_truck_yellow,
            getString(R.string.label_move_truck_to_pending), null, dataItem
        )
    }

    protected open fun getBottomSheetOption(dataItem: Truck): MutableList<BottomSheetViewItem> {
        val sectionItems = mutableListOf<BottomSheetViewItem>()
        sectionItems.add(getBottomSheetViewForApproveAction(dataItem))
        sectionItems.add(getBottomSheetViewForRejectAction(dataItem))
        return sectionItems
    }

    private fun showMoreOptionBottomSheet(dataItem: Truck) {
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
        if (dataItem !is Truck) {
            DebugLog.e("Wrong instance found. Expected: ${Truck::class.java.simpleName} Found: $dataItem")
            return@OnClickListener
        }
        when (it.id) {
            ACTION_APPROVE -> changeTruckStatus(dataItem, Truck.TRUCK_STATUS_CONFIRMED)
            ACTION_REJECT -> rejectTruck(dataItem)
            ACTION_PENDING -> changeTruckStatus(dataItem, Truck.TRUCK_STATUS_PENDING)
            else -> DebugLog.e("Id not matched")
        }
    }

    private fun changeTruckStatus(dataItem: Truck, truckStatus: String) {
        dataItem.verificationStatus = truckStatus
        updateTruckDetails(dataItem)
    }

    private fun rejectTruck(dataItem: Truck) {
        showConfirmDialog(
            requireActivity(), getString(R.string.msg_confirm_reject),
            { _, _ -> changeTruckStatus(dataItem, Truck.TRUCK_STATUS_REJECT) }, null,
            getString(R.string.label_yes), getString(R.string.label_cancel)
        )
    }

    private fun updateTruckDetails(dataItem: Truck) {
        if (!isInternetAvailable()) {
            showMessage(getString(R.string.msg_no_internet))
            return
        }

        val userId = dataItem.id
        if (userId.isNullOrBlank()) {
            DebugLog.e("Truck id found null")
            return
        }

        viewModel?.updateTruckDetails(userId, dataItem)
            ?.observe(viewLifecycleOwner, observeUpdateTruck(dataItem.verificationStatus, dataItem))
    }

    private fun observeUpdateTruck(verificationStatus: String?, dataItem: Truck) =
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
                            Truck.TRUCK_STATUS_REJECT -> statusChangedListener?.onRejected(dataItem)
                            Truck.TRUCK_STATUS_CONFIRMED -> statusChangedListener?.onConfirmed(
                                dataItem
                            )
                            Truck.TRUCK_STATUS_PENDING -> statusChangedListener?.onPending(dataItem)
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

    private fun initViewModel() {
        val repository =
            TruckRepository(TruckRemoteDataSource(AppApiInstance.api(getAuthToken(requireActivity()))))
        val viewModelFactory = TruckViewModelFactory(requireActivity().application, repository)
        viewModel = ViewModelProvider(this, viewModelFactory)[TruckViewModel::class.java]
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        swipeRefreshLayout.setOnRefreshListener { getData() }
        recyclerview.layoutManager = LinearLayoutManager(activity, RecyclerView.VERTICAL, false)
        val offsetPx = resources.getDimension(R.dimen.default_space)
        val topOffsetDecoration = TopAndBottomOffset(offsetPx.toInt(), offsetPx.toInt())
        recyclerview.addItemDecoration(topOffsetDecoration)
        getData()
        showSearchView()
    }

    private fun showSearchView() {
        searchView.visibility = View.VISIBLE
        searchView.searchEditText.addTextChangedListener(textWatcher)
    }

    protected open fun getData(truckNumberKeyword: String? = null) {
        if (!isInternetAvailable()) {
            showMessage(getString(R.string.msg_no_internet))
            return
        }

        viewModel?.getTruckList(Truck.TRUCK_STATUS_PENDING, truckNumberKeyword)
            ?.observe(viewLifecycleOwner, observeTruckList)
    }

    protected var observeTruckList = Observer<DataBound<MutableList<Truck>>> {
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

    private fun showData(data: MutableList<Truck>) {
        truckAdapter = TruckListAdapter(data as MutableList<Any>)
        truckAdapter?.onMoreClickListener = onMoreClickListener
        recyclerview.adapter = truckAdapter

        if (truckAdapter?.itemCount ?: 0 <= 0) {
            showMessageView(getString(R.string.msg_trucks_not_available))
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