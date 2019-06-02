package com.sneakers.sneakerschecker.screens.activity

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.iid.FirebaseInstanceId
import com.sneakers.sneakerschecker.R
import com.sneakers.sneakerschecker.constant.Constant
import com.sneakers.sneakerschecker.model.SharedPref
import com.sneakers.sneakerschecker.screens.authenticationScreen.AuthenticationFragment
import com.sneakers.sneakerschecker.screens.authenticationScreen.SplashFragment


class AuthenticationActivity : AppCompatActivity() {
    private lateinit var sharedPref: SharedPref

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_authentication)

        sharedPref = SharedPref(this)

        if (savedInstanceState == null) {
            val transaction = supportFragmentManager.beginTransaction()
            val sharedPref = SharedPref(this)

            if (sharedPref.getBool(Constant.ACCOUNT_UNLINK)) {
                sharedPref.setBool(false, Constant.ACCOUNT_UNLINK)
                transaction.add(R.id.authentication_layout, AuthenticationFragment())
                    .commit()
            } else {
                transaction.add(R.id.authentication_layout, SplashFragment())
                    .commit()
            }
        }

        getFCMToken()
    }

    private fun getFCMToken() {
        FirebaseInstanceId.getInstance().instanceId
            .addOnCompleteListener(OnCompleteListener { task ->
                if (!task.isSuccessful) {
                    return@OnCompleteListener
                }

                // Get new Instance ID token
                sharedPref.setString(task.result!!.token, Constant.FCM_TOKEN)
            })
    }
}
