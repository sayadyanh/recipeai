package com.homeapps.recipeai.database

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.homeapps.recipeai.models.Recipe
import java.text.SimpleDateFormat
import java.util.*

class FavoritesDbHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        const val DATABASE_VERSION = 1
        const val DATABASE_NAME = "Favorites.db"

        const val TABLE_FAVORITES = "favorites"
        const val COLUMN_ID = "id"
        const val COLUMN_RECIPE_ID = "recipe_id"
        const val COLUMN_NAME = "name"
        const val COLUMN_SHORT_DESCRIPTION = "short_description"
        const val COLUMN_FULL_DESCRIPTION = "full_description"
        const val COLUMN_IMAGE_URL = "image_url"
        const val COLUMN_COOKING_TIME = "cooking_time"
        const val COLUMN_SERVINGS = "servings"
        const val COLUMN_DIFFICULTY = "difficulty"
        const val COLUMN_CAPTURED_IMAGE_PATH = "captured_image_path"
        const val COLUMN_DATE_SAVED = "date_saved"
    }

    override fun onCreate(db: SQLiteDatabase) {
        val createTable = """
            CREATE TABLE $TABLE_FAVORITES (
                $COLUMN_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COLUMN_RECIPE_ID TEXT UNIQUE,
                $COLUMN_NAME TEXT,
                $COLUMN_SHORT_DESCRIPTION TEXT,
                $COLUMN_FULL_DESCRIPTION TEXT,
                $COLUMN_IMAGE_URL TEXT,
                $COLUMN_COOKING_TIME TEXT,
                $COLUMN_SERVINGS TEXT,
                $COLUMN_DIFFICULTY TEXT,
                $COLUMN_CAPTURED_IMAGE_PATH TEXT,
                $COLUMN_DATE_SAVED TEXT
            )
        """.trimIndent()

        db.execSQL(createTable)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLE_FAVORITES")
        onCreate(db)
    }

    fun addFavorite(recipe: Recipe): Boolean {
        val db = this.writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_RECIPE_ID, recipe.id)
            put(COLUMN_NAME, recipe.name)
            put(COLUMN_SHORT_DESCRIPTION, recipe.shortDescription)
            put(COLUMN_FULL_DESCRIPTION, recipe.fullDescription)
            put(COLUMN_IMAGE_URL, recipe.imageUrl)
            put(COLUMN_COOKING_TIME, recipe.cookingTime)
            put(COLUMN_SERVINGS, recipe.servings)
            put(COLUMN_DIFFICULTY, recipe.difficulty)
            put(COLUMN_CAPTURED_IMAGE_PATH, recipe.capturedImagePath)
            put(COLUMN_DATE_SAVED, getCurrentDate())
        }

        val result = db.insertWithOnConflict(TABLE_FAVORITES, null, values, SQLiteDatabase.CONFLICT_REPLACE)
        db.close()
        return result != -1L
    }

    fun removeFavorite(recipeId: String): Boolean {
        val db = this.writableDatabase
        val result = db.delete(TABLE_FAVORITES, "$COLUMN_RECIPE_ID = ?", arrayOf(recipeId))
        db.close()
        return result > 0
    }

    fun isFavorite(recipeId: String): Boolean {
        val db = this.readableDatabase
        val cursor = db.query(
            TABLE_FAVORITES,
            arrayOf(COLUMN_ID),
            "$COLUMN_RECIPE_ID = ?",
            arrayOf(recipeId),
            null, null, null
        )
        val exists = cursor.count > 0
        cursor.close()
        db.close()
        return exists
    }

    fun getAllFavorites(): List<Recipe> {
        val favorites = mutableListOf<Recipe>()
        val db = this.readableDatabase
        val cursor = db.query(
            TABLE_FAVORITES,
            null,
            null, null, null, null,
            "$COLUMN_DATE_SAVED DESC"
        )

        while (cursor.moveToNext()) {
            val recipe = Recipe(
                id = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_RECIPE_ID)),
                name = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_NAME)),
                shortDescription = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_SHORT_DESCRIPTION)),
                fullDescription = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_FULL_DESCRIPTION)),
                imageUrl = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_IMAGE_URL)),
                cookingTime = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_COOKING_TIME)),
                servings = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_SERVINGS)),
                difficulty = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DIFFICULTY)),
                capturedImagePath = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_CAPTURED_IMAGE_PATH))
            )
            favorites.add(recipe)
        }

        cursor.close()
        db.close()
        return favorites
    }

    fun getFavoritesByDate(date: String): List<Recipe> {
        val favorites = mutableListOf<Recipe>()
        val db = this.readableDatabase
        val cursor = db.query(
            TABLE_FAVORITES,
            null,
            "$COLUMN_DATE_SAVED = ?",
            arrayOf(date),
            null, null,
            "$COLUMN_DATE_SAVED DESC"
        )

        while (cursor.moveToNext()) {
            val recipe = Recipe(
                id = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_RECIPE_ID)),
                name = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_NAME)),
                shortDescription = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_SHORT_DESCRIPTION)),
                fullDescription = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_FULL_DESCRIPTION)),
                imageUrl = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_IMAGE_URL)),
                cookingTime = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_COOKING_TIME)),
                servings = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_SERVINGS)),
                difficulty = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DIFFICULTY)),
                capturedImagePath = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_CAPTURED_IMAGE_PATH))
            )
            favorites.add(recipe)
        }

        cursor.close()
        db.close()
        return favorites
    }

    fun getDatesWithFavorites(): List<String> {
        val dates = mutableListOf<String>()
        val db = this.readableDatabase
        val cursor = db.query(
            TABLE_FAVORITES,
            arrayOf("DISTINCT $COLUMN_DATE_SAVED"),
            null, null, null, null,
            "$COLUMN_DATE_SAVED DESC"
        )

        while (cursor.moveToNext()) {
            dates.add(cursor.getString(0))
        }

        cursor.close()
        db.close()
        return dates
    }

    private fun getCurrentDate(): String {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        return dateFormat.format(Date())
    }
}