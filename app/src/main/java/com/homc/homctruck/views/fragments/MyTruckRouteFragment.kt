package com.homc.homctruck.views.fragments

import android.os.Bundle
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
import com.homc.homctruck.di.DaggerAppComponent
import com.homc.homctruck.di.modules.AppModule
import com.homc.homctruck.di.modules.ViewModelModule
import com.homc.homctruck.restapi.DataBound
import com.homc.homctruck.utils.*
import com.homc.homctruck.viewmodels.TruckViewModel
import com.homc.homctruck.views.adapters.TruckRouteListAdapter
import com.homc.homctruck.views.dialogs.BottomSheetListDialogFragment
import com.homc.homctruck.views.dialogs.BottomSheetViewData
import com.homc.homctruck.views.dialogs.BottomSheetViewItem
import com.homc.homctruck.views.dialogs.BottomSheetViewSection
import kotlinx.android.synthetic.main.item_common_list_layout.*
import java.net.HttpURLConnection
import javax.inject.Inject

class MyTruckRouteFragment : BaseAppFragment() {

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory
    private var viewModel: TruckViewModel? = null
    private var truckRouteAdapter: TruckRouteListAdapter? = null
    private var navigationController: NavController? = null
    private var bottomSheetListDialogFragment: BottomSheetListDialogFragment? = null

    private val onMoreClickListener = View.OnClickListener {
        val dataItem = it.tag
        if (dataItem !is TruckRoute) {
            DebugLog.e("Wrong instance found. Expected: ${TruckRoute::class.java.simpleName} Found: $dataItem")
            return@OnClickListener
        }

        showMoreOptionBottomSheet(dataItem)
    }

    private fun showMoreOptionBottomSheet(dataItem: TruckRoute) {
        val sectionItems = ArrayList<BottomSheetViewItem>()
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
        val sections = ArrayList<BottomSheetViewSection>()
        sections.add(BottomSheetViewSection(viewItems = sectionItems))
        val bottomSheetViewData = BottomSheetViewData(bottomSheetViewSections = sections)
        bottomSheetListDialogFragment =
            BottomSheetListDialogFragment(bottomSheetViewData, onMoreOptionClickListener)
        parentFragmentManager.let { bottomSheetListDialogFragment?.show(it, "EDIT/DELETE") }
    }

    private val onMoreOptionClickListener: View.OnClickListener = View.OnClickListener {
        bottomSheetListDialogFragment?.dismiss()
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

    private fun editTruckRouteItem(dataItem: TruckRoute) {
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
                        DebugLog.e("Error: ${dataBound.error}")
                        showMessage("${dataBound.error}")
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
        DaggerAppComponent.builder().viewModelModule(ViewModelModule())
            .appModule(AppModule(requireActivity().application)).build().inject(this)
        viewModel = ViewModelProvider(this, viewModelFactory)[TruckViewModel::class.java]
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setToolBarTitle(getString(R.string.label_my_truck_routes))
        navigationController = Navigation.findNavController(requireView())
        swipeRefreshLayout.setOnRefreshListener { getData() }
        recyclerview.layoutManager = LinearLayoutManager(activity, RecyclerView.VERTICAL, false)
        val offsetPx = resources.getDimension(R.dimen.default_space)
        val topOffsetDecoration = TopAndBottomOffset(0, offsetPx.toInt())
        recyclerview.addItemDecoration(topOffsetDecoration)
        bottomButton.visibility = View.VISIBLE
        bottomButton.text = getString(R.string.menu_add_truck_route)
        bottomButton.setOnClickListener {
            onAddTruckRouteClicked()
        }
        getData()
    }

    private fun onAddTruckRouteClicked() {
        navigationController?.navigate(R.id.action_myTruckRouteFragment_to_addTruckRouteFragment)
    }

    private fun getData() {
        if (!isInternetAvailable()) {
            showMessage(getString(R.string.msg_no_internet))
            return
        }

        viewModel?.getUserTruckRouteList()
            ?.observe(viewLifecycleOwner, observeTruckRouteList)
    }

    private var observeTruckRouteList = Observer<DataBound<MutableList<TruckRoute>>> {
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
                        DebugLog.e("Error: ${dataBound.error}")
                        showMessage("${dataBound.error}")
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
        truckRouteAdapter = TruckRouteListAdapter(data as MutableList<Any>)
        truckRouteAdapter?.onMoreClickListener = onMoreClickListener
        recyclerview.adapter = truckRouteAdapter

        if (truckRouteAdapter?.itemCount == 0 || truckRouteAdapter?.itemCount == -1) {
            showMessageView(getString(R.string.msg_truck_routes_not_added_yet))
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
        const val ACTION_ID_EDIT = 111
        const val ACTION_ID_DELETE = 222
    }
}