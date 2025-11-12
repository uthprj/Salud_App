package com.example.salud_app.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBackIosNew
import androidx.compose.material.icons.filled.DataExploration
import androidx.compose.material.icons.filled.FormatListBulleted
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.PendingActions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.salud_app.ui.theme.Salud_AppTheme
import com.example.salud_app.R

enum class ScreenLevel { MAIN, SUB }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppScaffold(
    navController: NavController,
    title: String,
    screenLevel: ScreenLevel = ScreenLevel.MAIN,
    onBackClicked: (() -> Unit)? = null,
    showSaveButton: Boolean = false,
    onSaveClicked: () -> Unit = {},
    showMoreMenu: Boolean = false,
    moreMenuItems: @Composable ColumnScope.() -> Unit = {},
    content: @Composable (PaddingValues) -> Unit
) {
    Scaffold(
        topBar = {
            when (screenLevel) {
                ScreenLevel.MAIN -> {
                    MainTopBar(title = title)
                }
                ScreenLevel.SUB -> {
                    SubTopBar(
                        title = title,
                        showSaveButton = showSaveButton,
                        onSaveClicked = onSaveClicked,
                        showMoreMenu = showMoreMenu,
                        moreMenuItems = moreMenuItems,
                        onBackClicked = (onBackClicked ?: { navController.popBackStack() }) as () -> Unit
                    )
                }
            }
        },
        bottomBar = {
            if (screenLevel == ScreenLevel.MAIN) {
                BottomNavigationBar(navController)
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
        ) {
            if (screenLevel == ScreenLevel.MAIN) {
                Text(
                    text = title,
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier
                        .padding(start = 50.dp, top = 20.dp, bottom = 0.dp)
                )
            }

            content(PaddingValues())
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainTopBar(title: String) {
    TopAppBar(
        title = {
            Row(
                modifier = Modifier
                    .fillMaxWidth(),

                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Image(
                        painter = painterResource(id = R.drawable.salud_logo),
                        contentDescription = "Logo Salud",
                        modifier = Modifier.size(60.dp),
                        contentScale = ContentScale.Inside
                    )
                }
                IconButton(onClick = { }) {
                    Icon(
                        imageVector = Icons.Default.FormatListBulleted,
                        contentDescription = "Menu"
                    )
                }
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent),
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SubTopBar(
    title: String,
    showSaveButton: Boolean,
    onSaveClicked: () -> Unit,
    showMoreMenu: Boolean,
    moreMenuItems: @Composable ColumnScope.() -> Unit,
    onBackClicked: () -> Unit
) {
    var menuExpanded by remember { mutableStateOf(false) }

    TopAppBar(
        navigationIcon = {
            IconButton(onClick = onBackClicked) {
                Icon(
                    imageVector = Icons.Default.ArrowBackIosNew,
                    contentDescription = "Back"
                )
            }
        },
        title = {
            Text(
                text = title,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold
            )
        },
        actions = {
            if (showSaveButton) {
                TextButton(onClick = onSaveClicked) {
                    Text("Lưu")
                }
            }

            if (showMoreMenu) {
                Box {
                    IconButton(onClick = { menuExpanded = true }) {
                        Icon(Icons.Default.MoreVert, contentDescription = "More")
                    }
                    DropdownMenu(
                        expanded = menuExpanded,
                        onDismissRequest = { menuExpanded = false }
                    ) {
                        moreMenuItems()
                    }
                }
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
    )
}

/* ✅ BOTTOM NAVIGATION BAR */
@Composable
fun BottomNavigationBar(navController: NavController) {
    var selectedIndex by remember { mutableStateOf(0) }

    NavigationBar(
        containerColor = Color(0xFF345DA7),
        tonalElevation = 3.dp
    ) {
        NavigationBarItem(
            icon = { Icon(Icons.Default.Home, contentDescription = "Home") },
            label = { Text("Tổng quan") },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = Color.White,          // màu icon khi chọn
                unselectedIconColor = Color(0xFFB0C4FF),  // màu icon khi chưa chọn
                selectedTextColor = Color.White,
                unselectedTextColor = Color(0xFFB0C4FF),
                indicatorColor = Color(0xFF274B8A)        // nền tròn khi được chọn
            ),
            selected = selectedIndex == 0,
            onClick = { selectedIndex = 0 /* navController.navigate("home") */ }
        )
        NavigationBarItem(
            icon = { Icon(Icons.Default.PendingActions, contentDescription = "Sức khỏe") },
            label = { Text("Nhật ký") },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = Color.White,          // màu icon khi chọn
                unselectedIconColor = Color(0xFFB0C4FF),  // màu icon khi chưa chọn
                selectedTextColor = Color.White,
                unselectedTextColor = Color(0xFFB0C4FF),
                indicatorColor = Color(0xFF274B8A)        // nền tròn khi được chọn
            ),
            selected = selectedIndex == 1,
            onClick = { selectedIndex = 1 /* navController.navigate("history") */ }
        )

        NavigationBarItem(
            icon = { Icon(Icons.Default.Favorite, contentDescription = "Sức khỏe") },
            label = { Text("Dữ liệu") },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = Color.White,          // màu icon khi chọn
                unselectedIconColor = Color(0xFFB0C4FF),  // màu icon khi chưa chọn
                selectedTextColor = Color.White,
                unselectedTextColor = Color(0xFFB0C4FF),
                indicatorColor = Color(0xFF274B8A)        // nền tròn khi được chọn
            ),
            selected = selectedIndex == 2,
            onClick = { selectedIndex = 2 /* navController.navigate("health") */ }
        )

        NavigationBarItem(
            icon = { Icon(Icons.Default.Person, contentDescription = "Hồ sơ") },
            label = { Text("Hồ sơ") },
            selected = selectedIndex == 3,
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = Color.White,          // màu icon khi chọn
                unselectedIconColor = Color(0xFFB0C4FF),  // màu icon khi chưa chọn
                selectedTextColor = Color.White,
                unselectedTextColor = Color(0xFFB0C4FF),
                indicatorColor = Color(0xFF274B8A)        // nền tròn khi được chọn
            ),
            onClick = { selectedIndex = 3 /* navController.navigate("profile") */ }
        )
    }
}

@Preview(showBackground = true)
@Composable
fun AppScaffoldPreview() {
    Salud_AppTheme {
        val navController = rememberNavController()

        AppScaffold(
            navController = navController,
            title = "Nhật ký",
            screenLevel = ScreenLevel.MAIN,

        ) { paddingValues ->
            Box(modifier = Modifier.padding(paddingValues)) {
                Text("Màn hình chính có bottom bar")
            }
        }
    }
}
