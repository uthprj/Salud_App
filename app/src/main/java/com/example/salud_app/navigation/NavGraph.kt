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
import com.example.salud_app.ui.screen.data.health.bloodpressure.DataHealthBPScreen
import com.example.salud_app.ui.screen.data.health.bmi.DataHealthBMIScreen
import com.example.salud_app.ui.screen.data.health.heartrate.DataHealthHRScreen
import com.example.salud_app.ui.screen.data.health.height.DataHealthHeightScreen
import com.example.salud_app.ui.screen.data.health.weight.DataHealthWeightScreen
import com.example.salud_app.ui.screen.data.nutrition.DataNutritionScreen
import com.example.salud_app.ui.screen.data.exercise.DataExerciseScreen
import com.example.salud_app.ui.screen.data.sleep.DataSleepScreen
import com.example.salud_app.ui.screen.data.goal.DataGoalScreen
import com.example.salud_app.ui.screen.data.hint.DataHintScreen
import com.example.salud_app.ui.screen.diary.DiaryScreen
import com.example.salud_app.ui.screen.home.HomeScreen
import com.example.salud_app.ui.screen.profile.ProfileScreen
import com.example.salud_app.ui.screen.sign.LoginScreen
import com.example.salud_app.ui.screen.sign.SignInViewModel


@Composable
fun NavGraph(navController: NavHostController) {
    NavHost(navController = navController, startDestination = "splash") {
        composable("splash") { SplashScreen(navController, onAnimationComplete = {}) }
        composable("screen-test") { ScreenTest(navController, onBackClicked = { navController.popBackStack() }) }
        composable("data") { DataScreen(navController, onBackClicked = { navController.popBackStack()}) }
        composable("data-health") { DataHealthScreen(navController, onBackClicked = { navController.popBackStack()}) }
        composable("data-health-weight") { DataHealthWeightScreen(navController, onBackClicked = { navController.popBackStack()}) }
        composable("data-health-height") { DataHealthHeightScreen(navController, onBackClicked = { navController.popBackStack()}) }
        composable("data-health-BMI") { DataHealthBMIScreen(navController, onBackClicked = { navController.popBackStack()}) }
        composable("data-health-BP") { DataHealthBPScreen(navController, onBackClicked = { navController.popBackStack()}) }
        composable("data-health-HR") { DataHealthHRScreen(navController, onBackClicked = { navController.popBackStack()}) }
        composable("data-nutrition") { DataNutritionScreen(navController, onBackClicked = { navController.popBackStack()}) }
        composable("data-exercise") { DataExerciseScreen(navController, onBackClicked = { navController.popBackStack()}) }
        composable("data-sleep") { DataSleepScreen(navController, onBackClicked = { navController.popBackStack()}) }
        composable("data-goal") { DataGoalScreen(navController, onBackClicked = { navController.popBackStack()}) }
        composable("data-hint") { DataHintScreen(navController, onBackClicked = { navController.popBackStack()}) }

        composable("sign-in") {
            val signInViewModel: SignInViewModel = viewModel() // Lấy ViewModel mặc định
            LoginScreen(navController = navController, viewModel = signInViewModel)
        }
        composable("home") { HomeScreen(navController) }
        composable("diary") { DiaryScreen(navController) }
        composable("profile") { ProfileScreen(navController) }
    }
}