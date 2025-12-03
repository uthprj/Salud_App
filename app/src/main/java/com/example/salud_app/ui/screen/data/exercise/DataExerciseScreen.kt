package com.example.salud_app.ui.screen.data.exercise

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview

import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.salud_app.components.AppScaffold
import com.example.salud_app.components.ScreenLevel
import com.example.salud_app.components.date_picker.AppDatePicker
import com.example.salud_app.components.date_picker.CompactDatePicker
import com.example.salud_app.components.dialog.ConfirmDialog
import com.example.salud_app.components.draw_chart.AppLineChart
import com.example.salud_app.components.draw_chart.ChartDataPoint
import com.example.salud_app.model.Exercise
import com.example.salud_app.model.ExerciseSummary
import com.example.salud_app.ui.screen.data.DataScreen
import com.example.salud_app.ui.screen.data.nutrition.DataNutritionScreen
import com.example.salud_app.ui.theme.Salud_AppTheme
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import kotlin.math.abs

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DataExerciseScreen(
    navController: NavController,
    onBackClicked: () -> Unit,
    exerciseViewModel: ExerciseViewModel = viewModel()
) {
    val uiState by exerciseViewModel.uiState.collectAsState()
    val context = LocalContext.current

    var currentDate by remember { mutableStateOf(LocalDate.now()) }
    var currentTime by remember { mutableStateOf(LocalTime.now()) }
    var selectedExerciseType by remember { mutableStateOf("Chạy bộ") }
    var exerciseName by remember { mutableStateOf("") }
    var durationInput by remember { mutableStateOf("") }
    var caloriesInput by remember { mutableStateOf("") }
    var expandedExerciseType by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }
    val timePickerState = rememberTimePickerState(
        initialHour = currentTime.hour,
        initialMinute = currentTime.minute,
        is24Hour = true
    )

    val exerciseTypes = listOf(
        "Chạy bộ", "Đi bộ", "Đạp xe", "Bơi lội", "Gym", 
        "Yoga", "HIIT", "Nhảy dây", "Leo núi", 
        "Cầu lông", "Bóng đá", "Bóng rổ", "Khác"
    )

    // Auto-estimate calories khi thay đổi loại bài tập hoặc thời gian
    LaunchedEffect(selectedExerciseType, durationInput) {
        val duration = durationInput.toIntOrNull() ?: 0
        if (duration > 0) {
            val estimated = exerciseViewModel.estimateCalories(selectedExerciseType, duration)
            caloriesInput = estimated.toInt().toString()
        }
    }

    // Hiển thị thông báo khi lưu thành công
    LaunchedEffect(uiState.saveSuccess) {
        if (uiState.saveSuccess) {
            android.widget.Toast.makeText(context, "Đã lưu bài tập thành công", android.widget.Toast.LENGTH_SHORT).show()
            exerciseViewModel.clearSaveSuccess()
            // Reset form
            exerciseName = ""
            durationInput = ""
            caloriesInput = ""
        }
    }

    // Hiển thị thông báo lỗi
    LaunchedEffect(uiState.error) {
        uiState.error?.let {
            android.widget.Toast.makeText(context, it, android.widget.Toast.LENGTH_SHORT).show()
            exerciseViewModel.clearError()
        }
    }

    // Time Picker Dialog
    if (showTimePicker) {
        AlertDialog(
            onDismissRequest = { showTimePicker = false },
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(top = 28.dp, start = 20.dp, end = 20.dp, bottom = 12.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Composable TimePicker của Material 3
                TimePicker(state = timePickerState)

                // Hàng chứa nút Hủy và OK
                Row(
                    modifier = Modifier
                        .padding(top = 12.dp)
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = { showTimePicker = false }) {
                        Text("Hủy")
                    }
                    TextButton(
                        onClick = {
                            currentTime = LocalTime.of(timePickerState.hour, timePickerState.minute)
                            showTimePicker = false
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
        title = "Luyện tập",
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
                ExerciseSummaryCard(summary = uiState.todaySummary)
            }

            // --- PHẦN: THÊM DỮ LIỆU ---
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Thêm bài tập",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Normal
                    )
                    TextButton(
                        onClick = {
                            val duration = durationInput.toIntOrNull() ?: 0
                            val calories = caloriesInput.toDoubleOrNull() ?: 0.0
                            
                            if (duration > 0) {
                                exerciseViewModel.saveExercise(
                                    date = currentDate,
                                    time = currentTime.format(DateTimeFormatter.ofPattern("HH:mm")),
                                    exerciseType = selectedExerciseType,
                                    exerciseName = exerciseName.ifBlank { selectedExerciseType },
                                    duration = duration,
                                    caloriesBurned = calories
                                )
                            } else {
                                android.widget.Toast.makeText(context, "Vui lòng nhập thời gian", android.widget.Toast.LENGTH_SHORT).show()
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

            // Date & Time Picker
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Thời gian",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                    
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Compact Date Picker
                        CompactDatePicker(
                            currentDate = currentDate,
                            onDateChange = { currentDate = it },
                            dateFormat = DateTimeFormatter.ofPattern("dd/MM/yyyy")
                        )
                        
                        // Time Picker
                        Row(
                            modifier = Modifier
                                .clickable { showTimePicker = true }
                                .padding(8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.AccessTime,
                                contentDescription = "Chọn giờ",
                                modifier = Modifier.size(20.dp)
                            )
                            Text(
                                text = currentTime.format(DateTimeFormatter.ofPattern("HH:mm")),
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }


            // Exercise Type Dropdown
            item {
                ExposedDropdownMenuBox(
                    expanded = expandedExerciseType,
                    onExpandedChange = { expandedExerciseType = it }
                ) {
                    OutlinedTextField(
                        value = selectedExerciseType,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Loại bài tập") },
                        trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedExerciseType)
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
                        expanded = expandedExerciseType,
                        onDismissRequest = { expandedExerciseType = false }
                    ) {
                        exerciseTypes.forEach { type ->
                            DropdownMenuItem(
                                text = { Text(type) },
                                onClick = {
                                    selectedExerciseType = type
                                    expandedExerciseType = false
                                },
                                leadingIcon = {
                                    Icon(
                                        imageVector = getExerciseIcon(type),
                                        contentDescription = null,
                                        tint = getExerciseColor(type)
                                    )
                                }
                            )
                        }
                    }
                }
            }

            // Exercise Name (Optional)
            item {
                OutlinedTextField(
                    value = exerciseName,
                    onValueChange = { exerciseName = it },
                    label = { Text("Tên bài tập (tùy chọn)") },
                    placeholder = { Text("VD: Chạy bộ buổi sáng") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF6AB9F5),
                        unfocusedBorderColor = Color.Gray.copy(alpha = 0.5f)
                    )
                )
            }

            // Duration Input
            item {
                OutlinedTextField(
                    value = durationInput,
                    onValueChange = { durationInput = it.filter { c -> c.isDigit() } },
                    label = { Text("Thời gian (phút)") },
                    placeholder = { Text("VD: 30") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    leadingIcon = {
                        Icon(Icons.Outlined.Timer, contentDescription = null)
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF6AB9F5),
                        unfocusedBorderColor = Color.Gray.copy(alpha = 0.5f)
                    )
                )
            }

            // Calories Input (Auto-estimated)
            item {
                OutlinedTextField(
                    value = caloriesInput,
                    onValueChange = { caloriesInput = it.filter { c -> c.isDigit() || c == '.' } },
                    label = { Text("Calo tiêu hao (kcal)") },
                    placeholder = { Text("Tự động ước tính") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    leadingIcon = {
                        Icon(Icons.Outlined.LocalFireDepartment, contentDescription = null, tint = Color(0xFFE74C3C))
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF6AB9F5),
                        unfocusedBorderColor = Color.Gray.copy(alpha = 0.5f)
                    )
                )
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
                ExerciseStatisticsView(chartData = uiState.chartDataPoints)
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
            } else if (uiState.exerciseRecords.isEmpty()) {
                item {
                    Text(
                        text = "Chưa có dữ liệu luyện tập hôm nay",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.Gray,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            } else {
                items(uiState.exerciseRecords) { exercise ->
                    ExerciseHistoryItem(
                        exercise = exercise,
                        onDelete = { exerciseViewModel.deleteExercise(exercise.id) }
                    )
                }
            }

            item { Spacer(modifier = Modifier.height(16.dp)) }
        }
    }
}

@Composable
fun ExerciseSummaryCard(summary: ExerciseSummary) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF0F8FF))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            SummaryItem(
                icon = Icons.Outlined.LocalFireDepartment,
                value = "${summary.totalCaloriesBurned.toInt()}",
                label = "Calo đốt",
                color = Color(0xFFE74C3C)
            )
            SummaryItem(
                icon = Icons.Outlined.Timer,
                value = "${summary.totalDuration}",
                label = "Phút",
                color = Color(0xFF3498DB)
            )
            SummaryItem(
                icon = Icons.Outlined.FitnessCenter,
                value = "${summary.totalExercises}",
                label = "Bài tập",
                color = Color(0xFF2ECC71)
            )
        }
    }
}

