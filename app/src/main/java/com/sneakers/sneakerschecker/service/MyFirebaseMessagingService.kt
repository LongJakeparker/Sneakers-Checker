package com.sneakers.sneakerschecker.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.media.RingtoneManager
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.google.gson.Gson
import com.sneakers.sneakerschecker.R
import com.sneakers.sneakerschecker.TrueGrailsApplication
import com.sneakers.sneakerschecker.constant.Constant
import com.sneakers.sneakerschecker.model.ReloadCollectionEvent
import com.sneakers.sneakerschecker.model.SharedPref
import com.sneakers.sneakerschecker.model.SneakerModel
import com.sneakers.sneakerschecker.screens.activity.ObtainGrailActivity
import org.greenrobot.eventbus.EventBus

class MyFirebaseMessagingService : FirebaseMessagingService() {
    private var NOTIFICATION_ID = 1
    private lateinit var sharedPref: SharedPref

    override fun onConfigurationChanged(newConfig: Configuration?) {
        super.onConfigurationChanged(newConfig)
        /* now let's wait until the debugger attaches */
        android.os.Debug.waitForDebugger()
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        val payload = remoteMessage.data["payload"]
        val title = remoteMessage.data["title"]
        val body = remoteMessage.data["body"]
        val sneakerInfo = Gson().fromJson(payload, SneakerModel::class.java)
        if (!TrueGrailsApplication.mInstance?.isAppBackground()!!) {
            ObtainGrailActivity.start(baseContext, sneakerInfo, true)
            EventBus.getDefault().post(ReloadCollectionEvent())
        } else {
            generateNotification(
                sneakerInfo,
                body,
                title
            )
        }
    }

    private fun generateNotification(sneakerInfo: SneakerModel, body: String?, title: String?) {
        val intent: Intent = Intent(applicationContext, NotificationActionService::class.java)
            .putExtra(Constant.EXTRA_SNEAKER, sneakerInfo)
            .putExtra(Constant.EXTRA_IS_OBTAINED, true)
            .setAction(NotificationActionService.ACTION_OBTAINED_GRAIL)

        val pendingIntent = PendingIntent.getService(applicationContext, 0, intent, PendingIntent.FLAG_ONE_SHOT)

        val soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)

        createNotificationChannel()

        val notificationBuilder = NotificationCompat.Builder(this, Constant.CHANNEL_TRANSFER)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(title)
            .setContentText(body)
            .setAutoCancel(true)
            .setSound(soundUri)
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_HIGH)

        val notificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (NOTIFICATION_ID > 1073741824) {
            NOTIFICATION_ID = 0
        }

        notificationManager.notify(NOTIFICATION_ID++, notificationBuilder.build())
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // Create the NotificationChannel
            val name = Constant.CHANNEL_TRANSFER
            val descriptionText = Constant.CHANNEL_TRANSFER
            val importance = NotificationManager.IMPORTANCE_HIGH
            val mChannel = NotificationChannel(Constant.CHANNEL_TRANSFER, name, importance)
            mChannel.description = descriptionText
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(mChannel)
        }
    }

    override fun onNewToken(token: String?) {
        Log.e("FCM-TOKEN", token)
        sharedPref = SharedPref(this)
        if (token != null) {
            sharedPref.setString(token, Constant.FCM_TOKEN)
        }
    }
}