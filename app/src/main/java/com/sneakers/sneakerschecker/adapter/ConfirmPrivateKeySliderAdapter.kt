package com.sneakers.sneakerschecker.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.viewpager.widget.PagerAdapter
import com.sneakers.sneakerschecker.R
import com.sneakers.sneakerschecker.model.ConfirmPrivateKeySliderItem
import kotlinx.android.synthetic.main.confirm_private_key_slider_item.view.*

class ConfirmPrivateKeySliderAdapter(context: Context, items: ArrayList<ConfirmPrivateKeySliderItem>) : PagerAdapter() {
    private var mItems: ArrayList<ConfirmPrivateKeySliderItem> = items
    private var mContext: Context = context

    override fun instantiateItem(container: ViewGroup, position: Int): Any {
        val view = LayoutInflater.from(mContext).inflate(R.layout.confirm_private_key_slider_item, null)
        val item = getItem(position)
        view.tvTitle.text = item.title
        view.tvContent.text = item.content
        container.addView(view)
        return view
    }

    private fun getItem(position: Int): ConfirmPrivateKeySliderItem {
        return mItems[position]
    }

    override fun isViewFromObject(view: View, `object`: Any): Boolean {
        return view === `object`
    }

    override fun getCount(): Int {
        return mItems.size
    }

    override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
        container.removeView(`object` as View)
    }
}