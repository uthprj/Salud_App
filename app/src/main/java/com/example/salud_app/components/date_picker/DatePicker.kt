package com.example.salud_app.components.date_picker

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.salud_app.R
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

/**
 * Reusable DatePicker component cho toàn ứng dụng
 * Bao gồm DateSelector với navigation và DatePickerDialog
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppDatePicker(
    currentDate: LocalDate,
    onDateChange: (LocalDate) -> Unit,
    modifier: Modifier = Modifier,
    label: String = "Ngày",
    dateFormat: DateTimeFormatter = DateTimeFormatter.ofPattern(
        "EEE, dd/MM/yyyy",
        Locale("vi", "VN")
    ),
    backgroundColor: Color = Color(0xFF9CD5FF),
    showNavigationArrows: Boolean = true,
    iconSize: Dp = 20.dp,
    confirmButtonText: String = "Xác nhận",
    dismissButtonText: String = "Hủy",
    maxDate: LocalDate = LocalDate.now()
) {
    var showDatePicker by remember { mutableStateOf(false) }
    
    // Tạo validator để giới hạn ngày
    val selectableDates = remember(maxDate) {
        object : SelectableDates {
            override fun isSelectableDate(utcTimeMillis: Long): Boolean {
                val date = Instant.ofEpochMilli(utcTimeMillis)
                    .atZone(ZoneId.of("UTC"))
                    .toLocalDate()
                return !date.isAfter(maxDate)
            }
            
            override fun isSelectableYear(year: Int): Boolean {
                return year <= maxDate.year
            }
        }
    }
    
    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = currentDate.atStartOfDay(ZoneId.systemDefault())
            .toInstant().toEpochMilli(),
        selectableDates = selectableDates
    )

    Column(modifier = modifier) {
        DateSelector(
            currentDate = currentDate,
            onDateChange = onDateChange,
            onOpenDatePicker = { showDatePicker = true },
            label = label,
            dateFormat = dateFormat,
            backgroundColor = backgroundColor,
            showNavigationArrows = showNavigationArrows,
            iconSize = iconSize,
            maxDate = maxDate
        )

        if (showDatePicker) {
            AppDatePickerDialog(
                datePickerState = datePickerState,
                onDismiss = { showDatePicker = false },
                onConfirm = { selectedDate ->
                    onDateChange(selectedDate)
                    showDatePicker = false
                },
                confirmButtonText = confirmButtonText,
                dismissButtonText = dismissButtonText
            )
        }
    }
}

/**
 * DateSelector component - Hiển thị ngày hiện tại và các nút navigation
 */
@Composable
fun DateSelector(
    currentDate: LocalDate,
    onDateChange: (LocalDate) -> Unit,
    onOpenDatePicker: () -> Unit,
    modifier: Modifier = Modifier,
    label: String = "Ngày",
    dateFormat: DateTimeFormatter = DateTimeFormatter.ofPattern("EEE, dd/MM/yyyy",
        Locale("vi", "VN")
    ),
    backgroundColor: Color = Color(0xFF6AB9F5),
    showNavigationArrows: Boolean = true,
    iconSize: Dp = 20.dp,
    maxDate: LocalDate = LocalDate.now()
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 10.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Date display box
        Row(
            modifier = Modifier
                .background(backgroundColor, RoundedCornerShape(12.dp))
                .padding(horizontal = 20.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "$label  ",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
            )

            Text(
                text = currentDate.format(dateFormat),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
            )
        }

        // Navigation controls
        Row(
            modifier = Modifier
                .padding(horizontal = 20.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(5.dp)
        ) {
            if (showNavigationArrows) {
                // Previous day button
                Image(
                    painter = painterResource(R.drawable.arrow_back_ios_24px),
                    contentDescription = "Ngày trước",
                    modifier = Modifier
                        .size(iconSize)
                        .clickable { onDateChange(currentDate.minusDays(1)) }
                )
            }

            // Calendar icon
            Icon(
                imageVector = Icons.Default.CalendarToday,
                contentDescription = "Chọn ngày",
                tint = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier
                    .size(iconSize)
                    .clickable { onOpenDatePicker() }
            )

            if (showNavigationArrows) {
                // Next day button - disabled if current date >= maxDate
                val canGoNext = currentDate < maxDate
                Image(
                    painter = painterResource(R.drawable.arrow_forward_ios_24px),
                    contentDescription = "Ngày tiếp theo",
                    modifier = Modifier
                        .padding(start = 5.dp)
                        .size(iconSize)
                        .then(
                            if (canGoNext) Modifier.clickable { onDateChange(currentDate.plusDays(1)) }
                            else Modifier
                        ),
                    alpha = if (canGoNext) 1f else 0.3f
                )
            }
        }
    }
}

