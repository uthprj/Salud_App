package com.example.salud_app.ui.screen.data

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
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
    // Định nghĩa các màu sắc
    val healthBg = Color(0xFFFCE4EC)
    val healthIcon = Color(0xFFE91E63)
    val nutritionBg = Color(0xFFFFF3E0)
    val nutritionIcon = Color(0xFFFB8C00)
    val exerciseBg = Color(0xFFE8F5E9)
    val exerciseIcon = Color(0xFF4CAF50)
    val sleepBg = Color(0xFFE3F2FD)
    val sleepIcon = Color(0xFF1E88E5)
    val reportBg = Color(0xFFFFFDE7)
    val reportIcon = Color(0xFFFBC02D)

    AppScaffold(
        navController = navController,
        title = stringResource(R.string.data),
        screenLevel = ScreenLevel.MAIN,
    ) { innerPadding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            verticalArrangement = Arrangement.spacedBy(16.dp),

        ) {

            // 1. Sức khỏe
            DataItemRow(
                icon = painterResource(id = R.drawable.ic_ecg_heart),
                iconTint = healthIcon,
                backgroundColor = healthBg,
                text = stringResource(R.string.health),
                onClick = { navController.navigate("data-health") }
            )


            // 2. Dinh dưỡng
            DataItemRow(
                icon = painterResource(id = R.drawable.ic_grocery),
                iconTint = nutritionIcon,
                backgroundColor = nutritionBg,
                text = stringResource(R.string.nutrition),
                onClick = { navController.navigate("data-nutrition") }
            )

            // 3. Luyện tập
            DataItemRow(
                icon = painterResource(id = R.drawable.ic_exercise),
                iconTint = exerciseIcon,
                backgroundColor = exerciseBg,
                text = stringResource(R.string.exercise),
                onClick = {  }
            )

            // 4. Giấc ngủ
            DataItemRow(
                icon = painterResource(id = R.drawable.ic_moon_stars), // Icon mặt trăng
                iconTint = sleepIcon,
                backgroundColor = sleepBg,
                text = stringResource(R.string.sleep),
                onClick = {  }
            )

            // 5. Báo cáo
            DataItemRow(
                icon = painterResource(id = R.drawable.ic_finance),
                iconTint = reportIcon,
                backgroundColor = reportBg,
                text = stringResource(R.string.report),
                onClick = {  }
            )
        }
    }
}


@Composable
fun DataItemRow(
    icon: Painter,
    iconTint: Color,
    backgroundColor: Color,
    text: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            modifier = Modifier.size(48.dp),
            shape = CircleShape,
            color = backgroundColor
        ) {
            Icon(
                painter = icon,
                contentDescription = text,
                tint = iconTint,
                modifier = Modifier.padding(12.dp)
            )
        }

        Spacer(modifier = Modifier.width(16.dp))

        Text(
            text = text,
            style = MaterialTheme.typography.titleMedium
        )
    }
}



@Preview(showBackground = true)
@Composable
fun DataScreenPreview() {
    val navController = rememberNavController()

    Salud_AppTheme {
        DataScreen(navController = navController, onBackClicked = {})
    }
}