package com.sneakers.sneakerschecker.screens.activity

import android.content.Intent
import android.os.Bundle
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.iid.FirebaseInstanceId
import com.sneakers.sneakerschecker.MainActivity
import com.sneakers.sneakerschecker.R
import com.sneakers.sneakerschecker.constant.Constant
import com.sneakers.sneakerschecker.model.SharedPref
import com.sneakers.sneakerschecker.utils.CommonUtils
import kotlinx.android.synthetic.main.activity_splash.*


class SplashActivity : AppCompatActivity() {

    private lateinit var sharedPref: SharedPref

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        sharedPref = SharedPref(this)

        getFCMToken()
    }

    private fun getFCMToken() {
        FirebaseInstanceId.getInstance().instanceId
            .addOnCompleteListener(OnCompleteListener { task ->
                if (!task.isSuccessful) {
                    nextStep()
                    return@OnCompleteListener
                }

                // Get new Instance ID token
                sharedPref.setString(task.result!!.token, Constant.FCM_TOKEN)
                nextStep()
            })
    }

    private fun nextStep() {
        val myFadeInAnimation = AnimationUtils.loadAnimation(this, R.anim.fade_in_1s)
        myFadeInAnimation.setAnimationListener(object : Animation.AnimationListener {
            override fun onAnimationStart(animation: Animation) {

            }

            override fun onAnimationEnd(animation: Animation) {
                if (!CommonUtils.isNonLoginUser(this@SplashActivity)) {
                    if (sharedPref.getUser(Constant.LOGIN_USER)?.user?.username.isNullOrEmpty()) {
                        UpdateUserRegisterActivity.start(this@SplashActivity, null)
                        return
                    }
                }

                val intent = Intent(baseContext, MainActivity::class.java)
                startActivity(intent)
                finish()
            }

            override fun onAnimationRepeat(animation: Animation) {

            }
        })
        ivSplashLogo.startAnimation(myFadeInAnimation)
    }
}
