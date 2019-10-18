package com.sneakers.sneakerschecker.screens.activity

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.sneakers.sneakerschecker.R
import com.sneakers.sneakerschecker.screens.fragment.CreateNewFragment

class CreateNewActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_new)

        val transaction = supportFragmentManager.beginTransaction()
        transaction.add(R.id.fl_create_content, CreateNewFragment())
            .commit()
    }
}
