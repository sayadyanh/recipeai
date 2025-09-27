package com.homeapps.recipeai

import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.homeapps.recipeai.database.FavoritesDbHelper
import com.homeapps.recipeai.models.Recipe
import java.io.File

class RecipeActivity : AppCompatActivity() {

    private lateinit var recipeImage: ImageView
    private lateinit var recipeTitle: TextView
    private lateinit var recipeDescription: TextView
    private lateinit var cookingTime: TextView
    private lateinit var servings: TextView
    private lateinit var difficulty: TextView
    private lateinit var favoriteIcon: ImageView
    private lateinit var shareIcon: ImageView

    private lateinit var favoritesDbHelper: FavoritesDbHelper
    private var currentRecipe: Recipe? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_recipe)

        favoritesDbHelper = FavoritesDbHelper(this)
        initViews()
        setupListeners()
        loadRecipe()
    }

    private fun initViews() {
        recipeImage = findViewById(R.id.iv_recipe_image)
        recipeTitle = findViewById(R.id.tv_recipe_title)
        recipeDescription = findViewById(R.id.tv_recipe_description)
        cookingTime = findViewById(R.id.tv_cooking_time)
        servings = findViewById(R.id.tv_servings)
        difficulty = findViewById(R.id.tv_difficulty)
        favoriteIcon = findViewById(R.id.iv_favorite)
        shareIcon = findViewById(R.id.iv_share)

        findViewById<ImageView>(R.id.iv_back).setOnClickListener {
            finish()
        }
    }

    private fun setupListeners() {
        favoriteIcon.setOnClickListener {
            currentRecipe?.let { recipe ->
                toggleFavorite(recipe)
            }
        }

        shareIcon.setOnClickListener {
            currentRecipe?.let { recipe ->
                shareRecipe(recipe)
            }
        }
    }

    private fun loadRecipe() {
        val recipe = intent.getParcelableExtra<Recipe>("recipe")
        recipe?.let {
            currentRecipe = it
            displayRecipe(it)
        }
    }

    private fun displayRecipe(recipe: Recipe) {
        recipeTitle.text = recipe.name
        recipeDescription.text = recipe.fullDescription
        cookingTime.text = recipe.cookingTime
        servings.text = recipe.servings
        difficulty.text = recipe.difficulty

        // Load captured image if available, otherwise use placeholder
        if (!recipe.capturedImagePath.isNullOrEmpty() && File(recipe.capturedImagePath).exists()) {
            val bitmap = BitmapFactory.decodeFile(recipe.capturedImagePath)
            bitmap?.let {
                recipeImage.setImageBitmap(it)
                recipeImage.scaleType = ImageView.ScaleType.CENTER_CROP
            } ?: run {
                recipeImage.setBackgroundResource(R.drawable.cr8beeeeee)
            }
        } else {
            recipeImage.setBackgroundResource(R.drawable.cr8beeeeee)
        }

        // Update favorite icon state
        updateFavoriteIcon(recipe)
    }

    private fun updateFavoriteIcon(recipe: Recipe) {
        val isFavorite = favoritesDbHelper.isFavorite(recipe.id)
        if (isFavorite) {
            favoriteIcon.setColorFilter(ContextCompat.getColor(this, android.R.color.holo_red_dark))
        } else {
            favoriteIcon.setColorFilter(ContextCompat.getColor(this, R.color.gray))
        }
    }

    private fun toggleFavorite(recipe: Recipe) {
        val isFavorite = favoritesDbHelper.isFavorite(recipe.id)

        if (isFavorite) {
            if (favoritesDbHelper.removeFavorite(recipe.id)) {
                favoriteIcon.setColorFilter(ContextCompat.getColor(this, R.color.gray))
                Toast.makeText(this, "Removed from favorites", Toast.LENGTH_SHORT).show()
            }
        } else {
            if (favoritesDbHelper.addFavorite(recipe)) {
                favoriteIcon.setColorFilter(ContextCompat.getColor(this, android.R.color.holo_red_dark))
                Toast.makeText(this, "Added to favorites", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun shareRecipe(recipe: Recipe) {
        val shareText = """
            ${recipe.name}

            ${recipe.shortDescription}

            Cooking Time: ${recipe.cookingTime}
            Servings: ${recipe.servings}
            Difficulty: ${recipe.difficulty}

            Instructions:
            ${recipe.fullDescription}

            Shared from RecipeAI
        """.trimIndent()

        val shareIntent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, shareText)
            type = "text/plain"
        }

        startActivity(Intent.createChooser(shareIntent, "Share Recipe"))
    }
}