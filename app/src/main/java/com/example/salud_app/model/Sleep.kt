package com.example.salud_app.model

data class Sleep(
    val id: String = "",
    val userId: String = "",
    val date: String = "", // Ngày bắt đầu ngủ (dd/MM/yyyy)
    val endDate: String = "", // Ngày thức dậy (dd/MM/yyyy)
    val timestamp: Long = System.currentTimeMillis(),
    val sleepType: String = "Giấc ngủ chính", // "Giấc ngủ chính", "Giấc ngủ phụ"
    val startTime: String = "", // Giờ bắt đầu ngủ (HH:mm)
    val endTime: String = "", // Giờ thức dậy (HH:mm)
    val duration: Int = 0, // Thời gian ngủ (phút)
    val quality: Int = 3, // 1-5 (1: Rất tệ, 5: Rất tốt)
    val note: String = ""
)

data class SleepSummary(
    val date: String = "",
    val totalDuration: Int = 0, // Tổng thời gian ngủ (phút)
    val averageQuality: Double = 0.0, // Đánh giá trung bình
    val totalSleeps: Int = 0, // Số lần ngủ
    val targetHours: Double = 8.0 // Mục tiêu giờ ngủ
)