package com.example.pathologydetector.model

import androidx.compose.runtime.Immutable
import com.example.pathologydetector.data.room.AudioRecord

@Immutable
data class UiAudioRecord(
    val id: Long,
    val date: String,
    val duration: String,
    val fileName: String,
    val totalDuration: Long,
    val audioRecord: AudioRecord,
    val status: UiAnalysisResult,
)
