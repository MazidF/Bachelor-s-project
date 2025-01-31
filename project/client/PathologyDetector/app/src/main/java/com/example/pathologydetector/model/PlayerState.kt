package com.example.pathologydetector.model

data class PlayerState(
    val progress: Float = 0f,
    val mediaId: String? = null,
    val isPlaying: Boolean = false,
)
