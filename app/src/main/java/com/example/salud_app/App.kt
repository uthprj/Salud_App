package com.example.salud_app

import android.app.Application
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings
import com.google.firebase.firestore.PersistentCacheSettings
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class App : Application() {
    
    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    
    override fun onCreate() {
        super.onCreate()
        FirebaseApp.initializeApp(this)
        
        // Khởi tạo Firebase Auth NGAY trên main thread để sẵn sàng
        FirebaseAuth.getInstance()
        
        // Cấu hình Firestore với cache tối ưu
        val settings = FirebaseFirestoreSettings.Builder()
            .setLocalCacheSettings(
                PersistentCacheSettings.newBuilder()
                    .setSizeBytes(50L * 1024L * 1024L) // 50MB cache
                    .build()
            )
            .build()
        
        val firestore = FirebaseFirestore.getInstance()
        firestore.firestoreSettings = settings
        
        // Pre-warm Firestore connection trên background thread
        applicationScope.launch {
            try {
                // Query nhẹ để thiết lập connection sớm
                firestore.collection("User").limit(1).get()
            } catch (e: Exception) {
                // Ignore - chỉ để warm up connection
            }
        }
    }
}
