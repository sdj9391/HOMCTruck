package com.homc.homctruck.views.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.homc.homctruck.R
import com.homc.homctruck.data.models.Load
import com.homc.homctruck.data.models.Truck
import com.homc.homctruck.utils.setColorsAndCombineStrings

class TruckListAdapter(data: MutableList<Any>?) : BaseAdapter(data) {

    var onMoreClickListener: View.OnClickListener? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        if (viewType == VIEW_TYPE_TRUCK) {
            val itemView: View = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_truck, parent, false)
            return TruckViewHolder(itemView)
        }
        return super.onCreateViewHolder(parent, viewType)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (getItemViewType(position)) {
            VIEW_TYPE_TRUCK -> bindTruckView(holder as TruckViewHolder, position)
            else -> super.onBindViewHolder(holder, position)
        }
    }

    private fun bindTruckView(holder: TruckViewHolder, position: Int) {
        val context = holder.itemView.context
        val dataItem = dataItems?.get(position) as Truck
        setColorsAndCombineStrings(
            holder.titleTextView1,
            context.getString(R.string.label_owner_name),
            dataItem.ownerName
        )
        setColorsAndCombineStrings(
            holder.titleTextView2,
            context.getString(R.string.label_truck_number),
            dataItem.truckNumber
        )
        setColorsAndCombineStrings(
            holder.subtitleTextView1,
            context.getString(R.string.label_truck_type),
            dataItem.type
        )
        setColorsAndCombineStrings(
            holder.subtitleTextView2,
            context.getString(R.string.label_chesse_number),
            dataItem.chesseNumber
        )
        holder.moreButton.tag = dataItem
    }

    private inner class TruckViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val titleTextView1: TextView = itemView.findViewById(R.id.titleTextView1)
        val titleTextView2: TextView = itemView.findViewById(R.id.titleTextView2)
        val subtitleTextView1: TextView = itemView.findViewById(R.id.subtitleTextView1)
        val subtitleTextView2: TextView = itemView.findViewById(R.id.subtitleTextView2)
        val moreButton: ImageButton = itemView.findViewById(R.id.moreButton)

        init {
            moreButton.setOnClickListener(onMoreClickListener)
        }
    }

    override fun getItemViewType(position: Int): Int {
        return when (dataItems?.get(position)) {
            is Truck -> VIEW_TYPE_TRUCK
            else -> super.getItemViewType(position)
        }
    }

    companion object {
        const val VIEW_TYPE_TRUCK = 100
    }
}
