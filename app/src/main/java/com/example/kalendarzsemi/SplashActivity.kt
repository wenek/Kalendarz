package com.example.kalendarzsemi

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.example.kalendarzsemi.databinding.ActivitySplashBinding

class SplashActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var binding: ActivitySplashBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()

        val slideInAnim: Animation = AnimationUtils.loadAnimation(this, R.anim.slide_in_from_left)
        binding.appNameTextView.startAnimation(slideInAnim)

        val fadeInAnim: Animation = AnimationUtils.loadAnimation(this, R.anim.fade_in)
        binding.logoImageView.startAnimation(fadeInAnim)

        Handler(Looper.getMainLooper()).postDelayed({
            val currentUser = auth.currentUser
            val intent = if (currentUser != null) {
                Intent(this, CalendarActivity::class.java)
            } else {
                Intent(this, MainActivity::class.java)
            }
            startActivity(intent)
            finish()
        }, 3000)
    }
}
