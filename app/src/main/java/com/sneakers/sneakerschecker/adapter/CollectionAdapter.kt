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

    //    override fun getItemCount(): Int {
//        return items.size
//    }
//
//    // Inflates the item views
//    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CollectionViewHolder {
//        return CollectionViewHolder(
//            LayoutInflater.from(context).inflate(
//                R.layout.item_collection,
//                parent,
//                false
//            )
//        )
//    }
//
//    override fun onBindViewHolder(holder: CollectionViewHolder, position: Int) {
//        val item = items[position]
//
//        holder.tvItemIndexFront.text = (position + 1).toString()
//        holder.tvItemIndexBack.text = (position + 1).toString()
//        holder.tvItemNameFront.text = item.model
//        holder.tvItemNameBack.text = item.model
//        holder.tvItemSize.text = item.size.toString()
//        holder.tvItemBrand.text = item.brand
//    }
    override fun instantiateItem(container: ViewGroup, position: Int): Any {
        val view = LayoutInflater.from(context).inflate(R.layout.item_collection, null)
        val item = getItem(position)

        view.tvItemIndexFront.text = (position + 1).toString()
        view.tvItemIndexBack.text = (position + 1).toString()
        view.tvItemNameFront.text = item.model
        view.tvItemNameBack.text = item.model
        view.tvItemSize.text = item.size.toString()
        view.tvItemBrand.text = item.brand

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

//class CollectionViewHolder(view: View) : RecyclerView.ViewHolder(view) {
//    val tvItemIndexBack = view.tvItemIndexBack
//    val tvItemIndexFront = view.tvItemIndexFront
//    val tvItemNameFront = view.tvItemNameFront
//    val tvItemNameBack = view.tvItemNameBack
//    val tvItemSize = view.tvItemSize
//    val tvItemBrand = view.tvItemBrand
//    val btnSale = view.btnItemSale
//    val btnStolen = view.btnItemStolen
//    val btnFound = view.btnItemFound
//
//    init {
//        view.flipCard.setOnClickListener {
//            view.flipCard.flipTheView()
//            view.cvSale.isClickable = false
//            btnStolen.isClickable = false
//            view.cvFound.isClickable = false
//            view.flipCard.onFlipListener =
//                EasyFlipView.OnFlipAnimationListener { easyFlipView, newCurrentSide ->
//                    if (newCurrentSide == EasyFlipView.FlipState.BACK_SIDE) {
//                        view.cvSale.visibility = VISIBLE
//                        view.cvSale.startAnimation(
//                            AnimationUtils.loadAnimation(
//                                view.context,
//                                R.anim.fade_in_view
//                            )
//                        )
//                        btnStolen.visibility = VISIBLE
//                        btnStolen.startAnimation(
//                            AnimationUtils.loadAnimation(
//                                view.context,
//                                R.anim.fade_in_view
//                            )
//                        )
//
//                        view.cvSale.isClickable = true
//                        btnStolen.isClickable = true
//                        view.cvFound.isClickable = true
//                    } else {
//                        view.cvSale.visibility = GONE
//                        btnStolen.visibility = GONE
//                        view.cvFound.visibility = GONE
//                    }
//                }
//        }
//    }
//}