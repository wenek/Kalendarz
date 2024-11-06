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

        // Wczytanie preferencji kolorystycznych
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)
        val themePreference = sharedPreferences.getString("theme_preference", "blue")
        when (themePreference) {
            "blue" -> setTheme(R.style.Theme_KalendarzSemi_Blue)
            "green" -> setTheme(R.style.Theme_KalendarzSemi_Green)
            "red" -> setTheme(R.style.Theme_KalendarzSemi_Red)
        }

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_holiday_detail)

        // Inicjalizacja Firebase Auth i referencji do bazy danych
        auth = FirebaseAuth.getInstance()
        databaseReference = FirebaseDatabase.getInstance().reference

        // Odczytanie danych przekazanych z CalendarActivity
        val holidayName = intent.getStringExtra("holiday_name")
        val holidayDescription = intent.getStringExtra("holiday_description")

        // Wyświetlanie nazwy i opisu święta
        val textViewName = findViewById<TextView>(R.id.textViewHolidayName)
        val textViewDescription = findViewById<TextView>(R.id.textViewHolidayDescription)
        textViewName.text = holidayName
        textViewDescription.text = holidayDescription

        // Przycisk powrotu do CalendarActivity
        val btnBack = findViewById<Button>(R.id.btnBackToCalendar)
        btnBack.setOnClickListener {
            finish() // Wracamy do poprzedniej aktywności (CalendarActivity)
        }

        // Przycisk dodania do ulubionych
        val btnAddToFavorites = findViewById<Button>(R.id.btnAddToFavorites)
        btnAddToFavorites.setOnClickListener {
            saveHolidayToFavorites(holidayName, holidayDescription)
        }
    }

    // Funkcja do zapisania święta do ulubionych
    private fun saveHolidayToFavorites(holidayName: String?, holidayDescription: String?) {
        val currentUser = auth.currentUser
        val database = Firebase.database
        if (currentUser != null) {
            val userUid = currentUser.uid
            val favoritesRef = database.reference.child("users").child(userUid).child("favorites")

            // Sprawdź, czy święto już istnieje
            favoritesRef.get().addOnSuccessListener { dataSnapshot ->
                var alreadyExists = false

                // Przeglądaj wszystkie istniejące święta użytkownika
                for (snapshot in dataSnapshot.children) {
                    val existingName = snapshot.child("name").getValue(String::class.java)
                    if (existingName == holidayName) {
                        alreadyExists = true
                        break
                    }
                }

                if (alreadyExists) {
                    // Święto już istnieje w ulubionych
                    Toast.makeText(this, "To święto jest już w ulubionych", Toast.LENGTH_SHORT).show()
                } else {
                    // Tworzymy obiekt z informacjami o święcie
                    val holidayData = hashMapOf(
                        "name" to (holidayName ?: ""),
                        "description" to (holidayDescription ?: "")
                    )

                    // Zapisujemy święto do bazy danych
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
