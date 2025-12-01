package com.example.salud_app.model

data class LongTermGoal(
    val targetWeight: Double = 0.0,
    val targetHeight: Double = 0.0
)

data class DailyGoal(
    val date: String = "",
    val caloriesIn: Double = 0.0,
    val caloriesInTarget: Double = 2000.0,
    val caloriesOut: Double = 0.0,
    val caloriesOutTarget: Double = 500.0,
    val sleepMinutes: Int = 0,
    val sleepTarget: Int = 480,
    val exerciseMinutes: Int = 0,
    val exerciseTarget: Int = 30,
    val steps: Int = 0,
    val stepsTarget: Int = 10000
)

data class Goal(
    val longTermGoal: LongTermGoal = LongTermGoal(),
    val dailyGoal: DailyGoal = DailyGoal()
)
