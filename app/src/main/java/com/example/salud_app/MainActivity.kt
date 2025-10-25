package com.example.salud_app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Person
//import androidx.compose.material.icons.filled.Book
//import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.salud_app.ui.theme.Salud_AppTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            Salud_AppTheme {
                MainApp()
            }
        }
    }
}

@Composable
fun MainApp() {
    val selectedItem = remember { mutableStateOf(0) }
    val items = listOf("Home", "Nhật ký", "Dữ liệu", "Cá nhân")

    Scaffold(
        bottomBar = {
            BottomAppBar(
                containerColor = Color.White,
                contentColor = MaterialTheme.colorScheme.primary,
                actions = {
                    // Home
                    NavigationBarItem(
                        icon = {
                            Icon(
                                Icons.Default.Home,
                                contentDescription = "Home"
                            )
                        },
                        label = { Text("Home") },
                        selected = selectedItem.value == 0,
                        onClick = { selectedItem.value = 0 }
                    )

                    // Nhật ký
                    NavigationBarItem(
                        icon = {
                            Icon(
                                Icons.Default.List,
                                contentDescription = "Nhật ký"
                            )
                        },
                        label = { Text("Nhật ký") },
                        selected = selectedItem.value == 1,
                        onClick = { selectedItem.value = 1 }
                    )
                },
                floatingActionButton = {
                    FloatingActionButton(
                        onClick = {
                            // Xử lý sự kiện khi nhấn nút +
                            selectedItem.value = 2
                        },
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = Color.White
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Thêm")
                    }
                }
            )

            // Bottom Navigation phần còn lại
            NavigationBar(
                containerColor = Color.White
            ) {
                // Dữ liệu
                NavigationBarItem(
                    icon = {
                        Icon(
                            Icons.Default.AddCircle,
                            contentDescription = "Dữ liệu"
                        )
                    },
                    label = { Text("Dữ liệu") },
                    selected = selectedItem.value == 3,
                    onClick = { selectedItem.value = 3 }
                )

                // Cá nhân
                NavigationBarItem(
                    icon = {
                        Icon(
                            Icons.Default.Person,
                            contentDescription = "Cá nhân"
                        )
                    },
                    label = { Text("Cá nhân") },
                    selected = selectedItem.value == 4,
                    onClick = { selectedItem.value = 4 }
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentAlignment = Alignment.Center
        ) {
            when (selectedItem.value) {
                0 -> Text("Màn hình Home", style = MaterialTheme.typography.headlineMedium)
                1 -> Text("Màn hình Nhật ký", style = MaterialTheme.typography.headlineMedium)
                2 -> Text("Màn hình Thêm", style = MaterialTheme.typography.headlineMedium)
                3 -> Text("Màn hình Dữ liệu", style = MaterialTheme.typography.headlineMedium)
                4 -> Text("Màn hình Cá nhân", style = MaterialTheme.typography.headlineMedium)
            }
        }
    }
}

//@Preview(showBackground = true)
//@Composable
//fun MainAppPreview() {
//    Salud_AppTheme {
//        MainApp()
//    }
//}