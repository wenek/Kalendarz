package com.example.kalendarzsemi

import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
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

        // Ustawienie motywu na podstawie preferencji użytkownika
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)
        val themePreference = sharedPreferences.getString("theme_preference", "light")
        when (themePreference) {
            "light" -> setTheme(R.style.Theme_KalendarzSemi_Light)
            "dark" -> setTheme(R.style.Theme_KalendarzSemi_Dark)
            "vibrant" -> setTheme(R.style.Theme_KalendarzSemi_Vibrant)
        }

        super.onCreate(savedInstanceState)

        // Inicjalizacja ViewBinding
        binding = ActivityHolidayDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Inicjalizacja Firebase Auth i referencji do bazy danych
        auth = FirebaseAuth.getInstance()
        databaseReference = FirebaseDatabase.getInstance().reference

        // Pobieranie danych o święcie z intencji
        val holidayName = intent.getStringExtra("holiday_name")
        val holidayDescription = intent.getStringExtra("holiday_description")
        val holidayDate = intent.getStringExtra("holiday_date")

        // Wyświetlanie szczegółów święta
        binding.textViewHolidayName.text = holidayName
        binding.textViewHolidayDescription.text = holidayDescription
        binding.textViewHolidayDate.text = holidayDate // Wyświetlenie daty święta

        // Obsługa przycisku powrotu
        binding.btnBackToCalendar.setOnClickListener {
            finish()
        }

        // Obsługa przycisku dodawania do ulubionych
        binding.btnAddToFavorites.setOnClickListener {
            saveHolidayToFavorites(holidayName, holidayDescription, holidayDate)
        }
    }

    // Funkcja zapisująca święto do ulubionych
    private fun saveHolidayToFavorites(holidayName: String?, holidayDescription: String?, holidayDate: String?) {
        val currentUser = auth.currentUser
        val database = Firebase.database
        if (currentUser != null) {
            val userUid = currentUser.uid
            val favoritesRef = database.reference.child("users").child(userUid).child("favorites")

            // Sprawdzamy, czy święto już istnieje w ulubionych
            favoritesRef.get().addOnSuccessListener { dataSnapshot ->
                var alreadyExists = false

                // Iterujemy przez istniejące ulubione święta
                for (snapshot in dataSnapshot.children) {
                    val existingName = snapshot.child("name").getValue(String::class.java)
                    if (existingName == holidayName) {
                        alreadyExists = true
                        break
                    }
                }

                if (alreadyExists) {
                    // Powiadomienie użytkownika, że święto już jest w ulubionych
                    Toast.makeText(this, "To święto jest już w ulubionych", Toast.LENGTH_SHORT).show()
                } else {
                    // Tworzymy obiekt z informacjami o święcie
                    val holidayData = hashMapOf(
                        "name" to (holidayName ?: ""),
                        "description" to (holidayDescription ?: ""),
                        "date" to (holidayDate ?: "")
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
