package com.example.kalendarzsemi

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class FavoritesActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var favoritesAdapter: FavoritesAdapter
    private lateinit var databaseReference: DatabaseReference
    private lateinit var auth: FirebaseAuth
    private val favoriteHolidays = mutableListOf<Holiday>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_favorites)

        auth = FirebaseAuth.getInstance()
        databaseReference = FirebaseDatabase.getInstance().reference

        recyclerView = findViewById(R.id.recyclerViewFavorites)
        recyclerView.layoutManager = LinearLayoutManager(this)
        favoritesAdapter = FavoritesAdapter(favoriteHolidays)
        recyclerView.adapter = favoritesAdapter

        loadFavoriteHolidays()
    }

    private fun loadFavoriteHolidays() {
        val currentUser = auth.currentUser
        if (currentUser != null) {
            val favoritesRef = databaseReference.child("users").child(currentUser.uid).child("favorites")

            favoritesRef.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    favoriteHolidays.clear()
                    for (holidaySnapshot in snapshot.children) {
                        val holiday = holidaySnapshot.getValue(Holiday::class.java)
                        holiday?.let { favoriteHolidays.add(it) }
                    }
                    favoritesAdapter.notifyDataSetChanged()
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("FavoritesActivity", "Błąd pobierania ulubionych: ${error.message}")
                    Toast.makeText(this@FavoritesActivity, "Błąd ładowania ulubionych", Toast.LENGTH_SHORT).show()
                }
            })
        } else {
            Log.e("FavoritesActivity", "Użytkownik nie jest zalogowany.")
            Toast.makeText(this, "Musisz być zalogowany, aby wyświetlić ulubione święta", Toast.LENGTH_SHORT).show()
        }
    }
}
