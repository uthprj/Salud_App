package com.example.salud_app.model

data class Task(
    val id: String = "",
    val userId: String = "",
    val type: String = "EAT", // EAT, SLEEP, EXERCISE
    val date: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val description: String = "",
    val isCompleted: Boolean = false
)