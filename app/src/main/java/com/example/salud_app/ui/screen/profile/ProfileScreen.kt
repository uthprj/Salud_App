package com.example.salud_app.ui.screen.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
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

@Composable
fun ProfileScreen(
    navController: NavController,
    viewModel: ProfileViewModel = viewModel()) {
    val uiState by viewModel.uiState.collectAsState()
    val auth = FirebaseAuth.getInstance()

    Salud_AppTheme {
        AppScaffold(
            navController = navController,
            title = "Hồ Sơ",
            screenLevel = ScreenLevel.MAIN,
        ) { paddingValues ->
            if (uiState.isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else if (uiState.error != null) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "Lỗi: ${uiState.error}",
                            color = MaterialTheme.colorScheme.error
                        )
                        Button(
                            onClick = { viewModel.loadUserProfile() },
                            modifier = Modifier.padding(top = 16.dp)
                        ) {
                            Text("Thử lại")
                        }
                    }
                }
            } else {
                ProfileContent(
                    modifier = Modifier.padding(paddingValues),
                    uiState = uiState,
                    viewModel = viewModel,
                    onLogout = {
                        auth.signOut()
                        navController.navigate("sign-in") {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                )
            }
        }
    }
}

@Composable
private fun ProfileContent(
    modifier: Modifier = Modifier,
    uiState: ProfileUiState,
    viewModel: ProfileViewModel,
    onLogout: () -> Unit
) {
    var isEditPersonalInfo by remember { mutableStateOf(false) }
    
    // Editable fields for personal info
    var editFullName by remember { mutableStateOf("") }
    var editNumPhone by remember { mutableStateOf("") }

    val user = uiState.user
    
    // Initialize edit fields when entering edit mode
    LaunchedEffect(isEditPersonalInfo) {
        if (isEditPersonalInfo) {
            editFullName = user.fullName
            editNumPhone = user.numPhone
        }
    }

    Box(
        modifier = Modifier.padding(0.dp)
            .fillMaxSize()
    ) {
        LazyColumn(
            modifier = modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Profile Picture Section - Read Only
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(modifier = Modifier.size(80.dp)) {
                        if (user.photoUrl.isNotEmpty()) {
                            AsyncImage(
                                model = user.photoUrl,
                                contentDescription = "Profile Picture",
                                modifier = Modifier
                                    .size(80.dp)
                                    .clip(CircleShape)
                                    .border(2.dp, MaterialTheme.colorScheme.primary, CircleShape),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Box(
                                modifier = Modifier
                                    .size(80.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.primaryContainer)
                                    .border(2.dp, MaterialTheme.colorScheme.primary, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Person,
                                    contentDescription = "Default Avatar",
                                    modifier = Modifier.size(40.dp),
                                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }
                        }
                    }
                    
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = user.fullName.ifEmpty { "Chưa cập nhật" },
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = user.email,
                            fontSize = 12.sp,
                            color = Color.Gray,
                            modifier = Modifier.padding(top = 2.dp)
                        )
                    }
                }
            }

            item {
                Divider(color = Color.LightGray, thickness = 1.dp, modifier = Modifier.padding(vertical = 4.dp))
            }

            // THÔNG TIN CÁ NHÂN
            item {
                SectionHeader(title = "Thông tin cá nhân")
            }

            if (isEditPersonalInfo) {
                // Edit Mode - Personal Info
                item {
                    OutlinedTextField(
                        value = editFullName,
                        onValueChange = { editFullName = it },
                        label = { Text("Họ và tên") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )
                }

                item {
                    OutlinedTextField(
                        value = editNumPhone,
                        onValueChange = { editNumPhone = it },
                        label = { Text("Số điện thoại") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )
                }

                item {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        OutlinedButton(
                            onClick = { isEditPersonalInfo = false },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Hủy")
                        }
                        Button(
                            onClick = {
                                viewModel.updateUserProfile(
                                    fullName = editFullName,
                                    numPhone = editNumPhone
                                )
                                isEditPersonalInfo = false
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Lưu")
                        }
                    }
                }
            } else {
                // View Mode - Personal Info
                item {
                    CompactInfoCard(
                        label = "Họ và tên",
                        value = user.fullName.ifEmpty { "Chưa có" },
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                item {
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        CompactInfoCard(
                            label = "Số điện thoại",
                            value = user.numPhone.ifEmpty { "Chưa có" },
                            modifier = Modifier.weight(1f)
                        )
                        CompactInfoCard(
                            label = "Email",
                            value = user.email.ifEmpty { "Chưa có" },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                item {
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        CompactInfoCard(
                            label = "Giới tính",
                            value = when (user.gender) {
                                "MALE" -> "Nam"
                                "FEMALE" -> "Nữ"
                                "OTHER" -> "Khác"
                                else -> "Chưa có"
                            },
                            modifier = Modifier.weight(1f)
                        )
                        CompactInfoCard(
                            label = "Ngày sinh",
                            value = user.birthDate.ifEmpty { "Chưa có" },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                item {
                    Button(
                        onClick = { isEditPersonalInfo = true },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Cập nhật thông tin")
                    }
                }
            }

            // Logout Button
            item {
                Spacer(modifier = Modifier.height(8.dp))
                Button(
                    onClick = onLogout,
                    modifier = Modifier
                        .fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFEF5350)
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.ExitToApp,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Đăng xuất",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            item {
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@Composable
private fun SectionHeader(title: String, showDivider: Boolean = true) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(text = title, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
        if (showDivider) Spacer(modifier = Modifier.height(8.dp))
    }
}

@Composable
private fun CompactInfoCard(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        tonalElevation = 0.dp,
        shadowElevation = 0.dp,
        color = Color(0xFFF5F5F5),
        modifier = modifier
            .fillMaxWidth()
            .height(70.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = label,
                fontSize = 11.sp,
                color = Color.Gray,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = value,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color.Black
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun RoundedDropdown(value: String, onValueChange: (String) -> Unit, items: List<String>, modifier: Modifier = Modifier) {
    var expanded by remember { mutableStateOf(false) }

    Box(modifier = modifier) {
        Surface(
            shape = RoundedCornerShape(12.dp),
            tonalElevation = 0.dp,
            shadowElevation = 0.dp,
            color = Color.White,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(Color.Transparent)
                .clickable { expanded = true }
        ) {
            Row(modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 12.dp), verticalAlignment = Alignment.CenterVertically) {
                Text(text = value, modifier = Modifier.weight(1f))
                Icon(imageVector = Icons.Default.ArrowDropDown, contentDescription = null)
            }
        }

        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            items.forEach { item ->
                DropdownMenuItem(text = { Text(item) }, onClick = { onValueChange(item); expanded = false })
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ProfileScreenPreview() {
    ProfileScreen(
        navController = rememberNavController()
    )
}

