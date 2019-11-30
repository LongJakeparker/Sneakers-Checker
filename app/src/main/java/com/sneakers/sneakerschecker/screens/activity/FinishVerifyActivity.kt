package com.sneakers.sneakerschecker.screens.activity

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.sneakers.sneakerschecker.MainActivity
import com.sneakers.sneakerschecker.R
import kotlinx.android.synthetic.main.activity_finish_verify.*

class FinishVerifyActivity : AppCompatActivity() {

    companion object {
        fun start(activity: Activity) {
            val intent = Intent(activity, FinishVerifyActivity::class.java)
            activity.startActivity(intent)
            activity.finish()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_finish_verify)

        btnStart.setOnClickListener {
            UpdateUserRegisterActivity.start(this)
        }
    }
}
