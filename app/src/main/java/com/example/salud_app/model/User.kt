package com.example.salud_app.model

import androidx.room.PrimaryKey

enum class Gender {
    MALE,
    FEMALE,
    OTHER
}



data class User(
    @PrimaryKey(autoGenerate = true)
    val userId: Long = 0,
    val username: String = "",
    val password: String = "",
    val fullName: String = "",
    val birthDate: String = "",
    val gender: Gender = Gender.MALE,
    val numPhone: String = "",
    val email: String = "",
)