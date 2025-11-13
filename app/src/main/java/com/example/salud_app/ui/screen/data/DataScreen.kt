package com.example.salud_app.ui.screen.data

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.salud_app.R
import com.example.salud_app.components.AppScaffold
import com.example.salud_app.components.ScreenLevel
import com.example.salud_app.ui.theme.Salud_AppTheme

@Composable
fun DataScreen(
    navController: NavController,
    onBackClicked: () -> Unit
) {
    AppScaffold(
        navController = navController,
        title = stringResource(R.string.data),
        screenLevel = ScreenLevel.MAIN,

        ) { innerPadding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
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
        DataScreen(navController = navController, onBackClicked = {})
    }
}
