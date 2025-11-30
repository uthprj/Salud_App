package com.example.salud_app.ui.screen.data.health

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.salud_app.R
import com.example.salud_app.components.AppScaffold
import com.example.salud_app.components.ScreenLevel
import com.example.salud_app.ui.screen.data.health.weight.WeightViewModel
import com.example.salud_app.ui.screen.data.health.height.HeightViewModel
import com.example.salud_app.ui.screen.data.health.bloodpressure.BPViewModel
import com.example.salud_app.ui.screen.data.health.heartrate.HeartRateViewModel
import com.example.salud_app.ui.theme.Salud_AppTheme

@Composable
fun DataHealthScreen(
    navController: NavController,
    onBackClicked: () -> Unit,
    weightViewModel: WeightViewModel = viewModel(),
    heightViewModel: HeightViewModel = viewModel(),
    bpViewModel: BPViewModel = viewModel(),
    hrViewModel: HeartRateViewModel = viewModel()
) {
    // Theo dõi khi navigate back về màn hình này
    val currentBackStackEntry by navController.currentBackStackEntryAsState()
    
    // Reload dữ liệu khi back về màn hình này
    LaunchedEffect(currentBackStackEntry) {
        weightViewModel.loadWeightRecords()
        heightViewModel.loadHeightRecords()
        bpViewModel.loadBPRecords()
        hrViewModel.loadHRRecords()
    }

    val weightState by weightViewModel.uiState.collectAsState()
    val latestWeight = weightState.weightRecords.firstOrNull()
    val weightValue = latestWeight?.weight?.toString() ?: "--"
    val weightLastUpdated = latestWeight?.date?.let { "Cập nhật lần cuối $it" } ?: "Chưa có dữ liệu"

    val heightState by heightViewModel.uiState.collectAsState()
    val latestHeight = heightState.heightRecords.firstOrNull()
    val heightValue = latestHeight?.height?.toString() ?: "--"
    val heightLastUpdated = latestHeight?.date?.let { "Cập nhật lần cuối $it" } ?: "Chưa có dữ liệu"

    // Tính BMI từ weight và height
    val weight = latestWeight?.weight ?: 0.0
    val height = latestHeight?.height ?: 0.0
    val bmiValue = if (weight > 0 && height > 0) {
        val heightInMeters = height / 100.0
        String.format("%.1f", weight / (heightInMeters * heightInMeters))
    } else "--"
    val bmiLastUpdated = if (weight > 0 && height > 0) {
        val latestDate = maxOf(latestWeight?.date ?: "", latestHeight?.date ?: "")
        "Cập nhật lần cuối $latestDate"
    } else "Chưa có dữ liệu"

    // Lấy dữ liệu huyết áp
    val bpState by bpViewModel.uiState.collectAsState()
    val latestBP = bpState.bpRecords.firstOrNull()
    val bpValue = if (latestBP != null) "${latestBP.systolic}/${latestBP.diastolic}" else "--"
    val bpLastUpdated = latestBP?.date?.let { "Cập nhật lần cuối $it" } ?: "Chưa có dữ liệu"

    // Lấy dữ liệu nhịp tim
    val hrState by hrViewModel.uiState.collectAsState()
    val latestHR = hrState.hrRecords.firstOrNull()
    val hrValue = latestHR?.heartRate?.toString() ?: "--"
    val hrLastUpdated = latestHR?.date?.let { "Cập nhật lần cuối $it" } ?: "Chưa có dữ liệu"
    
    AppScaffold(
        navController = navController,
        title = stringResource(R.string.health),
        screenLevel = ScreenLevel.SUB,
        showMoreMenu = true,
        onBackClicked = onBackClicked
    ) { innerPadding ->

        // Sử dụng LazyColumn thay vì Column
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
//            contentPadding = PaddingValues(vertical = 8.dp)
        ) {
            // --- PHẦN: SỐ ĐO CƠ THỂ ---
            item {
                SectionHeader(text = "Số đo cơ thể")
            }
            item {
                HealthMetricCard(
                    title = stringResource(R.string.weight),
                    value = weightValue,
                    unit = "kg",
                    lastUpdated = weightLastUpdated,
                    onClick = { navController.navigate("data-health-weight") }
                )
                Spacer(modifier = Modifier.height(12.dp)) // Khoảng cách giữa các thẻ
            }
            item {
                HealthMetricCard(
                    title = "Chiều cao",
                    value = heightValue,
                    unit = "cm",
                    lastUpdated = heightLastUpdated,
                    onClick = { navController.navigate("data-health-height") }
                )
                Spacer(modifier = Modifier.height(12.dp))
            }
            item {
                HealthMetricCard(
                    title = "BMI",
                    value = bmiValue,
                    unit = "kg/m²",
                    lastUpdated = bmiLastUpdated,
                    onClick = { navController.navigate("data-health-bmi") }
                )
            }

            // --- PHẦN: CHỈ SỐ ---
            item {
                SectionHeader(text = "Chỉ số")
            }
            item {
                HealthMetricCard(
                    title = "Huyết áp",
                    value = bpValue,
                    unit = "mmHg",
                    lastUpdated = bpLastUpdated,
                    onClick = { navController.navigate("data-health-bp") }
                )
                Spacer(modifier = Modifier.height(12.dp))
            }
            item {
                HealthMetricCard(
                    title = "Nhịp tim",
                    value = hrValue,
                    unit = "bpm",
                    lastUpdated = hrLastUpdated,
                    onClick = { navController.navigate("data-health-hr") }
                )
            }
        }
    }
}

/**
 * Tiêu đề cho mỗi phần ("Số đo cơ thể", "Chỉ số", v.v.)
 */
@Composable
fun SectionHeader(text: String, modifier: Modifier = Modifier) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleLarge,
        modifier = modifier
            .fillMaxWidth()
            .padding(bottom = 8.dp)
    )
}

/**
 * Thẻ hiển thị thông tin có thể nhấp vào (Cân nặng, Chiều cao, v.v.)
 */
@Composable
fun HealthMetricCard(
    title: String,
    value: String,
    unit: String,
    lastUpdated: String,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .padding(bottom = 8.dp)
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        border = BorderStroke(1.dp, Color.LightGray)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Cột cho văn bản
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                )
                Text(
                    buildAnnotatedString {
                        withStyle(
                            style = SpanStyle(
                                color = MaterialTheme.colorScheme.primary,
                                fontSize = MaterialTheme.typography.bodyLarge.fontSize,
                                fontWeight = FontWeight.Bold
                            )
                        ) {
                            append(value)
                        }
                        append(" ")
                        withStyle(
                            style = SpanStyle(
                                fontSize = MaterialTheme.typography.bodyLarge.fontSize,
                            )
                        ) {
                            append(unit)
                        }
                    }
                )
                Text(
                    text = lastUpdated,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            // Mũi tên
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos,
                contentDescription = null, // Vì toàn bộ thẻ đã có onClick
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/**
 * Thẻ hiển thị Ghi chú (không thể nhấp)
 */
@Composable
fun NotesCard(text: String) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant, // Màu placeholder
            modifier = Modifier.padding(16.dp)
        )
    }
}


@Preview(showBackground = true)
@Composable
fun DataHealthScreenPreview() {
    val navController = rememberNavController()
    Salud_AppTheme {
        DataHealthScreen(navController = navController, onBackClicked = {})
    }
}