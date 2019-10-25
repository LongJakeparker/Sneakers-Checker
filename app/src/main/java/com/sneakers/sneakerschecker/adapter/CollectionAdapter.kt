package com.sneakers.sneakerschecker.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import androidx.viewpager.widget.PagerAdapter
import com.sneakers.sneakerschecker.R
import com.sneakers.sneakerschecker.model.SneakerModel
import com.wajahatkarim3.easyflipview.EasyFlipView
import kotlinx.android.synthetic.main.flash_card_layout_back.view.*
import kotlinx.android.synthetic.main.flash_card_layout_front.view.*
import kotlinx.android.synthetic.main.item_collection.view.*


class CollectionAdapter(val items: ArrayList<SneakerModel>, val context: Context) :
    PagerAdapter() {

    override fun instantiateItem(container: ViewGroup, position: Int): Any {
        val view = LayoutInflater.from(context).inflate(R.layout.item_collection, null)
        val item = getItem(position)

        view.tvItemIndexFront.text = (position + 1).toString()
        view.tvItemIndexBack.text = (position + 1).toString()
        view.tvItemNameFront.text = item.model
        view.tvItemNameBack.text = item.model
        view.tvItemSize.text = item.size.toString()
        view.tvItemBrand.text = item.brand

        if (item.limitedEdition) {
            view.ivLimitedFront.visibility = VISIBLE
            view.ivLimitedBack.visibility = VISIBLE
        } else {
            view.ivLimitedFront.visibility = GONE
            view.ivLimitedBack.visibility = GONE
        }

        view.flipCard.setOnClickListener {
            view.flipCard.flipTheView()
            view.cvSale.isClickable = false
            view.btnItemStolen.isClickable = false
            view.cvFound.isClickable = false
            view.flipCard.onFlipListener =
                EasyFlipView.OnFlipAnimationListener { easyFlipView, newCurrentSide ->
                    if (newCurrentSide == EasyFlipView.FlipState.BACK_SIDE) {
                        view.cvSale.visibility = VISIBLE
                        view.cvSale.startAnimation(
                            AnimationUtils.loadAnimation(
                                view.context,
                                R.anim.fade_in_view
                            )
                        )
                        view.btnItemStolen.visibility = VISIBLE
                        view.btnItemStolen.startAnimation(
                            AnimationUtils.loadAnimation(
                                view.context,
                                R.anim.fade_in_view
                            )
                        )

                        view.cvSale.isClickable = true
                        view.btnItemStolen.isClickable = true
                        view.cvFound.isClickable = true
                    } else {
                        view.cvSale.visibility = GONE
                        view.btnItemStolen.visibility = GONE
                        view.cvFound.visibility = GONE
                    }
                }
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
}