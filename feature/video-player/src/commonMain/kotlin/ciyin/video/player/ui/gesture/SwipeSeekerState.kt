package ciyin.video.player.ui.gesture

import androidx.annotation.UiThread
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.geometry.Offset
import kotlinx.coroutines.CoroutineScope
import kotlin.math.roundToInt


/** 记住与媒体进度绑定的滑动跳转状态。 */
@Composable
fun rememberSwipeSeekerState(
    screenWidthPx: Int,
    swipeSeekerConfig: SwipeSeekerConfig = SwipeSeekerConfig.Default,
    @UiThread onSeek: (offsetSeconds: Int) -> Unit,
): SwipeSeekerState {
    val onSeekState by rememberUpdatedState(onSeek)
    return remember(swipeSeekerConfig, screenWidthPx) {
        SwipeSeekerState(
            screenWidthPx,
            swipeSeekerConfig,
        ) { onSeekState(it) }
    }
}

/** 配置滑动跳转的灵敏度与边界。 */
@Immutable
data class SwipeSeekerConfig(
    /**
     * 从屏幕左边滑到屏幕的最右边的最大距离
     */
    val maxDragDelta: Float = 0f,
    /**
     * 从屏幕左边滑到屏幕的最右边会跳转的秒数
     */
    // 设计上是从左到右 90 秒正好跳过 op/ed, 而全面屏手机有全面屏手势,
    // 用户不能从最左边开始滑. 因此稍微留了点余量.
    // 实测差不多可以滑到 87 秒, 看三秒 op 让他知道他完了 op
    val maxDragSeconds: Int = 97,
) {
    companion object {
        /** 默认滑动跳转配置。 */
        val Default = SwipeSeekerConfig()
    }
}

/** 根据水平拖动计算并提交媒体跳转位置。 */
@Stable
class SwipeSeekerState(
    /**
     * 可滑动区域宽度
     */
    private val screenWidthPx: Int,
    /** 滑动跳转参数。 */
    private val swipeSeekerConfig: SwipeSeekerConfig = SwipeSeekerConfig.Default,
    /**
     * 当一次滑动结束时的回调. `offsetSeconds` 为本次快进的秒数
     */
    @UiThread val onSeek: (offsetSeconds: Int) -> Unit,
) {
    /**
     * [Float.NaN] iff not dragging
     */
    private var seekDelta: Float by mutableFloatStateOf(Float.NaN)

    /** 开始滑动并重置位置增量。 */
    @UiThread
    private fun onSwipeStarted() {
        seekDelta = 0f
    }

    /** 结束滑动并提交位置增量。 */
    @UiThread
    private fun onSwipeStopped() {
        if (seekDelta.isNaN()) return
        onSeek(deltaSeconds)
        seekDelta = Float.NaN
    }

    /** 根据水平位移更新预览位置。 */
    @UiThread
    private fun onSwipeOffset(offsetPx: Float) {
        seekDelta += offsetPx
    }

    /**
     * 是否正在快进, 即用户是否正在滑动屏幕
     */
    val isSeeking: Boolean by derivedStateOf {
        !seekDelta.isNaN()
    }

    /**
     * 当前正在快进的秒数.
     *
     * 当用户手指在屏幕上滑动时, [deltaSeconds] 将更新, 反映假如用户此时松开手指, 将会跳转的秒数.
     * - 若用户从屏幕左边滑到屏幕的右边, [deltaSeconds] 将会是 [SwipeSeekerConfig.maxDragSeconds].
     *
     * 当未在滑动时, [deltaSeconds] 为 `0`.
     *
     * 负数表示快退, 正数表示快进
     */
    val deltaSeconds: Int by derivedStateOf {
        if (seekDelta.isNaN()) {
            0
        } else {
            val percentage = seekDelta / screenWidthPx
            (percentage * swipeSeekerConfig.maxDragSeconds).roundToInt()
        }
    }


    companion object {
        fun Modifier.swipeToSeek(
            seekerState: SwipeSeekerState,
            orientation: Orientation,
            enabled: Boolean = true,
            interactionSource: MutableInteractionSource? = null,
            reverseDirection: Boolean = false,
            onDragStarted: suspend CoroutineScope.(startedPosition: Offset) -> Unit = {},
            onDragStopped: suspend CoroutineScope.(velocity: Float) -> Unit = {},
            onDelta: (Float) -> Unit = {},
        ): Modifier {
            return composed(
                inspectorInfo = {
                    name = "videoSeeker"
                    properties["seekerState"] = seekerState
                },
            ) {
                draggable(
                    rememberDraggableState {
                        seekerState.onSwipeOffset(it)
                        onDelta(it)
                    },
                    orientation,
                    onDragStarted = {
                        seekerState.onSwipeStarted()
                        onDragStarted(it)
                    },
                    onDragStopped = {
                        seekerState.onSwipeStopped()
                        onDragStopped(it)
                    },
                    enabled = enabled,
                    interactionSource = interactionSource,
                    reverseDirection = reverseDirection,
                )
            }
        }
    }
}
