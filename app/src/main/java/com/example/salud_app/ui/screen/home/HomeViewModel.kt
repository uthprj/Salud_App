package com.example.salud_app.ui.screen.home

import android.content.Context
import android.content.SharedPreferences
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.salud_app.components.draw_chart.ChartDataPoint
import com.example.salud_app.components.step_counter.StepCounterManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
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

// Data class cho lịch sử chỉ số sức khỏe
data class HealthMetricsHistory(
    val weightHistory: List<ChartDataPoint> = emptyList(),
    val heightHistory: List<ChartDataPoint> = emptyList(),
    val bmiHistory: List<ChartDataPoint> = emptyList(),
    val heartRateHistory: List<ChartDataPoint> = emptyList(),
    val systolicBPHistory: List<ChartDataPoint> = emptyList(),
    val diastolicBPHistory: List<ChartDataPoint> = emptyList()
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
    val heartRate: Long = 0,
    val systolicBP: Long = 0,
    val diastolicBP: Long = 0,
    val healthHistory: HealthMetricsHistory = HealthMetricsHistory(),
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
        viewModelScope.launch(Dispatchers.IO) {
            try {
                withContext(Dispatchers.Main) {
                    _uiState.value = _uiState.value.copy(isLoading = true, error = null)
                }
                
                val prefs = sharedPreferences ?: return@launch
                val today = LocalDate.now().format(dateFormatter)
                
                // Load targets từ SharedPreferences (nhanh - local)
                val stepsTarget = prefs.getInt("steps_target", 10000)
                val caloriesInTarget = prefs.getFloat("calories_in_target", 2000f).toDouble()
                val caloriesOutTarget = prefs.getFloat("calories_out_target", 500f).toDouble()
                val sleepTarget = prefs.getInt("sleep_target", 480)
                val exerciseTarget = prefs.getInt("exercise_target", 30)
                
                // Load current steps (nhanh - local)
                val currentSteps = stepCounterManager?.getCurrentSteps() ?: 0
                
                val currentUser = auth.currentUser
                
                // Hiển thị UI ngay với dữ liệu local trước
                withContext(Dispatchers.Main) {
                    _uiState.value = _uiState.value.copy(
                        steps = HomeIndicator(current = currentSteps.toDouble(), target = stepsTarget.toDouble(), unit = "bước"),
                        caloriesIn = HomeIndicator(target = caloriesInTarget, unit = "kcal"),
                        caloriesOut = HomeIndicator(target = caloriesOutTarget, unit = "kcal"),
                        sleepMinutes = HomeIndicator(target = sleepTarget.toDouble(), unit = "phút"),
                        exerciseMinutes = HomeIndicator(target = exerciseTarget.toDouble(), unit = "phút"),
                        isLoading = true
                    )
                }
                
                if (currentUser == null) {
                    withContext(Dispatchers.Main) {
                        _uiState.value = _uiState.value.copy(isLoading = false)
                    }
                    return@launch
                }

                val uid = currentUser.uid
                val userRef = firestore.collection("User").document(uid)

                // CHẠY TẤT CẢ QUERIES SONG SONG (PARALLEL)
                val nutritionDeferred = async {
                    try {
                        userRef.collection("NutritionRecords")
                            .whereEqualTo("date", today)
                            .get().await()
                    } catch (e: Exception) { null }
                }
                
                val exerciseDeferred = async {
                    try {
                        userRef.collection("ExerciseRecords")
                            .whereEqualTo("date", today)
                            .get().await()
                    } catch (e: Exception) { null }
                }
                
                val sleepDeferred = async {
                    try {
                        userRef.collection("SleepRecords")
                            .whereEqualTo("date", today)
                            .get().await()
                    } catch (e: Exception) { null }
                }
                
                val healthDeferred = async {
                    try {
                        userRef.collection("HealthRecords")
                            .orderBy("timestamp", Query.Direction.DESCENDING)
                            .limit(30)
                            .get().await()
                    } catch (e: Exception) { null }
                }
                
                // Đợi tất cả queries hoàn thành cùng lúc
                val results = awaitAll(nutritionDeferred, exerciseDeferred, sleepDeferred, healthDeferred)
                
                val nutritionDocs = results[0]
                val exerciseDocs = results[1]
                val sleepDocs = results[2]
                val healthDocs = results[3]
                
                // Xử lý kết quả
                var totalCaloriesIn = 0.0
                nutritionDocs?.forEach { doc ->
                    totalCaloriesIn += doc.getDouble("calories") ?: 0.0
                }
                
                var totalCaloriesOut = 0.0
                var totalExerciseMinutes = 0
                exerciseDocs?.forEach { doc ->
                    totalCaloriesOut += doc.getDouble("caloriesBurned") ?: 0.0
                    totalExerciseMinutes += (doc.getLong("duration")?.toInt() ?: 0)
                }
                
                var totalSleepMinutes = 0
                sleepDocs?.forEach { doc ->
                    totalSleepMinutes += (doc.getLong("duration")?.toInt() ?: 0)
                }
                
                // Xử lý Health Records - Lấy giá trị mới nhất (không phân biệt ngày)
                // Nếu hôm nay không có dữ liệu, sẽ lấy dữ liệu của ngày gần nhất
                var currentWeight = 0.0
                var currentHeight = 0.0
                var latestHeartRate = 0L
                var latestSystolic = 0L
                var latestDiastolic = 0L
                
                val weightPoints = mutableListOf<ChartDataPoint>()
                val heightPoints = mutableListOf<ChartDataPoint>()
                val bmiPoints = mutableListOf<ChartDataPoint>()
                val hrPoints = mutableListOf<ChartDataPoint>()
                val systolicPoints = mutableListOf<ChartDataPoint>()
                val diastolicPoints = mutableListOf<ChartDataPoint>()
                
                // Dữ liệu đã được sắp xếp theo timestamp DESC (mới nhất đầu tiên)
                // Nên giá trị đầu tiên tìm được sẽ là giá trị mới nhất
                healthDocs?.documents?.forEach { doc ->
                    try {
                        val dateStr = doc.getString("date") ?: return@forEach
                        val date = LocalDate.parse(dateStr, dateFormatter)
                        
                        val w = doc.getDouble("weight") ?: 0.0
                        val h = doc.getDouble("height") ?: 0.0
                        val hr = doc.getLong("heartRate") ?: 0L
                        val sys = doc.getLong("systolic") ?: 0L
                        val dia = doc.getLong("diastolic") ?: 0L
                        
                        // Lưu vào chart history
                        if (w > 0) {
                            weightPoints.add(ChartDataPoint(date, w.toFloat()))
                            // Lấy giá trị mới nhất cho hiển thị (chỉ lấy lần đầu tiên)
                            if (currentWeight == 0.0) currentWeight = w
                        }
                        if (h > 0) {
                            heightPoints.add(ChartDataPoint(date, h.toFloat()))
                            if (currentHeight == 0.0) currentHeight = h
                        }
                        if (hr > 0) {
                            hrPoints.add(ChartDataPoint(date, hr.toFloat()))
                            if (latestHeartRate == 0L) latestHeartRate = hr
                        }
                        if (sys > 0) {
                            systolicPoints.add(ChartDataPoint(date, sys.toFloat()))
                            if (latestSystolic == 0L) latestSystolic = sys
                        }
                        if (dia > 0) {
                            diastolicPoints.add(ChartDataPoint(date, dia.toFloat()))
                            if (latestDiastolic == 0L) latestDiastolic = dia
                        }
                        
                        // Tính BMI cho mỗi ngày có cả weight và height
                        if (w > 0 && h > 0) {
                            val heightM = h / 100
                            val bmiValue = w / (heightM * heightM)
                            bmiPoints.add(ChartDataPoint(date, bmiValue.toFloat()))
                        }
                    } catch (e: Exception) {
                        // Skip invalid record
                    }
                }
                
                // Nếu có weight nhưng không có height trong cùng record,
                // vẫn có thể tính BMI với height mới nhất
                if (bmiPoints.isEmpty() && currentWeight > 0 && currentHeight > 0) {
                    val heightM = currentHeight / 100
                    val bmiValue = currentWeight / (heightM * heightM)
                    bmiPoints.add(ChartDataPoint(LocalDate.now(), bmiValue.toFloat()))
                }
                
                val healthHistory = HealthMetricsHistory(
                    weightHistory = weightPoints.sortedBy { it.date },
                    heightHistory = heightPoints.sortedBy { it.date },
                    bmiHistory = bmiPoints.sortedBy { it.date },
                    heartRateHistory = hrPoints.sortedBy { it.date },
                    systolicBPHistory = systolicPoints.sortedBy { it.date },
                    diastolicBPHistory = diastolicPoints.sortedBy { it.date }
                )
                
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
                
                withContext(Dispatchers.Main) {
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
                        heartRate = latestHeartRate,
                        systolicBP = latestSystolic,
                        diastolicBP = latestDiastolic,
                        healthHistory = healthHistory,
                        isLoading = false
                    )
                }
                
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = "Lỗi khi tải dữ liệu: ${e.message}"
                    )
                }
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
