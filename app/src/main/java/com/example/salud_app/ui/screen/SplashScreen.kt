package com.example.salud_app.ui.screen

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import com.example.salud_app.R
import androidx.compose.ui.tooling.preview.Preview
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

    var hasLocationPermission by remember { mutableStateOf(false) }
    var hasNotificationPermission by remember { mutableStateOf(false) }

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

        delay(350)

        navController.navigate("Screen-test") {
            popUpTo("Splash") { inclusive = true }
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

@Preview
@Composable
fun SplashScreenWithAnimationPreview() {
    SplashScreen(
        navController = NavController(LocalContext.current),
        onAnimationComplete = {}
    )
}
