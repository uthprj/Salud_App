package com.example.salud_app.ui.screen.home

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
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
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController

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
                    style = Stroke(width = strokePx, cap = StrokeCap.Round),
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
fun HomeScreen(
    navController: NavController,
    homeViewModel: HomeViewModel = viewModel()
) {
    val context = LocalContext.current
    val uiState by homeViewModel.uiState.collectAsState()
    
    LaunchedEffect(Unit) {
        homeViewModel.initialize(context)
    }
    
    Salud_AppTheme {
        AppScaffold(
            navController = navController,
            title = "Tổng quan",
            screenLevel = ScreenLevel.MAIN,

            ) { paddingValues ->

                // Lấy progress từ uiState
                val rings = listOf(
                    RingData(uiState.steps.progress, Color(0xFFA837CD), 10.dp),         // bước chân
                    RingData(uiState.caloriesIn.progress, Color(0xFFFF9B00), 10.dp),    // calo nạp
                    RingData(uiState.caloriesOut.progress, Color(0xFFDE3D3D), 10.dp),   // calo đốt
                    RingData(uiState.sleepMinutes.progress, Color(0xFF4B89F5), 10.dp),  // ngủ nghỉ
                    RingData(uiState.exerciseMinutes.progress, Color(0xFF67E33A), 10.dp), // luyện tập
                )
                
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    item {
                        Spacer(modifier = Modifier.height(20.dp))
                        
                        MultiRingProgress(rings = rings, size = 260.dp)
                        
                        Spacer(modifier = Modifier.height(20.dp))

                        Text(
                            text = "Chỉ số",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold
                        )

                        Spacer(modifier = Modifier.height(15.dp))

                        
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(
                                    color = Color(0xBCE0FCFF),
                                    shape = RoundedCornerShape(16.dp)
                                )
                                .padding(all = 15.dp),
                        ) {
                            InfoItemWithProgress(
                                iconRes = R.drawable.barefoot_24px,
                                title = "Bước chân",
                                color = Color(0xFF710A92),
                                indicator = uiState.steps
                            )
                            InfoItemWithProgress(
                                iconRes = R.drawable.bolt_24px,
                                title = "Calo nạp",
                                color = Color(0xFFD58913),
                                indicator = uiState.caloriesIn
                            )
                            InfoItemWithProgress(
                                iconRes = R.drawable.mode_heat_24px,
                                title = "Calo đốt",
                                color = Color(0xFF930B0B),
                                indicator = uiState.caloriesOut
                            )
                            InfoItemWithProgress(
                                iconRes = R.drawable.bedtime_24px,
                                title = "Ngủ nghỉ",
                                color = Color(0xFF1351B8),
                                indicator = uiState.sleepMinutes
                            )
                            InfoItemWithProgress(
                                iconRes = R.drawable.exercise_24px,
                                title = "Luyện tập",
                                color = Color(0xFF2F9909),
                                indicator = uiState.exerciseMinutes
                            )
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        Text(
                            text = "Cảnh báo",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold
                        )

                        Spacer(modifier = Modifier.height(15.dp))

                        // Hiển thị các cảnh báo
                        if (uiState.warnings.isEmpty()) {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F5E9))
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Image(
                                        painter = painterResource(id = R.drawable.check_circle_24px),
                                        contentDescription = null,
                                        modifier = Modifier.size(32.dp),
                                        colorFilter = ColorFilter.tint(Color(0xFF4CAF50))
                                    )
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Text(
                                        text = "Tất cả chỉ số đều bình thường!",
                                        fontSize = 14.sp,
                                        color = Color(0xFF2E7D32)
                                    )
                                }
                            }
                        } else {
                            uiState.warnings.forEach { warning ->
                                WarningCard(warning = warning)
                                Spacer(modifier = Modifier.height(8.dp))
                            }
                        }

                        Spacer(modifier = Modifier.height(20.dp))
                    }
                }
        }
    }
}

@Composable
fun WarningCard(warning: HealthWarning) {
    val (backgroundColor, borderColor, iconColor) = when (warning.type) {
        WarningType.INFO -> Triple(
            Color(0xFFE3F2FD),
            Color(0xFF2196F3),
            Color(0xFF1976D2)
        )
        WarningType.WARNING -> Triple(
            Color(0xFFFFF3E0),
            Color(0xFFFF9800),
            Color(0xFFF57C00)
        )
        WarningType.DANGER -> Triple(
            Color(0xFFFFEBEE),
            Color(0xFFF44336),
            Color(0xFFD32F2F)
        )
    }
    
    val iconRes = when (warning.type) {
        WarningType.INFO -> R.drawable.info_24px
        WarningType.WARNING -> R.drawable.warning_24px
        WarningType.DANGER -> R.drawable.error_24px
    }
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        border = BorderStroke(1.dp, borderColor.copy(alpha = 0.5f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.Top
        ) {
            Image(
                painter = painterResource(id = iconRes),
                contentDescription = null,
                modifier = Modifier.size(28.dp),
                colorFilter = ColorFilter.tint(iconColor)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = warning.title,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = iconColor
                    )
                    if (warning.value.isNotEmpty()) {
                        Text(
                            text = warning.value,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            color = iconColor.copy(alpha = 0.8f)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = warning.message,
                    fontSize = 13.sp,
                    color = Color.DarkGray
                )
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
                .size(30.dp)
                .padding(top = 4.dp, bottom = 4.dp),
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

@Composable
fun InfoItemWithProgress(
    iconRes: Int,
    title: String,
    color: Color,
    indicator: HomeIndicator
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Image(
                    painter = painterResource(id = iconRes),
                    contentDescription = null,
                    modifier = Modifier
                        .size(28.dp)
                        .padding(end = 8.dp),
                    colorFilter = ColorFilter.tint(color)
                )
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color.Black
                    )
                )
            }
            Text(
                text = "${indicator.displayCurrent}/${indicator.displayTarget} ${indicator.unit}",
                style = MaterialTheme.typography.bodySmall.copy(
                    fontSize = 12.sp,
                    color = Color.DarkGray
                )
            )
        }
        
        Spacer(modifier = Modifier.height(4.dp))
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Progress bar
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(8.dp)
                    .background(Color.LightGray.copy(alpha = 0.3f), RoundedCornerShape(4.dp))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .fillMaxWidth(indicator.progress)
                        .background(color, RoundedCornerShape(4.dp))
                )
            }
            
            Spacer(modifier = Modifier.width(8.dp))
            
            // Percentage
            Text(
                text = "${indicator.percentage}%",
                style = MaterialTheme.typography.bodySmall.copy(
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = color
                ),
                modifier = Modifier.width(40.dp)
            )
        }
    }
}

@Preview
@Composable
fun HomeScreenPreview() {
    val navController = rememberNavController()
    HomeScreen(
        navController = navController
    )
}
