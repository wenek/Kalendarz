package com.example.kalendarzsemi

import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.PreferenceManager
import com.example.kalendarzsemi.databinding.ActivityRegisterBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import java.text.SimpleDateFormat
import java.util.*

class RegisterActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var database: DatabaseReference

    private var isPasswordVisible = false
    private var isConfirmPasswordVisible = false

    private lateinit var binding: ActivityRegisterBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)
        val themePreference = sharedPreferences.getString("theme_preference", "light")
        when (themePreference) {
            "light" -> setTheme(R.style.Theme_KalendarzSemi_Light)
            "dark" -> setTheme(R.style.Theme_KalendarzSemi_Dark)
            "vibrant" -> setTheme(R.style.Theme_KalendarzSemi_Vibrant)
        }

        super.onCreate(savedInstanceState)

        // Initialize ViewBinding
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize Firebase Auth and Database
        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance().reference

        // Password visibility toggles
        binding.showPasswordIcon.setOnClickListener {
            isPasswordVisible = !isPasswordVisible
            togglePasswordVisibility(binding.etPassword, isPasswordVisible, binding.showPasswordIcon)
        }

        binding.showConfirmPasswordIcon.setOnClickListener {
            isConfirmPasswordVisible = !isConfirmPasswordVisible
            togglePasswordVisibility(binding.etConfirmPassword, isConfirmPasswordVisible, binding.showConfirmPasswordIcon)
        }

        // Register button click listener
        binding.btnRegister.setOnClickListener {
            val name = binding.etName.text.toString()
            val email = binding.etEmail.text.toString()
            val password = binding.etPassword.text.toString()
            val passwordConfirm = binding.etConfirmPassword.text.toString()

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

        // Return to Login screen
        binding.btnReturnToLogin.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish() // Close Register Activity
        }
    }

    // Toggle password visibility
    private fun togglePasswordVisibility(editText: EditText, isVisible: Boolean, toggleIcon: ImageView) {
        if (isVisible) {
            editText.inputType = InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
            toggleIcon.setImageResource(R.drawable.baseline_visibility_off_24) // Hide icon
        } else {
            editText.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
            toggleIcon.setImageResource(R.drawable.baseline_visibility_24) // Show icon
        }
        // Move cursor to the end of the text
        editText.setSelection(editText.text.length)
    }

    // Register user using Firebase
    private fun registerUser(name: String, email: String, password: String) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    val userId = auth.currentUser?.uid ?: ""
                    saveUserToDatabase(userId, name, email)

                    Toast.makeText(this, "Rejestracja udana!", Toast.LENGTH_SHORT).show()
                    val intent = Intent(this, LoginActivity::class.java)
                    startActivity(intent)
                    finish() // Close Register Activity
                } else {
                    Toast.makeText(this, "Rejestracja nieudana: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                }
            }
    }

    // Save user data to Firebase Realtime Database
    private fun saveUserToDatabase(userId: String, name: String, email: String) {
        // Pobranie aktualnej daty utworzenia konta
        val creationDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())

        // Dane użytkownika
        val user = User(name, email, creationDate)

        // Zapis do Firebase
        database.child("users").child(userId).setValue(user)
            .addOnSuccessListener {
                Toast.makeText(this, "Użytkownik zapisany w bazie danych", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Błąd zapisu danych: ${it.message}", Toast.LENGTH_SHORT).show()
            }
    }
}

// Data class for User
data class User(
    val name: String,
    val email: String,
    val accountCreationDate: String
)
