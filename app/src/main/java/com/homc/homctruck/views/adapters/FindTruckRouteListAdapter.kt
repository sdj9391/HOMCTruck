package com.homc.homctruck.views.adapters

import com.homc.homctruck.R
import com.homc.homctruck.data.models.TruckRoute

class FindTruckRouteListAdapter(data: MutableList<Any>?) : TruckRouteListAdapter(data) {
    override fun bindTruckRouteView(holder: TruckRouteViewHolder, position: Int) {
        super.bindTruckRouteView(holder, position)
        val dataItem = dataItems?.get(position) as TruckRoute
        holder.moreButton.tag = dataItem.truck?.ownerId
        holder.moreButton.setImageResource(R.drawable.ic_enquiry)
    }
}
