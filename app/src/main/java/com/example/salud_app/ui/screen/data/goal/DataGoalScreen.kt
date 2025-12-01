package com.example.salud_app.ui.screen.data.goal

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.salud_app.R
import com.example.salud_app.components.AppScaffold
import com.example.salud_app.components.ScreenLevel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DataGoalScreen(
    navController: NavController,
    onBackClicked: () -> Unit,
    goalViewModel: GoalViewModel = viewModel()
) {
    val uiState by goalViewModel.uiState.collectAsState()
    val context = LocalContext.current

    // Initialize ViewModel với context
    LaunchedEffect(Unit) {
        goalViewModel.initialize(context)
    }

    // Mục tiêu dài hạn
    var targetWeightInput by remember { mutableStateOf("") }
    var targetHeightInput by remember { mutableStateOf("") }

    // Mục tiêu hàng ngày
    var caloriesInTargetInput by remember { mutableStateOf("") }
    var caloriesOutTargetInput by remember { mutableStateOf("") }
    var sleepTargetInput by remember { mutableStateOf("") }
    var exerciseTargetInput by remember { mutableStateOf("") }
    var stepsTargetInput by remember { mutableStateOf("") }
    var stepsInput by remember { mutableStateOf("") }

    // Cập nhật input khi data load xong
    LaunchedEffect(uiState.goal) {
        val longTerm = uiState.goal.longTermGoal
        val daily = uiState.goal.dailyGoal

        if (longTerm.targetWeight > 0) targetWeightInput = longTerm.targetWeight.toString()
        if (longTerm.targetHeight > 0) targetHeightInput = longTerm.targetHeight.toString()
        
        caloriesInTargetInput = daily.caloriesInTarget.toInt().toString()
        caloriesOutTargetInput = daily.caloriesOutTarget.toInt().toString()
        sleepTargetInput = (daily.sleepTarget / 60).toString() // Convert to hours
        exerciseTargetInput = daily.exerciseTarget.toString()
        stepsTargetInput = daily.stepsTarget.toString()
        stepsInput = daily.steps.toString()
    }

    // Hiển thị thông báo
    LaunchedEffect(uiState.saveSuccess) {
        if (uiState.saveSuccess) {
            Toast.makeText(context, "Đã lưu mục tiêu thành công", Toast.LENGTH_SHORT).show()
            goalViewModel.clearSaveSuccess()
        }
    }

    LaunchedEffect(uiState.error) {
        uiState.error?.let { error ->
            Toast.makeText(context, error, Toast.LENGTH_SHORT).show()
            goalViewModel.clearError()
        }
    }

    AppScaffold(
        navController = navController,
        title = stringResource(R.string.goal),
        screenLevel = ScreenLevel.SUB,
        onBackClicked = onBackClicked,
        showSaveButton = true,
        isSaving = uiState.isSaving,
        onSaveClicked = {
            // Lưu mục tiêu dài hạn
            val targetWeight = targetWeightInput.toDoubleOrNull() ?: 0.0
            val targetHeight = targetHeightInput.toDoubleOrNull() ?: 0.0
            goalViewModel.saveLongTermGoal(targetWeight, targetHeight)

            // Lưu mục tiêu hàng ngày
            val caloriesInTarget = caloriesInTargetInput.toDoubleOrNull() ?: 2000.0
            val caloriesOutTarget = caloriesOutTargetInput.toDoubleOrNull() ?: 500.0
            val sleepTarget = (sleepTargetInput.toIntOrNull() ?: 8) * 60 // Convert hours to minutes
            val exerciseTarget = exerciseTargetInput.toIntOrNull() ?: 30
            val stepsTarget = stepsTargetInput.toIntOrNull() ?: 10000
            goalViewModel.saveDailyTargets(caloriesInTarget, caloriesOutTarget, sleepTarget, exerciseTarget, stepsTarget)

            // Chỉ cập nhật bước chân thủ công khi không có sensor
            if (!uiState.hasStepSensor) {
                val steps = stepsInput.toIntOrNull() ?: 0
                goalViewModel.updateSteps(steps)
            }
        }
    ) { innerPadding ->

        if (uiState.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentPadding = PaddingValues(bottom = 16.dp)
            ) {
                // --- MỤC TIÊU DÀI HẠN ---
                item {
                    SectionHeader(text = "Mục tiêu dài hạn")
                }

                item {
                    LongTermGoalCard(
                        currentWeight = uiState.currentWeight,
                        targetWeightInput = targetWeightInput,
                        onTargetWeightChange = { targetWeightInput = it },
                        currentHeight = uiState.currentHeight,
                        targetHeightInput = targetHeightInput,
                        onTargetHeightChange = { targetHeightInput = it }
                    )
                }

                // --- MỤC TIÊU HÀNG NGÀY ---
                item {
                    SectionHeader(text = "Mục tiêu hàng ngày")
                    Text(
                        text = "Tự động đặt lại về 0 vào ngày mới",
                        fontSize = 12.sp,
                        color = Color.Gray,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }

                // Calo nạp
                item {
                    DailyGoalProgressCard(
                        icon = Icons.Default.Restaurant,
                        iconTint = Color(0xFFFB8C00),
                        title = "Calo nạp",
                        current = uiState.goal.dailyGoal.caloriesIn,
                        target = uiState.goal.dailyGoal.caloriesInTarget,
                        unit = "kcal",
                        targetInput = caloriesInTargetInput,
                        onTargetChange = { caloriesInTargetInput = it }
                    )
                }

                // Calo tiêu hao
                item {
                    DailyGoalProgressCard(
                        icon = Icons.Default.LocalFireDepartment,
                        iconTint = Color(0xFFE53935),
                        title = "Calo tiêu hao",
                        current = uiState.goal.dailyGoal.caloriesOut,
                        target = uiState.goal.dailyGoal.caloriesOutTarget,
                        unit = "kcal",
                        targetInput = caloriesOutTargetInput,
                        onTargetChange = { caloriesOutTargetInput = it }
                    )
                }

                // Giấc ngủ
                item {
                    DailyGoalProgressCard(
                        icon = Icons.Default.Bedtime,
                        iconTint = Color(0xFF1E88E5),
                        title = "Giấc ngủ",
                        current = uiState.goal.dailyGoal.sleepMinutes / 60.0,
                        target = uiState.goal.dailyGoal.sleepTarget / 60.0,
                        unit = "giờ",
                        targetInput = sleepTargetInput,
                        onTargetChange = { sleepTargetInput = it },
                        isHours = true
                    )
                }

                // Thời gian tập luyện
                item {
                    DailyGoalProgressCard(
                        icon = Icons.Default.FitnessCenter,
                        iconTint = Color(0xFF4CAF50),
                        title = "Thời gian tập luyện",
                        current = uiState.goal.dailyGoal.exerciseMinutes.toDouble(),
                        target = uiState.goal.dailyGoal.exerciseTarget.toDouble(),
                        unit = "phút",
                        targetInput = exerciseTargetInput,
                        onTargetChange = { exerciseTargetInput = it }
                    )
                }

                // Bước chân (có thể nhập thủ công hoặc đếm tự động)
                item {
                    StepsGoalCard(
                        steps = uiState.goal.dailyGoal.steps,
                        stepsTarget = uiState.goal.dailyGoal.stepsTarget,
                        stepsInput = stepsInput,
                        onStepsChange = { stepsInput = it },
                        stepsTargetInput = stepsTargetInput,
                        onStepsTargetChange = { stepsTargetInput = it },
                        hasStepSensor = uiState.hasStepSensor
                    )
                }

                item {
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }
    }
}

@Composable
private fun SectionHeader(text: String) {
    Text(
        text = text,
        fontWeight = FontWeight.Bold,
        fontSize = 18.sp,
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
    )
}

@Composable
private fun LongTermGoalCard(
    currentWeight: Double,
    targetWeightInput: String,
    onTargetWeightChange: (String) -> Unit,
    currentHeight: Double,
    targetHeightInput: String,
    onTargetHeightChange: (String) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Cân nặng
            val targetWeight = targetWeightInput.toDoubleOrNull() ?: 0.0
            val weightProgress = if (targetWeight > 0 && currentWeight > 0) {
                // Tính % tiến độ: nếu cần giảm cân hoặc tăng cân
                val diff = kotlin.math.abs(targetWeight - currentWeight)
                val initialDiff = if (currentWeight > targetWeight) currentWeight - targetWeight else targetWeight - currentWeight
                if (initialDiff > 0) ((initialDiff - diff) / initialDiff).coerceIn(0.0, 1.0).toFloat() else 1f
            } else 0f
            val weightProgressColor = when {
                weightProgress >= 1f -> Color(0xFF4CAF50)
                weightProgress >= 0.5f -> Color(0xFFFFC107)
                else -> Color(0xFFE91E63)
            }

            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.MonitorWeight,
                        contentDescription = null,
                        tint = Color(0xFFE91E63),
                        modifier = Modifier.size(32.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Cân nặng",
                            fontWeight = FontWeight.Medium,
                            fontSize = 16.sp
                        )
                        Text(
                            text = "Hiện tại: ${if (currentWeight > 0) "%.1f kg".format(currentWeight) else "Chưa có dữ liệu"}",
                            fontSize = 13.sp,
                            color = Color.Gray
                        )
                    }
                    OutlinedTextField(
                        value = targetWeightInput,
                        onValueChange = { if (it.isEmpty() || it.toDoubleOrNull() != null) onTargetWeightChange(it) },
                        modifier = Modifier.width(100.dp),
                        placeholder = { Text("Mục tiêu") },
                        suffix = { Text("kg") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        textStyle = LocalTextStyle.current.copy(fontSize = 14.sp)
                    )
                }
                
                // Progress bar cho cân nặng
                if (targetWeight > 0 && currentWeight > 0) {
                    Spacer(modifier = Modifier.height(8.dp))
                    LinearProgressIndicator(
                        progress = { weightProgress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp)),
                        color = weightProgressColor,
                        trackColor = Color(0xFFE0E0E0)
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = if (currentWeight > targetWeight) "Cần giảm: %.1f kg".format(currentWeight - targetWeight)
                                   else if (currentWeight < targetWeight) "Cần tăng: %.1f kg".format(targetWeight - currentWeight)
                                   else "Đã đạt mục tiêu!",
                            fontSize = 11.sp,
                            color = Color.Gray
                        )
                        Text(
                            text = "${(weightProgress * 100).toInt()}%",
                            fontSize = 12.sp,
                            color = weightProgressColor,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            HorizontalDivider()

            // Chiều cao
            val targetHeight = targetHeightInput.toDoubleOrNull() ?: 0.0
            val heightProgress = if (targetHeight > 0 && currentHeight > 0) {
                (currentHeight / targetHeight).coerceIn(0.0, 1.0).toFloat()
            } else 0f
            val heightProgressColor = when {
                heightProgress >= 1f -> Color(0xFF4CAF50)
                heightProgress >= 0.9f -> Color(0xFFFFC107)
                else -> Color(0xFF1E88E5)
            }

            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Height,
                        contentDescription = null,
                        tint = Color(0xFF1E88E5),
                        modifier = Modifier.size(32.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Chiều cao",
                            fontWeight = FontWeight.Medium,
                            fontSize = 16.sp
                        )
                        Text(
                            text = "Hiện tại: ${if (currentHeight > 0) "%.1f cm".format(currentHeight) else "Chưa có dữ liệu"}",
                            fontSize = 13.sp,
                            color = Color.Gray
                        )
                    }
                    OutlinedTextField(
                        value = targetHeightInput,
                        onValueChange = { if (it.isEmpty() || it.toDoubleOrNull() != null) onTargetHeightChange(it) },
                        modifier = Modifier.width(100.dp),
                        placeholder = { Text("Mục tiêu") },
                        suffix = { Text("cm") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        textStyle = LocalTextStyle.current.copy(fontSize = 14.sp)
                    )
                }

                // Progress bar cho chiều cao
                if (targetHeight > 0 && currentHeight > 0) {
                    Spacer(modifier = Modifier.height(8.dp))
                    LinearProgressIndicator(
                        progress = { heightProgress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp)),
                        color = heightProgressColor,
                        trackColor = Color(0xFFE0E0E0)
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = if (currentHeight < targetHeight) "Còn thiếu: %.1f cm".format(targetHeight - currentHeight)
                                   else "Đã đạt mục tiêu!",
                            fontSize = 11.sp,
                            color = Color.Gray
                        )
                        Text(
                            text = "${(heightProgress * 100).toInt()}%",
                            fontSize = 12.sp,
                            color = heightProgressColor,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun DailyGoalProgressCard(
    icon: ImageVector,
    iconTint: Color,
    title: String,
    current: Double,
    target: Double,
    unit: String,
    targetInput: String,
    onTargetChange: (String) -> Unit,
    isHours: Boolean = false
) {
    val progress = if (target > 0) (current / target).coerceIn(0.0, 1.0).toFloat() else 0f
    val progressColor = when {
        progress >= 1f -> Color(0xFF0B47A3)
        progress >= 0.5f -> Color(0xFF129A0D)
        else -> iconTint
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.size(28.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = title,
                        fontWeight = FontWeight.Medium,
                        fontSize = 15.sp
                    )
                    Text(
                        text = if (isHours) {
                            "%.1f / %.1f $unit".format(current, target)
                        } else {
                            "${current.toInt()} / ${target.toInt()} $unit"
                        },
                        fontSize = 13.sp,
                        color = Color.Gray
                    )
                }
                OutlinedTextField(
                    value = targetInput,
                    onValueChange = { if (it.isEmpty() || it.toIntOrNull() != null) onTargetChange(it) },
                    modifier = Modifier.width(90.dp),
                    placeholder = { Text("Mục tiêu") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    textStyle = LocalTextStyle.current.copy(fontSize = 14.sp)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Progress bar
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp)),
                color = progressColor,
                trackColor = Color(0xFFE0E0E0)
            )

            Text(
                text = "${(progress * 100).toInt()}%",
                fontSize = 12.sp,
                color = progressColor,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .align(Alignment.End)
                    .padding(top = 4.dp)
            )
        }
    }
}

