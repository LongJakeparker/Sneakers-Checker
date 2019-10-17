package com.sneakers.sneakerschecker.screens.activity

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.sneakers.sneakerschecker.R
import com.sneakers.sneakerschecker.constant.Constant
import com.sneakers.sneakerschecker.model.CommonUtils
import com.sneakers.sneakerschecker.model.SharedPref
import kotlinx.android.synthetic.main.activity_confirm_register.*

class ConfirmRegisterActivity : AppCompatActivity() {
    private lateinit var sharedPref: SharedPref

    companion object {
        fun start(activity: Activity) {
            val intent = Intent(activity, ConfirmRegisterActivity::class.java)
            activity.startActivity(intent)
            activity.overridePendingTransition(R.anim.slide_in, R.anim.slide_out)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_confirm_register)

        sharedPref = SharedPref(this)

        tvPrivateKey.text = "0x" + sharedPref.getCredentials(Constant.USER_CREDENTIALS).ecKeyPair.privateKey.toString(16)

        setupViewPager()

        btnCopy.setOnClickListener { CommonUtils.copyToClipboard(this, tvPrivateKey.text.toString()) }
        btnClose.setOnClickListener { finish() }
    }

    private fun setupViewPager() {

    }


}
