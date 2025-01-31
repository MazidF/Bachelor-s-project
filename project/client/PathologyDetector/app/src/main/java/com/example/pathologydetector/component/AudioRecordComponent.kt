package com.example.pathologydetector.component

import android.content.Context
import android.content.Context.VIBRATOR_SERVICE
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.AnchoredDraggableState
import androidx.compose.foundation.gestures.DraggableAnchors
import androidx.compose.foundation.gestures.animateTo
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.util.VelocityTracker
import androidx.compose.ui.input.pointer.util.addPointerInputChange
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension
import com.example.pathologydetector.R
import com.example.pathologydetector.data.room.AudioRecord
import com.example.pathologydetector.model.Model
import com.example.pathologydetector.model.PlayerState
import com.example.pathologydetector.model.UiAnalysisResult
import com.example.pathologydetector.model.UiAudioRecord
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.absoluteValue
import kotlin.math.roundToInt

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun AudioRecordComponent(
    playerState: PlayerState?,
    audioRecord: UiAudioRecord,
    onDeleteRecord: () -> Unit,
    onTogglePlaying: () -> Unit,
    onProcessRecord: () -> Unit,
    onSeekTo: (Float) -> Unit,
    onCancelProcessRecord: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val currentOnDeleteRecord by rememberUpdatedState(onDeleteRecord)
    val currentOnTogglePlaying by rememberUpdatedState(onTogglePlaying)

    var isGoingToBeDeleted by remember {
        mutableStateOf(false)
    }
    var layoutSize by remember {
        mutableStateOf(IntSize.Zero)
    }

    val density = LocalDensity.current
    val context = LocalContext.current

    val state = remember {
        AnchoredDraggableState(
            animationSpec = tween(),
            positionalThreshold = { it },
            initialValue = DragAnchor.START,
            velocityThreshold = { Float.MAX_VALUE },
        ).apply {
            val newAnchors = DraggableAnchors {
                DragAnchor.START at 0f
                DragAnchor.END at 0f
            }
            updateAnchors(newAnchors)
        }
    }

    LaunchedEffect(layoutSize) {
        if (layoutSize.width <= 0) return@LaunchedEffect

        val newAnchors = with(density) {
            DraggableAnchors {
                DragAnchor.START at 0.dp.toPx()
                DragAnchor.END at -layoutSize.width.toFloat()
            }
        }
        state.updateAnchors(newAnchors)
    }

    Box(
        modifier = Modifier.fillMaxWidth(),
    ) {
        val offset = state.requireOffset()
        if (offset < 0f) {
            val backgroundColor by animateColorAsState(
                if (isGoingToBeDeleted) Color.Gray else Color.Red,
                label = "backgroundColorAnimation",
            )

            Box(
                modifier = Modifier
                    .height(
                        with(density) {
                            layoutSize.height.toDp()
                        }
                    )
                    .fillMaxWidth()
                    .background(backgroundColor),
                contentAlignment = Alignment.CenterEnd,
            ) {
                Image(
                    imageVector = ImageVector.vectorResource(R.drawable.icon_delete),
                    colorFilter = ColorFilter.tint(Color.White),
                    modifier = Modifier
                        .padding(end = 10.dp)
                        .size(30.dp)
                        .graphicsLayer {
                            val scale = (offset.absoluteValue / 110.dp.toPx()).coerceAtMost(1f)
                            scaleX = scale
                            scaleY = scale

                            if (scale == 1f) {
                                if (isGoingToBeDeleted.not()) {
                                    isGoingToBeDeleted = true
                                    vibrate(context)
                                }
                            } else if (isGoingToBeDeleted) {
                                isGoingToBeDeleted = false
                                vibrate(context)
                            }
                        },
                    contentDescription = null,
                )
            }
        }

        ConstraintLayout(
            modifier = modifier
                .fillMaxWidth()
                .onGloballyPositioned { layoutCoordinates ->
                    layoutSize = layoutCoordinates.size
                }
                .offset {
                    IntOffset(
                        state
                            .requireOffset()
                            .roundToInt(), 0
                    )
                }
                .padding(vertical = 6.dp, horizontal = 14.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(Color(0xFFA7D9F3))
                .padding(vertical = 10.dp)
                .padding(start = 10.dp, end = 14.dp)
                .pointerInput(Unit) {
                    val velocityTracker = VelocityTracker()
                    coroutineScope {
                        detectHorizontalDragGestures(
                            onDragEnd = {
                                if (isGoingToBeDeleted) {
                                    launch {
                                        state.animateTo(DragAnchor.END)
                                        currentOnDeleteRecord()
                                    }
                                } else {
                                    launch {
                                        state.settle(velocityTracker.calculateVelocity().x)
                                        velocityTracker.resetTracking()
                                    }
                                }
                            }
                        ) { change, dragAmount ->
                            velocityTracker.addPointerInputChange(change)
                            state.dispatchRawDelta(dragAmount)
                            change.consume()
                        }
                    }
                },
        ) {
            val (model, loading, title, duration, date, result, slider, toggle) = createRefs()

            val modelState = audioRecord.status.model
            Text(
                text = modelState.modelName,
                color = Color.White,
                modifier = Modifier
                    .constrainAs(model) {
                        top.linkTo(parent.top)
                        absoluteLeft.linkTo(parent.absoluteLeft)
                    }
                    .background(
                        color = modelState.getColor(),
                        shape = RoundedCornerShape(4.dp),
                    )
                    .clickable {
                        if (
                            audioRecord.status is UiAnalysisResult.NotProcessed
                            || audioRecord.status is UiAnalysisResult.Error
                        ) {
                            onProcessRecord()
                        }
                    }
                    .padding(4.dp)
            )

            if (audioRecord.status is UiAnalysisResult.Processing) {
                CircularProgressIndicator(
                    modifier = Modifier
                        .padding(start = 10.dp)
                        .size(20.dp)
                        .clickable {
                            onCancelProcessRecord()
                        }
                        .constrainAs(loading) {
                            top.linkTo(model.top)
                            bottom.linkTo(model.bottom)
                            absoluteLeft.linkTo(model.absoluteRight)
                        },
                    color = modelState.getColor(),
                    trackColor = MaterialTheme.colorScheme.surfaceVariant,
                )
            }

            val textStyle = LocalTextStyle.current.copy(lineHeightStyle = null)
            Text(
                fontSize = 16.sp,
                style = textStyle,
                text = audioRecord.fileName,
                modifier = Modifier
                    .padding(top = 10.dp)
                    .constrainAs(title) {
                        top.linkTo(model.bottom)
                        width = Dimension.fillToConstraints
                        absoluteLeft.linkTo(parent.absoluteLeft)
                        absoluteRight.linkTo(toggle.absoluteLeft)
                        bottom.linkTo(duration.top)
                    }
            )

            val resultStatus = audioRecord.status.stateMessage
            Text(
                fontSize = 12.sp,
                style = textStyle,
                text = audioRecord.duration,
                modifier = Modifier.constrainAs(duration) {
                    top.linkTo(title.bottom)
                    width = Dimension.fillToConstraints
                    absoluteLeft.linkTo(parent.absoluteLeft)
                    absoluteRight.linkTo(toggle.absoluteLeft)
                }
            )

            IconButton(
                onClick = currentOnTogglePlaying,
                modifier = Modifier.constrainAs(toggle) {
                    top.linkTo(date.bottom)
                    bottom.linkTo(slider.top)
                    absoluteRight.linkTo(parent.absoluteRight)
                }
            ) {
                Icon(
                    imageVector = if (playerState?.isPlaying == true) {
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

            AnimatedVisibility(
                visible = playerState != null,
                modifier = Modifier.constrainAs(slider) {
                    top.linkTo(duration.bottom)
                    absoluteLeft.linkTo(parent.absoluteLeft)
                    absoluteRight.linkTo(parent.absoluteRight)
                }
            ) {
                Slider(
                    value = playerState?.progress ?: 0f,
                    onValueChange = onSeekTo,
                    colors = SliderDefaults.colors(
                        thumbColor = Color(0xFF304FFE),
                        activeTrackColor = Color(0xFF304FFE),
                    ),
                    modifier = Modifier.fillMaxWidth(),
                )
            }

            Text(
                fontSize = 12.sp,
                style = textStyle,
                text = audioRecord.date,
                modifier = Modifier.constrainAs(date) {
                    top.linkTo(model.top)
                    bottom.linkTo(model.bottom)
                    absoluteRight.linkTo(parent.absoluteRight)
                }
            )

            if (resultStatus != null) {
                Row(
                    modifier = Modifier
                        .padding(4.dp)
                        .fillMaxWidth()
                        .constrainAs(result) {
                            bottom.linkTo(parent.bottom)
                            top.linkTo(if (playerState != null) slider.bottom else duration.bottom)
                        },
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                ) {
                    Image(
                        imageVector = ImageVector.vectorResource(resultStatus.second),
                        colorFilter = ColorFilter.tint(audioRecord.status.color),
                        contentDescription = null,
                    )

                    Spacer(modifier = Modifier.width(4.dp))

                    Text(text = resultStatus.first, color = audioRecord.status.color)
                }
            }
        }
    }
}

fun vibrate(context: Context, durationMillis: Long = 60) {
    val vibrator = (context.getSystemService(VIBRATOR_SERVICE) as Vibrator)
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        vibrator.vibrate(
            VibrationEffect.createOneShot(
                durationMillis,
                VibrationEffect.EFFECT_DOUBLE_CLICK,
            )
        )
    } else {
        vibrator.vibrate(durationMillis)
    }
}

@Preview(showBackground = true)
@Composable
fun AudioRecordComponentPreview() {
    AudioRecordComponent(
        playerState = PlayerState(
            progress = 0.35f,
            isPlaying = true,
        ).takeIf { true },
        audioRecord = UiAudioRecord(
            id = 0,
            date = "10 Mar 6:02 pm",
            duration = "00:01:18",
            fileName = "Voice 005",
            status = UiAnalysisResult.Healthy(Model.ONE),
            audioRecord = AudioRecord(
                date = 0,
                duration = 0,
                filePath = "",
                fileName = "",
            ),
            totalDuration = 0,
        ),
        onTogglePlaying = { },
        onDeleteRecord = { },
        onProcessRecord = { },
        onSeekTo = { },
        onCancelProcessRecord = { }
    )
}

enum class DragAnchor {
    START, END;
}