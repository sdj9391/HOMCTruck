package com.homc.homctruck.utils

import android.content.Context
import android.view.*
import android.widget.*
import com.homc.homctruck.R
import java.util.*

class PopupMenuWindow : ListPopupWindow {

    private val onItemClickListener =
        AdapterView.OnItemClickListener { _, _, _, _ -> dismiss() }

    constructor(context: Context, anchorView: View, menu: Menu) : super(context) {
        setMenu(context, anchorView, menu)
    }

    constructor(context: Context, anchorView: View, menuRes: Int) : super(context) {
        val popupMenu = PopupMenu(context, getAnchorView())
        popupMenu.inflate(menuRes)
        val menu = popupMenu.menu
        setMenu(context, anchorView, menu)
    }

    private fun setMenu(context: Context, anchorView: View, menu: Menu) {
        setAnchorView(anchorView)
        isModal = true
        width = DEFAULT_WIDTH
        setOnItemClickListener(onItemClickListener)

        val menuItemList = mutableListOf<MenuItem>()
        for (index in 0 until menu.size()) {
            val menuItem = menu.getItem(index)
            if (menuItem.isVisible)
                menuItemList.add(menu.getItem(index))
        }

        val popupWindowAdapter =
            PopupWindowAdapter(context, R.layout.simple_menu_item, menuItemList)
        setAdapter(popupWindowAdapter)
    }

    private inner class PopupWindowAdapter(
        context: Context,
        resource: Int,
        val menuItemList: MutableList<MenuItem>
    ) :
        ArrayAdapter<MenuItem>(context, resource, menuItemList) {

        override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
            var view = convertView
            val holder: ViewHolder
            val menuItem = menuItemList[position]

            if (view == null) {
                view =
                    LayoutInflater.from(context).inflate(R.layout.simple_menu_item, parent, false)
                view?.id = menuItem.itemId
                holder = ViewHolder(view)
                view.tag = holder
            } else {
                holder = view.tag as ViewHolder
            }

            holder.titleTextView.text = menuItem.title
            val drawable = menuItem.icon
            if (drawable == null) {
                holder.iconImageView.visibility = View.GONE
            } else {
                holder.iconImageView.visibility = View.VISIBLE
                holder.iconImageView.setImageDrawable(drawable)
            }
            return view ?: View(context)
        }

        private inner class ViewHolder(itemView: View) {
            val titleTextView: TextView = itemView.findViewById(R.id.text_view_title)
            val iconImageView: ImageView = itemView.findViewById(R.id.image_view_icon)
        }
    }

    companion object {
        const val DEFAULT_WIDTH = 500
    }
}
