package com.ciyin.app.ui.screen.videoplayer

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.LockOpen
import androidx.compose.material.icons.rounded.PictureInPictureAlt
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.layout.boundsInWindow
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ciyin.app.sample.Res
import com.ciyin.app.sample.video_player_demo_auto_pip
import com.ciyin.app.sample.video_player_demo_buffering
import com.ciyin.app.sample.video_player_demo_close_settings
import com.ciyin.app.sample.video_player_demo_enter_pip
import com.ciyin.app.sample.video_player_demo_exit_pip
import com.ciyin.app.sample.video_player_demo_gesture_lock
import com.ciyin.app.sample.video_player_demo_lock
import com.ciyin.app.sample.video_player_demo_no_media
import com.ciyin.app.sample.video_player_demo_screenshot_error
import com.ciyin.app.sample.video_player_demo_screenshot_saved
import com.ciyin.app.sample.video_player_demo_settings
import com.ciyin.app.sample.video_player_demo_title
import com.ciyin.app.sample.video_player_demo_unlock
import ciyin.io.File
import ciyin.material.theme.AppTheme
import ciyin.platform.LocalContext
import ciyin.platform.currentPlatform
import ciyin.platform.files
import ciyin.platform.time.currentTimeMillis
import ciyin.video.player.MediampAudioLevelController
import ciyin.video.player.data.MediaCacheProgressInfo
import ciyin.video.player.ui.NoOpPlaybackSpeedController
import ciyin.video.player.ui.PlaybackSpeedControllerState
import ciyin.video.player.ui.ControllerVisibility
import ciyin.video.player.ui.PlayerControllerState
import ciyin.video.player.ui.VideoPlayer
import ciyin.video.player.ui.VideoScaffold
import ciyin.video.player.ui.component.PlayerFloatingButtonBox
import ciyin.video.player.ui.component.ScreenshotButton
import ciyin.video.player.ui.gesture.GestureFamily
import ciyin.video.player.ui.gesture.LockableVideoGestureHost
import ciyin.video.player.ui.gesture.NoOpLevelController
import ciyin.video.player.ui.gesture.PlayerGestureHost
import ciyin.video.player.ui.gesture.mouseFamily
import ciyin.video.player.ui.gesture.rememberGestureIndicatorState
import ciyin.video.player.ui.gesture.rememberPlayerFastSkipState
import ciyin.video.player.ui.gesture.rememberSwipeSeekerState
import ciyin.video.player.ui.loading.VideoLoadingIndicator
import ciyin.video.player.ui.pip.createPipController
import ciyin.video.player.ui.progress.AudioSwitcher
import ciyin.video.player.ui.progress.MediaProgressIndicatorText
import ciyin.video.player.ui.progress.MediaProgressSlider
import ciyin.video.player.ui.progress.PlayerControllerBar
import ciyin.video.player.ui.progress.PlayerControllerDefaults
import ciyin.video.player.ui.progress.rememberMediaProgressSliderState
import ciyin.video.player.ui.rememberAlwaysOnRequester
import ciyin.video.player.ui.sheet.SideSheetLayout
import ciyin.video.player.ui.sheet.VideoSideSheets
import ciyin.video.player.ui.sheet.hasPageAsState
import ciyin.video.player.ui.sheet.rememberVideoSideSheetsController
import ciyin.video.player.ui.top.PlayerTopBar
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource
import org.openani.mediamp.MediampPlayer
import org.openani.mediamp.PlaybackState
import org.openani.mediamp.features.AudioLevelController
import org.openani.mediamp.features.PlaybackSpeed
import org.openani.mediamp.features.Screenshots
import org.openani.mediamp.features.audioTracks
import org.openani.mediamp.togglePause

