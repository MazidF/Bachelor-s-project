package com.example.pathologydetector.data.room

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface AudioRecordDao {
    @Insert
    suspend fun insertAudioRecord(audioRecord: AudioRecord): Long

    @Update(onConflict = OnConflictStrategy.REPLACE)
    suspend fun updateAudioRecord(audioRecord: AudioRecord)

    @Delete
    suspend fun deleteAudioRecord(audioRecord: AudioRecord)

    @Query("SELECT * FROM ${AudioRecord.TABLE_NAME} ORDER BY date DESC")
    fun getAudioRecords(): Flow<List<AudioRecord>>
}
