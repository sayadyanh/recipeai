package com.homeapps.recipeai

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.CalendarView
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.homeapps.recipeai.adapters.RecipeAdapter
import com.homeapps.recipeai.database.FavoritesDbHelper
import com.homeapps.recipeai.models.Recipe
import java.text.SimpleDateFormat
import java.util.*

class FavoritesActivity : AppCompatActivity() {

    private lateinit var calendarView: CalendarView
    private lateinit var selectedDateRecyclerView: RecyclerView
    private lateinit var toggleButton: ImageView
    private lateinit var selectedDateTitle: TextView
    private lateinit var emptyStateText: TextView

    private lateinit var favoritesDbHelper: FavoritesDbHelper
    private lateinit var selectedDateAdapter: RecipeAdapter
    private lateinit var allFavoritesAdapter: RecipeAdapter

    private var isCalendarView = true
    private var selectedDate = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_favorites)

        favoritesDbHelper = FavoritesDbHelper(this)
        selectedDate = getCurrentDate()

        initViews()
        setupAdapters()
        setupListeners()
        updateView()
    }

    private fun initViews() {
        calendarView = findViewById(R.id.calendar_view)
        selectedDateRecyclerView = findViewById(R.id.rv_selected_date_recipes)
        toggleButton = findViewById(R.id.iv_toggle_view)
        selectedDateTitle = findViewById(R.id.tv_selected_date)
        emptyStateText = findViewById(R.id.tv_empty_state)

        findViewById<ImageView>(R.id.iv_back).setOnClickListener {
            finish()
        }
    }

    private fun setupAdapters() {
        selectedDateAdapter = RecipeAdapter { recipe ->
            openRecipeDetail(recipe)
        }

        allFavoritesAdapter = RecipeAdapter { recipe ->
            openRecipeDetail(recipe)
        }

        selectedDateRecyclerView.apply {
            layoutManager = LinearLayoutManager(this@FavoritesActivity)
            adapter = selectedDateAdapter
        }
    }

    private fun setupListeners() {
        toggleButton.setOnClickListener {
            toggleView()
        }

        calendarView.setOnDateChangeListener { _, year, month, dayOfMonth ->
            val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val calendar = Calendar.getInstance()
            calendar.set(year, month, dayOfMonth)
            selectedDate = dateFormat.format(calendar.time)

            updateSelectedDateTitle(year, month, dayOfMonth)
            loadRecipesForSelectedDate()
        }
    }

    private fun toggleView() {
        isCalendarView = !isCalendarView
        updateView()
    }

    private fun updateView() {
        if (isCalendarView) {
            // Show calendar view
            calendarView.visibility = View.VISIBLE
            selectedDateRecyclerView.visibility = View.VISIBLE
            selectedDateTitle.visibility = View.VISIBLE
            findViewById<View>(R.id.divider).visibility = View.VISIBLE
            emptyStateText.visibility = View.GONE
            toggleButton.setImageResource(R.drawable.ic_list)
            toggleButton.contentDescription = "Switch to list view"

            // Load recipes for the initially selected date
            loadRecipesForSelectedDate()
        } else {
            // Show all favorites list
            calendarView.visibility = View.GONE
            selectedDateTitle.visibility = View.GONE
            findViewById<View>(R.id.divider).visibility = View.GONE
            toggleButton.setImageResource(R.drawable.ic_calendar)
            toggleButton.contentDescription = "Switch to calendar view"

            loadAllFavorites()
        }
    }

    private fun loadRecipesForSelectedDate() {
        val recipes = favoritesDbHelper.getFavoritesByDate(selectedDate)

        if (recipes.isEmpty()) {
            selectedDateRecyclerView.visibility = View.GONE
            emptyStateText.visibility = View.VISIBLE
            emptyStateText.text = "No favorites saved on this date"
        } else {
            selectedDateRecyclerView.visibility = View.VISIBLE
            emptyStateText.visibility = View.GONE
            selectedDateAdapter.submitList(recipes)
        }
    }

    private fun loadAllFavorites() {
        val allFavorites = favoritesDbHelper.getAllFavorites()

        if (allFavorites.isEmpty()) {
            selectedDateRecyclerView.visibility = View.GONE
            emptyStateText.visibility = View.VISIBLE
            emptyStateText.text = "No favorites yet\n\nYour favorite recipes will appear here."
        } else {
            selectedDateRecyclerView.visibility = View.VISIBLE
            emptyStateText.visibility = View.GONE
            selectedDateRecyclerView.adapter = allFavoritesAdapter
            allFavoritesAdapter.submitList(allFavorites)
        }
    }

    private fun updateSelectedDateTitle(year: Int, month: Int, dayOfMonth: Int) {
        val calendar = Calendar.getInstance()
        calendar.set(year, month, dayOfMonth)

        val dateFormat = SimpleDateFormat("EEEE, MMMM d", Locale.getDefault())
        val formattedDate = dateFormat.format(calendar.time)

        selectedDateTitle.text = "$formattedDate Favorites"
    }

    private fun openRecipeDetail(recipe: Recipe) {
        val intent = Intent(this, RecipeActivity::class.java)
        intent.putExtra("recipe", recipe)
        startActivity(intent)
    }

    private fun getCurrentDate(): String {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        return dateFormat.format(Date())
    }

    override fun onResume() {
        super.onResume()
        // Refresh the data when returning to this activity
        if (isCalendarView) {
            loadRecipesForSelectedDate()
        } else {
            loadAllFavorites()
        }
    }
}