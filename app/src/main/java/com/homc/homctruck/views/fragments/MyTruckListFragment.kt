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
import com.homc.homctruck.data.models.Truck
import com.homc.homctruck.di.DaggerAppComponent
import com.homc.homctruck.di.modules.AppModule
import com.homc.homctruck.di.modules.ViewModelModule
import com.homc.homctruck.restapi.DataBound
import com.homc.homctruck.utils.*
import com.homc.homctruck.viewmodels.TruckViewModel
import com.homc.homctruck.views.adapters.TruckListAdapter
import com.homc.homctruck.views.dialogs.BottomSheetListDialogFragment
import com.homc.homctruck.views.dialogs.BottomSheetViewData
import com.homc.homctruck.views.dialogs.BottomSheetViewItem
import com.homc.homctruck.views.dialogs.BottomSheetViewSection
import kotlinx.android.synthetic.main.item_common_list_layout.*
import java.net.HttpURLConnection
import javax.inject.Inject

class MyTruckListFragment : BaseAppFragment() {

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory
    private var viewModel: TruckViewModel? = null
    private var truckAdapter: TruckListAdapter? = null
    private var navigationController: NavController? = null
    private var bottomSheetListDialogFragment: BottomSheetListDialogFragment? = null

    private val onMoreClickListener = View.OnClickListener {
        val dataItem = it.tag
        if (dataItem !is Truck) {
            DebugLog.e("Wrong instance found. Expected: ${Truck::class.java.simpleName} Found: $dataItem")
            return@OnClickListener
        }

        showMoreOptionBottomSheet(dataItem)
    }

    private fun showMoreOptionBottomSheet(dataItem: Truck) {
        val sectionItems = ArrayList<BottomSheetViewItem>()
        sectionItems.add(
            BottomSheetViewItem(
                ACTION_ID_EDIT, R.drawable.ic_edit_black,
                getString(R.string.label_edit_truck), null, dataItem
            )
        )
        sectionItems.add(
            BottomSheetViewItem(
                ACTION_ID_DELETE, R.drawable.ic_delete_black,
                getString(R.string.label_delete_truck), null, dataItem
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
        if (dataItem !is Truck) {
            DebugLog.e("Wrong instance found. Expected: ${Truck::class.java.simpleName} Found: $dataItem")
            return@OnClickListener
        }
        when (it.id) {
            ACTION_ID_EDIT -> editTruckItem(dataItem)
            ACTION_ID_DELETE -> deleteTruckItem(dataItem)
            else -> DebugLog.e("Id not matched")
        }
    }

    private fun editTruckItem(dataItem: Truck) {
        TemporaryCache.put(EditTruckFragment.EXTRA_TRUCK_DETAILS, dataItem)
        navigationController?.navigate(R.id.action_userTruckListFragment_to_editTruckFragment)
    }

    private fun deleteTruckItem(dataItem: Truck) {
        showConfirmDialog(
            requireActivity(), getString(R.string.msg_confirm_delete),
            { _, _ -> deleteTruck(dataItem) }, null,
            getString(R.string.label_yes), getString(R.string.label_cancel)
        )
    }

    private fun deleteTruck(dataItem: Truck) {
        if (!isInternetAvailable()) {
            showMessage(getString(R.string.msg_no_internet))
            return
        }

        val truckId = dataItem.id
        if (truckId.isNullOrBlank()) {
            DebugLog.e("Truck id found null")
            return
        }

        viewModel?.deleteTruck(truckId)
            ?.observe(viewLifecycleOwner, observeDeleteTruck)
    }

    private var observeDeleteTruck = Observer<DataBound<ApiMessage>> {
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
        setToolBarTitle(getString(R.string.label_my_trucks))
        navigationController = Navigation.findNavController(requireView())
        swipeRefreshLayout.setOnRefreshListener { getData() }
        recyclerview.layoutManager = LinearLayoutManager(activity, RecyclerView.VERTICAL, false)
        val offsetPx = resources.getDimension(R.dimen.default_space)
        val topOffsetDecoration = TopAndBottomOffset(0, offsetPx.toInt())
        recyclerview.addItemDecoration(topOffsetDecoration)
        bottomButton.visibility = View.VISIBLE
        bottomButton.text = getString(R.string.label_add_truck)
        bottomButton.setOnClickListener {
            onAddTruckClicked()
        }
        getData()
    }

    private fun onAddTruckClicked() {
        navigationController?.navigate(R.id.action_userTruckListFragment_to_addTruckFragment)
    }

    private fun getData() {
        if (!isInternetAvailable()) {
            showMessage(getString(R.string.msg_no_internet))
            return
        }

        viewModel?.getMyTruckList()
            ?.observe(viewLifecycleOwner, observeTruckList)
    }

    private var observeTruckList = Observer<DataBound<MutableList<Truck>>> {
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

    private fun showData(data: MutableList<Truck>) {
        truckAdapter = TruckListAdapter(data as MutableList<Any>)
        truckAdapter?.onMoreClickListener = onMoreClickListener
        recyclerview.adapter = truckAdapter

        if (truckAdapter?.itemCount == 0 || truckAdapter?.itemCount == -1) {
            showMessageView(getString(R.string.msg_add_truck_to_showcase))
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