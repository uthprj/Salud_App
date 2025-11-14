package com.example.salud_app.model

import androidx.room.PrimaryKey

enum class TaskType {
    Eat,
    Sleep,
    Exercise,
}

data class Tasks(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val userId: String = "",
    val type: TaskType = TaskType.Eat,
    val date: String = "",
    val description: String = ""
)