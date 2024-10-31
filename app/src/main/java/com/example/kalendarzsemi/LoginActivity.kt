package com.example.kalendarzsemi

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.PreferenceManager
import com.google.firebase.auth.FirebaseAuth

class LoginActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth

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

        // Inicjalizacja Firebase Auth
        auth = FirebaseAuth.getInstance()

        val emailField = findViewById<EditText>(R.id.etUsername)
        val passwordField = findViewById<EditText>(R.id.etPassword)
        val loginButton = findViewById<Button>(R.id.btnSubmit)

        // Obsługa logowania
        loginButton.setOnClickListener {
            val email = emailField.text.toString()
            val password = passwordField.text.toString()

            if (email.isNotEmpty() && password.isNotEmpty()) {
                loginUser(email, password)
            } else {
                Toast.makeText(this, "Wypełnij wszystkie pola", Toast.LENGTH_SHORT).show()
            }
        }

        val btnGoToRegister = findViewById<Button>(R.id.btnGoToRegister)

        btnGoToRegister.setOnClickListener {
            val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)
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

    // Funkcja do logowania użytkownika
    private fun loginUser(email: String, password: String) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    Toast.makeText(this, "Logowanie udane", Toast.LENGTH_SHORT).show()
                    // Przekierowanie do głównej aktywności
                    val intent = Intent(this, CalendarActivity::class.java)
                    startActivity(intent)
                    finish() // Zamykamy LoginActivity
                } else {
                    Toast.makeText(this, "Logowanie nieudane: ${task.exception?.message}",
                        Toast.LENGTH_SHORT).show()
                }
            }
    }
}
