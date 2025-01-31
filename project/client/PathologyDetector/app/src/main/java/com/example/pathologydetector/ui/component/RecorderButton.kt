package com.example.pathologydetector.ui.component

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.example.pathologydetector.model.RecorderState

@Composable
fun  RecorderButton(
    recorderState: RecorderState,
    modifier: Modifier = Modifier,
) {
    val infiniteTransition = rememberInfiniteTransition("recorder_button")
    val degree = infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000),
            repeatMode = RepeatMode.Restart,
        ),
        label = "recorder_button_degree",
    )


}
