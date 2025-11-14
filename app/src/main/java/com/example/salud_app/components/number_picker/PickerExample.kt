package com.example.salud_app.components.number_picker

import android.view.Surface
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun PickerExample() {
    Surface(modifier = Modifier.fillMaxSize()) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxSize()
        ) {

            val values = remember { (0..200).map { it.toString() } }
            val valuesPickerState = rememberPickerState()
            val units = remember { listOf("seconds", "minutes", "hours") }
            val unitsPickerState = rememberPickerState()

            Text(text = "Example Picker", modifier = Modifier.padding(top = 16.dp))
            Row(modifier = Modifier.fillMaxWidth()) {
                NumberPicker(
                    state = valuesPickerState,
                    items = values,
                    visibleItemsCount = 3,
                    modifier = Modifier.weight(0.3f),
                    textModifier = Modifier.padding(8.dp),
                    textStyle = TextStyle(fontSize = 32.sp)
                )
                NumberPicker(
                    state = unitsPickerState,
                    items = units,
                    visibleItemsCount = 3,
                    modifier = Modifier.weight(0.7f),
                    textModifier = Modifier.padding(8.dp),
                    textStyle = TextStyle(fontSize = 32.sp)
                )
            }

            Text(
                text = "Interval: ${valuesPickerState.selectedItem} ${unitsPickerState.selectedItem}",
                modifier = Modifier.padding(vertical = 16.dp)
            )

        }
    }
}

@Preview
@Composable
fun PickerExamplePreview() {
    PickerExample()
}