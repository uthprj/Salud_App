package com.example.salud_app.model

data class Exercise(
    val id: String = "",
    val userId: String = "",
    val date: String = "",
    val time: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val exerciseType: String = "", // "Chạy bộ", "Đạp xe", "Bơi lội", "Gym", "Yoga", "Đi bộ", "Khác"
    val exerciseName: String = "",
    val duration: Int = 0, // phút
    val caloriesBurned: Double = 0.0, // kcal
    val distance: Double = 0.0, // km (optional)
    val note: String = ""
)

data class ExerciseSummary(
    val date: String = "",
    val totalDuration: Int = 0, // phút
    val totalCaloriesBurned: Double = 0.0,
    val totalExercises: Int = 0,
    val targetCalories: Double = 500.0 // mục tiêu đốt calo mỗi ngày
)