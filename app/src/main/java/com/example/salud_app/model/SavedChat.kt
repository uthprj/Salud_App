package com.example.salud_app.model

data class SavedChat(
    val id: String = "",
    val name: String = "",
    val messages: List<ChatMessage> = emptyList(),
    val timestamp: Long = System.currentTimeMillis(),
    val preview: String = "" // Preview of first AI response
)
