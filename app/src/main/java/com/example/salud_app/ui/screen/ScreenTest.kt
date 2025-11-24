package com.example.salud_app.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
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
    onBackClicked: () -> Unit
) {
//    *********************************
//    *
//    *  THÊM CÁC BUTTON VÀO ĐÂY
//    * <title_button> to <route>
//    *
//    *********************************
    // Danh sách các màn hình để điều hướng đến

    val screenDestinations = listOf(
        "Splash" to "splash",
        "Screen Test" to "screen-test",
        "Data" to "data",
        "Sign In" to "sign-in",

        // Thêm các màn hình khác vào đây
    )

    AppScaffold(
        navController = navController,
        title = "Screen Test",
        screenLevel = ScreenLevel.MAIN,

        ) { innerPadding ->


        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(16.dp), // Padding cho nội dung bên trong LazyColumn
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp) // Khoảng cách giữa các button
        ) {
            items(screenDestinations) { (buttonText, route) ->
                Button(
                    onClick = { navController.navigate(route) },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(buttonText, style = MaterialTheme.typography.bodyMedium)
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ScreenTestPreview() { // Đổi tên Preview cho đúng
    val navController = rememberNavController()

    Salud_AppTheme {
        ScreenTest(navController = navController, onBackClicked = {})
    }
}
