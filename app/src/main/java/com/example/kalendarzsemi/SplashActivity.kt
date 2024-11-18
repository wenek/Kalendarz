package com.example.kalendarzsemi

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.example.kalendarzsemi.databinding.ActivitySplashBinding // Dodaj viewBinding

class SplashActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var binding: ActivitySplashBinding // ViewBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Inicjalizacja FirebaseAuth
        auth = FirebaseAuth.getInstance()

        // Załaduj animację
        val slideInAnim: Animation = AnimationUtils.loadAnimation(this, R.anim.slide_in_from_left)
        binding.appNameTextView.startAnimation(slideInAnim) // Uruchom animację na TextView

        // Sprawdzamy, czy użytkownik jest zalogowany
        Handler(Looper.getMainLooper()).postDelayed({
            val currentUser = auth.currentUser
            val intent = if (currentUser != null) {
                Intent(this, CalendarActivity::class.java) // Użytkownik zalogowany
            } else {
                Intent(this, MainActivity::class.java) // Użytkownik niezalogowany
            }
            startActivity(intent)
            finish() // Zamknięcie splash screen, aby użytkownik nie mógł wrócić
        }, 3000) // 3 sekundy na animację
    }
}
