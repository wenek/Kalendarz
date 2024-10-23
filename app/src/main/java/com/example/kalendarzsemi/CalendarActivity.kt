package com.example.kalendarzsemi

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.PreferenceManager
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.text.SimpleDateFormat
import java.util.*

class CalendarActivity : AppCompatActivity() {

    private lateinit var currentDate: Calendar
    private lateinit var holidaysContainer: LinearLayout
    private lateinit var dateTextView: TextView

    override fun onCreate(savedInstanceState: Bundle?) {

        // Ustawienie motywu na podstawie preferencji użytkownika
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)
        val themePreference = sharedPreferences.getString("theme_preference", "blue")
        when (themePreference) {
            "blue" -> setTheme(R.style.Theme_KalendarzSemi_Blue)
            "green" -> setTheme(R.style.Theme_KalendarzSemi_Green)
            "red" -> setTheme(R.style.Theme_KalendarzSemi_Red)
        }

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_calendar)

        holidaysContainer = findViewById(R.id.holidaysContainer)
        dateTextView = findViewById(R.id.tvDate)

        currentDate = Calendar.getInstance()
        updateCalendar()

        // Obsługa przycisków do zmiany daty
        val btnPreviousDay = findViewById<Button>(R.id.btnPreviousDay)
        val btnNextDay = findViewById<Button>(R.id.btnNextDay)

        btnPreviousDay.setOnClickListener {
            changeDay(-1)
        }

        btnNextDay.setOnClickListener {
            changeDay(1)
        }
    }

    // Funkcja do zmiany dnia
    private fun changeDay(amount: Int) {
        currentDate.add(Calendar.DAY_OF_YEAR, amount)
        updateCalendar()
    }

    // Funkcja do aktualizacji kalendarza
    private fun updateCalendar() {
        val dateFormat = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
        val formattedDate = dateFormat.format(currentDate.time)

        // Aktualizacja widoku daty
        dateTextView.text = formattedDate

        // Czyszczenie listy świąt
        holidaysContainer.removeAllViews()

        val holidays = loadHolidaysForDate(formattedDate)
        holidays.forEach { holiday ->
            // Każde święto będzie klikalnym TextView
            val holidayTextView = TextView(this).apply {
                text = holiday.first // Nazwa święta
                textSize = 18f
                setPadding(0, 8, 0, 8)
                setOnClickListener {
                    // Przejście do widoku szczegółów święta
                    val intent = Intent(this@CalendarActivity, HolidayDetailActivity::class.java)
                    intent.putExtra("holiday_name", holiday.first) // Przekazanie nazwy
                    intent.putExtra("holiday_description", holiday.second) // Przekazanie opisu
                    startActivity(intent)
                }
            }
            holidaysContainer.addView(holidayTextView)
        }
    }

    // Funkcja do ładowania świąt dla danego dnia
    private fun loadHolidaysForDate(date: String): List<Pair<String, String>> {
        val holidaysList = mutableListOf<Pair<String, String>>()

        // Odczytanie pliku JSON z raw
        val jsonString = loadJsonFromRaw(R.raw.holidays)
        val jsonObject = JSONObject(jsonString)

        // Pobranie świąt dla podanej daty
        val holidaysForDay = jsonObject.optJSONArray(date)
        holidaysForDay?.let {
            for (i in 0 until it.length()) {
                val holiday = it.getJSONObject(i)
                holidaysList.add(Pair(holiday.getString("name"), holiday.getString("description")))
            }
        }
        return holidaysList
    }

    // Funkcja do ładowania pliku JSON z zasobów raw
    private fun loadJsonFromRaw(resourceId: Int): String {
        val inputStream = resources.openRawResource(resourceId)
        val reader = BufferedReader(InputStreamReader(inputStream))
        return reader.use { it.readText() }
    }
}