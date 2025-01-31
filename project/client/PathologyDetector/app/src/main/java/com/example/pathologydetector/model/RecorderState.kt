package com.example.pathologydetector.model

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import kotlinx.coroutines.flow.Flow

@Stable
sealed interface RecorderState {

    @Immutable
    data object Idle: RecorderState

    data class Error(
        val errorMessage: String,
    ) : RecorderState

    @Immutable
    data class Recording(
        val maxAmplitude: Int,
        val totalDuration: String,
    ): RecorderState

    @Immutable
    data class Recorded(
        val outputFile: String,
        val totalDuration: Long,
        val suggestedName: String,
        val totalDurationString: String,
        val playerState: Flow<PlayerState>,
    ): RecorderState
}
