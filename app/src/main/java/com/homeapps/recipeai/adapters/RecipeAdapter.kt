package com.homeapps.recipeai.adapters

import android.graphics.BitmapFactory
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.homeapps.recipeai.R
import com.homeapps.recipeai.models.Recipe
import java.io.File

class RecipeAdapter(
    private val onRecipeClick: (Recipe) -> Unit
) : ListAdapter<Recipe, RecipeAdapter.RecipeViewHolder>(RecipeDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecipeViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_recipe, parent, false)
        return RecipeViewHolder(view, onRecipeClick)
    }

    override fun onBindViewHolder(holder: RecipeViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class RecipeViewHolder(
        itemView: View,
        private val onRecipeClick: (Recipe) -> Unit
    ) : RecyclerView.ViewHolder(itemView) {

        private val recipeImage: ImageView = itemView.findViewById(R.id.iv_recipe_image)
        private val recipeName: TextView = itemView.findViewById(R.id.tv_recipe_name)
        private val recipeDescription: TextView = itemView.findViewById(R.id.tv_recipe_description)
        private val cookingTime: TextView = itemView.findViewById(R.id.tv_cooking_time)
        private val servings: TextView = itemView.findViewById(R.id.tv_servings)
        private val difficulty: TextView = itemView.findViewById(R.id.tv_difficulty)

        fun bind(recipe: Recipe) {
            recipeName.text = recipe.name
            recipeDescription.text = recipe.shortDescription
            cookingTime.text = recipe.cookingTime
            servings.text = recipe.servings
            difficulty.text = recipe.difficulty

            // Load captured image if available, otherwise use placeholder
            if (!recipe.capturedImagePath.isNullOrEmpty() && File(recipe.capturedImagePath).exists()) {
                val bitmap = BitmapFactory.decodeFile(recipe.capturedImagePath)
                bitmap?.let {
                    recipeImage.setImageBitmap(it)
                    recipeImage.scaleType = ImageView.ScaleType.CENTER_CROP
                } ?: run {
                    recipeImage.setBackgroundResource(R.drawable.cr8beeeeee)
                }
            } else {
                recipeImage.setBackgroundResource(R.drawable.cr8beeeeee)
            }

            itemView.setOnClickListener {
                onRecipeClick(recipe)
            }
        }
    }

    private class RecipeDiffCallback : DiffUtil.ItemCallback<Recipe>() {
        override fun areItemsTheSame(oldItem: Recipe, newItem: Recipe): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Recipe, newItem: Recipe): Boolean {
            return oldItem == newItem
        }
    }
}