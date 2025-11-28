package com.example.salud_app.model

data class Sleep(
    val id: String = "",
    val userId: String = "",
    val date: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val startTime: String = "",
    val endTime: String = "",
    val duration: Long = 0, // in minutes
    val quality: String = "NORMAL" // EXCELLENT, GOOD, NORMAL, FAIR, POOR
)