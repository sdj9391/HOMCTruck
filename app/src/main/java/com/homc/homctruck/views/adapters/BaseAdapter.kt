package com.homc.homctruck.views.adapters

import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.homc.homctruck.utils.DebugLog

open class BaseAdapter(val data: MutableList<Any>?) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    protected var dataItems: MutableList<Any>? = null

    init {
        dataItems = data
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        if (viewType == VIEW_TYPE_TEMPORARY) {
            return TempViewHolder(View(parent.context))
        }
        DebugLog.e("Wrong viewType found: $viewType")
        return TempViewHolder(View(parent.context))
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        // Nothing to bind here
        DebugLog.e("Nothing to bind here")
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
    }
}