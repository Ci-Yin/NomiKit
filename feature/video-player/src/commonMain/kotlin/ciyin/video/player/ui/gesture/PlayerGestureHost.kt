package ciyin.video.player.ui.gesture

import androidx.annotation.UiThread
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemGestures
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsTopHeight
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.VolumeUp
import androidx.compose.material.icons.rounded.BrightnessHigh
import androidx.compose.material.icons.rounded.BrightnessLow
import androidx.compose.material.icons.rounded.BrightnessMedium
import androidx.compose.material.icons.rounded.FastForward
import androidx.compose.material.icons.rounded.FastRewind
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ProvideTextStyle
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusEvent
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.isShiftPressed
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import ciyin.platform.Platform
import ciyin.platform.currentPlatform
import ciyin.video.player.ui.internal.fixToString
import ciyin.video.player.ui.internal.rememberLatestTask
import ciyin.ui.foundation.effects.ComposeKey
import ciyin.ui.foundation.effects.onKey
import ciyin.ui.foundation.effects.onPointerEventMultiplatform
import ciyin.video.player.ui.internal.ifThen
import ciyin.video.player.ui.ControllerVisibility
import ciyin.video.player.ui.PlaybackSpeedControllerState
import ciyin.video.player.ui.PlayerControllerState
import ciyin.video.player.ui.gesture.SwipeSeekerState.Companion.swipeToSeek
import ciyin.video.player.ui.progress.PlayerProgressSliderState
import ciyin.video.player.ui.rememberAlwaysOnRequester
import ciyin.video.player.ui.top.needWorkaroundForFocusManager
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.openani.mediamp.features.AudioLevelController
import kotlin.math.absoluteValue
import kotlin.time.Duration.Companion.seconds

/** 将秒数渲染为手势提示使用的时间文本。 */
@Stable
private fun renderTime(seconds: Int): String {
    return "${(seconds / 60).fixToString(2)}:${(seconds % 60).fixToString(2)}"
}


/** 记住播放器手势提示状态。 */
@Composable
fun rememberGestureIndicatorState(): GestureIndicatorState = remember { GestureIndicatorState() }

/** 管理暂停、音量、亮度、跳转与倍速手势提示。 */
@Stable
class GestureIndicatorState {
    /** 当前手势提示类型。 */
    internal enum class State {
        PausedOnce,
        ResumedOnce,
        Volume,
        Brightness,
        Seeking,
        FastForward,
        FastBackward,
    }

    /** 手势提示是否可见。 */
    internal var visible: Boolean by mutableStateOf(false)

    /** 当前手势提示状态。 */
    internal var state: State? by mutableStateOf(null)

    /** 音量或亮度提示进度。 */
    internal var progressValue: Float by mutableFloatStateOf(0f)

    /** 跳转提示的秒数偏移。 */
    internal var deltaSeconds: Int by mutableIntStateOf(0)

    /** 用于忽略过期隐藏请求的票据。 */
    private var counter: Int = 0

    /** 开始显示提示并返回本次票据。 */
    private inline fun startShow(
        state: State,
        setup: () -> Unit = {},
    ): Int {
        val ticket = ++counter
        setup()
        this.state = state
        visible = true
        return ticket
    }

    /** 显示提示并在指定时间后自动隐藏。 */
    private inline fun show(
        state: State,
        setup: () -> Unit = {},
        action: () -> Unit
    ) {
        val ticket = ++counter
        try {
            setup()
            this.state = state
            visible = true
            action()
        } finally {
            if (this.counter == ticket && // no one changed the state after us
                this.state == state
            ) {
                visible = false
            }
        }
    }

    private companion object {
        private const val LONG: Long = 700
        private const val SHORT: Long = 500
    }

    @UiThread
    /** 长时间显示暂停提示。 */
    suspend fun showPausedLong() {
        show(State.PausedOnce) {
            delay(LONG)
        }
    }

    @UiThread
    /** 长时间显示继续播放提示。 */
    suspend fun showResumedLong() {
        show(State.ResumedOnce) {
            delay(LONG)
        }
    }

    @UiThread
    /** 显示音量比例提示。 */
    suspend fun showVolumeRange(currentRatio: Float) {
        show(State.Volume, setup = { progressValue = currentRatio }) {
            delay(SHORT)
        }
    }

    @UiThread
    /** 显示亮度比例提示。 */
    suspend fun showBrightnessRange(currentRatio: Float) {
        show(State.Brightness, setup = { progressValue = currentRatio }) {
            delay(SHORT)
        }
    }

