package com.example.salud_app.model

import com.example.salud_app.model.*
data class User(
    val userId: String = "",
    val fullName: String = "",
    val birthDate: String = "",
    val gender: String = "MALE",
    val numPhone: String = "",
    val email: String = "",
    val photoUrl: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val healthRecord: HealthRecord = HealthRecord(),
    val nutrition: Nutrition = Nutrition(),
    val sleep: Sleep = Sleep(),
    val exercise: Exercise = Exercise(),
    val task: Task = Task()
)