/** 完整展示播放器模块控制栏、手势、侧栏、截图与画中画能力。 */
@Composable
internal fun VideoPlayerDemoPlayerView(
    player: MediampPlayer,
    state: VideoPlayerDemoUiState,
    expanded: Boolean,
    onBack: () -> Unit,
    onFullscreenChange: (Boolean) -> Unit,
    onFeedback: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val controllerState = remember(player) {
        PlayerControllerState(ControllerVisibility.Visible)
    }
    val progressState = rememberMediaProgressSliderState(
        player = player,
        onPreview = {},
        onPreviewFinished = player::seekTo,
    )
    val playbackSpeed = remember(player) { player.features[PlaybackSpeed] }
    val playbackSpeedState = remember(player, playbackSpeed, scope) {
        PlaybackSpeedControllerState(
            playbackSpeed = playbackSpeed ?: NoOpPlaybackSpeedController,
            scope = scope,
        )
    }
    val audioFeature = remember(player) { player.features[AudioLevelController] }
    val audioController = remember(player, audioFeature) {
        audioFeature?.let { feature ->
            MediampAudioLevelController(feature) { _, _ -> Unit }
        } ?: NoOpLevelController
    }
    val screenshotFeature = remember(player) { player.features[Screenshots] }
    val pipController = remember(context, player) { createPipController(context, player) }
    val isInPipMode by pipController.isInPipMode.collectAsStateWithLifecycle()
    val sheetsController = rememberVideoSideSheetsController<VideoPlayerDemoSheet>()
    val anySideSheetVisible by sheetsController.hasPageAsState()
    val gestureFamily = currentPlatform().mouseFamily
    var gestureLocked by rememberSaveable(player) { mutableStateOf(false) }
    var autoPipEnabled by rememberSaveable(player) { mutableStateOf(false) }
    var videoBounds by remember { mutableStateOf(Rect.Zero) }
    var indicatorJob by remember(player) { mutableStateOf<Job?>(null) }
    val screenshotSavedPattern = stringResource(Res.string.video_player_demo_screenshot_saved)
    val screenshotErrorPattern = stringResource(Res.string.video_player_demo_screenshot_error)

    DisposableEffect(pipController) {
        onDispose(pipController::release)
    }
    LaunchedEffect(autoPipEnabled, pipController) {
        pipController.setAutoEnterEnabled(autoPipEnabled)
    }
    LaunchedEffect(expanded) {
        controllerState.toggleFullVisible(true)
    }

    VideoScaffold(
        expanded = expanded,
        modifier = modifier.background(AppTheme.colorScheme.surfaceLower),
        contentWindowInsets = WindowInsets(0),
        controllerState = controllerState,
        gestureLocked = gestureLocked,
        topBar = {
            if (expanded) {
                PlayerTopBar(
                    title = { Text(stringResource(Res.string.video_player_demo_title)) },
                    onBackPressed = onBack,
                    actions = {
                        if (pipController.isPipSupported) {
                            IconButton(
                                onClick = {
                                    if (isInPipMode) pipController.exitPip()
                                    else pipController.enterPip(videoBounds)
                                },
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.PictureInPictureAlt,
                                    contentDescription = stringResource(
                                        if (isInPipMode) {
                                            Res.string.video_player_demo_exit_pip
                                        } else {
                                            Res.string.video_player_demo_enter_pip
                                        },
                                    ),
                                )
                            }
                        }
                        IconButton(
                            onClick = {
                                sheetsController.navigateTo(VideoPlayerDemoSheet.Settings)
                            },
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Settings,
                                contentDescription = stringResource(
                                    Res.string.video_player_demo_settings,
                                ),
                            )
                        }
                    },
                )
            }
        },
        video = {
            VideoPlayer(
                player = player,
                modifier = Modifier
                    .fillMaxSize()
                    .onGloballyPositioned { coordinates ->
                        videoBounds = coordinates.boundsInWindow()
                    },
            )
        },
        gestureHost = {
            val swipeSeekerState = rememberSwipeSeekerState(constraints.maxWidth) { seconds ->
                player.skip(seconds * MillisecondsPerSecond)
            }
            val mediaProperties by player.mediaProperties.collectAsState(null)
            val enableSwipeToSeek = mediaProperties?.durationMillis != 0L
            val indicatorState = rememberGestureIndicatorState()
            val fastSkipState = playbackSpeed?.let { feature ->
                rememberPlayerFastSkipState(
                    playerState = feature,
                    gestureIndicatorState = indicatorState,
                )
            }

            LockableVideoGestureHost(
                locked = gestureLocked,
                controllerState = controllerState,
            ) {
                PlayerGestureHost(
                    controllerState = controllerState,
                    seekerState = swipeSeekerState,
                    progressSliderState = progressState,
                    indicatorState = indicatorState,
                    enableSwipeToSeek = enableSwipeToSeek,
                    audioController = audioController,
                    brightnessController = NoOpLevelController,
                    playbackSpeedControllerState = playbackSpeedState,
                    fastSkipState = fastSkipState,
                    audioLevelController = audioFeature,
                    family = gestureFamily,
                    isFullscreen = expanded,
                    onTogglePauseResume = {
                        indicatorJob?.cancel()
                        indicatorJob = scope.launch {
                            if (state.playbackState?.isPlaying == true) {
                                indicatorState.showPausedLong()
                            } else {
                                indicatorState.showResumedLong()
                            }
                        }
                        player.togglePause()
                    },
                    onToggleFullscreen = { onFullscreenChange(!expanded) },
                    onExitFullscreen = { onFullscreenChange(false) },
                    danmakuGestureHost = {},
                )
            }
        },
        floatingMessage = {
            VideoPlayerDemoStatus(
                state = state,
                anySideSheetVisible = anySideSheetVisible,
            )
        },
        bottomBar = {
            VideoPlayerDemoControllerBar(
                player = player,
                state = state,
                expanded = expanded,
                gestureFamily = gestureFamily,
                controllerState = controllerState,
                progressState = progressState,
                playbackSpeedState = playbackSpeedState,
                audioController = audioController,
                onFullscreenChange = onFullscreenChange,
            )
        },
        detachedProgressSlider = {
            MediaProgressSlider(
                state = progressState,
                cacheProgressInfoFlow = { MediaCacheProgressInfo.Empty },
            )
        },
        floatingBottomEnd = {
            PlayerControllerDefaults.FullscreenIcon(
                isFullscreen = expanded,
                onClickFullscreen = { onFullscreenChange(!expanded) },
            )
        },
        rhsButtons = {
            if (expanded && screenshotFeature != null) {
                ScreenshotButton(
                    onClick = {
                        scope.launch {
                            takeVideoPlayerDemoScreenshot(
                                screenshots = screenshotFeature,
                                destinationDirectory = context.files.cacheDir,
                                successPattern = screenshotSavedPattern,
                                errorPattern = screenshotErrorPattern,
                                onFeedback = onFeedback,
                            )
                        }
                    },
                )
            }
        },
        gestureLock = {
            if (expanded) {
                PlayerFloatingButtonBox {
                    IconButton(onClick = { gestureLocked = !gestureLocked }) {
                        Icon(
                            imageVector = if (gestureLocked) {
                                Icons.Rounded.LockOpen
                            } else {
                                Icons.Rounded.Lock
                            },
                            contentDescription = stringResource(
                                if (gestureLocked) {
                                    Res.string.video_player_demo_unlock
                                } else {
                                    Res.string.video_player_demo_lock
                                },
                            ),
                        )
                    }
                }
            }
        },
        rhsSheet = {
            VideoPlayerDemoSettingsSheets(
                controller = sheetsController,
                gestureLocked = gestureLocked,
                onGestureLockedChange = { gestureLocked = it },
                pipSupported = pipController.isPipSupported,
                autoPipEnabled = autoPipEnabled,
                onAutoPipEnabledChange = { autoPipEnabled = it },
            )
        },
    )
}

