package com.example.kalendarzsemi

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth

class SplashActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        // Inicjalizacja FirebaseAuth
        auth = FirebaseAuth.getInstance()

        // Powiadomienie Toast, aby sprawdzić, czy SplashActivity działa
        Toast.makeText(this, "Splash screen start!", Toast.LENGTH_SHORT).show()

        // Sprawdzamy, czy użytkownik jest zalogowany
        Handler(Looper.getMainLooper()).postDelayed({
            val currentUser = auth.currentUser
            if (currentUser != null) {
                // Użytkownik jest zalogowany, przekierowanie do CalendarActivity
                val intent = Intent(this, CalendarActivity::class.java)
                startActivity(intent)
            } else {
                // Użytkownik nie jest zalogowany, przekierowanie do MainActivity
                val intent = Intent(this, MainActivity::class.java)
                startActivity(intent)
            }
            // Zamknięcie ekranu splash, aby użytkownik nie mógł wrócić do niego
            finish()
        }, 3000)
    }
}
