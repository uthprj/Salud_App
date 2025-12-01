package com.example.salud_app.ui.screen.diary

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.SpeakerNotesOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.rememberNavController
import com.example.salud_app.R
import com.example.salud_app.components.AppScaffold
import com.example.salud_app.components.ScreenLevel
import com.example.salud_app.components.date_picker.AppDatePicker
import com.example.salud_app.ui.theme.Salud_AppTheme
import java.time.LocalDate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DiaryScreen(
    navController: androidx.navigation.NavController = rememberNavController(),
    diaryViewModel: DiaryViewModel = viewModel()
) {
    val uiState by diaryViewModel.uiState.collectAsState()
    var currentDate by remember { mutableStateOf(LocalDate.now()) }
    var expandedEntryId by remember { mutableStateOf<String?>(null) }

    // Load dữ liệu khi ngày thay đổi
    LaunchedEffect(currentDate) {
        diaryViewModel.loadDiaryForDate(currentDate)
    }

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

                when {
                    uiState.isLoading -> {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator()
                        }
                    }
                    uiState.entries.isEmpty() -> {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    imageVector = Icons.Default.SpeakerNotesOff,
                                    contentDescription = "empty",
                                    modifier = Modifier.size(100.dp)
                                )
                                Spacer(Modifier.height(8.dp))
                                Text(
                                    text = "Chưa có hoạt động nào",
                                    color = Color.Gray,
                                    fontSize = 16.sp
                                )
                                Text(
                                    text = "Thêm bữa ăn, giấc ngủ hoặc bài tập",
                                    color = Color.Gray,
                                    fontSize = 14.sp
                                )
                            }
                        }
                    }
                    else -> {
                        DiaryEntryList(
                            entries = uiState.entries,
                            expandedEntryId = expandedEntryId,
                            onCardClick = { entry ->
                                expandedEntryId = if (expandedEntryId == entry.id) null else entry.id
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun DiaryEntryList(
    entries: List<DiaryEntry>,
    expandedEntryId: String?,
    onCardClick: (DiaryEntry) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(entries) { entry ->
            DiaryEntryCard(
                entry = entry,
                isExpanded = expandedEntryId == entry.id,
                onClick = { onCardClick(entry) }
            )
        }
        
        item {
            Spacer(Modifier.height(16.dp))
        }
    }
}

@Composable
fun DiaryEntryCard(
    entry: DiaryEntry,
    isExpanded: Boolean,
    onClick: () -> Unit
) {
    val backgroundColor = when (entry.type) {
        "NUTRITION" -> Color(0xFFFFF3E0) // Cam nhạt
        "EXERCISE" -> Color(0xFFE8F5E9) // Xanh lá nhạt
        "SLEEP" -> Color(0xFFE3F2FD) // Xanh dương nhạt
        else -> Color.White
    }

    val accentColor = when (entry.type) {
        "NUTRITION" -> Color(0xFFFB8C00)
        "EXERCISE" -> Color(0xFF4CAF50)
        "SLEEP" -> Color(0xFF1E88E5)
        else -> Color.Gray
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Icon theo loại
                    when (entry.type) {
                        "NUTRITION" -> EatIcon()
                        "EXERCISE" -> ExerciseIcon()
                        "SLEEP" -> SleepIcon()
                    }
                    Text(
                        text = entry.title,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                }
                Text(
                    text = entry.time,
                    color = Color.Gray,
                    fontSize = 13.sp
                )
            }

            Spacer(Modifier.height(4.dp))
            
            Text(
                text = entry.description,
                color = Color.DarkGray,
                fontSize = 14.sp
            )

            // Hiển thị thông tin chính
            if (entry.calories > 0 || entry.duration > 0) {
                Spacer(Modifier.height(8.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    if (entry.calories > 0) {
                        Text(
                            text = if (entry.type == "EXERCISE") "-${entry.calories.toInt()} kcal"
                                   else "${entry.calories.toInt()} kcal",
                            color = accentColor,
                            fontWeight = FontWeight.Medium,
                            fontSize = 13.sp
                        )
                    }
                    if (entry.duration > 0) {
                        val hours = entry.duration / 60
                        val mins = entry.duration % 60
                        val durationText = if (hours > 0) "${hours}h ${mins}m" else "${mins} phút"
                        Text(
                            text = "$durationText",
                            color = accentColor,
                            fontWeight = FontWeight.Medium,
                            fontSize = 13.sp
                        )
                    }
                }
            }

            // Chi tiết mở rộng
            AnimatedVisibility(
                visible = isExpanded,
                enter = expandVertically(),
                exit = shrinkVertically()
            ) {
                Column {
                    Spacer(Modifier.height(12.dp))
                    HorizontalDivider(color = accentColor.copy(alpha = 0.3f))
                    Spacer(Modifier.height(12.dp))

                    if (entry.extraInfo.isNotEmpty()) {
                        Text(
                            text = entry.extraInfo,
                            color = Color.DarkGray,
                            fontSize = 13.sp
                        )
                    }

                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = "Ngày: ${entry.date}",
                        color = Color.Gray,
                        fontSize = 12.sp
                    )
                }
            }
        }
    }
}

@Preview
@Composable
fun PreviewDiary() {
    DiaryScreen()
}
