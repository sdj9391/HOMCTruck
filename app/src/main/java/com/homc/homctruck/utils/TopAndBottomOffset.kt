package com.homc.homctruck.utils

import android.graphics.Rect
import android.view.View
import androidx.recyclerview.widget.RecyclerView

class TopAndBottomOffset(var topOffset: Int, var bottomOffset: Int) :
    RecyclerView.ItemDecoration() {

    override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView,
                                state: RecyclerView.State) {
        super.getItemOffsets(outRect, view, parent, state)
        val dataSize = state.itemCount
        val position = parent.getChildAdapterPosition(view)
        // To add an blank plank at the end of recyclerView
        if (dataSize > 0 && position == 0) {
            outRect.set(0, topOffset, 0, 0)
        } else if (dataSize > 0 && position == dataSize - 1) {
            outRect.set(0, 0, 0, bottomOffset)
        } else {
            outRect.set(0, 0, 0, 0)
        }
    }
}