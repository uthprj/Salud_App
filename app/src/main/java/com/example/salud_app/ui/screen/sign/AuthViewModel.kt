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
import kotlinx.coroutines.launch

class SignInViewModel : ViewModel() {

    // Initialize FirebaseAuth lazily to avoid initialization-order issues
    private val auth by lazy { FirebaseAuth.getInstance() }

    // Sign in with Email/Password
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

        viewModelScope.launch(Dispatchers.Main) {
            try {
                auth.signInWithEmailAndPassword(email, password)
                    .addOnSuccessListener { authResult ->
                        val firebaseUser = authResult.user
                        if (firebaseUser != null) {
                            val user = User(
                                email = firebaseUser.email ?: "",
                                photoUrl = firebaseUser.photoUrl?.toString() ?: "",
                                fullName = firebaseUser.displayName ?: ""
                            )
                            onSuccess(user)
                        } else {
                            onFailure("Không lấy được thông tin người dùng")
                        }
                    }
                    .addOnFailureListener { e ->
                        onFailure("Đăng nhập thất bại: ${e.localizedMessage}")
                    }
            } catch (e: Exception) {
                onFailure("Lỗi: ${e.localizedMessage}")
            }
        }
    }

    // Sign up with Email/Password
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

        viewModelScope.launch(Dispatchers.Main) {
            try {
                auth.createUserWithEmailAndPassword(email, password)
                    .addOnSuccessListener { authResult ->
                        val firebaseUser = authResult.user
                        if (firebaseUser != null) {
                            val user = User(
                                email = firebaseUser.email ?: "",
                                photoUrl = firebaseUser.photoUrl?.toString() ?: "",
                                fullName = fullName
                            )

                            // Save user to Firestore
                            try {
                                val db = FirebaseFirestore.getInstance()
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

                                db.collection("User")
                                    .document(uid)
                                    .set(userData)
                                    .addOnSuccessListener {
                                        Log.d("AuthViewModel", "User saved to Firestore: $uid")
                                        onSuccess(user)
                                    }
                                    .addOnFailureListener { e ->
                                        Log.e("AuthViewModel", "Failed saving user to Firestore", e)
                                        onFailure("Lỗi lưu thông tin người dùng")
                                    }
                            } catch (e: Exception) {
                                Log.e("AuthViewModel", "Error writing to Firestore", e)
                                onFailure("Lỗi: ${e.localizedMessage}")
                            }
                        } else {
                            onFailure("Không lấy được thông tin người dùng")
                        }
                    }
                    .addOnFailureListener { e ->
                        onFailure("Đăng ký thất bại: ${e.localizedMessage}")
                    }
            } catch (e: Exception) {
                onFailure("Lỗi: ${e.localizedMessage}")
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
                auth.sendPasswordResetEmail(email)
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

        viewModelScope.launch(Dispatchers.Main) { // chạy trên Main thread
            try {
                val result = credentialManager.getCredential(context, request)
                if (result.credential is CustomCredential) {
                    val custom = result.credential as CustomCredential
                    if (custom.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                        val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(custom.data)
                        val token = googleIdTokenCredential.idToken

                        val credential = GoogleAuthProvider.getCredential(token, null)
                        auth.signInWithCredential(credential)
                            .addOnSuccessListener { authResult ->
                                val firebaseUser = authResult.user
                                if (firebaseUser != null) {
                                    // Build User object to pass to UI
                                    val user = User(
                                        email = firebaseUser.email ?: "",
                                        photoUrl = firebaseUser.photoUrl?.toString() ?: "",
                                        fullName = firebaseUser.displayName ?: ""
                                    )

                                    // Notify UI immediately
                                    onSuccess(user)

                                    // Asynchronously write user data to Firestore (collection: "User", doc id = uid)
                                    try {
                                        val db = FirebaseFirestore.getInstance()
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

                                        db.collection("User")
                                            .document(uid)
                                            .set(userData)
                                            .addOnSuccessListener {
                                                Log.d("AuthViewModel", "User saved to Firestore: $uid")
                                            }
                                            .addOnFailureListener { e ->
                                                Log.e("AuthViewModel", "Failed saving user to Firestore", e)
                                            }
                                    } catch (e: Exception) {
                                        Log.e("AuthViewModel", "Error writing to Firestore", e)
                                    }
                                } else {
                                    onFailure("Không lấy được thông tin người dùng")
                                }
                            }
                            .addOnFailureListener { e ->
                                onFailure("Đăng nhập thất bại: ${e.localizedMessage}")
                            }
                    }
                }
            } catch (e: NoCredentialException) {
                // mở màn hình thêm account
                launcher?.launch(getIntentToAddAccount())
            } catch (e: GetCredentialException) {
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
