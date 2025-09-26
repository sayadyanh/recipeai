package com.homeapps.recipeai

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.PopupMenu
import android.widget.TextView
import android.widget.Toast
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
}