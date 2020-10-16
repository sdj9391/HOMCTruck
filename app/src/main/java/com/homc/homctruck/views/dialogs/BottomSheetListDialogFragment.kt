package com.homc.homctruck.views.dialogs

import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import com.homc.homctruck.R
import kotlin.random.Random

/**
 * Steps to use this BottomSheetListDialogFragment:
 * 1. Create List of `BottomSheetViewItem` for a section
 * 2. Create List of `BottomSheetViewSection` by adding `BottomSheetViewItem` and Section Title
 * 3. Create `BottomSheetViewData` by adding `BottomSheetViewSection` and Bottom Sheet Title
 * 4. Create object of `BottomSheetListDialogFragment` by passing `BottomSheetViewData` and
 *    `onItemClickListener` for each bottom sheet view item
 * 5. Check view id into `onItemClickListener` to get which bottom sheet view item clicked
 *
 * NOTE: Ids passed into `BottomSheetViewItem` should be unique for each bottom sheet view item
 *
 * ** Check Reference Example in comment added at bottom
 */
class BottomSheetListDialogFragment(override var bottomSheetViewData: BottomSheetViewData,
                                    override var onItemClickListener: View.OnClickListener) :
        BottomSheetGridDialogFragment(bottomSheetViewData, onItemClickListener) {

    private var isSubtitleSingleLine = false
    constructor(bottomSheetViewData: BottomSheetViewData, onItemClickListener: View.OnClickListener,
                isSubtitleSingleLine: Boolean = false) : this(bottomSheetViewData, onItemClickListener) {
        this.isSubtitleSingleLine = isSubtitleSingleLine
    }

    /**
     * Method is use to bind BottomSheet
     */
    override fun bindBottomSheetView() {
        bottomSheetViewData.bottomSheetViewSections.forEach {
            bindSection(it)
        }
    }

    /**
     * Method is use to bind each section of BottomSheet
     */
    private fun bindSection(bottomSheetViewSections: BottomSheetViewSection) {
        val itemView: View = LayoutInflater.from(context)
                .inflate(R.layout.custom_bottom_sheet_dialog_section, null, false)

        val imageView = itemView.findViewById<View>(R.id.image_view)
        val titleTextView = itemView.findViewById<TextView>(R.id.text_view_title)
        val subTitleTextView = itemView.findViewById<TextView>(R.id.text_view_sub_title)
        val divider = itemView.findViewById<View>(R.id.divider)
        val linearLayout = itemView.findViewById<LinearLayout>(R.id.linear_layout)

        divider.visibility = View.GONE
        imageView.visibility = View.GONE
        titleTextView.visibility = View.GONE
        subTitleTextView.visibility = View.GONE

        bottomSheetViewSections.viewItems.forEach {
            bindItem(linearLayout, it)
        }

        (view as LinearLayout).addView(itemView);
    }

    /**
     * Method is use to bind each bottom sheet view item
     */
    private fun bindItem(linearLayout: LinearLayout, bottomSheetViewItem: BottomSheetViewItem) {
        val itemView: View = if (bottomSheetViewItem.subTitle.isNullOrBlank()) {
            LayoutInflater.from(context).inflate(R.layout.bottom_sheet_dialog_list_title_item,
                    null, false)
        } else {
            LayoutInflater.from(context).inflate(R.layout.bottom_sheet_dialog_list_item,
                    null, false)
        }

        itemView.id = bottomSheetViewItem.id ?: Random.nextInt()
        val imageView = itemView.findViewById<ImageView>(R.id.image_view)
        val titleTextView = itemView.findViewById<TextView>(R.id.text_view_title)
        val subTitleTextView = itemView.findViewById<TextView?>(R.id.text_view_sub_title)

        imageView.setImageResource(bottomSheetViewItem.icon ?: R.drawable.ic_arrow_forward_black)
        titleTextView.text = bottomSheetViewItem.title
        if (bottomSheetViewItem.subTitle.isNullOrBlank()) {
            subTitleTextView?.visibility = View.GONE
        } else {
            subTitleTextView?.text = bottomSheetViewItem.subTitle
            subTitleTextView?.setSingleLine(isSubtitleSingleLine)
            subTitleTextView?.visibility = View.VISIBLE
        }
        itemView.tag = bottomSheetViewItem.data
        itemView.setOnClickListener(onItemClickListener)
        linearLayout.addView(itemView)
    }
}

/**
 * Step Example:
 *
 * // Step 1
 * val sectionItems1 = mutableListOf<BottomSheetViewItem>()
 * sectionItems1.add(BottomSheetViewItem(1, R.drawable.ic_icon1, "Title 1", "sub Title 1", data1))
 * sectionItems1.add(BottomSheetViewItem(2, R.drawable.ic_icon2, "Title 2", "sub Title 2", data2))
 *
 * // Step 2
 * val sections = mutableListOf<BottomSheetViewSection>()
 * sections.add(BottomSheetViewSection(sectionItems1))
 *
 * // Step 3
 * val bottomSheetViewData = BottomSheetViewData(sections)
 *
 * // Step 4
 * val bottomSheetListDialogFragment = BottomSheetListDialogFragment(bottomSheetViewData, onItemClickListener)
 * bottomSheetListDialogFragment.show(getSupportFragmentManager(), "AddFeedItemBottomSheet")
 *
 * // Step 5
 * val onItemClickListener = View.OnClickListener { v ->
 *     when (v.id) {
 *         1 -> {
 *         }
 *
 *         2 -> {
 *         }
 *     }
 * }
 *
 */