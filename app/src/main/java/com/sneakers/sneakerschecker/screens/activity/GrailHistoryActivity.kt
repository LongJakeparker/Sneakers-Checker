package com.sneakers.sneakerschecker.screens.activity

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import com.sneakers.sneakerschecker.R
import com.sneakers.sneakerschecker.constant.Constant

class GrailHistoryActivity : BaseActivity() {

    companion object {
        fun start(activity: Activity, password: String) {
            val intent = Intent(activity, FinishVerifyActivity::class.java)
            intent.putExtra(Constant.EXTRA_USER_PASSWORD, password)
            activity.startActivity(intent)
            activity.finish()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun getLayoutId(): Int {
        return R.layout.activity_grail_history
    }

    override fun getScreenTitle(): String {
        return getString(R.string.label_grail_story)
    }
}
