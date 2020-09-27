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
import com.homc.homctruck.data.models.Load
import com.homc.homctruck.di.DaggerAppComponent
import com.homc.homctruck.di.modules.AppModule
import com.homc.homctruck.di.modules.ViewModelModule
import com.homc.homctruck.restapi.DataBound
import com.homc.homctruck.utils.*
import com.homc.homctruck.viewmodels.LoadViewModel
import com.homc.homctruck.views.adapters.LoadListAdapter
import com.homc.homctruck.views.dialogs.BottomSheetListDialogFragment
import com.homc.homctruck.views.dialogs.BottomSheetViewData
import com.homc.homctruck.views.dialogs.BottomSheetViewItem
import com.homc.homctruck.views.dialogs.BottomSheetViewSection
import kotlinx.android.synthetic.main.item_common_list_layout.*
import java.net.HttpURLConnection
import javax.inject.Inject

class MyLoadFragment : BaseAppFragment() {

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory
    private var viewModel: LoadViewModel? = null
    private var loadAdapter: LoadListAdapter? = null
    private var navigationController: NavController? = null
    private var bottomSheetListDialogFragment: BottomSheetListDialogFragment? = null

    private val onMoreClickListener = View.OnClickListener {
        val dataItem = it.tag
        if (dataItem !is Load) {
            DebugLog.e("Wrong instance found. Expected: ${Load::class.java.simpleName} Found: $dataItem")
            return@OnClickListener
        }

        showMoreOptionBottomSheet(dataItem)
    }

    private fun showMoreOptionBottomSheet(dataItem: Load) {
        val sectionItems = ArrayList<BottomSheetViewItem>()
        sectionItems.add(
            BottomSheetViewItem(
                ACTION_ID_EDIT, R.drawable.ic_edit_black,
                getString(R.string.label_edit_load), null, dataItem
            )
        )
        sectionItems.add(
            BottomSheetViewItem(
                ACTION_ID_DELETE, R.drawable.ic_delete_black,
                getString(R.string.label_delete_load), null, dataItem
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
        if (dataItem !is Load) {
            DebugLog.e("Wrong instance found. Expected: ${Load::class.java.simpleName} Found: $dataItem")
            return@OnClickListener
        }
        when (it.id) {
            ACTION_ID_EDIT -> editLoadItem(dataItem)
            ACTION_ID_DELETE -> deleteLoadItem(dataItem)
            else -> DebugLog.e("Id not matched")
        }
    }

    private fun editLoadItem(dataItem: Load) {
        TemporaryCache.put(EditLoadFragment.EXTRA_LOAD_DETAILS, dataItem)
        navigationController?.navigate(R.id.action_myLoadFragment_to_editLoadFragment)
    }

    private fun deleteLoadItem(dataItem: Load) {
        showConfirmDialog(
            requireActivity(), getString(R.string.msg_confirm_delete),
            { _, _ -> deleteLoad(dataItem) }, null,
            getString(R.string.label_yes), getString(R.string.label_cancel)
        )
    }

    private fun deleteLoad(dataItem: Load) {
        if (!isInternetAvailable()) {
            showMessage(getString(R.string.msg_no_internet))
            return
        }

        val loadId = dataItem.id
        if (loadId.isNullOrBlank()) {
            DebugLog.e("Truck id found null")
            return
        }

        viewModel?.deleteLoad(loadId)
            ?.observe(viewLifecycleOwner, observeDeleteLoad)
    }

    private var observeDeleteLoad = Observer<DataBound<ApiMessage>> {
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
        viewModel = ViewModelProvider(this, viewModelFactory)[LoadViewModel::class.java]
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setToolBarTitle(getString(R.string.label_my_loads))
        navigationController = Navigation.findNavController(requireView())
        swipeRefreshLayout.setOnRefreshListener { getData() }
        recyclerview.layoutManager = LinearLayoutManager(activity, RecyclerView.VERTICAL, false)
        val offsetPx = resources.getDimension(R.dimen.default_space)
        val topOffsetDecoration = TopAndBottomOffset(0, offsetPx.toInt())
        recyclerview.addItemDecoration(topOffsetDecoration)
        bottomButton.visibility = View.VISIBLE
        bottomButton.text = getString(R.string.menu_add_load)
        bottomButton.setOnClickListener {
            onAddLoadClicked()
        }
        getData()
    }

    private fun onAddLoadClicked() {
        navigationController?.navigate(R.id.action_myLoadFragment_to_addLoadFragment)
    }

    private fun getData() {
        if (!isInternetAvailable()) {
            showMessage(getString(R.string.msg_no_internet))
            return
        }

        viewModel?.getUserLoadList()
            ?.observe(viewLifecycleOwner, observeLoadList)
    }

    private var observeLoadList = Observer<DataBound<MutableList<Load>>> {
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

    private fun showData(data: MutableList<Load>) {
        loadAdapter = LoadListAdapter(data as MutableList<Any>)
        loadAdapter?.onMoreClickListener = onMoreClickListener
        recyclerview.adapter = loadAdapter

        if (loadAdapter?.itemCount == 0 || loadAdapter?.itemCount == -1) {
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