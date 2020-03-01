package com.sneakers.sneakerschecker.screens.activity

import android.app.Activity
import android.content.Intent
import com.sneakers.sneakerschecker.R

class SettingActivity : BaseActivity() {

    companion object {
        fun start(activity: Activity) {
            val intent = Intent(activity, SettingActivity::class.java)
            activity.startActivity(intent)
        }
    }

    override fun getLayoutId(): Int {
        return R.layout.activity_setting
    }

    override fun isShowBackButton(): Boolean {
        return true
    }

    override fun getScreenTitleId(): Int {
        return R.string.label_button_setting
    }
}
