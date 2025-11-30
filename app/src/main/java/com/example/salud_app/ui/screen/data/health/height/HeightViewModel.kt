package com.example.salud_app.ui.screen.data.health.height

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

data class HeightUiState(
    val heightRecords: List<HealthRecord> = emptyList(),
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val error: String? = null,
    val saveSuccess: Boolean = false
)

class HeightViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(HeightUiState())
    val uiState: StateFlow<HeightUiState> = _uiState.asStateFlow()

    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private val dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")

    init {
        loadHeightRecords()
    }

    /**
     * Tải danh sách chiều cao từ Firebase
     */
    fun loadHeightRecords() {
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
                    .filter { it.height > 0 } // Chỉ lấy các record có chiều cao
                    .sortedByDescending { it.timestamp } // Sắp xếp ở client

                _uiState.value = _uiState.value.copy(
                    heightRecords = records,
                    isLoading = false
                )

                Log.d("HeightViewModel", "Loaded ${records.size} height records")
            } catch (e: Exception) {
                Log.e("HeightViewModel", "Error loading height records", e)
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "Lỗi tải dữ liệu"
                )
            }
        }
    }

    /**
     * Lưu chiều cao lên Firebase
     */
    fun saveHeight(date: LocalDate, height: Double) {
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
                                "height" to height,
                                "timestamp" to System.currentTimeMillis()
                            )
                        )
                        .await()
                    Log.d("HeightViewModel", "Updated height record: $docId")
                } else {
                    // Tạo record mới
                    val record = HealthRecord(
                        userId = currentUser.uid,
                        date = dateString,
                        timestamp = System.currentTimeMillis(),
                        height = height
                    )
                    userHealthRef.add(record).await()
                    Log.d("HeightViewModel", "Created new height record for $dateString")
                }

                _uiState.value = _uiState.value.copy(
                    isSaving = false,
                    saveSuccess = true
                )

                // Tải lại dữ liệu sau khi lưu
                loadHeightRecords()

            } catch (e: Exception) {
                Log.e("HeightViewModel", "Error saving height", e)
                _uiState.value = _uiState.value.copy(
                    isSaving = false,
                    error = e.message ?: "Lỗi lưu dữ liệu"
                )
            }
        }
    }

    /**
     * Xóa một record chiều cao
     */
    fun deleteHeight(recordId: String) {
        viewModelScope.launch {
            try {
                val currentUser = auth.currentUser ?: return@launch

                firestore.collection("User")
                    .document(currentUser.uid)
                    .collection("HealthRecords")
                    .document(recordId)
                    .delete()
                    .await()

                Log.d("HeightViewModel", "Deleted height record: $recordId")
                loadHeightRecords()
            } catch (e: Exception) {
                Log.e("HeightViewModel", "Error deleting height", e)
                _uiState.value = _uiState.value.copy(
                    error = e.message ?: "Lỗi xóa dữ liệu"
                )
            }
        }
    }

    /**
     * Chuyển đổi HealthRecord thành ChartDataPoint để vẽ biểu đồ
     */
    fun getChartDataPoints(): List<ChartDataPoint> {
        return _uiState.value.heightRecords.mapNotNull { record ->
            try {
                val date = LocalDate.parse(record.date, dateFormatter)
                ChartDataPoint(date, record.height.toFloat())
            } catch (e: Exception) {
                null
            }
        }.sortedBy { it.date }
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
