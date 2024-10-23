package com.example.kalendarzsemi

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.Button
import android.widget.RadioButton
import android.widget.RadioGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.PreferenceManager

class SettingsActivity : AppCompatActivity() {

    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        // Odczytaj preferencje i ustaw motyw PRZED załadowaniem widoku
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)
        val themePreference = sharedPreferences.getString("theme_preference", "blue")
        when (themePreference) {
            "blue" -> setTheme(R.style.Theme_KalendarzSemi_Blue)
            "green" -> setTheme(R.style.Theme_KalendarzSemi_Green)
            "red" -> setTheme(R.style.Theme_KalendarzSemi_Red)
        }

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        // Referencje do elementów layoutu
        val radioGroupTheme = findViewById<RadioGroup>(R.id.radioGroupTheme)
        val rbBlue = findViewById<RadioButton>(R.id.rbBlue)
        val rbGreen = findViewById<RadioButton>(R.id.rbGreen)
        val rbRed = findViewById<RadioButton>(R.id.rbRed)

        // Odczytanie zapisanej kolorystyki i zaznaczenie odpowiedniego RadioButton
        val savedTheme = sharedPreferences.getString("theme_preference", "blue")
        when (savedTheme) {
            "blue" -> rbBlue.isChecked = true
            "green" -> rbGreen.isChecked = true
            "red" -> rbRed.isChecked = true
        }

        // Obsługa wyboru kolorystyki
        radioGroupTheme.setOnCheckedChangeListener { _, checkedId ->
            when (checkedId) {
                R.id.rbBlue -> saveThemePreference("blue")
                R.id.rbGreen -> saveThemePreference("green")
                R.id.rbRed -> saveThemePreference("red")
            }
            // Odśwież widok, aby zastosować nowy motyw
            recreate()
        }

        // Obsługa przycisku "Wróć"
        val btnReturn = findViewById<Button>(R.id.btnReturn)
        btnReturn.setOnClickListener {
            // Przejście z powrotem do MainActivity po zapisaniu zmian
            val intent = Intent(this, MainActivity::class.java)
            // Usuwamy wszystkie aktywności z góry stosu, aby wrócić do świeżego MainActivity
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
            finish() // Zamykamy SettingsActivity
        }
    }

    // Funkcja do zapisywania wybranej kolorystyki
    private fun saveThemePreference(theme: String) {
        val editor = sharedPreferences.edit()
        editor.putString("theme_preference", theme)
        editor.apply()
    }
}
