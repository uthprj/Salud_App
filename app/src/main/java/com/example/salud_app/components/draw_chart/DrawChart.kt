package com.example.salud_app.components.draw_chart

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

/**
 * Data class đại diện cho một điểm dữ liệu trên chart
 */
data class ChartDataPoint(
    val date: LocalDate,
    val value: Float
)

/**
 * Component vẽ biểu đồ đường dùng chung cho ứng dụng
 *
 * @param dataPoints Danh sách các điểm dữ liệu (ngày + giá trị)
 * @param lineColor Màu đường
 * @param modifier Modifier
 * @param chartHeight Chiều cao chart
 * @param showGrid Hiển thị lưới
 * @param showLabels Hiển thị nhãn trục X
 * @param showValues Hiển thị giá trị trên điểm
 * @param gridColor Màu lưới
 * @param labelColor Màu nhãn
 * @param strokeWidth Độ dày đường
 * @param pointRadius Bán kính điểm
 * @param dateFormat Format ngày hiển thị
 * @param unit Đơn vị (ví dụ: "kg", "bước", ...)
 * @param onPointClick Callback khi click vào điểm
 */
@Composable
fun AppLineChart(
    dataPoints: List<ChartDataPoint>,
    lineColor: Color = Color(0xFF6AB9F5),
    modifier: Modifier = Modifier,
    chartHeight: Dp = 200.dp,
    showGrid: Boolean = true,
    showLabels: Boolean = true,
    showValues: Boolean = false,
    gridColor: Color = Color.LightGray.copy(alpha = 0.5f),
    labelColor: Color = Color.Gray,
    strokeWidth: Float = 3f,
    pointRadius: Float = 6f,
    dateFormat: DateTimeFormatter = DateTimeFormatter.ofPattern("dd/MM"),
    unit: String = "",
    onPointClick: ((ChartDataPoint) -> Unit)? = null
) {
    // Sắp xếp dataPoints theo ngày
    val sortedDataPoints = dataPoints.sortedBy { it.date }
    val numPoints = sortedDataPoints.size

    if (numPoints == 0) return

    // Tìm giá trị min/max để scale
    val values = sortedDataPoints.map { it.value }
    val minValue = values.minOrNull() ?: 0f
    val maxValue = values.maxOrNull() ?: 100f
    val valueRange = if (maxValue - minValue == 0f) 1f else maxValue - minValue

    // Padding cho chart
    val paddingTop = 20f
    val paddingBottom = if (showLabels) 40f else 20f
    val paddingStart = 50f
    val paddingEnd = 20f

    var selectedPoint by remember { mutableStateOf<ChartDataPoint?>(null) }

    val textMeasurer = rememberTextMeasurer()

    Column(modifier = modifier) {
        // Hiển thị điểm được chọn
        if (selectedPoint != null) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "${selectedPoint!!.date.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))}: ${selectedPoint!!.value} $unit",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = lineColor
                )
            }
        }

        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(chartHeight)
                .pointerInput(sortedDataPoints) {
                    detectTapGestures { offset ->
                        val chartWidth = size.width - paddingStart - paddingEnd
                        val chartHeightPx = size.height - paddingTop - paddingBottom
                        val pointSpacing = if (numPoints > 1) chartWidth / (numPoints - 1) else chartWidth

                        // Tìm điểm gần nhất
                        var closestPoint: ChartDataPoint? = null
                        var minDistance = Float.MAX_VALUE

                        sortedDataPoints.forEachIndexed { index, dataPoint ->
                            val x = paddingStart + index * pointSpacing
                            val normalizedValue = (dataPoint.value - minValue) / valueRange
                            val y = paddingTop + chartHeightPx * (1 - normalizedValue)

                            val distance = kotlin.math.sqrt(
                                (offset.x - x) * (offset.x - x) +
                                        (offset.y - y) * (offset.y - y)
                            )

                            if (distance < minDistance && distance < 50f) {
                                minDistance = distance
                                closestPoint = dataPoint
                            }
                        }

                        if (closestPoint != null) {
                            selectedPoint = closestPoint
                            onPointClick?.invoke(closestPoint!!)
                        }
                    }
                }
        ) {
            val chartWidth = size.width - paddingStart - paddingEnd
            val chartHeightPx = size.height - paddingTop - paddingBottom
            val pointSpacing = if (numPoints > 1) chartWidth / (numPoints - 1) else chartWidth

            // Vẽ lưới ngang
            if (showGrid) {
                val gridLines = 5
                for (i in 0..gridLines) {
                    val y = paddingTop + chartHeightPx * i / gridLines
                    drawLine(
                        color = gridColor,
                        start = Offset(paddingStart, y),
                        end = Offset(size.width - paddingEnd, y),
                        strokeWidth = 1f
                    )

                    // Vẽ giá trị trục Y
                    val labelValue = maxValue - (valueRange * i / gridLines)
                    val labelText = "%.0f".format(labelValue)
                    val textLayoutResult = textMeasurer.measure(
                        text = labelText,
                        style = TextStyle(fontSize = 10.sp, color = labelColor)
                    )
                    drawText(
                        textLayoutResult = textLayoutResult,
                        topLeft = Offset(
                            paddingStart - textLayoutResult.size.width - 8f,
                            y - textLayoutResult.size.height / 2
                        )
                    )
                }
            }

            // Vẽ nhãn trục X (chỉ hiển thị một số nhãn để tránh chồng chéo)
            if (showLabels && numPoints > 0) {
                val maxLabels = 7
                val labelStep = if (numPoints <= maxLabels) 1 else ((numPoints - 1).toFloat() / (maxLabels - 1)).toInt().coerceAtLeast(1)

                sortedDataPoints.forEachIndexed { index, dataPoint ->
                    // Hiển thị nhãn đầu, cuối và các nhãn cách đều
                    val shouldShowLabel = when {
                        numPoints <= maxLabels -> true  // Hiển thị tất cả nếu ít điểm
                        index == 0 -> true  // Luôn hiển thị đầu
                        index == numPoints - 1 -> true  // Luôn hiển thị cuối
                        else -> index % labelStep == 0
                    }
                    
                    if (shouldShowLabel) {
                        val x = paddingStart + index * pointSpacing
                        val labelText = dataPoint.date.format(dateFormat)
                        val textLayoutResult = textMeasurer.measure(
                            text = labelText,
                            style = TextStyle(fontSize = 10.sp, color = labelColor)
                        )
                        drawText(
                            textLayoutResult = textLayoutResult,
                            topLeft = Offset(
                                x - textLayoutResult.size.width / 2,
                                size.height - paddingBottom + 8f
                            )
                        )
                    }
                }
            }

            if (sortedDataPoints.isEmpty()) return@Canvas

            // Vẽ đường nối các điểm
            val path = Path()
            var isFirstPoint = true

            sortedDataPoints.forEachIndexed { index, dataPoint ->
                val x = paddingStart + index * pointSpacing
                val normalizedValue = (dataPoint.value - minValue) / valueRange
                val y = paddingTop + chartHeightPx * (1 - normalizedValue)

                if (isFirstPoint) {
                    path.moveTo(x, y)
                    isFirstPoint = false
                } else {
                    path.lineTo(x, y)
                }
            }

            drawPath(
                path = path,
                color = lineColor,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )

            // Vẽ các điểm
            sortedDataPoints.forEachIndexed { index, dataPoint ->
                val x = paddingStart + index * pointSpacing
                val normalizedValue = (dataPoint.value - minValue) / valueRange
                val y = paddingTop + chartHeightPx * (1 - normalizedValue)

                // Điểm được chọn sẽ có viền lớn hơn
                val isSelected = selectedPoint?.date == dataPoint.date
                if (isSelected) {
                    drawCircle(
                        color = lineColor.copy(alpha = 0.3f),
                        radius = pointRadius * 2,
                        center = Offset(x, y)
                    )
                }

                drawCircle(
                    color = Color.White,
                    radius = pointRadius,
                    center = Offset(x, y)
                )
                drawCircle(
                    color = lineColor,
                    radius = pointRadius - 2f,
                    center = Offset(x, y)
                )

                // Hiển thị giá trị trên điểm
                if (showValues) {
                    val valueText = "%.1f".format(dataPoint.value)
                    val textLayoutResult = textMeasurer.measure(
                        text = valueText,
                        style = TextStyle(fontSize = 9.sp, color = lineColor)
                    )
                    drawText(
                        textLayoutResult = textLayoutResult,
                        topLeft = Offset(
                            x - textLayoutResult.size.width / 2,
                            y - pointRadius - textLayoutResult.size.height - 4f
                        )
                    )
                }
            }
        }
    }
}

/**
 * Helper function để tạo dữ liệu mẫu
 */
fun generateSampleData(
    startDate: LocalDate,
    endDate: LocalDate,
    baseValue: Float = 70f,
    variance: Float = 5f
): List<ChartDataPoint> {
    val totalDays = ChronoUnit.DAYS.between(startDate, endDate).toInt() + 1
    return (0 until totalDays).map { i ->
        ChartDataPoint(
            date = startDate.plusDays(i.toLong()),
            value = baseValue + (Math.random().toFloat() - 0.5f) * variance * 2
        )
    }
}

// ------------------------
// PREVIEW
// ------------------------
@Preview(showBackground = true)
@Composable
fun PreviewLineChart() {
    val startDate = LocalDate.now().minusDays(6)
    val endDate = LocalDate.now()
    val sampleData = generateSampleData(startDate, endDate, 72f, 3f)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(10.dp)
            .background(Color.White, RoundedCornerShape(12.dp))
            .padding(10.dp)
    ) {
        Text("Biểu đồ cân nặng", fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(8.dp))
        AppLineChart(
            dataPoints = sampleData,
            lineColor = Color(0xFF6AB9F5),
            unit = "kg"
        )
    }
}
