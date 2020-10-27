package com.homc.homctruck.views.adapters

import com.homc.homctruck.R
import com.homc.homctruck.data.models.Load

class FindLoadListAdapter(data: MutableList<Any>?) : LoadListAdapter(data) {
    override fun bindLoadView(holder: LoadViewHolder, position: Int) {
        super.bindLoadView(holder, position)
        val dataItem = dataItems?.get(position) as Load
        holder.moreButton.tag = dataItem.ownerId
        holder.moreButton.setImageResource(R.drawable.ic_enquiry)
    }
}
