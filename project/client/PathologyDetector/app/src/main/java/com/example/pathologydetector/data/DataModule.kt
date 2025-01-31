package com.example.pathologydetector.data

import android.content.Context
import androidx.room.Room
import com.example.pathologydetector.data.retrofit.AnalysisResult
import com.example.pathologydetector.data.retrofit.AnalysisResultAdapter
import com.example.pathologydetector.data.retrofit.PathologyDetectorApi
import com.example.pathologydetector.data.room.AudioDatabase
import com.example.pathologydetector.data.room.AudioRecordDao
import com.example.pathologydetector.data.room.RoomTypeConverts
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

var host: String? = null

@Module
@InstallIn(SingletonComponent::class)
class DataModule {
    @Provides
    fun providesDataBase(
        @ApplicationContext context: Context,
        roomTypeConverts: RoomTypeConverts,
    ): AudioDatabase = Room.databaseBuilder(
        context,
        AudioDatabase::class.java,
        AudioDatabase::class.java.simpleName,
    ).addTypeConverter(roomTypeConverts).build()

    @Provides
    fun providesAudioRecordDao(
        audioDatabase: AudioDatabase,
    ): AudioRecordDao = audioDatabase.audioRecordDao

    @Provides
    fun providesGson(): Gson = GsonBuilder()
        .setLenient()
        .registerTypeAdapter(AnalysisResult::class.java, AnalysisResultAdapter)
        .create()

    @Provides
    fun providesRetrofit(
        gson: Gson
    ): Retrofit = Retrofit.Builder()
        .baseUrl("http://192.168.1.193:5000/")
        .client(
            OkHttpClient.Builder()
                .addInterceptor { chain ->
                    val host = host ?: return@addInterceptor chain.proceed(chain.request())

                    val newRequest = chain.request()
                        .newBuilder()
                        .url(
                            chain.request().url
                                .newBuilder()
                                .host(host)
                                .build()
                        )
                        .build()
                    chain.proceed(newRequest)
                }
                .addInterceptor(
                    HttpLoggingInterceptor().apply {
                        level = HttpLoggingInterceptor.Level.BODY
                    }
                )
                .build()
        )
        .addConverterFactory(
            GsonConverterFactory.create(gson)
        )
        .build()

    @Provides
    fun providesApi(
        retrofit: Retrofit,
    ): PathologyDetectorApi = retrofit.create(PathologyDetectorApi::class.java)
}

