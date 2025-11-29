package com.example.salud_app.model

data class Nutrition(
    val id: String = "",
    val userId: String = "",
    val date: String = "",
    val time: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val mealType: String = "", // "Sáng", "Trưa", "Chiều", "Khác"
    val mealCategory: String = "", // "Bữa sáng", "Bữa trưa", "Bữa tối", "Bữa phụ"
    val mealName: String = "",
    val calories: Double = 0.0, // kcal
    val protein: Double = 0.0, // grams
    val carbs: Double = 0.0, // grams
    val fat: Double = 0.0 // grams
)

data class NutritionSummary(
    val date: String = "",
    val totalCalories: Double = 0.0,
    val totalProtein: Double = 0.0,
    val totalCarbs: Double = 0.0,
    val totalFat: Double = 0.0,
    val targetCalories: Double = 2500.0
)
