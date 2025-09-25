package com.homeapps.recipeai

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.PopupMenu
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

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

        // Setup menu dropdown
        menuIcon.setOnClickListener { view ->
            val popupMenu = PopupMenu(this, view)
            popupMenu.menuInflater.inflate(R.menu.profile_menu, popupMenu.menu)
            popupMenu.setOnMenuItemClickListener { menuItem ->
                when (menuItem.itemId) {
                    R.id.menu_profile -> {
                        // Handle profile settings
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
}