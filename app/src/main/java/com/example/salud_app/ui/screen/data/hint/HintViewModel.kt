package com.example.salud_app.ui.screen.data.hint

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.salud_app.BuildConfig
import com.example.salud_app.model.ChatMessage
import com.example.salud_app.model.QuickSuggestion
import com.example.salud_app.model.SavedChat
import com.example.salud_app.model.SuggestionType
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.generationConfig
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

data class HintUiState(
    val messages: List<ChatMessage> = emptyList(),
    val isLoading: Boolean = false,
    val currentWeight: Double = 0.0,
    val currentHeight: Double = 0.0,
    val targetWeight: Double = 0.0,
    val goalType: String = "", // "gain" hoặc "lose"
    val error: String? = null,
    val savedChats: List<SavedChat> = emptyList(),
    val showSaveDialog: Boolean = false,
    val showSavedChatsDialog: Boolean = false,
    val isSavingChat: Boolean = false
)

class HintViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(HintUiState())
    val uiState: StateFlow<HintUiState> = _uiState.asStateFlow()

    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private var sharedPreferences: SharedPreferences? = null
    
    private val generativeModel: GenerativeModel

    init {
        generativeModel = GenerativeModel(
            modelName = "gemini-2.0-flash-lite",
            apiKey = BuildConfig.API_KEY,
            generationConfig = generationConfig {
                temperature = 0.7f
                topK = 40
                topP = 0.95f
            }
        )
        
        addWelcomeMessage()
    }
    
    /**
     * Khởi tạo với context để lấy SharedPreferences
     */
    fun initialize(context: Context) {
        sharedPreferences = context.getSharedPreferences("goal_prefs", Context.MODE_PRIVATE)
        loadUserData()
    }

    /**
     * Tải dữ liệu người dùng từ Firebase và SharedPreferences
     */
    private fun loadUserData() {
        viewModelScope.launch {
            try {
                val currentUser = auth.currentUser ?: return@launch

                // Lấy cân nặng và chiều cao hiện tại từ SharedPreferences (Goal)
                val currentWeight = sharedPreferences?.getFloat("current_weight", 0f)?.toDouble() ?: 0.0
                val currentHeight = sharedPreferences?.getFloat("current_height", 0f)?.toDouble() ?: 0.0
                val targetWeight = sharedPreferences?.getFloat("target_weight", 0f)?.toDouble() ?: 0.0
                
                // Nếu không có trong SharedPreferences, lấy từ HealthRecords
                var finalWeight = currentWeight
                var finalHeight = currentHeight
                
                if (finalWeight == 0.0 || finalHeight == 0.0) {
                    val healthRecords = firestore.collection("User")
                        .document(currentUser.uid)
                        .collection("HealthRecords")
                        .orderBy("timestamp", com.google.firebase.firestore.Query.Direction.DESCENDING)
                        .limit(10)
                        .get()
                        .await()

                    for (doc in healthRecords.documents) {
                        if (finalWeight == 0.0) {
                            finalWeight = doc.getDouble("weight") ?: 0.0
                        }
                        if (finalHeight == 0.0) {
                            finalHeight = doc.getDouble("height") ?: 0.0
                        }
                        if (finalWeight > 0 && finalHeight > 0) break
                    }
                }

                // Xác định loại mục tiêu
                val goalType = if (targetWeight > 0 && finalWeight > 0) {
                    if (targetWeight > finalWeight) "gain" else "lose"
                } else ""

                _uiState.value = _uiState.value.copy(
                    currentWeight = finalWeight,
                    currentHeight = finalHeight,
                    targetWeight = targetWeight,
                    goalType = goalType
                )

            } catch (e: Exception) {
                Log.e("HintViewModel", "Error loading user data", e)
            }
        }
    }

    /**
     * Thêm tin nhắn chào mừng
     */
    private fun addWelcomeMessage() {
        val welcomeMsg = ChatMessage(
            id = "welcome",
            content = "Xin chào! Tôi là trợ lý AI của bạn. \n\nHãy chọn một gợi ý bên dưới để tôi hỗ trợ bạn!",
            isUser = false,
            timestamp = System.currentTimeMillis()
        )
        
        _uiState.value = _uiState.value.copy(
            messages = listOf(welcomeMsg)
        )
    }

    /**
     * Lấy danh sách gợi ý nhanh
     */
    fun getQuickSuggestions(): List<QuickSuggestion> {
        val currentState = _uiState.value
        val goalText = when {
            currentState.goalType == "gain" -> "tăng cân"
            currentState.goalType == "lose" -> "giảm cân"
            else -> "duy trì sức khỏe"
        }

        return listOf(
            QuickSuggestion(
                title = "Gợi ý bữa ăn",
                icon = "🍎",
                type = SuggestionType.MEAL_PLAN,
                prompt = "Tôi cần gợi ý thực đơn $goalText cho một ngày"
            ),
            QuickSuggestion(
                title = "Kế hoạch tập luyện",
                icon = "💪",
                type = SuggestionType.EXERCISE_PLAN,
                prompt = "Đề xuất bài tập phù hợp để $goalText"
            ),
            QuickSuggestion(
                title = "Phân tích sức khỏe",
                icon = "📊",
                type = SuggestionType.GENERAL_HEALTH,
                prompt = "Phân tích tình trạng sức khỏe hiện tại của tôi"
            ),
            QuickSuggestion(
                title = "Tips & Tricks hằng ngày",
                icon = "💡",
                type = SuggestionType.DAILY_TIPS,
                prompt = "Cho tôi tips và tricks hằng ngày về lối sống lành mạnh"
            )
        )
    }

    /**
     * Gửi tin nhắn và nhận phản hồi từ AI
     */
    fun sendMessage(userMessage: String) {
        if (userMessage.isBlank()) return

        viewModelScope.launch {
            try {
                // Thêm tin nhắn người dùng
                val userMsg = ChatMessage(
                    id = "user_${System.currentTimeMillis()}",
                    content = userMessage,
                    isUser = true,
                    timestamp = System.currentTimeMillis()
                )

                _uiState.value = _uiState.value.copy(
                    messages = _uiState.value.messages + userMsg,
                    isLoading = true
                )

                // Tạo context cho AI
                val context = buildContext()
                val chatHistory = buildChatHistory()
                
                // Xác định loại gợi ý từ userMessage
                val isMealPlan = userMessage.contains("bữa ăn", ignoreCase = true) || 
                                 userMessage.contains("thực đơn", ignoreCase = true) ||
                                 userMessage.contains("ăn uống", ignoreCase = true) ||
                                 userMessage.contains("thức ăn", ignoreCase = true) ||
                                 userMessage.contains("chế độ dinh dưỡng", ignoreCase = true)
                                 
                val isExercisePlan = userMessage.contains("bài tập", ignoreCase = true) ||
                                     userMessage.contains("tập luyện", ignoreCase = true) ||
                                     userMessage.contains("workout", ignoreCase = true) ||
                                     userMessage.contains("gym", ignoreCase = true) ||
                                     userMessage.contains("cardio", ignoreCase = true)
                
                val isDailyTips = userMessage.contains("tips", ignoreCase = true) ||
                                  userMessage.contains("tricks", ignoreCase = true) ||
                                  userMessage.contains("mẹo", ignoreCase = true) ||
                                  userMessage.contains("lối sống", ignoreCase = true) ||
                                  userMessage.contains("thói quen", ignoreCase = true) ||
                                  userMessage.contains("hằng ngày", ignoreCase = true)
                
                val fullPrompt = when {
                    isMealPlan -> """
                        BẠN LÀ TRỢ LÝ DINH DƯỠNG CHUYÊN NGHIỆP
                        
                        $context
                        
                        $chatHistory
                        
                        Yêu cầu của người dùng: $userMessage
                        
                        Hãy gợi ý thực đơn CHI TIẾT cho 1 ngày với định dạng sau:
                        
                        🌅Bữa sáng (7h-8h):
                        - [Tên món chính]: [Khối lượng cụ thể] - [Calo]
                        - [Món phụ 1]: [Khối lượng] - [Calo]
                        - [Món phụ 2]: [Khối lượng] - [Calo]
                        -Tổng: ~[XXX] kcal
                        
                        🍎Bữa phụ sáng (10h):
                        - [Loại trái cây/hạt]: [Khối lượng] - [Calo]
                        
                        🌞Bữa trưa (12h-13h):
                        - [Món chính - Protein]: [Khối lượng] - [Calo]
                        - [Tinh bột]: [Khối lượng] - [Calo]
                        - [Rau xào/canh]: [Khối lượng] - [Calo]
                        -Tổng: ~[XXX] kcal
                        
                        🥤Bữa phụ chiều (16h):
                        - [Sữa/sữa chuối/hạt]: [Khối lượng] - [Calo]
                        
                        🌙Bữa tối (18h-19h):
                        - [Món chính - Protein]: [Khối lượng] - [Calo]
                        - [Rau củ quả]: [Khối lượng] - [Calo]
                        - [Tinh bột (tùy chọn)]: [Khối lượng] - [Calo]
                        -Tổng: ~[XXX] kcal
                        
                        💧Nước: 2-2.5 lít/ngày (8-10 ly)
                        
                        ✨Tổng calo ngày: ~[XXXX] kcal
                        
                        📌Lưu ý quan trọng:
                        - [Lưu ý 1 về dinh dưỡng]
                        - [Lưu ý 2 về thời gian ăn]
                        
                        TRẢ LỜI NGẮN GỌN, CHI TIẾT, ĐÚNG ĐỊNH DẠNG TRÊN. KHÔNG NÓI NGOÀI LỀ.
                    """.trimIndent()
                    
                    isExercisePlan -> """
                        BẠN LÀ HUẤN LUYỆN VIÊN THỂ DỤC CHUYÊN NGHIỆP
                        
                        $context
                        
                        $chatHistory
                        
                        Yêu cầu của người dùng: $userMessage
                        
                        Hãy đề xuất kế hoạch tập luyện CHI TIẾT với định dạng sau:
                        
                        💪Kế hoạch tập [Loại: Cardio/Tăng cơ/Giảm cân]:
                        
                       KHỜi ĐỘNG (5-10 phút):
                        - [Bài khởi động 1]: [Thời gian/số lần]
                        - [Bài khởi động 2]: [Thời gian/số lần]
                        
                       BÀI TẬP CHÍNH:
                        
                        🏋️Bài 1: [Tên bài tập cụ thể] (Nhóm cơ: [Tên nhóm])
                        - Số set: [X] sets
                        - Số reps: [Y] reps/set (hoặc [Z] giây)
                        - Nghỉ giữa các set: [T] giây
                        - Cường độ: [Nhẹ/Vừa/Nặng]
                        
                        🏋️Bài 2: [Tên bài tập cụ thể] (Nhóm cơ: [Tên nhóm])
                        - Số set: [X] sets
                        - Số reps: [Y] reps/set (hoặc [Z] giây)
                        - Nghỉ giữa các set: [T] giây
                        - Cường độ: [Nhẹ/Vừa/Nặng]
                        
                        🏋️Bài 3: [Tên bài tập cụ thể] (Nhóm cơ: [Tên nhóm])
                        - Số set: [X] sets
                        - Số reps: [Y] reps/set (hoặc [Z] giây)
                        - Nghỉ giữa các set: [T] giây
                        - Cường độ: [Nhẹ/Vừa/Nặng]
                        
                        🏋️Bài 4: [Tên bài tập cụ thể] (Nhóm cơ: [Tên nhóm])
                        - Số set: [X] sets
                        - Số reps: [Y] reps/set (hoặc [Z] giây)
                        - Nghỉ giữa các set: [T] giây
                        - Cường độ: [Nhẹ/Vừa/Nặng]
                        
                       GIÃN CƠ (5-10 phút):
                        - [Bài giãn cơ 1]: [Thời gian]
                        - [Bài giãn cơ 2]: [Thời gian]
                        
                        ⏱️Tổng thời gian: [XX] phút
                        🔥Calo đốt: ~[XXX] kcal
                        
                        📌Lưu ý quan trọng:
                        - [Kỹ thuật thực hiện]
                        - [Tần suất tập/tuần]
                        - [Chế độ nghỉ ngơi]
                        
                        TRẢ LỜI NGẮN GỌN, CHI TIẾT, ĐÚNG ĐỊNH DẠNG TRÊN. KHÔNG NÓI NGOÀI LỀ.
                    """.trimIndent()
                    
                    isDailyTips -> """
                        BẠN LÀ CHUYÊN GIA VỀ LỐI SỐNG LÀNH MẠNH
                        
                        $context
                        
                        $chatHistory
                        
                        Yêu cầu của người dùng: $userMessage
                        
                        Hãy đưa ra TIPS & TRICKS HẰNG NGÀY về lối sống lành mạnh với định dạng sau:
                        
                        💡 TIPS & TRICKS HẰNG NGÀY VỀ LỐI SỐNG LÀNH MẠNH
                        
                        🌅 BUỔI SÁNG:
                        1. [Tip 1]: [Mô tả chi tiết và lợi ích]
                        2. [Tip 2]: [Mô tả chi tiết và lợi ích]
                        3. [Tip 3]: [Mô tả chi tiết và lợi ích]
                        
                        🌞 BUỔI TRƯA:
                        1. [Tip 1]: [Mô tả chi tiết và lợi ích]
                        2. [Tip 2]: [Mô tả chi tiết và lợi ích]
                        3. [Tip 3]: [Mô tả chi tiết và lợi ích]
                        
                        🌙 BUỔI TỐI:
                        1. [Tip 1]: [Mô tả chi tiết và lợi ích]
                        2. [Tip 2]: [Mô tả chi tiết và lợi ích]
                        3. [Tip 3]: [Mô tả chi tiết và lợi ích]
                        
                        💪 THÓI QUEN TỐT CẦN DUY TRÌ:
                        • [Thói quen 1]: [Tại sao quan trọng]
                        • [Thói quen 2]: [Tại sao quan trọng]
                        • [Thói quen 3]: [Tại sao quan trọng]
                        • [Thói quen 4]: [Tại sao quan trọng]
                        
                        ⚠️ NHỮNG ĐIỀU NÊN TRÁNH:
                        • [Điều nên tránh 1]: [Lý do]
                        • [Điều nên tránh 2]: [Lý do]
                        • [Điều nên tránh 3]: [Lý do]
                        
                        🎯 ĐẶC BIỆT CHO MỤC TIÊU CỦA BẠN:
                        [Gợi ý đặc biệt dựa trên mục tiêu tăng/giảm cân của người dùng]
                        
                        TRẢ LỜI NGẮN GỌN, THỰC TẾ, DỄ ÁP DỤNG. KHÔNG NÓI NGOÀI LỀ.
                    """.trimIndent()
                    
                    else -> """
                        BẠN LÀ TRỢ LÝ SỨC KHỎE AI CHUYÊN NGHIỆP
                        
                        QUY TẮC QUAN TRỌNG:
                        - CHỈ TRẢ LỜI CÂU HỎI LIÊN QUAN ĐẾN SỨC KHỎE, THỂ DỤC, DINH DƯỠNG, BMI, CÂN NẶNG, CHIỀU CAO, LỐI SỐNG LÀNH MẠNH
                        - NẾU CÂU HỎI KHÔNG LIÊN QUAN SỨC KHỎE: TRẢ LỜI "Xin lỗi, tôi chỉ có thể trả lời các câu hỏi liên quan đến sức khỏe, dinh dưỡng và thể dục."
                        - TRẢ LỜI TỰ NHIÊN, CHI TIẾT, DỰA VÀO THÔNG TIN NGƯỜI DÙNG ĐỂ TƯ VẤN CHÍNH XÁC
                        - LƯU Ý: Người dùng đang tự hỏi (không chọn gợi ý), hãy trả lời linh hoạt và phù hợp với câu hỏi
                        
                        $context
                        
                        $chatHistory
                        
                        Câu hỏi của người dùng: $userMessage
                        
                        Hãy trả lời câu hỏi một cách TỰ NHIÊN, CHI TIẾT dựa trên:
                        - Thông tin sức khỏe của người dùng (cân nặng, chiều cao, BMI, mục tiêu)
                        - Ngữ cảnh câu hỏi và lịch sử hội thoại
                        - Kiến thức chuyên môn về sức khỏe
                        
                        Cấu trúc câu trả lời (linh hoạt theo câu hỏi):
                        
                        💡 Trả lời câu hỏi:
                        [Trả lời chi tiết, tự nhiên câu hỏi của người dùng]
                        
                        📊 Phân tích dựa trên thông tin của bạn:
                        [Phân tích cụ thể dựa vào BMI, cân nặng, mục tiêu của người dùng]
                        
                        🎯 Gợi ý cho bạn:
                        [Gợi ý cụ thể phù hợp với tình trạng và mục tiêu]
                        
                        TRẢ LỜI TỰ NHIÊN NHƯ CHUYÊN GIA TƯ VẤN, DỰA VÀO THÔNG TIN CỤ THỂ CỦA NGƯỜI DÙNG.
                    """.trimIndent()
                }

                // Gọi Gemini AI
                val response = generativeModel.generateContent(fullPrompt)
                val aiResponse = response.text ?: "Xin lỗi, tôi không thể trả lời câu hỏi này."

                // Thêm tin nhắn AI
                val aiMsg = ChatMessage(
                    id = "ai_${System.currentTimeMillis()}",
                    content = aiResponse,
                    isUser = false,
                    timestamp = System.currentTimeMillis()
                )

                _uiState.value = _uiState.value.copy(
                    messages = _uiState.value.messages + aiMsg,
                    isLoading = false
                )

            } catch (e: Exception) {
                Log.e("HintViewModel", "Error sending message", e)
                
                val errorMsg = ChatMessage(
                    id = "error_${System.currentTimeMillis()}",
                    content = "Xin lỗi, đã có lỗi xảy ra. Vui lòng thử lại.",
                    isUser = false,
                    timestamp = System.currentTimeMillis()
                )

                _uiState.value = _uiState.value.copy(
                    messages = _uiState.value.messages + errorMsg,
                    isLoading = false,
                    error = e.message
                )
            }
        }
    }

    /**
     * Xây dựng context về người dùng cho AI
     */
    private fun buildContext(): String {
        val state = _uiState.value
        val bmi = if (state.currentHeight > 0 && state.currentWeight > 0) {
            val heightInMeters = state.currentHeight / 100
            state.currentWeight / (heightInMeters * heightInMeters)
        } else 0.0
        
        val bmiCategory = when {
            bmi < 18.5 -> "Thiếu cân"
            bmi < 25 -> "Bình thường"
            bmi < 30 -> "Thừa cân"
            else -> "Béo phì"
        }

        return buildString {
            appendLine("THÔNG TIN NGƯỜI DÙNG:")
            appendLine("=".repeat(30))
            
            if (state.currentWeight > 0) {
                appendLine("• Cân nặng: ${state.currentWeight} kg")
            }
            
            if (state.currentHeight > 0) {
                appendLine("• Chiều cao: ${state.currentHeight} cm")
            }
            
            if (bmi > 0) {
                appendLine("• BMI: %.1f ($bmiCategory)".format(bmi))
            }
            
            if (state.targetWeight > 0) {
                appendLine("• Mục tiêu: ${state.targetWeight} kg")
                val diff = kotlin.math.abs(state.targetWeight - state.currentWeight)
                when (state.goalType) {
                    "gain" -> appendLine("• Cần tăng: %.1f kg".format(diff))
                    "lose" -> appendLine("• Cần giảm: %.1f kg".format(diff))
                }
            }
            
            appendLine("=".repeat(30))
        }
    }

    /**
     * Xây dựng lịch sử chat để AI có ngữ cảnh liền mạch
     */
    private fun buildChatHistory(): String {
        val messages = _uiState.value.messages
        
        // Lấy 6 tin nhắn gần nhất (3 cặp hội thoại)
        val recentMessages = messages.takeLast(6).filter { it.id != "welcome" }
        
        if (recentMessages.isEmpty()) {
            return ""
        }
        
        return buildString {
            appendLine("\nLỊCH SỬ HỘI THOẠI GẦN ĐÂY:")
            appendLine("-".repeat(30))
            
            recentMessages.forEach { msg ->
                if (msg.isUser) {
                    appendLine("👤 Người dùng: ${msg.content}")
                } else {
                    appendLine("🤖 AI: ${msg.content.take(150)}${if (msg.content.length > 150) "..." else ""}")
                }
            }
            
            appendLine("-".repeat(30))
            appendLine("Hãy dựa vào lịch sử trên để trả lời có tính liên tục.\n")
        }
    }

    /**
     * Xóa lỗi
     */
    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }

    /**
     * Hiển thị dialog để lưu chat
     */
    fun showSaveChatDialog() {
        _uiState.value = _uiState.value.copy(showSaveDialog = true)
    }

    /**
     * Đóng dialog lưu chat
     */
    fun dismissSaveChatDialog() {
        _uiState.value = _uiState.value.copy(showSaveDialog = false)
    }

    /**
     * Lưu đoạn chat với tên
     */
    fun saveChat(chatName: String) {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isSavingChat = true)
                
                val currentUser = auth.currentUser ?: return@launch
                val messages = _uiState.value.messages
                
                if (messages.size <= 1) {
                    Log.d("HintViewModel", "No messages to save")
                    return@launch
                }
                
                // Tạo preview từ phản hồi AI đầu tiên
                val preview = messages.firstOrNull { !it.isUser }?.content?.take(100) ?: ""
                
                val chatId = firestore.collection("User")
                    .document(currentUser.uid)
                    .collection("SavedChats")
                    .document().id
                
                val savedChat = hashMapOf(
                    "id" to chatId,
                    "name" to chatName,
                    "messages" to messages.map { msg ->
                        hashMapOf(
                            "id" to msg.id,
                            "content" to msg.content,
                            "isUser" to msg.isUser,
                            "timestamp" to msg.timestamp
                        )
                    },
                    "timestamp" to System.currentTimeMillis(),
                    "preview" to preview
                )
                
                firestore.collection("User")
                    .document(currentUser.uid)
                    .collection("SavedChats")
                    .document(chatId)
                    .set(savedChat)
                    .await()
                
                Log.d("HintViewModel", "Chat saved successfully: $chatName")
                
                // Load lại danh sách saved chats
                loadSavedChats()
                
                _uiState.value = _uiState.value.copy(
                    isSavingChat = false,
                    showSaveDialog = false
                )
                
            } catch (e: Exception) {
                Log.e("HintViewModel", "Error saving chat", e)
                _uiState.value = _uiState.value.copy(isSavingChat = false)
            }
        }
    }

    /**
     * Hiển thị dialog xem các chat đã lưu
     */
    fun showSavedChatsDialog() {
        loadSavedChats()
        _uiState.value = _uiState.value.copy(showSavedChatsDialog = true)
    }

    /**
     * Đóng dialog xem chat đã lưu
     */
    fun dismissSavedChatsDialog() {
        _uiState.value = _uiState.value.copy(showSavedChatsDialog = false)
    }

    /**
     * Tải danh sách chat đã lưu
     */
    private fun loadSavedChats() {
        viewModelScope.launch {
            try {
                val currentUser = auth.currentUser ?: return@launch
                
                val snapshot = firestore.collection("User")
                    .document(currentUser.uid)
                    .collection("SavedChats")
                    .orderBy("timestamp", com.google.firebase.firestore.Query.Direction.DESCENDING)
                    .get()
                    .await()
                
                val chats = snapshot.documents.mapNotNull { doc ->
                    try {
                        SavedChat(
                            id = doc.getString("id") ?: "",
                            name = doc.getString("name") ?: "",
                            messages = (doc.get("messages") as? List<Map<String, Any>>)?.map { msgMap ->
                                ChatMessage(
                                    id = msgMap["id"] as? String ?: "",
                                    content = msgMap["content"] as? String ?: "",
                                    isUser = msgMap["isUser"] as? Boolean ?: false,
                                    timestamp = (msgMap["timestamp"] as? Long) ?: 0L
                                )
                            } ?: emptyList(),
                            timestamp = doc.getLong("timestamp") ?: 0L,
                            preview = doc.getString("preview") ?: ""
                        )
                    } catch (e: Exception) {
                        Log.e("HintViewModel", "Error parsing saved chat", e)
                        null
                    }
                }
                
                _uiState.value = _uiState.value.copy(savedChats = chats)
                
            } catch (e: Exception) {
                Log.e("HintViewModel", "Error loading saved chats", e)
            }
        }
    }

    /**
     * Tải lại một đoạn chat đã lưu
     */
    fun loadSavedChat(savedChat: SavedChat) {
        _uiState.value = _uiState.value.copy(
            messages = savedChat.messages,
            showSavedChatsDialog = false
        )
    }

    /**
     * Xóa một đoạn chat đã lưu
     */
    fun deleteSavedChat(chatId: String) {
        viewModelScope.launch {
            try {
                val currentUser = auth.currentUser ?: return@launch
                
                firestore.collection("User")
                    .document(currentUser.uid)
                    .collection("SavedChats")
                    .document(chatId)
                    .delete()
                    .await()
                
                Log.d("HintViewModel", "Chat deleted successfully")
                
                // Load lại danh sách
                loadSavedChats()
                
            } catch (e: Exception) {
                Log.e("HintViewModel", "Error deleting chat", e)
            }
        }
    }

    /**
     * Bắt đầu chat mới
     */
    fun startNewChat() {
        _uiState.value = HintUiState(
            currentWeight = _uiState.value.currentWeight,
            currentHeight = _uiState.value.currentHeight,
            targetWeight = _uiState.value.targetWeight,
            goalType = _uiState.value.goalType
        )
        addWelcomeMessage()
    }
}
