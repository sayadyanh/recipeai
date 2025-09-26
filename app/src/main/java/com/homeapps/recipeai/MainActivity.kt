package com.homeapps.recipeai

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.PopupMenu
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import java.net.URL
import kotlin.concurrent.thread

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Check if user is signed in
        val sharedPref = getSharedPreferences("user_prefs", MODE_PRIVATE)
        val isSignedIn = sharedPref.getBoolean("is_signed_in", false)
        val username = sharedPref.getString("username", "User")

        if (!isSignedIn) {
            // Redirect to signin if not signed in
            val intent = Intent(this, SigninActivity::class.java)
            startActivity(intent)
            finish()
            return
        }

        // Show main screen and hide welcome screen
        val welcomeScreen = findViewById<View>(R.id.ll_welcome_screen)
        val mainScreen = findViewById<View>(R.id.ll_main_screen)
        welcomeScreen.visibility = View.GONE
        mainScreen.visibility = View.VISIBLE

        // Setup UI elements
        val greetingText = findViewById<TextView>(R.id.tv_greeting)
        val menuIcon = findViewById<ImageView>(R.id.iv_menu)
        val avatarIcon = findViewById<ImageView>(R.id.iv_avatar)

        greetingText.text = "Hi, $username"

        // Load recipe data from API
        loadRecipeData()

        // Setup menu dropdown
        menuIcon.setOnClickListener { view ->
            val popupMenu = PopupMenu(this, view)
            popupMenu.menuInflater.inflate(R.menu.profile_menu, popupMenu.menu)

            // Force icons to show
            try {
                val fieldMPopup = PopupMenu::class.java.getDeclaredField("mPopup")
                fieldMPopup.isAccessible = true
                val mPopup = fieldMPopup.get(popupMenu)
                mPopup.javaClass
                    .getDeclaredMethod("setForceShowIcon", Boolean::class.java)
                    .invoke(mPopup, true)
            } catch (e: Exception) {
                e.printStackTrace()
            }

            popupMenu.setOnMenuItemClickListener { menuItem ->
                when (menuItem.itemId) {
                    R.id.menu_profile -> {
                        startActivity(Intent(this, ProfileActivity::class.java))
                        true
                    }
                    R.id.menu_settings -> {
                        startActivity(Intent(this, SettingsActivity::class.java))
                        true
                    }
                    R.id.menu_favorites -> {
                        startActivity(Intent(this, FavoritesActivity::class.java))
                        true
                    }
                    R.id.menu_preferences -> {
                        startActivity(Intent(this, PreferencesActivity::class.java))
                        true
                    }
                    R.id.menu_support -> {
                        startActivity(Intent(this, SupportActivity::class.java))
                        true
                    }
                    R.id.menu_logout -> {
                        // Handle logout
                        with(sharedPref.edit()) {
                            putBoolean("is_signed_in", false)
                            remove("username")
                            apply()
                        }
                        val intent = Intent(this, SigninActivity::class.java)
                        startActivity(intent)
                        finish()
                        true
                    }
                    else -> false
                }
            }
            popupMenu.show()
        }
    }

    private fun loadRecipeData() {
        val loadingIndicator = findViewById<ProgressBar>(R.id.pb_loading)
        val recipeContent = findViewById<LinearLayout>(R.id.ll_recipe_content)
        val errorText = findViewById<TextView>(R.id.tv_error)

        // Show loading state
        loadingIndicator.visibility = View.VISIBLE
        recipeContent.visibility = View.GONE
        errorText.visibility = View.GONE

        // Simulate API call with mock data
        thread {
            try {
                // Simulate network delay
                Thread.sleep(2000)

                // Mock API response data
                val recipeData = Recipe(
                    title = "Spaghetti Carbonara",
                    description = """
                        A classic Italian pasta dish made with eggs, cheese, pancetta, and pepper.

                        Ingredients:
                        • 400g spaghetti
                        • 200g pancetta or guanciale
                        • 4 large eggs
                        • 100g Pecorino Romano cheese
                        • Black pepper
                        • Salt

                        Instructions:
                        1. Cook spaghetti in salted boiling water until al dente
                        2. Cut pancetta into small cubes and cook until crispy
                        3. Beat eggs with grated cheese and black pepper
                        4. Drain pasta and mix with pancetta
                        5. Remove from heat and quickly mix in egg mixture
                        6. Serve immediately with extra cheese and pepper

                        Serves 4 people. Cooking time: 20 minutes.
                    """.trimIndent(),
                    imageUrl = null // We'll use a placeholder since we don't have actual image loading
                )

                // Update UI on main thread
                runOnUiThread {
                    displayRecipe(recipeData)
                }

            } catch (e: Exception) {
                runOnUiThread {
                    showError()
                }
            }
        }
    }

    private fun displayRecipe(recipe: Recipe) {
        val loadingIndicator = findViewById<ProgressBar>(R.id.pb_loading)
        val recipeContent = findViewById<LinearLayout>(R.id.ll_recipe_content)
        val errorText = findViewById<TextView>(R.id.tv_error)

        val recipeTitle = findViewById<TextView>(R.id.tv_recipe_title)
        val recipeDescription = findViewById<TextView>(R.id.tv_recipe_description)
        val recipeImage = findViewById<ImageView>(R.id.iv_recipe_image)

        // Hide loading, show content
        loadingIndicator.visibility = View.GONE
        recipeContent.visibility = View.VISIBLE
        errorText.visibility = View.GONE

        // Set recipe data
        recipeTitle.text = recipe.title
        recipeDescription.text = recipe.description

        // For now, use a placeholder background since we don't have image loading implemented
        recipeImage.setBackgroundResource(R.drawable.cr8beeeeee)
        // TODO: Load actual image from recipe.imageUrl using an image loading library like Glide or Picasso
    }

    private fun showError() {
        val loadingIndicator = findViewById<ProgressBar>(R.id.pb_loading)
        val recipeContent = findViewById<LinearLayout>(R.id.ll_recipe_content)
        val errorText = findViewById<TextView>(R.id.tv_error)

        loadingIndicator.visibility = View.GONE
        recipeContent.visibility = View.GONE
        errorText.visibility = View.VISIBLE
    }

    data class Recipe(
        val title: String,
        val description: String,
        val imageUrl: String?
    )
}