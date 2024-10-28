package com.example.kalendarzsemi

import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.kalendarzsemi.R

class ProfileActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        // Pobieranie referencji do elementów widoku
        val profileImage = findViewById<ImageView>(R.id.profileImage)
        val profileName = findViewById<TextView>(R.id.profileName)
        val profileEmail = findViewById<TextView>(R.id.profileEmail)
        val editProfileButton = findViewById<Button>(R.id.editProfileButton)

        // Ustawianie danych użytkownika - można pobrać z bazy danych lub preferencji użytkownika
        profileName.text = "Jan Kowalski"
        profileEmail.text = "jan.kowalski@example.com"

        // Przycisk do edycji profilu
        editProfileButton.setOnClickListener {
            // Tutaj możesz dodać przejście do widoku edycji profilu
        }
    }
}
