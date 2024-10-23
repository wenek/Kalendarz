package com.example.kalendarzsemi

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.PreferenceManager

class LoginActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        // Sprawdzamy preferencje kolorystyczne przed ustawieniem widoku
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)
        val themePreference = sharedPreferences.getString("theme_preference", "blue")
        when (themePreference) {
            "blue" -> setTheme(R.style.Theme_KalendarzSemi_Blue)
            "green" -> setTheme(R.style.Theme_KalendarzSemi_Green)
            "red" -> setTheme(R.style.Theme_KalendarzSemi_Red)
        }

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        // Obsługa przycisku logowania (tylko jako placeholder, można dodać logikę później)
        val btnSubmit = findViewById<Button>(R.id.btnSubmit)
        btnSubmit.setOnClickListener {
            // Placeholder na przyszłą logikę logowania
        }

        // Obsługa przycisku "Wróć"
        val btnReturnToMain = findViewById<Button>(R.id.btnReturnToMain)
        btnReturnToMain.setOnClickListener {
            // Powrót do MainActivity
            val intent = Intent(this, MainActivity::class.java)
            // Czyścimy stos, aby wrócić do głównego ekranu
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
            finish() // Zamykamy LoginActivity
        }
    }
}
