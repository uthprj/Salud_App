package com.example.salud_app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Person
//import androidx.compose.material.icons.filled.Book
//import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.salud_app.ui.screen.ScreenTest
import com.example.salud_app.ui.screen.SplashScreen
import com.example.salud_app.ui.screen.data.DataScreen
import com.example.salud_app.ui.theme.Salud_AppTheme

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
            composable("data") { DataScreen(navController) }

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