    @UiThread
    /** 显示媒体跳转位置提示。 */
    suspend fun showSeeking(
        deltaSeconds: Int,
    ) {
        show(State.Seeking, setup = { this.deltaSeconds = deltaSeconds }) {
            delay(SHORT)
        }
    }

    @UiThread
            /** 开始显示快进提示并返回票据。 */
    fun startFastForward(): Int {
        startShow(State.FastForward, setup = { })
        return counter
    }

    @UiThread
            /** 在票据有效时停止快进提示。 */
    fun stopFastForward(ticket: Int) {
        stopShow(ticket)
    }

    @UiThread
            /** 开始显示快退提示并返回票据。 */
    fun startFastBackward(): Int {
        startShow(State.FastBackward, setup = { })
        return counter
    }

    @UiThread
            /** 在票据有效时停止快退提示。 */
    fun stopFastBackward(ticket: Int) {
        stopShow(ticket)
    }

    /** 仅在票据仍有效时隐藏提示。 */
    private fun stopShow(ticket: Int) {
        if (ticket == this.counter) {
            visible = false
        }
    }
}

/**
 * 展示当前快进/快退秒数的指示器.
 *
 * `<< 00:00` / `>> 00:00`
 */
@Composable
fun GestureIndicator(
    state: GestureIndicatorState,
) {
    val shape = MaterialTheme.shapes.small
    val colors = MaterialTheme.colorScheme
    var lastDelta by remember(state) {
        mutableIntStateOf(state.deltaSeconds)
    }

    AnimatedVisibility(
        visible = state.visible,
        enter = fadeIn(spring(stiffness = Spring.StiffnessMedium)),
        exit = fadeOut(tween(durationMillis = 500)),
        label = "SeekPositionIndicator",
    ) {
        Surface(
            Modifier.alpha(0.8f),
            color = colors.surface,
            shape = shape,
            shadowElevation = 1.dp,
            contentColor = colors.onSurface,
        ) {
            val iconSize = 36.dp
            ProvideTextStyle(MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold)) {
                Row(
                    Modifier.background(Color.Transparent)
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                        .height(iconSize),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    // Used by volume and brightness
                    val progressIndicator: @Composable () -> Unit = remember(state, colors) {
                        // This remember is needed because Compose does not remember lambdas
                        // and can cause performance problem in this fast-changing composable.
                        {
                            LinearProgressIndicator(
                                progress = { state.progressValue },
                                modifier = Modifier.width(80.dp),
                                color = colors.primary,
                                trackColor = colors.onSurface.copy(alpha = 0.5f),
                                drawStopIndicator = {},
                            )
                        }
                    }

                    when (state.state) {
                        GestureIndicatorState.State.ResumedOnce -> {
                            Icon(
                                Icons.Rounded.PlayArrow, null,
                                Modifier.size(iconSize).background(Color.Transparent),
                            )
                        }

                        GestureIndicatorState.State.PausedOnce -> {
                            Icon(Icons.Rounded.Pause, null, Modifier.size(iconSize))
                        }

                        GestureIndicatorState.State.Seeking -> {
                            val deltaDuration = state.deltaSeconds
                            // 记忆变为 0 之前的 delta, 这样在快进/快退结束后, 会显示上一次的 delta, 而不是显示 0
                            val duration = if (deltaDuration == 0) {
                                lastDelta
                            } else {
                                deltaDuration.also {
                                    lastDelta = deltaDuration
                                }
                            }

                            Icon(
                                if (duration > 0) {
                                    Icons.Rounded.FastForward
                                } else {
                                    Icons.Rounded.FastRewind
                                },
                                null,
                                Modifier.size(iconSize),
                            )
                            val text = renderTime(duration.absoluteValue)
                            Text(
                                text,
                                maxLines = 1,
                            )
                        }

                        GestureIndicatorState.State.Volume -> {
                            Icon(
                                Icons.AutoMirrored.Rounded.VolumeUp, null,
                                Modifier.size(iconSize),
                            )
                            progressIndicator()
                        }

                        GestureIndicatorState.State.Brightness -> {
                            Icon(
                                when (state.progressValue) {
                                    in 0.67..1.0 -> Icons.Rounded.BrightnessHigh
                                    in 0.33..0.67 -> Icons.Rounded.BrightnessMedium
                                    else -> Icons.Rounded.BrightnessLow
                                },
                                null,
                                Modifier.size(iconSize),
                            )
                            progressIndicator()
                        }

                        GestureIndicatorState.State.FastForward -> {
                            Icon(Icons.Rounded.FastForward, null, Modifier.size(iconSize))
                        }

                        GestureIndicatorState.State.FastBackward -> {
                            Icon(Icons.Rounded.FastRewind, null, Modifier.size(iconSize))
                        }

                        null -> {}
                    }
                }
            }
        }
    }
}

