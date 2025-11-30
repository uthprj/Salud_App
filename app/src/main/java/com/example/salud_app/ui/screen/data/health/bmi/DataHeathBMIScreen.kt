package com.example.salud_app.ui.screen.data.health.bmi

import android.widget.Toast
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.salud_app.R
import com.example.salud_app.components.AppScaffold
import com.example.salud_app.components.ScreenLevel
import com.example.salud_app.components.draw_chart.AppLineChart
import com.example.salud_app.components.draw_chart.ChartDataPoint
import com.example.salud_app.ui.theme.Salud_AppTheme
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import kotlin.math.abs

@Composable
fun DataHealthBMIScreen(
    navController: NavController,
    onBackClicked: () -> Unit,
    viewModel: BMIViewModel = viewModel()
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()

    // Hiển thị Toast khi có lỗi
    LaunchedEffect(uiState.error) {
        uiState.error?.let { error ->
            Toast.makeText(context, error, Toast.LENGTH_SHORT).show()
            viewModel.clearError()
        }
    }

    AppScaffold(
        navController = navController,
        title = stringResource(R.string.bmi),
        screenLevel = ScreenLevel.SUB,
        showMoreMenu = true,
        onBackClicked = onBackClicked
    ) { innerPadding ->

        if (uiState.isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // BMI Circle Display
            item {
                BMICircleDisplay(
                    bmi = uiState.currentBMI,
                    category = uiState.bmiCategory,
                    weight = uiState.latestWeight,
                    height = uiState.latestHeight,
                    bmiColor = viewModel.getBMIColor(uiState.currentBMI)
                )
            }

            item {
                Column(Modifier.padding(horizontal = 16.dp)) {
                    Text(
                        text = "Thống kê",
                        style = MaterialTheme.typography.titleLarge,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp)
                    )
                }
            }
            item {
                BMIStatisticsView(bmiData = viewModel.getChartDataPoints())
            }

            // Thêm khoảng trống ở cuối
            item {
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

/**
 * Component hiển thị BMI dạng vòng tròn
 */
@Composable
fun BMICircleDisplay(
    bmi: Double,
    category: String,
    weight: Double,
    height: Double,
    bmiColor: Color
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Chỉ số BMI của bạn",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Vòng tròn BMI
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.size(200.dp)
            ) {
                // Vẽ vòng tròn nền
                Canvas(modifier = Modifier.size(200.dp)) {
                    val strokeWidth = 20f
                    val radius = (size.minDimension - strokeWidth) / 2
                    val center = Offset(size.width / 2, size.height / 2)

                    // Vòng tròn nền (xám nhạt)
                    drawCircle(
                        color = Color.LightGray.copy(alpha = 0.3f),
                        radius = radius,
                        center = center,
                        style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                    )

                    // Vòng tròn tiến trình BMI (0-40 scale)
                    val progress = (bmi.coerceIn(0.0, 40.0) / 40.0).toFloat()
                    val sweepAngle = 360f * progress

                    drawArc(
                        color = bmiColor,
                        startAngle = -90f,
                        sweepAngle = sweepAngle,
                        useCenter = false,
                        topLeft = Offset(center.x - radius, center.y - radius),
                        size = Size(radius * 2, radius * 2),
                        style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                    )
                }

                // Hiển thị giá trị BMI ở giữa
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = if (bmi > 0) String.format("%.1f", bmi) else "--",
                        fontSize = 48.sp,
                        fontWeight = FontWeight.Bold,
                        color = bmiColor
                    )
                    Text(
                        text = category,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        color = bmiColor
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Thông tin cân nặng và chiều cao
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                InfoItem(
                    label = "Cân nặng",
                    value = if (weight > 0) String.format("%.1f kg", weight) else "--"
                )
                InfoItem(
                    label = "Chiều cao",
                    value = if (height > 0) String.format("%.0f cm", height) else "--"
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Thang đo BMI
            BMIScaleIndicator()
        }
    }
}

@Composable
fun InfoItem(label: String, value: String) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
fun BMIScaleIndicator() {
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = "Thang đo BMI",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        // Thanh gradient màu
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .weight(18.5f)
                    .fillMaxHeight()
                    .background(Color(0xFF5DADE2), RoundedCornerShape(topStart = 6.dp, bottomStart = 6.dp))
            )
            Box(
                modifier = Modifier
                    .weight(6.5f)
                    .fillMaxHeight()
                    .background(Color(0xFF2ECC71))
            )
            Box(
                modifier = Modifier
                    .weight(5f)
                    .fillMaxHeight()
                    .background(Color(0xFFF39C12))
            )
            Box(
                modifier = Modifier
                    .weight(10f)
                    .fillMaxHeight()
                    .background(Color(0xFFE74C3C), RoundedCornerShape(topEnd = 6.dp, bottomEnd = 6.dp))
            )
        }

        // Labels
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("< 18.5", fontSize = 10.sp, color = Color(0xFF5DADE2))
            Text("18.5-25", fontSize = 10.sp, color = Color(0xFF2ECC71))
            Text("25-30", fontSize = 10.sp, color = Color(0xFFF39C12))
            Text("> 30", fontSize = 10.sp, color = Color(0xFFE74C3C))
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 2.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("Thiếu cân", fontSize = 9.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text("Bình thường", fontSize = 9.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text("Thừa cân", fontSize = 9.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text("Béo phì", fontSize = 9.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

/**
 * Section thống kê BMI
 */
@Composable
fun BMIStatisticsView(
    bmiData: List<ChartDataPoint>
) {
    var selectedTabIndex by remember { mutableIntStateOf(0) }
    val tabs = listOf("Tuần", "Tháng", "Năm")

    val today = LocalDate.now()

    // Tạo danh sách các điểm hiển thị dựa vào tab
    val displayPoints: List<ChartDataPoint> = when (selectedTabIndex) {
        0 -> {
            // Tuần: 7 điểm, mỗi điểm là 1 ngày
            (0..6).mapNotNull { i ->
                val date = today.minusDays((6 - i).toLong())
                bmiData.find { it.date == date }
            }
        }
        1 -> {
            // Tháng: 10 điểm, cách nhau 3 ngày (27 ngày)
            (0..9).mapNotNull { i ->
                val targetDate = today.minusDays(((9 - i) * 3).toLong())
                // Tìm dữ liệu gần nhất trong khoảng +-1 ngày
                bmiData.filter {
                    val diff = abs(ChronoUnit.DAYS.between(it.date, targetDate))
                    diff <= 1
                }.minByOrNull {
                    abs(ChronoUnit.DAYS.between(it.date, targetDate))
                }
            }
        }
        else -> {
            // Năm: 12 điểm, lấy dữ liệu cuối mỗi tháng
            (0..11).mapNotNull { i ->
                val targetMonth = today.minusMonths(i.toLong())
                bmiData.filter {
                    it.date.year == targetMonth.year && it.date.monthValue == targetMonth.monthValue
                }.maxByOrNull { it.date }
            }.reversed()
        }
    }

    // Format ngày theo tab
    val dateFormat = when (selectedTabIndex) {
        0 -> DateTimeFormatter.ofPattern("dd/MM")
        1 -> DateTimeFormatter.ofPattern("dd/MM")
        else -> DateTimeFormatter.ofPattern("'T.'MM")
    }

    Column {
        TabRow(
            selectedTabIndex = selectedTabIndex,
            containerColor = Color.Transparent
        ) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTabIndex == index,
                    onClick = { selectedTabIndex = index },
                    text = { Text(text = title) }
                )
            }
        }
        Spacer(modifier = Modifier.height(16.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 0.dp)
                .background(
                    color = Color(0),
                    RoundedCornerShape(12.dp)
                )
                .padding(start = 8.dp, end = 10.dp, top = 10.dp, bottom = 20.dp)
        ) {
            if (displayPoints.isNotEmpty()) {
                AppLineChart(
                    dataPoints = displayPoints,
                    lineColor = Color(0xFF2ECC71),
                    chartHeight = 220.dp,
                    unit = "",
                    showGrid = true,
                    showLabels = true,
                    dateFormat = dateFormat
                )
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(220.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "Chưa có dữ liệu BMI",
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

// Hàm lọc dữ liệu cho tuần (7 ngày gần nhất)
private fun filterDataForWeek(dataPoints: List<ChartDataPoint>): List<ChartDataPoint> {
    val today = LocalDate.now()
    val weekAgo = today.minusDays(7)
    return dataPoints.filter { it.date.isAfter(weekAgo) || it.date.isEqual(weekAgo) }
}

// Hàm lọc dữ liệu cho tháng (30 ngày gần nhất)
private fun filterDataForMonth(dataPoints: List<ChartDataPoint>): List<ChartDataPoint> {
    val today = LocalDate.now()
    val monthAgo = today.minusDays(30)
    return dataPoints.filter { it.date.isAfter(monthAgo) || it.date.isEqual(monthAgo) }
}

// Hàm lọc dữ liệu cho năm (12 tháng gần nhất - lấy ngày cuối mỗi tháng)
private fun filterDataForYear(dataPoints: List<ChartDataPoint>): List<ChartDataPoint> {
    val today = LocalDate.now()
    val yearAgo = today.minusMonths(12)

    // Lọc dữ liệu trong 12 tháng
    val yearData = dataPoints.filter { it.date.isAfter(yearAgo) || it.date.isEqual(yearAgo) }

    // Nhóm theo tháng và lấy record cuối cùng của mỗi tháng
    return yearData
        .groupBy { "${it.date.year}-${it.date.monthValue}" }
        .mapValues { (_, records) -> records.maxByOrNull { it.date } }
        .values
        .filterNotNull()
        .sortedBy { it.date }
}

@Preview(showBackground = true)
@Composable
fun PreviewDataHealthBMIScreen() {
    Salud_AppTheme {
        DataHealthBMIScreen(
            navController = rememberNavController(),
            onBackClicked = {}
        )
    }
}