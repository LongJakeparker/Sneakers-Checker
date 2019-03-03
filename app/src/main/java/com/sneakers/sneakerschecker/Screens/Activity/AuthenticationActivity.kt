package com.sneakers.sneakerschecker.Screens.Activity

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import com.sneakers.sneakerschecker.R
import com.sneakers.sneakerschecker.Screens.AuthenticationScreen.SplashFragment

class AuthenticationActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_authentication)

        if (savedInstanceState == null) {
            val transaction = supportFragmentManager.beginTransaction();
            transaction.add(R.id.authentication_layout, SplashFragment())
                .commit()
        }
    }
}
