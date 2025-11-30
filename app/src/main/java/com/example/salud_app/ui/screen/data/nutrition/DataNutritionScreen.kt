package com.example.salud_app.ui.screen.data.nutrition

import android.annotation.SuppressLint
import android.app.TimePickerDialog
import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.outlined.DonutLarge
import androidx.compose.material.icons.outlined.LocalFireDepartment
import androidx.compose.material.icons.outlined.Spa
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
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
import com.example.salud_app.model.NutritionSummary
import com.example.salud_app.ui.screen.data.health.weight.WeightViewModel
import com.example.salud_app.ui.theme.Salud_AppTheme
import java.util.Calendar

@SuppressLint("NotConstructor")
@Composable
fun DataNutritionScreen(
    navController: NavController,
    onBackClicked: () -> Unit,
    nutritionViewModel: NutritionViewModel = viewModel()
) {
    val uiState by nutritionViewModel.uiState.collectAsState()
    val context = LocalContext.current
    
    var selectedTab by remember { mutableStateOf(0) }
    val mealTabs = listOf("Sáng", "Trưa", "Chiều", "Khác")

    var mealType by remember { mutableStateOf("Bữa chính") }
    var foodInput by remember { mutableStateOf("") }
    var currentDate by remember { mutableStateOf(java.time.LocalDate.now()) }
    var currentTime by remember { mutableStateOf(java.time.LocalTime.now()) }
    
    // Cập nhật khi có kết quả phân tích từ Gemini
    LaunchedEffect(uiState.analysisResult) {
        uiState.analysisResult?.let { result ->
            // Tự động lưu với dữ liệu phân tích được
            if (foodInput.isNotBlank()) {
                nutritionViewModel.saveMeal(
                    date = currentDate,
                    time = currentTime.format(java.time.format.DateTimeFormatter.ofPattern("HH:mm")),
                    mealType = mealTabs[selectedTab],
                    mealCategory = mealType,
                    mealName = foodInput,
                    calories = result.calories,
                    protein = result.protein,
                    carbs = result.carbs,
                    fat = result.fat
                )
                nutritionViewModel.clearAnalysisResult()
            }
        }
    }
    
    // Hiển thị lỗi phân tích
    LaunchedEffect(uiState.analysisError) {
        uiState.analysisError?.let {
            android.widget.Toast.makeText(context, it, android.widget.Toast.LENGTH_SHORT).show()
            nutritionViewModel.clearAnalysisResult()
        }
    }
    
    // Hiển thị thông báo khi lưu thành công
    LaunchedEffect(uiState.saveSuccess) {
        if (uiState.saveSuccess) {
            android.widget.Toast.makeText(context, "Đã lưu món ăn thành công", android.widget.Toast.LENGTH_SHORT).show()
            nutritionViewModel.clearSaveSuccess()
            // Reset form
            foodInput = ""
        }
    }
    
    // Hiển thị thông báo lỗi
    LaunchedEffect(uiState.error) {
        uiState.error?.let {
            android.widget.Toast.makeText(context, it, android.widget.Toast.LENGTH_SHORT).show()
            nutritionViewModel.clearError()
        }
    }

    AppScaffold(
        navController = navController,
        title = "Dinh dưỡng",
        screenLevel = ScreenLevel.SUB,
        showMoreMenu = true,
        onBackClicked = onBackClicked
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            Text(
                text = "Hôm nay",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Normal,
                color = Color.Black.copy(alpha = 0.8f)
            )

            NutritionGrid(summary = uiState.todaySummary)

            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Thêm dữ liệu",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Normal
                    )
                    TextButton(
                        onClick = {
                            if (foodInput.isNotBlank()) {
                                // Gọi Gemini để phân tích món ăn
                                nutritionViewModel.analyzeMealWithGemini(foodInput)
                            } else {
                                android.widget.Toast.makeText(context, "Vui lòng nhập tên món ăn", android.widget.Toast.LENGTH_SHORT).show()
                            }
                        },
                        enabled = !uiState.isSaving && !uiState.isAnalyzing
                    ) {
                        Text(
                            text = when {
                                uiState.isAnalyzing -> "Đang phân tích..."
                                uiState.isSaving -> "Đang lưu..."
                                else -> "Thêm"
                            },
                            color = Color(0xFF4A90E2),
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    mealTabs.forEachIndexed { index, title ->
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier
                                .clickable { selectedTab = index }
                                .padding(vertical = 8.dp)
                        ) {
                            Text(
                                text = title,
                                fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal,
                                color = if (selectedTab == index) Color(0xFF4A90E2) else Color.Black
                            )
                            if (selectedTab == index) {
                                Box(
                                    modifier = Modifier
                                        .padding(top = 4.dp)
                                        .height(2.dp)
                                        .width(20.dp)
                                        .background(Color(0xFF4A90E2))
                                )
                            }
                        }
                    }
                }
            }

            InputFormSection(
                mealType = mealType,
                foodInput = foodInput,
                currentDate = currentDate,
                currentTime = currentTime,
                onMealTypeChange = { mealType = it },
                onFoodInputChange = { foodInput = it },
                onDateChange = { currentDate = it },
                onTimeChange = { currentTime = it }
            )
        }
    }
}

