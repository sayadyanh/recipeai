package com.homeapps.recipeai

import android.os.Bundle
import android.util.Patterns
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class SupportActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_support)

        val backButton = findViewById<ImageView>(R.id.iv_back)
        val emailField = findViewById<EditText>(R.id.et_email)
        val messageField = findViewById<EditText>(R.id.et_message)
        val submitButton = findViewById<Button>(R.id.btn_submit)

        // Pre-fill email from user preferences if available
        val sharedPref = getSharedPreferences("user_prefs", MODE_PRIVATE)
        val userEmail = sharedPref.getString("user_email", "")
        if (!userEmail.isNullOrEmpty()) {
            emailField.setText(userEmail)
        }

        backButton.setOnClickListener {
            finish()
        }

        submitButton.setOnClickListener {
            submitSupportRequest(emailField, messageField)
        }
    }

    private fun submitSupportRequest(emailField: EditText, messageField: EditText) {
        val email = emailField.text.toString().trim()
        val message = messageField.text.toString().trim()

        var isValid = true

        // Validate email
        if (email.isEmpty()) {
            emailField.error = "Email is required"
            isValid = false
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailField.error = "Please enter a valid email address"
            isValid = false
        } else {
            emailField.error = null
        }

        // Validate message
        if (message.isEmpty()) {
            messageField.error = "Message is required"
            isValid = false
        } else if (message.length < 10) {
            messageField.error = "Message must be at least 10 characters long"
            isValid = false
        } else {
            messageField.error = null
        }

        if (isValid) {
            // Save email for future use
            val sharedPref = getSharedPreferences("user_prefs", MODE_PRIVATE)
            with(sharedPref.edit()) {
                putString("user_email", email)
                apply()
            }

            // Simulate sending support request
            Toast.makeText(this, "Support request submitted successfully!", Toast.LENGTH_LONG).show()

            // Clear fields
            messageField.text.clear()

            // Close activity after a delay
            finish()
        }
    }
}