package com.sneakers.sneakerschecker.adapter

import android.app.Activity
import android.content.Context
import android.support.v4.view.PagerAdapter
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import com.sneakers.sneakerschecker.R
import com.sneakers.sneakerschecker.model.ValidatePagerModel
import kotlinx.android.synthetic.main.item_validate_progress.view.*


class ValidatePagerAdapter : PagerAdapter {
    private var mContext: Context
    private var pages: ArrayList<ValidatePagerModel>

    constructor(mContext: Context, pages: ArrayList<ValidatePagerModel>) {
        this.mContext = mContext
        this.pages = pages
    }

    override fun instantiateItem(collection: ViewGroup, position: Int): Any {
        val inflater = LayoutInflater.from(mContext)
        val layout = inflater.inflate(R.layout.item_validate_progress, collection, false) as ViewGroup
        pages[position].content?.let { layout.tvValidateProgressContent.setText(it) }
        if (pages[position].isSuccessed != null) {
            if (pages[position].isSuccessed!!) {
                layout.progress_validate.visibility = GONE
                layout.iconChecked.visibility = VISIBLE
            } else {
                layout.progress_validate.visibility = GONE
                layout.iconFailed.visibility = VISIBLE
                layout.tvValidateProgressContent.setBackgroundColor(mContext.resources.getColor(R.color.colorMain))
            }
        }
        collection.addView(layout)
        return layout
    }

    fun updatePager(activity: Activity, position: Int, isSuccessed: Boolean) {
        pages[position].isSuccessed = isSuccessed
        activity.runOnUiThread {
            notifyDataSetChanged()
        }
    }

    override fun getItemPosition(`object`: Any): Int {
        return PagerAdapter.POSITION_NONE
    }

    override fun destroyItem(collection: ViewGroup, position: Int, view: Any) {
        collection.removeView(view as View)
    }

    override fun getCount(): Int {
        return pages.size
    }

    override fun isViewFromObject(view: View, `object`: Any): Boolean {
        return view === `object`
    }

}