package com.sneakers.sneakerschecker.service

import android.app.IntentService
import android.content.Intent
import android.text.TextUtils
import com.sneakers.sneakerschecker.MainActivity
import com.sneakers.sneakerschecker.TrueGrailsApplication
import com.sneakers.sneakerschecker.constant.Constant
import com.sneakers.sneakerschecker.model.ReloadCollectionEvent
import com.sneakers.sneakerschecker.model.SneakerModel
import com.sneakers.sneakerschecker.screens.activity.ObtainGrailActivity
import org.greenrobot.eventbus.EventBus


class NotificationActionService : IntentService(NotificationActionService::class.java.simpleName) {

    companion object {
        val ACTION_OBTAINED_GRAIL = "action_obtained_grail"
    }

    override fun onHandleIntent(intent: Intent?) {
        val action = intent?.action
        if (TextUtils.equals(action, ACTION_OBTAINED_GRAIL)) {
            val sneakerInfo = intent?.getSerializableExtra(Constant.EXTRA_SNEAKER) as SneakerModel
            val isObtained = intent.getBooleanExtra(Constant.EXTRA_IS_OBTAINED, false)
            if (TrueGrailsApplication.mInstance?.isAppRunning()!!) {
                EventBus.getDefault().post(ReloadCollectionEvent())
                ObtainGrailActivity.start(this, sneakerInfo, true)
            } else {
                val parentIntent = Intent(this, MainActivity::class.java)
                parentIntent.putExtra(Constant.EXTRA_IS_OBTAINED, isObtained)
                parentIntent.putExtra(Constant.EXTRA_SNEAKER, sneakerInfo)
                startActivity(parentIntent)
            }
        }
    }
}