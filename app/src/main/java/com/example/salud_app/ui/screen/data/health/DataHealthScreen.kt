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
import androidx.navigation.compose.rememberNavController
import com.example.salud_app.R
import com.example.salud_app.components.AppScaffold
import com.example.salud_app.components.ScreenLevel
import com.example.salud_app.ui.screen.data.health.wieght.WeightViewModel
import com.example.salud_app.ui.theme.Salud_AppTheme

@Composable
fun DataHealthScreen(
    navController: NavController,
    onBackClicked: () -> Unit,
    weightViewModel: WeightViewModel = viewModel()
) {
    val weightState by weightViewModel.uiState.collectAsState()
    
    // Lấy record mới nhất
    val latestWeight = weightState.weightRecords.firstOrNull()
    val weightValue = latestWeight?.weight?.toString() ?: "--"
    val weightLastUpdated = latestWeight?.date?.let { "Cập nhật lần cuối $it" } ?: "Chưa có dữ liệu"

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
                    value = "160",
                    unit = "cm",
                    lastUpdated = "Cập nhật lần cuối 01/09/2025",
                    onClick = { /* TODO: Điều hướng đến màn hình Chiều cao */ }
                )
                Spacer(modifier = Modifier.height(12.dp))
            }
            item {
                HealthMetricCard(
                    title = "BMI",
                    value = "19",
                    unit = "kg/m2",
                    lastUpdated = "Cập nhật lần cuối 01/09/2025",
                    onClick = { /* TODO: Điều hướng đến màn hình BMI */ }
                )
            }

            // --- PHẦN: CHỈ SỐ ---
            item {
                SectionHeader(text = "Chỉ số")
            }
            item {
                HealthMetricCard(
                    title = "Huyết áp",
                    value = "120/80",
                    unit = "mmHg",
                    lastUpdated = "Cập nhật lần cuối 01/09/2025",
                    onClick = { /* TODO: Điều hướng đến màn hình Huyết áp */ }
                )
                Spacer(modifier = Modifier.height(12.dp))
            }
            item {
                HealthMetricCard(
                    title = "Nhịp tim",
                    value = "72",
                    unit = "bpm",
                    lastUpdated = "Cập nhật lần cuối 01/09/2025",
                    onClick = { /* TODO: Điều hướng đến màn hình Nhịp tim */ }
                )
            }

            // --- PHẦN: GHI CHÚ ---
            item {
                SectionHeader(text = "Ghi chú")
            }
            item {
                NotesCard(text = "Không có ghi chú...")
                Spacer(modifier = Modifier.height(16.dp)) // Padding ở cuối
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