/** 显示播放器当前的占位、缓冲或错误反馈。 */
@Composable
private fun VideoPlayerDemoStatus(
    state: VideoPlayerDemoUiState,
    anySideSheetVisible: Boolean,
) {
    if (anySideSheetVisible) return

    val message = when {
        state.errorMessage != null -> state.errorMessage
        !state.hasMedia -> stringResource(Res.string.video_player_demo_no_media)
        state.playbackState == null || state.playbackState == PlaybackState.PAUSED_BUFFERING -> {
            stringResource(Res.string.video_player_demo_buffering)
        }

        else -> null
    } ?: return
    val showProgress = state.hasMedia && state.errorMessage == null

    Surface(
        color = AppTheme.colorScheme.surfaceHigh,
        shape = AppTheme.shapes.medium,
    ) {
        VideoLoadingIndicator(
            showProgress = showProgress,
            text = { Text(message) },
            modifier = Modifier.padding(AppTheme.spacings.medium),
        )
    }
}

/** 组合播放、音量、进度、倍速、音轨与全屏控制。 */
@Composable
private fun VideoPlayerDemoControllerBar(
    player: MediampPlayer,
    state: VideoPlayerDemoUiState,
    expanded: Boolean,
    gestureFamily: GestureFamily,
    controllerState: PlayerControllerState,
    progressState: ciyin.video.player.ui.progress.PlayerProgressSliderState,
    playbackSpeedState: PlaybackSpeedControllerState,
    audioController: ciyin.video.player.ui.gesture.LevelController,
    onFullscreenChange: (Boolean) -> Unit,
) {
    PlayerControllerBar(
        startActions = {
            PlayerControllerDefaults.PlaybackIcon(
                isPlaying = { state.playbackState?.isPlaying == true },
                onClick = player::togglePause,
            )
            val mediampAudioController = audioController as? MediampAudioLevelController
            if (expanded && mediampAudioController != null && gestureFamily == GestureFamily.Mouse) {
                val level by mediampAudioController.levelFlow.collectAsState()
                val isMute by mediampAudioController.muteFlow.collectAsState()
                PlayerControllerDefaults.AudioIcon(
                    volume = level,
                    isMute = isMute,
                    maxValue = mediampAudioController.range.endInclusive,
                    onClick = mediampAudioController::toggleMute,
                    onchange = mediampAudioController::setLevel,
                    controllerState = controllerState,
                )
            }
        },
        progressIndicator = { MediaProgressIndicatorText(progressState) },
        progressSlider = {
            MediaProgressSlider(
                state = progressState,
                cacheProgressInfoFlow = { MediaCacheProgressInfo.Empty },
                showPreviewTimeTextOnThumb = expanded,
            )
        },
        danmakuEditor = EmptyControllerSlot,
        endActions = {
            if (expanded) {
                player.audioTracks?.let { tracks ->
                    PlayerControllerDefaults.AudioSwitcher(tracks)
                }
            }
            val alwaysOnRequester = rememberAlwaysOnRequester(
                controllerState = controllerState,
                debugName = "videoPlayerDemoSpeed",
            )
            PlayerControllerDefaults.SpeedSwitcher(
                playbackSpeedControllerState = playbackSpeedState,
                onExpandedChanged = { isExpanded ->
                    if (isExpanded) alwaysOnRequester.request()
                    else alwaysOnRequester.cancelRequest()
                },
            )
            PlayerControllerDefaults.FullscreenIcon(
                isFullscreen = expanded,
                onClickFullscreen = { onFullscreenChange(!expanded) },
            )
        },
        expanded = expanded,
    )
}

