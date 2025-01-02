package com.example.kalendarzsemi

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.preference.PreferenceManager

const val notificationID = 1
const val channelID = "channel1"
const val titleExtra = "titleExtra"
const val messageExtra = "messageExtra"

class Notification : BroadcastReceiver() {
    private val tag = "Notification"

    override fun onReceive(context: Context?, intent: Intent?) {
        Log.d(tag, "onReceive: Notification received")

        val title = intent?.getStringExtra(titleExtra)
        val message = intent?.getStringExtra(messageExtra)

        // Tworzenie powiadomienia
        val calendarIntent = Intent(context, CalendarActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            calendarIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = context?.let {
            NotificationCompat.Builder(it, channelID)
                .setSmallIcon(R.drawable.baseline_notifications_24)
                .setContentTitle(title)
                .setContentText(message)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .build()
        }

        val manager = context?.getSystemService(Context.NOTIFICATION_SERVICE) as android.app.NotificationManager
        manager.notify(notificationID, notification)

        // Ustaw następne powiadomienie
        scheduleNextNotification(context)
    }

    private fun scheduleNextNotification(context: Context) {
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
        val hour = sharedPreferences.getInt("notification_hour", 9) // Default hour: 9 AM
        val minute = sharedPreferences.getInt("notification_minute", 0) // Default minute: 00

        val calendar = java.util.Calendar.getInstance()
        calendar.set(java.util.Calendar.HOUR_OF_DAY, hour)
        calendar.set(java.util.Calendar.MINUTE, minute)
        calendar.set(java.util.Calendar.SECOND, 0)
        calendar.add(java.util.Calendar.DAY_OF_YEAR, 1) // Schedule for the next day

        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
            // For API level 31 or higher
            if (!alarmManager.canScheduleExactAlarms()) {
                Log.w(tag, "Exact alarms permission not granted. Requesting permission.")
                val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM)
                    .setData(Uri.parse("package:${context.packageName}"))
                context.startActivity(intent)
                return
            }
        }

        val intent = Intent(context, Notification::class.java).apply {
            putExtra(titleExtra, "Nietypowy Kalendarz")
            putExtra(messageExtra, "Przypomnienie o sprawdzeniu kalendarza")
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            notificationID,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        try {
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                calendar.timeInMillis,
                pendingIntent
            )
            Log.d(tag, "scheduleNextNotification: Next notification set for ${calendar.time}")
        } catch (e: SecurityException) {
            e.printStackTrace()
            Log.e(tag, "Failed to schedule exact alarm: ${e.message}")
        }
    }

}