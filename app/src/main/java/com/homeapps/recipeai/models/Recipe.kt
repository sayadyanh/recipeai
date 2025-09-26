package com.homeapps.recipeai.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Recipe(
    val id: String,
    val name: String,
    val shortDescription: String,
    val fullDescription: String,
    val imageUrl: String?,
    val cookingTime: String,
    val servings: String,
    val difficulty: String
) : Parcelable

@Parcelize
data class RecipeResponse(
    val recipes: List<Recipe>
) : Parcelable