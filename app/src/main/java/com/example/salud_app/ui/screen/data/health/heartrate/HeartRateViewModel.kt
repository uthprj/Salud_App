package com.example.salud_app.ui.screen.data.health.heartrate

import android.util.Log
import androidx.compose.ui.graphics.Color
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

data class HRUiState(
    val hrRecords: List<HealthRecord> = emptyList(),
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val error: String? = null,
    val saveSuccess: Boolean = false
)

class HeartRateViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(HRUiState())
    val uiState: StateFlow<HRUiState> = _uiState.asStateFlow()

    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private val dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")

    init {
        loadHRRecords()
    }

    /**
     * Tải danh sách nhịp tim từ Firebase
     */
    fun loadHRRecords() {
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

                val records = firestore.collection("User")
                    .document(currentUser.uid)
                    .collection("HealthRecords")
                    .get()
                    .await()
                    .documents
                    .mapNotNull { doc ->
                        doc.toObject(HealthRecord::class.java)?.copy(id = doc.id)
                    }
                    .filter { it.heartRate > 0 }
                    .sortedByDescending { it.timestamp }

                _uiState.value = _uiState.value.copy(
                    hrRecords = records,
                    isLoading = false
                )

                Log.d("HeartRateViewModel", "Loaded ${records.size} HR records")
            } catch (e: Exception) {
                Log.e("HeartRateViewModel", "Error loading HR records", e)
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "Lỗi tải dữ liệu"
                )
            }
        }
    }

    /**
     * Lưu nhịp tim lên Firebase
     */
    fun saveHR(date: LocalDate, heartRate: Long) {
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
                val userHealthRef = firestore.collection("User")
                    .document(currentUser.uid)
                    .collection("HealthRecords")

                // Kiểm tra xem đã có record cho ngày này chưa
                val existingRecords = userHealthRef
                    .whereEqualTo("date", dateString)
                    .get()
                    .await()

                if (existingRecords.documents.isNotEmpty()) {
                    // Cập nhật record cũ
                    val docId = existingRecords.documents.first().id
                    userHealthRef.document(docId)
                        .update(
                            mapOf(
                                "heartRate" to heartRate,
                                "timestamp" to System.currentTimeMillis()
                            )
                        )
                        .await()
                    Log.d("HeartRateViewModel", "Updated HR record: $docId")
                } else {
                    // Tạo record mới
                    val record = HealthRecord(
                        userId = currentUser.uid,
                        date = dateString,
                        timestamp = System.currentTimeMillis(),
                        heartRate = heartRate
                    )
                    userHealthRef.add(record).await()
                    Log.d("HeartRateViewModel", "Created new HR record for $dateString")
                }

                _uiState.value = _uiState.value.copy(
                    isSaving = false,
                    saveSuccess = true
                )

                loadHRRecords()

            } catch (e: Exception) {
                Log.e("HeartRateViewModel", "Error saving HR", e)
                _uiState.value = _uiState.value.copy(
                    isSaving = false,
                    error = e.message ?: "Lỗi lưu dữ liệu"
                )
            }
        }
    }

    /**
     * Xóa một record nhịp tim
     */
    fun deleteHR(recordId: String) {
        viewModelScope.launch {
            try {
                val currentUser = auth.currentUser ?: return@launch

                firestore.collection("User")
                    .document(currentUser.uid)
                    .collection("HealthRecords")
                    .document(recordId)
                    .delete()
                    .await()

                Log.d("HeartRateViewModel", "Deleted HR record: $recordId")
                loadHRRecords()
            } catch (e: Exception) {
                Log.e("HeartRateViewModel", "Error deleting HR", e)
                _uiState.value = _uiState.value.copy(
                    error = e.message ?: "Lỗi xóa dữ liệu"
                )
            }
        }
    }

    /**
     * Chuyển đổi thành ChartDataPoint để vẽ biểu đồ
     */
    fun getChartDataPoints(): List<ChartDataPoint> {
        return _uiState.value.hrRecords.mapNotNull { record ->
            try {
                val date = LocalDate.parse(record.date, dateFormatter)
                ChartDataPoint(date, record.heartRate.toFloat())
            } catch (e: Exception) {
                null
            }
        }.sortedBy { it.date }
    }

    /**
     * Phân loại nhịp tim
     */
    fun getHRCategory(heartRate: Long): String {
        return when {
            heartRate < 60 -> "Nhịp chậm"
            heartRate in 60..100 -> "Bình thường"
            heartRate > 100 -> "Nhịp nhanh"
            else -> "Không xác định"
        }
    }

    /**
     * Lấy màu theo nhịp tim
     */
    fun getHRColor(heartRate: Long): Color {
        return when {
            heartRate < 60 -> Color(0xFF3498DB) // Xanh dương - Nhịp chậm
            heartRate in 60..100 -> Color(0xFF2ECC71) // Xanh lá - Bình thường
            heartRate > 100 -> Color(0xFFE74C3C) // Đỏ - Nhịp nhanh
            else -> Color.Gray
        }
    }

    fun clearSaveSuccess() {
        _uiState.value = _uiState.value.copy(saveSuccess = false)
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}