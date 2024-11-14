package com.example.kalendarzsemi

import android.content.Intent
import android.os.Bundle
import android.text.method.HideReturnsTransformationMethod
import android.text.method.PasswordTransformationMethod
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.PreferenceManager
import com.google.firebase.auth.FirebaseAuth

class LoginActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private var isPasswordVisible = false // Zmienna do przechowywania stanu widoczności hasła

    override fun onCreate(savedInstanceState: Bundle?) {
        // Ustawienie motywu kolorystycznego
        // Ustawienie motywu na podstawie preferencji użytkownika
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)
        val themePreference = sharedPreferences.getString("theme_preference", "blue")
        when (themePreference) {
            "blue" -> setTheme(R.style.Theme_KalendarzSemi_Light)
            "green" -> setTheme(R.style.Theme_KalendarzSemi_Dark)
            "red" -> setTheme(R.style.Theme_KalendarzSemi_Vibrant)
        }

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        // Inicjalizacja Firebase Auth
        auth = FirebaseAuth.getInstance()

        val emailField = findViewById<EditText>(R.id.etUsername)
        val passwordField = findViewById<EditText>(R.id.etPassword)
        val showPasswordIcon = findViewById<ImageView>(R.id.showPasswordIcon)
        val loginButton = findViewById<Button>(R.id.btnSubmit)

        // Logika dla przycisku pokazania/ukrycia hasła
        showPasswordIcon.setOnClickListener {
            if (isPasswordVisible) {
                // Ukryj hasło
                passwordField.transformationMethod = PasswordTransformationMethod.getInstance()
                showPasswordIcon.setImageResource(R.drawable.baseline_visibility_off_24)
            } else {
                // Pokaż hasło
                passwordField.transformationMethod = HideReturnsTransformationMethod.getInstance()
                showPasswordIcon.setImageResource(R.drawable.baseline_visibility_24)
            }
            isPasswordVisible = !isPasswordVisible // Przełącz stan widoczności
            passwordField.setSelection(passwordField.text.length) // Ustawia kursor na końcu tekstu
        }

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
            startActivity(Intent(this, RegisterActivity::class.java))
        }

        val forgotPasswordText = findViewById<TextView>(R.id.tvForgotPassword)
        forgotPasswordText.setOnClickListener {
            val intent = Intent(this, ResetPasswordActivity::class.java)
            startActivity(intent)
        }

        // Przycisk "Wróć" do MainActivity
        val btnReturnToMain = findViewById<Button>(R.id.btnReturnToMain)
        btnReturnToMain.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
            finish()
        }
    }

    private fun loginUser(email: String, password: String) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    Toast.makeText(this, "Logowanie udane", Toast.LENGTH_SHORT).show()
                    startActivity(Intent(this, CalendarActivity::class.java))
                    finish()
                } else {
                    Toast.makeText(this, "Logowanie nieudane: ${task.exception?.message}",
                        Toast.LENGTH_SHORT).show()
                }
            }
    }
}
