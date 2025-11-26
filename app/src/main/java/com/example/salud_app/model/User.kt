package com.example.salud_app.model

data class User(
    val userId: String = "",
    val fullName: String = "",
    val birthDate: String = "",
    val gender: String = "MALE",
    val numPhone: String = "",
    val email: String = "",
    val photoUrl: String = "",
    val weight: String = "",
    val height: String = ""
)