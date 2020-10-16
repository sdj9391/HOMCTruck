package com.homc.homctruck.views.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.homc.homctruck.R
import com.homc.homctruck.data.models.*
import com.homc.homctruck.utils.setColorsAndCombineStrings
import kotlinx.android.synthetic.main.fragment_user_profile.*
import kotlinx.android.synthetic.main.item_user_details.view.*

class UserListAdapter(data: MutableList<Any>?) : BaseAdapter(data) {

    var onMoreClickListener: View.OnClickListener? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        if (viewType == VIEW_TYPE_USER) {
            val itemView: View = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_user_details, parent, false)
            return UserViewHolder(itemView)
        }
        return super.onCreateViewHolder(parent, viewType)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (getItemViewType(position)) {
            VIEW_TYPE_USER -> bindUserView(holder as UserViewHolder, position)
            else -> super.onBindViewHolder(holder, position)
        }
    }

    private fun bindUserView(holder: UserViewHolder, position: Int) {
        val context = holder.itemView.context
        val user = dataItems?.get(position) as User

        if (user.getName().isNullOrBlank()) {
            holder.titleTextView.text = context.getString(R.string.label_name_of_the_user)
        } else {
            setColorsAndCombineStrings(
                holder.titleTextView,
                context.getString(R.string.label_name),
                user.getName()
            )
        }

        setColorsAndCombineStrings(
            holder.subtitleTextView1,
            context.getString(R.string.label_mobile_number),
            context.getString(R.string.placeholder_plus_91, user.mobileNumber)
        )
        val email = user.email
        if (email.isNullOrBlank()) {
            holder.subtitleTextView2.visibility = View.GONE
        } else {
            setColorsAndCombineStrings(
                holder.subtitleTextView2,
                context.getString(R.string.label_email),
                email
            )
            holder.subtitleTextView2.visibility = View.VISIBLE
        }
        val panCardNumber = user.panCardNumber
        if (panCardNumber.isNullOrBlank()) {
            holder.subtitleTextView3.visibility = View.GONE
        } else {
            setColorsAndCombineStrings(
                holder.subtitleTextView3,
                context.getString(R.string.label_pan_card_number),
                panCardNumber
            )
            holder.subtitleTextView3.visibility = View.VISIBLE
        }
        val aadharCardNumber = user.aadharCardNumber
        if (aadharCardNumber.isNullOrBlank()) {
            holder.subtitleTextView4.visibility = View.GONE
        } else {
            setColorsAndCombineStrings(
                holder.subtitleTextView4,
                context.getString(R.string.label_aadhar_card_number),
                aadharCardNumber
            )
            holder.subtitleTextView4.visibility = View.VISIBLE
        }
        val address = user.address
        if (address == null) {
            holder.subtitleTextView5.visibility = View.GONE
        } else {
            val addressString = address.getFullAddress()
            if (addressString.isNullOrBlank()) {
                holder.subtitleTextView5.visibility = View.GONE
            } else {
                setColorsAndCombineStrings(
                    holder.subtitleTextView5,
                    context.getString(R.string.label_address),
                    addressString
                )
                holder.subtitleTextView5.visibility = View.VISIBLE
            }
        }
        holder.moreButton.tag = user
    }

    private inner class UserViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val titleTextView: TextView = itemView.findViewById(R.id.titleTextView)
        val subtitleTextView1: TextView = itemView.findViewById(R.id.subtitleTextView1)
        val subtitleTextView2: TextView = itemView.findViewById(R.id.subtitleTextView2)
        val subtitleTextView3: TextView = itemView.findViewById(R.id.subtitleTextView3)
        val subtitleTextView4: TextView = itemView.findViewById(R.id.subtitleTextView4)
        val subtitleTextView5: TextView = itemView.findViewById(R.id.subtitleTextView5)
        val moreButton: ImageButton = itemView.findViewById(R.id.moreButton)

        init {
            moreButton.visibility = View.VISIBLE
            moreButton.setOnClickListener(onMoreClickListener)
        }
    }

    override fun getItemViewType(position: Int): Int {
        return when (dataItems?.get(position)) {
            is User -> VIEW_TYPE_USER
            else -> super.getItemViewType(position)
        }
    }

    companion object {
        const val VIEW_TYPE_USER = 100
    }
}