@Composable
fun SummaryItem(
    icon: ImageVector,
    value: String,
    label: String,
    color: Color
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
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
fun ExerciseStatisticsView(chartData: List<ChartDataPoint>) {
    var selectedTabIndex by remember { mutableIntStateOf(0) }
    val tabs = listOf("Tuần", "Tháng", "Năm")
    val today = remember { LocalDate.now() }

    val displayPoints = remember(chartData, selectedTabIndex) {
        when (selectedTabIndex) {
            0 -> {
                // Tuần: 7 ngày gần nhất
                (0..6).mapNotNull { i ->
                    val date = today.minusDays((6 - i).toLong())
                    chartData.find { it.date == date }
                }
            }
            1 -> {
                // Tháng: 10 điểm, cách nhau 3 ngày
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
                // Năm: 12 tháng
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
                    lineColor = Color(0xFFE74C3C),
                    chartHeight = 180.dp,
                    showGrid = true,
                    showLabels = true,
                    dateFormat = dateFormat,
                    unit = "kcal"
                )
            }
        }
    }
}

@Composable
fun ExerciseHistoryItem(
    exercise: Exercise,
    onDelete: () -> Unit
) {
    var showDeleteDialog by remember { mutableStateOf(false) }

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
                            getExerciseColor(exercise.exerciseType).copy(alpha = 0.2f),
                            RoundedCornerShape(12.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = getExerciseIcon(exercise.exerciseType),
                        contentDescription = null,
                        tint = getExerciseColor(exercise.exerciseType),
                        modifier = Modifier.size(24.dp)
                    )
                }

                Column {
                    Text(
                        text = exercise.exerciseName.ifBlank { exercise.exerciseType },
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium
                    )
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "${exercise.duration} phút",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.Gray
                        )
                        Text(
                            text = "•",
                            color = Color.Gray
                        )
                        Text(
                            text = exercise.time,
                            style = MaterialTheme.typography.bodySmall,
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
                        text = "${exercise.caloriesBurned.toInt()}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFE74C3C)
                    )
                    Text(
                        text = "kcal",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray
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

    ConfirmDialog(
        showDialog = showDeleteDialog,
        title = "Xác nhận xóa",
        message = "Bạn có chắc muốn xóa bài tập này?",
        icon = Icons.Default.Warning,
        iconTint = Color(0xFFE74C3C),
        confirmButtonText = "Xóa",
        dismissButtonText = "Hủy",
        confirmButtonColor = Color(0xFFE74C3C),
        onConfirm = {
            onDelete()
            showDeleteDialog = false
        },
        onDismiss = {
            showDeleteDialog = false
        }
    )
}

