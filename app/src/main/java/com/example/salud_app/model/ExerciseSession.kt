package com.example.salud_app.model

import androidx.room.PrimaryKey

enum class ExerciseType {
    CARDIO,
    STRENGTH_TRAINING,
    FLEXIBILITY,
    BALANCE
}
data class ExerciseSession(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val enum: ExerciseType = ExerciseType.CARDIO,
    val duration: Int = 0, // in minutes
    val caloriesBurned: Int = 0
)