package com.example.salud_app.ui.screen.data.sleep

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.salud_app.components.AppScaffold
import com.example.salud_app.components.ScreenLevel
import com.example.salud_app.components.date_picker.CompactDatePicker
import com.example.salud_app.components.draw_chart.AppLineChart
import com.example.salud_app.components.draw_chart.ChartDataPoint
import com.example.salud_app.model.Sleep
import com.example.salud_app.model.SleepSummary
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import kotlin.math.abs

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DataSleepScreen(
    navController: NavController,
    onBackClicked: () -> Unit,
    sleepViewModel: SleepViewModel = viewModel()
) {
    val uiState by sleepViewModel.uiState.collectAsState()
    val context = LocalContext.current

    var sleepDate by remember { mutableStateOf(LocalDate.now()) }
    var wakeDate by remember { mutableStateOf(LocalDate.now().plusDays(1)) }
    var sleepTime by remember { mutableStateOf(LocalTime.of(22, 0)) }
    var wakeTime by remember { mutableStateOf(LocalTime.of(6, 0)) }
    var selectedSleepType by remember { mutableStateOf("Giấc ngủ chính") }
    var selectedQuality by remember { mutableIntStateOf(3) }
    var expandedSleepType by remember { mutableStateOf(false) }
    var showSleepTimePicker by remember { mutableStateOf(false) }
    var showWakeTimePicker by remember { mutableStateOf(false) }

    val sleepTypes = listOf("Giấc ngủ chính", "Giấc ngủ phụ")
    val qualityLabels = listOf("Rất tệ", "Tệ", "Bình thường", "Tốt", "Rất tốt")

    // Tính thời gian ngủ
    val sleepDuration = remember(sleepDate, wakeDate, sleepTime, wakeTime) {
        val sleepDateTime = sleepDate.atTime(sleepTime)
        val wakeDateTime = wakeDate.atTime(wakeTime)
        val minutes = ChronoUnit.MINUTES.between(sleepDateTime, wakeDateTime).toInt()
        if (minutes > 0) minutes else 0
    }

    // Khi đổi loại giấc ngủ
    LaunchedEffect(selectedSleepType) {
        if (selectedSleepType == "Giấc ngủ chính") {
            wakeDate = sleepDate.plusDays(1)
        } else {
            wakeDate = sleepDate
        }
    }

    // Sleep Time Picker State
    val sleepTimePickerState = rememberTimePickerState(
        initialHour = sleepTime.hour,
        initialMinute = sleepTime.minute,
        is24Hour = true
    )

    // Wake Time Picker State
    val wakeTimePickerState = rememberTimePickerState(
        initialHour = wakeTime.hour,
        initialMinute = wakeTime.minute,
        is24Hour = true
    )

    // Hiển thị thông báo khi lưu thành công
    LaunchedEffect(uiState.saveSuccess) {
        if (uiState.saveSuccess) {
            android.widget.Toast.makeText(context, "Đã lưu giấc ngủ thành công", android.widget.Toast.LENGTH_SHORT).show()
            sleepViewModel.clearSaveSuccess()
        }
    }

    // Hiển thị thông báo lỗi
    LaunchedEffect(uiState.error) {
        uiState.error?.let {
            android.widget.Toast.makeText(context, it, android.widget.Toast.LENGTH_SHORT).show()
            sleepViewModel.clearError()
        }
    }

    // Sleep Time Picker Dialog
    if (showSleepTimePicker) {
        AlertDialog(
            onDismissRequest = { showSleepTimePicker = false },
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(top = 28.dp, start = 20.dp, end = 20.dp, bottom = 12.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("Chọn giờ đi ngủ", style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.height(16.dp))
                TimePicker(state = sleepTimePickerState)
                Row(
                    modifier = Modifier.padding(top = 12.dp).fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = { showSleepTimePicker = false }) {
                        Text("Hủy")
                    }
                    TextButton(
                        onClick = {
                            sleepTime = LocalTime.of(sleepTimePickerState.hour, sleepTimePickerState.minute)
                            showSleepTimePicker = false
                        }
                    ) {
                        Text("OK")
                    }
                }
            }
        }
    }

    // Wake Time Picker Dialog
    if (showWakeTimePicker) {
        AlertDialog(
            onDismissRequest = { showWakeTimePicker = false },
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(top = 28.dp, start = 20.dp, end = 20.dp, bottom = 12.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("Chọn giờ thức dậy", style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.height(16.dp))
                TimePicker(state = wakeTimePickerState)
                Row(
                    modifier = Modifier.padding(top = 12.dp).fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = { showWakeTimePicker = false }) {
                        Text("Hủy")
                    }
                    TextButton(
                        onClick = {
                            wakeTime = LocalTime.of(wakeTimePickerState.hour, wakeTimePickerState.minute)
                            showWakeTimePicker = false
                        }
                    ) {
                        Text("OK")
                    }
                }
            }
        }
    }

    AppScaffold(
        navController = navController,
        title = "Giấc ngủ",
        screenLevel = ScreenLevel.SUB,
        showMoreMenu = true,
        onBackClicked = onBackClicked
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // --- PHẦN: TỔNG QUAN HÔM NAY ---
            item {
                Text(
                    text = "Hôm nay",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Normal,
                    color = Color.Black.copy(alpha = 0.8f)
                )
            }

            item {
                SleepSummaryCard(summary = uiState.todaySummary)
            }

            // --- PHẦN: THÊM DỮ LIỆU ---
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Thêm giấc ngủ",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Normal
                    )
                    TextButton(
                        onClick = {
                            if (sleepDuration > 0) {
                                sleepViewModel.saveSleep(
                                    sleepDate = sleepDate,
                                    wakeDate = wakeDate,
                                    sleepType = selectedSleepType,
                                    startTime = sleepTime.format(DateTimeFormatter.ofPattern("HH:mm")),
                                    endTime = wakeTime.format(DateTimeFormatter.ofPattern("HH:mm")),
                                    duration = sleepDuration,
                                    quality = selectedQuality
                                )
                            } else {
                                android.widget.Toast.makeText(context, "Thời gian ngủ không hợp lệ", android.widget.Toast.LENGTH_SHORT).show()
                            }
                        },
                        enabled = !uiState.isSaving
                    ) {
                        if (uiState.isSaving) {
                            CircularProgressIndicator(modifier = Modifier.size(16.dp))
                        } else {
                            Text("Lưu")
                        }
                    }
                }
            }

            // Sleep Type Dropdown
            item {
                ExposedDropdownMenuBox(
                    expanded = expandedSleepType,
                    onExpandedChange = { expandedSleepType = it }
                ) {
                    OutlinedTextField(
                        value = selectedSleepType,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Loại giấc ngủ") },
                        trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedSleepType)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF6AB9F5),
                            unfocusedBorderColor = Color.Gray.copy(alpha = 0.5f)
                        )
                    )
                    ExposedDropdownMenu(
                        expanded = expandedSleepType,
                        onDismissRequest = { expandedSleepType = false }
                    ) {
                        sleepTypes.forEach { type ->
                            DropdownMenuItem(
                                text = { Text(type) },
                                onClick = {
                                    selectedSleepType = type
                                    expandedSleepType = false
                                },
                                leadingIcon = {
                                    Icon(
                                        imageVector = if (type == "Giấc ngủ chính") Icons.Outlined.NightsStay else Icons.Outlined.LightMode,
                                        contentDescription = null,
                                        tint = if (type == "Giấc ngủ chính") Color(0xFF3F51B5) else Color(0xFFFFA726)
                                    )
                                }
                            )
                        }
                    }
                }
            }

            // Sleep Date & Time
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFFFFFF).copy(alpha = 0.1f))
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "Đi ngủ",
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF3F51B5)
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            CompactDatePicker(
                                currentDate = sleepDate,
                                onDateChange = { 
                                    sleepDate = it
                                    if (selectedSleepType == "Giấc ngủ chính") {
                                        wakeDate = it.plusDays(1)
                                    } else {
                                        wakeDate = it
                                    }
                                },
                                dateFormat = DateTimeFormatter.ofPattern("dd/MM/yyyy")
                            )
                            Row(
                                modifier = Modifier
                                    .clickable { showSleepTimePicker = true }
                                    .padding(8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AccessTime,
                                    contentDescription = "Chọn giờ",
                                    modifier = Modifier.size(20.dp),
                                    tint = Color(0xFF3F51B5)
                                )
                                Text(
                                    text = sleepTime.format(DateTimeFormatter.ofPattern("HH:mm")),
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF3F51B5)
                                )
                            }
                        }
                    }
                }
            }

            // Wake Date & Time
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFFFFFF).copy(alpha = 0.1f))
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "Thức dậy",
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFFFA726)
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            CompactDatePicker(
                                currentDate = wakeDate,
                                onDateChange = { wakeDate = it },
                                dateFormat = DateTimeFormatter.ofPattern("dd/MM/yyyy")
                            )
                            Row(
                                modifier = Modifier
                                    .clickable { showWakeTimePicker = true }
                                    .padding(8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AccessTime,
                                    contentDescription = "Chọn giờ",
                                    modifier = Modifier.size(20.dp),
                                    tint = Color(0xFFFFA726)
                                )
                                Text(
                                    text = wakeTime.format(DateTimeFormatter.ofPattern("HH:mm")),
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFFFA726)
                                )
                            }
                        }
                    }
                }
            }

            // Duration Display
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF4CAF50).copy(alpha = 0.1f))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Bedtime,
                            contentDescription = null,
                            tint = Color(0xFF4CAF50),
                            modifier = Modifier.size(32.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Thời gian ngủ",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.Gray
                            )
                            Text(
                                text = "${sleepDuration / 60} giờ ${sleepDuration % 60} phút",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF4CAF50)
                            )
                        }
                    }
                }
            }

            // Quality Rating
            item {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Đánh giá giấc ngủ",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        (1..5).forEach { rating ->
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.clickable { selectedQuality = rating }
                            ) {
                                Icon(
                                    imageVector = if (rating <= selectedQuality) Icons.Filled.Star else Icons.Outlined.StarBorder,
                                    contentDescription = null,
                                    tint = if (rating <= selectedQuality) Color(0xFFFFD700) else Color.Gray,
                                    modifier = Modifier.size(36.dp)
                                )
                                Text(
                                    text = qualityLabels[rating - 1],
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (rating == selectedQuality) Color(0xFFFFD700) else Color.Gray
                                )
                            }
                        }
                    }
                }
            }

            // --- PHẦN: THỐNG KÊ ---
            item { HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant) }

            item {
                Text(
                    text = "Thống kê",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }

            item {
                SleepStatisticsView(chartData = uiState.chartDataPoints)
            }

            // --- PHẦN: LỊCH SỬ ---
            item { HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant) }

            item {
                Text(
                    text = "Lịch sử hôm nay",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }

            if (uiState.isLoading) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
            } else if (uiState.sleepRecords.isEmpty()) {
                item {
                    Text(
                        text = "Chưa có dữ liệu giấc ngủ hôm nay",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.Gray,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            } else {
                items(uiState.sleepRecords) { sleep ->
                    SleepHistoryItem(
                        sleep = sleep,
                        onDelete = { sleepViewModel.deleteSleep(sleep.id) }
                    )
                }
            }

            item { Spacer(modifier = Modifier.height(16.dp)) }
        }
    }
}

