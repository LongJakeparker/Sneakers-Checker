package com.sneakers.sneakerschecker.screens.activity

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import com.sneakers.sneakerschecker.R
import com.sneakers.sneakerschecker.screens.authenticationScreen.CreateNewFragment

class LoginActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        val transaction = supportFragmentManager.beginTransaction()
        transaction.add(R.id.fl_login_content, CreateNewFragment())
            .commit()
    }
}
