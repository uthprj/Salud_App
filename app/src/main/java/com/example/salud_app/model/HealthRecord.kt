package com.example.salud_app.model

data class HealthRecord(
    val id: String = "",
    val userId: String = "",
    val date: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val weight: Double = 0.0, // kg
    val height: Double = 0.0, // cm
    val systolic: Long = 0, // Tâm thu
    val diastolic: Long = 0, // Tâm trương
    val heartRate: Long = 0,
    val bmi: Double = 0.0,
    val note: String = ""
)