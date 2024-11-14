package com.example.kalendarzsemi

import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.app.NotificationCompat

const val notificationID = 1
const val channelID = "channel1"
const val titleExtra = "titleExtra"
const val messageExtra = "messageExtra"

class Notification : BroadcastReceiver() {
    private val TAG = "Notification"

    override fun onReceive(context: Context?, intent: Intent?) {
        Log.d(TAG, "onReceive: Notification received")

        val title = intent?.getStringExtra(titleExtra)
        val message = intent?.getStringExtra(messageExtra)
        Log.d(TAG, "onReceive: Notification Title = $title, Message = $message")

        // Tworzenie Intent do otwarcia kalendarza
        val calendarIntent = Intent(context, CalendarActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            calendarIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Logowanie szczegółów powiadomienia
        val notification = context?.let {
            NotificationCompat.Builder(it, channelID)
                .setSmallIcon(R.drawable.baseline_notifications_24)
                .setContentTitle(title)
                .setContentText(message)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .build()
        }
        Log.d(TAG, "onReceive: Building and showing notification")

        val manager = context?.getSystemService(Context.NOTIFICATION_SERVICE) as android.app.NotificationManager
        manager.notify(notificationID, notification)
    }
}
