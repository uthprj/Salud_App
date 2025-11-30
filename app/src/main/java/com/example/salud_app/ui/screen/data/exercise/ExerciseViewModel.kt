package com.example.salud_app.ui.screen.data.exercise

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.salud_app.components.draw_chart.ChartDataPoint
import com.example.salud_app.model.Exercise
import com.example.salud_app.model.ExerciseSummary
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.time.LocalDate
import java.time.format.DateTimeFormatter

data class ExerciseUiState(
    val exerciseRecords: List<Exercise> = emptyList(),
    val todaySummary: ExerciseSummary = ExerciseSummary(),
    val chartDataPoints: List<ChartDataPoint> = emptyList(),
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val error: String? = null,
    val saveSuccess: Boolean = false
)

class ExerciseViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(ExerciseUiState())
    val uiState: StateFlow<ExerciseUiState> = _uiState.asStateFlow()

    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private val dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")

    init {
        loadTodayExercise()
    }

    /**
     * Tải dữ liệu luyện tập hôm nay
     */
    fun loadTodayExercise() {
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

                val today = LocalDate.now().format(dateFormatter)

                // Lấy tất cả records của hôm nay (không dùng orderBy để tránh cần index)
                val records = firestore.collection("User")
                    .document(currentUser.uid)
                    .collection("ExerciseRecords")
                    .whereEqualTo("date", today)
                    .get()
                    .await()
                    .documents
                    .mapNotNull { doc ->
                        doc.toObject(Exercise::class.java)?.copy(id = doc.id)
                    }
                    .sortedByDescending { it.timestamp } // Sort locally

                // Tính tổng
                val summary = ExerciseSummary(
                    date = today,
                    totalDuration = records.sumOf { it.duration },
                    totalCaloriesBurned = records.sumOf { it.caloriesBurned },
                    totalExercises = records.size
                )

                // Tính chart data points
                val chartPoints = calculateChartDataPoints()

                _uiState.value = _uiState.value.copy(
                    exerciseRecords = records,
                    todaySummary = summary,
                    chartDataPoints = chartPoints,
                    isLoading = false
                )

                Log.d("ExerciseViewModel", "Loaded ${records.size} exercise records for today")
            } catch (e: Exception) {
                Log.e("ExerciseViewModel", "Error loading exercise records", e)
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = null // Không hiển thị lỗi nếu chỉ là chưa có dữ liệu
                )
            }
        }
    }

    /**
     * Tính toán ChartDataPoints từ dữ liệu luyện tập
     */
    private suspend fun calculateChartDataPoints(): List<ChartDataPoint> {
        val currentUser = auth.currentUser ?: return emptyList()
        
        return try {
            val records = firestore.collection("User")
                .document(currentUser.uid)
                .collection("ExerciseRecords")
                .limit(100)
                .get()
                .await()
                .documents
                .mapNotNull { doc ->
                    doc.toObject(Exercise::class.java)?.copy(id = doc.id)
                }
                .sortedByDescending { it.timestamp } // Sort locally

            // Nhóm theo ngày và tính tổng calo
            records.groupBy { it.date }
                .mapNotNull { (dateStr, dailyRecords) ->
                    try {
                        val date = LocalDate.parse(dateStr, dateFormatter)
                        val totalCalories = dailyRecords.sumOf { it.caloriesBurned }
                        ChartDataPoint(date, totalCalories.toFloat())
                    } catch (e: Exception) {
                        null
                    }
                }
                .sortedBy { it.date }
        } catch (e: Exception) {
            Log.e("ExerciseViewModel", "Error calculating chart data", e)
            emptyList()
        }
    }

    /**
     * Lưu bài tập lên Firebase
     */
    fun saveExercise(
        date: LocalDate,
        time: String,
        exerciseType: String,
        exerciseName: String,
        duration: Int,
        caloriesBurned: Double,
        distance: Double = 0.0,
        note: String = ""
    ) {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isSaving = true, error = null, saveSuccess = false)

                val currentUser = auth.currentUser
                if (currentUser == null) {
                    _uiState.value = _uiState.value.copy(
                        isSaving = false,
                        error = "Chưa đăng nhập"
                    )
                    return@launch
                }

                val dateString = date.format(dateFormatter)
                val record = Exercise(
                    userId = currentUser.uid,
                    date = dateString,
                    time = time,
                    timestamp = System.currentTimeMillis(),
                    exerciseType = exerciseType,
                    exerciseName = exerciseName,
                    duration = duration,
                    caloriesBurned = caloriesBurned,
                    distance = distance,
                    note = note
                )

                // Gửi dữ liệu lên Firestore
                firestore.collection("User")
                    .document(currentUser.uid)
                    .collection("ExerciseRecords")
                    .add(record)
                    .await()

                Log.d("ExerciseViewModel", "Saved exercise record: $exerciseName")

                _uiState.value = _uiState.value.copy(
                    isSaving = false,
                    saveSuccess = true
                )

                // Tải lại dữ liệu sau khi lưu
                loadTodayExercise()

            } catch (e: Exception) {
                Log.e("ExerciseViewModel", "Error saving exercise", e)
                _uiState.value = _uiState.value.copy(
                    isSaving = false,
                    error = "Lỗi lưu dữ liệu"
                )
            }
        }
    }

    /**
     * Xóa một record luyện tập
     */
    fun deleteExercise(recordId: String) {
        viewModelScope.launch {
            try {
                val currentUser = auth.currentUser ?: return@launch

                firestore.collection("User")
                    .document(currentUser.uid)
                    .collection("ExerciseRecords")
                    .document(recordId)
                    .delete()
                    .await()

                Log.d("ExerciseViewModel", "Deleted exercise record: $recordId")
                loadTodayExercise()
            } catch (e: Exception) {
                Log.e("ExerciseViewModel", "Error deleting exercise", e)
                _uiState.value = _uiState.value.copy(
                    error = "Lỗi xóa dữ liệu"
                )
            }
        }
    }

    /**
     * Ước tính calo dựa trên loại bài tập và thời gian
     */
    fun estimateCalories(exerciseType: String, duration: Int): Double {
        // Calo/phút trung bình cho mỗi loại bài tập
        val caloriesPerMinute = when (exerciseType) {
            "Chạy bộ" -> 10.0
            "Đạp xe" -> 8.0
            "Bơi lội" -> 9.0
            "Gym" -> 7.0
            "Yoga" -> 4.0
            "Đi bộ" -> 5.0
            "HIIT" -> 12.0
            "Nhảy dây" -> 11.0
            "Leo núi" -> 9.0
            "Cầu lông" -> 7.0
            "Bóng đá" -> 9.0
            "Bóng rổ" -> 8.0
            else -> 6.0
        }
        return caloriesPerMinute * duration
    }

    fun clearSaveSuccess() {
        _uiState.value = _uiState.value.copy(saveSuccess = false)
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}