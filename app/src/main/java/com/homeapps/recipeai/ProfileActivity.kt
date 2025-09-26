package com.homeapps.recipeai

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class ProfileActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        val backButton = findViewById<ImageView>(R.id.iv_back)
        val avatarImage = findViewById<ImageView>(R.id.iv_profile_avatar)
        val usernameField = findViewById<EditText>(R.id.et_username)
        val saveButton = findViewById<Button>(R.id.btn_save_profile)

        // Load current username from SharedPreferences
        val sharedPref = getSharedPreferences("user_prefs", MODE_PRIVATE)
        val currentUsername = sharedPref.getString("username", "User")
        usernameField.setText(currentUsername)

        backButton.setOnClickListener {
            finish()
        }

        avatarImage.setOnClickListener {
            Toast.makeText(this, "Avatar selection coming soon", Toast.LENGTH_SHORT).show()
        }

        saveButton.setOnClickListener {
            val newUsername = usernameField.text.toString().trim()
            if (newUsername.isNotEmpty()) {
                // Save updated username
                with(sharedPref.edit()) {
                    putString("username", newUsername)
                    apply()
                }
                Toast.makeText(this, "Profile updated successfully", Toast.LENGTH_SHORT).show()
                finish()
            } else {
                usernameField.error = "Username cannot be empty"
            }
        }
    }
}