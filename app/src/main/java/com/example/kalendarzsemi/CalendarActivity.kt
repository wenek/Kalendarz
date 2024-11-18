package com.example.kalendarzsemi

import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.PreferenceManager
import com.google.firebase.auth.FirebaseAuth
import com.example.kalendarzsemi.databinding.ActivityCalendarBinding
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.text.SimpleDateFormat
import java.util.*

class CalendarActivity : AppCompatActivity() {

    private lateinit var currentDate: Calendar
    private lateinit var binding: ActivityCalendarBinding // Inicjalizacja zmiennej do ViewBinding

    override fun onCreate(savedInstanceState: Bundle?) {

        // Wczytanie preferencji motywu
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)
        val themePreference = sharedPreferences.getString("theme_preference", "light")
        when (themePreference) {
            "light" -> setTheme(R.style.Theme_KalendarzSemi_Light)
            "dark" -> setTheme(R.style.Theme_KalendarzSemi_Dark)
            "vibrant" -> setTheme(R.style.Theme_KalendarzSemi_Vibrant)
        }

        super.onCreate(savedInstanceState)
        binding = ActivityCalendarBinding.inflate(layoutInflater) // Tworzenie obiektu ViewBinding
        setContentView(binding.root) // Ustawienie widoku

        // Konfiguracja Toolbar
        val toolbar = binding.toolbar // Zamiast findViewById, korzystamy z viewBinding
        setSupportActionBar(toolbar)

        currentDate = Calendar.getInstance()
        updateCalendar()

        // Obsługa przycisków do zmiany daty
        binding.btnPreviousDay.setOnClickListener { // Zamiast findViewById
            changeDay(-1)
        }

        binding.btnNextDay.setOnClickListener { // Zamiast findViewById
            changeDay(1)
        }

        binding.btnSearchDate.setOnClickListener {
            openDatePicker()
        }
    }

    // Nadmuchanie menu z pliku XML (main_menu.xml)
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return true
    }

    // Obsługa kliknięć elementów menu
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.home -> {
                val intent = Intent(this, ProfileActivity::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(intent)
                true
            }
            R.id.calendar -> {
                val intent = Intent(this, CalendarActivity::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(intent)
                true
            }
            R.id.settings -> {
                val intent = Intent(this, SettingsActivity::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(intent)
                true
            }
            R.id.about -> {
                val intent = Intent(this, AboutActivity::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(intent)
                true
            }
            R.id.exit -> {
                finish() // Obsługa kliknięcia "Exit"
                true
            }
            R.id.logout -> {
                val auth = FirebaseAuth.getInstance()
                auth.signOut()

                val intent = Intent(this, LoginActivity::class.java)
                startActivity(intent)
                finish()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    // Funkcja otwierająca dialog do wyboru daty
    private fun openDatePicker() {
        val calendar = Calendar.getInstance()
        val datePickerDialog = DatePickerDialog(
            this,
            { _, year, month, dayOfMonth ->
                // Zaktualizuj kalendarz po wybraniu daty
                val selectedDate = Calendar.getInstance()
                selectedDate.set(year, month, dayOfMonth)
                currentDate = selectedDate
                updateCalendar()
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )

        datePickerDialog.show()
    }

    // Funkcja do zmiany dnia
    private fun changeDay(amount: Int) {
        currentDate.add(Calendar.DAY_OF_YEAR, amount)
        updateCalendar()
    }

    private fun updateCalendar() {
        val dateFormat = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
        val formattedDate = dateFormat.format(currentDate.time)

        // Update date view
        binding.tvDate.text = formattedDate // Zamiast findViewById

        // Clear previous holiday views
        binding.holidaysContainer.removeAllViews() // Zamiast findViewById

        // Load holidays for the selected date
        val holidays = loadHolidaysForDate(formattedDate)
        holidays.forEach { holiday ->
            val holidayTextView = TextView(this).apply {
                text = holiday.first // Holiday name
                textSize = 18f
                setPadding(0, 8, 0, 8)
                setOnClickListener {
                    // Pass holiday details and date to HolidayDetailActivity
                    val intent = Intent(this@CalendarActivity, HolidayDetailActivity::class.java)
                    intent.putExtra("holiday_name", holiday.first)       // Pass the name
                    intent.putExtra("holiday_description", holiday.second) // Pass the description
                    intent.putExtra("holiday_date", holiday.third)         // Pass the date
                    startActivity(intent)
                }
            }
            binding.holidaysContainer.addView(holidayTextView)
        }
    }

    private fun loadHolidaysForDate(date: String): List<Triple<String, String, String>> {
        val holidaysList = mutableListOf<Triple<String, String, String>>()

        // Load JSON file from resources
        val jsonString = loadJsonFromRaw(R.raw.holidays)
        val jsonObject = JSONObject(jsonString)

        // Retrieve holidays for the given date
        val holidaysForDay = jsonObject.optJSONArray(date)
        holidaysForDay?.let {
            for (i in 0 until it.length()) {
                val holiday = it.getJSONObject(i)
                holidaysList.add(Triple(holiday.getString("name"), holiday.getString("description"), date))
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
