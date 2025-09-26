package com.homeapps.recipeai

import android.os.Bundle
import android.view.View
import android.widget.CalendarView
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat

class FavoritesActivity : AppCompatActivity() {

    private lateinit var calendarView: CalendarView
    private lateinit var listContainer: LinearLayout
    private lateinit var toggleButton: ImageView
    private lateinit var emptyStateText: TextView
    private val favorites = mutableListOf<String>()
    private var isCalendarView = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_favorites)

        val backButton = findViewById<ImageView>(R.id.iv_back)
        calendarView = findViewById(R.id.calendar_view)
        listContainer = findViewById(R.id.ll_favorites_container)
        toggleButton = findViewById(R.id.iv_toggle_view)
        emptyStateText = findViewById(R.id.tv_empty_state)

        backButton.setOnClickListener {
            finish()
        }

        toggleButton.setOnClickListener {
            toggleView()
        }

        calendarView.setOnDateChangeListener { _, year, month, dayOfMonth ->
            val selectedDate = "${dayOfMonth}/${month + 1}/${year}"
            Toast.makeText(this, "Selected: $selectedDate", Toast.LENGTH_SHORT).show()
        }

        loadFavorites()
        updateView()
    }

    private fun toggleView() {
        isCalendarView = !isCalendarView
        updateView()
    }

    private fun updateView() {
        if (isCalendarView) {
            // Show calendar view
            calendarView.visibility = View.VISIBLE
            listContainer.visibility = View.GONE
            emptyStateText.visibility = View.GONE
            toggleButton.setImageResource(R.drawable.ic_list)
            toggleButton.contentDescription = "Switch to list view"
        } else {
            // Show list view
            calendarView.visibility = View.GONE
            if (favorites.isEmpty()) {
                listContainer.visibility = View.GONE
                emptyStateText.visibility = View.VISIBLE
            } else {
                listContainer.visibility = View.VISIBLE
                emptyStateText.visibility = View.GONE
                displayFavorites()
            }
            toggleButton.setImageResource(R.drawable.ic_calendar)
            toggleButton.contentDescription = "Switch to calendar view"
        }
    }

    private fun removeFavorite(favorite: String) {
        favorites.remove(favorite)
        saveFavorites()
        updateView()
    }

    private fun displayFavorites() {
        listContainer.removeAllViews()

        for (favorite in favorites) {
            val favoriteView = layoutInflater.inflate(R.layout.item_favorite, listContainer, false)
            val favoriteTitle = favoriteView.findViewById<TextView>(R.id.tv_favorite_title)
            val favoriteDescription = favoriteView.findViewById<TextView>(R.id.tv_favorite_description)
            val removeButton = favoriteView.findViewById<ImageView>(R.id.iv_remove_favorite)

            favoriteTitle.text = favorite
            favoriteDescription.text = "Added to favorites"
            removeButton.setOnClickListener {
                removeFavorite(favorite)
            }

            listContainer.addView(favoriteView)
        }
    }

    private fun loadFavorites() {
        val sharedPref = getSharedPreferences("favorites_prefs", MODE_PRIVATE)
        val favoritesString = sharedPref.getString("favorites", "")
        if (!favoritesString.isNullOrEmpty()) {
            favorites.clear()
            favorites.addAll(favoritesString.split(","))
        }
    }

    private fun saveFavorites() {
        val sharedPref = getSharedPreferences("favorites_prefs", MODE_PRIVATE)
        with(sharedPref.edit()) {
            putString("favorites", favorites.joinToString(","))
            apply()
        }
    }
}