@Composable
fun NutritionGrid(summary: NutritionSummary) {
    val carbsPercent = if (summary.totalCalories > 0) (summary.totalCarbs * 4 / summary.totalCalories * 100).toInt() else 45
    val proteinPercent = if (summary.totalCalories > 0) (summary.totalProtein * 4 / summary.totalCalories * 100).toInt() else 45
    val fatPercent = if (summary.totalCalories > 0) (summary.totalFat * 9 / summary.totalCalories * 100).toInt() else 45
    
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            NutritionCard(
                modifier = Modifier.weight(1f),
                title = "Tổng calo",
                value = summary.totalCalories.toInt().toString(),
                unit = "kcal",
                subText = "Mục tiêu: ${summary.targetCalories.toInt()} kcal",
                icon = Icons.Filled.LocalFireDepartment,
                backgroundColor = Color(0xFFFFF9C4),
                iconColor = Color(0xFFAF861D)
            )
            NutritionCard(
                modifier = Modifier.weight(1f),
                title = "Carb",
                value = summary.totalCarbs.toInt().toString(),
                unit = "g",
                subText = "$carbsPercent % tổng calo",
                icon = Icons.Outlined.Spa,
                backgroundColor = Color(0xFFC8E6C9),
                iconColor = Color(0xFF2E7D32)
            )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            NutritionCard(
                modifier = Modifier.weight(1f),
                title = "Protein",
                value = summary.totalProtein.toInt().toString(),
                unit = "g",
                subText = "$proteinPercent % tổng calo",
                icon = Icons.Outlined.DonutLarge,
                backgroundColor = Color(0xFFB3E5FC),
                iconColor = Color(0xFF0277BD)
            )
            NutritionCard(
                modifier = Modifier.weight(1f),
                title = "Fat",
                value = summary.totalFat.toInt().toString(),
                unit = "g",
                subText = "$fatPercent % tổng calo",
                icon = Icons.Outlined.LocalFireDepartment,
                backgroundColor = Color(0xFFFFCCBC),
                iconColor = Color(0xFFD84315)
            )
        }
    }
}

