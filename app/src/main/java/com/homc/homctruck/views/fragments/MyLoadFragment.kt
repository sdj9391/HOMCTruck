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
import com.homc.homctruck.data.models.Load
import com.homc.homctruck.data.repositories.LoadRepository
import com.homc.homctruck.data.sourceremote.LoadRemoteDataSource
import com.homc.homctruck.restapi.AppApiInstance
import com.homc.homctruck.restapi.DataBound
import com.homc.homctruck.utils.*
import com.homc.homctruck.viewmodels.LoadViewModel
import com.homc.homctruck.viewmodels.LoadViewModelFactory
import com.homc.homctruck.views.activities.RetryListener
import com.homc.homctruck.views.adapters.AdapterDataItem
import com.homc.homctruck.views.adapters.BaseAdapter
import com.homc.homctruck.views.adapters.LoadListAdapter
import com.homc.homctruck.views.dialogs.BottomSheetListDialogFragment
import com.homc.homctruck.views.dialogs.BottomSheetViewData
import com.homc.homctruck.views.dialogs.BottomSheetViewItem
import com.homc.homctruck.views.dialogs.BottomSheetViewSection
import kotlinx.android.synthetic.main.item_common_list_layout.*
import kotlinx.android.synthetic.main.item_search_view.view.*
import java.net.HttpURLConnection

open class MyLoadFragment : BaseAppFragment() {

    protected var viewModel: LoadViewModel? = null
    private var loadAdapter: LoadListAdapter? = null
    protected var navigationController: NavController? = null
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

    private val onMoreClickListener = View.OnClickListener {
        val dataItem = it.tag
        if (dataItem !is Load) {
            DebugLog.e("Wrong instance found. Expected: ${Load::class.java.simpleName} Found: $dataItem")
            return@OnClickListener
        }

        showMoreOptionBottomSheet(dataItem)
    }

    private val onPlankButtonClickListener = View.OnClickListener {
        navigationController?.navigate(R.id.action_myLoadFragment_to_myPastLoadFragment)
    }

    private fun showMoreOptionBottomSheet(dataItem: Load) {
        val sectionItems = mutableListOf<BottomSheetViewItem>()
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

    protected open fun editLoadItem(dataItem: Load) {
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
            LoadRepository(LoadRemoteDataSource(AppApiInstance.api(getAuthToken(requireActivity()))))
        val viewModelFactory = LoadViewModelFactory(requireActivity().application, repository)
        viewModel = ViewModelProvider(this, viewModelFactory)[LoadViewModel::class.java]
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setToolBarTitle(getString(R.string.label_my_loads))
        navigationController = Navigation.findNavController(requireView())
        swipeRefreshLayout.setOnRefreshListener { getData() }
        recyclerview.layoutManager = LinearLayoutManager(activity, RecyclerView.VERTICAL, false)
        val offsetPx = resources.getDimension(R.dimen.default_space)
        val topOffsetDecoration = TopAndBottomOffset(offsetPx.toInt(), offsetPx.toInt())
        recyclerview.addItemDecoration(topOffsetDecoration)
        buttonBackView.visibility = View.VISIBLE
        bottomButton.visibility = View.VISIBLE
        bottomButton.text = getString(R.string.menu_add_load)
        bottomButton.setOnClickListener {
            if (!canHaveFeatureAccess((requireContext()))) return@setOnClickListener
            onAddLoadClicked()
        }
        getData()
        showSearchView()
    }

    private fun showSearchView() {
        searchView.visibility = View.VISIBLE
        searchView.searchEditText.hint = getString(R.string.msg_search_load_by_material_type)
        searchView.searchEditText.addTextChangedListener(textWatcher)
    }

    private fun onAddLoadClicked() {
        navigationController?.navigate(R.id.action_myLoadFragment_to_addLoadFragment)
    }

    protected open fun getData(materialKeyword: String? = null) {
        if (!isInternetAvailable()) {
            showMessage(getString(R.string.msg_no_internet))
            return
        }

        viewModel?.getMyLoadList(materialKeyword)
            ?.observe(viewLifecycleOwner, observeLoadList)
    }

    protected var observeLoadList = Observer<DataBound<MutableList<Load>>> {
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

    private fun showData(data: MutableList<Load>) {
        addPastLoadPlank(data as MutableList<Any>)
        loadAdapter = LoadListAdapter(data as MutableList<Any>)
        loadAdapter?.onMoreClickListener = onMoreClickListener
        loadAdapter?.onPlankButtonClickListener = onPlankButtonClickListener
        recyclerview.adapter = loadAdapter

        if (loadAdapter?.itemCount ?: getPickCountToShowError() <= getPickCountToShowError()) {
            showMessageView(getString(R.string.msg_loads_not_added_yet))
        } else {
            hideMessageView()
        }
    }

    protected open fun getPickCountToShowError(): Int {
        return 1
    }

    protected open fun addPastLoadPlank(data: MutableList<Any>) {
        data.add(
            0,
            AdapterDataItem(
                BaseAdapter.VIEW_TYPE_PLANK_BUTTON,
                getString(R.string.label_past_loads)
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