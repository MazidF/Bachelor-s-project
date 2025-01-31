package com.example.pathologydetector.data.room

import androidx.compose.runtime.Immutable
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.pathologydetector.data.retrofit.AnalysisResult

@Immutable
@Entity(tableName = AudioRecord.TABLE_NAME)
data class AudioRecord(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val date: Long,
    val duration: Long,
    val fileName: String,
    val filePath: String,
    val status: AnalysisResult = AnalysisResult.NotProcessed,
) {
    companion object {
        const val TABLE_NAME = "audio_record"
    }
}
