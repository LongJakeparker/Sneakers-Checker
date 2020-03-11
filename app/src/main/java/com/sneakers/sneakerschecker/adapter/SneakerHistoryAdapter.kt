package com.sneakers.sneakerschecker.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.sneakers.sneakerschecker.R
import com.sneakers.sneakerschecker.model.SneakerHistory
import com.sneakers.sneakerschecker.utils.CommonUtils
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
        val item = data[position]

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

        when (item?.type) {
            SneakerHistory.TYPE_ISSUE -> {
                setTypeHistory(holder, R.drawable.ic_history_released, R.color.colorPutty)
                holder.tvContent.text = context?.getString(R.string.format_history_released, item.factoryName)
            }

            SneakerHistory.TYPE_CLAIM -> {
                setTypeHistory(holder, R.drawable.ic_own_by, R.color.colorOrangish)
                if (CommonUtils.getCurrentUser(context!!)?.user?.username == item.buyerName) {
                    if (position == 0) {
                        holder.tvContent.text = context?.getString(R.string.text_history_claim_owned)
                    } else {
                        holder.tvContent.text = context?.getString(R.string.text_history_claim_was_owned)
                    }
                } else {
                    holder.tvContent.text = context?.getString(R.string.text_history_claim, item.buyerName)
                }
            }

            SneakerHistory.TYPE_RESELL -> {
                if (CommonUtils.getCurrentUser(context!!)?.user?.username == item.buyerName) {
                    setTypeHistory(holder, R.drawable.ic_history_owned, R.color.colorBlueGreen)
                    if (position == 0) {
                        holder.tvContent.text = context?.getString(R.string.text_history_owned)
                    } else {
                        holder.tvContent.text = context?.getString(R.string.text_history_was_owned)
                    }
                } else {
                    setTypeHistory(holder, R.drawable.ic_history_resell, R.color.colorBlack)
                    holder.tvContent.text = context?.getString(R.string.format_history_resell, item.buyerName, item.sellerName)
                }
            }
        }

        val traceDate = CommonUtils.formatStringToDate(item?.createdAt!!)
        holder.tvTime.text = CommonUtils.formatDateToString("MMMM dd',' yyyy", traceDate)

    }

    private fun setTypeHistory(holder: SneakerHistoryViewHolder, icon: Int, color: Int) {
        holder.ivIcon.setImageResource(icon)
        holder.ivIcon.setColorFilter(ContextCompat.getColor(context!!, color), android.graphics.PorterDuff.Mode.SRC_IN)
        holder.tvTime.setTextColor(ContextCompat.getColor(context!!, color))
        holder.ivLine.setColorFilter(ContextCompat.getColor(context!!, color), android.graphics.PorterDuff.Mode.SRC_IN)
        holder.tvContent.setTextColor(ContextCompat.getColor(context!!, color))
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