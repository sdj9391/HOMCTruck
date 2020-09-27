package com.homc.homctruck.views.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.homc.homctruck.R
import com.homc.homctruck.data.models.Truck
import com.homc.homctruck.data.models.TruckRoute
import com.homc.homctruck.utils.formatDateForDisplay
import com.homc.homctruck.utils.setColorsAndCombineStrings

class TruckRouteListAdapter(data: MutableList<Any>?) : BaseAdapter(data) {

    var onMoreClickListener: View.OnClickListener? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        if (viewType == VIEW_TYPE_TRUCK_ROUTE) {
            val itemView: View = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_truck_route, parent, false)
            return TruckRouteViewHolder(itemView)
        }
        return super.onCreateViewHolder(parent, viewType)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (getItemViewType(position)) {
            VIEW_TYPE_TRUCK_ROUTE -> bindTruckRouteView(holder as TruckRouteViewHolder, position)
            else -> super.onBindViewHolder(holder, position)
        }
    }

    private fun bindTruckRouteView(holder: TruckRouteViewHolder, position: Int) {
        val context = holder.itemView.context
        val dataItem = dataItems?.get(position) as TruckRoute
        setColorsAndCombineStrings(
            holder.titleTextView,
            context.getString(R.string.label_truck_number),
            dataItem.truck?.truckNumber
        )
        setColorsAndCombineStrings(
            holder.subtitleTextView1,
            context.getString(R.string.label_location),
            context.getString(
                R.string.placeholder_x_to_y,
                dataItem.fromPlace?.city, dataItem.toPlace?.city
            )
        )
        setColorsAndCombineStrings(
            holder.subtitleTextView2,
            context.getString(R.string.label_date),
            context.getString(
                R.string.placeholder_x_to_y,
                formatDateForDisplay(dataItem.startJourneyDate ?: 0),
                formatDateForDisplay(dataItem.endJourneyDate ?: 0)
            )
        )
        holder.moreButton.tag = dataItem
    }

    private inner class TruckRouteViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val titleTextView: TextView = itemView.findViewById(R.id.titleTextView)
        val subtitleTextView1: TextView = itemView.findViewById(R.id.subtitleTextView1)
        val subtitleTextView2: TextView = itemView.findViewById(R.id.subtitleTextView2)
        val moreButton: ImageButton = itemView.findViewById(R.id.moreButton)

        init {
            moreButton.setOnClickListener(onMoreClickListener)
        }
    }

    override fun getItemViewType(position: Int): Int {
        return when (dataItems?.get(position)) {
            is TruckRoute -> VIEW_TYPE_TRUCK_ROUTE
            else -> super.getItemViewType(position)
        }
    }

    companion object {
        const val VIEW_TYPE_TRUCK_ROUTE = 100
    }
}
