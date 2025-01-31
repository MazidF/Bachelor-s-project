package com.example.pathologydetector

import android.annotation.SuppressLint
import android.content.Context
import android.media.MediaMetadataRetriever
import android.media.MediaMetadataRetriever.METADATA_KEY_DURATION
import android.util.Log
import android.widget.Toast
import androidx.compose.runtime.Stable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import com.example.pathologydetector.data.retrofit.AnalysisResult
import com.example.pathologydetector.data.retrofit.PathologyDetectorApi
import com.example.pathologydetector.data.room.AudioRecord
import com.example.pathologydetector.data.room.AudioRecordDao
import com.example.pathologydetector.model.MainUiState
import com.example.pathologydetector.model.Model
import com.example.pathologydetector.model.PlayerState
import com.example.pathologydetector.model.RecorderState
import com.example.pathologydetector.model.UiAnalysisResult
import com.example.pathologydetector.model.UiAudioRecord
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.transformLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import javax.inject.Inject

@Stable
@HiltViewModel
@SuppressLint("DefaultLocale", "SimpleDateFormat")
class MainViewModel @Inject constructor(
    private val player: ExoPlayer,
    private val audioRecordDao: AudioRecordDao,
    audioRecorderFactory: AudioRecorder.Factory,
    @ApplicationContext private val context: Context,
    private val pathologyDetectorApi: PathologyDetectorApi,
) : ViewModel() {
    private val timeFormatter = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
    private val audioRecorder = audioRecorderFactory.create(viewModelScope)
    private val innerState = MutableStateFlow(InnerState())

    @OptIn(ExperimentalCoroutinesApi::class)
    private val playerState = callbackFlow {
        var currentState = PlayerInnerState()

        fun updateState(block: (PlayerInnerState) -> PlayerInnerState) {
            currentState = block(currentState)
            trySend(currentState)
        }

        val listener = object : Player.Listener {
            override fun onIsPlayingChanged(isPlaying: Boolean) {
                updateState { state ->
                    state.copy(isPlaying = isPlaying)
                }
            }

            override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
                updateState { state ->
                    state.copy(currentMediaItem = mediaItem)
                }
            }

            override fun onPlayerError(error: PlaybackException) {
                player.prepare()
                player.playWhenReady = true
                player.seekTo(player.currentPosition)
            }

            override fun onPlaybackStateChanged(playbackState: Int) {
                if (playbackState == Player.STATE_ENDED) {
                    player.playWhenReady = false
                    player.seekToDefaultPosition()
                }
            }
        }

        player.addListener(listener)

        awaitClose {
            player.removeListener(listener)
        }
    }.distinctUntilChanged { oldState, newState ->
        oldState.isPlaying == newState.isPlaying
                && oldState.currentMediaItem?.mediaId == newState.currentMediaItem?.mediaId
    }.transformLatest { (isPlaying, mediaItem) ->
        if (mediaItem == null) return@transformLatest emit(PlayerState())

        fun getProgress(): Float {
            return (player.currentPosition.toFloat() / player.duration).coerceAtLeast(0f)
        }

        if (isPlaying) {
            while (currentCoroutineContext().isActive) {
                emit(
                    PlayerState(
                        isPlaying = true,
                        progress = getProgress(),
                        mediaId = mediaItem.mediaId,
                    )
                )
                delay(100)
            }
        } else {
            emit(
                PlayerState(
                    isPlaying = false,
                    progress = getProgress(),
                    mediaId = mediaItem.mediaId,
                )
            )
        }
    }.stateIn(
        scope = viewModelScope,
        initialValue = PlayerState(),
        started = SharingStarted.Eagerly,
    )

    private val recorderState = callbackFlow {
        val listener = object : AudioRecorder.Listener {
            override fun onClear() {
                trySend(RecorderState.Idle)
            }

            override fun onRecording(maxAmplitude: Int, totalDuration: Long) {
                trySend(RecorderState.Recording(maxAmplitude, totalDuration.formatDuration()))
            }

            override fun onStopRecording(canceled: Boolean, outputFile: String, totalDuration: Long) {
                if (canceled || totalDuration < MIN_REQUIRED_DURATION_IN_MILLIS) {
                    File(outputFile).delete()
                    trySend(RecorderState.Error("Please record a longer input!!"))
                } else {
                    trySend(
                        RecorderState.Recorded(
                            outputFile = outputFile,
                            totalDuration = totalDuration,
                            totalDurationString = totalDuration.formatDuration(),
                            suggestedName = "Voice ${System.currentTimeMillis()}",
                            playerState = playerState.filter { state -> state.mediaId == outputFile },
                        )
                    )
                }
            }
        }
        audioRecorder.listener = listener

        awaitClose {
            audioRecorder.listener = null
        }
    }.combine(innerState) { recorderStat, innerState ->
        if (innerState.currentRecordingAudioFile == null) {
            RecorderState.Idle
        } else {
            recorderStat
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = RecorderState.Idle,
    )

    val uiState = combine(
        innerState,
        playerState,
        recorderState,
        audioRecordDao.getAudioRecords(),
    ) { innerState, playerState, recorderState, records ->
        MainUiState(
            playerState = playerState,
            recorderState = recorderState,
            isPermissionGranted = innerState.permissionIsGranted,
            audioRecords = records.mapToUI(innerState.processingItems),
        )
    }.stateIn(
        scope = viewModelScope,
        initialValue = MainUiState(),
        started = SharingStarted.Eagerly,
    )

    fun permissionGranted() {
        innerState.update { state ->
            state.copy(permissionIsGranted = true)
        }
    }

    fun startRecording(context: Context) {
        val file = context.filesDir.resolve("${System.currentTimeMillis()}.wav")
        innerState.update { state ->
            state.copy(currentRecordingAudioFile = file.absolutePath)
        }
        audioRecorder.start(file.absolutePath, 100L)
    }

    fun stopRecording() {
        audioRecorder.stop(canceled = false)
    }

    fun cancelRecording(filePath: String) {
        innerState.update { state ->
            if (state.currentRecordingAudioFile == filePath) {
                state.copy(currentRecordingAudioFile = null)
            } else state
        }
        audioRecorder.stop(canceled = true)
    }

    fun saveRecording(fileName: String, filePath: String, duration: Long? = null) {
        innerState.update { state ->
            if (state.currentRecordingAudioFile == filePath) {
                state.copy(currentRecordingAudioFile = null)
            } else state
        }
        audioRecorder.reset()
        insertRecordedAudio(fileName, filePath, duration)
    }

    fun deleteRecord(audioRecord: AudioRecord) = viewModelScope.launch(Dispatchers.IO) {
        audioRecordDao.deleteAudioRecord(audioRecord)
    }

    fun requestProcess(audioRecord: AudioRecord, model: Model) {
        if (canBeProcessed(audioRecord).not()) return

        innerState.update { state ->
            if (state.processingItems.contains(audioRecord.id)) {
                state
            } else {
                val newItem = audioRecord.id to (requestApi(audioRecord, model) to model)
                state.copy(
                    processingItems = state.processingItems + newItem,
                )
            }
        }
    }

    fun cancelProcess(audioRecord: AudioRecord) {
        if (canBeProcessed(audioRecord).not()) return

        innerState.update { state ->
            state.processingItems[audioRecord.id]?.first?.cancel()
            state.copy(
                processingItems = state.processingItems - audioRecord.id
            )
        }
    }

    private fun canBeProcessed(audioRecord: AudioRecord): Boolean {
        return audioRecord.status == AnalysisResult.NotProcessed
                || audioRecord.status is AnalysisResult.Error
    }

    private fun requestApi(audioRecord: AudioRecord, model: Model) = viewModelScope.launch(Dispatchers.IO) {
        val file = File(audioRecord.filePath)
        val requestFile = RequestBody.create(MultipartBody.FORM, file)
        val modelPart = MultipartBody.Part.createFormData("model", model.modelName)
        val body = MultipartBody.Part.createFormData("audio", audioRecord.fileName, requestFile)

        while (isActive) {
            try {
                val result = pathologyDetectorApi.postAudioForAnalysis(body, modelPart)
                delay(1000)
                if (result.isSuccessful) {
                    audioRecordDao.updateAudioRecord(
                        audioRecord.copy(status = result.body()!!),
                    )
                    break
                } else if (result.isBadRequest) {
                    audioRecordDao.updateAudioRecord(
                        audioRecord.copy(status = result.body()!!),
                    )
                    break
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: IOException) {
                Log.e(TAG, "requestApi", e)
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "An error occurred, please try later.", Toast.LENGTH_SHORT).show()
                }
                Log.e(TAG, "requestApi", e)
                break
            }
            delay(5_000)
        }

        innerState.update { state ->
            state.copy(
                processingItems = state.processingItems - audioRecord.id,
            )
        }
    }

    private inline val <T> Response<T>.isBadRequest
        get() = code() in 400 until 500

    private fun insertRecordedAudio(
        fileName: String,
        filePath: String,
        duration: Long?,
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            val duration = duration ?: MediaMetadataRetriever().run {
                setDataSource(filePath)
                extractMetadata(METADATA_KEY_DURATION)!!.toLong().also {
                    release()
                }
            }
            val audioRecord = AudioRecord(
                filePath = filePath,
                fileName = fileName,
                date = System.currentTimeMillis(),
                duration = duration,
            )
            audioRecordDao.insertAudioRecord(audioRecord)
        }
    }

    private fun List<AudioRecord>.mapToUI(
        processingItems: Map<Long, Pair<Job, Model>>,
    ): List<UiAudioRecord> = map { item ->
        UiAudioRecord(
            id = item.id,
            audioRecord = item,
            fileName = item.fileName,
            totalDuration = item.duration,
            date = timeFormatter.format(item.date),
            duration = item.duration.formatDuration(),
            status = item.status.asUi(processingItems[item.id]?.second),
        )
    }

    private fun AnalysisResult.asUi(
        processingModel: Model?,
    ): UiAnalysisResult = when (this) {
        is AnalysisResult.Healthy -> UiAnalysisResult.Healthy(model)
        is AnalysisResult.Pathology -> UiAnalysisResult.Pathology(model)
        is AnalysisResult.Error -> UiAnalysisResult.Error(message, model)
        is AnalysisResult.NotProcessed -> if (processingModel != null) {
            UiAnalysisResult.Processing(processingModel)
        } else {
            UiAnalysisResult.NotProcessed
        }
    }

    private fun Float.formatConfidence(): String {
        val percentage = this * 100
        return String.format("%.2f%%", percentage)
    }

    private fun Long.formatDuration(): String {
        val minutes = (this / (1000 * 60)) % 60
        val seconds = (this / 1000) % 60
        val milliSeconds = this % 1000

        return String.format("%02d:%02d.%d", minutes, seconds, milliSeconds / 100)
    }

    override fun onCleared() {
        audioRecorder.release()
        super.onCleared()
    }

    fun onSeekTo(outputFile: String, position: Long) {
        if (player.currentMediaItem?.mediaId != outputFile) {
            player.playWhenReady = true
            player.prepare(outputFile)
        }
        player.seekTo(position)
    }

    fun onTogglePlayer(outputFile: String) {
        if (player.currentMediaItem?.mediaId != outputFile) {
            player.playWhenReady = true
            player.prepare(outputFile)
        } else {
            player.playWhenReady = player.playWhenReady.not()
        }
    }

    private fun ExoPlayer.prepare(filePath: String) {
        setMediaItem(MediaItem.Builder().setMediaId(filePath).setUri(filePath).build())
        prepare()
    }


    private data class InnerState(
        val permissionIsGranted: Boolean = false,
        val processingItems: Map<Long, Pair<Job, Model>> = emptyMap(),
        val currentRecordingAudioFile: String? = null,
    )

    private data class PlayerInnerState(
        val isPlaying: Boolean = false,
        val currentMediaItem: MediaItem? = null,
    )

    private companion object {
        const val TAG = "MainViewModel"
        const val MIN_REQUIRED_DURATION_IN_MILLIS = 2000L
    }
}
