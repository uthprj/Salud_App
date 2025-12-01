package com.example.salud_app.ui.screen.home

import android.content.Context
import android.content.SharedPreferences
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.salud_app.components.step_counter.StepCounterManager
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

data class HomeIndicator(
    val current: Double = 0.0,
    val target: Double = 1.0,
    val unit: String = ""
) {
    val progress: Float
        get() = if (target > 0) (current / target).coerceIn(0.0, 1.0).toFloat() else 0f
    
    val percentage: Int
        get() = (progress * 100).toInt()
    
    val displayCurrent: String
        get() = if (current == current.toLong().toDouble()) current.toLong().toString() else String.format("%.1f", current)
    
    val displayTarget: String
        get() = if (target == target.toLong().toDouble()) target.toLong().toString() else String.format("%.1f", target)
}

// Enum cho loại cảnh báo
enum class WarningType {
    INFO,      // Thông tin (xanh dương)
    WARNING,   // Cảnh báo (cam)
    DANGER     // Nguy hiểm (đỏ)
}

// Data class cho cảnh báo
data class HealthWarning(
    val title: String,
    val message: String,
    val type: WarningType,
    val value: String = ""
)

data class HomeUiState(
    val steps: HomeIndicator = HomeIndicator(target = 10000.0, unit = "bước"),
    val caloriesIn: HomeIndicator = HomeIndicator(target = 2000.0, unit = "kcal"),
    val caloriesOut: HomeIndicator = HomeIndicator(target = 500.0, unit = "kcal"),
    val sleepMinutes: HomeIndicator = HomeIndicator(target = 480.0, unit = "phút"),
    val exerciseMinutes: HomeIndicator = HomeIndicator(target = 30.0, unit = "phút"),
    val warnings: List<HealthWarning> = emptyList(),
    val bmi: Double = 0.0,
    val weight: Double = 0.0,
    val height: Double = 0.0,
    val isLoading: Boolean = false,
    val error: String? = null
)

class HomeViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")

    private var sharedPreferences: SharedPreferences? = null
    private var stepCounterManager: StepCounterManager? = null

    fun initialize(context: Context) {
        sharedPreferences = context.getSharedPreferences("goal_prefs", Context.MODE_PRIVATE)
        stepCounterManager = StepCounterManager.getInstance(context)
        
        // Bắt đầu đếm bước chân
        stepCounterManager?.startCounting()
        
        // Load dữ liệu
        loadHomeData()
        
        // Theo dõi số bước từ sensor
        viewModelScope.launch {
            stepCounterManager?.dailySteps?.collect { steps ->
                val currentState = _uiState.value
                _uiState.value = currentState.copy(
                    steps = currentState.steps.copy(current = steps.toDouble())
                )
            }
        }
    }

    fun loadHomeData() {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isLoading = true, error = null)
                
                val prefs = sharedPreferences ?: return@launch
                val today = LocalDate.now().format(dateFormatter)
                
                // Load targets từ SharedPreferences
                val stepsTarget = prefs.getInt("steps_target", 10000)
                val caloriesInTarget = prefs.getFloat("calories_in_target", 2000f).toDouble()
                val caloriesOutTarget = prefs.getFloat("calories_out_target", 500f).toDouble()
                val sleepTarget = prefs.getInt("sleep_target", 480)
                val exerciseTarget = prefs.getInt("exercise_target", 30)
                
                // Load current steps
                val currentSteps = stepCounterManager?.getCurrentSteps() ?: 0
                
                // Load dữ liệu thực tế từ Firebase
                val currentUser = auth.currentUser
                var totalCaloriesIn = 0.0
                var totalCaloriesOut = 0.0
                var totalSleepMinutes = 0
                var totalExerciseMinutes = 0
                var currentWeight = 0.0
                var currentHeight = 0.0
                
                if (currentUser != null) {
                    // Load Nutrition data
                    try {
                        val nutritionDocs = firestore.collection("User")
                            .document(currentUser.uid)
                            .collection("NutritionRecords")
                            .whereEqualTo("date", today)
                            .get()
                            .await()
                        
                        for (doc in nutritionDocs) {
                            totalCaloriesIn += doc.getDouble("calories") ?: 0.0
                        }
                    } catch (e: Exception) {
                        // Ignore
                    }
                    
                    // Load Exercise data
                    try {
                        val exerciseDocs = firestore.collection("User")
                            .document(currentUser.uid)
                            .collection("ExerciseRecords")
                            .whereEqualTo("date", today)
                            .get()
                            .await()
                        
                        for (doc in exerciseDocs) {
                            totalCaloriesOut += doc.getDouble("caloriesBurned") ?: 0.0
                            totalExerciseMinutes += (doc.getLong("duration")?.toInt() ?: 0)
                        }
                    } catch (e: Exception) {
                        // Ignore
                    }
                    
                    // Load Sleep data
                    try {
                        val sleepDocs = firestore.collection("User")
                            .document(currentUser.uid)
                            .collection("SleepRecords")
                            .whereEqualTo("date", today)
                            .get()
                            .await()
                        
                        for (doc in sleepDocs) {
                            totalSleepMinutes += (doc.getLong("duration")?.toInt() ?: 0)
                        }
                    } catch (e: Exception) {
                        // Ignore
                    }
                    
                    // Load Health data - Weight (lấy record mới nhất có weight > 0)
                    try {
                        val weightDocs = firestore.collection("User")
                            .document(currentUser.uid)
                            .collection("HealthRecords")
                            .whereGreaterThan("weight", 0)
                            .orderBy("weight")
                            .orderBy("timestamp", Query.Direction.DESCENDING)
                            .limit(10)
                            .get()
                            .await()
                        
                        if (!weightDocs.isEmpty) {
                            // Tìm record mới nhất có weight > 0
                            val latestWeight = weightDocs.documents
                                .mapNotNull { doc -> 
                                    val w = doc.getDouble("weight") ?: 0.0
                                    val ts = doc.getLong("timestamp") ?: 0L
                                    if (w > 0) Pair(w, ts) else null
                                }
                                .maxByOrNull { it.second }
                            
                            currentWeight = latestWeight?.first ?: 0.0
                        }
                    } catch (e: Exception) {
                        // Fallback: query đơn giản hơn
                        try {
                            val allHealthDocs = firestore.collection("User")
                                .document(currentUser.uid)
                                .collection("HealthRecords")
                                .get()
                                .await()
                            
                            val latestWeight = allHealthDocs.documents
                                .mapNotNull { doc ->
                                    val w = doc.getDouble("weight") ?: 0.0
                                    val ts = doc.getLong("timestamp") ?: 0L
                                    if (w > 0) Pair(w, ts) else null
                                }
                                .maxByOrNull { it.second }
                            
                            currentWeight = latestWeight?.first ?: 0.0
                        } catch (e2: Exception) {
                            // Ignore
                        }
                    }
                    
                    // Load Health data - Height (lấy record mới nhất có height > 0)
                    try {
                        val heightDocs = firestore.collection("User")
                            .document(currentUser.uid)
                            .collection("HealthRecords")
                            .whereGreaterThan("height", 0)
                            .orderBy("height")
                            .orderBy("timestamp", Query.Direction.DESCENDING)
                            .limit(10)
                            .get()
                            .await()
                        
                        if (!heightDocs.isEmpty) {
                            val latestHeight = heightDocs.documents
                                .mapNotNull { doc ->
                                    val h = doc.getDouble("height") ?: 0.0
                                    val ts = doc.getLong("timestamp") ?: 0L
                                    if (h > 0) Pair(h, ts) else null
                                }
                                .maxByOrNull { it.second }
                            
                            currentHeight = latestHeight?.first ?: 0.0
                        }
                    } catch (e: Exception) {
                        // Fallback: query đơn giản hơn
                        try {
                            val allHealthDocs = firestore.collection("User")
                                .document(currentUser.uid)
                                .collection("HealthRecords")
                                .get()
                                .await()
                            
                            val latestHeight = allHealthDocs.documents
                                .mapNotNull { doc ->
                                    val h = doc.getDouble("height") ?: 0.0
                                    val ts = doc.getLong("timestamp") ?: 0L
                                    if (h > 0) Pair(h, ts) else null
                                }
                                .maxByOrNull { it.second }
                            
                            currentHeight = latestHeight?.first ?: 0.0
                        } catch (e2: Exception) {
                            // Ignore
                        }
                    }
                }
                
                // Tính BMI và tạo cảnh báo
                val bmi = if (currentHeight > 0 && currentWeight > 0) {
                    val heightInMeters = currentHeight / 100
                    currentWeight / (heightInMeters * heightInMeters)
                } else 0.0
                
                val warnings = generateWarnings(
                    bmi = bmi,
                    weight = currentWeight,
                    height = currentHeight,
                    sleepMinutes = totalSleepMinutes,
                    sleepTarget = sleepTarget,
                    steps = currentSteps,
                    stepsTarget = stepsTarget,
                    caloriesIn = totalCaloriesIn,
                    caloriesInTarget = caloriesInTarget.toInt()
                )
                
                _uiState.value = HomeUiState(
                    steps = HomeIndicator(
                        current = currentSteps.toDouble(),
                        target = stepsTarget.toDouble(),
                        unit = "bước"
                    ),
                    caloriesIn = HomeIndicator(
                        current = totalCaloriesIn,
                        target = caloriesInTarget,
                        unit = "kcal"
                    ),
                    caloriesOut = HomeIndicator(
                        current = totalCaloriesOut,
                        target = caloriesOutTarget,
                        unit = "kcal"
                    ),
                    sleepMinutes = HomeIndicator(
                        current = totalSleepMinutes.toDouble(),
                        target = sleepTarget.toDouble(),
                        unit = "phút"
                    ),
                    exerciseMinutes = HomeIndicator(
                        current = totalExerciseMinutes.toDouble(),
                        target = exerciseTarget.toDouble(),
                        unit = "phút"
                    ),
                    warnings = warnings,
                    bmi = bmi,
                    weight = currentWeight,
                    height = currentHeight,
                    isLoading = false
                )
                
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "Lỗi khi tải dữ liệu: ${e.message}"
                )
            }
        }
    }
    
    private fun generateWarnings(
        bmi: Double,
        weight: Double,
        height: Double,
        sleepMinutes: Int,
        sleepTarget: Int,
        steps: Int,
        stepsTarget: Int,
        caloriesIn: Double,
        caloriesInTarget: Int
    ): List<HealthWarning> {
        val warnings = mutableListOf<HealthWarning>()
        
        // Cảnh báo BMI
        if (bmi > 0) {
            when {
                bmi < 18.5 -> warnings.add(
                    HealthWarning(
                        title = "BMI thấp",
                        message = "Bạn đang thiếu cân. Hãy bổ sung dinh dưỡng hợp lý.",
                        type = WarningType.WARNING,
                        value = String.format("%.1f", bmi)
                    )
                )
                bmi >= 25 && bmi < 30 -> warnings.add(
                    HealthWarning(
                        title = "BMI cao",
                        message = "Bạn đang thừa cân. Hãy tập luyện và ăn uống điều độ.",
                        type = WarningType.WARNING,
                        value = String.format("%.1f", bmi)
                    )
                )
                bmi >= 30 -> warnings.add(
                    HealthWarning(
                        title = "BMI rất cao",
                        message = "Bạn đang béo phì. Cần thay đổi lối sống ngay!",
                        type = WarningType.DANGER,
                        value = String.format("%.1f", bmi)
                    )
                )
            }
        }
        
        // Cảnh báo giấc ngủ
        if (sleepMinutes > 0) {
            val sleepHours = sleepMinutes / 60.0
            when {
                sleepHours < 5 -> warnings.add(
                    HealthWarning(
                        title = "Thiếu ngủ nghiêm trọng",
                        message = "Bạn ngủ quá ít! Cần ngủ ít nhất 7-8 tiếng mỗi ngày.",
                        type = WarningType.DANGER,
                        value = String.format("%.1f giờ", sleepHours)
                    )
                )
                sleepHours < 7 -> warnings.add(
                    HealthWarning(
                        title = "Thiếu ngủ",
                        message = "Bạn chưa ngủ đủ giấc. Hãy nghỉ ngơi nhiều hơn.",
                        type = WarningType.WARNING,
                        value = String.format("%.1f giờ", sleepHours)
                    )
                )
                sleepHours > 10 -> warnings.add(
                    HealthWarning(
                        title = "Ngủ quá nhiều",
                        message = "Ngủ quá nhiều cũng không tốt cho sức khỏe.",
                        type = WarningType.INFO,
                        value = String.format("%.1f giờ", sleepHours)
                    )
                )
            }
        }
        
        // Cảnh báo bước chân
        if (steps < stepsTarget * 0.3 && stepsTarget > 0) {
            warnings.add(
                HealthWarning(
                    title = "Ít vận động",
                    message = "Bạn mới đi được ${steps} bước. Hãy vận động nhiều hơn!",
                    type = WarningType.INFO,
                    value = "$steps bước"
                )
            )
        }
        
        // Cảnh báo calories
        if (caloriesIn > caloriesInTarget * 1.3 && caloriesInTarget > 0) {
            warnings.add(
                HealthWarning(
                    title = "Nạp quá nhiều calo",
                    message = "Bạn đã nạp vượt mục tiêu ${((caloriesIn / caloriesInTarget - 1) * 100).toInt()}%.",
                    type = WarningType.WARNING,
                    value = "${caloriesIn.toInt()} kcal"
                )
            )
        }
        
        return warnings
    }

    fun refresh() {
        loadHomeData()
    }
}
