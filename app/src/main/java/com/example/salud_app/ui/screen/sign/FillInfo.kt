package com.example.salud_app.ui.screen.sign

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.DialogProperties
import androidx.navigation.NavController
import com.example.salud_app.R
import com.example.salud_app.model.User
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import android.util.Log

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FillInfo(navController: NavController) {
    val context = LocalContext.current
    val auth = FirebaseAuth.getInstance()
    val db = FirebaseFirestore.getInstance()

    var fullName by remember { mutableStateOf("") }
    var birthDate by remember { mutableStateOf("") }
    var gender by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }

    var showDatePicker by remember { mutableStateOf(false) }
    val today = remember { System.currentTimeMillis() }
    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = today,
        selectableDates = object : SelectableDates {
            override fun isSelectableDate(utcTimeMillis: Long): Boolean {
                return utcTimeMillis <= today
            }
            override fun isSelectableYear(year: Int): Boolean {
                return year <= java.time.LocalDate.now().year
            }
        }
    )

    // Dropdown state for gender
    var expandedGender by remember { mutableStateOf(false) }
    val genderOptions = listOf("Nam", "Nữ", "Khác")

    // Chỉ validate số điện thoại nếu có nhập - không bắt buộc điền hết
    val isPhoneValid = phone.isEmpty() || phone.length == 10

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF6F8FB))
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {
            Spacer(modifier = Modifier.height(36.dp))

            // Logo
            Image(
                painter = painterResource(id = R.drawable.salud_logo),
                contentDescription = "Logo",
                modifier = Modifier
                    .width(200.dp)
                    .height(120.dp)
            )

            Card(
                modifier = Modifier
                    .padding(20.dp)
                    .fillMaxWidth()
                    .wrapContentHeight(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Vui lòng nhập thông tin của bạn",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.SemiBold
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Full Name
                    OutlinedTextField(
                        value = fullName,
                        onValueChange = { fullName = it },
                        placeholder = { Text("Họ và tên") },
                        leadingIcon = { Icon(Icons.Default.Badge, contentDescription = null) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Birth Date
                    OutlinedTextField(
                        value = birthDate,
                        onValueChange = {},
                        placeholder = { Text("Ngày sinh") },
                        leadingIcon = { Icon(Icons.Default.CalendarToday, contentDescription = null) },
                        trailingIcon = {
                            IconButton(onClick = { showDatePicker = true }) {
                                Icon(Icons.Default.CalendarToday, contentDescription = "Mở trình chọn ngày")
                            }
                        },
                        singleLine = true,
                        readOnly = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showDatePicker = true },
                        shape = RoundedCornerShape(12.dp)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Gender dropdown
                    Box(modifier = Modifier.fillMaxWidth()) {
                        OutlinedTextField(
                            value = gender,
                            onValueChange = {},
                            readOnly = true,
                            placeholder = { Text("Giới tính") },
                            leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                            trailingIcon = {
                                Icon(
                                    imageVector = Icons.Default.ArrowDropDown,
                                    contentDescription = null,
                                    modifier = Modifier.clickable {
                                        expandedGender = !expandedGender
                                    }
                                )
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { expandedGender = !expandedGender },
                            shape = RoundedCornerShape(12.dp)
                        )

                        DropdownMenu(
                            expanded = expandedGender,
                            onDismissRequest = { expandedGender = false },
                            offset = DpOffset(x = 200.dp, y = 0.dp),
                            modifier = Modifier
                                .width(150.dp)
                                .background(Color.White)
                        ) {
                            genderOptions.forEach { option ->
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(if (gender == option) Color(0xFFE0F2FF) else Color.Transparent)
                                        .clickable {
                                            gender = option
                                            expandedGender = false
                                        }
                                        .padding(horizontal = 16.dp, vertical = 12.dp)
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Text(option, color = Color.Black)
                                        if (gender == option) {
                                            Icon(
                                                imageVector = Icons.Default.Check,
                                                contentDescription = "Selected",
                                                tint = Color(0xFF3B82F6)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Phone
                    OutlinedTextField(
                        value = phone,
                        onValueChange = { phone = it.filter { ch -> ch.isDigit() }.take(10) },
                        placeholder = { Text("Số điện thoại") },
                        leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )

                    if (phone.isNotEmpty() && phone.length != 10) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Số điện thoại phải gồm 10 chữ số",
                            color = Color(0xFFB00020),
                            fontSize = 12.sp,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Submit button
                    Button(
                        onClick = {
                            val currentUser = auth.currentUser
                            if (currentUser != null && isPhoneValid) {
                                val uid = currentUser.uid
                                val user = User(
                                    userId = uid,
                                    fullName = fullName.ifBlank { currentUser.displayName ?: "" },
                                    birthDate = birthDate,
                                    gender = gender,
                                    numPhone = phone,
                                    email = currentUser.email ?: "",
                                    photoUrl = currentUser.photoUrl?.toString() ?: ""
                                )

                                db.collection("User")
                                    .document(uid)
                                    .set(user)
                                    .addOnSuccessListener {
                                        Log.d("FillInfo", "User info updated: $uid")
                                        Toast.makeText(context, "Đã lưu thông tin thành công", Toast.LENGTH_SHORT).show()
                                        navController.navigate("home") {
                                            popUpTo("login") { inclusive = true }
                                        }
                                    }
                                    .addOnFailureListener { e ->
                                        Log.e("FillInfo", "Failed to save user info", e)
                                        Toast.makeText(context, "Lỗi khi lưu thông tin: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
                                    }
                            } else {
                                Toast.makeText(context, "Vui lòng kiểm tra lại số điện thoại", Toast.LENGTH_SHORT).show()
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3B82F6)),
                        enabled = isPhoneValid
                    ) {
                        Text(text = "Lưu thông tin", color = Color.White, fontWeight = FontWeight.Bold)
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Skip button
                    Button(
                        onClick = {
                            val currentUser = auth.currentUser
                            if (currentUser != null) {
                                val uid = currentUser.uid
                                // Lưu thông tin cơ bản để đánh dấu đã qua màn hình này
                                val user = User(
                                    userId = uid,
                                    fullName = currentUser.displayName ?: "",
                                    birthDate = "",
                                    gender = "",
                                    numPhone = "",
                                    email = currentUser.email ?: "",
                                    photoUrl = currentUser.photoUrl?.toString() ?: ""
                                )

                                db.collection("User")
                                    .document(uid)
                                    .set(user)
                                    .addOnSuccessListener {
                                        Log.d("FillInfo", "User skipped fill info: $uid")
                                        navController.navigate("home") {
                                            popUpTo("login") { inclusive = true }
                                        }
                                    }
                                    .addOnFailureListener { e ->
                                        Log.e("FillInfo", "Failed to skip", e)
                                        // Vẫn cho phép tiếp tục ngay cả khi lỗi
                                        navController.navigate("home") {
                                            popUpTo("login") { inclusive = true }
                                        }
                                    }
                            }
                        },
                        modifier = Modifier
                            .width(100.dp)
                            .height(40.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF3B82F6))
                    ) {
                        Text(text = "Bỏ qua", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Material3 DatePickerDialog
        if (showDatePicker) {
            DatePickerDialog(
                onDismissRequest = { showDatePicker = false },
                confirmButton = {
                    TextButton(onClick = {
                        datePickerState.selectedDateMillis?.let { millis ->
                            birthDate = Instant.ofEpochMilli(millis)
                                .atZone(ZoneId.systemDefault())
                                .toLocalDate()
                                .toString()
                        }
                        showDatePicker = false
                    }) { Text("Xong") }
                },
                dismissButton = { TextButton(onClick = { showDatePicker = false }) { Text("Hủy") } },
                properties = DialogProperties()
            ) {
                DatePicker(state = datePickerState)
            }
        }
    }
}

@Preview
@Composable
fun FillInfoReview() {
    FillInfo(navController = NavController(LocalContext.current))
}
