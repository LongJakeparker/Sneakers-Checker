package com.sneakers.sneakerschecker.adapter

import androidx.recyclerview.widget.RecyclerView
import android.widget.TextView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import com.sneakers.sneakerschecker.R
import com.sneakers.sneakerschecker.model.CreditCard
import kotlinx.android.synthetic.main.item_credit_card.view.*


class ListCardAdapter(val data: ArrayList<CreditCard?>) : RecyclerView.Adapter<ListCardAdapter.ListCardViewHolder>() {
    var mListener: Listener? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ListCardViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val view = inflater.inflate(R.layout.item_credit_card, parent, false)
        return ListCardViewHolder(view)
    }

    override fun onBindViewHolder(holder: ListCardViewHolder, position: Int) {

    }

    override fun getItemCount(): Int {
        return data.size
    }

    interface Listener {
        fun onSelectItem(title: String)
    }

    fun setListener(listener: Listener) {
        mListener = listener
    }

    class ListCardViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val ivCardLogo: ImageView = itemView.ivCardLogo
        val tvCardId: TextView = itemView.tvCardId
        val tvCardValidDate: TextView = itemView.tvCardValidDate
    }
}