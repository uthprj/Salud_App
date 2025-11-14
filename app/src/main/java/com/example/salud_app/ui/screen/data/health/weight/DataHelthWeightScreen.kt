package com.example.salud_app.ui.screen.data.health.weight

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.salud_app.R
import com.example.salud_app.components.AppScaffold
import com.example.salud_app.components.ScreenLevel
import com.example.salud_app.components.number_picker.NumberPicker
import com.example.salud_app.components.number_picker.rememberPickerState
import com.example.salud_app.ui.theme.Salud_AppTheme

@Composable
fun DataHealthWeightScreen(
    navController: NavController,
    onBackClicked: () -> Unit
) {
    AppScaffold(
        navController = navController,
        title = stringResource(R.string.weight),
        screenLevel = ScreenLevel.SUB,
        showMoreMenu = true,
        onBackClicked = onBackClicked,
    ) { innerPadding ->

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(bottom = 16.dp)
        ) {
            // --- PHẦN: THÊM DỮ LIỆU ---
            item {
                Column(Modifier.padding(horizontal = 16.dp)) {
                    SectionHeader(text = "Thêm dữ liệu")
                    TimePickerRow(
                        date = "01/01/2025",
                        time = "00:00",
                        onDateClick = { /* TODO: Mở Date Picker */ },
                        onTimeClick = { /* TODO: Mở Time Picker */ }
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    WeightInputRow()
                    Spacer(modifier = Modifier.height(24.dp))
                }
            }

            // Đường phân cách
            item { Divider(color = MaterialTheme.colorScheme.surfaceVariant) }

            // --- PHẦN: THỐNG KÊ ---
            item {
                Column(Modifier.padding(horizontal = 16.dp)) {
                    SectionHeader(text = "Thống kê")
                }
            }
            item {
                StatisticsView()
            }

            // --- PHẦN: DANH SÁCH LỊCH SỬ ---
            item {
                Column(Modifier.padding(horizontal = 16.dp)) {
                    Spacer(modifier = Modifier.height(16.dp))
                    WeightHistoryItem(time = "00:45 Thứ năm, 2 Th10", weight = "73 kg")
                    WeightHistoryItem(time = "23:44 Thứ năm, 2 Th10", weight = "74 kg")
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
@Composable
fun TimePickerRow(
    date: String,
    time: String,
    onDateClick: () -> Unit,
    onTimeClick: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = "Thời gian",
            style = MaterialTheme.typography.bodyLarge
        )
        Row(horizontalArrangement = Arrangement.spacedBy(1.dp)) {
            Text(
                text = date,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .clickable(onClick = onDateClick)
                    .padding(vertical = 4.dp)
            )
            Text(
                text = time,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .clickable(onClick = onTimeClick)
                    .padding(vertical = 4.dp)
            )
        }
    }
}

/**
 * Hàng nhập Cân nặng (với Number Picker)
 */
@Composable
fun WeightInputRow() {
    // 1. Tạo danh sách giá trị cho mỗi picker
    val integerItems = remember { (0..200).map { it.toString() } }
    val fractionalItems = remember { (10..90).map { "%02d".format(it) } }

    // 2. Tạo và nhớ trạng thái cho mỗi picker
    val integerState = rememberPickerState("70")
    val fractionalState = rememberPickerState("00")

    Column {
        // --- Hàng tiêu đề và đơn vị ---
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "Cân nặng",
                style = MaterialTheme.typography.bodyLarge
            )
            Text(
                text = "kg",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
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
fun StatisticsView() {
    var selectedTabIndex by remember { mutableIntStateOf(1) }
    val tabs = listOf("Tuần", "Tháng", "Năm")

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
                .height(250.dp)
                .padding(horizontal = 16.dp)
                .background(
                    MaterialTheme.colorScheme.surfaceVariant,
                    RoundedCornerShape(12.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            Text("Chart", color = MaterialTheme.colorScheme.onSurfaceVariant)
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

@Preview(showBackground = true)
@Composable
fun DataHealthWeightScreenPreview() {
    val navController = rememberNavController()

    Salud_AppTheme {
        DataHealthWeightScreen(navController = navController, onBackClicked = {})
    }
}

