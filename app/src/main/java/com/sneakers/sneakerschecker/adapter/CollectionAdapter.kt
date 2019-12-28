package com.sneakers.sneakerschecker.adapter

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import androidx.viewpager.widget.PagerAdapter
import com.sneakers.sneakerschecker.R
import com.sneakers.sneakerschecker.model.SneakerModel
import com.sneakers.sneakerschecker.utils.CommonUtils
import kotlinx.android.synthetic.main.flash_card_layout_back.view.*
import kotlinx.android.synthetic.main.flash_card_layout_front.view.*
import kotlinx.android.synthetic.main.item_collection.view.*


class CollectionAdapter(val items: ArrayList<SneakerModel>, val context: Context) : PagerAdapter() {

    override fun instantiateItem(container: ViewGroup, position: Int): Any {
        val view = LayoutInflater.from(context).inflate(R.layout.item_collection, null)
        val item = getItem(position)

        view.tvItemIndexFront.text = (position + 1).toString()
        view.tvItemIndexBack.text = (position + 1).toString()
        view.tvItemIndexFrontDisabled.text = (position + 1).toString()
        view.tvItemIndexBackDisabled.text = (position + 1).toString()
        view.tvItemNameFront.text = item.model
        view.tvItemNameBack.text = item.model
        view.tvItemNameFrontDisabled.text = item.model
        view.tvItemNameBackDisabled.text = item.model
        view.tvItemSize.text = item.size.toString()
        view.tvItemSizeDisabled.text = item.size.toString()
        view.tvItemBrand.text = item.brand
        view.tvItemBrandDisabled.text = item.brand
        view.tvFurtherSpec.text = item.furtherSpec

        if (!item.colorway.isNullOrEmpty() && item.colorway?.contains("#")!!) {
            val color = ColorDrawable(Color.parseColor(item.colorway))
            view.ivItemColor.setImageDrawable(color)
        }

        CommonUtils.getBrandLogo(item.brand!!).let {
            view.ivBrandLogoFront.setImageResource(it)
            view.ivBrandLogoBack.setImageResource(it)
            view.ivBrandLogoFrontDisabled.setImageResource(it)
            view.ivBrandLogoBackDisabled.setImageResource(it)
        }

        if (item.limitedEdition!!) {
            view.ivLimitedFront.visibility = VISIBLE
            view.ivLimitedBack.visibility = VISIBLE
            view.ivLimitedFrontDisabled.visibility = VISIBLE
            view.ivLimitedBackDisabled.visibility = VISIBLE
        } else {
            view.ivLimitedFront.visibility = GONE
            view.ivLimitedBack.visibility = GONE
        }

        if (!item.isCardActivate) {
            view.rlViewItemStolenFront.visibility = VISIBLE
            view.clViewItemStolenBack.visibility = VISIBLE
        }

        view.flipCard.setOnClickListener {
            view.flipCard.flipTheView()
            item.isCardFliped = !item.isCardFliped
        }

        container.addView(view)
        return view
    }

    private fun getItem(position: Int): SneakerModel {
        return items[position]
    }

    override fun isViewFromObject(view: View, `object`: Any): Boolean {
        return view === `object`
    }

    override fun getCount(): Int {
        return items.size
    }

    override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
        container.removeView(`object` as View)
    }

    override fun getItemPosition(`object`: Any): Int {
        return POSITION_NONE
    }
}