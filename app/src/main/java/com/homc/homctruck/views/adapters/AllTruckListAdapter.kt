package com.homc.homctruck.views.adapters

import com.homc.homctruck.R
import com.homc.homctruck.data.models.Truck

class AllTruckListAdapter(data: MutableList<Any>?) : TruckListAdapter(data) {
    override fun changeButtonIcon(dataItem: Truck, holder: TruckViewHolder) {
        holder.moreButton.setImageResource(R.drawable.ic_more_vertical_black)
    }
}