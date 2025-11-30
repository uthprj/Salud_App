package com.example.salud_app.ui.screen.data.health.weight
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
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
import com.example.salud_app.components.number_picker.PickerState
import com.example.salud_app.components.number_picker.rememberPickerState
import com.example.salud_app.ui.screen.data.health.weight.*
import com.example.salud_app.ui.theme.Salud_AppTheme
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import kotlin.math.abs

@Composable
fun DataHealthWeightScreen(
    navController: NavController,
    onBackClicked: () -> Unit,
    viewModel: com.example.salud_app.ui.screen.data.health.weight.WeightViewModel = viewModel()
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()
    
    var currentDate by remember { mutableStateOf(LocalDate.now()) }

    // Tạo danh sách giá trị cho picker
    val integerItems = remember { (0..200).map { it.toString() } }
    val fractionalItems = remember { (0..99).map { "%02d".format(it) } }
    
    // Trạng thái picker
    val integerState = rememberPickerState("70")
    val fractionalState = rememberPickerState("00")

    // Hiển thị Toast khi lưu thành công
    LaunchedEffect(uiState.saveSuccess) {
        if (uiState.saveSuccess) {
            Toast.makeText(context, "Đã lưu cân nặng thành công!", Toast.LENGTH_SHORT).show()
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
        title = stringResource(R.string.weight),
        screenLevel = ScreenLevel.SUB,
        showMoreMenu = true,
        onBackClicked = onBackClicked,
        showSaveButton = true,
        onSaveClicked = {
            // Lấy giá trị cân nặng từ picker
            val intPart = integerState.selectedItem.toIntOrNull() ?: 70
            val fracPart = fractionalState.selectedItem.toIntOrNull() ?: 0
            val weight = intPart + fracPart / 100.0
            
            // Lưu lên Firebase
            viewModel.saveWeight(currentDate, weight)
        }
    ) { innerPadding ->

        // Hiển thị loading indicator khi đang lưu
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
                    SectionHeader(text = "Thêm dữ liệu")
                    AppDatePicker(
                        currentDate = currentDate,
                        onDateChange = { newDate -> currentDate = newDate },
                        label = "Ngày"
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    WeightInputRow(
                        integerState = integerState,
                        fractionalState = fractionalState,
                        integerItems = integerItems,
                        fractionalItems = fractionalItems
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                }
            }

            // Đường phân cách
            item { HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant) }

            // --- PHẦN: THỐNG KÊ ---
            item {
                Column(Modifier.padding(horizontal = 16.dp)) {
                    SectionHeader(text = "Thống kê")
                }
            }
            item {
                // Sử dụng dữ liệu đã cache từ ViewModel
                StatisticsView(weightData = uiState.chartDataPoints)
            }

            // --- PHẦN: DANH SÁCH LỊCH SỬ ---

            item { HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant) }
            item {
                Column(Modifier.fillMaxWidth()) {
                    SectionHeader(text = "Lịch sử")
                    
                    if (uiState.isLoading) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator()
                        }
                    } else if (uiState.weightRecords.isEmpty()) {
                        Text(
                            text = "Chưa có dữ liệu",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(16.dp)
                        )
                    } else {
                        // Hiển thị 10 record gần nhất
                        uiState.weightRecords.take(10).forEach { record ->
                            WeightHistoryItem(
                                time = record.date,
                                weight = "${record.weight} kg"
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
fun SectionHeader(text: String, modifier: Modifier = Modifier) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleLarge,
        modifier = modifier
            .fillMaxWidth()
            .padding(top = 16.dp, bottom = 8.dp)
    )
}

/**
 * Hàng hiển thị Thời gian (Ngày & Giờ)
 */
//@Composable
//fun TimePickerRow(
//    date: String,
//    time: String,
//    onDateClick: () -> Unit,
//    onTimeClick: () -> Unit
//) {
//    Row(
//        modifier = Modifier.fillMaxWidth(),
//        verticalAlignment = Alignment.CenterVertically,
//        horizontalArrangement = Arrangement.SpaceBetween
//    ) {
//        Text(
//            text = "Thời gian",
//            style = MaterialTheme.typography.bodyLarge
//        )
//        Row(horizontalArrangement = Arrangement.spacedBy(1.dp)) {
//            Text(
//                text = date,
//                style = MaterialTheme.typography.bodyLarge,
//                color = MaterialTheme.colorScheme.primary,
//                modifier = Modifier
//                    .clickable(onClick = onDateClick)
//                    .padding(vertical = 4.dp)
//            )
//            Text(
//                text = time,
//                style = MaterialTheme.typography.bodyLarge,
//                color = MaterialTheme.colorScheme.primary,
//                modifier = Modifier
//                    .clickable(onClick = onTimeClick)
//                    .padding(vertical = 4.dp)
//            )
//        }
//    }
//}

/**
 * Hàng nhập Cân nặng (với Number Picker)
 */
@Composable
fun WeightInputRow(
    integerState: PickerState,
    fractionalState: PickerState,
    integerItems: List<String>,
    fractionalItems: List<String>
) {
    Column {
        // --- Hàng tiêu đề và đơn vị ---
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "Cân nặng (kg)",
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

            // 3. Picker cho phần nguyên
            NumberPicker(
                state = integerState,
                items = integerItems,
                startIndex = integerItems.indexOf("70"),
                textStyle = textStyle,
                modifier = Modifier
                    .weight(1f)
                    .height(itemHeight * 3)
            )

            // Dấu chấm ngăn cách
            Text(
                text = ".",
                style = textStyle,
                modifier = Modifier.padding(horizontal = 8.dp)
            )

            // 4. Picker cho phần thập phân
            NumberPicker(
                state = fractionalState,
                items = fractionalItems,
                startIndex = fractionalItems.indexOf("00"),
                textStyle = textStyle,
                modifier = Modifier
                    .weight(1f)
                    .height(itemHeight * 3)
            )
        }
    }
}


/**
 * Composable cho phần Thống kê (Tab và Biểu đồ)
 */
@Composable
fun StatisticsView(
    weightData: List<ChartDataPoint> = emptyList()
) {
    var selectedTabIndex by remember { mutableIntStateOf(0) }
    val tabs = listOf("Tuần", "Tháng", "Năm")

    val today = remember { LocalDate.now() }
    
    // Tạo danh sách các điểm hiển thị dựa vào tab - dùng remember để cache
    val displayPoints = remember(weightData, selectedTabIndex) {
        when (selectedTabIndex) {
            0 -> {
                // Tuần: 7 điểm, mỗi điểm là 1 ngày
                (0..6).mapNotNull { i ->
                    val date = today.minusDays((6 - i).toLong())
                    weightData.find { it.date == date }
                }
            }
            1 -> {
                // Tháng: 10 điểm, cách nhau 3 ngày (27 ngày)
                (0..9).mapNotNull { i ->
                    val targetDate = today.minusDays(((9 - i) * 3).toLong())
                    // Tìm dữ liệu gần nhất trong khoảng +-1 ngày
                    weightData.filter { 
                        val diff = abs(ChronoUnit.DAYS.between(it.date, targetDate))
                        diff <= 1
                    }.minByOrNull {
                        abs(ChronoUnit.DAYS.between(it.date, targetDate))
                    }
                }
            }
            else -> {
                // Năm: 12 điểm, cách nhau 30 ngày (330 ngày ~ 11 tháng)
                (0..11).mapNotNull { i ->
                    val targetDate = today.minusDays(((11 - i) * 30).toLong())
                    // Tìm dữ liệu gần nhất trong khoảng +-15 ngày
                    weightData.filter { 
                        val diff = abs(ChronoUnit.DAYS.between(it.date, targetDate))
                        diff <= 15
                    }.minByOrNull {
                        abs(ChronoUnit.DAYS.between(it.date, targetDate))
                    }
                }
            }
        }
    }

    // Format ngày theo tab
    val dateFormat = when (selectedTabIndex) {
        0 -> DateTimeFormatter.ofPattern("dd/MM")  // Tuần: dd/MM
        1 -> DateTimeFormatter.ofPattern("dd/MM")  // Tháng: dd/MM
        else -> DateTimeFormatter.ofPattern("'T'.MM")
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
                    lineColor = Color(0xFF6AB9F5),
                    chartHeight = 220.dp,
                    unit = "kg",
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



/**
 * Một hàng trong danh sách lịch sử cân nặng
 */
@Composable
fun WeightHistoryItem(time: String, weight: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = time,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = weight,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
fun HistoryView(){

    Box(
        modifier = Modifier
            .fillMaxWidth()
    ){
        Column(){

        }
    }

}

@Preview(showBackground = true)
@Composable
fun DataHealthWeightScreenPreview() {
    val navController = rememberNavController()

    Salud_AppTheme {
        DataHealthWeightScreen(navController = navController, onBackClicked = {})
    }
}

