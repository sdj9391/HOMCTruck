package com.homc.homctruck.views.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.homc.homctruck.R
import com.homc.homctruck.data.models.Load
import com.homc.homctruck.utils.formatDateForDisplay
import com.homc.homctruck.utils.setColorsAndCombineStrings

open class LoadListAdapter(data: MutableList<Any>?) : BaseAdapter(data) {

    var onMoreClickListener: View.OnClickListener? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        if (viewType == VIEW_TYPE_LOAD) {
            val itemView: View = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_load, parent, false)
            return LoadViewHolder(itemView)
        }
        return super.onCreateViewHolder(parent, viewType)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (getItemViewType(position)) {
            VIEW_TYPE_LOAD -> bindLoadView(holder as LoadViewHolder, position)
            else -> super.onBindViewHolder(holder, position)
        }
    }

    protected open fun bindLoadView(holder: LoadViewHolder, position: Int) {
        val context = holder.itemView.context
        val dataItem = dataItems?.get(position) as Load
        setColorsAndCombineStrings(
            holder.titleTextView,
            context.getString(R.string.label_name_of_goods),
            dataItem.nameOfGoods
        )
        setColorsAndCombineStrings(
            holder.subtitleTextView1,
            context.getString(R.string.label_material_type),
            dataItem.typeOfMaterial
        )
        setColorsAndCombineStrings(
            holder.subtitleTextView2,
            context.getString(R.string.label_location),
            context.getString(
                R.string.placeholder_x_to_y,
                dataItem.fromCity, dataItem.toCity
            )
        )
        setColorsAndCombineStrings(
            holder.subtitleTextView3,
            context.getString(R.string.label_expected_pickup_date),
            formatDateForDisplay(dataItem.expectedPickUpDate ?: 0)
        )
        setColorsAndCombineStrings(
            holder.subtitleTextView4,
            context.getString(R.string.label_truck_type),
            dataItem.typeOfTruck
        )
        setColorsAndCombineStrings(
            holder.subtitleTextView5,
            context.getString(R.string.label_rate_per_ton),
            context.getString(R.string.placeholder_x_rs, dataItem.perTonRate.toString())
        )
        setColorsAndCombineStrings(
            holder.subtitleTextView6,
            context.getString(R.string.label_total_load),
            context.getString(R.string.placeholder_x_ton, dataItem.totalLoadInTons.toString())
        )
        setColorsAndCombineStrings(
            holder.subtitleTextView7,
            context.getString(R.string.label_total_amount),
            context.getString(R.string.placeholder_x_rs, dataItem.totalAmount.toString())
        )
        setColorsAndCombineStrings(
            holder.subtitleTextView8,
            context.getString(R.string.label_transit_days),
            dataItem.transitDaysForTruck.toString()
        )
        holder.moreButton.tag = dataItem
    }

    protected inner class LoadViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val titleTextView: TextView = itemView.findViewById(R.id.titleTextView)
        val subtitleTextView1: TextView = itemView.findViewById(R.id.subtitleTextView1)
        val subtitleTextView2: TextView = itemView.findViewById(R.id.subtitleTextView2)
        val subtitleTextView3: TextView = itemView.findViewById(R.id.subtitleTextView3)
        val subtitleTextView4: TextView = itemView.findViewById(R.id.subtitleTextView4)
        val subtitleTextView5: TextView = itemView.findViewById(R.id.subtitleTextView5)
        val subtitleTextView6: TextView = itemView.findViewById(R.id.subtitleTextView6)
        val subtitleTextView7: TextView = itemView.findViewById(R.id.subtitleTextView7)
        val subtitleTextView8: TextView = itemView.findViewById(R.id.subtitleTextView8)
        val moreButton: ImageButton = itemView.findViewById(R.id.moreButton)

        init {
            moreButton.setOnClickListener(onMoreClickListener)
        }
    }

    override fun getItemViewType(position: Int): Int {
        return when (dataItems?.get(position)) {
            is Load -> VIEW_TYPE_LOAD
            else -> super.getItemViewType(position)
        }
    }

    companion object {
        const val VIEW_TYPE_LOAD = 100
    }
}
