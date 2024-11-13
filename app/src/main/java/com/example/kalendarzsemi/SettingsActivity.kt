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
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.PreferenceManager
import com.example.kalendarzsemi.databinding.ActivitySettingsBinding
import java.util.Date

class SettingsActivity : AppCompatActivity() {

    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var binding: ActivitySettingsBinding
    private val REQUEST_CODE = 1001

    companion object {
        const val titleExtra = "titleExtra"
        const val messageExtra = "messageExtra"
        const val notificationID = 101
        const val channelID = "notification_channel_id"
    }

    @RequiresApi(Build.VERSION_CODES.S)
    override fun onCreate(savedInstanceState: Bundle?) {
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)
        val themePreference = sharedPreferences.getString("theme_preference", "blue")
        when (themePreference) {
            "blue" -> setTheme(R.style.Theme_KalendarzSemi_Blue)
            "green" -> setTheme(R.style.Theme_KalendarzSemi_Green)
            "red" -> setTheme(R.style.Theme_KalendarzSemi_Red)
        }


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(arrayOf(android.Manifest.permission.POST_NOTIFICATIONS), REQUEST_CODE)
            }
        }

        super.onCreate(savedInstanceState)
        binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)

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
        binding.btnSaveNotification.setOnClickListener { scheduleNotification() }
    }

    @RequiresApi(Build.VERSION_CODES.S)
    private fun scheduleNotification() {
        val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager

        if (!alarmManager.canScheduleExactAlarms()) {
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

        try {
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, getTime(), pendingIntent)
            showAlert(getTime(), "Nietypowy Kalendarz", "Przypomnienie o sprawdzeniu kalendarza")
        } catch (e: SecurityException) {
            e.printStackTrace()
            Toast.makeText(this, "Exact alarm permission is required", Toast.LENGTH_SHORT).show()
        }
    }

    private fun showAlert(time: Long, title: String, message: String) {
        val date = Date(time)
        val dateFormat = android.text.format.DateFormat.getLongDateFormat(applicationContext)
        val timeFormat = android.text.format.DateFormat.getTimeFormat(applicationContext)

        AlertDialog.Builder(this)
            .setTitle("Powiadomienie")
            .setMessage("Title: $title\nMessage: $message\nData: ${dateFormat.format(date)} ${timeFormat.format(date)}")
            .setPositiveButton("OK", null)
            .show()
    }

    private fun getTime(): Long {
        val calendar = java.util.Calendar.getInstance()
        calendar.set(
            binding.datePicker.year,
            binding.datePicker.month,
            binding.datePicker.dayOfMonth,
            binding.timePicker.hour,
            binding.timePicker.minute
        )
        return calendar.timeInMillis
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

    private fun saveThemePreference(theme: String) {
        sharedPreferences.edit().putString("theme_preference", theme).apply()
    }
}
