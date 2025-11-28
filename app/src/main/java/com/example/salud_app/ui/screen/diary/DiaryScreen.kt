package com.example.salud_app.ui.screen.diary

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
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
import com.example.salud_app.components.date_picker.AppDatePicker
import com.example.salud_app.ui.theme.Salud_AppTheme
import java.time.LocalDate
import com.example.salud_app.model.Task

@Composable
fun DiaryScreen(
    navController: androidx.navigation.NavController = rememberNavController(),
    tasks: List<Task> = sampleTasks
) {
    var currentDate by remember { mutableStateOf(LocalDate.now()) }
    var expandedTaskId by remember { mutableStateOf<String?>(null) }

    Salud_AppTheme {
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

                // Sử dụng AppDatePicker component dùng chung
                AppDatePicker(
                    currentDate = currentDate,
                    onDateChange = { newDate -> currentDate = newDate },
                    label = "Nhật ký "
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
        }
    }
}

@Composable
fun TaskCardList(
    tasks: List<Task>,
    expandedTaskId: String?,
    onCardClick: (Task) -> Unit
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
                            "EAT" -> "🍽️ Ăn uống"
                            "SLEEP" -> "😴 Ngủ nghỉ"
                            "EXERCISE" -> "🏃‍♂️ Tập luyện"
                            else -> "❓ Khác"
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
    Task(
        id = "1",
        userId = "user123",
        type = "EAT",
        date = "2025-11-14",
        description = "Ăn sáng: 2 trứng, 1 ly sữa",
        isCompleted = false
    ),
    Task(
        id = "2",
        userId = "user123",
        type = "SLEEP",
        date = "2025-11-14",
        description = "Ngủ trưa 30 phút",
        isCompleted = false
    ),
    Task(
        id = "3",
        userId = "user123",
        type = "EXERCISE",
        date = "2025-11-14",
        description = "Chạy bộ 20 phút",
        isCompleted = true
    )
)

@Preview
@Composable
fun PreviewDiary() {
    DiaryScreen()
}
