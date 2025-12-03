package com.example.salud_app.components.dialog

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Warning
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import com.example.salud_app.R



/**
 * Reusable Confirmation Dialog component với thiết kế đẹp
 * 
 * @param showDialog State để hiển thị/ẩn dialog
 * @param title Tiêu đề của dialog
 * @param message Nội dung thông báo
 * @param icon Icon hiển thị (mặc định là Warning)
 * @param iconTint Màu của icon
 * @param confirmButtonText Text của nút xác nhận (mặc định "Xác nhận")
 * @param dismissButtonText Text của nút hủy (mặc định "Hủy")
 * @param confirmButtonColor Màu nền của nút xác nhận
 * @param onConfirm Callback khi nhấn nút xác nhận
 * @param onDismiss Callback khi nhấn nút hủy hoặc dismiss dialog
 */
@Composable
fun ConfirmDialog(
    showDialog: Boolean,
    title: String,
    message: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    icon: ImageVector? = Icons.Default.Warning,
    iconTint: Color = Color(0xFFF39C12),
    confirmButtonText: String = "Xác nhận",
    dismissButtonText: String = "Hủy",
    confirmButtonColor: Color = Color(0xFF6AB9F5)
) {
    if (showDialog) {
        Dialog(onDismissRequest = onDismiss) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Icon
                    if (icon != null) {
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .background(
                                    iconTint.copy(alpha = 0.15f),
                                    RoundedCornerShape(32.dp)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = icon,
                                contentDescription = null,
                                tint = iconTint,
                                modifier = Modifier.size(32.dp)
                            )
                        }
                    }

                    // Title
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black,
                        textAlign = TextAlign.Center
                    )

                    // Message
                    Text(
                        text = message,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.Gray,
                        textAlign = TextAlign.Center,
                        lineHeight = 20.sp
                    )

                    // Buttons
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Dismiss Button
                        Button(
                            onClick = onDismiss,
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFFF5F5F5),
                                contentColor = Color.Gray
                            )
                        ) {
                            Text(
                                text = dismissButtonText,
                                fontWeight = FontWeight.Medium,
                                fontSize = 15.sp
                            )
                        }

                        // Confirm Button
                        Button(
                            onClick = onConfirm,
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = confirmButtonColor
                            )
                        ) {
                            Text(
                                text = confirmButtonText,
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Delete Confirmation Dialog - Biến thể chuyên dụng cho xóa
 */
@Composable
fun DeleteConfirmDialog(
    showDialog: Boolean,
    itemName: String = "mục này",
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    ConfirmDialog(
        showDialog = showDialog,
        title = "Xác nhận xóa",
        message = "Bạn có chắc chắn muốn xóa $itemName? Hành động này không thể hoàn tác.",
        confirmButtonText = "Xóa",
        dismissButtonText = "Hủy",
        confirmButtonColor = MaterialTheme.colorScheme.primary,
        iconTint = Color(0xFFE74C3C),
        onConfirm = onConfirm,
        onDismiss = onDismiss,
        icon = Icons.Default.Delete
    )
}

/**
 * Simple Confirmation Dialog - Không có icon
 */
@Composable
fun SimpleConfirmDialog(
    showDialog: Boolean,
    message: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    confirmButtonText: String = "Xác nhận",
    dismissButtonText: String = "Hủy"
) {
    ConfirmDialog(
        showDialog = showDialog,
        title = "Xác nhận",
        message = message,
        confirmButtonText = confirmButtonText,
        dismissButtonText = dismissButtonText,
        onConfirm = onConfirm,
        onDismiss = onDismiss,
        icon = null
    )
}

/**
 * Input Dialog - Dialog có input field với label
 * 
 * @param showDialog State để hiển thị/ẩn dialog
 * @param title Tiêu đề của dialog
 * @param message Nội dung hướng dẫn
 * @param label Label cho input field
 * @param placeholder Placeholder cho input field
 * @param initialValue Giá trị ban đầu của input
 * @param icon Icon hiển thị (optional)
 * @param iconTint Màu của icon
 * @param confirmButtonText Text của nút xác nhận
 * @param dismissButtonText Text của nút hủy
 * @param confirmButtonColor Màu nền của nút xác nhận
 * @param singleLine Input là single line hay không
 * @param maxLines Số dòng tối đa cho input
 * @param onConfirm Callback khi nhấn nút xác nhận với giá trị input
 * @param onDismiss Callback khi nhấn nút hủy
 * @param inputValidator Hàm validate input, return null nếu valid, error message nếu invalid
 */
@Composable
fun InputDialog(
    showDialog: Boolean,
    title: String,
    message: String,
    label: String,
    placeholder: String = "",
    initialValue: String = "",
    icon: ImageVector? = null,
    iconTint: Color = Color(0xFF6AB9F5),
    confirmButtonText: String = "Xác nhận",
    dismissButtonText: String = "Hủy",
    confirmButtonColor: Color = Color(0xFF6AB9F5),
    singleLine: Boolean = true,
    maxLines: Int = 1,
    onConfirm: (String) -> Unit,
    onDismiss: () -> Unit,
    inputValidator: (String) -> String? = { if (it.isBlank()) "Không được để trống" else null }
) {
    var inputValue by remember(showDialog) { mutableStateOf(initialValue) }
    var errorMessage by remember(showDialog) { mutableStateOf<String?>(null) }
    
    if (showDialog) {
        Dialog(onDismissRequest = onDismiss) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Icon
                    if (icon != null) {
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .background(
                                    iconTint.copy(alpha = 0.15f),
                                    RoundedCornerShape(32.dp)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = icon,
                                contentDescription = null,
                                tint = iconTint,
                                modifier = Modifier.size(32.dp)
                            )
                        }
                    }

                    // Title
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black,
                        textAlign = TextAlign.Center
                    )

                    // Message
                    Text(
                        text = message,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.Gray,
                        textAlign = TextAlign.Center,
                        lineHeight = 20.sp
                    )

                    // Input Field
                    OutlinedTextField(
                        value = inputValue,
                        onValueChange = { 
                            inputValue = it
                            errorMessage = null
                        },
                        label = { Text(label) },
                        placeholder = { Text(placeholder) },
                        singleLine = singleLine,
                        maxLines = maxLines,
                        isError = errorMessage != null,
                        supportingText = if (errorMessage != null) {
                            { Text(errorMessage!!, color = MaterialTheme.colorScheme.error) }
                        } else null,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = confirmButtonColor,
                            focusedLabelColor = confirmButtonColor
                        )
                    )

                    // Buttons
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Dismiss Button
                        Button(
                            onClick = onDismiss,
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFFF5F5F5),
                                contentColor = Color.Gray
                            )
                        ) {
                            Text(
                                text = dismissButtonText,
                                fontWeight = FontWeight.Medium,
                                fontSize = 15.sp
                            )
                        }

                        // Confirm Button
                        Button(
                            onClick = {
                                val error = inputValidator(inputValue)
                                if (error != null) {
                                    errorMessage = error
                                } else {
                                    onConfirm(inputValue.trim())
                                }
                            },
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = confirmButtonColor
                            )
                        ) {
                            Text(
                                text = confirmButtonText,
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp
                            )
                        }
                    }
                }
            }
        }
    }
}



