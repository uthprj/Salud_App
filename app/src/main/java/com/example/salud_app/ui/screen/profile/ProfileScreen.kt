package com.example.salud_app.ui.screen.profile

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import coil.compose.AsyncImage
import com.example.salud_app.components.AppScaffold
import com.example.salud_app.components.ScreenLevel
import com.example.salud_app.ui.theme.Salud_AppTheme
import com.google.firebase.auth.FirebaseAuth
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    navController: NavController,
    viewModel: ProfileViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val auth = FirebaseAuth.getInstance()
    val context = LocalContext.current
    
    // Edit states
    var isEditing by remember { mutableStateOf(false) }
    var editFullName by remember { mutableStateOf("") }
    var editPhone by remember { mutableStateOf("") }
    var editGender by remember { mutableStateOf("") }
    var editBirthDate by remember { mutableStateOf("") }
    
    // Dialog states
    var showGenderDialog by remember { mutableStateOf(false) }
    var showDatePicker by remember { mutableStateOf(false) }
    var showLogoutDialog by remember { mutableStateOf(false) }
    
    // Initialize edit fields
    LaunchedEffect(uiState.user, isEditing) {
        if (isEditing) {
            editFullName = uiState.user.fullName
            editPhone = uiState.user.numPhone
            editGender = uiState.user.gender
            editBirthDate = uiState.user.birthDate
        }
    }

    // Date picker state
    val today = remember { System.currentTimeMillis() }
    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = today,
        selectableDates = object : SelectableDates {
            override fun isSelectableDate(utcTimeMillis: Long): Boolean {
                return utcTimeMillis <= today
            }
            override fun isSelectableYear(year: Int): Boolean {
                return year <= LocalDate.now().year
            }
        }
    )

    Salud_AppTheme {
        AppScaffold(
            navController = navController,
            title = "Hồ sơ cá nhân",
            screenLevel = ScreenLevel.MAIN,
        ) { paddingValues ->
            when {
                uiState.isLoading -> {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(paddingValues),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = Color(0xFF3B82F6))
                    }
                }
                uiState.error != null -> {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(paddingValues),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Outlined.ErrorOutline,
                                contentDescription = null,
                                modifier = Modifier.size(64.dp),
                                tint = Color(0xFFEF5350)
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "Đã có lỗi xảy ra",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                text = uiState.error ?: "",
                                fontSize = 14.sp,
                                color = Color.Gray
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Button(onClick = { viewModel.loadUserProfile() }) {
                                Text("Thử lại")
                            }
                        }
                    }
                }
                else -> {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(paddingValues)
                            .padding(horizontal = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Header với Avatar
                        item {
                            Spacer(modifier = Modifier.height(8.dp))
                            ProfileHeader(
                                photoUrl = uiState.user.photoUrl,
                                fullName = uiState.user.fullName,
                                email = uiState.user.email
                            )
                        }
                        
                        // Card thông tin tài khoản (không chỉnh sửa được)
                        item {
                            AccountInfoCard(
                                email = uiState.user.email,
                                userId = uiState.user.userId
                            )
                        }
                        
                        // Card thông tin cá nhân (chỉnh sửa được)
                        item {
                            PersonalInfoCard(
                                isEditing = isEditing,
                                fullName = if (isEditing) editFullName else uiState.user.fullName,
                                phone = if (isEditing) editPhone else uiState.user.numPhone,
                                gender = if (isEditing) editGender else uiState.user.gender,
                                birthDate = if (isEditing) editBirthDate else uiState.user.birthDate,
                                onFullNameChange = { editFullName = it },
                                onPhoneChange = { editPhone = it },
                                onGenderClick = { showGenderDialog = true },
                                onBirthDateClick = { showDatePicker = true },
                                onEditClick = { isEditing = true },
                                onSaveClick = {
                                    viewModel.updateUserProfile(
                                        fullName = editFullName,
                                        numPhone = editPhone,
                                        gender = editGender,
                                        birthDate = editBirthDate
                                    )
                                    isEditing = false
                                    Toast.makeText(context, "Đã cập nhật thông tin", Toast.LENGTH_SHORT).show()
                                },
                                onCancelClick = { isEditing = false }
                            )
                        }
                        
                        // Nút đăng xuất
                        item {
                            Spacer(modifier = Modifier.height(8.dp))
                            Button(
                                onClick = { showLogoutDialog = true },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(52.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFFFFEBEE)
                                ),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ExitToApp,
                                    contentDescription = null,
                                    tint = Color(0xFFEF5350),
                                    modifier = Modifier.size(22.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Đăng xuất",
                                    color = Color(0xFFEF5350),
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                        
                        item { Spacer(modifier = Modifier.height(24.dp)) }
                    }
                }
            }
        }
    }
    
    // Gender Dialog
    if (showGenderDialog) {
        AlertDialog(
            onDismissRequest = { showGenderDialog = false },
            title = { Text("Chọn giới tính") },
            text = {
                Column {
                    listOf(
                        "MALE" to "Nam",
                        "FEMALE" to "Nữ",
                        "OTHER" to "Khác"
                    ).forEach { (value, label) ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    editGender = value
                                    showGenderDialog = false
                                }
                                .padding(vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = editGender == value,
                                onClick = {
                                    editGender = value
                                    showGenderDialog = false
                                }
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(text = label, fontSize = 16.sp)
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showGenderDialog = false }) {
                    Text("Đóng")
                }
            }
        )
    }
    
    // Date Picker Dialog
    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        val date = Instant.ofEpochMilli(millis)
                            .atZone(ZoneId.systemDefault())
                            .toLocalDate()
                        editBirthDate = date.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
                    }
                    showDatePicker = false
                }) {
                    Text("Xác nhận")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("Hủy")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
    
    // Logout Confirmation Dialog
    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            icon = {
                Icon(
                    imageVector = Icons.Outlined.Logout,
                    contentDescription = null,
                    tint = Color(0xFFEF5350),
                    modifier = Modifier.size(40.dp)
                )
            },
            title = {
                Text(
                    text = "Đăng xuất",
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
            },
            text = {
                Text(
                    text = "Bạn có chắc chắn muốn đăng xuất khỏi tài khoản?",
                    textAlign = TextAlign.Center
                )
            },
            confirmButton = {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = {
                            auth.signOut()
                            navController.navigate("sign-in") {
                                popUpTo(0) { inclusive = true }
                            }
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF5350))
                    ) {
                        Text("Đăng xuất")
                    }

                    OutlinedButton(
                        onClick = { showLogoutDialog = false },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Hủy")
                    }
                }
            },
            // dismissButton bỏ trống vì đã đặt trong Row
            dismissButton = {}
        )
    }

}

