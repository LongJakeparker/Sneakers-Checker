package com.sneakers.sneakerschecker.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.sneakers.sneakerschecker.R
import com.sneakers.sneakerschecker.model.SneakerHistory
import kotlinx.android.synthetic.main.item_sneaker_history.view.*


class SneakerHistoryAdapter(val data: ArrayList<SneakerHistory?>) :
    RecyclerView.Adapter<SneakerHistoryAdapter.SneakerHistoryViewHolder>() {
    var mListener: Listener? = null
    var context: Context? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SneakerHistoryViewHolder {
        context = parent.context
        val inflater = LayoutInflater.from(context)
        val view = inflater.inflate(R.layout.item_sneaker_history, parent, false)
        return SneakerHistoryViewHolder(view)
    }

    override fun onBindViewHolder(holder: SneakerHistoryViewHolder, position: Int) {
        if (position == 0) {
            val lp = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            lp.setMargins(
                context?.resources?.getDimension(R.dimen.activity_margin_20dp)!!.toInt(),
                context?.resources?.getDimension(R.dimen.activity_margin_24dp)!!.toInt(),
                context?.resources?.getDimension(R.dimen.activity_margin_20dp)!!.toInt(),
                0
            )
            holder.root.layoutParams = lp
        } else if (position == itemCount - 1) {
            holder.ivLine.visibility = View.GONE
            val lp = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            lp.setMargins(
                context?.resources?.getDimension(R.dimen.activity_margin_20dp)!!.toInt(),
                0,
                context?.resources?.getDimension(R.dimen.activity_margin_20dp)!!.toInt(),
                context?.resources?.getDimension(R.dimen.activity_margin_36dp)!!.toInt()
            )
            holder.root.layoutParams = lp
        }
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

    class SneakerHistoryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val root: LinearLayout = itemView.root
        val ivIcon: ImageView = itemView.ivIcon
        val tvTime: TextView = itemView.tvTime
        val ivLine: ImageView = itemView.ivLine
        val tvContent: TextView = itemView.tvContent
    }
}