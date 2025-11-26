package com.example.salud_app.ui.screen.profile

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.salud_app.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

data class ProfileUiState(
    val user: User = User(),
    val isLoading: Boolean = true,
    val error: String? = null
)

class ProfileViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    init {
        loadUserProfile()
    }

    fun loadUserProfile() {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isLoading = true, error = null)
                val currentUser = auth.currentUser
                if (currentUser != null) {
                    val userDoc = firestore.collection("User")
                        .document(currentUser.uid)
                        .get()
                        .await()

                    val user = userDoc.toObject(User::class.java) ?: User()
                    _uiState.value = ProfileUiState(user = user, isLoading = false)
                } else {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = "User not authenticated"
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "Unknown error"
                )
            }
        }
    }

    fun updateUserProfile(
        fullName: String? = null,
        numPhone: String? = null,
        birthDate: String? = null,
        gender: String? = null,
        weight: String? = null,
        height: String? = null,
        photoUrl: String? = null
    ) {
        viewModelScope.launch {
            try {
                val currentUser = auth.currentUser ?: return@launch
                val updates = mutableMapOf<String, Any>()

                fullName?.let { updates["fullName"] = it }
                numPhone?.let { updates["numPhone"] = it }
                birthDate?.let { updates["birthDate"] = it }
                gender?.let { updates["gender"] = it }
                weight?.let { updates["weight"] = it }
                height?.let { updates["height"] = it }
                photoUrl?.let { updates["photoUrl"] = it }

                if (updates.isNotEmpty()) {
                    firestore.collection("User")
                        .document(currentUser.uid)
                        .update(updates)
                        .await()

                    // Reload profile after update
                    loadUserProfile()
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = e.message ?: "Failed to update profile"
                )
            }
        }
    }

    fun updateStepGoal(goal: String) {
        // Implement if you have a separate goals collection
        // For now, you can add to User model
    }

    fun updateHeartRateGoal(goal: String) {
        // Implement if you have a separate goals collection
    }

    fun updateSleepSchedule(enabled: Boolean, from: String, to: String) {
        // Implement if you have a separate sleep schedule collection
    }
}