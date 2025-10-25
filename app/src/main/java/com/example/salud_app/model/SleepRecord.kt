package com.example.salud_app.model

enum class SleepQuality {
    EXCELLENT,
    GOOD,
    NORMAL,
    FAIR,
    POOR
}
data class SleepRecord(
    val startTime: String = "",
    val endTime: String = "",
    val enum: SleepQuality = SleepQuality.NORMAL
)