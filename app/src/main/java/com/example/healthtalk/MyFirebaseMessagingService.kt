package com.example.healthtalk

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import android.widget.RemoteViews
import androidx.core.app.NotificationCompat
import com.example.healthtalk.pages.LoginPage
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class MyFirebaseMessagingService : FirebaseMessagingService() {

    private val channelId = "notification_channel"
    private val channelName = "com.example.healthtalk"

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        Log.d("Message Received", "....")

        if (remoteMessage.notification != null) {
            remoteMessage.notification!!.title?.let { remoteMessage.notification!!.body?.let { it1 ->
                generateNotification(it,
                    it1
                )
            } }
        }

    }

    @SuppressLint("RemoteViewLayout")
    fun getRemoteView(title: String, text: String): RemoteViews {
        val remoteView = RemoteViews("com.example.beeptalk", R.layout.notification);
        remoteView.setTextViewText(R.id.notification_title, title)
        remoteView.setTextViewText(R.id.notification_text, text)
        return remoteView;
    }

    private fun generateNotification(title: String, text: String) {
        val intent = Intent(this, LoginPage::class.java);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

        val pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_ONE_SHOT);

        var builder: NotificationCompat.Builder =
            NotificationCompat.Builder(applicationContext, channelId)
                .setSmallIcon(R.drawable.ic_person)
                .setAutoCancel(true)
                .setVibrate(longArrayOf(1000, 1000, 1000, 1000))
                .setOnlyAlertOnce(true)
                .setContentIntent(pendingIntent);

        builder = builder.setContent(getRemoteView(title, text))
        val notificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationChannel =
                NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_HIGH)
            notificationManager.createNotificationChannel(notificationChannel)
        }

        notificationManager.notify(0, builder.build())
    }

}