package com.example.salud_app.ui.screen

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import com.example.salud_app.R
import androidx.compose.ui.tooling.preview.Preview
import com.google.firebase.auth.FirebaseAuth
import androidx.compose.ui.unit.dp
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import kotlinx.coroutines.delay
import androidx.navigation.NavController

@Composable
fun SplashScreen(
    navController: NavController,
    onAnimationComplete: () -> Unit
) {
    val context = LocalContext.current
    val activity = context as? Activity

    var hasLocationPermission by remember { mutableStateOf(false) }
    var hasNotificationPermission by remember { mutableStateOf(false) }
    var hasActivityRecognitionPermission by remember { mutableStateOf(false) }
    var showNoInternetDialog by remember { mutableStateOf(false) }

    // Launcher xin quyền Location
    val locationLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { perms ->
        val fine = perms[Manifest.permission.ACCESS_FINE_LOCATION] == true
        val coarse = perms[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        hasLocationPermission = fine || coarse
    }

    // Launcher xin quyền Notification
    val notificationLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        hasNotificationPermission = granted
    }

    // Launcher xin quyền Activity Recognition (đếm bước chân)
    val activityRecognitionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        hasActivityRecognitionPermission = granted
    }

    // Animations
    val logoAlpha = remember { Animatable(0f) }
    val logoScale = remember { Animatable(0.7f) }
    val logoOffsetY = remember { Animatable(-60f) }

    // UI
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Image(
            painter = painterResource(R.drawable.salud_logo),
            contentDescription = null,
            modifier = Modifier
                .size(300.dp)
                .alpha(logoAlpha.value)
                .scale(logoScale.value)
                .offset(y = logoOffsetY.value.dp)
        )
    }

    // Dialog không có internet
    if (showNoInternetDialog) {
        AlertDialog(
            onDismissRequest = { },
            title = { Text("Không có kết nối") },
            text = { Text("Vui lòng kết nối internet và quay lại sau.") },
            confirmButton = {
                Button(
                    onClick = {
                        activity?.finishAffinity()
                    }
                ) {
                    Text("Đồng ý")
                }
            }
        )
    }

    LaunchedEffect(Unit) {

        // --- Animation ---
        logoAlpha.animateTo(1f, tween(300))
        logoOffsetY.animateTo(
            targetValue = 0f,
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessLow
            )
        )
        logoScale.animateTo(1.15f, tween(100))
        logoScale.animateTo(
            1f,
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessVeryLow
            )
        )

        delay(400)

        // --- Check và xin quyền ---
        val locationGranted = checkLocationPermission(context)
        val notifGranted = checkNotificationPermission(context)
        val activityRecognitionGranted = checkActivityRecognitionPermission(context)

        if (!locationGranted) {
            locationLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }

        if (!notifGranted && Build.VERSION.SDK_INT >= 33) {
            notificationLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }

        // Xin quyền đếm bước chân (Android 10+)
        if (!activityRecognitionGranted && Build.VERSION.SDK_INT >= 29) {
            activityRecognitionLauncher.launch(Manifest.permission.ACTIVITY_RECOGNITION)
        }

        delay(350)

        // Kiểm tra kết nối internet
        if (!isInternetAvailable(context)) {
            showNoInternetDialog = true
            return@LaunchedEffect
        }

         // Nếu đã đăng nhập (Firebase user tồn tại) -> vào thẳng Home
         val currentUser = FirebaseAuth.getInstance().currentUser
         if (currentUser != null) {
             navController.navigate("home") {
                 popUpTo("splash") { inclusive = true }
             }
         } else {
            // Chưa đăng nhập -> vào màn hình đăng nhập
            navController.navigate("sign-in") {
                popUpTo("splash") { inclusive = true }
            }
         }
        onAnimationComplete()
    }
}

fun checkLocationPermission(context: Context): Boolean {
    val fine = ContextCompat.checkSelfPermission(
        context, Manifest.permission.ACCESS_FINE_LOCATION
    ) == PackageManager.PERMISSION_GRANTED

    val coarse = ContextCompat.checkSelfPermission(
        context, Manifest.permission.ACCESS_COARSE_LOCATION
    ) == PackageManager.PERMISSION_GRANTED

    return fine || coarse
}

fun isInternetAvailable(context: Context): Boolean {
    val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    val network = connectivityManager.activeNetwork ?: return false
    val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
    return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
            capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
}

fun checkNotificationPermission(context: Context): Boolean {
    return if (Build.VERSION.SDK_INT >= 33) {
        ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.POST_NOTIFICATIONS
        ) == PackageManager.PERMISSION_GRANTED
    } else {
        NotificationManagerCompat.from(context).areNotificationsEnabled()
    }
}

fun checkActivityRecognitionPermission(context: Context): Boolean {
    return if (Build.VERSION.SDK_INT >= 29) {
        ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACTIVITY_RECOGNITION
        ) == PackageManager.PERMISSION_GRANTED
    } else {
        // Android < 10 không cần permission này
        true
    }
}

@Preview
@Composable
fun SplashScreenWithAnimationPreview() {
    SplashScreen(
        navController = NavController(LocalContext.current),
        onAnimationComplete = {}
    )
}
