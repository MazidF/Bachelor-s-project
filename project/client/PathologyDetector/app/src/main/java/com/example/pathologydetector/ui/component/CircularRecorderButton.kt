package com.example.pathologydetector.ui.component

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp

const val MAX_AMPLITUDE = 32767f

@Composable
fun CircularRecorderButton(amplitude: Int) {
    // Normalize the amplitude to a value between 0.0 and 1.0
    val normalizedAmplitude = (amplitude / MAX_AMPLITUDE).coerceIn(0f, 1f)

    // Animate the button's properties based on the amplitude
    val animatedSize by animateFloatAsState(
        targetValue = 100f + (normalizedAmplitude * 50f), // Adjust size based on amplitude
        animationSpec = tween(durationMillis = 200) // Control animation duration
    )

    val animatedBorderWidth by animateFloatAsState(
        targetValue = 4f + (normalizedAmplitude * 8f), // Adjust border thickness
        animationSpec = tween(durationMillis = 200)
    )

    // Draw the circular button using Canvas
    Canvas(
        modifier = Modifier
            .size(animatedSize.dp)
            .padding(16.dp)
    ) {
        // Draw a circle with dynamic size and border width
        drawCircle(
            color = Color.Green,
            radius = size.minDimension / 2,
            style = Stroke(width = animatedBorderWidth)
        )
    }
}