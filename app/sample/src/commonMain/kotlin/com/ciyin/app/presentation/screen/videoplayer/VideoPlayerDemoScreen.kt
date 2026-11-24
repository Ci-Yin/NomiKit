package com.ciyin.app.presentation.screen.videoplayer

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import io.github.composefluent.icons.Icons
import io.github.composefluent.icons.regular.Play
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
import ciyin.designsystem.component.NomiBorder
import ciyin.designsystem.component.button.NomiButton
import ciyin.designsystem.component.icon.NomiIcon
import ciyin.designsystem.component.surface.NomiSurface
import ciyin.designsystem.component.surface.NomiSurfaceColors
import ciyin.designsystem.component.text.NomiText
import ciyin.designsystem.component.text.NomiTextField
import ciyin.designsystem.component.text.NomiTextFieldVariant
import ciyin.designsystem.theme.NomiTheme
import ciyin.material.theme.AppTheme
import ciyin.ui.foundation.systemuicontroller.SystemUiControllerEffect
import ciyin.ui.foundation.viewmodel.collectSideEffects
import ciyin.ui.foundation.viewmodel.viewModel
import com.ciyin.app.presentation.component.SamplePageScaffold
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
import kotlinx.coroutines.delay
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
    var snackbarMessage by remember { mutableStateOf<String?>(null) }
    val loadErrorMessage = stringResource(Res.string.video_player_demo_load_error)
    val playerUnavailableMessage = stringResource(Res.string.video_player_demo_vlc_unavailable)
    var isFullscreen by rememberSaveable { mutableStateOf(false) }
    var loadJob by remember(player) { mutableStateOf<Job?>(null) }

    SystemUiControllerEffect(isFullscreen) {
        isSystemBarsVisible = !isFullscreen
    }

    LaunchedEffect(snackbarMessage) {
        val message = snackbarMessage ?: return@LaunchedEffect
        delay(SampleSnackbarDurationMillis)
        if (snackbarMessage == message) {
            snackbarMessage = null
        }
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
                onFeedback = { message -> snackbarMessage = message },
                modifier = Modifier.fillMaxSize(),
            )
        } else {
            VideoPlayerDemoContent(
                state = state,
                player = player,
                playerUnavailableMessage = playerUnavailableMessage,
                onFullscreenChange = { isFullscreen = it },
                onFeedback = { message -> snackbarMessage = message },
                onAction = viewModel.dispatchAction,
            )
        }
        snackbarMessage?.let { message ->
            SampleSnackbar(
                message = message,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(NomiTheme.spacings.large),
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
@Composable
private fun VideoPlayerDemoContent(
    state: VideoPlayerDemoUiState,
    player: MediampPlayer?,
    playerUnavailableMessage: String,
    onFullscreenChange: (Boolean) -> Unit,
    onFeedback: (String) -> Unit,
    onAction: (VideoPlayerDemoAction) -> Unit,
) {
    val spacings = AppTheme.spacings

    SamplePageScaffold(
        title = stringResource(Res.string.video_player_demo_title),
        onBack = { onAction(VideoPlayerDemoAction.BackClick) },
        backContentDescription = stringResource(Res.string.video_player_demo_back),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = spacings.large, vertical = spacings.medium),
            verticalArrangement = Arrangement.spacedBy(spacings.medium),
        ) {
            NomiTextField(
                value = state.url,
                onValueChange = { onAction(VideoPlayerDemoAction.UrlChange(it)) },
                modifier = Modifier.fillMaxWidth(),
                variant = NomiTextFieldVariant.Outlined,
                label = { NomiText(stringResource(Res.string.video_player_demo_url_label)) },
                singleLine = true,
            )
            NomiButton(
                onClick = { onAction(VideoPlayerDemoAction.PlayClick) },
                modifier = Modifier.fillMaxWidth(),
                enabled = player != null,
            ) {
                NomiIcon(
                    imageVector = Icons.Regular.Play,
                    contentDescription = null,
                )
                NomiText(
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
            NomiText(
                text = stringResource(
                    Res.string.video_player_demo_state,
                    state.playbackState?.name ?: "-",
                ),
                style = NomiTheme.typography.bodySmall,
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
        NomiText(
            text = message,
            color = AppTheme.colorScheme.textSecondary,
            style = AppTheme.typography.bodyMedium,
        )
    }
}


/**
 * 样例页底部提示条，使用 Nomi 表面与文本组件叠加在页面之上。
 *
 * @param message 提示文案。
 * @param modifier 应用于提示条根节点的修饰符。
 */
@Composable
private fun SampleSnackbar(
    message: String,
    modifier: Modifier = Modifier,
) {
    NomiSurface(
        modifier = modifier,
        shape = NomiTheme.shapes.medium,
        colors = NomiSurfaceColors(
            containerColor = NomiTheme.colorScheme.surfaceHigh,
            contentColor = NomiTheme.colorScheme.textPrimary,
        ),
        border = NomiBorder.Stroke(
            BorderStroke(
                width = NomiTheme.sizes.strokes.thin,
                color = NomiTheme.colorScheme.outline,
            ),
        ),
    ) {
        NomiText(
            text = message,
            modifier = Modifier.padding(
                horizontal = NomiTheme.spacings.large,
                vertical = NomiTheme.spacings.medium,
            ),
            style = NomiTheme.typography.bodyMedium,
        )
    }
}

/** 样例提示条自动隐藏时长，单位毫秒。 */
private const val SampleSnackbarDurationMillis = 3_000L
