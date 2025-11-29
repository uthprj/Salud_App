package com.example.salud_app.navigation

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.salud_app.ui.screen.SplashScreen
import com.example.salud_app.ui.screen.ScreenTest
import com.example.salud_app.ui.screen.data.*
import com.example.salud_app.ui.screen.data.health.DataHealthScreen
import com.example.salud_app.ui.screen.data.health.wieght.DataHealthWeightScreen
import com.example.salud_app.ui.screen.data.nutrition.DataNutritionScreen
import com.example.salud_app.ui.screen.diary.DiaryScreen
import com.example.salud_app.ui.screen.home.HomeScreen
import com.example.salud_app.ui.screen.profile.ProfileScreen
import com.example.salud_app.ui.screen.sign.FillInfo
import com.example.salud_app.ui.screen.sign.LoginScreen
import com.example.salud_app.ui.screen.sign.SignInViewModel


@Composable
fun NavGraph(navController: NavHostController) {
    NavHost(navController = navController, startDestination = "sign-in") {
        composable("splash") { SplashScreen(navController, onAnimationComplete = {}) }
        composable("screen-test") { ScreenTest(navController, onBackClicked = { navController.popBackStack() }) }
        composable("data") { DataScreen(navController, onBackClicked = { navController.popBackStack()}) }
        composable("data-health") { DataHealthScreen(navController, onBackClicked = { navController.popBackStack()}) }
        composable("data-health-weight") { DataHealthWeightScreen(navController, onBackClicked = { navController.popBackStack()}) }
        composable("data-nutrition") { DataNutritionScreen(navController, onBackClicked = { navController.popBackStack()}) }
        composable("sign-in") {
            val signInViewModel: SignInViewModel = viewModel() // Lấy ViewModel mặc định
            LoginScreen(navController = navController, viewModel = signInViewModel)
        }
        composable("home") { HomeScreen(navController) }
        composable("diary") { DiaryScreen(navController) }
        composable("profile") { ProfileScreen(navController) }
        composable("fill-info") { FillInfo(navController) }
    }
}