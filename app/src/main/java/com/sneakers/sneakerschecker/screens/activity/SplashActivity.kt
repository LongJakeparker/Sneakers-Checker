package com.sneakers.sneakerschecker.screens.activity

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.support.v7.app.AppCompatActivity
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.iid.FirebaseInstanceId
import com.sneakers.sneakerschecker.MainActivity
import com.sneakers.sneakerschecker.R
import com.sneakers.sneakerschecker.constant.Constant
import com.sneakers.sneakerschecker.model.SharedPref


class SplashActivity : AppCompatActivity() {
    /** Delay duration */
    private val SPLASH_DISPLAY_LENGTH: Long = 4000

    private lateinit var sharedPref: SharedPref

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        sharedPref = SharedPref(this)

        Handler().postDelayed({
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            overridePendingTransition(R.anim.slide_in, R.anim.slide_out)
            finish()
//            val sharedPref = SharedPref(this.context!!)
//            if (sharedPref.getString(Constant.USER_CREDENTIALS) != "") {
//                val intent = Intent(activity, MainActivity::class.java)
//                startActivity(intent)
//                activity!!.finish()
//            }
//            else {
//                val transaction = activity!!.supportFragmentManager.beginTransaction();
//                transaction.replace(R.id.authentication_layout, AuthenticationFragment())
//                    .commitAllowingStateLoss()
//            }
        }, SPLASH_DISPLAY_LENGTH)

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
