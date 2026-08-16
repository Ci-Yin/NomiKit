package com.ciyin.app.ui.screen.videoplayer

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import ciyin.material.theme.AppTheme
import ciyin.ui.foundation.systemuicontroller.SystemUiControllerEffect
import ciyin.ui.foundation.viewmodel.collectSideEffects
import ciyin.ui.foundation.viewmodel.viewModel
import com.ciyin.app.sample.Res
import com.ciyin.app.sample.video_player_demo_back
import com.ciyin.app.sample.video_player_demo_load_error
import com.ciyin.app.sample.video_player_demo_play
import com.ciyin.app.sample.video_player_demo_state
import com.ciyin.app.sample.video_player_demo_title
import com.ciyin.app.sample.video_player_demo_url_label
import com.ciyin.app.sample.video_player_demo_vlc_unavailable
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource
import org.openani.mediamp.MediampPlayer
import org.openani.mediamp.PlaybackState
import org.openani.mediamp.playUri

/** 视频播放器真实 URL 播放示例。 */
@Composable
internal fun VideoPlayerDemoScreen(
    onBack: () -> Unit,
    viewModel: VideoPlayerDemoViewModel = viewModel(::VideoPlayerDemoViewModel),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val player = rememberVideoPlayerDemoPlayer()
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    val loadErrorMessage = stringResource(Res.string.video_player_demo_load_error)
    val playerUnavailableMessage = stringResource(Res.string.video_player_demo_vlc_unavailable)
    var isFullscreen by rememberSaveable { mutableStateOf(false) }
    var loadJob by remember(player) { mutableStateOf<Job?>(null) }

    SystemUiControllerEffect(isFullscreen) {
        isSystemBarsVisible = !isFullscreen
    }

    LaunchedEffect(player) {
        val availablePlayer = player ?: return@LaunchedEffect
        launch {
            availablePlayer.playbackState.collect { playbackState ->
                viewModel.dispatchAction(VideoPlayerDemoAction.PlaybackChanged(playbackState))
            }
        }
        launch {
            availablePlayer.mediaData.collect { mediaData ->
                viewModel.dispatchAction(
                    VideoPlayerDemoAction.MediaAvailabilityChanged(mediaData != null),
                )
            }
        }
    }

    viewModel.collectSideEffects { effect ->
        when (effect) {
            is VideoPlayerDemoEffect.LoadUrl -> {
                loadJob?.cancel()
                if (player == null) {
                    viewModel.dispatchAction(
                        VideoPlayerDemoAction.PlaybackFailed(playerUnavailableMessage),
                    )
                } else {
                    loadJob = scope.launch {
                        loadAndStartVideo(
                            player = player,
                            url = effect.url,
                            loadErrorMessage = loadErrorMessage,
                            onAction = viewModel.dispatchAction,
                        )
                    }
                }
            }

            VideoPlayerDemoEffect.NavigateBack -> onBack()
        }
    }

    Box(Modifier.fillMaxSize()) {
        if (isFullscreen && player != null) {
            VideoPlayerDemoPlayerView(
                player = player,
                state = state,
                expanded = true,
                onBack = { isFullscreen = false },
                onFullscreenChange = { isFullscreen = it },
                onFeedback = { message ->
                    scope.launch { snackbarHostState.showSnackbar(message) }
                },
                modifier = Modifier.fillMaxSize(),
            )
            SnackbarHost(
                hostState = snackbarHostState,
                modifier = Modifier.align(Alignment.BottomCenter),
            )
        } else {
            VideoPlayerDemoContent(
                state = state,
                player = player,
                playerUnavailableMessage = playerUnavailableMessage,
                snackbarHostState = snackbarHostState,
                onFullscreenChange = { isFullscreen = it },
                onFeedback = { message ->
                    scope.launch { snackbarHostState.showSnackbar(message) }
                },
                onAction = viewModel.dispatchAction,
            )
        }
    }
}

