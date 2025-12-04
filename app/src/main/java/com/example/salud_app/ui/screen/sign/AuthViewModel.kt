package com.example.salud_app.ui.screen.sign

import android.content.Context
import android.content.Intent
import android.provider.Settings
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.result.ActivityResult
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialException
import androidx.credentials.exceptions.NoCredentialException
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.salud_app.R
import com.example.salud_app.model.User
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class SignInViewModel : ViewModel() {

    // Khởi tạo ngay lập tức thay vì lazy để tránh delay lần đầu
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
    
    // Loading state để UI hiển thị progress
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    // Sign in with Email/Password - Tối ưu với await() để nhanh hơn
    fun signInWithEmail(
        email: String,
        password: String,
        onSuccess: (User) -> Unit,
        onFailure: (String) -> Unit
    ) {
        if (email.isBlank() || password.isBlank()) {
            onFailure("Email và mật khẩu không được để trống")
            return
        }

        _isLoading.value = true
        viewModelScope.launch(Dispatchers.IO) {
            try {
                // Sử dụng await() - không cần timeout vì Firebase tự handle
                val authResult = auth.signInWithEmailAndPassword(email, password).await()
                
                val firebaseUser = authResult.user
                if (firebaseUser != null) {
                    val user = User(
                        email = firebaseUser.email ?: "",
                        photoUrl = firebaseUser.photoUrl?.toString() ?: "",
                        fullName = firebaseUser.displayName ?: ""
                    )
                    withContext(Dispatchers.Main) {
                        _isLoading.value = false
                        onSuccess(user)
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        _isLoading.value = false
                        onFailure("Không lấy được thông tin người dùng")
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    _isLoading.value = false
                    onFailure("Mật khẩu hoặc mail sai")
                }
            }
        }
    }

    // Sign up with Email/Password - Tối ưu: gọi onSuccess ngay, lưu Firestore async
    fun signUpWithEmail(
        email: String,
        password: String,
        fullName: String,
        onSuccess: (User) -> Unit,
        onFailure: (String) -> Unit
    ) {
        if (email.isBlank() || password.isBlank()) {
            onFailure("Email và mật khẩu không được để trống")
            return
        }

        if (password.length < 6) {
            onFailure("Mật khẩu phải có ít nhất 6 ký tự")
            return
        }

        _isLoading.value = true
        viewModelScope.launch(Dispatchers.IO) {
            try {
                // Sử dụng await() - không timeout
                val authResult = auth.createUserWithEmailAndPassword(email, password).await()
                
                val firebaseUser = authResult.user
                if (firebaseUser != null) {
                    val user = User(
                        email = firebaseUser.email ?: "",
                        photoUrl = firebaseUser.photoUrl?.toString() ?: "",
                        fullName = fullName
                    )

                    // GỌI onSuccess NGAY LẬP TỨC - không đợi Firestore
                    withContext(Dispatchers.Main) {
                        _isLoading.value = false
                        onSuccess(user)
                    }

                    // Lưu user vào Firestore ASYNC (không block UI)
                    val uid = firebaseUser.uid
                    val userData = hashMapOf<String, Any>(
                        "userId" to uid,
                        "fullName" to fullName,
                        "birthDate" to "",
                        "gender" to "",
                        "numPhone" to "",
                        "email" to email,
                        "photoUrl" to ""
                    )

                    // Fire and forget - không đợi kết quả
                    db.collection("User")
                        .document(uid)
                        .set(userData)
                        .addOnSuccessListener {
                            Log.d("AuthViewModel", "User saved to Firestore: $uid")
                        }
                        .addOnFailureListener { e ->
                            Log.e("AuthViewModel", "Failed saving user to Firestore", e)
                        }
                } else {
                    withContext(Dispatchers.Main) {
                        _isLoading.value = false
                        onFailure("Không lấy được thông tin người dùng")
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    _isLoading.value = false
                    onFailure("Đăng ký thất bại: ${e.localizedMessage}")
                }
            }
        }
    }

    // Reset Password
    fun resetPassword(
        email: String,
        onSuccess: () -> Unit,
        onFailure: (String) -> Unit
    ) {
        if (email.isBlank()) {
            onFailure("Email không được để trống")
            return
        }

        viewModelScope.launch(Dispatchers.Main) {
            try {
                // Configure action code settings to use custom URL
                val actionCodeSettings = com.google.firebase.auth.ActionCodeSettings.newBuilder()
                    .setUrl("https://salud-app-263a5.web.app")
                    .setHandleCodeInApp(false)
                    .setAndroidPackageName(
                        "com.example.salud_app",
                        false,
                        null
                    )
                    .build()

                auth.sendPasswordResetEmail(email, actionCodeSettings)
                    .addOnSuccessListener {
                        onSuccess()
                    }
                    .addOnFailureListener { e ->
                        onFailure("Gửi email thất bại: ${e.localizedMessage}")
                    }
            } catch (e: Exception) {
                onFailure("Lỗi: ${e.localizedMessage}")
            }
        }
    }

    fun signInWithGoogle(
        context: Context,
        launcher: ManagedActivityResultLauncher<Intent, ActivityResult>?,
        onSuccess: (User) -> Unit,
        onFailure: (String) -> Unit
    ) {
        val credentialManager = CredentialManager.create(context)

        val googleIdOption = GetGoogleIdOption.Builder()
            .setFilterByAuthorizedAccounts(false)
            .setAutoSelectEnabled(false)
            .setServerClientId(context.getString(R.string.default_web_client_id))
            .build()

        val request = GetCredentialRequest.Builder()
            .addCredentialOption(googleIdOption)
            .build()

        _isLoading.value = true
        viewModelScope.launch(Dispatchers.Main) {
            try {
                val result = credentialManager.getCredential(context, request)
                if (result.credential is CustomCredential) {
                    val custom = result.credential as CustomCredential
                    if (custom.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                        val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(custom.data)
                        val token = googleIdTokenCredential.idToken

                        val credential = GoogleAuthProvider.getCredential(token, null)
                        
                        try {
                            val authResult = auth.signInWithCredential(credential).await()
                            
                            val firebaseUser = authResult.user
                            if (firebaseUser != null) {
                                val user = User(
                                    email = firebaseUser.email ?: "",
                                    photoUrl = firebaseUser.photoUrl?.toString() ?: "",
                                    fullName = firebaseUser.displayName ?: ""
                                )

                                _isLoading.value = false
                                // Notify UI immediately - không đợi Firestore
                                onSuccess(user)

                                // Lưu Firestore async (fire and forget)
                                viewModelScope.launch(Dispatchers.IO) {
                                    try {
                                        val uid = firebaseUser.uid
                                        val userData = hashMapOf<String, Any>(
                                            "userId" to uid,
                                            "fullName" to (user.fullName),
                                            "birthDate" to "",
                                            "gender" to "",
                                            "numPhone" to "",
                                            "email" to (user.email),
                                            "photoUrl" to (user.photoUrl)
                                        )
                                        db.collection("User").document(uid).set(userData).await()
                                        Log.d("AuthViewModel", "User saved to Firestore: $uid")
                                    } catch (e: Exception) {
                                        Log.e("AuthViewModel", "Failed saving user to Firestore", e)
                                    }
                                }
                            } else {
                                _isLoading.value = false
                                onFailure("Không lấy được thông tin người dùng")
                            }
                        } catch (e: Exception) {
                            _isLoading.value = false
                            onFailure("Đăng nhập thất bại: ${e.localizedMessage}")
                        }
                    }
                }
            } catch (e: NoCredentialException) {
                _isLoading.value = false
                launcher?.launch(getIntentToAddAccount())
            } catch (e: GetCredentialException) {
                _isLoading.value = false
                e.printStackTrace()
                onFailure("Lỗi: ${e.localizedMessage}")
            }
        }
    }

    private fun getIntentToAddAccount(): Intent {
        return Intent(Settings.ACTION_ADD_ACCOUNT).apply {
            putExtra(Settings.EXTRA_ACCOUNT_TYPES, arrayOf("com.google"))
        }
    }
}
