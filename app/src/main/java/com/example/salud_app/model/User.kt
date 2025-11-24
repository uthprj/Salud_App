package com.example.salud_app.model

data class User(
    val username: String = "",
    val password: String = "",
    val fullName: String = "",
    val birthDate: String = "",
    val gender: String = "MALE",
    val numPhone: String = "",
    val email: String = "",
    val photoUrl: String = ""
)