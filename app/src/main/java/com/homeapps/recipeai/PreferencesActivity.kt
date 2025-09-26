package com.homeapps.recipeai

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat

class PreferencesActivity : AppCompatActivity() {

    private lateinit var keywordInput: EditText
    private lateinit var addButton: Button
    private lateinit var keywordsContainer: LinearLayout
    private val keywords = mutableListOf<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_preferences)

        val backButton = findViewById<ImageView>(R.id.iv_back)
        keywordInput = findViewById(R.id.et_keyword)
        addButton = findViewById(R.id.btn_add_keyword)
        keywordsContainer = findViewById(R.id.ll_keywords_container)

        backButton.setOnClickListener {
            finish()
        }

        addButton.setOnClickListener {
            addKeyword()
        }

        loadKeywords()
        displayKeywords()
    }

    private fun addKeyword() {
        val keyword = keywordInput.text.toString().trim()
        if (keyword.isNotEmpty() && !keywords.contains(keyword)) {
            keywords.add(keyword)
            keywordInput.text.clear()
            saveKeywords()
            displayKeywords()
            Toast.makeText(this, "Keyword added", Toast.LENGTH_SHORT).show()
        } else if (keywords.contains(keyword)) {
            Toast.makeText(this, "Keyword already exists", Toast.LENGTH_SHORT).show()
        } else {
            keywordInput.error = "Keyword cannot be empty"
        }
    }

    private fun removeKeyword(keyword: String) {
        keywords.remove(keyword)
        saveKeywords()
        displayKeywords()
        Toast.makeText(this, "Keyword removed", Toast.LENGTH_SHORT).show()
    }

    private fun displayKeywords() {
        keywordsContainer.removeAllViews()

        if (keywords.isEmpty()) {
            val emptyText = TextView(this)
            emptyText.text = "No keywords added yet"
            emptyText.textSize = 16f
            emptyText.setTextColor(ContextCompat.getColor(this, android.R.color.darker_gray))
            emptyText.setPadding(16, 32, 16, 16)
            keywordsContainer.addView(emptyText)
            return
        }

        for (keyword in keywords) {
            val keywordView = layoutInflater.inflate(R.layout.item_keyword, keywordsContainer, false)
            val keywordText = keywordView.findViewById<TextView>(R.id.tv_keyword)
            val removeButton = keywordView.findViewById<ImageView>(R.id.iv_remove)

            keywordText.text = keyword
            removeButton.setOnClickListener {
                removeKeyword(keyword)
            }

            keywordsContainer.addView(keywordView)
        }
    }

    private fun loadKeywords() {
        val sharedPref = getSharedPreferences("preferences_prefs", MODE_PRIVATE)
        val keywordsString = sharedPref.getString("keywords", "")
        if (!keywordsString.isNullOrEmpty()) {
            keywords.clear()
            keywords.addAll(keywordsString.split(","))
        }
    }

    private fun saveKeywords() {
        val sharedPref = getSharedPreferences("preferences_prefs", MODE_PRIVATE)
        with(sharedPref.edit()) {
            putString("keywords", keywords.joinToString(","))
            apply()
        }
    }
}