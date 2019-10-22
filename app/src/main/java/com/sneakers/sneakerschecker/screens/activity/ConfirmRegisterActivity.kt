package com.sneakers.sneakerschecker.screens.activity

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.sneakers.sneakerschecker.R
import com.sneakers.sneakerschecker.adapter.ConfirmPrivateKeySliderAdapter
import com.sneakers.sneakerschecker.constant.Constant
import com.sneakers.sneakerschecker.model.CommonUtils
import com.sneakers.sneakerschecker.model.ConfirmPrivateKeySliderItem
import com.sneakers.sneakerschecker.model.SharedPref
import kotlinx.android.synthetic.main.activity_confirm_register.*

class ConfirmRegisterActivity : AppCompatActivity() {
    private lateinit var sharedPref: SharedPref
    private lateinit var confirmPrivateKeySliderList: ArrayList<ConfirmPrivateKeySliderItem>

    companion object {
        fun start(activity: Activity) {
            val intent = Intent(activity, ConfirmRegisterActivity::class.java)
            activity.startActivity(intent)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_confirm_register)

        sharedPref = SharedPref(this)

        tvPrivateKey.text = "0x" + sharedPref.getCredentials(Constant.USER_CREDENTIALS).ecKeyPair.privateKey.toString(16)
//        tvPrivateKey.text = "0xadvregaregaaggregqmj44235431343rwe234n23o423"

        confirmPrivateKeySliderList = arrayListOf(
            ConfirmPrivateKeySliderItem(
                resources.getString(R.string.confirm_title_1),
                resources.getString(R.string.confirm_content_1)
            ),
            ConfirmPrivateKeySliderItem(
                resources.getString(R.string.confirm_title_2),
                resources.getString(R.string.confirm_content_temp)
            ),
            ConfirmPrivateKeySliderItem(
                resources.getString(R.string.confirm_title_3),
                resources.getString(R.string.confirm_content_temp)
            )
        )

        setupViewPager()

        btnCopy.setOnClickListener { CommonUtils.copyToClipboard(this, tvPrivateKey.text.toString()) }
        btnClose.setOnClickListener { finish() }
    }

    private fun setupViewPager() {
        viewpager.adapter = ConfirmPrivateKeySliderAdapter(this, confirmPrivateKeySliderList)
        dotsIndicator.setViewPager(viewpager)
    }


}
