package com.example.salud_app.ui.screen.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.compose.rememberNavController
import com.example.salud_app.components.AppScaffold
import com.example.salud_app.components.ScreenLevel
import com.example.salud_app.ui.theme.Salud_AppTheme

@Composable
fun ProfileScreen() {
    Salud_AppTheme {
        val navController = rememberNavController()

        AppScaffold(
            navController = navController,
            title = "Hồ Sơ",
            screenLevel = ScreenLevel.MAIN,
        ) { paddingValues ->
            ProfileContent(Modifier.padding(paddingValues))
        }
    }
}

@Composable
private fun ProfileContent(modifier: Modifier = Modifier) {
    var stepGoal by remember { mutableStateOf("5.000") }
    var heartRate by remember { mutableStateOf("20") }
    var sleepEnabled by remember { mutableStateOf(true) }
    var sleepFrom by remember { mutableStateOf("23:00") }
    var sleepTo by remember { mutableStateOf("7:00") }
    var gender by remember { mutableStateOf("Nam") }
    var birthDate by remember { mutableStateOf("27 Th2, 2005") }
    var weight by remember { mutableStateOf("59") }
    var height by remember { mutableStateOf("160cm") }

    Box(
        modifier = Modifier.padding(0.dp)
            .fillMaxSize()
    )
    {
        LazyColumn(
            modifier = modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                SectionHeader(title = "Mục tiêu hoạt động")
            }

            item {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    RoundedDropdown(
                        value = stepGoal,
                        onValueChange = { stepGoal = it },
                        items = listOf("5.000", "7.000", "10.000"),
                        modifier = Modifier.weight(1f)
                    )
                    RoundedDropdown(
                        value = heartRate,
                        onValueChange = { heartRate = it },
                        items = listOf("20", "60", "80"),
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            item {
                Divider(color = Color.LightGray, thickness = 1.dp)
            }

            item {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth().padding(end = 30.dp)
                ) {
                    SectionHeader(title = "Lịch ngủ", showDivider = false)
                    Switch(checked = sleepEnabled, onCheckedChange = { sleepEnabled = it })
                }
            }

            item {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    RoundedDropdown(
                        value = sleepFrom,
                        onValueChange = { sleepFrom = it },
                        items = listOf("22:00", "23:00", "00:00"),
                        modifier = Modifier.weight(1f)
                    )
                    RoundedDropdown(
                        value = sleepTo,
                        onValueChange = { sleepTo = it },
                        items = listOf("6:00", "7:00", "8:00"),
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            item {
                Divider(color = Color.LightGray, thickness = 1.dp)
            }

            item {
                SectionHeader(title = "Giới thiệu về bạn")
            }

            item {
                // grid: gender | birth
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    RoundedDropdown(
                        value = gender,
                        onValueChange = { gender = it },
                        items = listOf("Nam", "Nữ", "Khác"),
                        modifier = Modifier.weight(1f)
                    )
                    RoundedDropdown(
                        value = birthDate,
                        onValueChange = { birthDate = it },
                        items = listOf("27 Th2, 2005", "01 Th1, 1990"),
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            item {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    RoundedDropdown(
                        value = weight,
                        onValueChange = { weight = it },
                        items = listOf("59", "60", "65"),
                        modifier = Modifier.weight(1f)
                    )
                    RoundedDropdown(
                        value = height,
                        onValueChange = { height = it },
                        items = listOf("160cm", "165cm", "170cm"),
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            item {
                Spacer(modifier = Modifier.height(24.dp))
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
    ProfileScreen()
}

