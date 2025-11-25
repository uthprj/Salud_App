package com.example.salud_app.model

data class HealthRecord(
    val date: String = "",
    val weight: Double = 0.0, //kg
    val height: Double = 0.0, //cm
    val systolic: Int = 0, // Tâm thu
    val diastolic: Int = 0, // Tâm trương
    val heartRate: Int = 0,
    val bmi: Int = 0,
    val note : String? = ""
)