package com.homeapps.recipeai

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class SettingsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        val backButton = findViewById<ImageView>(R.id.iv_back)
        val removeAccountButton = findViewById<Button>(R.id.btn_remove_account)

        backButton.setOnClickListener {
            finish()
        }

        removeAccountButton.setOnClickListener {
            showRemoveAccountDialog()
        }
    }

    private fun showRemoveAccountDialog() {
        AlertDialog.Builder(this)
            .setTitle("Remove Account")
            .setMessage("Are you sure you want to remove your account? This action cannot be undone.")
            .setPositiveButton("Remove") { _, _ ->
                removeAccount()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun removeAccount() {
        // Clear all user data
        val sharedPref = getSharedPreferences("user_prefs", MODE_PRIVATE)
        with(sharedPref.edit()) {
            clear()
            apply()
        }

        // Clear favorites data
        val favoritesPrefs = getSharedPreferences("favorites_prefs", MODE_PRIVATE)
        with(favoritesPrefs.edit()) {
            clear()
            apply()
        }

        Toast.makeText(this, "Account removed successfully", Toast.LENGTH_SHORT).show()

        // Navigate back to signin
        val intent = Intent(this, SigninActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}