package com.sneakers.sneakerschecker.adapter

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.text.Html
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.sneakers.sneakerschecker.R
import com.sneakers.sneakerschecker.model.SneakerBoardModel
import com.sneakers.sneakerschecker.utils.CommonUtils
import de.hdodenhof.circleimageview.CircleImageView
import kotlinx.android.synthetic.main.item_sneaker_board.view.*


class SneakerBoardAdapter(val data: ArrayList<SneakerBoardModel?>) :
    RecyclerView.Adapter<SneakerBoardAdapter.ListSneakerViewHolder>() {
    var mListener: Listener? = null
    var mCopyListener: CopyListener? = null
    var context: Context? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ListSneakerViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val view = inflater.inflate(R.layout.item_sneaker_board, parent, false)
        context = parent.context
        return ListSneakerViewHolder(view)
    }

    override fun onBindViewHolder(holder: ListSneakerViewHolder, position: Int) {
        if (data[position] == null)
            return

        val itemInfo = data[position]?.sneakerInfo
        val ownerInfo = data[position]?.OwnerInfo

        if (itemInfo?.limitedEdition!!) {
            holder.root.setBackgroundResource(R.drawable.bg_item_sneaker_limit)
            holder.ivLimited.visibility = View.VISIBLE
        } else {
            holder.root.setBackgroundResource(R.drawable.bg_item_sneaker)
            holder.ivLimited.visibility = View.GONE
        }

        holder.ivItemBrand.setImageResource(CommonUtils.getBrandLogo(itemInfo.brand!!))

        if (!itemInfo.colorway.isNullOrEmpty() && itemInfo.colorway?.contains("#")!!) {
            val color = ColorDrawable(Color.parseColor(itemInfo.colorway))
            holder.ivItemColor.setImageDrawable(color)
        }

        holder.tvItemName.text = itemInfo.model
        holder.tvItemSize.text = context?.getString(R.string.format_size, itemInfo.size.toString())
        holder.tvItemReleaseDate.text =
            context?.getString(R.string.format_release_date, itemInfo.releaseDate)

        if (ownerInfo != null) {
            holder.rlBtnSetOwnerInfo.visibility = View.GONE
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                holder.tvItemOwnerName.text = Html.fromHtml(context?.getString(R.string.format_owned_by, ownerInfo.username), Html.FROM_HTML_MODE_COMPACT)
            } else {
                holder.tvItemOwnerName.text = Html.fromHtml(context?.getString(R.string.format_owned_by, ownerInfo.username))
            }
            holder.tvItemOwnerPhone.text = ownerInfo.userIdentity
        } else {
            holder.rlBtnSetOwnerInfo.visibility = View.VISIBLE
            holder.tvGetOwnerInfo.setOnClickListener {
                if (CommonUtils.isNonLoginUser(context!!)) {
                    mListener?.onNotifyLogin()
                } else {
                    mListener?.onSelectItem(itemInfo.id, position)
                    holder.rlProgressGetOwnerInfo.visibility = View.VISIBLE
                }
            }
        }

        holder.ivCopyOwnerPhone.setOnClickListener { mCopyListener?.onSelectItem(ownerInfo?.userIdentity) }
    }

    override fun getItemCount(): Int {
        return data.size
    }

    interface Listener {
        fun onSelectItem(itemId: Long?, position: Int)

        fun onNotifyLogin()
    }

    interface CopyListener {
        fun onSelectItem(copyData: String?)
    }

    fun setListener(listener: Listener) {
        mListener = listener
    }

    fun setCopyListener(listener: CopyListener) {
        mCopyListener = listener
    }

    class ListSneakerViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val root: LinearLayout = itemView.root
        val ivItemBrand: ImageView = itemView.ivItemBrand
        val ivItemColor: CircleImageView = itemView.ivItemColor
        val tvItemName: TextView = itemView.tvItemName
        val ivLimited: ImageView = itemView.ivLimited
        val tvItemSize: TextView = itemView.tvItemSize
        val tvItemReleaseDate: TextView = itemView.tvItemReleaseDate
        val tvItemOwnerName: TextView = itemView.tvItemOwnerName
        val tvItemOwnerPhone: TextView = itemView.tvItemOwnerPhone
        val ivCopyOwnerPhone: ImageView = itemView.ivCopyOwnerPhone
        val rlBtnSetOwnerInfo: RelativeLayout = itemView.rlBtnSetOwnerInfo
        val tvGetOwnerInfo: TextView = itemView.tvGetOwnerInfo
        val rlProgressGetOwnerInfo: RelativeLayout = itemView.rlProgressGetOwnerInfo
    }
}