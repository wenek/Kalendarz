package com.example.kalendarzsemi

import android.os.Bundle
import android.util.Log
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.auth.FirebaseAuth
import android.widget.Toast
import com.google.firebase.database.*

class ProfileActivity : AppCompatActivity() {

    private lateinit var databaseReference: DatabaseReference
    private lateinit var auth: FirebaseAuth

    private lateinit var nameTextView: TextView
    private lateinit var emailTextView: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        // Inicjalizacja Firebase Auth i referencji do bazy danych
        auth = FirebaseAuth.getInstance()
        databaseReference = FirebaseDatabase.getInstance().reference

        // Znajdź widoki
        nameTextView = findViewById(R.id.profileName)
        emailTextView = findViewById(R.id.profileEmail)

        // Załaduj dane użytkownika
        loadUserProfile()
    }

    private fun loadUserProfile() {
        val currentUser = auth.currentUser
        if (currentUser != null) {
            val userUid = currentUser.uid
            val userRef = databaseReference.child("Users").child(userUid)

            // Logowanie UID użytkownika
            Log.e("UserProfile", "User UID: $userUid")

            // Pobieranie informacji z bazy danych
            userRef.get().addOnSuccessListener { snapshot ->
                if (snapshot.exists()) {
                    val name = snapshot.child("name").value.toString()
                    val email = snapshot.child("email").value.toString()

                    // Logowanie pobranych danych
                    Log.e("UserProfile", "Name: $name, Email: $email")

                    // Wyświetlanie pobranych informacji w widoku
                    findViewById<TextView>(R.id.profileName).text = name
                    findViewById<TextView>(R.id.profileEmail).text = email
                } else {
                    // Logowanie braku danych w bazie
                    Log.e("UserProfile", "Dane użytkownika nie istnieją")
                    Toast.makeText(this, "Dane użytkownika nie istnieją", Toast.LENGTH_SHORT).show()
                }
            }.addOnFailureListener { exception ->
                // Logowanie błędu podczas pobierania danych
                Log.e("UserProfile", "Błąd odczytu danych: ${exception.message}")
                Toast.makeText(this, "Błąd odczytu danych: ${exception.message}", Toast.LENGTH_SHORT).show()
            }
        } else {
            // Logowanie braku zalogowanego użytkownika
            Log.e("UserProfile", "Użytkownik nie jest zalogowany")
            Toast.makeText(this, "Użytkownik nie jest zalogowany", Toast.LENGTH_SHORT).show()
        }
    }



}
