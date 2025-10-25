package com.example.salud_app.model

import androidx.room.PrimaryKey

data class Exercise(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val userId: String = "",
    val date: String = "",
    val sessions: List<ExerciseSession> = emptyList()
)