package com.example.pathologydetector.model

import androidx.compose.runtime.Immutable

@Immutable
data class MainUiState(
    val playerState: PlayerState? = null,
    val isPermissionGranted: Boolean = false,
    val audioRecords: List<UiAudioRecord> = emptyList(),
    val recorderState: RecorderState = RecorderState.Idle,
)
