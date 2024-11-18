package com.example.kalendarzsemi

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.PreferenceManager
import com.google.firebase.auth.FirebaseAuth
import com.example.kalendarzsemi.databinding.ActivityAboutBinding

class AboutActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var binding: ActivityAboutBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        // Ustawienie tematu
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)
        val themePreference = sharedPreferences.getString("theme_preference", "light")
        when (themePreference) {
            "light" -> setTheme(R.style.Theme_KalendarzSemi_Light)
            "dark" -> setTheme(R.style.Theme_KalendarzSemi_Dark)
            "vibrant" -> setTheme(R.style.Theme_KalendarzSemi_Vibrant)
        }

        super.onCreate(savedInstanceState)

        // Inicjalizacja bindingu
        binding = ActivityAboutBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Ustawienie opisu aplikacji
        binding.appDescription.text = getString(R.string.app_description_content)

        // Konfiguracja Toolbar
        setSupportActionBar(binding.toolbar)

        // Obsługa przycisku powrotu
        binding.btnReturn.setOnClickListener {
            finish() // Zamyka AboutActivity i wraca do poprzedniego widoku
        }
    }

    // Nadmuchanie menu z pliku XML
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return true
    }

    // Obsługa kliknięć w elementy menu
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.home -> {
                startActivity(Intent(this, ProfileActivity::class.java))
                finish()
                true
            }
            R.id.calendar -> {
                startActivity(Intent(this, CalendarActivity::class.java))
                finish()
                true
            }
            R.id.settings -> {
                startActivity(Intent(this, SettingsActivity::class.java))
                finish()
                true
            }
            R.id.logout -> {
                auth.signOut()
                startActivity(Intent(this, LoginActivity::class.java))
                finish()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}
