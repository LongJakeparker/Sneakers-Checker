package com.sneakers.sneakerschecker.screens.activity

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import com.sneakers.sneakerschecker.R
import com.sneakers.sneakerschecker.adapter.ListCardAdapter
import com.sneakers.sneakerschecker.model.CreditCard
import kotlinx.android.synthetic.main.activity_manage_card.*

class ManageCardActivity : AppCompatActivity(), View.OnClickListener {
    var cardListAdapter: ListCardAdapter? = null
    var cardList = ArrayList<CreditCard?>()

    companion object {
        fun start(activity: Activity) {
            val intent = Intent(activity, ManageCardActivity::class.java)
            activity.startActivity(intent)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_manage_card)

        cardList.add(null)
        cardList.add(null)
        cardList.add(null)

        setUpCardList()

        ibBack.setOnClickListener(this)
    }

    private fun setUpCardList() {
        val layoutManager = LinearLayoutManager(this)
        rvCreditCard.layoutManager = layoutManager
        cardListAdapter = ListCardAdapter(cardList)
        cardListAdapter!!.setListener(object : ListCardAdapter.Listener {
            override fun onSelectItem(title: String) {

            }

        })
        rvCreditCard.adapter = cardListAdapter
        rvCreditCard.isFocusable = false
        rvCreditCard.isNestedScrollingEnabled = false
    }

    override fun onClick(v: View?) {
        when (v) {
            ibBack -> finish()
        }
    }
}
