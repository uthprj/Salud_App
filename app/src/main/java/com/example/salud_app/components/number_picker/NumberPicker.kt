package com.example.salud_app.components.number_picker

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.salud_app.ui.theme.Salud_AppTheme
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun NumberPicker(
    items: List<String>,
    state: PickerState = rememberPickerState(),
    modifier: Modifier = Modifier,
    startIndex: Int = 0,
    visibleItemsCount: Int = 3,
    textModifier: Modifier = Modifier.padding(vertical = 12.dp),
    textStyle: TextStyle = LocalTextStyle.current,
    dividerColor: Color = LocalContentColor.current,
) {
    // --- CÁC BIẾN MỚI ĐỂ HỖ TRỢ NHẬP LIỆU ---
    var isEditing by remember { mutableStateOf(false) }
    var editFieldValue by remember { mutableStateOf(state.selectedItem) }
    val focusRequester = remember { FocusRequester() }
    val keyboardController = LocalSoftwareKeyboardController.current
    val coroutineScope = rememberCoroutineScope()
    // -----------------------------------------

    val visibleItemsMiddle = visibleItemsCount / 2
    val listScrollCount = Integer.MAX_VALUE
    val listScrollMiddle = listScrollCount / 2
    val listStartIndex = remember(items, startIndex) {
        listScrollMiddle - listScrollMiddle % items.size - visibleItemsMiddle + startIndex
    }

    fun getItem(index: Int) = items.getOrElse(index % items.size) { "" }

    val listState = rememberLazyListState(initialFirstVisibleItemIndex = listStartIndex)
    val flingBehavior = rememberSnapFlingBehavior(lazyListState = listState)

    val itemHeightPixels = remember { mutableStateOf(0) }
    val itemHeightDp = pixelsToDp(itemHeightPixels.value)

    val fadingEdgeGradient = remember {
        Brush.verticalGradient(
            0f to Color.Transparent,
            0.5f to Color.Black,
            1f to Color.Transparent
        )
    }

    // Cập nhật giá trị khi cuộn
    LaunchedEffect(listState) {
        snapshotFlow { listState.firstVisibleItemIndex }
            .map { index -> getItem(index + visibleItemsMiddle) }
            .distinctUntilChanged()
            .collect { item ->
                if (!isEditing) {
                    state.selectedItem = item
                    editFieldValue = item
                }
            }
    }

    // Hàm để xử lý khi nhập liệu xong
    fun onEditDone() {
        val finalValue = editFieldValue.ifBlank { items.first() }
        val targetIndex = items.indexOf(finalValue).coerceAtLeast(0)
        state.selectedItem = items[targetIndex]
        isEditing = false
        keyboardController?.hide()

        // Cuộn đến giá trị mới
        coroutineScope.launch {
            val newStartIndex = listScrollMiddle - listScrollMiddle % items.size - visibleItemsMiddle + targetIndex
            listState.scrollToItem(newStartIndex)
        }
    }

    Box(modifier = modifier) {
        if (isEditing) {
            // --- GIAO DIỆN KHI NHẬP LIỆU ---
            BasicTextField(
                value = editFieldValue,
                onValueChange = { newValue ->
                    // Chỉ cho phép nhập số và giới hạn độ dài
                    if (newValue.all { it.isDigit() } && newValue.length <= items.last().length) {
                        editFieldValue = newValue
                    }
                },
                modifier = Modifier
                    .align(Alignment.Center)
                    .focusRequester(focusRequester)
                    .onFocusChanged { focusState ->
                        if (!focusState.isFocused) {
                            onEditDone()
                        }
                    },
                textStyle = textStyle.copy(textAlign = TextAlign.Center),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Number,
                    imeAction = ImeAction.Done
                ),
                keyboardActions = KeyboardActions(onDone = { onEditDone() }),
                singleLine = true
            )
            // Yêu cầu focus và hiển thị bàn phím khi isEditing=true
            LaunchedEffect(Unit) {
                focusRequester.requestFocus()
            }
        } else {
            // --- GIAO DIỆN PICKER BÌNH THƯỜNG ---
            LazyColumn(
                state = listState,
                flingBehavior = flingBehavior,
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(itemHeightDp * visibleItemsCount)
                    .fadingEdge(fadingEdgeGradient)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null // không có hiệu ứng gợn sóng
                    ) {
                        isEditing = true
                    }
            ) {
                items(listScrollCount) { index ->
                    Text(
                        text = getItem(index),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        style = textStyle,
                        modifier = Modifier
                            .onSizeChanged { size -> itemHeightPixels.value = size.height }
                            .then(textModifier)
                    )
                }
            }
        }

        // Đường kẻ ngang (hiển thị cả 2 chế độ)
        val dividerModifier = Modifier
            .offset(y = itemHeightDp * visibleItemsMiddle)
            .padding(horizontal = 20.dp)

        Divider(color = dividerColor, modifier = dividerModifier)
        Divider(color = dividerColor, modifier = dividerModifier.offset(y = itemHeightDp))
    }
}

private fun Modifier.fadingEdge(brush: Brush) = this
    .graphicsLayer(compositingStrategy = CompositingStrategy.Offscreen)
    .drawWithContent {
        drawContent()
        drawRect(brush = brush, blendMode = BlendMode.DstIn)
    }

@Composable
private fun pixelsToDp(pixels: Int) = with(LocalDensity.current) { pixels.toDp() }

@Preview(showBackground = true, widthDp = 300)
@Composable
private fun NumberPickerPreview() {
    Salud_AppTheme {
        val items = remember { (0..99).map { "%02d".format(it) } }
        val pickerState = rememberPickerState("25")

        NumberPicker(
            items = items,
            state = pickerState,
            startIndex = 25,
            visibleItemsCount = 3,
            textStyle = MaterialTheme.typography.headlineLarge,
            modifier = Modifier.padding(16.dp)
        )
    }
}

