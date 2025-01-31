package com.example.pathologydetector.component

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.dp
import com.example.pathologydetector.R

@Composable
fun FloatingButton(
    isPermissionGranted: Boolean,
    onRecordVoiceClicked: () -> Unit,
    onChooseVoiceClicked: () -> Unit,
) {
    var isVoiceSelectionEnabled by remember { mutableStateOf(false) }
    val currentOnRecordVoiceClicked by rememberUpdatedState(onRecordVoiceClicked)
    val currentOnChooseVoiceClicked by rememberUpdatedState(onChooseVoiceClicked)

    Image(
        imageVector = if (isVoiceSelectionEnabled) {
            ImageVector.vectorResource(R.drawable.icon_drive_upload)
        } else {
            ImageVector.vectorResource(R.drawable.icon_microphone)
        },
        colorFilter = ColorFilter.tint(Color.White),
        contentDescription = null,
        modifier = Modifier
            .padding(10.dp)
            .clip(RoundedCornerShape(percent = 50))
            .background(
                if (isPermissionGranted || isVoiceSelectionEnabled) {
                    Color(0xFF304FFE)
                } else {
                    Color(0xFFD61A05)
                }
            )
            .size(50.dp)
            .pointerInput(isVoiceSelectionEnabled) {
                if (isVoiceSelectionEnabled) {
                    detectTapGestures(
                        onTap = {
                            currentOnChooseVoiceClicked()
                        },
                        onLongPress = {
                            currentOnChooseVoiceClicked()
                        },
                        onDoubleTap = {
                            isVoiceSelectionEnabled = !isVoiceSelectionEnabled
                        }
                    )
                } else {
                    detectTapGestures(
                        onTap = {
                            currentOnRecordVoiceClicked()
                        },
                        onLongPress = {
                            currentOnRecordVoiceClicked()
                        },
                        onDoubleTap = {
                            isVoiceSelectionEnabled = !isVoiceSelectionEnabled
                        },
                    )
                }
            }
            .padding(10.dp)
    )
}