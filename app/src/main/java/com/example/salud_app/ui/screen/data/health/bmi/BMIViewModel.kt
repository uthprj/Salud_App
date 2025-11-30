package com.example.salud_app.ui.screen.data.health.bmi

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.salud_app.components.draw_chart.ChartDataPoint
import com.example.salud_app.model.HealthRecord
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.time.LocalDate
import java.time.format.DateTimeFormatter

data class BMIUiState(
    val bmiRecords: List<HealthRecord> = emptyList(),
    val currentBMI: Double = 0.0,
    val latestWeight: Double = 0.0,
    val latestHeight: Double = 0.0,
    val bmiCategory: String = "",
    val isLoading: Boolean = false,
    val error: String? = null
)

class BMIViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(BMIUiState())
    val uiState: StateFlow<BMIUiState> = _uiState.asStateFlow()

    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private val dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")

    init {
        loadBMIData()
    }

    /**
     * Tải dữ liệu BMI từ Firebase
     */
    fun loadBMIData() {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isLoading = true, error = null)

                val currentUser = auth.currentUser
                if (currentUser == null) {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = "Chưa đăng nhập"
                    )
                    return@launch
                }

                // Lấy tất cả records có cả weight và height
                val records = firestore.collection("User")
                    .document(currentUser.uid)
                    .collection("HealthRecords")
                    .get()
                    .await()
                    .documents
                    .mapNotNull { doc ->
                        doc.toObject(HealthRecord::class.java)?.copy(id = doc.id)
                    }
                    .filter { it.weight > 0 && it.height > 0 } // Chỉ lấy record có cả weight và height
                    .sortedByDescending { it.timestamp }

                // Lấy weight và height mới nhất (có thể từ các record khác nhau)
                val allRecords = firestore.collection("User")
                    .document(currentUser.uid)
                    .collection("HealthRecords")
                    .get()
                    .await()
                    .documents
                    .mapNotNull { doc ->
                        doc.toObject(HealthRecord::class.java)?.copy(id = doc.id)
                    }

                val latestWeight = allRecords
                    .filter { it.weight > 0 }
                    .maxByOrNull { it.timestamp }
                    ?.weight ?: 0.0

                val latestHeight = allRecords
                    .filter { it.height > 0 }
                    .maxByOrNull { it.timestamp }
                    ?.height ?: 0.0

                // Tính BMI hiện tại
                val currentBMI = calculateBMI(latestWeight, latestHeight)
                val category = getBMICategory(currentBMI)

                _uiState.value = _uiState.value.copy(
                    bmiRecords = records,
                    currentBMI = currentBMI,
                    latestWeight = latestWeight,
                    latestHeight = latestHeight,
                    bmiCategory = category,
                    isLoading = false
                )

                Log.d("BMIViewModel", "Loaded BMI data: $currentBMI ($category)")
            } catch (e: Exception) {
                Log.e("BMIViewModel", "Error loading BMI data", e)
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "Lỗi tải dữ liệu"
                )
            }
        }
    }

    /**
     * Tính BMI từ cân nặng (kg) và chiều cao (cm)
     */
    private fun calculateBMI(weight: Double, height: Double): Double {
        if (weight <= 0 || height <= 0) return 0.0
        val heightInMeters = height / 100.0
        return weight / (heightInMeters * heightInMeters)
    }

    /**
     * Phân loại BMI
     */
    private fun getBMICategory(bmi: Double): String {
        return when {
            bmi <= 0 -> "Chưa có dữ liệu"
            bmi < 18.5 -> "Thiếu cân"
            bmi < 25 -> "Bình thường"
            bmi < 30 -> "Thừa cân"
            bmi < 35 -> "Béo phì độ 1"
            bmi < 40 -> "Béo phì độ 2"
            else -> "Béo phì độ 3"
        }
    }

    /**
     * Lấy màu theo BMI
     */
    fun getBMIColor(bmi: Double): androidx.compose.ui.graphics.Color {
        return when {
            bmi <= 0 -> androidx.compose.ui.graphics.Color.Gray
            bmi < 18.5 -> androidx.compose.ui.graphics.Color(0xFF5DADE2) // Xanh dương - Thiếu cân
            bmi < 25 -> androidx.compose.ui.graphics.Color(0xFF2ECC71) // Xanh lá - Bình thường
            bmi < 30 -> androidx.compose.ui.graphics.Color(0xFFF39C12) // Cam - Thừa cân
            bmi < 35 -> androidx.compose.ui.graphics.Color(0xFFE74C3C) // Đỏ nhạt - Béo phì độ 1
            bmi < 40 -> androidx.compose.ui.graphics.Color(0xFFC0392B) // Đỏ đậm - Béo phì độ 2
            else -> androidx.compose.ui.graphics.Color(0xFF8E44AD) // Tím - Béo phì độ 3
        }
    }

    /**
     * Chuyển đổi thành ChartDataPoint để vẽ biểu đồ
     */
    fun getChartDataPoints(): List<ChartDataPoint> {
        return _uiState.value.bmiRecords.mapNotNull { record ->
            try {
                val date = LocalDate.parse(record.date, dateFormatter)
                val bmi = calculateBMI(record.weight, record.height)
                if (bmi > 0) ChartDataPoint(date, bmi.toFloat()) else null
            } catch (e: Exception) {
                null
            }
        }.sortedBy { it.date }
    }

    /**
     * Xóa thông báo lỗi
     */
    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}