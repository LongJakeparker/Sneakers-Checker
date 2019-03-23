package com.sneakers.sneakerschecker

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v4.view.GravityCompat
import android.view.Gravity
import android.view.View
import com.sneakers.sneakerschecker.constant.Constant
import com.sneakers.sneakerschecker.model.GenerateQrCode
import com.sneakers.sneakerschecker.model.SharedPref
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity(), View.OnClickListener {

    private lateinit var sharedPref: SharedPref

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        sharedPref = SharedPref(this)

        tvAddressMain.text = sharedPref.getString(Constant.WALLET_ADDRESS)

        val qrCode = GenerateQrCode.WalletAddress(this, 0.55)

        if (qrCode != null) {
            ivQrCodeMain.setImageBitmap(qrCode)
        }

        btnMenuMain.setOnClickListener(this)
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.btnMenuMain -> drawer_layout.openDrawer(GravityCompat.START)
        }
    }
}
