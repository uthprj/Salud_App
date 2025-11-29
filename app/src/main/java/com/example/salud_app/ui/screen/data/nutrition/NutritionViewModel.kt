package com.example.salud_app.ui.screen.data.nutrition

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.salud_app.model.Nutrition
import com.example.salud_app.model.NutritionSummary
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.generationConfig
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.time.LocalDate
import java.time.format.DateTimeFormatter


@Serializable
data class GeminiNutritionResponse(
    val calories: Double = 0.0,
    val protein: Double = 0.0,
    val carbs: Double = 0.0,
    val fat: Double = 0.0
)

data class NutritionUiState(
    val nutritionRecords: List<Nutrition> = emptyList(),
    val todaySummary: NutritionSummary = NutritionSummary(),
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val error: String? = null,
    val saveSuccess: Boolean = false,

    val isAnalyzing: Boolean = false,
    val analysisResult: GeminiNutritionResponse? = null,
    val analysisError: String? = null
)

class NutritionViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(NutritionUiState())
    val uiState: StateFlow<NutritionUiState> = _uiState.asStateFlow()

    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private val dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")

    // Khởi tạo Gemini Model
    private val generativeModel: GenerativeModel

    // THÊM DÒNG NÀY: Cấu hình bộ phân tích JSON
    private val json = Json { ignoreUnknownKeys = true }

    // SỬA LẠI Ở ĐÂY: Gộp hai khối init làm một
    init {
        val apiKey = "AIzaSyA2To3lzualjlQ9rlEK89eLGtRFw--9UhU"

        generativeModel = GenerativeModel(
            modelName = "gemini-2.0-flash-lite",
            apiKey = apiKey,
            generationConfig = generationConfig {
                responseMimeType = "application/json"
            }
        )
        // Gọi loadTodayNutrition() một lần duy nhất ở đây
        loadTodayNutrition()
    }

    // phân tích món ăn bằng Gemini
    fun analyzeMealWithGemini(mealName: String) {
        if (mealName.isBlank()) return

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isAnalyzing = true,
                analysisResult = null,
                analysisError = null
            )
            try {
                val prompt = """
                    Phân tích giá trị dinh dưỡng của món ăn sau đây và chỉ trả về một đối tượng JSON duy nhất, không giải thích gì thêm.
                    Món ăn: "$mealName"

                    Đối tượng JSON phải có cấu trúc như sau:
                    {
                      "calories": number,
                      "protein": number,
                      "carbs": number,
                      "fat": number
                    }

                    Nếu không thể phân tích hoặc không nhận dạng được món ăn, hãy trả về giá trị 0 cho tất cả các trường.
                """.trimIndent()

                val response = generativeModel.generateContent(prompt)
                val responseText = response.text

                Log.d("GeminiAPI", "Raw JSON response: $responseText")

                if (responseText != null) {
                    val result = json.decodeFromString<GeminiNutritionResponse>(responseText)
                    _uiState.value = _uiState.value.copy(
                        isAnalyzing = false,
                        analysisResult = result
                    )
                } else {
                    throw Exception("Không nhận được phản hồi từ API.")
                }

            } catch (e: Exception) {
                Log.e("GeminiAPI", "Error analyzing meal", e)
                _uiState.value = _uiState.value.copy(
                    isAnalyzing = false,
                    analysisError = "Không thể phân tích: ${e.message}"
                )
            }
        }
    }

    //     reset phân tích
    fun clearAnalysisResult() {
        _uiState.value = _uiState.value.copy(analysisResult = null, analysisError = null)
    }

    /**
     * Tải dữ liệu dinh dưỡng hôm nay
     */
    fun loadTodayNutrition() {
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

                // Lấy tất cả records của hôm nay
                val records = firestore.collection("User")
                    .document(currentUser.uid)
                    .collection("NutritionRecords")
                    .whereEqualTo("date", today)
                    .get()
                    .await()
                    .documents
                    .mapNotNull { doc ->
                        doc.toObject(Nutrition::class.java)?.copy(id = doc.id)
                    }
                    .sortedByDescending { it.timestamp }

                // Tính tổng
                val summary = NutritionSummary(
                    date = today,
                    totalCalories = records.sumOf { it.calories },
                    totalProtein = records.sumOf { it.protein },
                    totalCarbs = records.sumOf { it.carbs },
                    totalFat = records.sumOf { it.fat }
                )

                _uiState.value = _uiState.value.copy(
                    nutritionRecords = records,
                    todaySummary = summary,
                    isLoading = false
                )

                Log.d("NutritionViewModel", "Loaded ${records.size} nutrition records for today")
            } catch (e: Exception) {
                Log.e("NutritionViewModel", "Error loading nutrition records", e)
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "Lỗi tải dữ liệu"
                )
            }
        }
    }

    /**
     * Lưu món ăn lên Firebase
     */
    fun saveMeal(
        date: LocalDate,
        time: String,
        mealType: String,
        mealCategory: String,
        mealName: String,
        calories: Double,
        protein: Double,
        carbs: Double,
        fat: Double
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
                val record = Nutrition(
                    userId = currentUser.uid,
                    date = dateString,
                    time = time,
                    timestamp = System.currentTimeMillis(),
                    mealType = mealType,
                    mealCategory = mealCategory,
                    mealName = mealName,
                    calories = calories,
                    protein = protein,
                    carbs = carbs,
                    fat = fat
                )

                // gửi dữ liệu lên Firestore
                firestore.collection("User")
                    .document(currentUser.uid)
                    .collection("NutritionRecords")
                    .add(record)
                    .await()

                Log.d("NutritionViewModel", "Saved nutrition record: $mealName")

                _uiState.value = _uiState.value.copy(
                    isSaving = false,
                    saveSuccess = true
                )

                // Tải lại dữ liệu sau khi lưu
                loadTodayNutrition()

            } catch (e: Exception) {
                Log.e("NutritionViewModel", "Error saving nutrition", e)
                _uiState.value = _uiState.value.copy(
                    isSaving = false,
                    error = e.message ?: "Lỗi lưu dữ liệu"
                )
            }
        }
    }

    /**
     * Xóa một record dinh dưỡng
     */
    fun deleteMeal(recordId: String) {
        viewModelScope.launch {
            try {
                val currentUser = auth.currentUser ?: return@launch

                firestore.collection("User")
                    .document(currentUser.uid)
                    .collection("NutritionRecords")
                    .document(recordId)
                    .delete()
                    .await()

                Log.d("NutritionViewModel", "Deleted nutrition record: $recordId")
                loadTodayNutrition()
            } catch (e: Exception) {
                Log.e("NutritionViewModel", "Error deleting nutrition", e)
                _uiState.value = _uiState.value.copy(
                    error = e.message ?: "Lỗi xóa dữ liệu"
                )
            }
        }
    }

    /**
     * Đánh dấu đã xử lý thông báo lưu thành công
     */
    fun clearSaveSuccess() {
        _uiState.value = _uiState.value.copy(saveSuccess = false)
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}