@Composable
fun NutritionCard(
    modifier: Modifier = Modifier,
    title: String,
    value: String,
    unit: String,
    subText: String,
    icon: ImageVector,
    backgroundColor: Color,
    iconColor: Color
) {


    Card(
        modifier = modifier.height(160.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = backgroundColor)
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF5D5D5D)
                )
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconColor
                )
            }

            Column (
//                horizontalAlignment = Alignment.CenterHorizontally
                modifier = Modifier.fillMaxHeight(),
                verticalArrangement = Arrangement.Center
            )
            {

                Row(verticalAlignment = Alignment.Bottom) {
                    Text(
                        text = value,
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = unit,
                        style = MaterialTheme.typography.bodyLarge,
                        color = Color.Black.copy(alpha = 0.7f),
                        modifier = Modifier.padding(bottom = 4.dp)
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                if (subText.contains("Mục tiêu")) {
                    Text(text = subText, style = MaterialTheme.typography.bodyMedium, color = Color.Black)
                } else {
                    Text(text = subText, style = MaterialTheme.typography.bodyMedium, color = Color.Black)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class) // Cần thêm OptIn cho TimePicker
@Composable
fun InputFormSection(
    mealType: String,
    foodInput: String,
    currentDate: java.time.LocalDate,
    currentTime: java.time.LocalTime,
    onMealTypeChange: (String) -> Unit,
    onFoodInputChange: (String) -> Unit,
    onDateChange: (java.time.LocalDate) -> Unit,
    onTimeChange: (java.time.LocalTime) -> Unit
) {
    var showTimePicker by remember { mutableStateOf(false) }

    // --- DIALOG MỚI SỬ DỤNG TIME PICKER CỦA MATERIAL 3 ---
    if (showTimePicker) {
        val timePickerState = rememberTimePickerState(
            initialHour = currentTime.hour,
            initialMinute = currentTime.minute,
            is24Hour = true
        )

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
                            onTimeChange(java.time.LocalTime.of(timePickerState.hour, timePickerState.minute))
                            showTimePicker = false
                        }
                    ) {
                        Text("OK")
                    }
                }
            }
        }
    }


    Column(verticalArrangement = Arrangement.spacedBy(20.dp)) {
        var expanded by remember { mutableStateOf(false) }
        val mealTypes = listOf("Bữa chính", "Bữa phụ")

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Loại bữa ăn",
                modifier = Modifier.weight(1f),
                color = Color.Black.copy(alpha = 0.8f)
            )
            ExposedDropdownMenuBox(
                expanded = expanded,
                onExpandedChange = { expanded = !expanded },
                modifier = Modifier.weight(2f)
            ) {
                OutlinedTextField(
                    value = mealType,
                    onValueChange = {},
                    readOnly = true,
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                    modifier = Modifier
                        .menuAnchor()
                        .fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedBorderColor = Color.LightGray,
                        focusedBorderColor = Color(0xFF4A90E2)
                    )
                )
                ExposedDropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    mealTypes.forEach { selectionOption ->
                        DropdownMenuItem(
                            text = { Text(selectionOption) },
                            onClick = {
                                onMealTypeChange(selectionOption)
                                expanded = false
                            }
                        )
                    }
                }
            }
        }

        // --- THỜI GIAN (ĐÃ SỬA LẠI) ---
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Thời gian",
                modifier = Modifier.weight(1f),
                color = Color.Black.copy(alpha = 0.8f)
            )

            Row(
                modifier = Modifier.weight(2f),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Component chọn ngày
                CompactDatePicker(
                    currentDate = currentDate,
                    onDateChange = { newDate -> onDateChange(newDate) },
                )

                // Text hiển thị giờ, có thể nhấn để mở Time Picker
                Text(
                    text = currentTime.format(java.time.format.DateTimeFormatter.ofPattern("HH:mm")),
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier
                        .clickable { showTimePicker = true } // Mở dialog khi nhấn
                        .padding(8.dp) // Thêm padding để dễ nhấn hơn
                )
            }
        }

        // --- NHẬP MÓN ĂN (KHÔNG ĐỔI) ---
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.Top
        ) {
            Text(
                text = "Nhập món ăn",
                modifier = Modifier
                    .weight(1f)
                    .padding(top = 12.dp),
                color = Color.Black.copy(alpha = 0.8f)
            )
            OutlinedTextField(
                value = foodInput,
                onValueChange = onFoodInputChange,
                modifier = Modifier
                    .weight(2f)
                    .height(100.dp),
                placeholder = { Text("Nhập món ăn tại đây", color = Color.LightGray) },
                shape = RoundedCornerShape(8.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedBorderColor = Color.LightGray,
                    focusedBorderColor = Color(0xFF4A90E2)
                )
            )
        }
    }
}



@Preview(showBackground = true)
@Composable
fun DataNutritionScreenPreview() {
    val navController = rememberNavController()
    Salud_AppTheme {
        DataNutritionScreen(navController = navController, onBackClicked = {})
    }
}
