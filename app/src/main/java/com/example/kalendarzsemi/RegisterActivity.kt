package com.example.kalendarzsemi

import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.PreferenceManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class RegisterActivity : AppCompatActivity() {

    // Zmienne do obsługi Firebase Authentication i Database
    private lateinit var auth: FirebaseAuth
    private lateinit var database: DatabaseReference

    // Flagi dla pól hasła
    private var isPasswordVisible = false
    private var isConfirmPasswordVisible = false

    override fun onCreate(savedInstanceState: Bundle?) {
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)
        val themePreference = sharedPreferences.getString("theme_preference", "light")
        when (themePreference) {
            "light" -> setTheme(R.style.Theme_KalendarzSemi_Light)
            "dark" -> setTheme(R.style.Theme_KalendarzSemi_Dark)
            "vibrant" -> setTheme(R.style.Theme_KalendarzSemi_Vibrant)
        }

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        // Inicjalizacja Firebase Auth i Database
        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance().reference

        val nameField = findViewById<EditText>(R.id.etName)
        val emailField = findViewById<EditText>(R.id.etEmail)
        val passwordField = findViewById<EditText>(R.id.etPassword)
        val passwordConfirmField = findViewById<EditText>(R.id.etConfirmPassword)
        val passwordToggle = findViewById<ImageView>(R.id.showPasswordIcon)
        val confirmPasswordToggle = findViewById<ImageView>(R.id.showConfirmPasswordIcon)
        val registerButton = findViewById<Button>(R.id.btnRegister)
        val returnToLoginButton = findViewById<Button>(R.id.btnReturnToLogin)

        // Obsługa przycisku przełączania widoczności hasła
        passwordToggle.setOnClickListener {
            isPasswordVisible = !isPasswordVisible
            togglePasswordVisibility(passwordField, isPasswordVisible, passwordToggle)
        }

        // Obsługa przycisku przełączania widoczności potwierdzenia hasła
        confirmPasswordToggle.setOnClickListener {
            isConfirmPasswordVisible = !isConfirmPasswordVisible
            togglePasswordVisibility(passwordConfirmField, isConfirmPasswordVisible, confirmPasswordToggle)
        }

        // Obsługa przycisku rejestracji
        registerButton.setOnClickListener {
            val name = nameField.text.toString()
            val email = emailField.text.toString()
            val password = passwordField.text.toString()
            val passwordConfirm = passwordConfirmField.text.toString()

            if (email.isNotEmpty() && password.isNotEmpty() && passwordConfirm.isNotEmpty()) {
                if (password != passwordConfirm) {
                    Toast.makeText(this, "Hasła nie są takie same", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }
                registerUser(name, email, password)
            } else {
                Toast.makeText(this, "Wypełnij wszystkie pola", Toast.LENGTH_SHORT).show()
            }
        }

        // Powrót do ekranu logowania
        returnToLoginButton.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish() // Zamyka ekran rejestracji
        }
    }

    // Funkcja do przełączania widoczności hasła
    private fun togglePasswordVisibility(editText: EditText, isVisible: Boolean, toggleIcon: ImageView) {
        if (isVisible) {
            editText.inputType = InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
            toggleIcon.setImageResource(R.drawable.baseline_visibility_off_24) // Ikona 'ukryj hasło'
        } else {
            editText.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
            toggleIcon.setImageResource(R.drawable.baseline_visibility_24) // Ikona 'pokaż hasło'
        }
        // Ustawienie kursora na końcu tekstu
        editText.setSelection(editText.text.length)
    }

    // Funkcja do rejestracji użytkownika za pomocą Firebase
    private fun registerUser(name: String, email: String, password: String) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    val userId = auth.currentUser?.uid ?: ""
                    saveUserToDatabase(userId, name, email)

                    Toast.makeText(this, "Rejestracja udana!", Toast.LENGTH_SHORT).show()
                    val intent = Intent(this, LoginActivity::class.java)
                    startActivity(intent)
                    finish() // Zamyka ekran rejestracji
                } else {
                    Toast.makeText(
                        this,
                        "Rejestracja nieudana: ${task.exception?.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
    }

    // Funkcja do zapisywania użytkownika w Firebase Realtime Database
    private fun saveUserToDatabase(userId: String, name: String, email: String) {
        val user = User(name, email)
        database.child("users").child(userId).setValue(user)
            .addOnSuccessListener {
                Toast.makeText(this, "Użytkownik zapisany w bazie danych", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Błąd zapisu danych: ${it.message}", Toast.LENGTH_SHORT).show()
                Log.e("RegisterActivity", "Database Error: ${it.message}")
            }
    }
}

// Klasa reprezentująca dane użytkownika
data class User(val name: String, val email: String)
