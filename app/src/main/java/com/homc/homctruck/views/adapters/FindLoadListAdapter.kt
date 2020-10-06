package com.homc.homctruck.views.adapters

import android.view.View

class FindLoadListAdapter(data: MutableList<Any>?) : LoadListAdapter(data) {
    override fun bindLoadView(holder: LoadViewHolder, position: Int) {
        super.bindLoadView(holder, position)
        holder.moreButton.visibility = View.GONE
    }
}
