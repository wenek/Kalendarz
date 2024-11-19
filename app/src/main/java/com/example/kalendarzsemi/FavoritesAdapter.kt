package com.example.kalendarzsemi

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class FavoritesAdapter
    (private val favoriteHolidays: List<Holiday>) : RecyclerView.Adapter<FavoritesAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_favorite, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val holiday = favoriteHolidays[position]
        holder.nameTextView.text = holiday.name
        holder.descriptionTextView.text = holiday.description
        holder.dateTextView.text = holiday.date // Set the holiday date here
    }

    override fun getItemCount() = favoriteHolidays.size

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val nameTextView: TextView = itemView.findViewById(R.id.textViewHolidayName)
        val descriptionTextView: TextView = itemView.findViewById(R.id.textViewHolidayDescription)
        val dateTextView: TextView = itemView.findViewById(R.id.textViewHolidayDate) // New date TextView
    }
}

