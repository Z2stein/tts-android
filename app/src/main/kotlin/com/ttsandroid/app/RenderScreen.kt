package com.ttsandroid.app

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.ttsandroid.domain.RenderStyle

@Composable
fun RenderScreen(
    state: RenderUiState,
    onTextChanged: (String) -> Unit,
    onStyleSelected: (RenderStyle) -> Unit,
    onGenerateClicked: () -> Unit,
    onCancelClicked: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("German Text to Speech (MVP)")

        BasicTextField(
            value = state.text(),
            onValueChange = onTextChanged,
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
        )

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(onClick = { onStyleSelected(RenderStyle.NEUTRAL) }) {
                Text("Neutral")
            }
            Button(onClick = { onStyleSelected(RenderStyle.EXPRESSIVE) }) {
                Text("Expressive")
            }
        }

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(onClick = onGenerateClicked, enabled = !state.isRendering()) {
                Text("Generate")
            }
            Button(onClick = onCancelClicked, enabled = state.isRendering()) {
                Text("Cancel")
            }
        }

        Text("Progress: ${state.completedChunks()}/${state.totalChunks()}")
        Text("Status: ${state.statusMessage()}")
        state.outputPath()?.let { Text("Output: $it") }
    }
}
