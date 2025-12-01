package com.example.salud_app.ui.screen.data.hint

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.salud_app.components.AppScaffold
import com.example.salud_app.components.ScreenLevel
import com.example.salud_app.model.ChatMessage
import com.example.salud_app.model.QuickSuggestion
import com.example.salud_app.model.SavedChat
import com.example.salud_app.ui.theme.Salud_AppTheme
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun DataHintScreen(
    navController: NavController,
    onBackClicked: () -> Unit,
    hintViewModel: HintViewModel = viewModel()
) {
    val uiState by hintViewModel.uiState.collectAsState()
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current
    
    var messageText by remember { mutableStateOf("") }
    var showSuggestions by remember { mutableStateOf(true) }
    
    // Initialize ViewModel với context
    LaunchedEffect(Unit) {
        hintViewModel.initialize(context)
    }
    
    // Auto scroll to bottom when new message arrives
    LaunchedEffect(uiState.messages.size) {
        if (uiState.messages.isNotEmpty()) {
            coroutineScope.launch {
                listState.animateScrollToItem(uiState.messages.size - 1)
            }
        }
    }

    AppScaffold(
        navController = navController,
        title = "Trợ lý AI",
        screenLevel = ScreenLevel.SUB,
        showMoreMenu = true,
        moreMenuItems = {
            DropdownMenuItem(
                text = { Text("Lưu đoạn chat") },
                onClick = { 
                    if (uiState.messages.size > 1) {
                        hintViewModel.showSaveChatDialog()
                    }
                },
                leadingIcon = { Icon(Icons.Default.Save, contentDescription = null) },
                enabled = uiState.messages.size > 1
            )
            DropdownMenuItem(
                text = { Text("Xem chat đã lưu") },
                onClick = { hintViewModel.showSavedChatsDialog() },
                leadingIcon = { Icon(Icons.Default.History, contentDescription = null) }
            )
        },
        onBackClicked = onBackClicked
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(Color(0xFFF5F5F5))
        ) {
            // Chat messages
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                state = listState,
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(uiState.messages) { message ->
                    ChatMessageBubble(message)
                }
                
                // Quick suggestion buttons after welcome message
                if (uiState.messages.size == 1 && showSuggestions) {
                    item {
                        SuggestionButtons(
                            onSuggestionClick = { suggestion ->
                                messageText = suggestion
                                showSuggestions = false
                            }
                        )
                    }
                }
                
                // Loading indicator
                if (uiState.isLoading) {
                    item {
                        TypingIndicator()
                    }
                }
            }

            // Input area
            MessageInputBar(
                messageText = messageText,
                onMessageChange = { messageText = it },
                onSendClick = {
                    if (messageText.isNotBlank()) {
                        hintViewModel.sendMessage(messageText)
                        messageText = ""
                        showSuggestions = false
                    }
                },
                enabled = !uiState.isLoading
            )
        }
        
        // Save chat dialog
        if (uiState.showSaveDialog) {
            SaveChatDialog(
                onDismiss = { hintViewModel.dismissSaveChatDialog() },
                onSave = { chatName ->
                    hintViewModel.saveChat(chatName)
                },
                isSaving = uiState.isSavingChat
            )
        }
        
        // Saved chats dialog
        if (uiState.showSavedChatsDialog) {
            SavedChatsDialog(
                savedChats = uiState.savedChats,
                onDismiss = { hintViewModel.dismissSavedChatsDialog() },
                onLoadChat = { chat ->
                    hintViewModel.loadSavedChat(chat)
                },
                onDeleteChat = { chatId ->
                    hintViewModel.deleteSavedChat(chatId)
                }
            )
        }
    }
}

