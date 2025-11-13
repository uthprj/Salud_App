package com.example.salud_app.ui.screen.data

import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.salud_app.components.AppScaffold
import com.example.salud_app.components.ScreenLevel

@Composable
fun DataScreen(navController: NavController) {
    AppScaffold(
        navController = navController,
        title = "Dữ liệu",
        screenLevel = ScreenLevel.SUB,
        showMoreMenu = true,
        moreMenuItems = {
            DropdownMenuItem(text = { Text("Cài đặt") }, onClick = { /* Handle settings click */ })
            DropdownMenuItem(text = { Text("Đăng xuất") }, onClick = { /* Handle logout click */ })
        },
        showSaveButton = true,
        onBackClicked = { navController.popBackStack() }
    ){
        Button(
            onClick = { navController.navigate("screen-test") },
//            content = { Text("Screen Test") }
        ) {
            Text("Screen Test")

        }
    }
}

@Preview(showBackground = true)
@Composable
fun DataScreenPreview() {
    // Tạo một NavController giả để sử dụng trong preview
    val navController = rememberNavController()
    DataScreen(navController = navController)
}