package com.example.salud_app.navigation

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.salud_app.ui.screen.SplashScreen
import com.example.salud_app.ui.screen.ScreenTest
import com.example.salud_app.ui.screen.data.DataScreen

@Composable
fun NavGraph(navController: NavHostController) {
    NavHost(navController = navController, startDestination = "splash") {
        composable("splash") { SplashScreen(navController) }
        composable("screen-test") { ScreenTest(navController, onBackClicked = { navController.popBackStack() }) }
        composable("data") { DataScreen(navController) }
    }
}