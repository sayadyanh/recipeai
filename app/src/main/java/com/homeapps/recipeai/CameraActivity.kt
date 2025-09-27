package com.homeapps.recipeai

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Base64
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.homeapps.recipeai.models.Recipe
import com.homeapps.recipeai.models.RecipeResponse
import org.json.JSONArray
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL
import java.text.SimpleDateFormat
import java.util.*
import kotlin.concurrent.thread

class CameraActivity : AppCompatActivity() {

    private lateinit var capturedImageView: ImageView
    private lateinit var captureButton: Button
    private lateinit var generateButton: Button
    private lateinit var loadingProgressBar: ProgressBar
    private var capturedBitmap: Bitmap? = null
    private var currentPhotoPath: String = ""

    companion object {
        private const val API_BASE_URL = "https://api.openai.com/v1/chat/completions"
        private const val API_KEY = "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"
    }

    private val cameraLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == RESULT_OK) {
            // Load the full-resolution image from file
            if (currentPhotoPath.isNotEmpty()) {
                val bitmap = BitmapFactory.decodeFile(currentPhotoPath)
                bitmap?.let {
                    // Scale down the image for display while keeping the original for API
                    val scaledBitmap = scaleBitmapForDisplay(it)
                    capturedBitmap = it
                    capturedImageView.setImageBitmap(scaledBitmap)
                    generateButton.visibility = View.VISIBLE
                }
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
            // Create a file to store the full-resolution image
            val photoFile: File? = try {
                createImageFile()
            } catch (ex: IOException) {
                Toast.makeText(this, "Error creating image file", Toast.LENGTH_SHORT).show()
                null
            }

            photoFile?.also {
                val photoURI: Uri = FileProvider.getUriForFile(
                    this,
                    "com.homeapps.recipeai.fileprovider",
                    it
                )
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
                cameraLauncher.launch(takePictureIntent)
            }
        } else {
            Toast.makeText(this, "No camera app available", Toast.LENGTH_SHORT).show()
        }
    }

    @Throws(IOException::class)
    private fun createImageFile(): File {
        val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val storageDir: File? = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File.createTempFile(
            "JPEG_${timeStamp}_",
            ".jpg",
            storageDir
        ).apply {
            currentPhotoPath = absolutePath
        }
    }

    private fun scaleBitmapForDisplay(bitmap: Bitmap): Bitmap {
        val maxWidth = 800
        val maxHeight = 600

        val width = bitmap.width
        val height = bitmap.height

        val scale = minOf(maxWidth.toFloat() / width, maxHeight.toFloat() / height)

        return if (scale < 1.0f) {
            val newWidth = (width * scale).toInt()
            val newHeight = (height * scale).toInt()
            Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, true)
        } else {
            bitmap
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
                if (API_KEY == "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") {
                    // Fall back to mock data if API key is not configured
                    generateMockRecipes()
                    return@thread
                }

                capturedBitmap?.let { bitmap ->
                    val base64Image = bitmapToBase64(bitmap)
                    val recipes = callOpenAIVisionAPI(base64Image)

                    runOnUiThread {
                        showLoading(false)
                        if (recipes.isNotEmpty()) {
                            val response = RecipeResponse(recipes = recipes)
                            openRecipesActivity(response)
                        } else {
                            generateMockRecipes()
                        }
                    }
                } ?: run {
                    generateMockRecipes()
                }

            } catch (e: Exception) {
                runOnUiThread {
                    showLoading(false)
                    Toast.makeText(this@CameraActivity, "Failed to generate recipes. Please try again.", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun bitmapToBase64(bitmap: Bitmap): String {
        val byteArrayOutputStream = ByteArrayOutputStream()
        // Compress the image to reduce API payload size
        bitmap.compress(Bitmap.CompressFormat.JPEG, 80, byteArrayOutputStream)
        val byteArray = byteArrayOutputStream.toByteArray()
        return Base64.encodeToString(byteArray, Base64.DEFAULT)
    }

    private fun callOpenAIVisionAPI(base64Image: String): List<Recipe> {
        val url = URL(API_BASE_URL)
        val connection = url.openConnection() as HttpURLConnection

        connection.requestMethod = "POST"
        connection.setRequestProperty("Content-Type", "application/json")
        connection.setRequestProperty("Authorization", "Bearer $API_KEY")
        connection.doOutput = true

        val prompt = """
            Analyze this food image and create exactly 3 different recipe suggestions. For each recipe, provide:
            - A descriptive name
            - A short description (max 80 characters ending with "...")
            - Full detailed description with ingredients and step-by-step instructions
            - Cooking time
            - Number of servings
            - Difficulty level (Easy, Medium, Hard)

            Return the response as a JSON array with this structure:
            [
              {
                "id": "1",
                "name": "Recipe Name",
                "shortDescription": "Brief description...",
                "fullDescription": "Detailed recipe with ingredients and instructions",
                "cookingTime": "X minutes",
                "servings": "X people",
                "difficulty": "Easy/Medium/Hard"
              }
            ]
        """.trimIndent()

        val requestBody = JSONObject().apply {
            put("model", "gpt-4-vision-preview")
            put("messages", JSONArray().apply {
                put(JSONObject().apply {
                    put("role", "user")
                    put("content", JSONArray().apply {
                        put(JSONObject().apply {
                            put("type", "text")
                            put("text", prompt)
                        })
                        put(JSONObject().apply {
                            put("type", "image_url")
                            put("image_url", JSONObject().apply {
                                put("url", "data:image/jpeg;base64,$base64Image")
                            })
                        })
                    })
                })
            })
            put("max_tokens", 2000)
        }

        connection.outputStream.use { os ->
            os.write(requestBody.toString().toByteArray())
        }

        val responseCode = connection.responseCode
        val response = if (responseCode == HttpURLConnection.HTTP_OK) {
            connection.inputStream.bufferedReader().use { it.readText() }
        } else {
            connection.errorStream?.bufferedReader()?.use { it.readText() } ?: "Unknown error"
        }

        return if (responseCode == HttpURLConnection.HTTP_OK) {
            parseOpenAIResponse(response)
        } else {
            emptyList()
        }
    }

    private fun parseOpenAIResponse(response: String): List<Recipe> {
        return try {
            val jsonResponse = JSONObject(response)
            val choices = jsonResponse.getJSONArray("choices")
            val content = choices.getJSONObject(0)
                .getJSONObject("message")
                .getString("content")

            // Extract JSON from the response (it might be wrapped in markdown)
            val jsonStart = content.indexOf('[')
            val jsonEnd = content.lastIndexOf(']')

            if (jsonStart != -1 && jsonEnd != -1) {
                val recipesJson = content.substring(jsonStart, jsonEnd + 1)
                val recipesArray = JSONArray(recipesJson)

                val recipes = mutableListOf<Recipe>()
                for (i in 0 until recipesArray.length()) {
                    val recipeJson = recipesArray.getJSONObject(i)
                    recipes.add(
                        Recipe(
                            id = recipeJson.getString("id"),
                            name = recipeJson.getString("name"),
                            shortDescription = recipeJson.getString("shortDescription"),
                            fullDescription = recipeJson.getString("fullDescription"),
                            imageUrl = null,
                            cookingTime = recipeJson.getString("cookingTime"),
                            servings = recipeJson.getString("servings"),
                            difficulty = recipeJson.getString("difficulty"),
                            capturedImagePath = currentPhotoPath
                        )
                    )
                }
                recipes
            } else {
                emptyList()
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    private fun generateMockRecipes() {
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
                difficulty = "Medium",
                capturedImagePath = currentPhotoPath
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
                difficulty = "Easy",
                capturedImagePath = currentPhotoPath
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
                difficulty = "Easy",
                capturedImagePath = currentPhotoPath
            )
        )

        val response = RecipeResponse(recipes = mockRecipes)

        runOnUiThread {
            showLoading(false)
            openRecipesActivity(response)
        }
    }

    private fun openRecipesActivity(response: RecipeResponse) {
        val intent = Intent(this, RecipesActivity::class.java)
        intent.putExtra("recipes_response", response)
        startActivity(intent)
        finish()
    }
}