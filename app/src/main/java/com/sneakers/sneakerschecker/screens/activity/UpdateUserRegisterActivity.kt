package com.sneakers.sneakerschecker.screens.activity

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.sneakers.sneakerschecker.R
import com.sneakers.sneakerschecker.screens.fragment.LoginFragment
import com.sneakers.sneakerschecker.screens.fragment.UpdateUserRegisterFragment

class UpdateUserRegisterActivity : AppCompatActivity() {

    companion object {
        fun start(activity: Activity) {
            val intent = Intent(activity, UpdateUserRegisterActivity::class.java)
            activity.startActivity(intent)
            activity.finish()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_update_user_register)

        val transaction = supportFragmentManager.beginTransaction()
        transaction.add(R.id.fl_update_content, UpdateUserRegisterFragment())
            .commit()
    }
}
