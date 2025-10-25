package com.example.salud_app.model

import androidx.room.PrimaryKey

data class Sleep(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val userId: String = "",
    val date: String = "",
    val sleepRecords: List<SleepRecord> = emptyList()
)