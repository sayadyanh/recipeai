package com.homeapps.recipeai

import android.content.Intent
import android.os.Bundle
import android.text.SpannableString
import android.text.Spanned
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.util.Patterns
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import com.google.android.material.textfield.TextInputEditText

class SigninActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_signin)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        setupClickListeners()
        setupTermsAndPrivacy()
    }

    private fun setupClickListeners() {
        findViewById<Button>(R.id.btn_signin).setOnClickListener {
            attemptSignin()
        }

        findViewById<TextView>(R.id.tv_forgot_password).setOnClickListener {
            Toast.makeText(this, "Forgot password clicked", Toast.LENGTH_SHORT).show()
        }

        findViewById<TextView>(R.id.tv_create_account).setOnClickListener {
            Toast.makeText(this, "Create account clicked", Toast.LENGTH_SHORT).show()
        }

        findViewById<Button>(R.id.btn_google_signin).setOnClickListener {
            Toast.makeText(this, "Google sign-in clicked", Toast.LENGTH_SHORT).show()
        }

        findViewById<Button>(R.id.btn_apple_signin).setOnClickListener {
            Toast.makeText(this, "Apple sign-in clicked", Toast.LENGTH_SHORT).show()
        }
    }

    private fun setupTermsAndPrivacy() {
        val termsText = getString(R.string.terms_privacy_text)
        val spannableString = SpannableString(termsText)

        val termsStart = termsText.indexOf("Terms of Service")
        val termsEnd = termsStart + "Terms of Service".length

        val privacyStart = termsText.indexOf("Privacy Policy")
        val privacyEnd = privacyStart + "Privacy Policy".length

        val termsClickableSpan = object : ClickableSpan() {
            override fun onClick(widget: View) {
                Toast.makeText(this@SigninActivity, "Terms of Service clicked", Toast.LENGTH_SHORT).show()
            }
        }

        val privacyClickableSpan = object : ClickableSpan() {
            override fun onClick(widget: View) {
                Toast.makeText(this@SigninActivity, "Privacy Policy clicked", Toast.LENGTH_SHORT).show()
            }
        }

        spannableString.setSpan(
            termsClickableSpan,
            termsStart,
            termsEnd,
            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        )

        spannableString.setSpan(
            privacyClickableSpan,
            privacyStart,
            privacyEnd,
            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        )

        findViewById<TextView>(R.id.tv_terms_privacy).text = spannableString
        findViewById<TextView>(R.id.tv_terms_privacy).movementMethod = LinkMovementMethod.getInstance()
    }

    private fun attemptSignin() {
        val emailField = findViewById<TextInputEditText>(R.id.et_email)
        val passwordField = findViewById<TextInputEditText>(R.id.et_password)

        val email = emailField.text.toString().trim()
        val password = passwordField.text.toString().trim()

        var isValid = true

        if (email.isEmpty()) {
            emailField.error = getString(R.string.error_email_required)
            isValid = false
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailField.error = getString(R.string.error_invalid_email)
            isValid = false
        } else {
            emailField.error = null
        }

        if (password.isEmpty()) {
            passwordField.error = getString(R.string.error_password_required)
            isValid = false
        } else if (password.length < 6) {
            passwordField.error = getString(R.string.error_password_too_short)
            isValid = false
        } else {
            passwordField.error = null
        }

        if (isValid) {
            Toast.makeText(this, "Signin successful!", Toast.LENGTH_SHORT).show()

            // Extract username from email (part before @)
            val username = email.substringBefore("@")

            // Save sign-in state to SharedPreferences
            val sharedPref = getSharedPreferences("user_prefs", MODE_PRIVATE)
            with(sharedPref.edit()) {
                putBoolean("is_signed_in", true)
                putString("username", username)
                apply()
            }

            // Navigate to MainActivity
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        }
    }
}