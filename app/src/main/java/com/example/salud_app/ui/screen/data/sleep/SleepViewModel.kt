package com.example.salud_app.ui.screen.data.sleep

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.salud_app.components.draw_chart.ChartDataPoint
import com.example.salud_app.model.Sleep
import com.example.salud_app.model.SleepSummary
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.time.LocalDate
import java.time.format.DateTimeFormatter

data class SleepUiState(
    val sleepRecords: List<Sleep> = emptyList(),
    val todaySummary: SleepSummary = SleepSummary(),
    val chartDataPoints: List<ChartDataPoint> = emptyList(),
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val error: String? = null,
    val saveSuccess: Boolean = false
)

class SleepViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(SleepUiState())
    val uiState: StateFlow<SleepUiState> = _uiState.asStateFlow()

    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private val dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")

    init {
        loadTodaySleep()
    }

    fun loadTodaySleep() {
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
                    .collection("SleepRecords")
                    .whereEqualTo("date", today)
                    .get()
                    .await()
                    .documents
                    .mapNotNull { doc ->
                        doc.toObject(Sleep::class.java)?.copy(id = doc.id)
                    }
                    .sortedByDescending { it.timestamp }

                // Tính tổng
                val summary = SleepSummary(
                    date = today,
                    totalDuration = records.sumOf { it.duration },
                    averageQuality = if (records.isNotEmpty()) records.map { it.quality }.average() else 0.0,
                    totalSleeps = records.size
                )

                // Tính chart data points
                val chartPoints = calculateChartDataPoints()

                _uiState.value = _uiState.value.copy(
                    sleepRecords = records,
                    todaySummary = summary,
                    chartDataPoints = chartPoints,
                    isLoading = false
                )

                Log.d("SleepViewModel", "Loaded ${records.size} sleep records for today")
            } catch (e: Exception) {
                Log.e("SleepViewModel", "Error loading sleep records", e)
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = null
                )
            }
        }
    }

    private suspend fun calculateChartDataPoints(): List<ChartDataPoint> {
        val currentUser = auth.currentUser ?: return emptyList()

        return try {
            val records = firestore.collection("User")
                .document(currentUser.uid)
                .collection("SleepRecords")
                .limit(100)
                .get()
                .await()
                .documents
                .mapNotNull { doc ->
                    doc.toObject(Sleep::class.java)?.copy(id = doc.id)
                }
                .sortedByDescending { it.timestamp }

            // Nhóm theo ngày và tính tổng giờ ngủ
            records.groupBy { it.date }
                .mapNotNull { (dateStr, dailyRecords) ->
                    try {
                        val date = LocalDate.parse(dateStr, dateFormatter)
                        val totalHours = dailyRecords.sumOf { it.duration } / 60.0f
                        ChartDataPoint(date, totalHours)
                    } catch (e: Exception) {
                        null
                    }
                }
                .sortedBy { it.date }
        } catch (e: Exception) {
            Log.e("SleepViewModel", "Error calculating chart data", e)
            emptyList()
        }
    }

    fun saveSleep(
        sleepDate: LocalDate,
        wakeDate: LocalDate,
        sleepType: String,
        startTime: String,
        endTime: String,
        duration: Int,
        quality: Int,
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

                val sleepDateString = sleepDate.format(dateFormatter)
                val wakeDateString = wakeDate.format(dateFormatter)
                
                val record = Sleep(
                    userId = currentUser.uid,
                    date = sleepDateString,
                    endDate = wakeDateString,
                    timestamp = System.currentTimeMillis(),
                    sleepType = sleepType,
                    startTime = startTime,
                    endTime = endTime,
                    duration = duration,
                    quality = quality,
                    note = note
                )

                firestore.collection("User")
                    .document(currentUser.uid)
                    .collection("SleepRecords")
                    .add(record)
                    .await()

                Log.d("SleepViewModel", "Saved sleep record: $sleepType - $duration mins")

                _uiState.value = _uiState.value.copy(
                    isSaving = false,
                    saveSuccess = true
                )

                loadTodaySleep()

            } catch (e: Exception) {
                Log.e("SleepViewModel", "Error saving sleep", e)
                _uiState.value = _uiState.value.copy(
                    isSaving = false,
                    error = "Lỗi lưu dữ liệu"
                )
            }
        }
    }

    fun deleteSleep(recordId: String) {
        viewModelScope.launch {
            try {
                val currentUser = auth.currentUser ?: return@launch

                firestore.collection("User")
                    .document(currentUser.uid)
                    .collection("SleepRecords")
                    .document(recordId)
                    .delete()
                    .await()

                Log.d("SleepViewModel", "Deleted sleep record: $recordId")
                loadTodaySleep()
            } catch (e: Exception) {
                Log.e("SleepViewModel", "Error deleting sleep", e)
                _uiState.value = _uiState.value.copy(
                    error = "Lỗi xóa dữ liệu"
                )
            }
        }
    }

    fun clearSaveSuccess() {
        _uiState.value = _uiState.value.copy(saveSuccess = false)
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}