package com.example.pathologydetector.data.room

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(
    version = 1,
    exportSchema = true,
    entities = [AudioRecord::class],
)
@TypeConverters(RoomTypeConverts::class)
abstract class AudioDatabase : RoomDatabase() {
    abstract val audioRecordDao: AudioRecordDao
}
