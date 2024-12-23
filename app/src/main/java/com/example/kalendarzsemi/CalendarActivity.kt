package com.example.kalendarzsemi

import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
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
    private lateinit var binding: ActivityCalendarBinding

    // 1. Inicjalizacja i ustawienia aplikacji
    override fun onCreate(savedInstanceState: Bundle?) {
        loadTheme()
        super.onCreate(savedInstanceState)
        binding = ActivityCalendarBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupToolbar()
        currentDate = Calendar.getInstance()
        updateCalendar()
        setupButtonListeners()
    }

    private fun loadTheme() {
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)
        val themePreference = sharedPreferences.getString("theme_preference", "light")
        when (themePreference) {
            "light" -> setTheme(R.style.Theme_KalendarzSemi_Light)
            "dark" -> setTheme(R.style.Theme_KalendarzSemi_Dark)
            "vibrant" -> setTheme(R.style.Theme_KalendarzSemi_Vibrant)
        }
    }

    private fun setupToolbar() {
        val toolbar = binding.toolbar
        setSupportActionBar(toolbar)
    }

    private fun setupButtonListeners() {
        binding.btnPreviousDay.setOnClickListener { changeDay(-1) }
        binding.btnNextDay.setOnClickListener { changeDay(1) }
        binding.btnSearchDate.setOnClickListener { openDatePicker() }
        binding.btnSearchByName.setOnClickListener { showSearchByNameDialog() }
    }

    // 2. Obsługa menu
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return true
    }

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
                finish()
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

    // 3. Obsługa zdarzeń użytkownika
    private fun openDatePicker() {
        val calendar = Calendar.getInstance()
        val datePickerDialog = DatePickerDialog(
            this,
            { _, year, month, dayOfMonth ->
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

    private fun changeDay(amount: Int) {
        currentDate.add(Calendar.DAY_OF_YEAR, amount)
        updateCalendar()
    }

    private fun updateCalendar() {
        val dateFormat = SimpleDateFormat("dd.MM", Locale.getDefault())
        val formattedDate = dateFormat.format(currentDate.time)
        binding.tvDate.text = formattedDate
        binding.holidaysContainer.removeAllViews()

        val holidays = loadHolidaysForDate(formattedDate)
        holidays.forEach { holiday ->
            val holidayTextView = TextView(this).apply {
                text = holiday.first
                textSize = 20f
                setPadding(0, 16, 0, 16)
                setOnClickListener {
                    val intent = Intent(this@CalendarActivity, HolidayDetailActivity::class.java)
                    intent.putExtra("holiday_name", holiday.first)
                    intent.putExtra("holiday_description", holiday.second)
                    intent.putExtra("holiday_date", holiday.third)
                    startActivity(intent)
                }
            }
            binding.holidaysContainer.addView(holidayTextView)
        }

        if (holidays.isNotEmpty()) {
            calculateDaysUntilHoliday(holidays[0].third)
        } else {
            binding.tvDaysUntilHoliday.text = getString(R.string.no_upcoming_holidays)
        }
    }

    // 4. Obsługa świąt
    private fun calculateDaysUntilHoliday(holidayDateString: String) {
        try {
            val currentYear = Calendar.getInstance().get(Calendar.YEAR)
            val fullHolidayDateString = "$holidayDateString.$currentYear"
            val dateFormat = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
            val holidayDate = dateFormat.parse(fullHolidayDateString)
            val today = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }

            val daysDifference = ((holidayDate!!.time - today.timeInMillis) / (1000 * 60 * 60 * 24)).toInt()
            binding.tvDaysUntilHoliday.text = when {
                daysDifference > 0 -> getString(R.string.days_until_holiday, daysDifference)
                daysDifference == 0 -> getString(R.string.holiday_today)
                else -> getString(R.string.holiday_passed)
            }
        } catch (e: Exception) {
            binding.tvDaysUntilHoliday.text = getString(R.string.error_calculating_days)
        }
    }

    private fun showSearchByNameDialog() {
        val input = EditText(this)
        input.hint = getString(R.string.enter_holiday_name)

        AlertDialog.Builder(this)
            .setTitle(getString(R.string.search_holiday))
            .setView(input)
            .setPositiveButton(getString(R.string.search)) { _, _ ->
                val holidayName = input.text.toString().trim()
                searchHolidayByName(holidayName)
            }
            .setNegativeButton(getString(R.string.cancel), null)
            .show()
    }

    private fun searchHolidayByName(name: String) {
        val jsonString = loadJsonFromRaw(R.raw.holidays)
        val jsonObject = JSONObject(jsonString)

        var holidayFound: Triple<String, String, String>? = null
        jsonObject.keys().forEach { date ->
            val holidaysForDay = jsonObject.optJSONArray(date)
            holidaysForDay?.let {
                for (i in 0 until it.length()) {
                    val holiday = it.getJSONObject(i)
                    if (holiday.getString("name").equals(name, ignoreCase = true)) {
                        holidayFound = Triple(
                            holiday.getString("name"),
                            holiday.getString("description"),
                            date
                        )
                        break
                    }
                }
            }
            if (holidayFound != null) return@forEach
        }

        if (holidayFound != null) {
            val intent = Intent(this, HolidayDetailActivity::class.java)
            intent.putExtra("holiday_name", holidayFound!!.first)
            intent.putExtra("holiday_description", holidayFound!!.second)
            intent.putExtra("holiday_date", holidayFound!!.third)
            startActivity(intent)
        } else {
            AlertDialog.Builder(this)
                .setTitle(getString(R.string.holiday_not_found))
                .setMessage(getString(R.string.no_holiday_named, name))
                .setPositiveButton(getString(R.string.ok), null)
                .show()
        }
    }

    private fun loadHolidaysForDate(date: String): List<Triple<String, String, String>> {
        val holidaysList = mutableListOf<Triple<String, String, String>>()
        val jsonString = loadJsonFromRaw(R.raw.holidays)
        val jsonObject = JSONObject(jsonString)
        val holidaysForDay = jsonObject.optJSONArray(date)
        holidaysForDay?.let {
            for (i in 0 until it.length()) {
                val holiday = it.getJSONObject(i)
                holidaysList.add(Triple(holiday.getString("name"), holiday.getString("description"), date))
            }
        }
        return holidaysList
    }

    // 5. Funkcje narzędziowe
    private fun loadJsonFromRaw(resourceId: Int): String {
        val inputStream = resources.openRawResource(resourceId)
        val reader = BufferedReader(InputStreamReader(inputStream))
        return reader.use { it.readText() }
    }
}
