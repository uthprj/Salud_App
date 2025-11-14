package com.example.salud_app.components.number_picker

import androidx.compose.runtime.*

/**
 * Lớp state holder cho NumberPicker.
 * @param initialValue Giá trị được chọn ban đầu.
 */
@Stable
class PickerState(initialValue: String) {
    var selectedItem by mutableStateOf(initialValue)
}

/**
 * Ghi nhớ một [PickerState] trong Composable.
 * @param initialValue Giá trị được chọn ban đầu.
 */
@Composable
fun rememberPickerState(initialValue: String = ""): PickerState {
    return remember(initialValue) { PickerState(initialValue) }
}