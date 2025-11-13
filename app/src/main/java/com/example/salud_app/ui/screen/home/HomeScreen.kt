package com.example.salud_app.ui.screen.home

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.example.salud_app.R
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.compose.rememberNavController
import com.example.salud_app.components.*
import com.example.salud_app.ui.theme.Salud_AppTheme
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.graphics.ColorFilter

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

                    Spacer(modifier = Modifier.height(20.dp))
                    
                    Column(
                        modifier = Modifier
                            .background(
                                color = Color(0x9CE7E7E7),
                                shape = RoundedCornerShape(16.dp)
                            )
                            .padding(vertical = 7.dp),
                    ) {

                        Row(
                            modifier = Modifier.fillMaxWidth()
                                .padding(start = 10.dp, end = 10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            InfoItem(R.drawable.barefoot_24px, "1400 mét", Color(0xFF4CAF50))
                            InfoItem(R.drawable.cardiology_24px, "72 pbm", Color(0xFF2196F3))
                            InfoItem(R.drawable.water_drop_24px, "1,5/2 lít nước", Color(0xFF64B5F6))
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth()
                                .padding(start = 35.dp, end = 35.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            InfoItem(R.drawable.bolt_24px, "400/2000 calo", Color(0xFFFFEB3B))
                            InfoItem(R.drawable.snooze_24px, "8/8h ngủ", Color(0xFF7E57C2))
                        }
                    }
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color(0xFFF2F8FF))
                            .padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        item {
                            GoalCard(index = 1)
                        }
                        item {
                            GoalCard(index = 1)
                        }
                        item {
                            GoalCard(index = 1)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun GoalCard(index: Int) {
    Card(
        modifier = Modifier
            .fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
        ) {
            Text(
                text = "Mục tiêu ${index}",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Mô tả chi tiết về mục tiêu số ${index}.",
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontSize = 14.sp,
                    color = Color.DarkGray
                )
            )
        }
    }

}

@Composable
fun InfoItem(iconRes: Int, text: String, color: Color) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Image(
            painter = painterResource(id = iconRes),
            contentDescription = null,
            modifier = Modifier
                .size(25.dp)
                .padding(end = 4.dp),
            colorFilter = ColorFilter.tint(color)
        )
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium.copy(
                fontSize = 16.sp,
                color = Color.Black
            )
        )
    }
}

@Preview
@Composable
fun HomeScreenPreview() {
    HomeScreen()
}
