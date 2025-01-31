package com.example.pathologydetector

import android.Manifest
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.FabPosition
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.lifecycleScope
import com.example.pathologydetector.component.AudioRecordComponent
import com.example.pathologydetector.component.FloatingButton
import com.example.pathologydetector.component.HostChangerDialog
import com.example.pathologydetector.component.ModelSelectorDialog
import com.example.pathologydetector.component.VoiceRecorderBottomSheet
import com.example.pathologydetector.component.VoiceSaverDialog
import com.example.pathologydetector.data.room.AudioRecord
import com.example.pathologydetector.model.Model
import com.example.pathologydetector.model.PlayerState
import com.example.pathologydetector.model.RecorderState
import com.example.pathologydetector.model.UiAudioRecord
import com.example.pathologydetector.ui.theme.PathologyDetectorTheme
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.PermissionState
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import kotlin.math.roundToLong

@OptIn(ExperimentalPermissionsApi::class)
@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private val viewModel by viewModels<MainViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()

        setContent {
            val recordAudioPermission = rememberPermissionState(
                permission = Manifest.permission.RECORD_AUDIO,
                onPermissionResult = { isGranted ->
                    if (isGranted) {
                        viewModel.permissionGranted()
                    }
                }
            ).also { permissionState ->
                if (permissionState.status.isGranted) {
                    viewModel.permissionGranted()
                }
            }

            PathologyDetectorTheme {
                MainScreen(
                    recordAudioPermission = recordAudioPermission,
                    onSaveRecording = { fileName, filePath, duration ->
                        viewModel.saveRecording(fileName, filePath, duration)
                    },
                    onDeleteRecord = { audioRecord ->
                        viewModel.deleteRecord(audioRecord)
                    },
                    onTogglePlaying = { audioRecord ->
                        viewModel.onTogglePlayer(audioRecord.filePath)
                    },
                    onProcessRecord = { audioRecord, model ->
                        viewModel.requestProcess(audioRecord, model)
                    },
                    onCancelProcessRecord = { audioRecord ->
                        viewModel.cancelProcess(audioRecord)
                    },
                )
            }
        }
    }

    @Composable
    private fun MainScreen(
        onDeleteRecord: (AudioRecord) -> Unit,
        onTogglePlaying: (AudioRecord) -> Unit,
        onCancelProcessRecord: (AudioRecord) -> Unit,
        onProcessRecord: (AudioRecord, Model) -> Unit,
        onSaveRecording: (String, String, Long?) -> Unit,
        recordAudioPermission: PermissionState,
    ) {
        val context = LocalContext.current

        val uiState by viewModel.uiState.collectAsState()
        val listState = rememberLazyListState()
        var isHostChangerEnabled by remember {
            mutableStateOf(false)
        }

        val coroutineScope = rememberCoroutineScope()
        val pickAudioLauncher = rememberLauncherForActivityResult(
            ActivityResultContracts.GetContent()
        ) { pickedUri ->
            val uri = pickedUri ?: return@rememberLauncherForActivityResult
            coroutineScope.launch(Dispatchers.IO) {
                val newFile = contentResolver.openInputStream(uri)?.use { inputStream ->
                    val newFile = File(filesDir, "${System.currentTimeMillis()}.waw").also {
                        it.createNewFile()
                    }
                    FileOutputStream(newFile).use { outputStream ->
                        inputStream.copyTo(outputStream)
                    }
                    newFile
                } ?: return@launch

                onSaveRecording(newFile.nameWithoutExtension, newFile.absolutePath, null)
            }

            lifecycleScope.launchWhenResumed {
                delay(500)
                listState.scrollToItem(0)
            }
        }

        var isRecorderBottomSheetVisible by remember { mutableStateOf(false) }
        if (isRecorderBottomSheetVisible) {
            VoiceRecorderBottomSheet(
                recorderState = uiState.recorderState,
                onStopRecording = {
                    viewModel.stopRecording()
                },
                onStartRecording = {
                    viewModel.startRecording(context)
                },
                onDismissRequest = {
                    isRecorderBottomSheetVisible = false
                },
            )
        }

        val recorderState = uiState.recorderState
        if (recorderState is RecorderState.Recorded) {
            val playerState by recorderState.playerState.collectAsState(PlayerState())

            VoiceSaverDialog(
                playerState = playerState,
                suggestedName = recorderState.suggestedName,
                totalDuration = recorderState.totalDurationString,
                onDismissRequest = {
                    viewModel.cancelRecording(recorderState.outputFile)
                },
                onSaveRecordingClicked = { fileName ->
                    viewModel.saveRecording(
                        fileName,
                        recorderState.outputFile,
                        recorderState.totalDuration,
                    )
                    coroutineScope.launch {
                        delay(300)
                        listState.animateScrollToItem(0)
                    }
                },
                onSeekTo = { progress ->
                    viewModel.onSeekTo(
                        recorderState.outputFile,
                        (progress * recorderState.totalDuration).roundToLong()
                    )
                },
                onTogglePlayer = {
                    viewModel.onTogglePlayer(recorderState.outputFile)
                },
            )
        }


        var showHostChanger by remember {
            mutableStateOf(false)
        }

        Scaffold(
            modifier = Modifier.fillMaxSize(),
            topBar = {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFF304FFE))
                        .multipleTapDetector(4) {
                            showHostChanger = true
                        }
                        .windowInsetsPadding(WindowInsets.statusBars)
                        .padding(top = 10.dp)
                        .padding(vertical = 6.dp, horizontal = 10.dp),
                ) {
                    Text(
                        fontSize = 20.sp,
                        color = Color.White,
                        text = "Voice Pathology Detector",
                    )
                }
            },
            floatingActionButton = {
                FloatingButton(
                    isPermissionGranted = uiState.isPermissionGranted,
                    onRecordVoiceClicked = {
                        if (recordAudioPermission.status.isGranted) {
                            isRecorderBottomSheetVisible = true
                        } else {
                            recordAudioPermission.launchPermissionRequest()
                        }
                    },
                    onChooseVoiceClicked = { pickAudioLauncher.launch("audio/*") },
                )
            },
            floatingActionButtonPosition = FabPosition.End,
            contentWindowInsets = WindowInsets.navigationBars,
        ) { padding ->
            Column(
                modifier = Modifier
                    .padding(padding)
                    .fillMaxSize()
                    .background(Color.White)
                    .pointerInput(Unit) {
                        detectTapGestures(onDoubleTap = {
                            isHostChangerEnabled = !isHostChangerEnabled
                        })
                    },
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                AudioRecords(
                    listState = listState,
                    modifier = Modifier.weight(1f),
                    audioRecords = uiState.audioRecords,
                    onTogglePlaying = onTogglePlaying,
                    onProcessRecord = onProcessRecord,
                    onDeleteRecord = onDeleteRecord,
                    onCancelProcessRecord = onCancelProcessRecord,
                )
            }
        }

        if (showHostChanger) {
            HostChangerDialog {
                showHostChanger = false
            }
        }
    }

    @OptIn(ExperimentalFoundationApi::class)
    @Composable
    private fun AudioRecords(
        listState: LazyListState,
        audioRecords: List<UiAudioRecord>,
        onDeleteRecord: (AudioRecord) -> Unit,
        onTogglePlaying: (AudioRecord) -> Unit,
        onCancelProcessRecord: (AudioRecord) -> Unit,
        onProcessRecord: (AudioRecord, Model) -> Unit,
        modifier: Modifier = Modifier,
    ) {
        var showModelSelectorForItem by remember {
            mutableStateOf<AudioRecord?>(null)
        }

        LazyColumn(
            state = listState,
            modifier = modifier
                .wrapContentHeight(align = Alignment.Top)
                .background(Color.White),
            contentPadding = PaddingValues(vertical = 10.dp),
        ) {
            items(
                items = audioRecords,
                key = UiAudioRecord::id,
            ) { item ->
                val playerState by viewModel.uiState.mapLatest { state ->
                    state.playerState?.takeIf {
                        it.mediaId == item.audioRecord.filePath
                    }
                }.collectAsState(null)

                AudioRecordComponent(
                    playerState = playerState,
                    audioRecord = item,
                    onSeekTo = { progress ->
                        viewModel.onSeekTo(
                            item.audioRecord.filePath,
                            (progress * item.totalDuration).roundToLong()
                        )
                    },
                    modifier = Modifier.animateItemPlacement(),
                    onDeleteRecord = { onDeleteRecord(item.audioRecord) },
                    onTogglePlaying = { onTogglePlaying(item.audioRecord) },
                    onProcessRecord = { showModelSelectorForItem = item.audioRecord },
                    onCancelProcessRecord = { onCancelProcessRecord(item.audioRecord) },
                )
            }
        }

        showModelSelectorForItem?.let { record ->
            ModelSelectorDialog(
                onDismissRequest = {
                    showModelSelectorForItem = null
                },
                onModelPicked = { model ->
                    onProcessRecord(record, model)
                    showModelSelectorForItem = null
                }
            )
        }
    }
}

@Composable
fun Modifier.multipleTapDetector(count: Int, onTapped: () -> Unit): Modifier {
    var tapCount by remember {
        mutableIntStateOf(0)
    }
    var lastTouchTime by remember {
        mutableLongStateOf(-1)
    }

    val currentOnTapped by rememberUpdatedState(onTapped)

    return this.pointerInput(Unit) {
        // Custom gesture detection
        detectTapGestures(
            onTap = {
                val now = System.currentTimeMillis()
                if (now - lastTouchTime > 400) {
                    tapCount = 0
                }
                lastTouchTime = now

                // Increment the tap count
                tapCount += 1

                // Trigger action on 4 taps
                if (tapCount >= count) {
                    tapCount = 0
                    currentOnTapped()
                }
            }
        )
    }
}
