package com.example.kalendarzsemi

import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
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
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)
        val themePreference = sharedPreferences.getString("theme_preference", "light")
        when (themePreference) {
            "light" -> setTheme(R.style.Theme_KalendarzSemi_Light)
            "dark" -> setTheme(R.style.Theme_KalendarzSemi_Dark)
            "vibrant" -> setTheme(R.style.Theme_KalendarzSemi_Vibrant)
        }
        super.onCreate(savedInstanceState)

        binding = ActivityProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        databaseReference = FirebaseDatabase.getInstance().reference
        loadUserProfile()
        setSupportActionBar(binding.toolbar)

        binding.showFavoritesButton.setOnClickListener {
            startActivity(Intent(this, FavoritesActivity::class.java))
        }
        binding.profileImage.setOnClickListener {
            startActivity(Intent(this, ProfileActivity::class.java))
        }
        binding.logoutButton.setOnClickListener {
            val auth = FirebaseAuth.getInstance()
            auth.signOut()

            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish()
        }
        binding.editBioButton.setOnClickListener {
            val currentBio = binding.profileBio.text.toString()
            val editText = EditText(this)
            editText.setText(currentBio)
            editText.inputType = InputType.TYPE_CLASS_TEXT
            AlertDialog.Builder(this)
                .setTitle("Edytuj opis")
                .setView(editText)
                .setPositiveButton("Zapisz") { _, _ ->
                    val newBio = editText.text.toString()
                    saveBioToDatabase(newBio)
                }
                .setNegativeButton("Anuluj", null)
                .show()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.home -> {
                val intent = Intent(this, ProfileActivity::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(intent)
                true
            }
            R.id.calendar -> {
                val intent = Intent(this, CalendarActivity::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(intent)
                true
            }
            R.id.settings -> {
                val intent = Intent(this, SettingsActivity::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(intent)
                true
            }
            R.id.about -> {
                val intent = Intent(this, AboutActivity::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(intent)
                true
            }
            R.id.exit -> {
                finish()
                true
            }
            R.id.logout -> {
                val auth = FirebaseAuth.getInstance()
                auth.signOut()

                val intent = Intent(this, LoginActivity::class.java)
                startActivity(intent)
                finish()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun saveBioToDatabase(bio: String) {
        val userUid = auth.currentUser?.uid ?: return
        databaseReference.child("users").child(userUid).child("bio").setValue(bio)
            .addOnSuccessListener {
                Toast.makeText(this, "Bio updated successfully!", Toast.LENGTH_SHORT).show()
                loadUserProfile()

                binding.profileBio.visibility = View.VISIBLE
                binding.editBioButton.text = getString(R.string.edit_bio)
            }
            .addOnFailureListener { exception ->
                Toast.makeText(this, "Failed to update bio: ${exception.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun loadUserProfile() {
        val currentUser = auth.currentUser
        if (currentUser != null) {
            val userUid = currentUser.uid
            val userRef = databaseReference.child("users").child(userUid)

            userRef.get().addOnSuccessListener { snapshot ->
                if (snapshot.exists()) {
                    val email = snapshot.child("email").value.toString()
                    val name = snapshot.child("name").value.toString()
                    val bio = snapshot.child("bio").value.toString()

                    binding.profileName.text = name
                    binding.profileEmail.text = email
                    binding.profileBio.text = bio
                    binding.profileBio.visibility = View.VISIBLE

                    val favoritesRef = snapshot.child("favorites")
                    val favoritesCount = favoritesRef.childrenCount
                    binding.profileStatsAccountCreate.text = getString(R.string.profile_stats, favoritesCount)

                    val accountCreationDate = snapshot.child("accountCreationDate").value.toString()
                    binding.profileStatsHolidaysCount.text = getString(R.string.profile_creation_date, accountCreationDate)
                }
            }.addOnFailureListener { exception ->
                Log.e("UserProfile", "Error reading data: ${exception.message}")
            }
        }
    }
}
