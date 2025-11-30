package com.example.salud_app.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.ArrowBackIosNew
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.DataExploration
import androidx.compose.material.icons.filled.FormatListBulleted
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PendingActions
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Place
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
    isSaving: Boolean = false,
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
                        isSaving = isSaving,
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
                .padding(horizontal = 16.dp)
        ) {
            if (screenLevel == ScreenLevel.MAIN) {
                Text(
                    text = title,
                    fontSize = 35.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier
                        .padding( top = 0.dp, bottom = 10.dp)
                        .padding(start = 10.dp)
                )
            }

            content(PaddingValues())
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainTopBar(title: String) {
    var expanded by remember { mutableStateOf(false) }
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
                        modifier = Modifier
                            .size(60.dp),
                        contentScale = ContentScale.Inside
                    )
                }
                IconButton(onClick = { expanded = true }) {
                    Icon(
                        imageVector = Icons.Default.FormatListBulleted,
                        contentDescription = "Menu"
                    )
                    DropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false },
                        modifier = Modifier
                            .background(Color(0xFFE8E8E8))
                            .padding(4.dp)
                    ) {
                        DropdownMenuItem(
                            text = { Text("Tìm phòng tập") },
                            onClick = { expanded = false },
                            leadingIcon = { Icon(Icons.Default.Place, contentDescription = null) }
                        )

                        DropdownMenuItem(
                            text = { Text("Đánh giá") },
                            onClick = { expanded = false },
                            leadingIcon = { Icon(Icons.Default.Chat, contentDescription = null) }
                        )

                        DropdownMenuItem(
                            text = { Text("Hỗ trợ") },
                            onClick = { expanded = false },
                            leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null) }
                        )

                        DropdownMenuItem(
                            text = { Text("Thông tin") },
                            onClick = { expanded = false },
                            leadingIcon = { Icon(Icons.Default.Info, contentDescription = null) }
                        )
                    }
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
    isSaving: Boolean = false,
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
                style = MaterialTheme.typography.titleLarge,
//                fontSize = 22.sp,
//                fontWeight = FontWeight.Bold
            )
        },
        actions = {
            if (showSaveButton) {
                TextButton(
                    onClick = onSaveClicked,
                    enabled = !isSaving
                ) {
                    if (isSaving) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text("Lưu")
                    }
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

/* BOTTOM NAVIGATION BAR */
@Composable
fun BottomNavigationBar(navController: NavController) {
    // Lấy route hiện tại từ navController
    val currentRoute = navController.currentBackStackEntry?.destination?.route

    val selectedIndex = when (currentRoute) {
        "home" -> 0
        "diary" -> 1
        "data" -> 2
        "profile" -> 3
        else -> 0
    }

    NavigationBar(
        containerColor = MaterialTheme.colorScheme.primary,
        tonalElevation = 3.dp
    ) {
        NavigationBarItem(
            icon = { Icon(Icons.Default.Home, contentDescription = "Home") },
            label = { Text("Tổng quan") },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = Color.White,
                unselectedIconColor = Color(0xFFB0C4FF),
                selectedTextColor = Color.White,
                unselectedTextColor = Color(0xFFB0C4FF),
                indicatorColor = Color(0xFF274B8A)
            ),
            selected = selectedIndex == 0,
            onClick = {
                navController.navigate("home") {
                    popUpTo("home") { inclusive = true }
                    launchSingleTop = true
                }
            }
        )
        NavigationBarItem(
            icon = { Icon(Icons.Default.PendingActions, contentDescription = "Nhật ký") },
            label = { Text("Nhật ký") },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = Color.White,
                unselectedIconColor = Color(0xFFB0C4FF),
                selectedTextColor = Color.White,
                unselectedTextColor = Color(0xFFB0C4FF),
                indicatorColor = Color(0xFF274B8A)
            ),
            selected = selectedIndex == 1,
            onClick = {
                navController.navigate("diary") {
                    popUpTo("home")
                    launchSingleTop = true
                }
            }
        )

        NavigationBarItem(
            icon = { Icon(Icons.Default.Favorite, contentDescription = "Dữ liệu") },
            label = { Text("Dữ liệu") },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = Color.White,
                unselectedIconColor = Color(0xFFB0C4FF),
                selectedTextColor = Color.White,
                unselectedTextColor = Color(0xFFB0C4FF),
                indicatorColor = Color(0xFF274B8A)
            ),
            selected = selectedIndex == 2,
            onClick = {
                navController.navigate("data") {
                    popUpTo("home")
                    launchSingleTop = true
                }
            }
        )

        NavigationBarItem(
            icon = { Icon(Icons.Default.Person, contentDescription = "Hồ sơ") },
            label = { Text("Hồ sơ") },
            selected = selectedIndex == 3,
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = Color.White,
                unselectedIconColor = Color(0xFFB0C4FF),
                selectedTextColor = Color.White,
                unselectedTextColor = Color(0xFFB0C4FF),
                indicatorColor = Color(0xFF274B8A)
            ),
            onClick = {
                navController.navigate("profile") {
                    popUpTo("home")
                    launchSingleTop = true
                }
            }
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
