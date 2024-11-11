package com.example.kalendarzsemi

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth

class ResetPasswordActivity : AppCompatActivity() {

    private lateinit var emailField: EditText
    private lateinit var resetPasswordButton: Button
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_reset_password)

        // Inicjalizacja pól
        emailField = findViewById(R.id.etEmail)
        resetPasswordButton = findViewById(R.id.btnResetPassword)
        auth = FirebaseAuth.getInstance()

        // Obsługa kliknięcia przycisku resetowania hasła
        resetPasswordButton.setOnClickListener {
            val email = emailField.text.toString()

            if (email.isNotEmpty()) {
                resetPassword(email)
            } else {
                Toast.makeText(this, "Podaj adres e-mail", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun resetPassword(email: String) {
        auth.sendPasswordResetEmail(email)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Toast.makeText(this, "E-mail resetujący hasło został wysłany.", Toast.LENGTH_SHORT).show()
                    finish() // Opcjonalnie: zamknięcie aktywności po wysłaniu e-maila
                } else {
                    Toast.makeText(this, "Błąd: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                }
            }
    }
}