@Composable
fun ChatMessageBubble(message: ChatMessage) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (message.isUser) Arrangement.End else Arrangement.Start
    ) {
        if (!message.isUser) {
            // AI avatar
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF6AB9F5)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "AI",
                    color = Color.White,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
        }
        
        Card(
            modifier = Modifier.widthIn(max = 280.dp),
            shape = RoundedCornerShape(
                topStart = 16.dp,
                topEnd = 16.dp,
                bottomStart = if (message.isUser) 16.dp else 4.dp,
                bottomEnd = if (message.isUser) 4.dp else 16.dp
            ),
            colors = CardDefaults.cardColors(
                containerColor = if (message.isUser) Color(0xFF6AB9F5) else Color.White
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Text(
                text = message.content,
                modifier = Modifier.padding(12.dp),
                color = if (message.isUser) Color.White else Color.Black,
                fontSize = 14.sp,
                lineHeight = 20.sp
            )
        }
        
        if (message.isUser) {
            Spacer(modifier = Modifier.width(8.dp))
            // User avatar
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF9C27B0)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "U",
                    color = Color.White,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun SuggestionButtons(
    onSuggestionClick: (String) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = "💡 Gợi ý cho bạn:",
            fontSize = 13.sp,
            color = Color.Gray,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(bottom = 4.dp)
        )
        
        // Button 1: Meal suggestion
        OutlinedButton(
            onClick = { onSuggestionClick("Gợi ý bữa ăn phù hợp cho tôi") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.outlinedButtonColors(
                containerColor = Color.White,
                contentColor = Color(0xFF6AB9F5)
            ),
            border = ButtonDefaults.outlinedButtonBorder.copy(
                width = 1.dp,
                brush = androidx.compose.ui.graphics.SolidColor(Color(0xFF6AB9F5))
            )
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Start,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "🍎",
                    fontSize = 20.sp,
                    modifier = Modifier.padding(end = 8.dp)
                )
                Text(
                    text = "Gợi ý bữa ăn phù hợp",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
        
        // Button 2: Exercise plan
        OutlinedButton(
            onClick = { onSuggestionClick("Gợi ý bài tập tập luyện") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.outlinedButtonColors(
                containerColor = Color.White,
                contentColor = Color(0xFF6AB9F5)
            ),
            border = ButtonDefaults.outlinedButtonBorder.copy(
                width = 1.dp,
                brush = androidx.compose.ui.graphics.SolidColor(Color(0xFF6AB9F5))
            )
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Start,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "💪",
                    fontSize = 20.sp,
                    modifier = Modifier.padding(end = 8.dp)
                )
                Text(
                    text = "Gợi ý bài tập tập luyện",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
        
        // Button 3: Health analysis
        OutlinedButton(
            onClick = { onSuggestionClick("Phân tích chỉ số sức khỏe của tôi") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.outlinedButtonColors(
                containerColor = Color.White,
                contentColor = Color(0xFF6AB9F5)
            ),
            border = ButtonDefaults.outlinedButtonBorder.copy(
                width = 1.dp,
                brush = androidx.compose.ui.graphics.SolidColor(Color(0xFF6AB9F5))
            )
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Start,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "📊",
                    fontSize = 20.sp,
                    modifier = Modifier.padding(end = 8.dp)
                )
                Text(
                    text = "Phân tích chỉ số sức khỏe",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SuggestionChip(
    suggestion: QuickSuggestion,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier.width(160.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.Start
        ) {
            Text(
                text = suggestion.icon,
                fontSize = 24.sp,
                modifier = Modifier.padding(bottom = 4.dp)
            )
            Text(
                text = suggestion.title,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                color = Color.Black
            )
        }
    }
}

@Composable
fun TypingIndicator() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Start
    ) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(CircleShape)
                .background(Color(0xFF6AB9F5)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "AI",
                color = Color.White,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )
        }
        Spacer(modifier = Modifier.width(8.dp))
        
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                repeat(3) { index ->
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(Color.Gray.copy(alpha = 0.5f))
                    )
                }
            }
        }
    }
}

