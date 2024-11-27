package com.example.kalendarzsemi

import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.kalendarzsemi.databinding.ActivityFavoritesBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class FavoritesActivity : AppCompatActivity() {

    private lateinit var binding: ActivityFavoritesBinding
    private lateinit var favoritesAdapter: FavoritesAdapter
    private lateinit var databaseReference: DatabaseReference
    private lateinit var auth: FirebaseAuth
    private val favoriteHolidays = mutableListOf<Holiday>()
    private val tag = "FavoritesActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)
        val themePreference = sharedPreferences.getString("theme_preference", "light")
        when (themePreference) {
            "light" -> setTheme(R.style.Theme_KalendarzSemi_Light)
            "dark" -> setTheme(R.style.Theme_KalendarzSemi_Dark)
            "vibrant" -> setTheme(R.style.Theme_KalendarzSemi_Vibrant)
        }
        super.onCreate(savedInstanceState)

        binding = ActivityFavoritesBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        databaseReference = FirebaseDatabase.getInstance().reference

        binding.recyclerViewFavorites.layoutManager = LinearLayoutManager(this)
        favoritesAdapter = FavoritesAdapter(favoriteHolidays)
        binding.recyclerViewFavorites.adapter = favoritesAdapter

        val toolbar = binding.toolbar
        setSupportActionBar(toolbar)

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
                    Log.e(tag, "Error loading favorites: ${error.message}")
                    Toast.makeText(this@FavoritesActivity, "Error loading favorites", Toast.LENGTH_SHORT).show()
                }
            })
        } else {
            Log.e(tag, "User is not logged in.")
            Toast.makeText(this, "You must be logged in to view favorite holidays.", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_favorites_sort, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.sort_by_date -> {
                sortFavoritesByDate()
                true
            }
            R.id.sort_alphabetically -> {
                sortFavoritesAlphabetically()
                true
            }
            R.id.sort_by_addition -> {
                sortFavoritesByAdditionOrder()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun sortFavoritesByDate() {
        favoriteHolidays.sortBy { it.date }
        favoritesAdapter.notifyDataSetChanged()
        Toast.makeText(this, "Sorted by date", Toast.LENGTH_SHORT).show()
    }

    private fun sortFavoritesAlphabetically() {
        favoriteHolidays.sortBy { it.name }
        favoritesAdapter.notifyDataSetChanged()
        Toast.makeText(this, "Sorted alphabetically", Toast.LENGTH_SHORT).show()
    }

    private fun sortFavoritesByAdditionOrder() {
        loadFavoriteHolidays()
        Toast.makeText(this, "Sorted by addition order", Toast.LENGTH_SHORT).show()
    }
}
