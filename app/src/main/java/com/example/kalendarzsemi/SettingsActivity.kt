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
        val themePreference = sharedPreferences.getString("theme_preference", "light")
        when (themePreference) {
            "light" -> setTheme(R.style.Theme_KalendarzSemi_Light)
            "dark" -> setTheme(R.style.Theme_KalendarzSemi_Dark)
            "vibrant" -> setTheme(R.style.Theme_KalendarzSemi_Vibrant)
        }

        super.onCreate(savedInstanceState)
        binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Display previously saved notification time
        displaySaveNotification()

        // Set the radio buttons based on the saved theme preference
        val savedTheme = sharedPreferences.getString("theme_preference", "light")
        when (savedTheme) {
            "light" -> binding.rbLight.isChecked = true
            "dark" -> binding.rbDark.isChecked = true
            "vibrant" -> binding.rbCustom.isChecked = true
        }

        // Handle theme change when radio button is selected
        binding.radioGroupTheme.setOnCheckedChangeListener { _, checkedId ->
            val theme = when (checkedId) {
                R.id.rbLight -> "light"
                R.id.rbDark -> "dark"
                R.id.rbCustom -> "vibrant"
                else -> "light"
            }
            saveThemePreference(theme)

            // After changing the theme, restart MainActivity with the updated theme
            restartMainActivityWithTheme()
        }

        // Button handling
        binding.btnReturn.setOnClickListener { finish() }
        createNotificationChannel()

        // Schedule daily notifications
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
        sharedPreferences.edit().putLong("notification_time", timeInMillis).apply()
        Log.d(TAG, "saveNotificationTime: Notification time saved as $timeInMillis")
    }

    private fun formatTime(timeInMillis: Long): String {
        val dateFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
        return dateFormat.format(Date(timeInMillis))
    }

    private fun saveThemePreference(theme: String) {
        sharedPreferences.edit().putString("theme_preference", theme).apply()
    }

    // Function to restart MainActivity with the new theme
    private fun restartMainActivityWithTheme() {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish() // Close the current SettingsActivity
    }
}

