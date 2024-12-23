package com.example.kalendarzsemi

import android.content.Intent
import android.os.Bundle
import android.text.method.HideReturnsTransformationMethod
import android.text.method.PasswordTransformationMethod
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.PreferenceManager
import com.google.firebase.auth.FirebaseAuth
import com.example.kalendarzsemi.databinding.ActivityLoginBinding

class LoginActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private var isPasswordVisible = false
    private lateinit var binding: ActivityLoginBinding

    override fun onCreate(savedInstanceState: Bundle?) {

        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)
        val themePreference = sharedPreferences.getString("theme_preference", "light")
        when (themePreference) {
            "light" -> setTheme(R.style.Theme_KalendarzSemi_Light)
            "dark" -> setTheme(R.style.Theme_KalendarzSemi_Dark)
            "vibrant" -> setTheme(R.style.Theme_KalendarzSemi_Vibrant)
        }
        super.onCreate(savedInstanceState)

        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()

        binding.showPasswordIcon.setOnClickListener {
            if (isPasswordVisible) {
                // Hide password
                binding.etPassword.transformationMethod = PasswordTransformationMethod.getInstance()
                binding.showPasswordIcon.setImageResource(R.drawable.baseline_visibility_off_24)
            } else {
                // Show password
                binding.etPassword.transformationMethod = HideReturnsTransformationMethod.getInstance()
                binding.showPasswordIcon.setImageResource(R.drawable.baseline_visibility_24)
            }
            isPasswordVisible = !isPasswordVisible
            binding.etPassword.setSelection(binding.etPassword.text.length)
        }

        binding.btnSubmit.setOnClickListener {
            val email = binding.etEmail.text.toString()
            val password = binding.etPassword.text.toString()

            if (email.isNotEmpty() && password.isNotEmpty()) {
                loginUser(email, password)
            } else {
                Toast.makeText(this, "Wypełnij wszystkie pola", Toast.LENGTH_SHORT).show()
            }
        }

        binding.btnGoToRegister.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }

        binding.tvForgotPassword.setOnClickListener {
            startActivity(Intent(this, ResetPasswordActivity::class.java))
        }

        binding.btnReturnToMain.setOnClickListener {
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
                    Toast.makeText(this, "Logowanie nieudane: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                }
            }
    }
}
