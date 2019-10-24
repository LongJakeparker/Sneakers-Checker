package com.sneakers.sneakerschecker.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.viewpager.widget.PagerAdapter
import com.sneakers.sneakerschecker.R
import com.sneakers.sneakerschecker.model.MainSliderItem
import kotlinx.android.synthetic.main.main_slider_item.view.*

class MainSliderAdapter(context: Context, items: ArrayList<MainSliderItem>) : PagerAdapter() {
    private var mItems: ArrayList<MainSliderItem> = items
    private var mContext: Context = context

    override fun instantiateItem(container: ViewGroup, position: Int): Any {
        val view = LayoutInflater.from(mContext).inflate(R.layout.main_slider_item, null)
        val item = getItem(position)
        view.tvSliderContent.text = mContext.resources.getText(item.content)
        view.ivSliderImage.setImageResource(item.background)
        container.addView(view)
        return view
    }

    private fun getItem(position: Int): MainSliderItem {
        return mItems[position % mItems.size]
    }

    override fun isViewFromObject(view: View, `object`: Any): Boolean {
        return view === `object`
    }

    override fun getCount(): Int {
        return if (mItems.size != 1) {
            Int.MAX_VALUE
        } else {
            mItems.size
        }
    }

    override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
        container.removeView(`object` as View)
    }
}