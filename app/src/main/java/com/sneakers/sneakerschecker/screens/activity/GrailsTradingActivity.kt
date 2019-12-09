package com.sneakers.sneakerschecker.screens.activity

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.sneakers.sneakerschecker.R
import com.sneakers.sneakerschecker.constant.Constant
import com.sneakers.sneakerschecker.model.SneakerModel
import com.sneakers.sneakerschecker.screens.fragment.CreateTransferFragment

class GrailsTradingActivity : AppCompatActivity() {

    companion object {
        fun start(activity: Activity, sneaker: SneakerModel) {
            val intent = Intent(activity, GrailsTradingActivity::class.java)
            intent.putExtra(Constant.EXTRA_SNEAKER, sneaker)
            activity.startActivity(intent)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_grails_trading)

        val transaction = supportFragmentManager.beginTransaction()
        transaction.add(R.id.fl_transfer_content, CreateTransferFragment())
            .commit()
    }
}
