package com.sneakers.sneakerschecker.adapter

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.View.VISIBLE
import android.view.ViewGroup
import com.sneakers.sneakerschecker.R
import com.sneakers.sneakerschecker.model.SneakerModel
import kotlinx.android.synthetic.main.item_collection.view.*

class collectionAdapter(val items: ArrayList<SneakerModel>, val context: Context) :
    RecyclerView.Adapter<CollectionViewHolder>() {

    // Gets the number of animals in the list
    override fun getItemCount(): Int {
        return items.size
    }

    // Inflates the item views
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CollectionViewHolder {
        return CollectionViewHolder(LayoutInflater.from(context).inflate(R.layout.item_collection, parent, false))
    }

    // Binds each animal in the ArrayList to a view
    override fun onBindViewHolder(holder: CollectionViewHolder, position: Int) {
        val item = items[position]

        holder.tvSneakerId.text = item.id.toString()
        holder.tvSneakerName.text = item.model
        holder.tvSneakerSize.text = item.size.toString() + " US"
        holder.tvSneakerBrand.text = item.brand
        holder.tvSneakerCondition.text = item.condition
        holder.tvSneakerReleaseDate.text = item.releaseDate
        if (item.limitedEdition) {
            holder.tvIsLimited.visibility = VISIBLE
        }
    }
}

class CollectionViewHolder(view: View) : RecyclerView.ViewHolder(view) {
    // Holds the TextView that will add each animal to
    val tvSneakerId = view.tvSneakerId
    val tvSneakerName = view.tvSneakerName
    val tvSneakerSize = view.tvSneakerSize
    val tvSneakerBrand = view.tvSneakerBrand
    val tvSneakerCondition = view.tvSneakerCondition
    val tvSneakerReleaseDate = view.tvReleaseDate
    val tvIsLimited = view.tvIsLimited
}