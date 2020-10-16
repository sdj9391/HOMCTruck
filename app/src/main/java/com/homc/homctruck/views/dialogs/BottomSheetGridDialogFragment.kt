package com.homc.homctruck.views.dialogs

import android.os.Bundle
import android.util.DisplayMetrics
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.GridLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.homc.homctruck.R
import kotlin.random.Random


/**
 * Steps to use this BottomSheetGridDialogFragment:
 * 1. Create List of `BottomSheetViewItem` for each section
 * 2. Create List of `BottomSheetViewSection` by adding `BottomSheetViewItem` and Section Title
 * 3. Create `BottomSheetViewData` by adding `BottomSheetViewSection` and Bottom Sheet Title
 * 4. Create object of `BottomSheetGridDialogFragment` by passing `BottomSheetViewData` and
 *    `onItemClickListener` for each bottom sheet view item
 * 5. Check view id into `onItemClickListener` to get which bottom sheet view item clicked
 *
 * NOTE: Ids passed into `BottomSheetViewItem` should be unique for each bottom sheet view item
 *
 * ** Check Reference Example in comment added at bottom
 */
open class BottomSheetGridDialogFragment(open var bottomSheetViewData: BottomSheetViewData,
                                         open var onItemClickListener: View.OnClickListener) : BottomSheetDialogFragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.custom_bottom_sheet_dialog_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        bindBottomSheetView()
    }

    /**
     * Method is use to bing BottomSheet
     */
    open fun bindBottomSheetView() {
        val displayMetrics = DisplayMetrics()
        (activity as AppCompatActivity).windowManager.defaultDisplay.getMetrics(displayMetrics)
        val itemWidthPx = displayMetrics.widthPixels / COLUMN_COUNT

        val title = bottomSheetViewData.title ?: String()
        var showTitle = true
        bottomSheetViewData.bottomSheetViewSections.forEach {
            bindSection(title, showTitle, it, itemWidthPx)
            showTitle = false
        }
    }

    /**
     * Method is use to bind each section of BottomSheet
     */
    private fun bindSection(title: String, showTitle: Boolean, bottomSheetViewSections: BottomSheetViewSection, itemWidthPx: Int) {
        val itemView: View = LayoutInflater.from(context)
                .inflate(R.layout.custom_bottom_sheet_dialog_section, null, false)

        val imageView = itemView.findViewById<View>(R.id.image_view)
        val titleTextView = itemView.findViewById<TextView>(R.id.text_view_title)
        val subTitleTextView = itemView.findViewById<TextView>(R.id.text_view_sub_title)
        val divider = itemView.findViewById<View>(R.id.divider)
        val gridLayout = itemView.findViewById<GridLayout>(R.id.grid_layout)
        gridLayout.columnCount = COLUMN_COUNT

        // Added condition to show hide title, closeButton and divider of each section
        if (showTitle) {
            divider.visibility = View.VISIBLE
            imageView.visibility = View.VISIBLE
            titleTextView.visibility = View.VISIBLE
            imageView.setOnClickListener { this.dismiss() }
            titleTextView.text = title
        } else {
            divider.visibility = View.GONE
            imageView.visibility = View.GONE
            titleTextView.visibility = View.GONE
        }

        val subTitle = bottomSheetViewSections.subtitle
        if (subTitle.isNullOrEmpty()) {
            subTitleTextView.visibility = View.GONE
        } else {
            subTitleTextView.visibility = View.VISIBLE
            subTitleTextView.text = subTitle
        }

        bottomSheetViewSections.viewItems.forEach {
            bindItem(gridLayout, it, itemWidthPx)
        }

        (view as LinearLayout).addView(itemView);
    }

    /**
     * Method is use to bind each bottom sheet view item
     */
    private fun bindItem(gridLayout: GridLayout, bottomSheetViewItem: BottomSheetViewItem, itemWidthPx: Int) {
        val itemView: View = LayoutInflater.from(context)
                .inflate(R.layout.bottom_sheet_dialog_grid_item, null, false)
        itemView.id = bottomSheetViewItem.id ?: Random.nextInt()
        val imageView = itemView.findViewById<ImageView>(R.id.image_view)
        val titleTextView = itemView.findViewById<TextView>(R.id.text_view_title)
        imageView.setImageResource(bottomSheetViewItem.icon ?: R.drawable.ic_arrow_forward_black)
        titleTextView.text = bottomSheetViewItem.title
        itemView.tag = bottomSheetViewItem.data
        itemView.setOnClickListener(onItemClickListener)
        gridLayout.addView(itemView)
        val layoutParam = itemView.layoutParams
        layoutParam.width = itemWidthPx
        itemView.layoutParams = layoutParam
    }

    companion object {
        const val COLUMN_COUNT = 3
    }
}

/**
 * It is use to hold the single plank data
 */
class BottomSheetViewItem(var id: Int?, var icon: Int?, var title: String?, var subTitle: String? = null, var data: Any? = null)

/**
 * It is use to hold the data of group of view items
 */
class BottomSheetViewSection(var subtitle: String? = null, var viewItems: MutableList<BottomSheetViewItem>)

/**
 * It is use to hold the data of complete BottomSheetView
 */
class BottomSheetViewData(var title: String? = null, var bottomSheetViewSections: MutableList<BottomSheetViewSection>)

/**
 * Step Example:
 *
 * // Step 1
 * val sectionItems1 = mutableListOf<BottomSheetViewItem>()
 * sectionItems1.add(BottomSheetViewItem(1, R.drawable.ic_icon1, "Item 1", null))
 * sectionItems1.add(BottomSheetViewItem(2, R.drawable.ic_icon2, "Item 2", null))
 *
 * val sectionItems2 = mutableListOf<BottomSheetViewItem>()
 * sectionItems2.add(BottomSheetViewItem(3, R.drawable.ic_icon3, "Item 3", null))
 * sectionItems2.add(BottomSheetViewItem(4, R.drawable.ic_icon4, "Item 4", null))
 *
 * // Step 2
 * val sections = mutableListOf<BottomSheetViewSection>()
 * sections.add(BottomSheetViewSection("Sections 1", sectionItems1))
 * sections.add(BottomSheetViewSection("Sections 2", sectionItems2))
 *
 * // Step 3
 * val bottomSheetViewData = BottomSheetViewData("BottomSheetView", sections)
 *
 * // Step 4
 * val bottomSheetGridDialogFragment = BottomSheetGridDialogFragment(bottomSheetViewData, onItemClickListener)
 * bottomSheetGridDialogFragment.show(getSupportFragmentManager(), "AddFeedItemBottomSheet")
 *
 * // Step 5
 * val onItemClickListener = View.OnClickListener { v ->
 *     when (v.id) {
 *         1 -> {
 *         }
 *
 *         2 -> {
 *         }
 *
 *         3 -> {
 *         }
 *
 *         4 -> {
 *         }
 *     }
 * }
 *
 */