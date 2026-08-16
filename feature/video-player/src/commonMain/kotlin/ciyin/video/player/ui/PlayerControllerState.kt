package ciyin.video.player.ui

import androidx.compose.runtime.*
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Modifier
import ciyin.video.player.ui.internal.hoverable


/**
 * @param initialVisibility 变更不会更新
 */
@Composable
fun rememberVideoControllerState(
    initialVisibility: ControllerVisibility = PlayerControllerState.DefaultInitialVisibility
): PlayerControllerState {
    return remember {
        PlayerControllerState(initialVisibility)
    }
}

/** 描述控制栏各部分是否可见。 */
@Immutable
data class ControllerVisibility(
    /** 是否显示顶部控制栏。 */
    val topBar: Boolean,
    /** 是否显示底部控制栏。 */
    val bottomBar: Boolean,
    /** 是否显示右下角浮动控制区。 */
    val floatingBottomEnd: Boolean,
    /** 是否显示右侧控制区。 */
    val rhsBar: Boolean,
    /** 是否显示手势锁。 */
    val gestureLock: Boolean,
    /** 是否只显示独立进度条。 */
    val detachedSlider: Boolean
) {
    companion object {
        @Stable
        val Visible = ControllerVisibility(
            topBar = true,
            bottomBar = true,
            floatingBottomEnd = false,
            rhsBar = true,
            gestureLock = true,
            detachedSlider = false,
        )

        @Stable
        val Invisible = ControllerVisibility(
            topBar = false,
            bottomBar = false,
            floatingBottomEnd = true,
            rhsBar = false,
            gestureLock = false,
            detachedSlider = false,
        )

        @Stable
        val DetachedSliderOnly = ControllerVisibility(
            topBar = false,
            bottomBar = false,
            floatingBottomEnd = false,
            rhsBar = false,
            gestureLock = false,
            detachedSlider = true,
        )
    }
}

/** 管理播放器控制栏的显隐请求与自动隐藏计时。 */
@Stable
class PlayerControllerState(
    initialVisibility: ControllerVisibility = DefaultInitialVisibility
) {
    companion object {
        /** 控制栏默认从隐藏状态开始。 */
        val DefaultInitialVisibility = ControllerVisibility.Invisible
    }

    /** 完整控制栏当前是否可见。 */
    private var fullVisible by mutableStateOf(initialVisibility == ControllerVisibility.Visible)

    /** 是否存在进度条常显请求。 */
    private val hasProgressBarRequester by derivedStateOf { progressBarRequesters.isNotEmpty() }

    /**
     * 当前 UI 应当显示的状态
     */
    val visibility: ControllerVisibility by derivedStateOf {
        // 根据 hasProgressBarRequester, alwaysOn 和 fullVisible 计算正确的 `ControllerVisibility`
        if (alwaysOn) return@derivedStateOf ControllerVisibility.Visible
        if (fullVisible) return@derivedStateOf ControllerVisibility.Visible
        if (hasProgressBarRequester) return@derivedStateOf ControllerVisibility.DetachedSliderOnly
        ControllerVisibility.Invisible
    }

    /**
     * 切换显示或隐藏整个控制器.
     *
     * 此操作拥有比 [setRequestProgressBar] 更低的优先级.
     * 如果此时有人请求显示进度条, `toggleEntireVisible(false)` 将会延迟到那个人取消请求后才隐藏进度条.
     * 如果此时没有人请求显示进度条, 此函数将立即生效.
     *
     * @param visible 为 `true` 时显示整个控制器
     */
    fun toggleFullVisible(visible: Boolean? = null) {
        fullVisible = visible ?: !fullVisible
    }

    /** 直接设置完整控制栏可见性的回调。 */
    val setFullVisible: (visible: Boolean) -> Unit = {
        fullVisible = it
    }

    /** 请求完整控制栏常显的调用方。 */
    private val alwaysOnRequests = SnapshotStateList<Any>()

    /**
     * 总是显示. 也就是不要在 3 秒后自动隐藏.
     */
    val alwaysOn: Boolean by derivedStateOf {
        alwaysOnRequests.isNotEmpty()
    }

    /**
     * 请求控制器总是显示.
     */
    fun setRequestAlwaysOn(requester: Any, isAlwaysOn: Boolean) {
        if (isAlwaysOn) {
            if (requester in alwaysOnRequests) return
            alwaysOnRequests.add(requester)
        } else {
            alwaysOnRequests.remove(requester)
        }
    }

    /** 请求进度条常显的调用方。 */
    private val progressBarRequesters = SnapshotStateList<Any>()

    /**
     * 请求显示进度条
     * 当目前没有显示进度条时, 将显示独立的进度条.
     * 若目前已经有进度条, 则会保持该状态, 防止自动关闭.
     *
     * @param requester 是谁希望请求显示进度条. 在 [cancelRequestProgressBarVisible] 时需要传入相同实例. 同一时刻有任一 requester 则会让进度条一直显示.
     */
    fun setRequestProgressBar(requester: Any) {
        if (requester in progressBarRequesters) return
        progressBarRequesters.add(requester)
    }

    /**
     * 取消显示进度条
     */
    fun cancelRequestProgressBarVisible(requester: Any) {
        progressBarRequesters.remove(requester)
    }

    /** 返回当前请求控制器常驻的对象，仅用于聚焦测试。 */
    internal fun getAlwaysOnRequesters(): List<Any> {
        return alwaysOnRequests
    }
}

/** 在交互期间请求控制栏保持显示。 */
interface AlwaysOnRequester {
    /** 请求控制栏保持显示。 */
    fun request()

    /** 取消控制栏常显请求。 */
    fun cancelRequest()
}

/** 记住绑定到指定控制器的常显请求器。 */
@Composable
fun rememberAlwaysOnRequester(
    controllerState: PlayerControllerState,
    debugName: String
): AlwaysOnRequester {
    val requester = remember(controllerState, debugName) {
        object : AlwaysOnRequester {
            override fun request() {
                controllerState.setRequestAlwaysOn(this, true)
            }

            override fun cancelRequest() {
                controllerState.setRequestAlwaysOn(this, false)
            }

            override fun toString(): String {
                return "AlwaysOnRequester($debugName)"
            }
        }
    }
    DisposableEffect(requester) {
        onDispose {
            requester.cancelRequest()
        }
    }
    return requester
}

/** 悬停时让控制栏保持显示。 */
fun Modifier.hoverToRequestAlwaysOn(
    requester: AlwaysOnRequester
): Modifier = hoverable(
    onHover = {
        requester.request()
    },
    onUnhover = {
        requester.cancelRequest()
    },
)