@Composable
fun MessageInputBar(
    messageText: String,
    onMessageChange: (String) -> Unit,
    onSendClick: () -> Unit,
    enabled: Boolean = true
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = Color.White,
        shadowElevation = 8.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedTextField(
                value = messageText,
                onValueChange = onMessageChange,
                modifier = Modifier.weight(1f),
                placeholder = { Text("Nhập câu hỏi hoặc chọn gợi ý...") },
                shape = RoundedCornerShape(24.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFF6AB9F5),
                    unfocusedBorderColor = Color.Gray.copy(alpha = 0.3f)
                ),
                enabled = enabled,
                maxLines = 3
            )
            
            IconButton(
                onClick = onSendClick,
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(
                        if (messageText.isBlank() || !enabled) Color.Gray.copy(alpha = 0.3f)
                        else Color(0xFF6AB9F5)
                    ),
                enabled = messageText.isNotBlank() && enabled
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Send,
                    contentDescription = "Gửi",
                    tint = Color.White
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun DataHintScreenPreview() {
    val navController = rememberNavController()
    Salud_AppTheme {
        DataHintScreen(navController = navController, onBackClicked = {})
    }
}

@Composable
fun SaveChatDialog(
    onDismiss: () -> Unit,
    onSave: (String) -> Unit,
    isSaving: Boolean
) {
    var chatName by remember { mutableStateOf("") }
    
    AlertDialog(
        onDismissRequest = { if (!isSaving) onDismiss() },
        title = { 
            Text(
                text = "Lưu đoạn chat",
                fontWeight = FontWeight.SemiBold
            ) 
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Nhập tên để lưu đoạn chat này, bạn có thể xem lại sau.",
                    fontSize = 14.sp,
                    color = Color.Gray,
                    lineHeight = 20.sp,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                
                OutlinedTextField(
                    value = chatName,
                    onValueChange = { chatName = it },
                    label = { Text("Tên đoạn chat") },
                    placeholder = { Text("Ví dụ: Kế hoạch tập gym") },
                    singleLine = true,
                    enabled = !isSaving,
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF6AB9F5),
                        focusedLabelColor = Color(0xFF6AB9F5)
                    )
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = { 
                    if (chatName.isNotBlank()) {
                        onSave(chatName.trim())
                    }
                },
                enabled = chatName.isNotBlank() && !isSaving
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
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                enabled = !isSaving
            ) {
                Text("Hủy")
            }
        }
    )
}

@Composable
fun SavedChatsDialog(
    savedChats: List<SavedChat>,
    onDismiss: () -> Unit,
    onLoadChat: (SavedChat) -> Unit,
    onDeleteChat: (String) -> Unit
) {
    var chatToDelete by remember { mutableStateOf<String?>(null) }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { 
            Text(
                "Chat đã lưu",
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            if (savedChats.isEmpty()) {
                Text(
                    "Chưa có đoạn chat nào được lưu",
                    fontSize = 14.sp,
                    color = Color.Gray,
                    modifier = Modifier.padding(vertical = 16.dp)
                )
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 400.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(savedChats) { chat ->
                        SavedChatItem(
                            savedChat = chat,
                            onLoadClick = { onLoadChat(chat) },
                            onDeleteClick = { chatToDelete = chat.id }
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Đóng")
            }
        }
    )
    
    // Delete confirmation dialog
    if (chatToDelete != null) {
        AlertDialog(
            onDismissRequest = { chatToDelete = null },
            title = { Text("Xác nhận xóa") },
            text = { Text("Bạn có chắc muốn xóa đoạn chat này?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        onDeleteChat(chatToDelete!!)
                        chatToDelete = null
                    }
                ) {
                    Text("Xóa", color = Color.Red)
                }
            },
            dismissButton = {
                TextButton(onClick = { chatToDelete = null }) {
                    Text("Hủy")
                }
            }
        )
    }
}

@Composable
fun SavedChatItem(
    savedChat: SavedChat,
    onLoadClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
    val dateString = dateFormat.format(Date(savedChat.timestamp))
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .clickable { onLoadClick() }
            ) {
                Text(
                    text = savedChat.name,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.Black
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = savedChat.preview,
                    fontSize = 12.sp,
                    color = Color.Gray,
                    maxLines = 2,
                    lineHeight = 16.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = dateString,
                    fontSize = 11.sp,
                    color = Color.Gray
                )
            }
            
            IconButton(
                onClick = onDeleteClick,
                modifier = Modifier.size(40.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Xóa",
                    tint = Color.Red
                )
            }
        }
    }
}
