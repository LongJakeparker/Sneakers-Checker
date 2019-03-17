package com.sneakers.sneakerschecker.adapter

import android.content.Context
import android.graphics.Color
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.sneakers.sneakerschecker.R
import android.util.Log
import com.sneakers.sneakerschecker.screens.interfaces.ConfirmPhraseOnClick
import kotlinx.android.synthetic.main.item_confirm_phrase_layout.view.*
import kotlinx.android.synthetic.main.item_phrase_layout.view.*
import java.util.logging.LoggingMXBean


class ConfirmPhraseAdapter(val items : List<String>, val context: Context) : RecyclerView.Adapter<ConfirmPhraseViewHolder>() {

    override fun getItemCount(): Int {
        return items.size
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ConfirmPhraseViewHolder {
        return ConfirmPhraseViewHolder(LayoutInflater.from(context).inflate(R.layout.item_confirm_phrase_layout, parent, false))
    }

    override fun onBindViewHolder(holder: ConfirmPhraseViewHolder, position: Int) {
        holder?.tvPassPhrase?.text = items[position]
        holder?.itemView.setOnClickListener{
            //on click
            holder?.tvPassPhrase.setBackgroundResource(R.drawable.confirm_button_background)
        }
    }
}

class ConfirmPhraseViewHolder (view: View) : RecyclerView.ViewHolder(view) {
    val tvPassPhrase = view.txtPassPhraseConfirm

}