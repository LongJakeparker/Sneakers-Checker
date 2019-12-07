package com.sneakers.sneakerschecker.screens.activity

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.sneakers.sneakerschecker.MainActivity
import com.sneakers.sneakerschecker.R
import com.sneakers.sneakerschecker.constant.Constant
import kotlinx.android.synthetic.main.activity_finish_verify.*

class FinishVerifyActivity : AppCompatActivity() {

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
        setContentView(R.layout.activity_finish_verify)

        val password = intent?.getStringExtra(Constant.EXTRA_USER_PASSWORD)

        btnStart.setOnClickListener {
            UpdateUserRegisterActivity.start(this, password!!)
        }
    }
}