/** 提交媒体 URL，等待播放器就绪后开始播放。 */
private suspend fun loadAndStartVideo(
    player: MediampPlayer,
    url: String,
    loadErrorMessage: String,
    onAction: (VideoPlayerDemoAction) -> Unit,
) {
    val startedFromError = player.getCurrentPlaybackState() == PlaybackState.ERROR
    when (val result = submitVideoUrl(url) { player.playUri(it) }) {
        VideoUrlPlayResult.Empty -> Unit
        is VideoUrlPlayResult.Failed -> {
            onAction(VideoPlayerDemoAction.PlaybackFailed(result.message))
        }

        is VideoUrlPlayResult.Started -> {
            try {
                if (startedFromError) {
                    player.playbackState.first { it != PlaybackState.ERROR }
                }
                when (
                    player.playbackState.first {
                        it == PlaybackState.READY ||
                                it == PlaybackState.PAUSED ||
                                it == PlaybackState.PLAYING ||
                                it == PlaybackState.ERROR
                    }
                ) {
                    PlaybackState.READY, PlaybackState.PAUSED -> player.resume()
                    PlaybackState.ERROR -> onAction(
                        VideoPlayerDemoAction.PlaybackFailed(loadErrorMessage),
                    )

                    else -> Unit
                }
            } catch (cancellation: CancellationException) {
                throw cancellation
            } catch (error: Exception) {
                onAction(
                    VideoPlayerDemoAction.PlaybackFailed(
                        error.message ?: error::class.simpleName ?: error.toString(),
                    ),
                )
            }
        }
    }
}

/** 只依赖状态、播放器和动作回调的视频播放器页面内容。 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun VideoPlayerDemoContent(
    state: VideoPlayerDemoUiState,
    player: MediampPlayer?,
    playerUnavailableMessage: String,
    snackbarHostState: SnackbarHostState,
    onFullscreenChange: (Boolean) -> Unit,
    onFeedback: (String) -> Unit,
    onAction: (VideoPlayerDemoAction) -> Unit,
) {
    val spacings = AppTheme.spacings

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(Res.string.video_player_demo_title)) },
                navigationIcon = {
                    IconButton(onClick = { onAction(VideoPlayerDemoAction.BackClick) }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(Res.string.video_player_demo_back),
                        )
                    }
                },
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = spacings.large),
            verticalArrangement = Arrangement.spacedBy(spacings.medium),
        ) {
            OutlinedTextField(
                value = state.url,
                onValueChange = { onAction(VideoPlayerDemoAction.UrlChange(it)) },
                modifier = Modifier.fillMaxWidth(),
                label = { Text(stringResource(Res.string.video_player_demo_url_label)) },
                singleLine = true,
            )
            Button(
                onClick = { onAction(VideoPlayerDemoAction.PlayClick) },
                modifier = Modifier.fillMaxWidth(),
                enabled = player != null,
            ) {
                Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = null,
                )
                Text(
                    text = stringResource(Res.string.video_player_demo_play),
                    modifier = Modifier.padding(start = spacings.small),
                )
            }
            if (player == null) {
                VideoPlayerUnavailableContent(playerUnavailableMessage)
            } else {
                VideoPlayerDemoPlayerView(
                    player = player,
                    state = state,
                    expanded = false,
                    onBack = { onAction(VideoPlayerDemoAction.BackClick) },
                    onFullscreenChange = onFullscreenChange,
                    onFeedback = onFeedback,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
            Text(
                text = stringResource(
                    Res.string.video_player_demo_state,
                    state.playbackState?.name ?: "-",
                ),
                style = MaterialTheme.typography.bodySmall,
                color = AppTheme.colorScheme.textSecondary,
            )
        }
    }
}

/** 显示当前平台播放器不可用时的占位内容。 */
@Composable
private fun VideoPlayerUnavailableContent(message: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(AppTheme.spacings.huge),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = message,
            color = AppTheme.colorScheme.textSecondary,
            style = AppTheme.typography.bodyMedium,
        )
    }
}
