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
import androidx.compose.material.icons.filled.Edit

/**
 * Custom Input Dialog component với thiết kế đẹp
 * 
 * @param showDialog State để hiển thị/ẩn dialog
 * @param title Tiêu đề của dialog
 * @param message Thông điệp hướng dẫn (optional)
 * @param inputValue Giá trị input hiện tại
 * @param onInputChange Callback khi input thay đổi
 * @param inputPlaceholder Placeholder cho input field
 * @param icon Icon hiển thị (mặc định là Edit)
 * @param iconTint Màu của icon
 * @param confirmButtonText Text của nút xác nhận (mặc định "Xác nhận")
 * @param dismissButtonText Text của nút hủy (mặc định "Hủy")
 * @param confirmButtonColor Màu nền của nút xác nhận
 * @param onConfirm Callback khi nhấn nút xác nhận
 * @param onDismiss Callback khi nhấn nút hủy hoặc dismiss dialog
 * @param isLoading State loading khi đang xử lý
 * @param singleLine Input là single line hay multi line
 */
@Composable
fun InputDialog(
    showDialog: Boolean,
    title: String,
    inputValue: String,
    onInputChange: (String) -> Unit,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    message: String? = null,
    inputPlaceholder: String = "",
    icon: ImageVector? = Icons.Default.Edit,
    iconTint: Color = Color(0xFF6AB9F5),
    confirmButtonText: String = "Xác nhận",
    dismissButtonText: String = "Hủy",
    confirmButtonColor: Color = Color(0xFF6AB9F5),
    isLoading: Boolean = false,
    singleLine: Boolean = true,
    enabled: Boolean = true
) {
    if (showDialog) {
        Dialog(onDismissRequest = { if (!isLoading) onDismiss() }) {
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

                    // Message (optional)
                    if (message != null) {
                        Text(
                            text = message,
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.Gray,
                            textAlign = TextAlign.Center,
                            lineHeight = 20.sp
                        )
                    }

                    // Input Field
                    OutlinedTextField(
                        value = inputValue,
                        onValueChange = onInputChange,
                        placeholder = { 
                            Text(
                                text = inputPlaceholder,
                                color = Color.Gray.copy(alpha = 0.6f)
                            )
                        },
                        singleLine = singleLine,
                        enabled = enabled && !isLoading,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = iconTint,
                            unfocusedBorderColor = Color.LightGray,
                            focusedContainerColor = Color.White,
                            unfocusedContainerColor = Color(0xFFF8F9FA)
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
                            enabled = !isLoading,
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
                            enabled = inputValue.isNotBlank() && !isLoading,
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = confirmButtonColor,
                                disabledContainerColor = confirmButtonColor.copy(alpha = 0.5f)
                            )
                        ) {
                            if (isLoading) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(20.dp),
                                    strokeWidth = 2.dp,
                                    color = Color.White
                                )
                            } else {
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
}
