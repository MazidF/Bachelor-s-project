package com.example.pathologydetector

import android.annotation.SuppressLint
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaMetadataRetriever
import android.media.MediaMetadataRetriever.METADATA_KEY_DURATION
import android.media.MediaRecorder
import android.util.Log
import androidx.annotation.OptIn
import androidx.media3.common.util.UnstableApi
import androidx.media3.extractor.OpusUtil
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.launch
import java.io.File
import java.io.IOException
import java.io.RandomAccessFile
import java.nio.ByteBuffer
import java.nio.ByteOrder


@Suppress("DEPRECATION")
class AudioRecorder @AssistedInject constructor(
    @Assisted private val coroutineScope: CoroutineScope,
) {
    var listener: Listener? = null

    private var startTime: Long = -1
    private var recorderJob: Job? = null
    private lateinit var outputFile: String
    private var audioRecord: AudioRecord? = null

    @OptIn(UnstableApi::class)
    @SuppressLint("MissingPermission")
    fun start(
        outputFile: String,
        sampleTimeInMillis: Long,
    ) {
        if (recorderJob != null) return
        this.outputFile = outputFile

        val audioRecord = AudioRecord(
            MediaRecorder.AudioSource.MIC,
            OpusUtil.SAMPLE_RATE,
            CHANNEL_CONFIG,
            AUDIO_FORMAT,
            BUFFER_SIZE
        ).also {
            audioRecord = it
        }

        recorderJob = coroutineScope.launch(Dispatchers.IO) {
            audioRecord.startRecording()
            startTime = System.currentTimeMillis()
            writeAudioRecorderOutput(outputFile, audioRecord)

            Log.d("mazid", "before loop: ${audioRecord.recordingState}")
            while (audioRecord.recordingState == AudioRecord.RECORDSTATE_RECORDING) {
                listener?.onRecording(
                    maxAmplitude = 0,
                    totalDuration = System.currentTimeMillis() - startTime,
                )
                delay(sampleTimeInMillis)
                Log.d("mazid", "in the loop: ${audioRecord.recordingState}")
            }
            Log.d("mazid", "after loop: ${audioRecord.recordingState}")
        }.also { job ->
            job.invokeOnCompletion {
                if (recorderJob === job) {
                    recorderJob = null
                }
            }
        }
    }

    private fun CoroutineScope.writeAudioRecorderOutput(
        outputFile: String,
        audioRecord: AudioRecord,
    ) = launch {
        val file = File(outputFile)
        val buffer = ByteArray(BUFFER_SIZE)
        try {
            RandomAccessFile(file, "rw").use { fos ->
                // Write WAV header placeholder (we'll fix it later)
                fos.write(ByteArray(44))

                while (audioRecord.recordingState == AudioRecord.RECORDSTATE_RECORDING) {
                    ensureActive()
                    val bytesRead = audioRecord.read(buffer, 0, buffer.size)
                    Log.d("mazid_r", "readbyte: $bytesRead")
                    if (bytesRead > 0) {
                        fos.write(buffer, 0, bytesRead)
                    }
                }

                ensureActive()
                fos.fd.sync()

                // Finalize WAV file (add correct header)
                val header = createWavHeader(audioRecord, fos.length().toInt())
                fos.seek(0)
                fos.write(header)
            }

            listener?.onStopRecording(canceled = false, outputFile, getDuration(outputFile))
        } catch (e: IOException) {
            Log.e(TAG, e.message, e)
        }
    }

    private fun getDuration(filePath: String): Long {
        val retriever = MediaMetadataRetriever()
        try {
            retriever.setDataSource(filePath)
            val durationStr = retriever.extractMetadata(METADATA_KEY_DURATION)
            return durationStr!!.toLong()
        } catch (_: Exception) {
            return -1 // Return -1 in case of error
        } finally {
            retriever.release()
        }
    }

    fun stop(canceled: Boolean) {
        val recorderJob = recorderJob ?: return reset()

        if (canceled) {
            recorderJob.cancel()
        }

        try {
            audioRecord?.stop()
        } catch (_: Exception) {}
        try {
            audioRecord?.release()
        } catch (_: Exception) {}
        audioRecord = null
    }

    fun reset() {
        listener?.onClear()
    }

    fun release() {
        stop(canceled = true)
    }

    // Function to create a basic WAV file header
    private fun createWavHeader(audioRecord: AudioRecord, dataSize: Int): ByteArray {
        val header = ByteBuffer.allocate(44)
        header.order(ByteOrder.LITTLE_ENDIAN)

        // RIFF header
        header.put("RIFF".toByteArray())
        header.putInt(36 + dataSize)  // ChunkSize (file size - 8)
        header.put("WAVE".toByteArray())

        // fmt chunk
        header.put("fmt ".toByteArray())
        header.putInt(16)  // SubChunk1Size
        header.putShort(1)  // AudioFormat (PCM)
        header.putShort(audioRecord.channelCount.toShort())  // NumChannels (Mono)
        header.putInt(audioRecord.sampleRate)  // SampleRate (example 16kHz)
        header.putInt(audioRecord.sampleRate * audioRecord.channelCount * 2)  // ByteRate (SampleRate * NumChannels * BitsPerSample/8)
        header.putShort((audioRecord.channelCount * 2).toShort())  // BlockAlign (NumChannels * BitsPerSample/8)
        header.putShort(16)  // BitsPerSample

        // data chunk
        header.put("data".toByteArray())
        header.putInt(dataSize)  // DataSize (audio data size)

        return header.array()
    }

    interface Listener {
        fun onClear()
        fun onRecording(maxAmplitude: Int, totalDuration: Long) {}
        fun onStopRecording(canceled: Boolean, outputFile: String, totalDuration: Long) {}
    }

    @AssistedFactory
    interface Factory {
        fun create(
            coroutineScope: CoroutineScope,
        ): AudioRecorder
    }

    private companion object {
        const val TAG = "AudioRecorder"

        const val SAMPLE_RATE: Int = 44100 // 44.1kHz sample rate
        const val CHANNEL_CONFIG: Int = AudioFormat.CHANNEL_IN_MONO
        const val AUDIO_FORMAT: Int = AudioFormat.ENCODING_PCM_16BIT
        val BUFFER_SIZE: Int = AudioRecord.getMinBufferSize(SAMPLE_RATE, CHANNEL_CONFIG, AUDIO_FORMAT)
    }
}
