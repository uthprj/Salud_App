package com.example.salud_app.model

data class FoodItem(
    val name: String = "",
    val calories: Int = 0,
    val proteins: Double = 0.0, // grams
    val carbohydrates: Double = 0.0, // grams
    val fats: Double = 0.0, // grams
)
