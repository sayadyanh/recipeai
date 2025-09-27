package com.homeapps.recipeai

import android.graphics.BitmapFactory
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.homeapps.recipeai.models.Recipe
import java.io.File

class RecipeActivity : AppCompatActivity() {

    private lateinit var recipeImage: ImageView
    private lateinit var recipeTitle: TextView
    private lateinit var recipeDescription: TextView
    private lateinit var cookingTime: TextView
    private lateinit var servings: TextView
    private lateinit var difficulty: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_recipe)

        initViews()
        loadRecipe()
    }

    private fun initViews() {
        recipeImage = findViewById(R.id.iv_recipe_image)
        recipeTitle = findViewById(R.id.tv_recipe_title)
        recipeDescription = findViewById(R.id.tv_recipe_description)
        cookingTime = findViewById(R.id.tv_cooking_time)
        servings = findViewById(R.id.tv_servings)
        difficulty = findViewById(R.id.tv_difficulty)

        findViewById<ImageView>(R.id.iv_back).setOnClickListener {
            finish()
        }
    }

    private fun loadRecipe() {
        val recipe = intent.getParcelableExtra<Recipe>("recipe")
        recipe?.let { displayRecipe(it) }
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
    }
}