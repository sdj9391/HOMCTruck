package com.homc.homctruck.views.adapters

import android.view.View

class FindTruckRouteListAdapter(data: MutableList<Any>?) : TruckRouteListAdapter(data) {
    override fun bindTruckRouteView(holder: TruckRouteViewHolder, position: Int) {
        super.bindTruckRouteView(holder, position)
        holder.moreButton.visibility = View.GONE
    }
}
