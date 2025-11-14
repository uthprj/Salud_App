# Hướng dẫn sử dụng NumberPicker

## import
```
import com.example.salud_app.components.number_picker.NumberPicker
import com.example.salud_app.components.number_picker.rememberPickerState
```

## chuẩn bị dữ liệu
```agsl
val items = (0..59).map { "%02d".format(it) }
```

## tạo pickerstate
```kotlin
val pickerState = rememberPickerState("05")
```

## Dùng NumberPicker
```kotlin
NumberPicker(
    items = items,
    state = pickerState,
    startIndex = 5,
    visibleItemsCount = 3,
    textStyle = MaterialTheme.typography.headlineLarge,
    modifier = Modifier.padding(16.dp)
)
```

## Lấy giá trị đã chọn
```kotlin
val selectedValue = pickerState.selectedItem
```


## Ví dụ lấy từ DataHelthWeightScreen:
```kotlin
@Composable
fun WeightInputRow() {
    // 1. Tạo danh sách giá trị cho mỗi picker
    val integerItems = remember { (0..200).map { it.toString() } }
    val fractionalItems = remember { (10..90).map { "%02d".format(it) } }

    // 2. Tạo và nhớ trạng thái cho mỗi picker
    val integerState = rememberPickerState("70")
    val fractionalState = rememberPickerState("00")

    Column {

        // --- Hàng chứa các Number Picker ---
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            val textStyle = MaterialTheme.typography.headlineLarge
            val itemHeight = 65.dp

            // 3. Picker cho phần nguyên
            NumberPicker(
                state = integerState,
                items = integerItems,
                startIndex = integerItems.indexOf("70"),
                textStyle = textStyle,
                modifier = Modifier
                    .weight(1f)
                    .height(itemHeight * 3)
            )

            // Dấu chấm ngăn cách
            Text(
                text = ".",
                style = textStyle,
                modifier = Modifier.padding(horizontal = 8.dp)
            )

            // 4. Picker cho phần thập phân
            NumberPicker(
                state = fractionalState,
                items = fractionalItems,
                startIndex = fractionalItems.indexOf("00"),
                textStyle = textStyle,
                modifier = Modifier
                    .weight(1f)
                    .height(itemHeight * 3)
            )
        }
    }
}

```