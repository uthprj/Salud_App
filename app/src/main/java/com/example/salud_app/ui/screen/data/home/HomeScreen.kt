package com.example.salud_app.ui.screen.data.home

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.compose.rememberNavController
import com.example.salud_app.components.*
import com.example.salud_app.ui.theme.Salud_AppTheme

data class RingData(
    val progress: Float,
    val color: Color,
    val stroke: Dp
)

@Composable
fun MultiRingProgress(
    rings: List<RingData>,
    size: Dp = 220.dp

) {
    Box(
        modifier = Modifier.size(size),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {

            rings.forEachIndexed { index, ring ->

                val strokePx = 16.dp.toPx()
                val inset = index * 50f

                drawArc(
                    color = ring.color,
                    startAngle = -90f,
                    sweepAngle = 360f * ring.progress,
                    useCenter = false,
                    style = Stroke(width = strokePx),
                    topLeft = Offset(strokePx + inset, strokePx + inset),
                    size = Size(
                        size.toPx() - (strokePx * 2) - inset * 2,
                        size.toPx() - (strokePx * 2) - inset * 2
                    )
                )
            }
        }
    }
}


@Composable
fun HomeScreen() {
    Salud_AppTheme {
        val navController = rememberNavController()

        AppScaffold(
            navController = navController,
            title = "Tổng quan",
            screenLevel = ScreenLevel.MAIN,

            ) { paddingValues ->
            Box(
                modifier = Modifier.padding(0.dp)
                    .fillMaxSize()
            ) {
                val rings = listOf(
                    RingData(0.75f, Color(0xFF4CAF50), 10.dp),   // nước
                    RingData(0.40f, Color(0xFF3F51B5), 10.dp),   // bước chân
                    RingData(0.20f, Color(0xFF9C27B0), 10.dp),   // calories
                    RingData(0.60f, Color(0xFF2196F3), 10.dp),   // nhịp tim
                    RingData(0.50f, Color(0xFFFF9800), 10.dp)    // Sleep
                )

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {

                    Spacer(modifier = Modifier.height(20.dp))

                    MultiRingProgress(rings = rings, size = 260.dp)

                    Spacer(modifier = Modifier.height(24.dp))

                    Text("Nước uống hôm nay", fontSize = 18.sp, fontWeight = FontWeight.Medium)
                    Text("1500 / 2000 ml", fontSize = 24.sp, fontWeight = FontWeight.Bold)

                    Spacer(modifier = Modifier.height(40.dp))

                    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        InfoRow("Bước chân", "4300 / 10000", Color(0xFF3F51B5))
                        InfoRow("Calo đốt", "263 kcal", Color(0xFF9C27B0))
                        InfoRow("Nhịp tim", "96 bpm", Color(0xFF2196F3))
                    }
                }
            }
        }
    }
}

@Composable
fun InfoRow(label: String, value: String, color: Color) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, fontSize = 18.sp, color = color, fontWeight = FontWeight.Medium)
        Text(value, fontSize = 18.sp, fontWeight = FontWeight.Bold)
    }
}

@Preview
@Composable
fun HomeScreenPreview() {
    HomeScreen()
}
