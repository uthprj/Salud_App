package com.example.salud_app.ui.screen.data.health.bloodpressure

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

data class BPUiState(
    val bpRecords: List<HealthRecord> = emptyList(),
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val error: String? = null,
    val saveSuccess: Boolean = false
)

class BPViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(BPUiState())
    val uiState: StateFlow<BPUiState> = _uiState.asStateFlow()

    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private val dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")

    init {
        loadBPRecords()
    }

    /**
     * Tải danh sách huyết áp từ Firebase
     */
    fun loadBPRecords() {
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

                // Lấy từ subcollection HealthRecords trong User/{uid}
                val records = firestore.collection("User")
                    .document(currentUser.uid)
                    .collection("HealthRecords")
                    .get()
                    .await()
                    .documents
                    .mapNotNull { doc ->
                        doc.toObject(HealthRecord::class.java)?.copy(id = doc.id)
                    }
                    .filter { it.systolic > 0 && it.diastolic > 0 } // Chỉ lấy các record có huyết áp
                    .sortedByDescending { it.timestamp }

                _uiState.value = _uiState.value.copy(
                    bpRecords = records,
                    isLoading = false
                )

                Log.d("BPViewModel", "Loaded ${records.size} BP records")
            } catch (e: Exception) {
                Log.e("BPViewModel", "Error loading BP records", e)
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "Lỗi tải dữ liệu"
                )
            }
        }
    }

    /**
     * Lưu huyết áp lên Firebase
     */
    fun saveBP(date: LocalDate, systolic: Long, diastolic: Long) {
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
                                "systolic" to systolic,
                                "diastolic" to diastolic,
                                "timestamp" to System.currentTimeMillis()
                            )
                        )
                        .await()
                    Log.d("BPViewModel", "Updated BP record: $docId")
                } else {
                    // Tạo record mới
                    val record = HealthRecord(
                        userId = currentUser.uid,
                        date = dateString,
                        timestamp = System.currentTimeMillis(),
                        systolic = systolic,
                        diastolic = diastolic
                    )
                    userHealthRef.add(record).await()
                    Log.d("BPViewModel", "Created new BP record for $dateString")
                }

                _uiState.value = _uiState.value.copy(
                    isSaving = false,
                    saveSuccess = true
                )

                // Tải lại dữ liệu sau khi lưu
                loadBPRecords()

            } catch (e: Exception) {
                Log.e("BPViewModel", "Error saving BP", e)
                _uiState.value = _uiState.value.copy(
                    isSaving = false,
                    error = e.message ?: "Lỗi lưu dữ liệu"
                )
            }
        }
    }

    /**
     * Xóa một record huyết áp
     */
    fun deleteBP(recordId: String) {
        viewModelScope.launch {
            try {
                val currentUser = auth.currentUser ?: return@launch

                firestore.collection("User")
                    .document(currentUser.uid)
                    .collection("HealthRecords")
                    .document(recordId)
                    .delete()
                    .await()

                Log.d("BPViewModel", "Deleted BP record: $recordId")
                loadBPRecords()
            } catch (e: Exception) {
                Log.e("BPViewModel", "Error deleting BP", e)
                _uiState.value = _uiState.value.copy(
                    error = e.message ?: "Lỗi xóa dữ liệu"
                )
            }
        }
    }

    /**
     * Chuyển đổi HealthRecord thành ChartDataPoint để vẽ biểu đồ (Systolic)
     */
    fun getSystolicChartDataPoints(): List<ChartDataPoint> {
        return _uiState.value.bpRecords.mapNotNull { record ->
            try {
                val date = LocalDate.parse(record.date, dateFormatter)
                ChartDataPoint(date, record.systolic.toFloat())
            } catch (e: Exception) {
                null
            }
        }.sortedBy { it.date }
    }

    /**
     * Chuyển đổi HealthRecord thành ChartDataPoint để vẽ biểu đồ (Diastolic)
     */
    fun getDiastolicChartDataPoints(): List<ChartDataPoint> {
        return _uiState.value.bpRecords.mapNotNull { record ->
            try {
                val date = LocalDate.parse(record.date, dateFormatter)
                ChartDataPoint(date, record.diastolic.toFloat())
            } catch (e: Exception) {
                null
            }
        }.sortedBy { it.date }
    }

    /**
     * Phân loại huyết áp
     */
    fun getBPCategory(systolic: Long, diastolic: Long): String {
        return when {
            systolic < 90 || diastolic < 60 -> "Huyết áp thấp"
            systolic < 120 && diastolic < 80 -> "Bình thường"
            systolic in 120..129 && diastolic < 80 -> "Tăng cao"
            systolic in 130..139 || diastolic in 80..89 -> "Cao độ 1"
            systolic >= 140 || diastolic >= 90 -> "Cao độ 2"
            else -> "Không xác định"
        }
    }

    /**
     * Lấy màu theo huyết áp
     */
    fun getBPColor(systolic: Long, diastolic: Long): androidx.compose.ui.graphics.Color {
        return when {
            systolic < 90 || diastolic < 60 -> androidx.compose.ui.graphics.Color(0xFF5DADE2) // Xanh dương - Thấp
            systolic < 120 && diastolic < 80 -> androidx.compose.ui.graphics.Color(0xFF2ECC71) // Xanh lá - Bình thường
            systolic in 120..129 && diastolic < 80 -> androidx.compose.ui.graphics.Color(0xFFF39C12) // Cam - Tăng cao
            systolic in 130..139 || diastolic in 80..89 -> androidx.compose.ui.graphics.Color(0xFFE74C3C) // Đỏ nhạt - Cao độ 1
            systolic >= 140 || diastolic >= 90 -> androidx.compose.ui.graphics.Color(0xFFC0392B) // Đỏ đậm - Cao độ 2
            else -> androidx.compose.ui.graphics.Color.Gray
        }
    }

    /**
     * Đánh dấu đã xử lý thông báo lưu thành công
     */
    fun clearSaveSuccess() {
        _uiState.value = _uiState.value.copy(saveSuccess = false)
    }

    /**
     * Xóa thông báo lỗi
     */
    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}
