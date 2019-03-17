package com.sneakers.sneakerschecker.adapter

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.sneakers.sneakerschecker.R
import kotlinx.android.synthetic.main.item_phrase_layout.view.*

class PhraseAdapter(val items : List<String>, val context: Context) : RecyclerView.Adapter<ViewHolder>() {

    override fun getItemCount(): Int {
        return items.size
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(LayoutInflater.from(context).inflate(R.layout.item_phrase_layout, parent, false))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder?.tvPassPhrase?.text = items.get(position)
        holder?.tvPassPhraseIndex?.text = (position + 1).toString()
    }
}

class ViewHolder (view: View) : RecyclerView.ViewHolder(view) {
    val tvPassPhrase = view.txtPassPhrase
    val tvPassPhraseIndex = view.txtPassPhraseIndex
}