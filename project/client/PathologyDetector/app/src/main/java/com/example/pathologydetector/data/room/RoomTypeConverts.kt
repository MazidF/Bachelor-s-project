package com.example.pathologydetector.data.room

import android.util.Log
import androidx.room.ProvidedTypeConverter
import androidx.room.TypeConverter
import com.example.pathologydetector.data.retrofit.AnalysisResult
import com.google.gson.Gson
import javax.inject.Inject

@ProvidedTypeConverter
class RoomTypeConverts @Inject constructor(
    private val gson: Gson
) {
    @TypeConverter
    fun analysisResultToString(analysisResult: AnalysisResult): String {
        return gson.toJson(analysisResult)
    }

    @TypeConverter
    fun analysisResultFromString(json: String): AnalysisResult {
        return gson.fromJson(json, AnalysisResult::class.java)
    }
}