@Composable
private fun StepsGoalCard(
    steps: Int,
    stepsTarget: Int,
    stepsInput: String,
    onStepsChange: (String) -> Unit,
    stepsTargetInput: String,
    onStepsTargetChange: (String) -> Unit,
    hasStepSensor: Boolean = false
) {
    val progress = if (stepsTarget > 0) (steps.toFloat() / stepsTarget).coerceIn(0f, 1f) else 0f
    val progressColor = when {
        progress >= 1f -> Color(0xFF4CAF50)
        progress >= 0.5f -> Color(0xFFFFC107)
        else -> Color(0xFF9C27B0)
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.DirectionsWalk,
                    contentDescription = null,
                    tint = Color(0xFF9C27B0),
                    modifier = Modifier.size(28.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Bước chân",
                        fontWeight = FontWeight.Medium,
                        fontSize = 15.sp
                    )
                    if (hasStepSensor) {
                        Text(
                            text = "Đang đếm tự động",
                            fontSize = 11.sp,
                            color = Color(0xFF4CAF50)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            if (hasStepSensor) {
                // Nếu có sensor, chỉ hiển thị số bước và mục tiêu
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "$steps",
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF9C27B0)
                        )
                        Text(
                            text = "bước hôm nay",
                            fontSize = 13.sp,
                            color = Color.Gray
                        )
                    }
                    OutlinedTextField(
                        value = stepsTargetInput,
                        onValueChange = { if (it.isEmpty() || it.toIntOrNull() != null) onStepsTargetChange(it) },
                        modifier = Modifier.width(120.dp),
                        label = { Text("Mục tiêu") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true
                    )
                }
            } else {
                // Không có sensor, cho phép nhập thủ công
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedTextField(
                        value = stepsInput,
                        onValueChange = { if (it.isEmpty() || it.toIntOrNull() != null) onStepsChange(it) },
                        modifier = Modifier.weight(1f),
                        label = { Text("Số bước hiện tại") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = stepsTargetInput,
                        onValueChange = { if (it.isEmpty() || it.toIntOrNull() != null) onStepsTargetChange(it) },
                        modifier = Modifier.weight(1f),
                        label = { Text("Mục tiêu") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Progress bar
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp)),
                color = progressColor,
                trackColor = Color(0xFFE0E0E0)
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "$steps / $stepsTarget bước",
                    fontSize = 12.sp,
                    color = Color.Gray
                )
                Text(
                    text = "${(progress * 100).toInt()}%",
                    fontSize = 12.sp,
                    color = progressColor,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}