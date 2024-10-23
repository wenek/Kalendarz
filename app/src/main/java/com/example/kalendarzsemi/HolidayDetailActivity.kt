package com.example.kalendarzsemi

import android.content.SharedPreferences
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.PreferenceManager

class HolidayDetailActivity : AppCompatActivity() {

    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {

        // Wczytanie preferencji kolorystycznych
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)
        val themePreference = sharedPreferences.getString("theme_preference", "blue")
        when (themePreference) {
            "blue" -> setTheme(R.style.Theme_KalendarzSemi_Blue)
            "green" -> setTheme(R.style.Theme_KalendarzSemi_Green)
            "red" -> setTheme(R.style.Theme_KalendarzSemi_Red)
        }

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_holiday_detail)

        // Odczytanie danych przekazanych z CalendarActivity
        val holidayName = intent.getStringExtra("holiday_name")
        val holidayDescription = intent.getStringExtra("holiday_description")

        // Wyświetlanie nazwy i opisu święta
        val textViewName = findViewById<TextView>(R.id.textViewHolidayName)
        val textViewDescription = findViewById<TextView>(R.id.textViewHolidayDescription)
        textViewName.text = holidayName
        textViewDescription.text = holidayDescription

        // Przycisk powrotu do CalendarActivity
        val btnBack = findViewById<Button>(R.id.btnBackToCalendar)
        btnBack.setOnClickListener {
            finish() // Wracamy do poprzedniej aktywności (CalendarActivity)
        }
    }
}
