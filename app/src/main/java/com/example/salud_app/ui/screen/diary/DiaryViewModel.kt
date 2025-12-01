package com.example.salud_app.ui.screen.diary

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.salud_app.R
import com.example.salud_app.model.Exercise
import com.example.salud_app.model.Nutrition
import com.example.salud_app.model.Sleep
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Composable
fun ExerciseIcon() {
    Image(
        painter = painterResource(id = R.drawable.exercise_24px),
        contentDescription = "Exercise Icon",
        colorFilter = ColorFilter.tint(Color(0xFF4CAF50)) // Xanh lá
    )
}

@Composable
fun SleepIcon() {
    Image(
        painter = painterResource(id = R.drawable.bedtime_24px),
        contentDescription = "Sleep Icon",
        colorFilter = ColorFilter.tint(Color(0xFF1E88E5)) // Xanh dương
    )
}

@Composable
fun EatIcon() {
    Image(
        painter = painterResource(id = R.drawable.fork_spoon_24px),
        contentDescription = "Eat Icon",
        colorFilter = ColorFilter.tint(Color(0xFFFB8C00)) // Cam
    )
}

data class DiaryEntry(
    val id: String = "",
    val type: String = "", // "NUTRITION", "SLEEP", "EXERCISE"
    val title: String = "",
    val description: String = "",
    val time: String = "",
    val date: String = "",
    val timestamp: Long = 0L,
    // Chi tiết thêm
    val calories: Double = 0.0,
    val duration: Int = 0, // phút
    val extraInfo: String = ""
)

data class DiaryUiState(
    val entries: List<DiaryEntry> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

class DiaryViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(DiaryUiState())
    val uiState: StateFlow<DiaryUiState> = _uiState.asStateFlow()

    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private val dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")

    /**
     * Load tất cả dữ liệu của ngày được chọn
     */
    fun loadDiaryForDate(date: LocalDate) {
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

                val dateString = date.format(dateFormatter)
                val entries = mutableListOf<DiaryEntry>()

                // 1. Lấy dữ liệu Nutrition
                val nutritionDocs = firestore.collection("User")
                    .document(currentUser.uid)
                    .collection("NutritionRecords")
                    .whereEqualTo("date", dateString)
                    .get()
                    .await()

                for (doc in nutritionDocs) {
                    val nutrition = doc.toObject(Nutrition::class.java)
                    entries.add(
                        DiaryEntry(
                            id = doc.id,
                            type = "NUTRITION",
                            title = "${nutrition.mealCategory}",
                            description = nutrition.mealName,
                            time = nutrition.time,
                            date = nutrition.date,
                            timestamp = nutrition.timestamp,
                            calories = nutrition.calories,
                            extraInfo = "Protein: ${nutrition.protein.toInt()}g | Carbs: ${nutrition.carbs.toInt()}g | Fat: ${nutrition.fat.toInt()}g"
                        )
                    )
                }

                // 2. Lấy dữ liệu Exercise
                val exerciseDocs = firestore.collection("User")
                    .document(currentUser.uid)
                    .collection("ExerciseRecords")
                    .whereEqualTo("date", dateString)
                    .get()
                    .await()

                for (doc in exerciseDocs) {
                    val exercise = doc.toObject(Exercise::class.java)
                    entries.add(
                        DiaryEntry(
                            id = doc.id,
                            type = "EXERCISE",
                            title = "${exercise.exerciseType}",
                            description = exercise.exerciseName.ifEmpty { exercise.exerciseType },
                            time = exercise.time,
                            date = exercise.date,
                            timestamp = exercise.timestamp,
                            calories = exercise.caloriesBurned,
                            duration = exercise.duration,
                            extraInfo = if (exercise.distance > 0) "Khoảng cách: ${exercise.distance} km" else ""
                        )
                    )
                }

                // 3. Lấy dữ liệu Sleep
                val sleepDocs = firestore.collection("User")
                    .document(currentUser.uid)
                    .collection("SleepRecords")
                    .whereEqualTo("date", dateString)
                    .get()
                    .await()

                for (doc in sleepDocs) {
                    val sleep = doc.toObject(Sleep::class.java)
                    val hours = sleep.duration / 60
                    val minutes = sleep.duration % 60
                    val qualityText = when (sleep.quality) {
                        1 -> "Rất tệ"
                        2 -> "Tệ"
                        3 -> "Bình thường"
                        4 -> "Tốt"
                        5 -> "Rất tốt"
                        else -> ""
                    }
                    entries.add(
                        DiaryEntry(
                            id = doc.id,
                            type = "SLEEP",
                            title = "${sleep.sleepType}",
                            description = "${sleep.startTime} - ${sleep.endTime}",
                            time = sleep.startTime,
                            date = sleep.date,
                            timestamp = sleep.timestamp,
                            duration = sleep.duration,
                            extraInfo = "Thời gian: ${hours}h ${minutes}m | Chất lượng: $qualityText"
                        )
                    )
                }

                // Sắp xếp theo thời gian (mới nhất trước)
                val sortedEntries = entries.sortedByDescending { it.timestamp }

                _uiState.value = _uiState.value.copy(
                    entries = sortedEntries,
                    isLoading = false
                )

                Log.d("DiaryViewModel", "Loaded ${sortedEntries.size} entries for $dateString")

            } catch (e: Exception) {
                Log.e("DiaryViewModel", "Error loading diary", e)
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "Lỗi tải dữ liệu: ${e.message}"
                )
            }
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}
