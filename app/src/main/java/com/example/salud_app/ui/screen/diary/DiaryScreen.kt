package com.example.salud_app.ui.screen.diary

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.rememberNavController
import com.example.salud_app.components.AppScaffold
import com.example.salud_app.components.ScreenLevel
import com.example.salud_app.model.TaskType
import com.example.salud_app.model.Tasks
import com.example.salud_app.ui.theme.Salud_AppTheme
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DiaryScreen(
    tasks: List<Tasks> = sampleTasks
) {
    var currentDate by remember { mutableStateOf(LocalDate.now()) }
    var expandedTaskId by remember { mutableStateOf<Long?>(null) }

    // State mở DatePicker
    var showDatePicker by remember { mutableStateOf(false) }
    val datePickerState = rememberDatePickerState()

    Salud_AppTheme {
        val navController = rememberNavController()

        AppScaffold(
            navController = navController,
            title = "Nhật ký",
            screenLevel = ScreenLevel.MAIN
        ) { paddingValues ->

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .background(MaterialTheme.colorScheme.background)
            ) {

                DateSelector(
                    currentDate = currentDate,
                    onDateChange = { newDate -> currentDate = newDate },
                    onOpenDatePicker = { showDatePicker = true }
                )

                Spacer(Modifier.height(12.dp))

                TaskCardList(
                    tasks = tasks,
                    expandedTaskId = expandedTaskId,
                    onCardClick = { task ->
                        expandedTaskId =
                            if (expandedTaskId == task.id) null else task.id
                    }
                )
            }

            // ----------------------
            //     DATE PICKER UI
            // ----------------------
            if (showDatePicker) {
                DatePickerDialog(
                    onDismissRequest = { showDatePicker = false },
                    confirmButton = {
                        TextButton(
                            onClick = {
                                val millis = datePickerState.selectedDateMillis
                                if (millis != null) {
                                    currentDate = Instant.ofEpochMilli(millis)
                                        .atZone(ZoneId.systemDefault())
                                        .toLocalDate()
                                }
                                showDatePicker = false
                            }
                        ) {
                            Text("Done")
                        }
                    }
                ) {
                    DatePicker(state = datePickerState)
                }
            }
        }
    }
}

@Composable
fun DateSelector(
    currentDate: LocalDate,
    onDateChange: (LocalDate) -> Unit,
    onOpenDatePicker: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF4EA0AC), RoundedCornerShape(12.dp))
            .padding(horizontal = 20.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text("<",
            fontWeight = FontWeight.Bold,
            fontSize = MaterialTheme.typography.titleLarge.fontSize,
            modifier = Modifier.clickable { onDateChange(currentDate.minusDays(1)) }
        )

        Text(
            text = currentDate.toString(),
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.clickable { onOpenDatePicker() }
        )

        Text(">",
            fontWeight = FontWeight.Bold,
            fontSize = MaterialTheme.typography.titleLarge.fontSize,
            modifier = Modifier.clickable { onDateChange(currentDate.plusDays(1)) }
        )
    }
}

@Composable
fun TaskCardList(
    tasks: List<Tasks>,
    expandedTaskId: Long?,
    onCardClick: (Tasks) -> Unit
) {
    Column(
        Modifier.fillMaxWidth().padding(horizontal = 16.dp)
    ) {
        tasks.forEach { task ->

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp)
                    .background(Color.White, RoundedCornerShape(12.dp))
                    .clickable { onCardClick(task) }
                    .padding(16.dp)
            ) {
                Column {
                    Text(
                        text = when (task.type) {
                            TaskType.Eat -> "🍽️ Ăn uống"
                            TaskType.Sleep -> "😴 Ngủ nghỉ"
                            TaskType.Exercise -> "🏃‍♂️ Tập luyện"
                        },
                        fontWeight = FontWeight.Bold,
                        fontSize = MaterialTheme.typography.titleMedium.fontSize
                    )

                    Spacer(Modifier.height(4.dp))
                    Text(task.description, color = Color.DarkGray)
                }
            }

            AnimatedVisibility(
                visible = expandedTaskId == task.id,
                enter = expandVertically(),
                exit = shrinkVertically()
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 6.dp)
                        .background(Color(0xFFFFFFFF), RoundedCornerShape(12.dp))
                        .padding(16.dp)
                ) {
                    Column {
                        Text(
                            text = "Chi tiết",
                            fontWeight = FontWeight.Bold,
                            fontSize = MaterialTheme.typography.titleMedium.fontSize
                        )
                        Spacer(Modifier.height(8.dp))

                        Text("Loại: ${task.type}")
                        Text("Ngày: ${task.date}")
                        Text("Mô tả: ${task.description}")
                    }
                }
            }
        }
    }
}

// ------------------------
// DỮ LIỆU MẪU
// ------------------------
val sampleTasks = listOf(
    Tasks(
        id = 1,
        userId = "user123",
        type = TaskType.Eat,
        date = "2025-11-14",
        description = "Ăn sáng: 2 trứng, 1 ly sữa"
    ),
    Tasks(
        id = 2,
        userId = "user123",
        type = TaskType.Sleep,
        date = "2025-11-14",
        description = "Ngủ trưa 30 phút"
    ),
    Tasks(
        id = 3,
        userId = "user123",
        type = TaskType.Exercise,
        date = "2025-11-14",
        description = "Chạy bộ 20 phút"
    )
)

@Preview
@Composable
fun PreviewDiary() {
    DiaryScreen()
}
