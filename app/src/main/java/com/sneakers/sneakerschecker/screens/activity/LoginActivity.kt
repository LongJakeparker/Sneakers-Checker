package com.sneakers.sneakerschecker.screens.activity

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.sneakers.sneakerschecker.R
import com.sneakers.sneakerschecker.screens.fragment.ImportPrivateKeyFragment

class LoginActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        val transaction = supportFragmentManager.beginTransaction()
        transaction.add(R.id.fl_login_content, ImportPrivateKeyFragment())
            .commit()
    }
}
