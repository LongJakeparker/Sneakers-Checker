package com.sneakers.sneakerschecker.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.os.Build
import android.support.v4.app.NotificationCompat
import android.util.Log
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.sneakers.sneakerschecker.MainActivity
import com.sneakers.sneakerschecker.R
import com.sneakers.sneakerschecker.constant.Constant

class MyFirebaseMessagingService: FirebaseMessagingService() {
    private var NOTIFICATION_ID = 1

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        generateNotification(remoteMessage.notification?.body, remoteMessage.notification?.title)
    }

    private fun generateNotification(body: String?, title: String?) {
        val intent = Intent(this, MainActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)

        val pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_ONE_SHOT)

        val soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)

        createNotificationChannel()

        val notificationBuilder = NotificationCompat.Builder(this, Constant.CHANNEL_TRANSFER)
            .setSmallIcon(R.drawable.sneaker_image_sample)
            .setContentTitle(title)
            .setContentText(body)
            .setAutoCancel(true)
            .setSound(soundUri)
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_HIGH)

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

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

    override fun onNewToken(p0: String?) {
        Log.e("FCM-TOKEN", p0)
        //sendRegistrationToServer()
    }
}