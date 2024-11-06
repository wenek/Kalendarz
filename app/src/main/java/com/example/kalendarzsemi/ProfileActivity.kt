package com.example.kalendarzsemi

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class ProfileActivity : AppCompatActivity() {

    private lateinit var databaseReference: DatabaseReference
    private lateinit var auth: FirebaseAuth
    private lateinit var nameTextView: TextView
    private lateinit var emailTextView: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        setContentView(R.layout.activity_profile)

        // Konfiguracja Firebase
        auth = FirebaseAuth.getInstance()
        databaseReference = FirebaseDatabase.getInstance().reference

        // Konfiguracja Toolbar
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)

        // Znalezienie widoków
        nameTextView = findViewById(R.id.profileName)
        emailTextView = findViewById(R.id.profileEmail)

        // Załaduj dane użytkownika
        loadUserProfile()

        val btnShowFavorites = findViewById<Button>(R.id.showFavoritesButton)
        btnShowFavorites.setOnClickListener {
            val intent = Intent(this, FavoritesActivity::class.java)
            startActivity(intent)
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
                    nameTextView.text = name
                    emailTextView.text = email
                } else {
                    Log.e("UserProfile", "Dane użytkownika nie istnieją")
                }
            }.addOnFailureListener { exception ->
                Log.e("UserProfile", "Błąd odczytu danych: ${exception.message}")
            }
        } else {
            Log.e("UserProfile", "Użytkownik nie jest zalogowany")
        }
    }

}
