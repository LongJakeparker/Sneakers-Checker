package com.sneakers.sneakerschecker.adapter

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.sneakers.sneakerschecker.R
import com.sneakers.sneakerschecker.interfaces.ConfirmPhraseCallBack
import kotlinx.android.synthetic.main.item_confirm_phrase_layout.view.*


class ConfirmPhraseAdapter(val items : List<String>, val context: Context, val phraseCallBack: ConfirmPhraseCallBack) : RecyclerView.Adapter<ConfirmPhraseViewHolder>() {

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
            phraseCallBack.ClickCallBack(items[position])
        }
    }
}

class ConfirmPhraseViewHolder (view: View) : RecyclerView.ViewHolder(view) {
    val tvPassPhrase = view.txtPassPhraseConfirm

}