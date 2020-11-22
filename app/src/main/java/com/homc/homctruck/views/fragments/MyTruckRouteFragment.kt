package com.homc.homctruck.views.fragments

import android.os.Bundle
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.homc.homctruck.R
import com.homc.homctruck.data.models.ApiMessage
import com.homc.homctruck.data.models.TruckRoute
import com.homc.homctruck.data.repositories.TruckRepository
import com.homc.homctruck.data.sourceremote.TruckRemoteDataSource
import com.homc.homctruck.restapi.AppApiInstance
import com.homc.homctruck.restapi.DataBound
import com.homc.homctruck.utils.*
import com.homc.homctruck.viewmodels.TruckViewModel
import com.homc.homctruck.viewmodels.TruckViewModelFactory
import com.homc.homctruck.views.activities.RetryListener
import com.homc.homctruck.views.adapters.AdapterDataItem
import com.homc.homctruck.views.adapters.BaseAdapter
import com.homc.homctruck.views.adapters.TruckRouteListAdapter
import com.homc.homctruck.views.dialogs.BottomSheetListDialogFragment
import com.homc.homctruck.views.dialogs.BottomSheetViewData
import com.homc.homctruck.views.dialogs.BottomSheetViewItem
import com.homc.homctruck.views.dialogs.BottomSheetViewSection
import kotlinx.android.synthetic.main.item_common_list_layout.*
import kotlinx.android.synthetic.main.item_search_view.view.*
import java.net.HttpURLConnection

open class MyTruckRouteFragment : BaseAppFragment() {

    protected var viewModel: TruckViewModel? = null
    private var truckRouteAdapter: TruckRouteListAdapter? = null
    protected var navigationController: NavController? = null
    private var bottomSheetListDialogFragment: BottomSheetListDialogFragment? = null

    private val onMoreClickListener = View.OnClickListener {
        val dataItem = it.tag
        if (dataItem !is TruckRoute) {
            DebugLog.e("Wrong instance found. Expected: ${TruckRoute::class.java.simpleName} Found: $dataItem")
            return@OnClickListener
        }

        showMoreOptionBottomSheet(dataItem)
    }

    private val textWatcher: TextWatcher? = object : TextWatcher {
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            if (count > 2 || count == 0) {
                getData(s.toString())
            }
        }