// Preview cho ConfirmDialog mặc định
@Preview(name = "Default Confirm Dialog", showBackground = true)
@Composable
fun ConfirmDialogPreview() {
    // Để thấy được dialog, chúng ta cần đặt nó trong một Box
    // và giả lập trạng thái showDialog = true
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        ConfirmDialog(
            showDialog = true,
            title = "Xác nhận hành động",
            message = "Đây là một hành động quan trọng. Bạn có muốn tiếp tục không?",
            onConfirm = { /* Do nothing in preview */ },
            onDismiss = { /* Do nothing in preview */ }
        )
    }
}

// Preview cho DeleteConfirmDialog
@Preview(name = "Delete Confirm Dialog", showBackground = true)
@Composable
fun DeleteConfirmDialogPreview() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        DeleteConfirmDialog(
            showDialog = true,
            itemName = "bữa ăn 'Phở Bò'",
            onConfirm = { /* Do nothing in preview */ },
            onDismiss = { /* Do nothing in preview */ }
        )
    }
}

// Preview cho SimpleConfirmDialog (không có icon)
@Preview(name = "Simple Confirm Dialog", showBackground = true)
@Composable
fun SimpleConfirmDialogPreview() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        SimpleConfirmDialog(
            showDialog = true,
            message = "Bạn có muốn lưu các thay đổi này không?",
            onConfirm = { /* Do nothing in new preview */ },
            onDismiss = { /* Do nothing in new preview */ }
        )
    }
}

// Preview cho InputDialog
@Preview(name = "Input Dialog", showBackground = true)
@Composable
fun InputDialogPreview() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        InputDialog(
            showDialog = true,
            title = "Lưu đoạn chat",
            message = "Nhập tên để lưu đoạn chat này, bạn có thể xem lại sau.",
            label = "Tên đoạn chat",
            placeholder = "Ví dụ: Kế hoạch tập gym",
            onConfirm = { /* Do nothing in preview */ },
            onDismiss = { /* Do nothing in preview */ }
        )
    }
}
