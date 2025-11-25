package com.example.salud_app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.salud_app.ui.screen.ScreenTest
import com.example.salud_app.ui.screen.SplashScreen
import com.example.salud_app.ui.screen.data.DataScreen
import com.example.salud_app.ui.screen.data.health.DataHealthScreen
import com.example.salud_app.ui.screen.data.health.weight.DataHealthWeightScreen
import com.example.salud_app.ui.screen.home.HomeScreen
import com.example.salud_app.ui.screen.sign.FillInfo
import com.example.salud_app.ui.screen.sign.LoginScreen
import com.example.salud_app.ui.screen.sign.SignInViewModel
import com.example.salud_app.ui.theme.Salud_AppTheme
import com.google.firebase.auth.FirebaseAuth

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            Salud_AppTheme {
                val navController = rememberNavController()

                MainApp(navController)
            }
        }
    }
}

@Composable
fun MainApp(navController: NavHostController) {
//    // Kiểm tra xem user đã đăng nhập chưa
//    val auth = FirebaseAuth.getInstance()
//    val startDestination = if (auth.currentUser != null) "home" else "sign-in"
    
    Surface( modifier = Modifier
        .fillMaxSize()
        .safeDrawingPadding()
//        .background(Color(0xFFFBFDFF))
        ,
        color = Color(0xFFFBFDFF)
    ) {
        NavHost(
            navController = navController,
            startDestination = "Splash"
        ) {
            composable("Splash") { SplashScreen(navController, onAnimationComplete = {}) }
            composable("screen-test") { ScreenTest(navController, onBackClicked = { navController.popBackStack() }) }
            composable("data") { DataScreen(navController, onBackClicked = { navController.popBackStack()}) }
            composable("data-health") { DataHealthScreen(navController, onBackClicked = { navController.popBackStack()}) }
            composable("data-health-weight") { DataHealthWeightScreen(navController, onBackClicked = { navController.popBackStack()}) }
            composable("sign-in") {
                val signInViewModel: SignInViewModel = viewModel() // Lấy ViewModel mặc định
                LoginScreen(navController = navController, viewModel = signInViewModel)
            }
            composable("home") { HomeScreen(navController) }
            composable("fill-info") { FillInfo(navController) }
        }
    }
}

//@Preview(showBackground = true)
//@Composable
//fun MainAppPreview() {
//    Salud_AppTheme {
//        MainApp()
//    }
//}