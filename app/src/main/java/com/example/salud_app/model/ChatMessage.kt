package com.example.salud_app.model

data class ChatMessage(
    val id: String = "",
    val content: String = "",
    val isUser: Boolean = true,
    val timestamp: Long = System.currentTimeMillis()
)

enum class SuggestionType {
    MEAL_PLAN,
    EXERCISE_PLAN,
    GENERAL_HEALTH,
    DAILY_TIPS
}

data class QuickSuggestion(
    val title: String,
    val icon: String,
    val type: SuggestionType,
    val prompt: String
)
