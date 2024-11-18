package com.example.kalendarzsemi

import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.PreferenceManager
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.database

class HolidayDetailActivity : AppCompatActivity() {

    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var databaseReference: DatabaseReference
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {

        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)
        val themePreference = sharedPreferences.getString("theme_preference", "light")
        when (themePreference) {
            "light" -> setTheme(R.style.Theme_KalendarzSemi_Light)
            "dark" -> setTheme(R.style.Theme_KalendarzSemi_Dark)
            "vibrant" -> setTheme(R.style.Theme_KalendarzSemi_Vibrant)
        }

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_holiday_detail)

// Initialize Firebase Auth and database reference
        auth = FirebaseAuth.getInstance()
        databaseReference = FirebaseDatabase.getInstance().reference

        // Retrieve holiday data from the intent
        val holidayName = intent.getStringExtra("holiday_name")
        val holidayDescription = intent.getStringExtra("holiday_description")
        val holidayDate = intent.getStringExtra("holiday_date") // Get holiday date

        // Display holiday details
        val textViewName = findViewById<TextView>(R.id.textViewHolidayName)
        val textViewDescription = findViewById<TextView>(R.id.textViewHolidayDescription)
        val textViewDate = findViewById<TextView>(R.id.textViewHolidayDate) // New TextView for date

        textViewName.text = holidayName
        textViewDescription.text = holidayDescription
        textViewDate.text = holidayDate // Display the holiday date

        // Handle back button
        val btnBack = findViewById<Button>(R.id.btnBackToCalendar)
        btnBack.setOnClickListener {
            finish()
        }

        // Handle adding to favorites
        val btnAddToFavorites = findViewById<Button>(R.id.btnAddToFavorites)
        btnAddToFavorites.setOnClickListener {
            saveHolidayToFavorites(holidayName, holidayDescription, holidayDate)
        }
    }

    // Function to save the holiday to favorites with date
    private fun saveHolidayToFavorites(holidayName: String?, holidayDescription: String?, holidayDate: String?) {
        val currentUser = auth.currentUser
        val database = Firebase.database
        if (currentUser != null) {
            val userUid = currentUser.uid
            val favoritesRef = database.reference.child("users").child(userUid).child("favorites")

            // Check if the holiday already exists
            favoritesRef.get().addOnSuccessListener { dataSnapshot ->
                var alreadyExists = false

                // Iterate through the user's existing favorites to see if this holiday is already added
                for (snapshot in dataSnapshot.children) {
                    val existingName = snapshot.child("name").getValue(String::class.java)
                    if (existingName == holidayName) {
                        alreadyExists = true
                        break
                    }
                }

                if (alreadyExists) {
                    // Notify user that the holiday is already in favorites
                    Toast.makeText(this, "To święto jest już w ulubionych", Toast.LENGTH_SHORT).show()
                } else {
                    // Create an object with holiday information, including the date
                    val holidayData = hashMapOf(
                        "name" to (holidayName ?: ""),
                        "description" to (holidayDescription ?: ""),
                        "date" to (holidayDate ?: "") // New field to store the holiday date
                    )

                    // Save the holiday to the database
                    favoritesRef.push().setValue(holidayData)
                        .addOnSuccessListener {
                            Log.d("HolidayDetailActivity", "Święto dodane do ulubionych: $holidayName")
                            Toast.makeText(this, "Święto dodane do ulubionych", Toast.LENGTH_SHORT).show()
                        }
                        .addOnFailureListener { exception ->
                            Log.e("HolidayDetailActivity", "Błąd przy dodawaniu do ulubionych: ${exception.message}", exception)
                            Toast.makeText(this, "Błąd przy dodawaniu do ulubionych", Toast.LENGTH_SHORT).show()
                        }
                }
            }.addOnFailureListener { exception ->
                Log.e("HolidayDetailActivity", "Błąd podczas sprawdzania ulubionych: ${exception.message}", exception)
                Toast.makeText(this, "Błąd przy dostępie do ulubionych", Toast.LENGTH_SHORT).show()
            }
        } else {
            Log.e("HolidayDetailActivity", "Użytkownik nie jest zalogowany, brak dostępu do ulubionych.")
            Toast.makeText(this, "Musisz być zalogowany, aby zapisać święto do ulubionych", Toast.LENGTH_SHORT).show()
        }
    }


}
