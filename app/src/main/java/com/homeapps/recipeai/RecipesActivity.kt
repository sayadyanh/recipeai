package com.homeapps.recipeai

import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.homeapps.recipeai.adapters.RecipeAdapter
import com.homeapps.recipeai.models.Recipe
import com.homeapps.recipeai.models.RecipeResponse

class RecipesActivity : AppCompatActivity() {

    private lateinit var recipesRecyclerView: RecyclerView
    private lateinit var recipeAdapter: RecipeAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_recipes)

        initViews()
        setupRecyclerView()
        loadRecipes()
    }

    private fun initViews() {
        recipesRecyclerView = findViewById(R.id.rv_recipes)

        findViewById<ImageView>(R.id.iv_back).setOnClickListener {
            finish()
        }
    }

    private fun setupRecyclerView() {
        recipeAdapter = RecipeAdapter { recipe ->
            openRecipeDetail(recipe)
        }

        recipesRecyclerView.apply {
            layoutManager = LinearLayoutManager(this@RecipesActivity)
            adapter = recipeAdapter
        }
    }

    private fun loadRecipes() {
        val recipesResponse = intent.getParcelableExtra<RecipeResponse>("recipes_response")
        recipesResponse?.let { response ->
            recipeAdapter.submitList(response.recipes)
        }
    }

    private fun openRecipeDetail(recipe: Recipe) {
        val intent = Intent(this, RecipeActivity::class.java)
        intent.putExtra("recipe", recipe)
        startActivity(intent)
    }
}