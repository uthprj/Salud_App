package com.example.salud_app.model

import androidx.room.PrimaryKey

data class Meal(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0 ,
    val name: String = "", // "Breakfast", "Lunch", etc.
    val foodItems: List<FoodItem> = emptyList(),
    val calories: Int = 0,
    val proteins: Double = 0.0, // grams
    val carbohydrates: Double = 0.0, // grams
    val fats: Double = 0.0 // grams
)