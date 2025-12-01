package com.example.salud_app.ui.screen.data.health.bloodpressure

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
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.salud_app.R
import com.example.salud_app.components.AppScaffold
import com.example.salud_app.components.ScreenLevel
import com.example.salud_app.components.date_picker.AppDatePicker
import com.example.salud_app.components.draw_chart.AppLineChart
import com.example.salud_app.components.draw_chart.ChartDataPoint
import com.example.salud_app.components.number_picker.NumberPicker
import com.example.salud_app.components.number_picker.PickerState
import com.example.salud_app.components.number_picker.rememberPickerState
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import kotlin.math.abs

@Composable
fun DataHealthBPScreen(
    navController: NavController,
    onBackClicked: () -> Unit,
    viewModel: BPViewModel = viewModel()
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()

    var currentDate by remember { mutableStateOf(LocalDate.now()) }

    // Tạo danh sách giá trị cho picker huyết áp
    val systolicItems = remember { (60..220).map { it.toString() } }
    val diastolicItems = remember { (40..140).map { it.toString() } }

    // Trạng thái picker
    val systolicState = rememberPickerState("120")
    val diastolicState = rememberPickerState("80")

    // Hiển thị Toast khi lưu thành công
    LaunchedEffect(uiState.saveSuccess) {
        if (uiState.saveSuccess) {
            Toast.makeText(context, "Đã lưu huyết áp thành công!", Toast.LENGTH_SHORT).show()
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
        title = stringResource(R.string.blood_pressure),
        screenLevel = ScreenLevel.SUB,
        showMoreMenu = true,
        onBackClicked = onBackClicked,
        showSaveButton = true,
        isSaving = uiState.isSaving,
        onSaveClicked = {
            // Lấy giá trị huyết áp từ picker
            val systolic = systolicState.selectedItem.toLongOrNull() ?: 120
            val diastolic = diastolicState.selectedItem.toLongOrNull() ?: 80

            // Lưu lên Firebase
            viewModel.saveBP(currentDate, systolic, diastolic)
        }
    ) { innerPadding ->

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(bottom = 16.dp)
        ) {
            // --- PHẦN: THÊM DỮ LIỆU ---
            item {
                Column(Modifier.fillMaxWidth()) {
                    BPSectionHeader(text = "Thêm dữ liệu")
                    AppDatePicker(
                        currentDate = currentDate,
                        onDateChange = { newDate -> currentDate = newDate },
                        maxDate = LocalDate.now()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    BPInputRow(
                        systolicState = systolicState,
                        diastolicState = diastolicState,
                        systolicItems = systolicItems,
                        diastolicItems = diastolicItems
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                }
            }

            // Đường phân cách
            item { HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant) }

            // --- PHẦN: THỐNG KÊ ---
            item {
                Column(Modifier.padding(horizontal = 16.dp)) {
                    BPSectionHeader(text = "Thống kê")
                }
            }
            item {
                BPStatisticsView(
                    systolicData = uiState.systolicChartPoints,
                    diastolicData = uiState.diastolicChartPoints
                )
            }

            // --- PHẦN: DANH SÁCH LỊCH SỬ ---
            item { HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant) }
            item {
                Column(Modifier.fillMaxWidth()) {
                    BPSectionHeader(text = "Lịch sử")

                    if (uiState.isLoading) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator()
                        }
                    } else if (uiState.bpRecords.isEmpty()) {
                        Text(
                            text = "Chưa có dữ liệu",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(16.dp)
                        )
                    } else {
                        // Hiển thị 10 record gần nhất
                        uiState.bpRecords.take(10).forEach { record ->
                            BPHistoryItem(
                                time = record.date,
                                systolic = record.systolic,
                                diastolic = record.diastolic,
                                category = viewModel.getBPCategory(record.systolic, record.diastolic),
                                color = viewModel.getBPColor(record.systolic, record.diastolic)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))
                }
            }
        }
    }
}

/**
 * Tiêu đề cho mỗi phần
 */
@Composable
fun BPSectionHeader(text: String, modifier: Modifier = Modifier) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleLarge,
        modifier = modifier
            .fillMaxWidth()
            .padding(top = 16.dp, bottom = 8.dp)
    )
}

/**
 * Hàng nhập Huyết áp (với Number Picker)
 */