/**
 * DatePickerDialog component - Dialog chọn ngày
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppDatePickerDialog(
    datePickerState: DatePickerState,
    onDismiss: () -> Unit,
    onConfirm: (LocalDate) -> Unit,
    confirmButtonText: String = "Xác nhận",
    dismissButtonText: String = "Hủy"
) {
    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(
                onClick = {
                    val millis = datePickerState.selectedDateMillis
                    if (millis != null) {
                        val selectedDate = Instant.ofEpochMilli(millis)
                            .atZone(ZoneId.systemDefault())
                            .toLocalDate()
                        onConfirm(selectedDate)
                    } else {
                        onDismiss()
                    }
                }
            ) {
                Text(confirmButtonText)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(dismissButtonText)
            }
        }
    ) {
        DatePicker(state = datePickerState)
    }
}

/**
 * Compact DatePicker - Chỉ hiển thị icon calendar và ngày
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CompactDatePicker(
    currentDate: LocalDate,
    onDateChange: (LocalDate) -> Unit,
    modifier: Modifier = Modifier,
    dateFormat: DateTimeFormatter = DateTimeFormatter.ofPattern("EEE, dd/MM/yyyy",
        Locale("vi", "VN")
    ),
    maxDate: LocalDate = LocalDate.now()
) {
    var showDatePicker by remember { mutableStateOf(false) }
    
    // Tạo validator để giới hạn ngày
    val selectableDates = remember(maxDate) {
        object : SelectableDates {
            override fun isSelectableDate(utcTimeMillis: Long): Boolean {
                val date = Instant.ofEpochMilli(utcTimeMillis)
                    .atZone(ZoneId.of("UTC"))
                    .toLocalDate()
                return !date.isAfter(maxDate)
            }
            
            override fun isSelectableYear(year: Int): Boolean {
                return year <= maxDate.year
            }
        }
    }
    
    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = currentDate.atStartOfDay(ZoneId.systemDefault())
            .toInstant().toEpochMilli(),
        selectableDates = selectableDates
    )

    Row(
        modifier = modifier
            .clickable { showDatePicker = true }
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Icon(
            imageVector = Icons.Default.CalendarToday,
            contentDescription = "Chọn ngày",
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(20.dp)
        )

        Text(
            text = currentDate.format(dateFormat),
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium
        )
    }

    if (showDatePicker) {
        AppDatePickerDialog(
            datePickerState = datePickerState,
            onDismiss = { showDatePicker = false },
            onConfirm = { selectedDate ->
                onDateChange(selectedDate)
                showDatePicker = false
            }
        )
    }
}

// ------------------------
// PREVIEW
// ------------------------
@Preview(showBackground = true)
@Composable
fun PreviewAppDatePicker() {
    var date by remember { mutableStateOf(LocalDate.now()) }
    
    MaterialTheme {
        AppDatePicker(
            currentDate = date,
            onDateChange = { date = it },
            label = "Nhật ký ngày",
            maxDate = LocalDate.now()
        )
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewCompactDatePicker() {
    var date by remember { mutableStateOf(LocalDate.now()) }
    
    MaterialTheme {
        CompactDatePicker(
            currentDate = date,
            onDateChange = { date = it }
        )
    }
}