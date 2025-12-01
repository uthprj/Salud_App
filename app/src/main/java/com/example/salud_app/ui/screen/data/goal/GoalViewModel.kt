package com.example.salud_app.ui.screen.data.goal

import android.content.Context
import android.content.SharedPreferences
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.salud_app.model.DailyGoal
import com.example.salud_app.model.Goal
import com.example.salud_app.model.LongTermGoal
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.time.LocalDate
import java.time.format.DateTimeFormatter

data class GoalUiState(
    val goal: Goal = Goal(),
    val currentWeight: Double = 0.0,
    val currentHeight: Double = 0.0,
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val error: String? = null,
    val saveSuccess: Boolean = false
)

class GoalViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(GoalUiState())
    val uiState: StateFlow<GoalUiState> = _uiState.asStateFlow()

    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private val dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")

    private var sharedPreferences: SharedPreferences? = null

    fun initialize(context: Context) {
        sharedPreferences = context.getSharedPreferences("goal_prefs", Context.MODE_PRIVATE)
        loadGoals()
        loadCurrentHealthData()
    }

    /**
     * Load mục tiêu từ SharedPreferences
     */
    private fun loadGoals() {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isLoading = true, error = null)

                val prefs = sharedPreferences ?: return@launch

                val today = LocalDate.now().format(dateFormatter)
                val savedDate = prefs.getString("daily_date", "") ?: ""

                // Lấy mục tiêu dài hạn
                val longTermGoal = LongTermGoal(
                    targetWeight = prefs.getFloat("target_weight", 0f).toDouble(),
                    targetHeight = prefs.getFloat("target_height", 0f).toDouble()
                )

                // Lấy mục tiêu hàng ngày - reset nếu ngày mới
                val dailyGoal = if (savedDate == today) {
                    DailyGoal(
                        date = today,
                        caloriesIn = prefs.getFloat("calories_in", 0f).toDouble(),
                        caloriesInTarget = prefs.getFloat("calories_in_target", 2000f).toDouble(),
                        caloriesOut = prefs.getFloat("calories_out", 0f).toDouble(),
                        caloriesOutTarget = prefs.getFloat("calories_out_target", 500f).toDouble(),
                        sleepMinutes = prefs.getInt("sleep_minutes", 0),
                        sleepTarget = prefs.getInt("sleep_target", 480),
                        exerciseMinutes = prefs.getInt("exercise_minutes", 0),
                        exerciseTarget = prefs.getInt("exercise_target", 30),
                        steps = prefs.getInt("steps", 0),
                        stepsTarget = prefs.getInt("steps_target", 10000)
                    )
                } else {
                    // Reset daily goals cho ngày mới nhưng giữ lại targets
                    val newDailyGoal = DailyGoal(
                        date = today,
                        caloriesIn = 0.0,
                        caloriesInTarget = prefs.getFloat("calories_in_target", 2000f).toDouble(),
                        caloriesOut = 0.0,
                        caloriesOutTarget = prefs.getFloat("calories_out_target", 500f).toDouble(),
                        sleepMinutes = 0,
                        sleepTarget = prefs.getInt("sleep_target", 480),
                        exerciseMinutes = 0,
                        exerciseTarget = prefs.getInt("exercise_target", 30),
                        steps = 0,
                        stepsTarget = prefs.getInt("steps_target", 10000)
                    )
                    // Save new date
                    prefs.edit().putString("daily_date", today).apply()
                    newDailyGoal
                }

                _uiState.value = _uiState.value.copy(
                    goal = Goal(longTermGoal, dailyGoal),
                    isLoading = false
                )

                // Load dữ liệu thực tế từ Firebase
                loadTodayActualData()

            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "Lỗi khi tải mục tiêu: ${e.message}"
                )
            }
        }
    }

    /**
     * Load dữ liệu cân nặng và chiều cao hiện tại từ Firebase
     */
    private fun loadCurrentHealthData() {
        viewModelScope.launch {
            try {
                val currentUser = auth.currentUser ?: return@launch

                // Lấy cân nặng mới nhất
                val weightDoc = firestore.collection("User")
                    .document(currentUser.uid)
                    .collection("weight")
                    .orderBy("timestamp", com.google.firebase.firestore.Query.Direction.DESCENDING)
                    .limit(1)
                    .get()
                    .await()

                val currentWeight = if (!weightDoc.isEmpty) {
                    weightDoc.documents.first().getDouble("value") ?: 0.0
                } else 0.0

                // Lấy chiều cao mới nhất
                val heightDoc = firestore.collection("User")
                    .document(currentUser.uid)
                    .collection("height")
                    .orderBy("timestamp", com.google.firebase.firestore.Query.Direction.DESCENDING)
                    .limit(1)
                    .get()
                    .await()

                val currentHeight = if (!heightDoc.isEmpty) {
                    heightDoc.documents.first().getDouble("value") ?: 0.0
                } else 0.0

                _uiState.value = _uiState.value.copy(
                    currentWeight = currentWeight,
                    currentHeight = currentHeight
                )

            } catch (e: Exception) {
                // Ignore error for health data
            }
        }
    }

    /**
     * Load dữ liệu thực tế hôm nay từ Firebase (dinh dưỡng, luyện tập, giấc ngủ)
     */
    private fun loadTodayActualData() {
        viewModelScope.launch {
            try {
                val currentUser = auth.currentUser ?: return@launch
                val today = LocalDate.now().format(dateFormatter)

                // Lấy tổng calo nạp hôm nay từ NutritionRecords
                var totalCaloriesIn = 0.0
                val nutritionDocs = firestore.collection("User")
                    .document(currentUser.uid)
                    .collection("NutritionRecords")
                    .whereEqualTo("date", today)
                    .get()
                    .await()
                
                for (doc in nutritionDocs) {
                    totalCaloriesIn += doc.getDouble("calories") ?: 0.0
                }

                // Lấy tổng calo tiêu hao và thời gian tập hôm nay từ ExerciseRecords
                var totalCaloriesOut = 0.0
                var totalExerciseMinutes = 0
                val exerciseDocs = firestore.collection("User")
                    .document(currentUser.uid)
                    .collection("ExerciseRecords")
                    .whereEqualTo("date", today)
                    .get()
                    .await()

                for (doc in exerciseDocs) {
                    totalCaloriesOut += doc.getDouble("calories") ?: 0.0
                    totalExerciseMinutes += (doc.getLong("duration")?.toInt() ?: 0)
                }

                // Lấy tổng thời gian ngủ hôm nay từ SleepRecords
                var totalSleepMinutes = 0
                val sleepDocs = firestore.collection("User")
                    .document(currentUser.uid)
                    .collection("SleepRecords")
                    .whereEqualTo("date", today)
                    .get()
                    .await()

                for (doc in sleepDocs) {
                    totalSleepMinutes += (doc.getLong("duration")?.toInt() ?: 0)
                }

                // Cập nhật UI state với dữ liệu thực tế
                val currentGoal = _uiState.value.goal
                val updatedDailyGoal = currentGoal.dailyGoal.copy(
                    caloriesIn = totalCaloriesIn,
                    caloriesOut = totalCaloriesOut,
                    exerciseMinutes = totalExerciseMinutes,
                    sleepMinutes = totalSleepMinutes
                )

                _uiState.value = _uiState.value.copy(
                    goal = currentGoal.copy(dailyGoal = updatedDailyGoal)
                )

                // Lưu vào SharedPreferences
                saveActualDataToPrefs(totalCaloriesIn, totalCaloriesOut, totalExerciseMinutes, totalSleepMinutes)

            } catch (e: Exception) {
                // Ignore errors for actual data loading
            }
        }
    }

    private fun saveActualDataToPrefs(
        caloriesIn: Double,
        caloriesOut: Double,
        exerciseMinutes: Int,
        sleepMinutes: Int
    ) {
        sharedPreferences?.edit()?.apply {
            putFloat("calories_in", caloriesIn.toFloat())
            putFloat("calories_out", caloriesOut.toFloat())
            putInt("exercise_minutes", exerciseMinutes)
            putInt("sleep_minutes", sleepMinutes)
            apply()
        }
    }

    /**
     * Lưu mục tiêu dài hạn
     */
    fun saveLongTermGoal(targetWeight: Double, targetHeight: Double) {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isSaving = true, error = null, saveSuccess = false)

                sharedPreferences?.edit()?.apply {
                    putFloat("target_weight", targetWeight.toFloat())
                    putFloat("target_height", targetHeight.toFloat())
                    apply()
                }

                val currentGoal = _uiState.value.goal
                _uiState.value = _uiState.value.copy(
                    goal = currentGoal.copy(
                        longTermGoal = LongTermGoal(targetWeight, targetHeight)
                    ),
                    isSaving = false,
                    saveSuccess = true
                )

            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isSaving = false,
                    error = "Lỗi khi lưu mục tiêu: ${e.message}"
                )
            }
        }
    }

    /**
     * Lưu mục tiêu hàng ngày (targets)
     */
    fun saveDailyTargets(
        caloriesInTarget: Double,
        caloriesOutTarget: Double,
        sleepTarget: Int,
        exerciseTarget: Int,
        stepsTarget: Int
    ) {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isSaving = true, error = null, saveSuccess = false)

                sharedPreferences?.edit()?.apply {
                    putFloat("calories_in_target", caloriesInTarget.toFloat())
                    putFloat("calories_out_target", caloriesOutTarget.toFloat())
                    putInt("sleep_target", sleepTarget)
                    putInt("exercise_target", exerciseTarget)
                    putInt("steps_target", stepsTarget)
                    apply()
                }

                val currentGoal = _uiState.value.goal
                _uiState.value = _uiState.value.copy(
                    goal = currentGoal.copy(
                        dailyGoal = currentGoal.dailyGoal.copy(
                            caloriesInTarget = caloriesInTarget,
                            caloriesOutTarget = caloriesOutTarget,
                            sleepTarget = sleepTarget,
                            exerciseTarget = exerciseTarget,
                            stepsTarget = stepsTarget
                        )
                    ),
                    isSaving = false,
                    saveSuccess = true
                )

            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isSaving = false,
                    error = "Lỗi khi lưu mục tiêu: ${e.message}"
                )
            }
        }
    }

    /**
     * Cập nhật bước chân (thủ công)
     */
    fun updateSteps(steps: Int) {
        viewModelScope.launch {
            sharedPreferences?.edit()?.putInt("steps", steps)?.apply()
            
            val currentGoal = _uiState.value.goal
            _uiState.value = _uiState.value.copy(
                goal = currentGoal.copy(
                    dailyGoal = currentGoal.dailyGoal.copy(steps = steps)
                )
            )
        }
    }

    fun clearSaveSuccess() {
        _uiState.value = _uiState.value.copy(saveSuccess = false)
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }

    fun refresh() {
        loadGoals()
        loadCurrentHealthData()
    }
}