        override fun afterTextChanged(s: android.text.Editable?) {}
    }

    private val onPlankButtonClickListener = View.OnClickListener {
        navigationController?.navigate(R.id.action_myTruckRouteFragment_to_myPastTruckRouteFragment)
    }

    private fun showMoreOptionBottomSheet(dataItem: TruckRoute) {
        val sectionItems = mutableListOf<BottomSheetViewItem>()
        sectionItems.add(
            BottomSheetViewItem(
                ACTION_ID_EDIT, R.drawable.ic_edit_black,
                getString(R.string.label_edit_truck_route), null, dataItem
            )
        )
        sectionItems.add(
            BottomSheetViewItem(
                ACTION_ID_DELETE, R.drawable.ic_delete_black,
                getString(R.string.label_delete_truck_route), null, dataItem
            )
        )
        val sections = mutableListOf<BottomSheetViewSection>()
        sections.add(BottomSheetViewSection(viewItems = sectionItems))
        val bottomSheetViewData = BottomSheetViewData(bottomSheetViewSections = sections)
        bottomSheetListDialogFragment =
            BottomSheetListDialogFragment(bottomSheetViewData, onMoreOptionClickListener)
        parentFragmentManager.let { bottomSheetListDialogFragment?.show(it, "EDIT/DELETE") }
    }

    private val onMoreOptionClickListener: View.OnClickListener = View.OnClickListener {
        bottomSheetListDialogFragment?.dismiss()
        if (!canHaveFeatureAccess((requireContext()))) return@OnClickListener
        val dataItem = it.tag
        if (dataItem !is TruckRoute) {
            DebugLog.e("Wrong instance found. Expected: ${TruckRoute::class.java.simpleName} Found: $dataItem")
            return@OnClickListener
        }
        when (it.id) {
            ACTION_ID_EDIT -> editTruckRouteItem(dataItem)
            ACTION_ID_DELETE -> deleteTruckRouteItem(dataItem)
            else -> DebugLog.e("Id not matched")
        }
    }

    protected open fun editTruckRouteItem(dataItem: TruckRoute) {
        TemporaryCache.put(EditTruckRouteFragment.EXTRA_TRUCK_ROUTE_DETAILS, dataItem)
        navigationController?.navigate(R.id.action_myTruckRouteFragment_to_editTruckRouteFragment)
    }

    private fun deleteTruckRouteItem(dataItem: TruckRoute) {
        showConfirmDialog(
            requireActivity(), getString(R.string.msg_confirm_delete),
            { _, _ -> deleteTruck(dataItem) }, null,
            getString(R.string.label_yes), getString(R.string.label_cancel)
        )
    }

    private fun deleteTruck(dataItem: TruckRoute) {
        if (!isInternetAvailable()) {
            showMessage(getString(R.string.msg_no_internet))
            return
        }

        val truckRouteId = dataItem.id
        if (truckRouteId.isNullOrBlank()) {
            DebugLog.e("Truck route id found null")
            return
        }

        viewModel?.deleteTruckRoute(truckRouteId)
            ?.observe(viewLifecycleOwner, observeDeleteTruckRoute)
    }

    private var observeDeleteTruckRoute = Observer<DataBound<ApiMessage>> {
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
        setToolBarTitle(getString(R.string.label_my_truck_routes))
        navigationController = Navigation.findNavController(requireView())
        swipeRefreshLayout.setOnRefreshListener { getData() }
        recyclerview.layoutManager = LinearLayoutManager(activity, RecyclerView.VERTICAL, false)
        val offsetPx = resources.getDimension(R.dimen.default_space)
        val topOffsetDecoration = TopAndBottomOffset(offsetPx.toInt(), offsetPx.toInt())
        recyclerview.addItemDecoration(topOffsetDecoration)
        buttonBackView.visibility = View.VISIBLE
        bottomButton.visibility = View.VISIBLE
        bottomButton.text = getString(R.string.menu_add_truck_route)
        bottomButton.setOnClickListener {
            if (!canHaveFeatureAccess((requireContext()))) return@setOnClickListener
            onAddTruckRouteClicked()
        }
        getData()
        showSearchView()
    }

    private fun showSearchView() {
        searchView.visibility = View.VISIBLE
        searchView.searchEditText.hint = getString(R.string.msg_search_truck_route_by_truck_number)
        searchView.searchEditText.addTextChangedListener(textWatcher)
    }

    private fun onAddTruckRouteClicked() {
        navigationController?.navigate(R.id.action_myTruckRouteFragment_to_addTruckRouteFragment)
    }

    protected open fun getData(truckNumberKeyword: String? = null) {
        if (!isInternetAvailable()) {
            showMessage(getString(R.string.msg_no_internet))
            return
        }

        viewModel?.getMyTruckRouteList(truckNumberKeyword)
            ?.observe(viewLifecycleOwner, observeTruckRouteList)
    }

    protected var observeTruckRouteList = Observer<DataBound<MutableList<TruckRoute>>> {
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

    private fun showData(data: MutableList<TruckRoute>) {
        addPastTruckRoutePlank(data as MutableList<Any>)
        truckRouteAdapter = TruckRouteListAdapter(data as MutableList<Any>)
        truckRouteAdapter?.onMoreClickListener = onMoreClickListener
        truckRouteAdapter?.onPlankButtonClickListener = onPlankButtonClickListener
        recyclerview.adapter = truckRouteAdapter

        if (truckRouteAdapter?.itemCount ?: getPickCountToShowError() <= getPickCountToShowError()) {
            showMessageView(getString(R.string.msg_truck_routes_not_added_yet))
        } else {
            hideMessageView()
        }
    }

    protected open fun getPickCountToShowError(): Int {
        return 1
    }

    protected open fun addPastTruckRoutePlank(data: MutableList<Any>) {
        data.add(
            0,
            AdapterDataItem(
                BaseAdapter.VIEW_TYPE_PLANK_BUTTON,
                getString(R.string.label_past_truck_routes)
            )
        )
    }

    private fun showMessageView(message: String) {
        emptyView.visibility = View.VISIBLE
        val errorTitle = emptyView.findViewById<TextView>(R.id.messageTitle)
        errorTitle?.text = message
        // recyclerview.visibility = View.GONE
    }

    private fun hideMessageView() {
        emptyView?.visibility = View.GONE
        // recyclerview.visibility = View.VISIBLE
    }

    companion object {
        const val ACTION_ID_EDIT = 111
        const val ACTION_ID_DELETE = 222
    }
}