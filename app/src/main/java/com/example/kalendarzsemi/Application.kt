package com.example.kalendarzsemi

import android.app.Application
import android.content.SharedPreferences
import androidx.preference.PreferenceManager

class MyApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        // Load the theme preference
        val sharedPreferences: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)
        val themePreference = sharedPreferences.getString("theme_preference", "light")

        when (themePreference) {
            "light" -> setTheme(R.style.Theme_KalendarzSemi_Light)
            "dark" -> setTheme(R.style.Theme_KalendarzSemi_Dark)
            "vibrant" -> setTheme(R.style.Theme_KalendarzSemi_Vibrant)
        }
    }
}