fun getExerciseIcon(type: String): ImageVector {
    return when (type) {
        "Chạy bộ" -> Icons.Outlined.DirectionsRun
        "Đi bộ" -> Icons.Outlined.DirectionsWalk
        "Đạp xe" -> Icons.Outlined.DirectionsBike
        "Bơi lội" -> Icons.Outlined.Pool
        "Gym" -> Icons.Outlined.FitnessCenter
        "Yoga" -> Icons.Outlined.SelfImprovement
        "HIIT" -> Icons.Outlined.FlashOn
        "Nhảy dây" -> Icons.Outlined.Sports
        "Leo núi" -> Icons.Outlined.Terrain
        "Cầu lông" -> Icons.Outlined.SportsTennis
        "Bóng đá" -> Icons.Outlined.SportsSoccer
        "Bóng rổ" -> Icons.Outlined.SportsBasketball
        else -> Icons.Outlined.FitnessCenter
    }
}

fun getExerciseColor(type: String): Color {
    return when (type) {
        "Chạy bộ" -> Color(0xFFE74C3C)
        "Đi bộ" -> Color(0xFF2ECC71)
        "Đạp xe" -> Color(0xFF3498DB)
        "Bơi lội" -> Color(0xFF1ABC9C)
        "Gym" -> Color(0xFF9B59B6)
        "Yoga" -> Color(0xFFF39C12)
        "HIIT" -> Color(0xFFE91E63)
        "Nhảy dây" -> Color(0xFFFF5722)
        "Leo núi" -> Color(0xFF795548)
        "Cầu lông" -> Color(0xFF00BCD4)
        "Bóng đá" -> Color(0xFF4CAF50)
        "Bóng rổ" -> Color(0xFFFF9800)
        else -> Color(0xFF607D8B)
    }
}
