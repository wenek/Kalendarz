package com.example.kalendarzsemi

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
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
    private val tag = "Notification"

    companion object {
        const val NOTIFICATIONID = 101
        const val CHANNELID = "notification_channel_id"
    }

    @RequiresApi(Build.VERSION_CODES.S)
    override fun onCreate(savedInstanceState: Bundle?) {
        Log.d(tag, "onCreate: SettingsActivity started")
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

        displaySaveNotification()

        val savedTheme = sharedPreferences.getString("theme_preference", "light")
        when (savedTheme) {
            "light" -> binding.rbLight.isChecked = true
            "dark" -> binding.rbDark.isChecked = true
            "vibrant" -> binding.rbCustom.isChecked = true
        }

        binding.radioGroupTheme.setOnCheckedChangeListener { _, checkedId ->
            val theme = when (checkedId) {
                R.id.rbLight -> "light"
                R.id.rbDark -> "dark"
                R.id.rbCustom -> "vibrant"
                else -> "light"
            }
            saveThemePreference(theme)
            Toast.makeText(this, "Theme changed to: $theme", Toast.LENGTH_SHORT).show()
            restartCalendarActivityWithTheme()
        }

        binding.btnReturn.setOnClickListener { finish() }
        createNotificationChannel()

        binding.btnSaveNotification.setOnClickListener {
            Log.d(tag, "btnSaveNotification: Scheduling daily notification")
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                checkExactAlarmPermission()
            } else {
                Log.d(tag, "Exact alarm permission check skipped for Android versions below 12")
            }
            scheduleDailyNotification()
        }
        binding.btnCancelNotification.setOnClickListener {
            cancelScheduledNotification()
        }
    }

    @RequiresApi(Build.VERSION_CODES.S)
    private fun checkExactAlarmPermission() {
        val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        if (!alarmManager.canScheduleExactAlarms()) {
            Log.w(tag, "Exact alarms permission not granted")
            val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM)
                .setData(Uri.parse("package:$packageName"))
            startActivity(intent)
            Toast.makeText(this, "Włącz uprawnienia do ustawiania powiadomień dokładnych", Toast.LENGTH_SHORT).show()
        }
    }

    @RequiresApi(Build.VERSION_CODES.S)
    private fun scheduleDailyNotification() {
        Log.d(tag, "scheduleDailyNotification: Starting notification setup")
        val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(applicationContext, Notification::class.java).apply {
            putExtra(titleExtra, "Nietypowy Kalendarz")
            putExtra(messageExtra, "Przypomnienie o sprawdzeniu kalendarza")
        }

        val pendingIntent = PendingIntent.getBroadcast(
            applicationContext,
            NOTIFICATIONID,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val triggerTime = getDailyTriggerTime()
        saveNotificationTime(triggerTime)
        displaySaveNotification()
        try {
            alarmManager.setRepeating(
                AlarmManager.RTC_WAKEUP,
                triggerTime,
                AlarmManager.INTERVAL_DAY,
                pendingIntent
            )
            showTimeSetConfirmation(triggerTime)
        } catch (e: SecurityException) {
            e.printStackTrace()
            Toast.makeText(this, "Nie można ustawić powiadomienia. Sprawdź uprawnienia.", Toast.LENGTH_LONG).show()
        }
    }

    private fun getDailyTriggerTime(): Long {
        val calendar = java.util.Calendar.getInstance()

        calendar.set(
            java.util.Calendar.HOUR_OF_DAY,
            binding.timePicker.hour
        )
        calendar.set(java.util.Calendar.MINUTE, binding.timePicker.minute)
        calendar.set(java.util.Calendar.SECOND, 0)

        if (calendar.timeInMillis <= System.currentTimeMillis()) {
            calendar.add(java.util.Calendar.DAY_OF_YEAR, 1)
        }
        Log.d(tag, "getDailyTriggerTime: Next notification time is ${calendar.timeInMillis}")
        return calendar.timeInMillis
    }

    private fun showTimeSetConfirmation(triggerTime: Long) {
        val formattedTime = formatTime(triggerTime)
        Toast.makeText(this, "Powiadomienie ustawione na: $formattedTime", Toast.LENGTH_SHORT).show()
    }

    private fun cancelScheduledNotification() {
        val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(applicationContext, Notification::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            applicationContext, NOTIFICATIONID, intent, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        alarmManager.cancel(pendingIntent)
        Toast.makeText(this, "Zaplanowane powiadomienie anulowane", Toast.LENGTH_SHORT).show()
    }

    private fun createNotificationChannel() {
        val name = "Notify Channel"
        val desc = "A Description of the Channel"
        val importance = android.app.NotificationManager.IMPORTANCE_DEFAULT
        val channel = android.app.NotificationChannel(CHANNELID, name, importance).apply {
            description = desc
        }
        val notificationManager = getSystemService(android.app.NotificationManager::class.java)
        notificationManager?.createNotificationChannel(channel)
    }

    private fun displaySaveNotification() {
        val timeInMillis = sharedPreferences.getLong("notification_time", 0L)
        if (timeInMillis != 0L) {
            val formattedTime = formatTime(timeInMillis)
            binding.tvNotificationTime.text = getString(R.string.notification_time_info, formattedTime)
            Log.d(tag, "displaySavedNotificationTime: Displaying saved time $formattedTime")
        } else {
            binding.tvNotificationTime.text = getString(R.string.no_notification_time_set)
        }
    }

    private fun saveNotificationTime(timeInMillis: Long) {
        sharedPreferences.edit().putLong("notification_time", timeInMillis).apply()
        Log.d(tag, "saveNotificationTime: Notification time saved as $timeInMillis")
    }

    private fun formatTime(timeInMillis: Long): String {
        val dateFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
        return dateFormat.format(Date(timeInMillis))
    }

    private fun saveThemePreference(theme: String) {
        sharedPreferences.edit().putString("theme_preference", theme).apply()
    }

    private fun restartCalendarActivityWithTheme() {
        val intent = Intent(this, CalendarActivity::class.java)
        startActivity(intent)
        finish()
    }
}
