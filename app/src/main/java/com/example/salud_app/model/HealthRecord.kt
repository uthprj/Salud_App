package com.example.salud_app.model

import androidx.room.PrimaryKey

data class HealthRecord(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val userId: String = "",
    val date: String = "",
    val weight: Double = 0.0, //kg
    val height: Double = 0.0, //cm
    val bloodPressure: BloodPressure? = null,
    val heartRate: Int = 0,
    val bmi: Double = weight / ( height * height ),
)