/** 将当前平台映射为适用的播放器手势族。 */
@Stable
val Platform.mouseFamily: GestureFamily
    get() = when (this) {
        is Platform.Desktop -> GestureFamily.Mouse
        is Platform.Android, is Platform.Ios -> GestureFamily.Touch
    }

/** 描述触摸设备与桌面设备的手势策略。 */
@Immutable
enum class GestureFamily(
    /** 是否启用桌面手势布局兼容。 */
    val useDesktopGestureLayoutWorkaround: Boolean,
    /** 单击是否切换播放暂停。 */
    val clickToPauseResume: Boolean,
    /** 单击是否切换控制栏。 */
    val clickToToggleController: Boolean,
    /** 双击是否切换全屏。 */
    val doubleClickToFullscreen: Boolean,
    /** 双击是否切换播放暂停。 */
    val doubleClickToPauseResume: Boolean,
    /** 是否启用滑动跳转。 */
    val swipeToSeek: Boolean,
    /** 是否使用右侧滑动调节音量。 */
    val swipeRhsForVolume: Boolean,
    /** 是否使用左侧滑动调节亮度。 */
    val swipeLhsForBrightness: Boolean,
    /** 是否使用长按快进。 */
    val longPressForFastSkip: Boolean,
    /** 是否使用滚轮调节音量。 */
    val scrollForVolume: Boolean,
    /** 是否自动隐藏控制栏。 */
    val autoHideController: Boolean,
    /** 是否在底栏显示音量控制。 */
    val volumeControllerOnBottomBar: Boolean,
    /** 空格键是否切换播放暂停。 */
    val keyboardSpaceForPauseResume: Boolean = true,
    /** 上下键是否调节音量。 */
    val keyboardUpDownForVolume: Boolean = true,
    /** 左右键是否跳转进度。 */
    val keyboardLeftRightToSeek: Boolean = true,
    /** 鼠标悬停是否显示控制栏。 */
    val mouseHoverForController: Boolean = true, // not supported on mobile
    /** 键盘是否控制全屏。 */
    val keyboardControlFullscreen: Boolean = true,
    /** 键盘是否控制倍速。 */
    val keyboardControlSpeed: Boolean = true,
    /** 键盘是否切换弹幕。 */
    val keyboardToggleDanmaku: Boolean = true,
) {
    Touch(
        useDesktopGestureLayoutWorkaround = false,
        clickToPauseResume = false,
        clickToToggleController = true,
        doubleClickToFullscreen = false,
        doubleClickToPauseResume = true,
        swipeToSeek = true,
        swipeRhsForVolume = true,
        swipeLhsForBrightness = true,
        longPressForFastSkip = true,
        volumeControllerOnBottomBar = false,
        scrollForVolume = false,
        autoHideController = true,
        mouseHoverForController = false,
    ),
    Mouse(
        useDesktopGestureLayoutWorkaround = true,
        clickToPauseResume = true,
        clickToToggleController = false,
        doubleClickToFullscreen = true,
        doubleClickToPauseResume = false,
        swipeToSeek = false,
        swipeRhsForVolume = false,
        swipeLhsForBrightness = false,
        longPressForFastSkip = false,
        scrollForVolume = true,
        autoHideController = false,
        volumeControllerOnBottomBar = true,
    )
}

/** 鼠标移动后控制栏保持显示的时长。 */
private val VideoGestureMouseMoveShowControllerDuration = 3.seconds

/** 触摸操作后控制栏保持显示的时长。 */
private val VideoGestureTouchShowControllerDuration = 3.seconds

