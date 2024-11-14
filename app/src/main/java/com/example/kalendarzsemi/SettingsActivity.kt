package com.example.kalendarzsemi

import android.app.AlarmManager
import android.app.AlertDialog
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.PreferenceManager
import com.example.kalendarzsemi.databinding.ActivitySettingsBinding
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class SettingsActivity : AppCompatActivity() {

    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var binding: ActivitySettingsBinding
    private val REQUEST_CODE = 1001
    private val TAG = "Notification"

    companion object {
        const val notificationID = 101
        const val channelID = "notification_channel_id"
    }

    @RequiresApi(Build.VERSION_CODES.S)
    override fun onCreate(savedInstanceState: Bundle?) {
        Log.d(TAG, "onCreate: SettingsActivity started")
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)
        val themePreference = sharedPreferences.getString("theme_preference", "blue")
        when (themePreference) {
            "blue" -> setTheme(R.style.Theme_KalendarzSemi_Blue)
            "green" -> setTheme(R.style.Theme_KalendarzSemi_Green)
            "red" -> setTheme(R.style.Theme_KalendarzSemi_Red)
        }

        // Logowanie podczas sprawdzania uprawnień do powiadomień
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                Log.d(TAG, "onCreate: Requesting POST_NOTIFICATIONS permission")
                requestPermissions(arrayOf(android.Manifest.permission.POST_NOTIFICATIONS), REQUEST_CODE)
            } else {
                Log.d(TAG, "onCreate: POST_NOTIFICATIONS permission already granted")
            }
        }

        super.onCreate(savedInstanceState)
        binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        displaySaveNotification()

        // Theme handling
        val savedTheme = sharedPreferences.getString("theme_preference", "blue")
        when (savedTheme) {
            "blue" -> binding.rbBlue.isChecked = true
            "green" -> binding.rbGreen.isChecked = true
            "red" -> binding.rbRed.isChecked = true
        }

        binding.radioGroupTheme.setOnCheckedChangeListener { _, checkedId ->
            val theme = when (checkedId) {
                R.id.rbBlue -> "blue"
                R.id.rbGreen -> "green"
                R.id.rbRed -> "red"
                else -> "blue"
            }
            saveThemePreference(theme)
            recreate()
        }

        // Button handling
        binding.btnReturn.setOnClickListener { finish() }
        createNotificationChannel()
        binding.btnSaveNotification.setOnClickListener {
            Log.d(TAG, "btnSaveNotification: Scheduling daily notification")
            scheduleDailyNotification()
        }
    }

    @RequiresApi(Build.VERSION_CODES.S)
    private fun scheduleDailyNotification() {
        Log.d(TAG, "scheduleDailyNotification: Starting notification setup")
        val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager

        if (!alarmManager.canScheduleExactAlarms()) {
            Log.w(TAG, "scheduleDailyNotification: Exact alarms permission not granted")
            val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM)
                .setData(Uri.parse("package:$packageName"))
            startActivity(intent)
            Toast.makeText(this, "Enable exact alarm permission in settings", Toast.LENGTH_SHORT).show()
            return
        }

        val intent = Intent(applicationContext, Notification::class.java).apply {
            putExtra(titleExtra, "Nietypowy Kalendarz")
            putExtra(messageExtra, "Przypomnienie o sprawdzeniu kalendarza")
        }

        val pendingIntent = PendingIntent.getBroadcast(
            applicationContext,
            notificationID,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val triggerTime = getDailyTriggerTime()
        saveNotificationTime(triggerTime)
        displaySaveNotification()

        try {
            // Set a repeating alarm for daily notifications
            alarmManager.setRepeating(
                AlarmManager.RTC_WAKEUP,
                triggerTime,
                AlarmManager.INTERVAL_DAY,  // 24-hour interval
                pendingIntent
            )

            showAlert(triggerTime, "Nietypowy Kalendarz", "Codzienne przypomnienie o sprawdzeniu kalendarza")
        } catch (e: SecurityException) {
            e.printStackTrace()
            Toast.makeText(this, "Exact alarm permission is required", Toast.LENGTH_SHORT).show()
        }
    }

    // Helper method to get the first trigger time
    private fun getDailyTriggerTime(): Long {
        val calendar = java.util.Calendar.getInstance()

        // Set the calendar to the time chosen by the user
        calendar.set(
            java.util.Calendar.HOUR_OF_DAY,
            binding.timePicker.hour
        )
        calendar.set(java.util.Calendar.MINUTE, binding.timePicker.minute)
        calendar.set(java.util.Calendar.SECOND, 0)

        // If the time is earlier than the current time, set the alarm for the next day
        if (calendar.timeInMillis <= System.currentTimeMillis()) {
            calendar.add(java.util.Calendar.DAY_OF_YEAR, 1)
        }
        Log.d(TAG, "getDailyTriggerTime: Next notification time is ${calendar.timeInMillis}")
        return calendar.timeInMillis
    }

    private fun showAlert(time: Long, title: String, message: String) {

        AlertDialog.Builder(this)
            .setTitle("Powiadomienie")
            .setMessage("Title: $title\nMessage: $message\n")
            .setPositiveButton("OK", null)
            .show()
    }

    private fun createNotificationChannel() {
        val name = "Notif Channel"
        val desc = "A Description of the Channel"
        val importance = android.app.NotificationManager.IMPORTANCE_DEFAULT
        val channel = android.app.NotificationChannel(channelID, name, importance).apply {
            description = desc
        }
        val notificationManager = getSystemService(android.app.NotificationManager::class.java)
        notificationManager?.createNotificationChannel(channel)
    }

    private fun displaySaveNotification() {
        val timeInMillis = sharedPreferences.getLong("notification_time", 0L)
        if (timeInMillis != 0L) {
            val formattedTime = formatTime(timeInMillis)
            binding.tvNotificationTime.text = "Wybrana godzina: $formattedTime"
            Log.d(TAG, "displaySavedNotificationTime: Displaying saved time $formattedTime")
        }
    }

    private fun saveNotificationTime(timeInMillis: Long) {
        // Zapisywanie godziny powiadomienia w SharedPreferences
        sharedPreferences.edit().putLong("notification_time", timeInMillis).apply()
        Log.d(TAG, "saveNotificationTime: Notification time saved as $timeInMillis")
    }

    private fun formatTime(timeInMillis: Long): String {
        // Formatowanie godziny na czytelny tekst
        val dateFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
        return dateFormat.format(Date(timeInMillis))
    }

    private fun saveThemePreference(theme: String) {
        sharedPreferences.edit().putString("theme_preference", theme).apply()
    }
}
