package com.example.kalendarzsemi

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.PreferenceManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.example.kalendarzsemi.databinding.ActivityProfileBinding

class ProfileActivity : AppCompatActivity() {

    private lateinit var databaseReference: DatabaseReference
    private lateinit var auth: FirebaseAuth
    private lateinit var binding: ActivityProfileBinding

    override fun onCreate(savedInstanceState: Bundle?) {

        // Set the theme based on user preferences
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)
        val themePreference = sharedPreferences.getString("theme_preference", "light")
        when (themePreference) {
            "light" -> setTheme(R.style.Theme_KalendarzSemi_Light)
            "dark" -> setTheme(R.style.Theme_KalendarzSemi_Dark)
            "vibrant" -> setTheme(R.style.Theme_KalendarzSemi_Vibrant)
        }

        super.onCreate(savedInstanceState)

        // Initialize ViewBinding
        binding = ActivityProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize Firebase
        auth = FirebaseAuth.getInstance()
        databaseReference = FirebaseDatabase.getInstance().reference

        // Set up the Toolbar
        setSupportActionBar(binding.toolbar)

        // Load user profile data
        loadUserProfile()

        // Button to navigate to Favorites Activity
        binding.showFavoritesButton.setOnClickListener {
            val intent = Intent(this, FavoritesActivity::class.java)
            startActivity(intent)
        }

    }

    // Inflate the menu from XML
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return true
    }

    // Handle menu item clicks
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
            R.id.about -> {
                val intent = Intent(this, AboutActivity::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(intent)
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

    // Load user profile data from Firebase
    private fun loadUserProfile() {
        val currentUser = auth.currentUser
        if (currentUser != null) {
            val userUid = currentUser.uid
            val userRef = databaseReference.child("users").child(userUid)

            Log.e("UserProfile", "User UID: $userUid")

            userRef.get().addOnSuccessListener { snapshot ->
                if (snapshot.exists()) {
                    val email = snapshot.child("email").value.toString()
                    val name = snapshot.child("name").value.toString()
                    Log.e("UserProfile", "Name: $name, Email: $email")
                    binding.profileName.text = name
                    binding.profileEmail.text = email
                } else {
                    Log.e("UserProfile", "User data does not exist")
                }
            }.addOnFailureListener { exception ->
                Log.e("UserProfile", "Error reading data: ${exception.message}")
            }
        } else {
            Log.e("UserProfile", "User is not logged in")
        }
    }
}
