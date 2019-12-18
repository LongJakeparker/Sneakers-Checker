package com.sneakers.sneakerschecker.screens.activity

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.sneakers.sneakerschecker.R
import com.sneakers.sneakerschecker.screens.fragment.UserProfileFragment

class UserProfileActivity : AppCompatActivity() {

    companion object {
        fun start(context: Context) {
            val intent = Intent(context, UserProfileActivity::class.java)
            context.startActivity(intent)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_profile)

        val transaction = supportFragmentManager.beginTransaction()
        transaction.add(R.id.fl_update_content, UserProfileFragment())
            .commit()
    }
}
