package com.example.pathologydetector.data.retrofit

import okhttp3.MultipartBody
import retrofit2.Response
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part

interface PathologyDetectorApi {

    @Multipart
    @POST("/upload")
    suspend fun postAudioForAnalysis(
        @Part audio: MultipartBody.Part,
        @Part model: MultipartBody.Part,
    ): Response<AnalysisResult>
}
