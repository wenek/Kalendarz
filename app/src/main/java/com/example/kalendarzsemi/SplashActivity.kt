package com.example.kalendarzsemi

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class SplashActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        // Powiadomienie Toast, aby sprawdzić, czy SplashActivity działa
        Toast.makeText(this, "Splash screen start!", Toast.LENGTH_SHORT).show()

        // Przekierowanie do MainActivity po 3 sekundach
        Handler(Looper.getMainLooper()).postDelayed({
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish() // Zamknięcie ekranu splash, aby użytkownik nie mógł wrócić do niego
        }, 3000)
    }
}
