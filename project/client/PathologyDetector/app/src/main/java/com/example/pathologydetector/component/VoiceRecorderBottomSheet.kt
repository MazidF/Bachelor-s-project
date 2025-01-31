package com.example.pathologydetector.component

import android.annotation.SuppressLint
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetValue
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import com.example.pathologydetector.R
import com.example.pathologydetector.model.RecorderState

@SuppressLint("ReturnFromAwaitPointerEventScope")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VoiceRecorderBottomSheet(
    recorderState: RecorderState,
    onStartRecording: () -> Unit,
    onStopRecording: () -> Unit,
    onDismissRequest: () -> Unit,
) {
    val currentOnStopRecording by rememberUpdatedState(onStopRecording)
    val currentOnStartRecording by rememberUpdatedState(onStartRecording)

    val context = LocalContext.current
    LaunchedEffect(recorderState) {
        if (recorderState is RecorderState.Error) {
            vibrate(context, durationMillis = 100)
        }
    }

    ModalBottomSheet(
        windowInsets = NonWindowInsets(),
        onDismissRequest = onDismissRequest,
    ) {
        when (recorderState) {
            is RecorderState.Recording -> {
                Text(
                    text = recorderState.totalDuration,
                    modifier = Modifier.align(Alignment.CenterHorizontally),
                )
            }

            is RecorderState.Recorded -> {
                Text(
                    text = recorderState.totalDurationString,
                    modifier = Modifier.align(Alignment.CenterHorizontally),
                )
            }

            is RecorderState.Error -> {
                Text(
                    color = Color.Red,
                    text = recorderState.errorMessage,
                    modifier = Modifier.align(Alignment.CenterHorizontally),
                )
            }

            RecorderState.Idle -> {
                Text(
                    text = "Press to record.",
                    modifier = Modifier.align(Alignment.CenterHorizontally),
                )
            }
        }

        Image(
            imageVector = ImageVector.vectorResource(R.drawable.icon_microphone),
            colorFilter = ColorFilter.tint(Color.White),
            contentDescription = null,
            modifier = Modifier
                .windowInsetsPadding(WindowInsets.navigationBars)
                .align(Alignment.CenterHorizontally)
                .padding(10.dp)
                .clip(RoundedCornerShape(percent = 50))
                .background(Color(0xFF304FFE))
                .size(50.dp)
                .padding(10.dp)
                .pointerInput(Unit) {
                    awaitPointerEventScope {
                        while (true) {
                            val event = awaitPointerEvent()
                            when (event.type) {
                                PointerEventType.Press -> {
                                    currentOnStartRecording()
                                }

                                PointerEventType.Unknown,
                                PointerEventType.Release -> {
                                    currentOnStopRecording()
                                }
                            }
                        }
                    }
                }
        )
    }
}

private class NonWindowInsets : WindowInsets {
    override fun getBottom(density: Density) = 0

    override fun getLeft(density: Density, layoutDirection: LayoutDirection) = 0

    override fun getRight(density: Density, layoutDirection: LayoutDirection) = 0

    override fun getTop(density: Density) = 0
}