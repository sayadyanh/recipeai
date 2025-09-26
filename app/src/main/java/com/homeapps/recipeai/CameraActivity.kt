package com.homeapps.recipeai

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.homeapps.recipeai.models.Recipe
import com.homeapps.recipeai.models.RecipeResponse
import kotlin.concurrent.thread

class CameraActivity : AppCompatActivity() {

    private lateinit var capturedImageView: ImageView
    private lateinit var captureButton: Button
    private lateinit var generateButton: Button
    private lateinit var loadingProgressBar: ProgressBar
    private var capturedBitmap: Bitmap? = null

    private val cameraLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == RESULT_OK) {
            val imageBitmap = result.data?.extras?.get("data") as Bitmap?
            imageBitmap?.let {
                capturedBitmap = it
                capturedImageView.setImageBitmap(it)
                generateButton.visibility = android.view.View.VISIBLE
            }
        }
    }

    private val requestPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
        if (isGranted) {
            openCamera()
        } else {
            Toast.makeText(this, "Camera permission is required to take photos", Toast.LENGTH_LONG).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_camera)

        initViews()
        setupListeners()
        checkCameraPermission()
    }

    private fun initViews() {
        capturedImageView = findViewById(R.id.iv_captured_image)
        captureButton = findViewById(R.id.btn_capture)
        generateButton = findViewById(R.id.btn_generate)
        loadingProgressBar = findViewById(R.id.pb_generating)
    }

    private fun setupListeners() {
        findViewById<ImageView>(R.id.iv_back).setOnClickListener {
            finish()
        }

        captureButton.setOnClickListener {
            checkCameraPermission()
        }

        generateButton.setOnClickListener {
            generateRecipe()
        }
    }

    private fun checkCameraPermission() {
        when {
            ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED -> {
                openCamera()
            }
            ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.CAMERA) -> {
                Toast.makeText(this, "Camera permission is needed to take photos of your food", Toast.LENGTH_LONG).show()
                requestPermissionLauncher.launch(Manifest.permission.CAMERA)
            }
            else -> {
                requestPermissionLauncher.launch(Manifest.permission.CAMERA)
            }
        }
    }

    private fun openCamera() {
        val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        if (takePictureIntent.resolveActivity(packageManager) != null) {
            cameraLauncher.launch(takePictureIntent)
        } else {
            Toast.makeText(this, "No camera app available", Toast.LENGTH_SHORT).show()
        }
    }

    private fun generateRecipe() {
        capturedBitmap?.let {
            showLoading(true)
            callBackendAPI()
        } ?: run {
            Toast.makeText(this, "Please take a photo first", Toast.LENGTH_SHORT).show()
        }
    }

    private fun showLoading(isLoading: Boolean) {
        loadingProgressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        generateButton.isEnabled = !isLoading
        generateButton.text = if (isLoading) "Generating..." else "Generate Recipe"
    }

    private fun callBackendAPI() {
        thread {
            try {
                // Simulate API call delay
                Thread.sleep(3000)

                // Mock API response with 3 recipes
                val mockRecipes = listOf(
                    Recipe(
                        id = "1",
                        name = "Spaghetti Carbonara",
                        shortDescription = "A classic Italian pasta dish with eggs, cheese, and pancetta. Rich and creamy...",
                        fullDescription = """
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

                            This traditional Roman dish is perfect for a quick weeknight dinner.
                        """.trimIndent(),
                        imageUrl = null,
                        cookingTime = "20 minutes",
                        servings = "4 people",
                        difficulty = "Medium"
                    ),
                    Recipe(
                        id = "2",
                        name = "Chicken Alfredo",
                        shortDescription = "Creamy chicken pasta with garlic and parmesan. A comfort food favorite...",
                        fullDescription = """
                            Creamy chicken alfredo pasta with tender chicken and rich parmesan sauce.

                            Ingredients:
                            • 500g fettuccine pasta
                            • 2 chicken breasts, sliced
                            • 1 cup heavy cream
                            • 1 cup grated parmesan
                            • 4 cloves garlic, minced
                            • 2 tbsp butter
                            • Salt and pepper

                            Instructions:
                            1. Cook pasta according to package directions
                            2. Season and cook chicken until golden brown
                            3. Make alfredo sauce with cream, butter, and parmesan
                            4. Combine pasta, chicken, and sauce
                            5. Garnish with fresh parsley and serve hot

                            A restaurant-quality dish you can make at home.
                        """.trimIndent(),
                        imageUrl = null,
                        cookingTime = "25 minutes",
                        servings = "4 people",
                        difficulty = "Easy"
                    ),
                    Recipe(
                        id = "3",
                        name = "Mediterranean Salad",
                        shortDescription = "Fresh vegetables with olive oil and feta cheese. Light and healthy...",
                        fullDescription = """
                            A fresh Mediterranean salad with vegetables, olives, and feta cheese.

                            Ingredients:
                            • 2 tomatoes, chopped
                            • 1 cucumber, diced
                            • 1 red onion, thinly sliced
                            • 1/2 cup kalamata olives
                            • 200g feta cheese, crumbled
                            • 3 tbsp olive oil
                            • 1 tbsp lemon juice
                            • Fresh oregano

                            Instructions:
                            1. Combine tomatoes, cucumber, and onion in a bowl
                            2. Add olives and feta cheese
                            3. Whisk together olive oil, lemon juice, and oregano
                            4. Toss salad with dressing
                            5. Let sit for 10 minutes before serving

                            Perfect as a side dish or light lunch.
                        """.trimIndent(),
                        imageUrl = null,
                        cookingTime = "15 minutes",
                        servings = "4 people",
                        difficulty = "Easy"
                    )
                )

                val response = RecipeResponse(recipes = mockRecipes)

                runOnUiThread {
                    showLoading(false)
                    openRecipesActivity(response)
                }

            } catch (e: Exception) {
                runOnUiThread {
                    showLoading(false)
                    Toast.makeText(this@CameraActivity, "Failed to generate recipes. Please try again.", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun openRecipesActivity(response: RecipeResponse) {
        val intent = Intent(this, RecipesActivity::class.java)
        intent.putExtra("recipes_response", response)
        startActivity(intent)
        finish()
    }
}