/** 显示播放器设置侧栏。 */
@Composable
private fun VideoPlayerDemoSettingsSheets(
    controller: ciyin.video.player.ui.sheet.VideoSideSheetsController<VideoPlayerDemoSheet>,
    gestureLocked: Boolean,
    onGestureLockedChange: (Boolean) -> Unit,
    pipSupported: Boolean,
    autoPipEnabled: Boolean,
    onAutoPipEnabledChange: (Boolean) -> Unit,
) {
    VideoSideSheets(controller = controller) { page ->
        when (page) {
            VideoPlayerDemoSheet.Settings -> {
                SideSheetLayout(
                    title = { Text(stringResource(Res.string.video_player_demo_settings)) },
                    onDismissRequest = ::closeSideSheet,
                    closeButton = {
                        IconButton(onClick = ::closeSideSheet) {
                            Icon(
                                imageVector = Icons.Rounded.Close,
                                contentDescription = stringResource(
                                    Res.string.video_player_demo_close_settings,
                                ),
                            )
                        }
                    },
                ) {
                    VideoPlayerDemoSettingToggle(
                        label = stringResource(Res.string.video_player_demo_gesture_lock),
                        checked = gestureLocked,
                        onCheckedChange = onGestureLockedChange,
                    )
                    if (pipSupported) {
                        HorizontalDivider(color = AppTheme.colorScheme.divider)
                        VideoPlayerDemoSettingToggle(
                            label = stringResource(Res.string.video_player_demo_auto_pip),
                            checked = autoPipEnabled,
                            onCheckedChange = onAutoPipEnabledChange,
                        )
                    }
                }
            }
        }
    }
}

/** 显示一个可整行点击的播放器布尔设置。 */
@Composable
private fun VideoPlayerDemoSettingToggle(
    label: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCheckedChange(!checked) }
            .padding(
                horizontal = AppTheme.spacings.large,
                vertical = AppTheme.spacings.medium,
            ),
        horizontalArrangement = Arrangement.spacedBy(AppTheme.spacings.medium),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = label,
            modifier = Modifier.weight(1f),
            style = AppTheme.typography.bodyLarge,
            color = AppTheme.colorScheme.textPrimary,
        )
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
        )
    }
}

/** 将当前视频帧保存到应用缓存目录并回报结果。 */
private suspend fun takeVideoPlayerDemoScreenshot(
    screenshots: Screenshots,
    destinationDirectory: File,
    successPattern: String,
    errorPattern: String,
    onFeedback: (String) -> Unit,
) {
    val destination = File(
        parent = destinationDirectory,
        child = "video-player-${currentTimeMillis()}.png",
    )
    try {
        screenshots.takeScreenshot(destination.absolutePath)
        onFeedback(successPattern.replace("%1\$s", destination.absolutePath))
    } catch (cancellation: CancellationException) {
        throw cancellation
    } catch (error: Exception) {
        val reason = error.message ?: error::class.simpleName ?: error.toString()
        onFeedback(errorPattern.replace("%1\$s", reason))
    }
}

/** 播放器设置侧栏中的页面。 */
private enum class VideoPlayerDemoSheet {
    /** 播放器交互设置。 */
    Settings,
}

/** 一秒包含的毫秒数。 */
private const val MillisecondsPerSecond = 1_000L

/** 控制栏中的空插槽。 */
private val EmptyControllerSlot: @Composable RowScope.() -> Unit = {}