@Composable
fun BPInputRow(
    systolicState: PickerState,
    diastolicState: PickerState,
    systolicItems: List<String>,
    diastolicItems: List<String>
) {
    Column {
        // --- Hàng tiêu đề ---
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "Huyết áp (mmHg)",
                style = MaterialTheme.typography.bodyLarge
            )
        }
        Spacer(modifier = Modifier.height(8.dp))

        // --- Hàng chứa các Number Picker ---
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            val textStyle = MaterialTheme.typography.headlineLarge
            val itemHeight = 65.dp

            // Picker cho Tâm thu (Systolic)
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = "Tâm thu",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                NumberPicker(
                    state = systolicState,
                    items = systolicItems,
                    startIndex = systolicItems.indexOf("120"),
                    textStyle = textStyle,
                    modifier = Modifier.height(itemHeight * 3)
                )
            }

            // Dấu gạch chéo ngăn cách
            Text(
                text = "/",
                style = textStyle,
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            // Picker cho Tâm trương (Diastolic)
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = "Tâm trương",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                NumberPicker(
                    state = diastolicState,
                    items = diastolicItems,
                    startIndex = diastolicItems.indexOf("80"),
                    textStyle = textStyle,
                    modifier = Modifier.height(itemHeight * 3)
                )
            }
        }
    }
}

/**
 * Composable cho phần Thống kê Huyết áp
 */
@Composable
fun BPStatisticsView(
    systolicData: List<ChartDataPoint>,
    diastolicData: List<ChartDataPoint>
) {
    var selectedTabIndex by remember { mutableIntStateOf(0) }
    val tabs = listOf("Tuần", "Tháng", "Năm")

    val today = remember { LocalDate.now() }

    // Dùng remember để cache filtered data
    val filteredSystolic = remember(systolicData, selectedTabIndex) {
        when (selectedTabIndex) {
            0 -> {
                (0..6).mapNotNull { i ->
                    val date = today.minusDays((6 - i).toLong())
                    systolicData.find { it.date == date }
                }
            }
            1 -> {
                (0..9).mapNotNull { i ->
                    val targetDate = today.minusDays(((9 - i) * 3).toLong())
                    systolicData.filter {
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
                    systolicData.filter {
                        it.date.year == targetMonth.year && it.date.monthValue == targetMonth.monthValue
                    }.maxByOrNull { it.date }
                }.reversed()
            }
        }
    }

    val filteredDiastolic = remember(diastolicData, selectedTabIndex) {
        when (selectedTabIndex) {
            0 -> {
                (0..6).mapNotNull { i ->
                    val date = today.minusDays((6 - i).toLong())
                    diastolicData.find { it.date == date }
                }
            }
            1 -> {
                (0..9).mapNotNull { i ->
                    val targetDate = today.minusDays(((9 - i) * 3).toLong())
                    diastolicData.filter {
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
                    diastolicData.filter {
                        it.date.year == targetMonth.year && it.date.monthValue == targetMonth.monthValue
                    }.maxByOrNull { it.date }
                }.reversed()
            }
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
            if (filteredSystolic.isNotEmpty()) {
                Column {
                    // Biểu đồ Tâm thu
                    Text(
                        text = "Tâm thu (Systolic)",
                        style = MaterialTheme.typography.labelMedium,
                        color = Color(0xFFE74C3C),
                        modifier = Modifier.padding(bottom = 4.dp)
                    )
                    AppLineChart(
                        dataPoints = filteredSystolic,
                        lineColor = Color(0xFFE74C3C),
                        chartHeight = 100.dp,
                        unit = "mmHg",
                        showGrid = true,
                        showLabels = false,
                        dateFormat = dateFormat
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Biểu đồ Tâm trương
                    Text(
                        text = "Tâm trương (Diastolic)",
                        style = MaterialTheme.typography.labelMedium,
                        color = Color(0xFF3498DB),
                        modifier = Modifier.padding(bottom = 4.dp)
                    )
                    AppLineChart(
                        dataPoints = filteredDiastolic,
                        lineColor = Color(0xFF3498DB),
                        chartHeight = 100.dp,
                        unit = "mmHg",
                        showGrid = true,
                        showLabels = true,
                        dateFormat = dateFormat
                    )
                }
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

/**
 * Một hàng trong danh sách lịch sử huyết áp
 */
@Composable
fun BPHistoryItem(
    time: String,
    systolic: Long,
    diastolic: Long,
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
            text = "$systolic/$diastolic mmHg",
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.SemiBold,
            color = color
        )
    }
}