/** 组合播放器点击、拖动、键盘与全屏手势层。 */
@Composable
fun PlayerGestureHost(
    controllerState: PlayerControllerState,
    seekerState: SwipeSeekerState,
    progressSliderState: PlayerProgressSliderState,
    indicatorState: GestureIndicatorState,
    enableSwipeToSeek: Boolean,
    audioController: LevelController,
    brightnessController: LevelController,
    playbackSpeedControllerState: PlaybackSpeedControllerState?,
    modifier: Modifier = Modifier,
    fastSkipState: FastSkipState?,
    audioLevelController: AudioLevelController?,
    family: GestureFamily = currentPlatform().mouseFamily,
    isFullscreen: Boolean = false,
    onTogglePauseResume: () -> Unit = {},
    onToggleFullscreen: () -> Unit = {},
    onExitFullscreen: () -> Unit = {},
    onToggleDanmaku: () -> Unit = {},
    onDrag: (Float) -> Unit = {},
    onDragEnd: () -> Unit = {},
    danmakuGestureHost: @Composable (() -> Unit),
) {
    val onTogglePauseResumeState by rememberUpdatedState(onTogglePauseResume)

    BoxWithConstraints {
        Row(
            Modifier.align(Alignment.TopCenter)
                .padding(top = 16.dp),
        ) {
            LaunchedEffect(seekerState.deltaSeconds) {
                if (seekerState.isSeeking) {
                    indicatorState.showSeeking(seekerState.deltaSeconds)
                }
            }
            GestureIndicator(indicatorState)
        }

        // TODO: 临时解决方案, 安卓和 PC 需要不同的组件层级关系才能实现各种快捷手势
        val needWorkaroundForFocusManager = needWorkaroundForFocusManager
        if (family.useDesktopGestureLayoutWorkaround) {
            val indicatorTasker = rememberLatestTask()
            val focusRequester = remember { FocusRequester() }
            val manager = LocalFocusManager.current
            val keyboardFocus = remember { FocusRequester() } // focus 了才能用键盘快捷键

            Box(
                modifier
                    .focusRequester(keyboardFocus)
                    .ifThen(family.swipeToSeek) {
                        swipeToSeek(seekerState, Orientation.Horizontal)
                    }
                    .ifThen(family.keyboardLeftRightToSeek) {
                        onKeyboardHorizontalDirection(
                            onBackward = {
                                seekerState.onSeek(-5)
                            },
                            onForward = {
                                seekerState.onSeek(5)
                            },
                        )
                    }
                    .ifThen(family.keyboardUpDownForVolume && audioLevelController != null) {
                        if (audioLevelController == null) return@ifThen this
                        onKeyEvent {
                            if (it.type == KeyEventType.KeyUp) return@onKeyEvent false
                            val consumed = when {
                                it.isShiftPressed && it.key == ComposeKey.DirectionUp -> {
                                    audioLevelController.volumeUp(0.01f)
                                    true
                                }

                                it.isShiftPressed && it.key == ComposeKey.DirectionDown -> {
                                    audioLevelController.volumeDown(0.01f)
                                    true
                                }

                                it.key == ComposeKey.DirectionUp -> {
                                    audioLevelController.volumeUp()
                                    true
                                }

                                it.key == ComposeKey.DirectionDown -> {
                                    audioLevelController.volumeDown()
                                    true
                                }

                                else -> false
                            }
                            if (consumed) {
                                audioLevelController.setMute(false)
                                indicatorTasker.launch {
                                    indicatorState.showVolumeRange(audioLevelController.volume.value / audioLevelController.maxVolume)
                                }
                            }
                            consumed
                        }
                    }
                    .ifThen(family.keyboardSpaceForPauseResume) {
                        onKey(ComposeKey.Spacebar) {
                            onTogglePauseResumeState()
                        }
                    }
                    .ifThen(family.mouseHoverForController) {
                        val scope = rememberLatestTask()
                        // 没有人请求 alwaysOn 时自动隐藏控制器
                        LaunchedEffect(true) {
                            snapshotFlow { controllerState.alwaysOn }.collectLatest { alwaysOn ->
                                if (alwaysOn) return@collectLatest
                                snapshotFlow { controllerState.visibility != ControllerVisibility.Invisible }.collectLatest {
                                    if (!it) {
                                        delay(VideoGestureMouseMoveShowControllerDuration)
                                        controllerState.toggleFullVisible(false)
                                    }
                                }
                            }
                        }
                        // 这里不能用 hover, 因为在当控制器隐藏后, hover 状态仍然有, 于是下次移动鼠标时不会重复触发 hover 事件, 也就无法显示
                        // See test case: `mouse - mouseHoverForController - center screen twice`
                        onPointerEventMultiplatform(PointerEventType.Move) { _ ->
                            controllerState.toggleFullVisible(true)
                            keyboardFocus.requestFocus()
                            scope.launch {
                                delay(VideoGestureMouseMoveShowControllerDuration)
                                controllerState.toggleFullVisible(false)
                            }
                        }
                    }
                    .ifThen(family.keyboardControlFullscreen) {
                        onKey(ComposeKey.Escape) {
                            if (needWorkaroundForFocusManager) {
                                manager.clearFocus()
                            }
                            onExitFullscreen()
                        }.onKey(ComposeKey.F) {
                            if (needWorkaroundForFocusManager) {
                                manager.clearFocus()
                            }
                            onToggleFullscreen()
                        }
                    }
                    .ifThen(family.keyboardControlSpeed && playbackSpeedControllerState != null) {
                        if (playbackSpeedControllerState == null) return@ifThen this
                        onKey(ComposeKey.A) { playbackSpeedControllerState.speedDown() }
                            .onKey(ComposeKey.D) { playbackSpeedControllerState.speedUp() }
                            .onKey(ComposeKey.S) { playbackSpeedControllerState.reset() }
                    }
                    .ifThen(family.keyboardToggleDanmaku) {
                        onKey(ComposeKey.B, onToggleDanmaku)
                    }
                    .ifThen(family.scrollForVolume && audioLevelController != null) {
                        if (audioLevelController == null) return@ifThen this
                        onPointerEventMultiplatform(PointerEventType.Scroll) { event ->
                            event.changes.firstOrNull()?.scrollDelta?.y?.run {
                                audioLevelController.setMute(false)
                                if (this < 0) audioLevelController.volumeUp()
                                else if (this > 0) audioLevelController.volumeDown()

                                indicatorTasker.launch {
                                    indicatorState.showVolumeRange(audioLevelController.volume.value / audioLevelController.maxVolume)
                                }
                            }
                        }
                    }
                    .fillMaxSize(),
            ) {
                Box(
                    Modifier
                        .ifThen(needWorkaroundForFocusManager) {
                            onFocusEvent {
                                if (it.hasFocus) {
                                    focusRequester.requestFocus()
                                }
                            }
                        }
                        .matchParentSize()
                        .combinedClickable(
                            remember { MutableInteractionSource() },
                            indication = null,
                            onClick = remember(family) {
                                {
                                    if (family.clickToPauseResume) {
                                        onTogglePauseResumeState()
                                    }
                                    if (family.clickToToggleController) {
                                        controllerState.toggleFullVisible()
                                    }
                                }
                            },
                            onDoubleClick = remember(family, onToggleFullscreen) {
                                {
                                    if (needWorkaroundForFocusManager) {
                                        manager.clearFocus()
                                    }
                                    if (family.doubleClickToFullscreen) {
                                        onToggleFullscreen()
                                    }
                                    if (family.doubleClickToPauseResume) {
                                        onTogglePauseResumeState()
                                    }
                                }
                            },
                        ),

                    ) {

                }

                Box(Modifier.focusRequester(focusRequester).matchParentSize())

                // Render danmakuGestureHost inside the desktop branch
                Box(
                    Modifier
                        .matchParentSize()
                        .padding(vertical = 8.dp)
                ) {
                    danmakuGestureHost()
                }

                SideEffect {
                    focusRequester.requestFocus()
                }
            }
        } else {

            val focusManager by rememberUpdatedState(LocalFocusManager.current) // workaround for #288

            if (family.autoHideController) {
                LaunchedEffect(controllerState.visibility, controllerState.alwaysOn) {
                    if (controllerState.alwaysOn) return@LaunchedEffect
                    if (controllerState.visibility.bottomBar) {
                        delay(VideoGestureTouchShowControllerDuration)
                        controllerState.toggleFullVisible(false)
                    }
                }
            }

            val scope = rememberCoroutineScope()

            @Composable
            fun Modifier.combineClickableWithFamilyGesture() = this then
                    combinedClickable(
                        remember { MutableInteractionSource() },
                        indication = null,
                        onClick = remember(family) {
                            {
                                if (family.clickToPauseResume) {
                                    onTogglePauseResumeState()
                                }
                                if (family.clickToToggleController) {
                                    focusManager.clearFocus()
                                    controllerState.toggleFullVisible()
                                }
                            }
                        },
                        onDoubleClick = remember(family, onToggleFullscreen) {
                            {
                                if (family.doubleClickToFullscreen) {
                                    onToggleFullscreen()
                                }
                                if (family.doubleClickToPauseResume) {
                                    onTogglePauseResumeState()
                                }
                            }
                        },
                    )
            // 状态栏区域响应点击手势
            Box(
                Modifier.fillMaxWidth()
                    .ifThen(isFullscreen) {
                        windowInsetsTopHeight(WindowInsets.systemGestures)
                    }
                    .combineClickableWithFamilyGesture(),
            )
            Box(
                modifier
                    .testTag("VideoGestureHost")
                    .ifThen(needWorkaroundForFocusManager) {
                        onFocusEvent {
                            if (it.hasFocus) {
                                focusManager.clearFocus()
                            }
                        }
                    }
                    .combineClickableWithFamilyGesture()
                    .ifThen(family.swipeToSeek && enableSwipeToSeek) {
                        val swipeToSeekRequester =
                            rememberAlwaysOnRequester(controllerState, "swipeToSeek")
                        swipeToSeek(
                            seekerState,
                            Orientation.Horizontal,
                            onDragStarted = {
                                if (controllerState.visibility.bottomBar) {
                                    swipeToSeekRequester.request()
                                }
                                controllerState.setRequestProgressBar(swipeToSeekRequester)
                            },
                            onDragStopped = {
                                if (controllerState.visibility.bottomBar) {
                                    swipeToSeekRequester.cancelRequest()
                                }
                                controllerState.cancelRequestProgressBarVisible(swipeToSeekRequester)
                                progressSliderState.finishPreview()
                            },
                        ) {
                            progressSliderState.run {
                                if (totalDurationMillis == 0L) return@run
                                val offsetRatio =
                                    (currentPositionMillis + seekerState.deltaSeconds.times(1000)).toFloat() / totalDurationMillis
                                previewPositionRatio(offsetRatio.coerceIn(0f, 1f))
                            }
                        }
                    }
                    .ifThen(family.keyboardLeftRightToSeek) {
                        onKeyboardHorizontalDirection(
                            onBackward = {
                                seekerState.onSeek(-5)
                            },
                            onForward = {
                                seekerState.onSeek(5)
                            },
                        )
                    }
                    .ifThen(family.keyboardUpDownForVolume) {
                        audioController.let { controller ->
                            onKey(ComposeKey.DirectionUp) {
                                controller.increaseLevel(0.10f)
                            }
                            onKey(ComposeKey.DirectionDown) {
                                controller.decreaseLevel(0.10f)
                            }
                        }
                    }
                    .ifThen(family.keyboardSpaceForPauseResume) {
                        onKey(ComposeKey.Spacebar) {
                            onTogglePauseResumeState()
                        }
                    }
                    .ifThen(family.longPressForFastSkip) {
                        fastSkipState?.let {
                            longPressFastSkip(it, SkipDirection.FORWARD)
                        }
                    }
                    .pointerInput(Unit) {
                        var startPointX = 0f
                        detectVerticalDragGestures(
                            onDragStart = { startPointX = it.x },
                            onDragEnd = { onDragEnd() }
                        ) { point, change ->

                            /**
                             * 左边亮度控制 （0%～40%）
                             * 中间触发画中画 （40%～60%）
                             * 右边音量控制 （60%～100%）
                             *
                             */
                            if (family.swipeLhsForBrightness && startPointX < size.width * 0.4f) {
                                // Left area - brightness control
                                val changeLevel =
                                    brightnessController.level + -(change / size.height)
                                brightnessController.setLevel(changeLevel)
                                scope.launch { indicatorState.showBrightnessRange(changeLevel) }
                            } else if (family.swipeRhsForVolume && startPointX > size.width * 0.6f) {
                                // Right area - volume control
                                val changeLevel = audioController.level + -(change / size.height)
                                audioController.setLevel(changeLevel)
                                scope.launch { indicatorState.showVolumeRange(changeLevel) }
                            } else {
                                // Trigger PiP mode when swiping down in the middle area
                                onDrag(change)
                            }
                        }
                    }
                    .fillMaxSize(),
            ) {
                // Render danmakuGestureHost inside the mobile branch
                Box(
                    Modifier
                        .matchParentSize()
                        .padding(vertical = 8.dp)
                ) {
                    danmakuGestureHost()
                }
            }


        }
    }
}

/** 根据垂直拖动距离计算新的层级值。 */
private fun calculateLevelFromHeight(
    currentLevel: Float,
    heightChange: Float,
    containerHeight: Float,
    sensitivity: Float = 1.0f
): Float {
    // 将高度变化转换为level变化（0.0-1.0范围）
    val levelChange = (heightChange / containerHeight) * sensitivity

    // 返回新的level，确保在有效范围内
    return (currentLevel - levelChange).coerceIn(0.0f, 1.0f)
}
