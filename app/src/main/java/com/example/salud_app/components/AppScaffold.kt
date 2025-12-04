package com.example.salud_app.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.zIndex
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Place
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
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
    var showComingSoonDialog by remember { mutableStateOf(false) }
    
    // Coming Soon Dialog
    if (showComingSoonDialog) {
        ComingSoonDialog(onDismiss = { showComingSoonDialog = false })
    }
    
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
                        painter = painterResource(id = R.drawable.dehaze_24px),
                        tint = Color.Black,
                        modifier = Modifier.padding(0.dp),
                        contentDescription = "Menu",
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
                            onClick = { 
                                expanded = false
                                showComingSoonDialog = true
                            },
                            leadingIcon = { Icon(Icons.Default.Place, contentDescription = null) }
                        )

                        DropdownMenuItem(
                            text = { Text("Đánh giá") },
                            onClick = { 
                                expanded = false
                                showComingSoonDialog = true
                            },
                            leadingIcon = { Icon(Icons.Default.Chat, contentDescription = null) }
                        )

                        DropdownMenuItem(
                            text = { Text("Hỗ trợ") },
                            onClick = { 
                                expanded = false
                                showComingSoonDialog = true
                            },
                            leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null) }
                        )

                        DropdownMenuItem(
                            text = { Text("Thông tin") },
                            onClick = { 
                                expanded = false
                                showComingSoonDialog = true
                            },
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
    val currentRoute = navController.currentBackStackEntry?.destination?.route

    val selectedIndex = when (currentRoute) {
        "home" -> 0
        "diary" -> 1
        "data" -> 2
        "profile" -> 3
        else -> 0
    }

    val primaryColor = MaterialTheme.colorScheme.primary

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(90.dp)
    ) {
        // Background với curve ở giữa
        Surface(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .height(80.dp),
            color = primaryColor,
            shadowElevation = 16.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 8.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Home
                NavBarItem(
                    icon = R.drawable.home_24px,
                    label = "Home",
                    selected = selectedIndex == 0,
                    onClick = {
                        navController.navigate("home") {
                            popUpTo("home") { inclusive = true }
                            launchSingleTop = true
                        }
                    }
                )
                
                // Nhật ký
                NavBarItem(
                    icon = R.drawable.pending_actions_24px,
                    label = "Nhật ký",
                    selected = selectedIndex == 1,
                    onClick = {
                        navController.navigate("diary") {
                            popUpTo("home")
                            launchSingleTop = true
                        }
                    }
                )
                
                // Spacer cho FAB
                Spacer(modifier = Modifier.width(72.dp))
                
                // Dữ liệu
                NavBarItem(
                    icon = R.drawable.favorite_24px,
                    label = "Dữ liệu",
                    selected = selectedIndex == 2,
                    onClick = {
                        navController.navigate("data") {
                            popUpTo("home")
                            launchSingleTop = true
                        }
                    }
                )
                
                // Cá nhân
                NavBarItem(
                    icon = R.drawable.person_24px,
                    label = "Cá nhân",
                    selected = selectedIndex == 3,
                    onClick = {
                        navController.navigate("profile") {
                            popUpTo("home")
                            launchSingleTop = true
                        }
                    }
                )
            }
        }
        
        // Floating AI Button ở giữa
        Box(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .offset(y = 0.dp)
        ) {
            // Vòng tròn nền trắng phía sau
            Surface(
                modifier = Modifier
                    .size(72.dp)
                    .align(Alignment.Center),
                shape = CircleShape,
                color = Color.White,
                shadowElevation = 8.dp
            ) {}
            
            // FAB chính
            FloatingActionButton(
                onClick = {
                    navController.navigate("data-hint") {
                        popUpTo("home")
                        launchSingleTop = true
                    }
                },
                modifier = Modifier
                    .size(64.dp)
                    .align(Alignment.Center),
                shape = CircleShape,
                containerColor = Color(0xFF5B9FE8),
                contentColor = Color.White,
                elevation = FloatingActionButtonDefaults.elevation(
                    defaultElevation = 6.dp,
                    pressedElevation = 12.dp
                )
            ) {
                Image(
                    painter = painterResource(id = R.drawable.robot_2_24px),
                    contentDescription = "AI Assistant",
                    colorFilter = ColorFilter.tint(Color.White),
                    modifier = Modifier.size(36.dp)
                )
            }
        }
    }
}

@Composable
fun NavBarItem(
    icon: Int,
    label: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    val selectedColor = Color.White
    val unselectedColor = Color.White.copy(alpha = 0.5f)
    val currentColor = if (selected) selectedColor else unselectedColor
    
    Column(
        modifier = Modifier
            .clickable(
                indication = null,
                interactionSource = remember { MutableInteractionSource() },
                onClick = onClick
            )
            .padding(horizontal = 12.dp, vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(if (selected) 40.dp else 36.dp)
                .background(
                    color = if (selected) Color.White.copy(alpha = 0.2f) else Color.Transparent,
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(id = icon),
                contentDescription = label,
                tint = currentColor,
                modifier = Modifier.size(24.dp)
            )
        }

        
        Text(
            text = label,
            fontSize = 12.sp,
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
            color = currentColor
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

/**
 * Dialog hiển thị thông báo "Coming Soon" cho các chức năng chưa phát triển
 */
@Composable
fun ComingSoonDialog(onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                painter = painterResource(id = R.drawable.salud_logo),
                contentDescription = null,
                modifier = Modifier.size(60.dp),
                tint = Color.Unspecified
            )
        },
        title = {
            Text(
                text = "Sắp ra mắt!",
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp,
                color = MaterialTheme.colorScheme.primary
            )
        },
        text = {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp) // khoảng cách đều giữa các Text
            ) {
                Text(
                    text = "Tính năng này đang được phát triển.",
                    fontSize = 16.sp,
                    color = Color.Gray,
                    textAlign = TextAlign.Center // đảm bảo text căn giữa trong box
                )
                Text(
                    text = "Vui lòng quay lại sau nhé !",
                    fontSize = 14.sp,
                    color = Color.Gray,
                    textAlign = TextAlign.Center
                )
            }

        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Text("Đã hiểu", color = Color.White)
            }
        },
        containerColor = Color.White,
        shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp)
    )
}
