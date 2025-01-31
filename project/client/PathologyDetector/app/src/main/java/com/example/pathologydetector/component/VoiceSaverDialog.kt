package com.example.pathologydetector.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.pathologydetector.R
import com.example.pathologydetector.model.PlayerState

@Composable
fun VoiceSaverDialog(
    suggestedName: String,
    totalDuration: String,
    playerState: PlayerState,
    onSeekTo: (Float) -> Unit,
    onTogglePlayer: () -> Unit,
    onDismissRequest: () -> Unit,
    onSaveRecordingClicked: (String) -> Unit,
) {
    var fileName by remember {
        mutableStateOf(suggestedName)
    }

    val currentOnSaveRecordingClicked by rememberUpdatedState(onSaveRecordingClicked)

    Dialog(
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = true,
            decorFitsSystemWindows = true,
        ),
        onDismissRequest = onDismissRequest,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White, shape = RoundedCornerShape(10.dp))
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            TextField(
                value = fileName,
                onValueChange = { newValue ->
                    fileName = newValue
                },
                suffix = {
                    Text(text = ".wav")
                },
                label = {
                    Text(text = "File Name")
                },
                singleLine = true,
                isError = fileName.isBlank(),
                shape = RoundedCornerShape(10.dp),
                colors = TextFieldDefaults.colors(
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    disabledIndicatorColor = Color.Transparent,
                    errorIndicatorColor = Color.Transparent
                ),
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
            ) {
                IconButton(onClick = onTogglePlayer) {
                    Icon(
                        imageVector = if (playerState.isPlaying) {
                            ImageVector.vectorResource(R.drawable.icon_pause)
                        } else {
                            ImageVector.vectorResource(R.drawable.icon_play)
                        },
                        modifier = Modifier
                            .padding(4.dp)
                            .size(24.dp),
                        contentDescription = null,
                    )
                }

                Slider(
                    value = playerState.progress,
                    onValueChange = onSeekTo,
                    colors = SliderDefaults.colors(
                        thumbColor = Color(0xFF304FFE),
                        activeTrackColor = Color(0xFF304FFE),
                    )
                )
            }

            Text(
                fontSize = 24.sp,
                text = totalDuration,
                modifier = Modifier.padding(vertical = 4.dp),
            )

            Button(
                onClick = {
                    currentOnSaveRecordingClicked(fileName)
                },
                enabled = fileName.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF304FFE))
            ) {
                Text(text = "Save")
            }
        }
    }
}