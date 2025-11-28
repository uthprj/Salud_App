package com.example.salud_app.model

data class Exercise(
    val id: String = "",
    val userId: String = "",
    val date: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val type: String = "", // CARDIO, STRENGTH_TRAINING, FLEXIBILITY, BALANCE
    val duration: Long = 0, // in minutes
    val caloriesBurned: Long = 0,
    val steps: Long = 0,
    val distance: Double = 0.0 // km
)