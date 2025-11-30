package com.example.salud_app.ui.screen.data.health.weight

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

data class WeightUiState(
    val weightRecords: List<HealthRecord> = emptyList(),
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val error: String? = null,
    val saveSuccess: Boolean = false
)

class WeightViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(_root_ide_package_.com.example.salud_app.ui.screen.data.health.weight.WeightUiState())
    val uiState: StateFlow<com.example.salud_app.ui.screen.data.health.weight.WeightUiState> = _uiState.asStateFlow()

    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private val dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")

    init {
        loadWeightRecords()
    }

    /**
     * Tải danh sách cân nặng từ Firebase
     */
    fun loadWeightRecords() {
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
                    .filter { it.weight > 0 } // Chỉ lấy các record có cân nặng
                    .sortedByDescending { it.timestamp } // Sắp xếp ở client

                _uiState.value = _uiState.value.copy(
                    weightRecords = records,
                    isLoading = false
                )
                
                Log.d("WeightViewModel", "Loaded ${records.size} weight records")
            } catch (e: Exception) {
                Log.e("WeightViewModel", "Error loading weight records", e)
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "Lỗi tải dữ liệu"
                )
            }
        }
    }

    /**
     * Lưu cân nặng lên Firebase
     */
    fun saveWeight(date: LocalDate, weight: Double) {
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
                                "weight" to weight,
                                "timestamp" to System.currentTimeMillis()
                            )
                        )
                        .await()
                    Log.d("WeightViewModel", "Updated weight record: $docId")
                } else {
                    // Tạo record mới
                    val record = HealthRecord(
                        userId = currentUser.uid,
                        date = dateString,
                        timestamp = System.currentTimeMillis(),
                        weight = weight
                    )
                    userHealthRef.add(record).await()
                    Log.d("WeightViewModel", "Created new weight record for $dateString")
                }

                _uiState.value = _uiState.value.copy(
                    isSaving = false,
                    saveSuccess = true
                )
                
                // Tải lại dữ liệu sau khi lưu
                loadWeightRecords()
                
            } catch (e: Exception) {
                Log.e("WeightViewModel", "Error saving weight", e)
                _uiState.value = _uiState.value.copy(
                    isSaving = false,
                    error = e.message ?: "Lỗi lưu dữ liệu"
                )
            }
        }
    }

    /**
     * Xóa một record cân nặng
     */
    fun deleteWeight(recordId: String) {
        viewModelScope.launch {
            try {
                val currentUser = auth.currentUser ?: return@launch
                
                firestore.collection("User")
                    .document(currentUser.uid)
                    .collection("HealthRecords")
                    .document(recordId)
                    .delete()
                    .await()
                
                Log.d("WeightViewModel", "Deleted weight record: $recordId")
                loadWeightRecords()
            } catch (e: Exception) {
                Log.e("WeightViewModel", "Error deleting weight", e)
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
        return _uiState.value.weightRecords.mapNotNull { record ->
            try {
                val date = LocalDate.parse(record.date, dateFormatter)
                ChartDataPoint(date, record.weight.toFloat())
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