@Composable
fun SleepSummaryCard(summary: SleepSummary) {
    val hours = summary.totalDuration / 60
    val minutes = summary.totalDuration % 60

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFE8EAF6))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            SleepSummaryItem(
                icon = Icons.Outlined.Bedtime,
                value = "${hours}h ${minutes}m",
                label = "Thời gian",
                color = Color(0xFF3F51B5)
            )
            SleepSummaryItem(
                icon = Icons.Outlined.Star,
                value = String.format("%.1f", summary.averageQuality),
                label = "Đánh giá TB",
                color = Color(0xFFFFD700)
            )
            SleepSummaryItem(
                icon = Icons.Outlined.NightsStay,
                value = "${summary.totalSleeps}",
                label = "Lần ngủ",
                color = Color(0xFF9C27B0)
            )
        }
    }
}

@Composable
fun SleepSummaryItem(
    icon: ImageVector,
    value: String,
    label: String,
    color: Color
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = color,
            modifier = Modifier.size(28.dp)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = color
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = Color.Gray
        )
    }
}

@Composable
fun SleepStatisticsView(chartData: List<ChartDataPoint>) {
    var selectedTabIndex by remember { mutableIntStateOf(0) }
    val tabs = listOf("Tuần", "Tháng", "Năm")
    val today = remember { LocalDate.now() }

    val displayPoints = remember(chartData, selectedTabIndex) {
        when (selectedTabIndex) {
            0 -> {
                (0..6).mapNotNull { i ->
                    val date = today.minusDays((6 - i).toLong())
                    chartData.find { it.date == date }
                }
            }
            1 -> {
                (0..9).mapNotNull { i ->
                    val targetDate = today.minusDays(((9 - i) * 3).toLong())
                    chartData.filter {
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
                    chartData.filter {
                        it.date.year == targetMonth.year && it.date.monthValue == targetMonth.monthValue
                    }.maxByOrNull { it.date }
                }.reversed()
            }
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
                .background(Color(0xFFF5F5F5), RoundedCornerShape(16.dp))
                .padding(16.dp)
        ) {
            if (displayPoints.isEmpty()) {
                Text(
                    text = "Chưa có dữ liệu",
                    color = Color.Gray,
                    modifier = Modifier
                        .align(Alignment.Center)
                        .padding(32.dp)
                )
            } else {
                AppLineChart(
                    dataPoints = displayPoints,
                    lineColor = Color(0xFF3F51B5),
                    chartHeight = 180.dp,
                    showGrid = true,
                    showLabels = true,
                    dateFormat = dateFormat,
                    unit = "giờ"
                )
            }
        }
    }
}

@Composable
fun SleepHistoryItem(
    sleep: Sleep,
    onDelete: () -> Unit
) {
    var showDeleteDialog by remember { mutableStateOf(false) }
    val qualityLabels = listOf("Rất tệ", "Tệ", "Bình thường", "Tốt", "Rất tốt")
    val hours = sleep.duration / 60
    val minutes = sleep.duration % 60

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .background(
                            if (sleep.sleepType == "Giấc ngủ chính") Color(0xFF3F51B5).copy(alpha = 0.2f)
                            else Color(0xFFFFA726).copy(alpha = 0.2f),
                            RoundedCornerShape(12.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (sleep.sleepType == "Giấc ngủ chính") Icons.Outlined.NightsStay else Icons.Outlined.LightMode,
                        contentDescription = null,
                        tint = if (sleep.sleepType == "Giấc ngủ chính") Color(0xFF3F51B5) else Color(0xFFFFA726),
                        modifier = Modifier.size(24.dp)
                    )
                }

                Column {
                    Text(
                        text = sleep.sleepType,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = "${sleep.startTime} - ${sleep.endTime}",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.Gray
                        )
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        repeat(sleep.quality) {
                            Icon(
                                imageVector = Icons.Filled.Star,
                                contentDescription = null,
                                tint = Color(0xFFFFD700),
                                modifier = Modifier.size(12.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = qualityLabels.getOrElse(sleep.quality - 1) { "" },
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.Gray
                        )
                    }
                }
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "${hours}h ${minutes}m",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF3F51B5)
                    )
                }

                IconButton(
                    onClick = { showDeleteDialog = true },
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Xóa",
                        tint = Color.Gray,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Xác nhận xóa") },
            text = { Text("Bạn có chắc muốn xóa giấc ngủ này?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        onDelete()
                        showDeleteDialog = false
                    }
                ) {
                    Text("Xóa", color = Color.Red)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Hủy")
                }
            }
        )
    }
}