@Composable
private fun ProfileHeader(
    photoUrl: String,
    fullName: String,
    email: String
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Avatar
        Box(
            modifier = Modifier
                .size(100.dp)
                .clip(CircleShape)
                .background(Color(0xFFE3F2FD))
                .border(3.dp, Color(0xFF3B82F6), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            if (photoUrl.isNotEmpty()) {
                AsyncImage(
                    model = photoUrl,
                    contentDescription = "Avatar",
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(CircleShape),
                    contentScale = ContentScale.Crop
                )
            } else {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = null,
                    modifier = Modifier.size(50.dp),
                    tint = Color(0xFF3B82F6)
                )
            }
        }
        
        Spacer(modifier = Modifier.height(12.dp))
        
        Text(
            text = fullName.ifEmpty { "Chưa cập nhật tên" },
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF1E293B)
        )
        
        Text(
            text = email,
            fontSize = 14.sp,
            color = Color.Gray
        )
    }
}

@Composable
private fun AccountInfoCard(
    email: String,
    userId: String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF8FAFC)),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Outlined.Security,
                    contentDescription = null,
                    tint = Color(0xFF64748B),
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Thông tin tài khoản",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF64748B)
                )
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            InfoRow(
                icon = Icons.Outlined.Email,
                label = "Email",
                value = email.ifEmpty { "Chưa có" },
                isLocked = true
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            InfoRow(
                icon = Icons.Outlined.Badge,
                label = "ID người dùng",
                value = if (userId.length > 20) "${userId.take(20)}..." else userId.ifEmpty { "Chưa có" },
                isLocked = true
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PersonalInfoCard(
    isEditing: Boolean,
    fullName: String,
    phone: String,
    gender: String,
    birthDate: String,
    onFullNameChange: (String) -> Unit,
    onPhoneChange: (String) -> Unit,
    onGenderClick: () -> Unit,
    onBirthDateClick: () -> Unit,
    onEditClick: () -> Unit,
    onSaveClick: () -> Unit,
    onCancelClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Outlined.Person,
                        contentDescription = null,
                        tint = Color(0xFF3B82F6),
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Thông tin cá nhân",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF3B82F6)
                    )
                }
                
                if (!isEditing) {
                    IconButton(
                        onClick = onEditClick,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Chỉnh sửa",
                            tint = Color(0xFF3B82F6),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            if (isEditing) {
                // Edit Mode
                OutlinedTextField(
                    value = fullName,
                    onValueChange = onFullNameChange,
                    label = { Text("Họ và tên") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Outlined.Person,
                            contentDescription = null,
                            tint = Color.Gray
                        )
                    },
                    singleLine = true
                )
                
                Spacer(modifier = Modifier.height(12.dp))
                
                OutlinedTextField(
                    value = phone,
                    onValueChange = onPhoneChange,
                    label = { Text("Số điện thoại") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Outlined.Phone,
                            contentDescription = null,
                            tint = Color.Gray
                        )
                    },
                    singleLine = true
                )
                
                Spacer(modifier = Modifier.height(12.dp))
                
                // Gender selector
                OutlinedCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onGenderClick() },
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = when (gender) {
                                    "MALE" -> Icons.Outlined.Male
                                    "FEMALE" -> Icons.Outlined.Female
                                    else -> Icons.Outlined.Person
                                },
                                contentDescription = null,
                                tint = Color.Gray
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "Giới tính",
                                    fontSize = 12.sp,
                                    color = Color.Gray
                                )
                                Text(
                                    text = when (gender) {
                                        "MALE" -> "Nam"
                                        "FEMALE" -> "Nữ"
                                        "OTHER" -> "Khác"
                                        else -> "Chọn giới tính"
                                    },
                                    fontSize = 16.sp
                                )
                            }
                        }
                        Icon(
                            imageVector = Icons.Default.ArrowDropDown,
                            contentDescription = null,
                            tint = Color.Gray
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(12.dp))
                
                // Birth date selector
                OutlinedCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onBirthDateClick() },
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Outlined.CalendarMonth,
                                contentDescription = null,
                                tint = Color.Gray
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "Ngày sinh",
                                    fontSize = 12.sp,
                                    color = Color.Gray
                                )
                                Text(
                                    text = birthDate.ifEmpty { "Chọn ngày sinh" },
                                    fontSize = 16.sp
                                )
                            }
                        }
                        Icon(
                            imageVector = Icons.Default.CalendarToday,
                            contentDescription = null,
                            tint = Color.Gray
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(20.dp))
                
                // Action buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = onCancelClick,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Hủy")
                    }
                    Button(
                        onClick = onSaveClick,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3B82F6))
                    ) {
                        Icon(
                            imageVector = Icons.Default.Save,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Lưu")
                    }
                }
            } else {
                // View Mode
                EditableInfoRow(
                    icon = Icons.Outlined.Person,
                    label = "Họ và tên",
                    value = fullName.ifEmpty { "Chưa cập nhật" }
                )
                
                HorizontalDivider(
                    modifier = Modifier.padding(vertical = 12.dp),
                    color = Color(0xFFE2E8F0)
                )
                
                EditableInfoRow(
                    icon = Icons.Outlined.Phone,
                    label = "Số điện thoại",
                    value = phone.ifEmpty { "Chưa cập nhật" }
                )
                
                HorizontalDivider(
                    modifier = Modifier.padding(vertical = 12.dp),
                    color = Color(0xFFE2E8F0)
                )
                
                EditableInfoRow(
                    icon = when (gender) {
                        "MALE" -> Icons.Outlined.Male
                        "FEMALE" -> Icons.Outlined.Female
                        else -> Icons.Outlined.Person
                    },
                    label = "Giới tính",
                    value = when (gender) {
                        "MALE" -> "Nam"
                        "FEMALE" -> "Nữ"
                        "OTHER" -> "Khác"
                        else -> "Chưa cập nhật"
                    }
                )
                
                HorizontalDivider(
                    modifier = Modifier.padding(vertical = 12.dp),
                    color = Color(0xFFE2E8F0)
                )
                
                EditableInfoRow(
                    icon = Icons.Outlined.CalendarMonth,
                    label = "Ngày sinh",
                    value = birthDate.ifEmpty { "Chưa cập nhật" }
                )
            }
        }
    }
}

@Composable
private fun InfoRow(
    icon: ImageVector,
    label: String,
    value: String,
    isLocked: Boolean = false
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = Color(0xFF94A3B8),
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = label,
                fontSize = 12.sp,
                color = Color(0xFF94A3B8)
            )
            Text(
                text = value,
                fontSize = 14.sp,
                color = Color(0xFF475569)
            )
        }
        if (isLocked) {
            Icon(
                imageVector = Icons.Outlined.Lock,
                contentDescription = "Không thể chỉnh sửa",
                tint = Color(0xFFCBD5E1),
                modifier = Modifier.size(16.dp)
            )
        }
    }
}

@Composable
private fun EditableInfoRow(
    icon: ImageVector,
    label: String,
    value: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(Color(0xFFEFF6FF)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = Color(0xFF3B82F6),
                modifier = Modifier.size(20.dp)
            )
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text(
                text = label,
                fontSize = 12.sp,
                color = Color(0xFF94A3B8)
            )
            Text(
                text = value,
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF1E293B)
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ProfileScreenPreview() {
    ProfileScreen(navController = rememberNavController())
}
