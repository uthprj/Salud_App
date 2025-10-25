package com.example.salud_app.model

import androidx.room.PrimaryKey

data class Nutrition(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val userId: String = "",
    val date: String = "",
    val meals: List<Meal> = emptyList(),
    val totalCalories: Int = 0,
    val totalProteins: Double = 0.0, // grams
    val totalCarbohydrates: Double = 0.0, // grams
    val totalFats: Double = 0.0 // grams
)
