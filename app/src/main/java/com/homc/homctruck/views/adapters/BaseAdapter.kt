package com.homc.homctruck.views.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.homc.homctruck.R
import com.homc.homctruck.utils.DebugLog

open class BaseAdapter(val data: MutableList<Any>?) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    protected var dataItems: MutableList<Any>? = null
    var onPlankButtonClickListener: View.OnClickListener? = null

    init {
        dataItems = data
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        if (viewType == VIEW_TYPE_TEMPORARY) {
            return TempViewHolder(View(parent.context))
        } else if (viewType == VIEW_TYPE_PLANK_BUTTON) {
            val itemView: View = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_plank_button, parent, false)
            return PlankButtonViewHolder(itemView)
        }
        DebugLog.e("Wrong viewType found: $viewType")
        return TempViewHolder(View(parent.context))
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (getItemViewType(position)) {
            VIEW_TYPE_PLANK_BUTTON -> bindPlankButtonView(holder as PlankButtonViewHolder, position)
            else -> {
                // Nothing to bind here
                DebugLog.e("Nothing to bind here")
            }
        }
    }

    private fun bindPlankButtonView(holder: PlankButtonViewHolder, position: Int) {
        val dataItem = dataItems?.get(position) as AdapterDataItem
        holder.textPlankButton.text = dataItem.data.toString()
    }

    override fun getItemViewType(position: Int): Int {
        return when (val dataItem = dataItems?.get(position)) {
            is AdapterDataItem -> dataItem.id
            else -> super.getItemViewType(position)
        }
    }

    private inner class PlankButtonViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val textPlankButton: TextView = itemView.findViewById(R.id.textPlankButton)

        init {
            textPlankButton.setOnClickListener(onPlankButtonClickListener)
        }
    }

    private class TempViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)

    override fun getItemCount(): Int {
        return dataItems?.size ?: -1
    }

    fun addItem(dataItem: Any) {
        if (dataItems == null) {
            dataItems = mutableListOf()
        }

        dataItems?.add(dataItem)
        notifyDataSetChanged()
    }

    fun addItemAtPosition(position: Int, dataItem: Any) {
        dataItems?.let {
            if (it.size >= position) {
                dataItems?.add(position, dataItem)
            } else {
                DebugLog.e("can't add plank to position $position")
            }
        }
        notifyDataSetChanged()
    }

    fun addAllItems(dataItemList: MutableList<Any>) {
        if (dataItems == null) {
            setAllItems(dataItemList)
            return
        }

        dataItems?.let {
            val to = it.size
            it.addAll(dataItemList)
            if (to == 0) {
                notifyDataSetChanged()
                return
            }
            notifyItemRangeChanged(to, dataItemList.size)
        }
    }

    fun setAllItems(dataItemList: MutableList<Any>) {
        dataItems = dataItemList
        notifyDataSetChanged()
    }

    fun deleteItem(position: Int) {
        dataItems?.removeAt(position)
        notifyItemRemoved(position)
    }

    fun clearList() {
        dataItems?.clear()
        notifyDataSetChanged()
    }

    /**
     * @param from From index included
     * @param to   To index not included
     */
    fun clearRangeOfItems(from: Int, to: Int) {
        dataItems?.subList(from, to)?.clear()
        notifyDataSetChanged()
    }

    fun getItem(position: Int): Any? {
        return dataItems?.get(position)
    }

    fun getItemPosition(dataItem: Any): Int {
        return dataItems?.indexOf(dataItem) ?: -1
    }

    fun getAllItems(): MutableList<Any>? {
        return dataItems
    }

    fun removeItemAtPosition(position: Int) {
        dataItems?.let {
            if (it.size > position && position >= 0) {
                it.removeAt(position)
                notifyDataSetChanged()
            }
        }
    }

    fun removeItem(dataItem: Any): Boolean {
        val isItemDeleted = dataItems?.remove(dataItem) ?: false
        notifyDataSetChanged()
        return isItemDeleted
    }

    companion object {
        const val VIEW_TYPE_TEMPORARY = 0
        const val VIEW_TYPE_PLANK_BUTTON = 101
    }
}

class AdapterDataItem(val id: Int, val data: Any?)