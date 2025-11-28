package com.example.salud_app.model

data class Nutrition(
    val id: String = "",
    val userId: String = "",
    val date: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val breakfastCalories: Long = 0,
    val lunchCalories: Long = 0,
    val dinnerCalories: Long = 0,
    val snackCalories: Long = 0,
    val totalCalories: Long = 0,
    val totalProteins: Double = 0.0, // grams
    val totalCarbohydrates: Double = 0.0, // grams
    val totalFats: Double = 0.0 // grams
)
