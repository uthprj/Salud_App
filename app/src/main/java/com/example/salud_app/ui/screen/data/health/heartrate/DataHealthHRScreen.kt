package com.example.salud_app.ui.screen.data.health.heartrate

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.salud_app.R
import com.example.salud_app.components.AppScaffold
import com.example.salud_app.components.ScreenLevel
import com.example.salud_app.components.date_picker.AppDatePicker
import com.example.salud_app.components.draw_chart.AppLineChart
import com.example.salud_app.components.draw_chart.ChartDataPoint
import com.example.salud_app.components.number_picker.NumberPicker
import com.example.salud_app.components.number_picker.rememberPickerState
import com.example.salud_app.ui.theme.Salud_AppTheme
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import kotlin.math.abs

@Composable
fun DataHealthHRScreen(
    navController: NavController,
    onBackClicked: () -> Unit,
    viewModel: HeartRateViewModel = viewModel()
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()

    var currentDate by remember { mutableStateOf(LocalDate.now()) }

    // Tạo danh sách giá trị cho picker nhịp tim (40-200 bpm)
    val hrItems = remember { (40..200).map { it.toString() } }

    // Trạng thái picker
    val hrState = rememberPickerState("72")

    // Hiển thị Toast khi lưu thành công
    LaunchedEffect(uiState.saveSuccess) {
        if (uiState.saveSuccess) {
            Toast.makeText(context, "Đã lưu nhịp tim thành công!", Toast.LENGTH_SHORT).show()
            viewModel.clearSaveSuccess()
        }
    }

    // Hiển thị Toast khi có lỗi
    LaunchedEffect(uiState.error) {
        uiState.error?.let { error ->
            Toast.makeText(context, error, Toast.LENGTH_SHORT).show()
            viewModel.clearError()
        }
    }

    AppScaffold(
        navController = navController,
        title = stringResource(R.string.heart_rate),
        screenLevel = ScreenLevel.SUB,
        showMoreMenu = true,
        onBackClicked = onBackClicked,
        showSaveButton = true,
        onSaveClicked = {
            val heartRate = hrState.selectedItem.toLongOrNull() ?: 72
            viewModel.saveHR(currentDate, heartRate)
        }
    ) { innerPadding ->

        if (uiState.isSaving) {
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
                .padding(innerPadding),
            contentPadding = PaddingValues(bottom = 16.dp)
        ) {
            // --- PHẦN: THÊM DỮ LIỆU ---
            item {
                Column(Modifier.fillMaxWidth()) {
                    HRSectionHeader(text = "Thêm dữ liệu")
                    AppDatePicker(
                        currentDate = currentDate,
                        onDateChange = { newDate -> currentDate = newDate },
                        label = "Ngày"
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    HRInputRow(
                        hrState = hrState,
                        hrItems = hrItems
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                }
            }

            // Đường phân cách
            item { HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant) }

            // --- PHẦN: THỐNG KÊ ---
            item {
                Column(Modifier.padding(horizontal = 16.dp)) {
                    HRSectionHeader(text = "Thống kê")
                }
            }
            item {
                HRStatisticsView(hrData = viewModel.getChartDataPoints())
            }

            // --- PHẦN: DANH SÁCH LỊCH SỬ ---
            item { HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant) }
            item {
                Column(Modifier.fillMaxWidth()) {
                    HRSectionHeader(text = "Lịch sử")

                    if (uiState.isLoading) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator()
                        }
                    } else if (uiState.hrRecords.isEmpty()) {
                        Text(
                            text = "Chưa có dữ liệu",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(16.dp)
                        )
                    } else {
                        uiState.hrRecords.take(10).forEach { record ->
                            HRHistoryItem(
                                time = record.date,
                                heartRate = record.heartRate,
                                category = viewModel.getHRCategory(record.heartRate),
                                color = viewModel.getHRColor(record.heartRate)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))
                }
            }
        }
    }
}

@Composable
fun HRSectionHeader(text: String, modifier: Modifier = Modifier) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleLarge,
        modifier = modifier
            .fillMaxWidth()
            .padding(top = 16.dp, bottom = 8.dp)
    )
}

@Composable
fun HRInputRow(
    hrState: com.example.salud_app.components.number_picker.PickerState,
    hrItems: List<String>
) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "Nhịp tim (bpm)",
                style = MaterialTheme.typography.bodyLarge
            )
        }
        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            val textStyle = MaterialTheme.typography.headlineLarge
            val itemHeight = 65.dp

            NumberPicker(
                state = hrState,
                items = hrItems,
                startIndex = hrItems.indexOf("72"),
                textStyle = textStyle,
                modifier = Modifier
                    .width(120.dp)
                    .height(itemHeight * 3)
            )

            Spacer(modifier = Modifier.width(16.dp))

            Text(
                text = "bpm",
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun HRStatisticsView(
    hrData: List<ChartDataPoint>
) {
    var selectedTabIndex by remember { mutableIntStateOf(0) }
    val tabs = listOf("Tuần", "Tháng", "Năm")

    val today = LocalDate.now()

    val displayPoints: List<ChartDataPoint> = when (selectedTabIndex) {
        0 -> {
            (0..6).mapNotNull { i ->
                val date = today.minusDays((6 - i).toLong())
                hrData.find { it.date == date }
            }
        }
        1 -> {
            (0..9).mapNotNull { i ->
                val targetDate = today.minusDays(((9 - i) * 3).toLong())
                hrData.filter {
                    val diff = abs(ChronoUnit.DAYS.between(it.date, targetDate))
                    diff <= 1
                }.minByOrNull {
                    abs(ChronoUnit.DAYS.between(it.date, targetDate))
                }
            }
        }
        else -> {
            (0..11).mapNotNull { i ->
                val targetMonth = today.minusMonths(i.toLong())
                hrData.filter {
                    it.date.year == targetMonth.year && it.date.monthValue == targetMonth.monthValue
                }.maxByOrNull { it.date }
            }.reversed()
        }
    }

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
                    lineColor = Color(0xFFE74C3C),
                    chartHeight = 220.dp,
                    unit = "bpm",
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
                        "Chưa có dữ liệu",
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
fun HRHistoryItem(
    time: String,
    heartRate: Long,
    category: String,
    color: Color
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp, horizontal = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = time,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = category,
                style = MaterialTheme.typography.labelSmall,
                color = color
            )
        }
        Text(
            text = "$heartRate bpm",
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.SemiBold,
            color = color
        )
    }
}

@Preview(showBackground = true)
@Composable
fun DataHealthHRScreenPreview() {
    Salud_AppTheme {
        DataHealthHRScreen(
            navController = rememberNavController(),
            onBackClicked = {}
        )
    }
}