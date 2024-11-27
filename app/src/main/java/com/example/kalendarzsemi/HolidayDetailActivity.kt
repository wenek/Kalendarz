package com.example.kalendarzsemi

import android.content.SharedPreferences
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.PreferenceManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.database
import com.example.kalendarzsemi.databinding.ActivityHolidayDetailBinding
import com.google.firebase.Firebase

class HolidayDetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityHolidayDetailBinding
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var databaseReference: DatabaseReference
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)
        val themePreference = sharedPreferences.getString("theme_preference", "light")
        when (themePreference) {
            "light" -> setTheme(R.style.Theme_KalendarzSemi_Light)
            "dark" -> setTheme(R.style.Theme_KalendarzSemi_Dark)
            "vibrant" -> setTheme(R.style.Theme_KalendarzSemi_Vibrant)
        }

        super.onCreate(savedInstanceState)

        binding = ActivityHolidayDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        databaseReference = FirebaseDatabase.getInstance().reference

        val holidayName = intent.getStringExtra("holiday_name")
        val holidayDescription = intent.getStringExtra("holiday_description")
        val holidayDate = intent.getStringExtra("holiday_date")

        binding.textViewHolidayName.text = holidayName
        binding.textViewHolidayDescription.text = holidayDescription
        binding.textViewHolidayDate.text = holidayDate
        binding.btnBackToCalendar.setOnClickListener {
            finish()
        }
        binding.btnAddToFavorites.setOnClickListener {
            saveHolidayToFavorites(holidayName, holidayDescription, holidayDate)
        }
    }

    private fun saveHolidayToFavorites(holidayName: String?, holidayDescription: String?, holidayDate: String?) {
        val currentUser = auth.currentUser
        val database = Firebase.database
        if (currentUser != null) {
            val userUid = currentUser.uid
            val favoritesRef = database.reference.child("users").child(userUid).child("favorites")

            favoritesRef.get().addOnSuccessListener { dataSnapshot ->
                var alreadyExists = false

                for (snapshot in dataSnapshot.children) {
                    val existingName = snapshot.child("name").getValue(String::class.java)
                    if (existingName == holidayName) {
                        alreadyExists = true
                        break
                    }
                }

                if (alreadyExists) {
                    Toast.makeText(this, "To święto jest już w ulubionych", Toast.LENGTH_SHORT).show()
                } else {
                    val holidayData = hashMapOf(
                        "name" to (holidayName ?: ""),
                        "description" to (holidayDescription ?: ""),
                        "date" to (holidayDate ?: "")
                    )

                    favoritesRef.push().setValue(holidayData)
                        .addOnSuccessListener {
                            Toast.makeText(this, "Święto dodane do ulubionych", Toast.LENGTH_SHORT).show()
                        }
                        .addOnFailureListener {
                            Toast.makeText(this, "Błąd przy dodawaniu do ulubionych", Toast.LENGTH_SHORT).show()
                        }
                }
            }.addOnFailureListener {
                Toast.makeText(this, "Błąd przy dostępie do ulubionych", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(this, "Musisz być zalogowany, aby zapisać święto do ulubionych", Toast.LENGTH_SHORT).show()
        }
    }
}
