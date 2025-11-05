package com.example.salud_app.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.salud_app.components.AppScaffold
import com.example.salud_app.components.ScreenLevel
import com.example.salud_app.ui.theme.Salud_AppTheme

@Composable
fun ScreenTest(
    navController: NavController,
//     onBackClicked không cần thiết ở HomeScreen vì nó là màn hình chính
     onBackClicked: () -> Unit
) {
    AppScaffold(
        navController = navController,
        title = "Screen Test",
        screenLevel = ScreenLevel.MAIN,

    ) { innerPadding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding) // Áp dụng padding từ Scaffold
                .padding(16.dp), // Thêm padding riêng cho nội dung
            horizontalAlignment = Alignment.CenterHorizontally, // Căn giữa các button
            verticalArrangement = Arrangement.Center // Căn các button ra giữa màn hình
        ) {
            Button(
                onClick = { navController.navigate("splash") },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Splash Screen")
            }

            // data screen
            Button(
                onClick = { navController.navigate("data") },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Data Screen")
            }



            // Thêm các button khác nếu cần...
        }
    }
}

@Preview(showBackground = true)
@Composable
fun HomeScreenPreview() {
    val navController = rememberNavController()

    Salud_AppTheme {
        ScreenTest(navController = navController, onBackClicked = {})
    }
}
